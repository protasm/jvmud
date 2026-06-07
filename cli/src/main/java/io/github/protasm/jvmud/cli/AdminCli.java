package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCObjectInspection;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.pipeline.CompilationObserver;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.pipeline.CompilationUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/** Local JVMud administration shell backed by the real runtime. */
public final class AdminCli {
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
        PrintWriter out = new PrintWriter(System.out, true);
        AdminCli cli = new AdminCli(out);
        Path mudlib = (args.length > 0) ? Path.of(args[0]) : Path.of("mudlib");
        String configObjectPath = (args.length > 1) ? args[1] : MudlibBoot.DEFAULT_CONFIG_PATH;
        cli.boot(mudlib, configObjectPath);
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
            case "boot" -> boot(
                    command.pathArgument(0, Path.of("mudlib")),
                    command.optional(1, MudlibBoot.DEFAULT_CONFIG_PATH));
            case "pwd" -> pwd();
            case "cd" -> cd(command.optional(0, "/"));
            case "ls" -> ls(command.optional(0, "."));
            case "cat" -> cat(command.required(0));
            case "verbosity" -> verbosity(command.optional(0, ""));
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

    public void boot(Path mudlibRoot, String configObjectPath) {
        this.mudlibRoot = mudlibRoot.toAbsolutePath().normalize();
        this.virtualCwd = Path.of("");
        runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(this.mudlibRoot)
                .compilationObserver(new CliCompilationObserver())
                .build());
        EngineEfuns.registerCore(runtime);
        handles.clear();
        objectNames.clear();
        info("Booted runtime with mudlib root " + this.mudlibRoot);

        suppressCompilationFailures = true;
        MudlibBootResult bootResult;
        try {
            bootResult = new MudlibBoot(runtime, this.mudlibRoot, configObjectPath, false).boot();
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

    private void help() {
        out.println("Admin commands:");
        helpLine("h", "help", "Show this command reference.");
        helpLine("b", "boot [mudlib] [config]", "Start a fresh runtime with an optional mudlib config object.");
        helpLine("", "call <handle> <method> [args...]", "Invoke a method on a loaded object handle.");
        helpLine("", "cat <path>", "Print a file from the virtual mudlib filesystem.");
        helpLine("", "cd [path]", "Change the current virtual mudlib directory.");
        helpLine("n", "clone <path>", "Compile if needed and create a new LPC object instance.");
        helpLine("x", "destruct <handle>", "Remove an object from the runtime.");
        helpLine("i", "inspect <handle>", "Show object state, inventory, environment, and methods.");
        helpLine("l", "load <path>", "Compile, load, and register an LPC object.");
        helpLine("k", "look <handle>", "Call long() or short() and display object text.");
        helpLine("", "ls [path]", "List a virtual mudlib directory or file.");
        helpLine("m", "move <handle> <dest>", "Move one object handle into another object's inventory.");
        helpLine("o", "objects", "List known CLI object handles.");
        helpLine("", "pwd", "Print the current virtual mudlib directory.");
        helpLine("r", "reload <path>", "Recompile and replace a loaded LPC object.");
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
            boot(Path.of("mudlib"));
        }
    }

    private String canonicalCommand(String name) {
        return switch (name) {
        case "h" -> "help";
        case "b" -> "boot";
        case "v" -> "verbosity";
        case "l" -> "load";
        case "r" -> "reload";
        case "n" -> "clone";
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
        case "boot" -> "boot [mudlib] [config]";
        case "call" -> "call <handle> <method> [args...]";
        case "cat" -> "cat <path>";
        case "cd" -> "cd [path]";
        case "clone" -> "clone <path>";
        case "destruct" -> "destruct <handle>";
        case "inspect" -> "inspect <handle>";
        case "load" -> "load <path>";
        case "look" -> "look <handle>";
        case "ls" -> "ls [path]";
        case "move" -> "move <handle> <dest>";
        case "objects" -> "objects";
        case "pwd" -> "pwd";
        case "reload" -> "reload <path>";
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
