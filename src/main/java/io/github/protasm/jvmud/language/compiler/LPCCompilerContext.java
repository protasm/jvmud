package io.github.protasm.jvmud.language.compiler;

import io.github.protasm.jvmud.language.compiler.parser.Parser;
import io.github.protasm.jvmud.language.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTInherit;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.language.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.language.compiler.preprocessor.IncludeResolution;
import io.github.protasm.jvmud.language.compiler.preprocessor.IncludeResolver;
import io.github.protasm.jvmud.language.compiler.preprocessor.Preprocessor;
import io.github.protasm.jvmud.language.compiler.scanner.Scanner;
import io.github.protasm.jvmud.language.compiler.token.TokenList;
import io.github.protasm.jvmud.language.compiler.transpiler.FieldTypeTranspiler;
import io.github.protasm.jvmud.language.compiler.transpiler.LocalTypeTranspiler;
import io.github.protasm.jvmud.language.compiler.transpiler.MethodVarargsTranspiler;
import io.github.protasm.jvmud.language.compiler.transpiler.UntypedMethodTranspiler;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.efun.EfunRegistry;
import io.github.protasm.jvmud.language.efun.EfunSignature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Source resolution, compatibility transforms and callable declarations for LPC compilation.
 *
 * <p>This context is safe to construct for a compiler, formatter or test without constructing a
 * world. Its caches contain parsed declarations, never live LPC objects. A hosted runtime can share
 * its efun registry so compilation sees exactly the signatures that execution will expose.</p>
 */
public final class LPCCompilerContext implements LPCCompilationEnvironment {
    private IncludeResolver includeResolver;
    private final EfunRegistry efunRegistry;
    private LPCCompilationOptions options = LPCCompilationOptions.defaults();
    private LPCGlobalFunctions globalFunctions = LPCGlobalFunctions.empty();
    private ParserOptions parserOptions = ParserOptions.defaults();
    private final Map<String, Optional<ASTObject>> globalDeclarations = new LinkedHashMap<>();
    private final Map<String, GlobalObjectSource> inMemorySources = new LinkedHashMap<>();

    /** Creates a strict compiler context that resolves no external source by default. */
    public LPCCompilerContext() {
        this(null);
    }

    public LPCCompilerContext(IncludeResolver includeResolver) {
        this(includeResolver, new EfunRegistry());
    }

    public LPCCompilerContext(IncludeResolver includeResolver, EfunRegistry efunRegistry) {
        setIncludeResolver(includeResolver);
        this.efunRegistry = Objects.requireNonNull(efunRegistry, "efunRegistry");
    }

    @Override
    public IncludeResolver includeResolver() {
        return includeResolver;
    }

    public void setIncludeResolver(IncludeResolver includeResolver) {
        this.includeResolver = includeResolver != null ? includeResolver : Preprocessor.rejectingResolver();
        globalDeclarations.clear();
    }

    public EfunRegistry efunRegistry() {
        return efunRegistry;
    }

    /** Adds a compile-time declaration without registering an executable callback. */
    public void registerSignature(EfunSignature signature) {
        efunRegistry.registerSignature(signature);
    }

    public void setOptions(LPCCompilationOptions options) {
        this.options = Objects.requireNonNull(options, "options");
        globalDeclarations.clear();
    }

    public void setGlobalFunctions(LPCGlobalFunctions globalFunctions) {
        this.globalFunctions = Objects.requireNonNull(globalFunctions, "globalFunctions");
        globalDeclarations.clear();
    }

    public void setParserOptions(ParserOptions parserOptions) {
        this.parserOptions = Objects.requireNonNull(parserOptions, "parserOptions");
        globalDeclarations.clear();
    }

    public ParserOptions parserOptions() {
        return parserOptions;
    }

    /** Registers source for a global object compiled directly from memory. */
    public void registerInMemoryObjectSource(String objectPath, String source, String displayPath) {
        String normalized = normalizeMudlibPath(objectPath);
        if (normalized == null || source == null) return;
        inMemorySources.put(normalized, new GlobalObjectSource(source, null,
                displayPath != null ? displayPath : "/" + normalized + ".c"));
        globalDeclarations.remove(normalized);
    }

