package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCObjectInspection;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.ir.IRPrettyPrinter;
import io.github.protasm.jvmud.compiler.ir.TypedIR;
import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.visitor.PrintVisitor;
import io.github.protasm.jvmud.compiler.pipeline.CompilationObserver;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.pipeline.CompilationUnit;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.scanner.Scanner;
import io.github.protasm.jvmud.compiler.token.Token;
import io.github.protasm.jvmud.compiler.token.TokenList;
import io.github.protasm.jvmud.instance.MudlibBoot;
import io.github.protasm.jvmud.instance.MudlibBootResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/** Local JVMud administration shell backed by the real runtime. */
public final class AdminCli {
    private static final Path DEFAULT_CONFIG_FILE = Path.of("mudlibs", "lpmuseum", "jvmud", "lpmuseum.config");

    private final PrintWriter out;
    private final Map<String, Object> handles = new java.util.LinkedHashMap<>();
    private final Map<Object, String> objectNames = new IdentityHashMap<>();
    private LPCRuntime runtime;
    private Path mudlibRoot;
    private Path virtualCwd = Path.of("");
    private Verbosity verbosity = Verbosity.NORMAL;
    private boolean suppressCompilationFailures;
    private boolean running = true;

    public AdminCli(PrintWriter out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: scripts/jvmud-admin [mudlib-config-file]");
        }
        PrintWriter out = new PrintWriter(System.out, true);
        AdminCli cli = new AdminCli(out);
        Path configFile = args.length == 1 ? Path.of(args[0]) : DEFAULT_CONFIG_FILE;
        cli.bootConfig(configFile);
        cli.run(new BufferedReader(new InputStreamReader(System.in)));
    }

    public void run(BufferedReader in) throws IOException {
        out.println("JVMud admin CLI. Type 'help' for commands.");
        while (running) {
            out.print("jvmud> ");
            out.flush();
            String line = in.readLine();
            if (line == null) {
                break;
            }
            execute(line);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void execute(String line) {
        if (line == null || line.isBlank()) {
            return;
        }

        CommandLine command = CommandLine.parse(line);
        if (!command.isBlank()) {
            executeAdminCommand(command);
        }
    }

    private void executeAdminCommand(CommandLine command) {
        String canonicalName = canonicalCommand(command.name());

        try {
            switch (canonicalName) {
            case "help" -> help();
            case "boot" -> bootConfig(command.pathArgument(0, DEFAULT_CONFIG_FILE));
            case "pwd" -> pwd();
            case "cd" -> cd(command.optional(0, "/"));
            case "ls" -> ls(command.optional(0, "."));
            case "cat" -> cat(command.required(0));
            case "verbosity" -> verbosity(command.optional(0, ""));
            case "preprocess" -> preprocess(command.required(0));
            case "scan" -> scan(command.required(0));
            case "parse" -> parse(command.required(0));
            case "ir" -> ir(command.required(0));
            case "compile" -> compile(command.required(0));
            case "load" -> load(command.required(0));
            case "reload" -> reload(command.required(0));
            case "clone" -> clone(command.required(0));
            case "call" -> call(command.required(0), command.required(1), command.argumentsAfter(2));
            case "move" -> move(command.required(0), command.required(1));
            case "look" -> look(command.required(0));
            case "objects" -> objects();
            case "where" -> where(command.required(0));
            case "inspect" -> inspect(command.required(0));
            case "destruct" -> destruct(command.required(0));
            case "quit", "exit" -> running = false;
            default -> {
                out.println("Unknown command: " + command.name());
                out.println("Usage: help");
            }
            }
        } catch (RuntimeException e) {
            out.println("Error: " + e.getMessage());
            String syntax = syntaxFor(canonicalName);
            if (syntax != null) {
                out.println("Usage: " + syntax);
            }
        }
    }

    public void boot(Path mudlibRoot) {
        boot(mudlibRoot, MudlibBoot.DEFAULT_CONFIG_PATH);
    }

    public void bootConfig(Path configFile) {
        Path resolvedConfigFile = resolveConfigFile(configFile);
        Path mudlibRoot = mudlibRootForConfigFile(resolvedConfigFile);
        String configObjectPath = mudlibRoot.relativize(resolvedConfigFile).toString()
                .replace('\\', '/');
        boot(mudlibRoot, configObjectPath);
    }

    public void boot(Path mudlibRoot, String configObjectPath) {
        this.mudlibRoot = mudlibRoot.toAbsolutePath().normalize();
        this.virtualCwd = Path.of("");
        runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(this.mudlibRoot)
                .compilationObserver(new CliCompilationObserver())
                .build());
        CoreEfuns.registerCore(runtime);
        handles.clear();
        objectNames.clear();
        info("Booted runtime with mudlib root " + this.mudlibRoot);

        suppressCompilationFailures = true;
        MudlibBootResult bootResult;
        try {
            bootResult = new MudlibBoot(runtime, this.mudlibRoot, configObjectPath, false, false).boot();
        } finally {
            suppressCompilationFailures = false;
        }
        for (String objectId : bootResult.preloadedObjects()) {
            remember(objectId, runtime.loadOrGetObject(objectId));
        }
        if (bootResult.startingRoom() != null) {
            remember(bootResult.startingRoom(), runtime.loadOrGetObject(bootResult.startingRoom()));
        }
        if (!bootResult.preloadedObjects().isEmpty()) {
            info("Preloaded " + bootResult.preloadedObjects().size() + " startup object(s).");
        }
        if (!bootResult.skippedPreloads().isEmpty()) {
            info("Skipped " + bootResult.skippedPreloads().size() + " startup object(s) not yet supported.");
        }
    }

    private static Path mudlibRootForConfigFile(Path configFile) {
        Path normalized = resolveConfigFile(configFile);
        Path configDir = normalized.getParent();
        if (configDir == null) {
            throw new IllegalArgumentException("Config file must have a parent directory: " + configFile);
        }
        if ("jvmud".equals(configDir.getFileName().toString())) {
            Path root = configDir.getParent();
            if (root == null) {
                throw new IllegalArgumentException("Config file must live inside a mudlib root: " + configFile);
            }
            return root;
        }
        return configDir;
    }

    private static Path resolveConfigFile(Path configFile) {
        if (configFile.isAbsolute()) {
            return configFile.normalize();
        }
        return launchRoot().resolve(configFile).normalize();
    }

    private static Path launchRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (isRepositoryRoot(current)) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    private static boolean isRepositoryRoot(Path path) {
        return Files.exists(path.resolve("pom.xml")) && Files.isDirectory(path.resolve("mudlibs"));
    }

    private void help() {
        out.println("Admin commands:");
        helpLine("h", "help", "Show this command reference.");
        helpLine("b", "boot [mudlib-config-file]", "Start a fresh mudlib sandbox without a player session.");
        helpLine("", "call <handle> <method> [args...]", "Invoke a method on a loaded object handle.");
        helpLine("", "cat <path>", "Print a file from the virtual mudlib filesystem.");
        helpLine("", "cd [path]", "Change the current virtual mudlib directory.");
        helpLine("", "clone <path>", "Compile if needed and create a new LPC object instance.");
        helpLine("c", "compile <path>", "Compile an LPC source file to bytecode without loading it.");
        helpLine("x", "destruct <handle>", "Remove an object from the runtime.");
        helpLine("i", "inspect <handle>", "Show object state, inventory, environment, and methods.");
        helpLine("ir", "ir <path>", "Compile through lowering and print the typed IR.");
        helpLine("l", "load <path>", "Compile, load, and register an LPC object.");
        helpLine("k", "look <handle>", "Call long() or short() and display object text.");
        helpLine("", "ls [path]", "List a virtual mudlib directory or file.");
        helpLine("m", "move <handle> <dest>", "Move one object handle into another object's inventory.");
        helpLine("o", "objects", "List known CLI object handles.");
        helpLine("p", "parse <path>", "Preprocess, scan, parse, and print the LPC AST.");
        helpLine("pp", "preprocess <path>", "Expand includes, macros, and preprocessor directives.");
        helpLine("", "pwd", "Print the current virtual mudlib directory.");
        helpLine("r", "reload <path>", "Recompile and replace a loaded LPC object.");
        helpLine("s", "scan <path>", "Preprocess and print scanner tokens for an LPC source file.");
        helpLine("v", "verbosity [quiet|normal|watch]", "Show or change compiler/shell output detail.");
        helpLine("w", "where <handle>", "Show the environment containing an object.");
        helpLine("q", "quit", "Exit the local shell.");
    }

    private void helpLine(String alias, String command, String description) {
        out.printf("  %-2s %-35s %s%n", alias, command, description);
    }

    private void verbosity(String value) {
        if (value == null || value.isBlank()) {
            out.println("verbosity is " + verbosity.name().toLowerCase());
            return;
        }

        verbosity = Verbosity.parse(value);
        out.println("verbosity set to " + verbosity.name().toLowerCase());
    }

    private void pwd() {
        ensureBooted();
        out.println(virtualDisplayPath(virtualCwd));
    }

    private void cd(String path) {
        ensureBooted();
        Path target = resolveVirtualPath(path);
        if (!Files.isDirectory(target)) {
            throw new IllegalArgumentException("Not a directory: " + path);
        }
        virtualCwd = mudlibRoot.relativize(target);
        out.println(virtualDisplayPath(virtualCwd));
    }

    private void ls(String path) {
        ensureBooted();
        Path target = resolveVirtualPath(path);
        if (Files.isDirectory(target)) {
            try (Stream<Path> entries = Files.list(target)) {
                entries.sorted().forEach(entry -> out.println(displayName(entry)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not list: " + path, e);
            }
            return;
        }

        if (Files.exists(target)) {
            out.println(displayName(target));
            return;
        }

        throw new IllegalArgumentException("No such file or directory: " + path);
    }

    private void cat(String path) {
        ensureBooted();
        Path target = resolveVirtualPath(path);
        if (!Files.isRegularFile(target)) {
            throw new IllegalArgumentException("Not a file: " + path);
        }

        try {
            int lineNumber = 1;
            for (String line : Files.readAllLines(target)) {
                out.printf("%4d  %s%n", lineNumber, line);
                lineNumber++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read: " + path, e);
        }
    }

    private void preprocess(String path) {
        SourceInput input = readSourceInput(path);
        Preprocessor preprocessor = new Preprocessor(includeResolver());
        out.print(preprocessor.preprocessWithMapping(input.sourcePath(), input.source(), input.displayPath()).source());
    }

    private void scan(String path) {
        SourceInput input = readSourceInput(path);
        Scanner scanner = new Scanner(new Preprocessor(includeResolver()));
        TokenList tokens = scanner.scan(input.sourcePath(), input.source(), input.displayPath());
        for (int i = 0; i < tokens.size(); i++) {
            Token<?> token = tokens.get(i);
            out.printf("%4d  %-24s %-20s line %d%n",
                    i,
                    token.type(),
                    token.lexeme() == null ? "" : token.lexeme(),
                    token.line());
        }
    }

    private void parse(String path) {
        SourceInput input = readSourceInput(path);
        Scanner scanner = new Scanner(new Preprocessor(includeResolver()));
        TokenList tokens = scanner.scan(input.sourcePath(), input.source(), input.displayPath());
        Parser parser = new Parser(ParserOptions.defaults());
        ASTObject astObject = parser.parse(input.sourceName(), tokens);
        astObject.accept(new PrintVisitor(out));
    }

    private void compile(String path) {
        SourceInput input = readSourceInput(path);
        CompilationResult result = compileSource(input);

        if (!result.getProblems().isEmpty()) {
            reportProblems(result.getProblems());
            return;
        }

        byte[] bytecode = result.getBytecode();
        if (bytecode == null) {
            out.println("Compilation did not produce bytecode.");
            return;
        }

        String internalName = result.getAstObject() != null
                ? result.getAstObject().name()
                : input.sourceName();
        out.println("Compiled " + input.sourceName() + " as " + internalName + " (" + bytecode.length + " bytes).");
    }

    private void ir(String path) {
        SourceInput input = readSourceInput(path);
        CompilationResult result = compileSource(input);

        if (result.getTypedIr() == null) {
            reportProblems(result.getProblems());
            if (result.getProblems().isEmpty()) {
                out.println("Compilation did not produce typed IR.");
            }
            return;
        }

        TypedIR typedIr = result.getTypedIr();
        out.print(IRPrettyPrinter.format(typedIr));
        if (!result.getProblems().isEmpty()) {
            reportProblems(result.getProblems());
        }
    }

    private CompilationResult compileSource(SourceInput input) {
        ensureBooted();
        return runtime.compile(input.sourcePath());
    }

    private void load(String path) {
        ensureBooted();
        LPCObjectHandle handle = runtime.load(resolveVirtualPath(path));
        remember(handle.internalName(), handle.instance());
        info("Loaded " + handle.internalName());
    }

    private void reload(String path) {
        ensureBooted();
        String runtimePath = runtimePath(path);
        Object previous = handles.remove(runtimePath);
        if (previous != null) {
            objectNames.remove(previous);
        }

        LPCObjectHandle handle = runtime.reload(resolveVirtualPath(path));
        remember(handle.internalName(), handle.instance());
        info("Reloaded " + handle.internalName());
    }

    private void clone(String path) {
        ensureBooted();
        String runtimePath = runtimePath(path);
        Object object = runtime.cloneObject(runtimePath);
        String handle = nextHandle(runtimePath);
        remember(handle, object);
        info("Cloned " + runtimePath + " as " + handle);
    }

    private void call(String handle, String method, String[] args) {
        ensureBooted();
        Object object = object(handle);
        Object result = invoke(object, method, args);
        String output = consumeOutput();
        if (!output.isEmpty()) {
            out.print(output);
            if (!output.endsWith("\n")) {
                out.println();
            }
        }
        out.println("=> " + result);
    }

    private void move(String handle, String destinationHandle) {
        ensureBooted();
        Object object = object(handle);
        Object destination = object(destinationHandle);
        runtime.moveObject(object, destination);
        info("Moved " + handle + " to " + destinationHandle);
    }

    private void look(String handle) {
        ensureBooted();
        Object result = lookAt(object(handle));
        if (result != null && !Integer.valueOf(0).equals(result)) {
            out.println(result);
        }
    }

    private Object lookAt(Object object) {
        Object result = invokeFirstAvailable(object, "longWithArgument", "long", "short");
        String output = consumeOutput();
        if (!output.isEmpty()) {
            out.print(output);
            if (!output.endsWith("\n")) {
                out.println();
            }
        }
        return result;
    }

    private void objects() {
        ensureBooted();
        if (handles.isEmpty()) {
            out.println("(no objects)");
            return;
        }
        handles.forEach((name, object) -> {
            LPCObjectInspection inspection = runtime.inspectObject(object);
            out.println(name + " : " + nullText(inspection.objectId()));
        });
    }

    private void where(String handle) {
        ensureBooted();
        Object environment = runtime.environment(object(handle));
        out.println(handle + " is in " + (environment == null ? "(nowhere)" : objectName(environment)));
    }

    private void inspect(String handle) {
        ensureBooted();
        LPCObjectInspection inspection = runtime.inspectObject(object(handle));
        out.println(handle + " : " + nullText(inspection.objectId()));
        out.println("  runtime id: " + nullText(inspection.objectId()));
        out.println("  environment: " + nullText(inspection.environmentId()));
        out.println("  inventory:");
        if (inspection.inventoryIds().isEmpty()) {
            out.println("    (empty)");
        } else {
            inspection.inventoryIds().forEach(item -> out.println("    " + item));
        }
        out.println("  fields:");
        if (inspection.fields().isEmpty()) {
            out.println("    (none)");
        } else {
            inspection.fields().forEach(field -> out.println("    "
                    + field.type() + " " + field.name() + " = " + field.value()
                    + "  [" + field.ownerName() + "]"));
        }
        out.println("  methods:");
        if (inspection.methods().isEmpty()) {
            out.println("    (none)");
        } else {
            inspection.methods().forEach(method -> out.println("    "
                    + method.returnType() + " " + method.name() + "("
                    + String.join(", ", method.parameterTypes()) + ")"
                    + "  [" + method.ownerName() + "]"));
        }
    }

    private void destruct(String handle) {
        ensureBooted();
        Object object = object(handle);
        runtime.destructObject(object);
        handles.remove(handle);
        objectNames.remove(object);
        info("Destructed " + handle);
    }

    private Object invokeFirstAvailable(Object object, String... methods) {
        RuntimeException last = null;
        for (String method : methods) {
            try {
                if ("longWithArgument".equals(method)) {
                    return runtime.invokeObject(object, "long", new Object[] {null});
                }
                return invoke(object, method, new String[0]);
            } catch (RuntimeException e) {
                last = e;
            }
        }
        throw last != null ? last : new IllegalArgumentException("No method supplied.");
    }

    private Object invoke(Object object, String method, String[] args) {
        return runtime.invokeObject(object, method, (Object[]) args);
    }

    private String consumeOutput() {
        String output = runtime.outputTranscript();
        runtime.clearOutputTranscript();
        return output;
    }

    private Object object(String handle) {
        Object object = handles.get(handle);
        if (object == null) {
            throw new IllegalArgumentException("Unknown object handle: " + handle);
        }
        return object;
    }

    private String objectName(Object object) {
        String name = objectNames.get(object);
        if (name != null) {
            return name;
        }
        LPCObjectInspection inspection = runtime.inspectObject(object);
        return inspection.objectId() == null ? object.toString() : inspection.objectId();
    }

    private void remember(String handle, Object object) {
        handles.put(handle, object);
        objectNames.put(object, handle);
    }

    private String nextHandle(String base) {
        String normalized = stripExtension(base);
        String candidate = normalized;
        int suffix = 1;
        while (handles.containsKey(candidate)) {
            candidate = normalized + "#" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String stripExtension(String value) {
        int dot = value.lastIndexOf('.');
        return dot == -1 ? value : value.substring(0, dot);
    }

    private void ensureBooted() {
        if (runtime == null) {
            boot(Path.of("mudlibs", "lp245"));
        }
    }

    private String canonicalCommand(String name) {
        return switch (name) {
        case "h" -> "help";
        case "b" -> "boot";
        case "v" -> "verbosity";
        case "pp" -> "preprocess";
        case "s" -> "scan";
        case "p" -> "parse";
        case "c" -> "compile";
        case "l" -> "load";
        case "r" -> "reload";
        case "i" -> "inspect";
        case "m" -> "move";
        case "k" -> "look";
        case "o" -> "objects";
        case "w" -> "where";
        case "x" -> "destruct";
        case "q" -> "quit";
        default -> name;
        };
    }

    private String syntaxFor(String command) {
        return switch (command) {
        case "help" -> "help";
        case "boot" -> "boot [mudlib-config-file]";
        case "call" -> "call <handle> <method> [args...]";
        case "cat" -> "cat <path>";
        case "cd" -> "cd [path]";
        case "clone" -> "clone <path>";
        case "compile" -> "compile <path>";
        case "destruct" -> "destruct <handle>";
        case "inspect" -> "inspect <handle>";
        case "ir" -> "ir <path>";
        case "load" -> "load <path>";
        case "look" -> "look <handle>";
        case "ls" -> "ls [path]";
        case "move" -> "move <handle> <dest>";
        case "objects" -> "objects";
        case "parse" -> "parse <path>";
        case "preprocess" -> "preprocess <path>";
        case "pwd" -> "pwd";
        case "reload" -> "reload <path>";
        case "scan" -> "scan <path>";
        case "verbosity" -> "verbosity [quiet|normal|watch]";
        case "where" -> "where <handle>";
        case "quit", "exit" -> "quit";
        default -> null;
        };
    }

    private void info(String message) {
        if (verbosity != Verbosity.QUIET) {
            out.println(message);
        }
    }

    private Path resolveVirtualPath(String path) {
        Path requested = Path.of(path);
        Path resolved = requested.isAbsolute()
                ? mudlibRoot.resolve(stripLeadingSlash(path))
                : mudlibRoot.resolve(virtualCwd).resolve(requested);
        resolved = resolved.normalize();

        if (!resolved.startsWith(mudlibRoot)) {
            throw new IllegalArgumentException("Path escapes mudlib root: " + path);
        }
        return resolved;
    }

    private String runtimePath(String path) {
        Path resolved = resolveVirtualPath(path);
        String relative = mudlibRoot.relativize(resolved).toString().replace('\\', '/');
        return stripExtension(relative);
    }

    private SourceInput readSourceInput(String path) {
        ensureBooted();
        Path sourcePath = resolveSourceFile(path);
        try {
            String source = Files.readString(sourcePath);
            String sourceName = stripExtension(mudlibRoot.relativize(sourcePath).toString().replace('\\', '/'));
            String displayPath = "/" + sourceName;
            return new SourceInput(sourcePath, sourceName, displayPath, source);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read source file: " + path, e);
        }
    }

    private Path resolveSourceFile(String path) {
        Path resolved = resolveVirtualPath(path);
        if (Files.isRegularFile(resolved)) {
            return resolved;
        }

        Path withExtension = resolved.resolveSibling(resolved.getFileName() + ".c");
        if (Files.isRegularFile(withExtension)) {
            return withExtension;
        }

        throw new IllegalArgumentException("No such source file: " + path);
    }

    private SearchPathIncludeResolver includeResolver() {
        return new SearchPathIncludeResolver(mudlibRoot, List.of());
    }

    private void reportProblems(List<CompilationProblem> problems) {
        for (CompilationProblem problem : problems) {
            StringBuilder message = new StringBuilder();
            message.append(problem.getStage()).append(": ").append(problem.getMessage());
            if (problem.getLine() != null) {
                message.append(" (line ").append(problem.getLine()).append(")");
            }
            Throwable throwable = problem.getThrowable();
            if (throwable != null && throwable.getMessage() != null) {
                message.append(" - ").append(throwable.getMessage());
            }
            out.println(message);
        }
    }

    private String stripLeadingSlash(String path) {
        String stripped = path;
        while (stripped.startsWith("/") || stripped.startsWith("\\")) {
            stripped = stripped.substring(1);
        }
        return stripped;
    }

    private String virtualDisplayPath(Path relativePath) {
        String path = relativePath.toString().replace('\\', '/');
        return path.isEmpty() ? "/" : "/" + path;
    }

    private String displayName(Path path) {
        String name = path.getFileName().toString();
        return Files.isDirectory(path) ? name + "/" : name;
    }

    private String nullText(String value) {
        return value == null ? "(none)" : value;
    }

    private record SourceInput(Path sourcePath, String sourceName, String displayPath, String source) {
    }

    private enum Verbosity {
        QUIET,
        NORMAL,
        WATCH;

        static Verbosity parse(String value) {
            for (Verbosity level : values()) {
                if (level.name().equalsIgnoreCase(value)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Unknown verbosity: " + value);
        }
    }

    private final class CliCompilationObserver implements CompilationObserver {
        @Override
        public void stageStarted(CompilationUnit unit, CompilationStage stage) {
            if (verbosity == Verbosity.WATCH) {
                out.println("[compile] " + unit.parseName() + " " + stage.name().toLowerCase() + "...");
            }
        }

        @Override
        public void stageSucceeded(CompilationUnit unit, CompilationStage stage) {
            if (verbosity == Verbosity.WATCH) {
                out.println("[compile] " + unit.parseName() + " " + stage.name().toLowerCase() + " ok");
            }
        }

        @Override
        public void stageFailed(CompilationUnit unit, CompilationStage stage, CompilationProblem problem) {
            if (verbosity != Verbosity.QUIET && !suppressCompilationFailures) {
                out.println("[compile] " + unit.parseName() + " " + stage.name().toLowerCase()
                        + " failed: " + problem.getMessage());
            }
        }
    }
}
