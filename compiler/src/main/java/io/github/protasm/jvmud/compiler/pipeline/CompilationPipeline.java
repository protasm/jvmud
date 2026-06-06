package io.github.protasm.jvmud.compiler.pipeline;

import io.github.protasm.jvmud.compiler.bytecode.BytecodeCompileException;
import io.github.protasm.jvmud.compiler.bytecode.BytecodeCompiler;
import io.github.protasm.jvmud.compiler.parser.ParseException;
import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.scanner.ScanException;
import io.github.protasm.jvmud.compiler.scanner.Scanner;
import io.github.protasm.jvmud.compiler.ir.IRLowerer;
import io.github.protasm.jvmud.compiler.ir.IRLoweringResult;
import io.github.protasm.jvmud.compiler.ir.TypedIR;
import io.github.protasm.jvmud.compiler.semantic.SemanticAnalysisResult;
import io.github.protasm.jvmud.compiler.semantic.SemanticAnalyzer;
import io.github.protasm.jvmud.compiler.semantic.SemanticModel;
import io.github.protasm.jvmud.compiler.token.TokenList;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolution;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CompilationPipeline {
    private final String parentInternalName;
    private final RuntimeContext runtimeContext;
    private final CompilationObserver observer;

    public CompilationPipeline(String parentInternalName) {
        this(parentInternalName, new RuntimeContext(null));
    }

    public CompilationPipeline(String parentInternalName, RuntimeContext runtimeContext) {
        this(parentInternalName, runtimeContext, CompilationObserver.NONE);
    }

    public CompilationPipeline(String parentInternalName, RuntimeContext runtimeContext, CompilationObserver observer) {
        this.parentInternalName = parentInternalName;
        this.runtimeContext = (runtimeContext != null) ? runtimeContext : new RuntimeContext(null);
        this.observer = (observer != null) ? observer : CompilationObserver.NONE;
    }

    public CompilationResult run(String source) {
        return run(source, ParserOptions.defaults());
    }

    public CompilationResult run(String source, ParserOptions parserOptions) {
        return run(null, source, null, null, parserOptions);
    }

    public CompilationResult run(Path sourcePath, String source, ParserOptions parserOptions) {
        return run(sourcePath, source, null, null, parserOptions);
    }

    public CompilationResult run(
            Path sourcePath, String source, String sourceName, String displayPath, ParserOptions parserOptions) {
        ParserOptions options = Objects.requireNonNull(parserOptions, "parserOptions");
        CompilationUnit unit = new CompilationUnit(sourcePath, sourceName, displayPath, source);
        Set<String> inheritanceStack = new HashSet<>();
        inheritanceStack.add(inheritanceKey(unit));
        return run(unit, options, true, inheritanceStack);
    }

    private CompilationResult run(
            CompilationUnit unit, ParserOptions parserOptions, boolean includeCodegen, Set<String> inheritanceStack) {
        List<CompilationProblem> problems = new ArrayList<>();
        TokenList tokens = null;
        ASTObject astObject = null;
        SemanticModel semanticModel = null;
        TypedIR typedIr = null;
        byte[] bytecode = null;

        Scanner scanner = new Scanner(runtimeContext.newPreprocessor());
        try {
            observer.stageStarted(unit, CompilationStage.SCAN);
            tokens = scanner.scan(unit.sourcePath(), unit.source(), unit.displayPath());
            unit.setTokens(tokens);
            observer.stageSucceeded(unit, CompilationStage.SCAN);
        } catch (ScanException e) {
            CompilationProblem problem = new CompilationProblem(CompilationStage.SCAN, "Error scanning source", e);
            problems.add(problem);
            observer.stageFailed(unit, CompilationStage.SCAN, problem);
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        Parser parser = new Parser(runtimeContext, parserOptions);
        try {
            observer.stageStarted(unit, CompilationStage.PARSE);
            astObject = parser.parse(unit.parseName(), tokens);
            unit.setASTObject(astObject);
            String inheritedPath = normalizeInheritedPath(astObject.parentName());
            unit.setInheritedPath(inheritedPath);
            astObject.setParentName(inheritedPath);
            observer.stageSucceeded(unit, CompilationStage.PARSE);
        } catch (ParseException e) {
            Integer line = e.line() >= 0 ? e.line() : null;
            CompilationProblem problem = new CompilationProblem(CompilationStage.PARSE, "Error parsing tokens", line, e);
            problems.add(problem);
            observer.stageFailed(unit, CompilationStage.PARSE, problem);
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        } catch (IllegalArgumentException e) {
            CompilationProblem problem = new CompilationProblem(CompilationStage.PARSE, "Error parsing tokens", e);
            problems.add(problem);
            observer.stageFailed(unit, CompilationStage.PARSE, problem);
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        if (unit.inheritedPath() != null) {
            CompilationUnit parentUnit =
                    resolveAndAnalyzeParent(unit, parserOptions, inheritanceStack, problems);
            unit.setParentUnit(parentUnit);
            if (!problems.isEmpty()) {
                return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
            }

            if (astObject != null && parentUnit != null && parentUnit.astObject() != null)
                astObject.setParentName(parentUnit.astObject().name());
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer(runtimeContext);
        try {
            observer.stageStarted(unit, CompilationStage.ANALYZE);
            SemanticAnalysisResult analysisResult = analyzer.analyze(unit);
            semanticModel = analysisResult.semanticModel();
            unit.setSemanticModel(semanticModel);
            problems.addAll(analysisResult.problems());

            if (!analysisResult.succeeded()) {
                for (CompilationProblem problem : analysisResult.problems()) {
                    observer.stageFailed(unit, CompilationStage.ANALYZE, problem);
                }
                return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
            }
            observer.stageSucceeded(unit, CompilationStage.ANALYZE);
        } catch (IllegalArgumentException e) {
            CompilationProblem problem = new CompilationProblem(CompilationStage.ANALYZE, "Error analyzing ASTObject", e);
            problems.add(problem);
            observer.stageFailed(unit, CompilationStage.ANALYZE, problem);
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        if (!includeCodegen)
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);

        IRLowerer lowerer = new IRLowerer(parentInternalName);
        try {
            observer.stageStarted(unit, CompilationStage.LOWER);
            IRLoweringResult loweringResult = lowerer.lower(semanticModel);
            typedIr = loweringResult.typedIr();
            problems.addAll(loweringResult.problems());

            if (!loweringResult.succeeded()) {
                for (CompilationProblem problem : loweringResult.problems()) {
                    observer.stageFailed(unit, CompilationStage.LOWER, problem);
                }
                return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
            }
            observer.stageSucceeded(unit, CompilationStage.LOWER);
        } catch (IllegalArgumentException e) {
            CompilationProblem problem = new CompilationProblem(CompilationStage.LOWER, "Error lowering semantic model", e);
            problems.add(problem);
            observer.stageFailed(unit, CompilationStage.LOWER, problem);
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        BytecodeCompiler compiler = new BytecodeCompiler(parentInternalName);
        try {
            observer.stageStarted(unit, CompilationStage.COMPILE);
            bytecode = compiler.compile(typedIr);
            observer.stageSucceeded(unit, CompilationStage.COMPILE);
        } catch (BytecodeCompileException | IllegalArgumentException e) {
            CompilationProblem problem = new CompilationProblem(CompilationStage.COMPILE, "Error compiling typed IR", e);
            problems.add(problem);
            observer.stageFailed(unit, CompilationStage.COMPILE, problem);
        }

        return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
    }

    private CompilationUnit resolveAndAnalyzeParent(
            CompilationUnit childUnit,
            ParserOptions parserOptions,
            Set<String> inheritanceStack,
            List<CompilationProblem> problems) {
        IncludeResolution resolution;
        String inheritedPath = normalizeInheritedPath(childUnit.inheritedPath());

        try {
            resolution = resolveInheritedSource(childUnit, inheritedPath);
        } catch (Exception e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.PARSE,
                            "Cannot inherit '" + childUnit.inheritedPath() + "': " + e.getMessage(),
                            e));
            return null;
        }

        CompilationUnit parentUnit =
                new CompilationUnit(
                        resolution.resolvedPath(),
                        resolvedParentSourceName(childUnit, inheritedPath, resolution),
                        resolution.displayPath(),
                        resolution.source());

        String parentKey = inheritanceKey(parentUnit);
        if (inheritanceStack.contains(parentKey)) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.PARSE,
                            "Circular inherit detected for '" + parentUnit.parseName() + "'",
                            (Throwable) null));
            return null;
        }

        Set<String> nextStack = new HashSet<>(inheritanceStack);
        nextStack.add(parentKey);

        CompilationResult parentResult = run(parentUnit, parserOptions, false, nextStack);
        problems.addAll(parentResult.getProblems());

        if (parentResult.getSemanticModel() == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Failed to analyze inherited object '" + childUnit.inheritedPath() + "'",
                            (Throwable) null));
        }

        return parentUnit;
    }

    private IncludeResolution resolveInheritedSource(CompilationUnit childUnit, String inheritedPath)
            throws Exception {
        if (inheritedPath == null) {
            throw new IllegalArgumentException("inherit path cannot be empty");
        }

        String normalized = normalizeInheritedPath(inheritedPath);
        if (normalized == null) {
            throw new IllegalArgumentException("inherit path cannot be empty");
        }

        if (normalized.startsWith("/")) {
            IncludeResolver resolver = runtimeContext.includeResolver();
            return resolver.resolve(childUnit.sourcePath(), normalized, false);
        }

        Path sourcePath = childUnit.sourcePath();
        if (sourcePath == null) {
            throw new IllegalArgumentException("relative inherits require a source path");
        }

        Path parentDir = sourcePath.normalize().getParent();
        if (parentDir == null) {
            throw new IllegalArgumentException("relative inherits require a parent directory");
        }

        for (String candidatePath : inheritedSourceCandidates(normalized)) {
            Path candidate = parentDir.resolve(candidatePath).normalize();
            if (Files.isRegularFile(candidate)) {
                String displayPath = localInheritedDisplayPath(childUnit, candidatePath);
                return new IncludeResolution(Files.readString(candidate), candidate, displayPath);
            }
        }

        IncludeResolver resolver = runtimeContext.includeResolver();
        Exception resolverFailure = null;
        for (String candidatePath : inheritedSourceCandidates(normalized)) {
            try {
                return resolver.resolve(childUnit.sourcePath(), candidatePath, false);
            } catch (Exception e) {
                resolverFailure = e;
            }
        }
        Path localCandidate = parentDir.resolve(normalized).normalize();
        throw new IllegalArgumentException(
                "relative inherit not found at " + localCandidate.toAbsolutePath(), resolverFailure);
    }

    private List<String> inheritedSourceCandidates(String inheritedPath) {
        if (inheritedPath.endsWith(".c")) {
            return List.of(inheritedPath);
        }
        return List.of(inheritedPath, inheritedPath + ".c");
    }

    private String localInheritedDisplayPath(CompilationUnit childUnit, String inheritedPath) {
        String baseDisplay = childUnit.displayPath();
        if ((baseDisplay == null) || baseDisplay.isBlank()) {
            String sourceName = childUnit.sourceName();
            if ((sourceName == null) || sourceName.isBlank()) {
                return inheritedPath;
            }
            baseDisplay = "/" + sourceName;
        }

        Path basePath = Path.of(baseDisplay);
        Path parent = basePath.getParent();
        if (parent == null) {
            return inheritedPath;
        }

        Path combined = parent.resolve(inheritedPath).normalize();
        String display = combined.toString().replace('\\', '/');
        if (!display.startsWith("/")) {
            display = "/" + display;
        }
        return display;
    }

    private String inheritanceKey(CompilationUnit unit) {
        if (unit.sourcePath() != null)
            return unit.sourcePath().normalize().toString();

        if (unit.displayPath() != null)
            return unit.displayPath();

        return unit.parseName();
    }

    private String normalizeInheritedPath(String inheritedPath) {
        if (inheritedPath == null)
            return null;

        String trimmed = inheritedPath.trim();

        if (trimmed.isEmpty())
            return null;

        if ((trimmed.length() >= 2) && trimmed.startsWith("\"") && trimmed.endsWith("\""))
            trimmed = trimmed.substring(1, trimmed.length() - 1);

        if (trimmed.isEmpty())
            return null;

        return trimmed;
    }

    private String resolvedParentSourceName(
            CompilationUnit childUnit, String inheritedPath, IncludeResolution resolution) {
        String normalized =
                normalizeInheritedPath((resolution.displayPath() != null) ? resolution.displayPath() : inheritedPath);
        if ((normalized == null) || normalized.isEmpty())
            return null;

        Path path = Path.of(normalized);
        String stem = stripExtension(path.getFileName().toString());
        Path withoutExt = (path.getParent() == null) ? Path.of(stem) : path.getParent().resolve(stem);
        Path normalizedPath = withoutExt.normalize();

        if ((childUnit.sourceName() != null) && !normalizedPath.isAbsolute()) {
            Path childPath = Path.of(childUnit.sourceName());
            if (childPath.getNameCount() > 0) {
                Path childPrefix = childPath.getName(0);
                if ((normalizedPath.getNameCount() == 0) || !normalizedPath.getName(0).equals(childPrefix))
                    normalizedPath = childPrefix.resolve(normalizedPath).normalize();
            }
        }

        String normalizedName = normalizedPath.toString();

        normalizedName = normalizedName.replace('\\', '/');

        while (normalizedName.startsWith("/"))
            normalizedName = normalizedName.substring(1);

        return normalizedName;
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? name : name.substring(0, dot);
    }
}