    /** Selects an ordinary global object while preserving the other name-resolution settings. */
    public void setMfunObjectPath(String path) {
        setGlobalFunctions(new LPCGlobalFunctions(normalizeMudlibPath(path), null,
                globalFunctions.compatibilityObjectPath(), globalFunctions.compatibilitySourcePath(),
                globalFunctions.compatibilityOverrides(), globalFunctions.engineFunctionAliases()));
    }

    @Override
    public Preprocessor newPreprocessor() {
        return new Preprocessor(includeResolver, options.predefines(), options.functionPredefines());
    }

    @Override
    public boolean dynamicTypes() {
        return options.dynamicTypes();
    }

    @Override
    public boolean transpileImplicitSelfCalls() {
        return options.transpileImplicitSelfCalls();
    }

    @Override
    public String engineFunctionName(String name) {
        return globalFunctions.engineFunctionAliases().getOrDefault(name, name);
    }

    @Override
    public TokenList transpileSourceTokens(Path sourcePath, TokenList tokens) {
        TokenList translated = new FieldTypeTranspiler().transpile(sourcePath,
                options.sourceRoot(), tokens, options.fieldTypeOverrides());
        return options.transpileUntypedMethods()
                ? new UntypedMethodTranspiler().transpile(translated) : translated;
    }

    @Override
    public void transpileSourceLocals(Path sourcePath, ASTObject object) {
        new MethodVarargsTranspiler().transpile(sourcePath, options.sourceRoot(), object,
                options.methodVarargsOverrides());
        new LocalTypeTranspiler().transpile(sourcePath, options.sourceRoot(), object,
                options.localTypeOverrides());
    }

    @Override
    public boolean hasFunctionNamed(String name) {
        return !name.equals(engineFunctionName(name))
                || !efunRegistry.signatures(name).isEmpty()
                || !efunRegistry.signatures(engineFunctionName(name)).isEmpty()
                || declaredGlobalMethod(globalFunctions.mudlibObjectPath(), name, -1) != null
                || declaredGlobalMethod(globalFunctions.compatibilityObjectPath(), name, -1) != null;
    }

    @Override
    public EfunSignature resolveFunctionSignature(String name, int arity) {
        LPCGlobalFunction global = resolveGlobalFunction(name, arity);
        if (global != null) return global.signature();
        EfunSignature nativeSignature = resolveEngineFunctionSignature(name, arity);
        if (nativeSignature != null) return nativeSignature;
        String nativeName = engineFunctionName(name);
        return name.equals(nativeName) ? null : resolveEngineFunctionSignature(nativeName, arity);
    }

    @Override
    public EfunSignature resolveEngineFunctionSignature(String name, int arity) {
        return efunRegistry.lookupSignature(name, arity);
    }

    /** Resolves a global declaration without loading or invoking its defining object. */
    public LPCGlobalFunction resolveGlobalFunction(String name, int arity) {
        if (globalFunctions.compatibilityOverrides().contains(name)) {
            LPCGlobalFunction override = resolveGlobalFunction(globalFunctions.compatibilityObjectPath(), name, arity);
            if (override != null) return override;
        }
        LPCGlobalFunction ordinary = resolveGlobalFunction(globalFunctions.mudlibObjectPath(), name, arity);
        return ordinary != null ? ordinary
                : resolveGlobalFunction(globalFunctions.compatibilityObjectPath(), name, arity);
    }

    private LPCGlobalFunction resolveGlobalFunction(String objectPath, String name, int arity) {
        if (objectPath == null) return null;
        ASTMethod method = declaredGlobalMethod(objectPath, name, arity);
        return method != null ? new LPCGlobalFunction(objectPath, globalMethodSignature(method, arity)) : null;
    }

    /** A negative arity queries name presence without selecting a callable signature. */
    private ASTMethod declaredGlobalMethod(String objectPath, String name, int arity) {
        return declaredGlobalMethod(objectPath, name, arity, new HashSet<>());
    }

    private ASTMethod declaredGlobalMethod(String objectPath, String name, int arity, Set<String> visitedPaths) {
        String normalizedPath = normalizeMudlibPath(objectPath);
        if (normalizedPath == null || !visitedPaths.add(normalizedPath)) {
            return null;
        }
        Optional<ASTObject> declaration = globalDeclarations.computeIfAbsent(
                normalizedPath,
                this::parseGlobalObjectDeclaration);
        if (declaration.isEmpty()) {
            return null;
        }
        ASTObject object = declaration.orElseThrow();
        ASTMethod directMethod = object.methods().getAll(name).stream()
                .filter(method -> arity < 0 || acceptsArity(method, arity))
                .min(Comparator.comparingInt(this::parameterCount))
                .orElse(null);
        if (directMethod != null) {
            return directMethod;
        }
        for (ASTInherit inherit : object.inherits()) {
            ASTMethod inheritedMethod = declaredGlobalMethod(inherit.path(), name, arity, visitedPaths);
            if (inheritedMethod != null) {
                return inheritedMethod;
            }
        }
        return null;
    }

    private boolean acceptsArity(ASTMethod method, int arity) {
        int parameterCount = parameterCount(method);
        return parameterCount >= arity || (method.modifiers().isVarargs() && arity >= parameterCount);
    }

    private Optional<ASTObject> parseGlobalObjectDeclaration(String objectPath) {
        try {
            GlobalObjectSource source = globalObjectSource(objectPath);
            if (source == null) {
                return Optional.empty();
            }
            Scanner scanner = new Scanner(newPreprocessor());
            TokenList tokens = transpileSourceTokens(source.sourcePath(),
                    scanner.scan(source.sourcePath(), source.source(), source.displayPath()));
            Parser parser = new Parser(this, parserOptions);
            return Optional.of(parser.parse(objectPath, tokens));
        } catch (IOException e) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Could not parse global function object: " + objectPath, e);
        }
    }

    private GlobalObjectSource globalObjectSource(String objectPath) throws IOException {
        GlobalObjectSource inMemory = inMemorySources.get(objectPath);
        if (inMemory != null) {
            return inMemory;
        }

        if (objectPath.equals(globalFunctions.mudlibObjectPath())
                && globalFunctions.mudlibSourcePath() != null) {
            Path sourcePath = globalFunctions.mudlibSourcePath();
            if (Files.isRegularFile(sourcePath)) {
                return new GlobalObjectSource(
                        Files.readString(sourcePath),
                        sourcePath,
                        "/" + objectPath + ".c");
            }
        }

        if (objectPath.equals(globalFunctions.compatibilityObjectPath())
                && globalFunctions.compatibilitySourcePath() != null) {
            Path sourcePath = globalFunctions.compatibilitySourcePath();
            if (Files.isRegularFile(sourcePath)) {
                return new GlobalObjectSource(
                        Files.readString(sourcePath),
                        sourcePath,
                        "/" + objectPath + ".c");
            }
        }

        String includePath = "/" + objectPath + ".c";
        IncludeResolution resolution = includeResolver.resolve(null, includePath, false);
        return new GlobalObjectSource(resolution.source(), resolution.resolvedPath(), resolution.displayPath());
    }

    /**
     * Builds the declaration-only calling contract used by existing mudlib globals.
     *
     * <p>These objects have been parsed, not semantically analyzed. Unresolved source types retain
     * the established {@code mixed} fallback; resolving their types is a separate language change,
     * rather than a side effect of discovering a signature or reorganizing host ownership.</p>
     */
    private EfunSignature globalMethodSignature(ASTMethod method, int arity) {
        List<LPCType> parameterTypes = new ArrayList<>();
        if (method.parameters() != null && method.parameters().size() > 0) {
            for (int i = 0; i < arity; i++) {
                ASTParameter parameter = method.parameters().get(Math.min(i, method.parameters().size() - 1));
                parameterTypes.add(lpcTypeOrMixed(parameter.symbol()));
            }
        }
        Symbol returnSymbol = method.symbol();
        return new EfunSignature(
                new Symbol(lpcTypeOrMixed(returnSymbol), returnSymbol.name()),
                parameterTypes);
    }

    private int parameterCount(ASTMethod method) {
        return method.parameters() != null ? method.parameters().size() : 0;
    }

    private LPCType lpcTypeOrMixed(Symbol symbol) {
        return symbol != null && symbol.lpcType() != null ? symbol.lpcType() : LPCType.LPCMIXED;
    }

    private record GlobalObjectSource(String source, Path sourcePath, String displayPath) {}

    private static String normalizeMudlibPath(String path) {
        if (path == null || path.isBlank()) return null;
        String normalized = path.trim();
        if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\""))
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.endsWith(".c")) normalized = normalized.substring(0, normalized.length() - 2);
        return normalized.isBlank() ? null : normalized;
    }
}
