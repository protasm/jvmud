package io.github.protasm.jvmud.compiler.semantic;

import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.pipeline.CompilationUnit;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArgument;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
import io.github.protasm.jvmud.compiler.parser.ast.ASTInherit;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMapNode;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprClosureArgument;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCollectionTransform;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFromEndIndex;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInlineCallable;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeField;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprMappingEntry;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprMappingLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprError;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprProtectedEval;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSequence;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSliceAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSliceStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSortArray;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprTernary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprTypedFunctionLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedParentCall;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedQualifiedCall;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBreak;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtContinue;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtDoWhile;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtEmpty;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtFor;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtForeach;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtSwitch;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtWhile;
import io.github.protasm.jvmud.compiler.parser.type.AssignOpType;
import io.github.protasm.jvmud.compiler.parser.type.BinaryOpType;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.parser.type.UnaryOpType;
import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.compiler.semantic.SemanticScope.ScopedSymbol;
import io.github.protasm.jvmud.compiler.token.Token;
import io.github.protasm.jvmud.compiler.token.TokenType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/** Performs semantic analysis on a parsed AST and produces a typed model. */
public final class SemanticAnalyzer {
    private final TypeResolver typeResolver;
    private final RuntimeContext runtimeContext;

    public SemanticAnalyzer() {
        this(new TypeResolver(), null);
    }

    public SemanticAnalyzer(TypeResolver typeResolver) {
        this(typeResolver, null);
    }

    public SemanticAnalyzer(RuntimeContext runtimeContext) {
        this(new TypeResolver(), runtimeContext);
    }

    public SemanticAnalyzer(TypeResolver typeResolver, RuntimeContext runtimeContext) {
        this.typeResolver = Objects.requireNonNull(typeResolver, "typeResolver");
        this.runtimeContext =
                (runtimeContext != null) ? runtimeContext : new RuntimeContext(null);
    }

    public SemanticAnalysisResult analyze(ASTObject astObject) {
        return analyze(astObject, null);
    }

    public SemanticAnalysisResult analyze(CompilationUnit unit) {
        if (unit == null)
            throw new IllegalArgumentException("CompilationUnit cannot be null.");

        return analyze(unit.astObject(), unit);
    }

    private SemanticAnalysisResult analyze(ASTObject astObject, CompilationUnit unit) {
        if (astObject == null)
            throw new IllegalArgumentException("ASTObject cannot be null.");

        List<CompilationProblem> problems = new ArrayList<>();
        CompilationUnit parentUnit = (unit != null) ? unit.parentUnit() : null;
        List<CompilationUnit> directParentUnits = directParentUnits(unit, parentUnit);
        SemanticScope parentScope = (parentUnit != null && parentUnit.semanticModel() != null)
                ? parentUnit.semanticModel().objectScope()
                : null;
        SemanticScope objectScope = new SemanticScope(parentScope);

        validateInheritance(astObject, problems);
        resolveObjectSignatures(astObject, problems);
        validateDefinitionsHaveDeclarations(astObject, problems);
        validateDuplicates(astObject.fields(), "field", problems);
        validateDuplicateMethods(astObject.methods(), problems);
        validateDuplicateInheritedMembers(astObject, directParentUnits, problems);
        mergeParentSymbols(objectScope, directParentUnits);

        for (ASTField field : astObject.fields()) {
            boolean shadowsInherited =
                    hasInheritedField(objectScope, field.symbol().name());
            if (shadowsInherited) {
                // Field shadowing: inherited field remains in scope. Shadowing is permitted for
                // compatibility with LPC mudlibs, so we do not surface it as an error.
            }

            objectScope.declare(field.symbol(), unit, field, null);
        }

        for (ASTMethod method : astObject.methods()) {
            ASTMethod overridden = findOverriddenMethod(directParentUnits, method);
            if (overridden != null && !isSignatureCompatible(method, overridden)) {
                // Override detection: overriding is allowed only when the typed LPC signatures
                // match; otherwise surface a hard error.
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Method '" + method.symbol().name() + "' overrides with incompatible signature",
                                method.line()));
            }
            method.setOverrides(overridden);

            objectScope.declare(method.symbol(), unit, null, method);
        }

        resolveIdentifiers(astObject, objectScope, parentUnit, problems);

        for (ASTMethod method : astObject.methods()) {
            SemanticScope methodScope = new SemanticScope(objectScope);

            declareParameters(methodScope, method, problems);
            assignLocalSlots(method, problems);
            validateLocalInitializers(method, problems);
            ensureImplicitReturn(method);
            resolveLocalTypes(method, problems);
        }

        SemanticTypeChecker typeChecker = new SemanticTypeChecker(problems);
        typeChecker.check(astObject);

        return new SemanticAnalysisResult(new SemanticModel(astObject, objectScope), problems);
    }

    private List<CompilationUnit> directParentUnits(CompilationUnit unit, CompilationUnit parentUnit) {
        if (unit != null && !unit.directParentUnits().isEmpty())
            return unit.directParentUnits();

        if (parentUnit != null)
            return List.of(parentUnit);

        return List.of();
    }

    private void resolveSymbolType(Symbol symbol, int line, List<CompilationProblem> problems) {
        if (symbol == null)
            return;

        if (symbol.lpcType() != null)
            return;

        LPCType resolved = typeResolver.resolve(symbol.declaredTypeName());

        if (resolved == null) {
            if (symbol.declaredTypeName() != null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Unknown type '" + symbol.declaredTypeName() + "' for symbol '" + symbol.name() + "'",
                                line));
            }

            symbol.resolveDeclaredType(LPCType.LPCMIXED);
            return;
        }

        symbol.resolveDeclaredType(resolved);
    }

    private void declareUnique(
            Symbol symbol, SemanticScope scope, List<CompilationProblem> problems, String kind) {
        ScopedSymbol existing = scope.resolveLocally(symbol.name());

        if (existing != null && existing.symbol() != symbol) {
            // Duplicate detection is limited to the child scope; inherited entries have already
            // been merged with origin metadata, so seeing a different symbol here means the child
            // redeclared the same name.
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate " + kind + " '" + symbol.name() + "' in scope",
                            (Throwable) null));
            return;
        }

        scope.declare(symbol);
    }

    private void declareParameters(
            SemanticScope methodScope, ASTMethod method, List<CompilationProblem> problems) {
        if (method.parameters() == null)
            return;

        for (ASTParameter parameter : method.parameters())
            declareUnique(parameter.symbol(), methodScope, problems, "parameter");
    }

    private static int parameterCount(ASTMethod method) {
        return (method.parameters() != null) ? method.parameters().size() : 0;
    }

    private void resolveLocalTypes(ASTMethod method, List<CompilationProblem> problems) {
        if (method.locals() == null)
            return;

        for (ASTLocal local : method.locals())
            resolveSymbolType(local.symbol(), local.line(), problems);
    }

    private void ensureImplicitReturn(ASTMethod method) {
        if (method.body() == null)
            return;

        List<ASTStatement> statements = method.body().statements();

        if (statements.isEmpty() || !(statements.get(statements.size() - 1) instanceof ASTStmtReturn))
            statements.add(new ASTStmtReturn(method.body().line(), defaultReturnValue(method), true));
    }

    private ASTExpression defaultReturnValue(ASTMethod method) {
        LPCType methodType = method.symbol().lpcType();
        if (methodType == LPCType.LPCVOID)
            return null;

        return new ASTExprLiteralInteger(
                method.body().line(), new Token<>(TokenType.T_INT_LITERAL, "0", 0, null));
    }

    private void assignLocalSlots(ASTMethod method, List<CompilationProblem> problems) {
        Set<Symbol> parameterSymbols = parameterSymbols(method);
        int slot = 1; // slot 0 reserved for "this"

        if (method.parameters() != null) {
            for (ASTParameter parameter : method.parameters()) {
                ASTLocal paramLocal = findLocalForSymbol(method.locals(), parameter.symbol());
                if (paramLocal != null) {
                    paramLocal.setSlot(slot);
                    paramLocal.setScopeDepth(Math.max(paramLocal.scopeDepth(), 0));
                }
                slot++;
            }
        }

        LocalSlotAllocator allocator = new LocalSlotAllocator(parameterCount(method));

        for (ASTLocal local : method.locals()) {
            if (parameterSymbols.contains(local.symbol()))
                continue;
            allocator.place(local, problems);
        }
    }

    private Set<Symbol> parameterSymbols(ASTMethod method) {
        if (method.parameters() == null)
            return Set.of();

        Set<Symbol> symbols = new HashSet<>();
        for (ASTParameter parameter : method.parameters())
            symbols.add(parameter.symbol());
        return symbols;
    }

    private ASTLocal findLocalForSymbol(List<ASTLocal> locals, Symbol symbol) {
        if (locals == null || symbol == null)
            return null;

        for (ASTLocal local : locals) {
            if (local.symbol() == symbol)
                return local;
        }

        return null;
    }

    private void validateLocalInitializers(ASTMethod method, List<CompilationProblem> problems) {
        if (method.body() == null)
            return;

        validateInitializers(method.body(), problems);
    }

    private void validateInitializers(ASTStatement statement, List<CompilationProblem> problems) {
        if (statement == null)
            return;

        if (statement instanceof ASTStmtBlock block) {
            for (ASTStatement nested : block)
                validateInitializers(nested, problems);
            return;
        }

        if (statement instanceof ASTStmtIfThenElse stmtIf) {
            validateInitializers(stmtIf.thenBranch(), problems);
            if (stmtIf.elseBranch() != null)
                validateInitializers(stmtIf.elseBranch(), problems);
            return;
        }

        if (statement instanceof ASTStmtFor stmtFor) {
            inspectInitializerExpression(stmtFor.initializer(), problems);
            inspectInitializerExpression(stmtFor.update(), problems);
            validateInitializers(stmtFor.body(), problems);
            return;
        }

        if (statement instanceof ASTStmtForeach stmtForeach) {
            inspectInitializerExpression(stmtForeach.iterable(), problems);
            validateInitializers(stmtForeach.body(), problems);
            return;
        }

        if (statement instanceof ASTStmtWhile stmtWhile) {
            validateInitializers(stmtWhile.body(), problems);
            return;
        }

        if (statement instanceof ASTStmtDoWhile stmtDoWhile) {
            validateInitializers(stmtDoWhile.body(), problems);
            return;
        }

        if (statement instanceof ASTStmtSwitch stmtSwitch) {
            inspectInitializerExpression(stmtSwitch.expression(), problems);
            for (ASTStmtSwitch.SwitchCase switchCase : stmtSwitch.cases()) {
                if (!switchCase.isDefault()) {
                    inspectInitializerExpression(switchCase.expression(), problems);
                    if (switchCase.isRange())
                        inspectInitializerExpression(switchCase.rangeEndExpression(), problems);
                }
                for (ASTStatement nested : switchCase.statements())
                    validateInitializers(nested, problems);
            }
            return;
        }

        if (statement instanceof ASTStmtExpression stmtExpression)
            inspectInitializerExpression(stmtExpression.expression(), problems);
    }

    private void inspectInitializerExpression(ASTExpression expression, List<CompilationProblem> problems) {
        if (expression instanceof ASTExprLocalStore store && store.isDeclarationInitializer()) {
            if (referencesLocal(store.value(), store.local())) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Cannot reference local '" + store.local().symbol().name()
                                        + "' in its own initializer.",
                                store.line()));
            }
        }
    }

    private boolean referencesLocal(ASTExpression expression, ASTLocal local) {
        if (expression == null || local == null)
            return false;

        if (expression instanceof ASTExprLocalAccess access)
            return access.local() == local;

        if (expression instanceof ASTExprLocalStore store)
            return store.local() == local || referencesLocal(store.value(), local);

        if (expression instanceof ASTExprInvokeLocal invoke)
            return invoke.local() == local || referencesArguments(invoke.arguments(), local);

        if (expression instanceof ASTExprDynamicInvoke invoke)
            return referencesLocal(invoke.target(), local) || referencesArguments(invoke.arguments(), local);

        if (expression instanceof ASTExprInvokeField invoke)
            return referencesArguments(invoke.arguments(), local);

        if (expression instanceof ASTExprCallMethod call)
            return referencesArguments(call.arguments(), local);

        if (expression instanceof ASTExprCallEfun call)
            return referencesArguments(call.arguments(), local);

        if (expression instanceof ASTExprUnresolvedQualifiedCall call)
            return referencesArguments(call.arguments(), local);

        if (expression instanceof ASTExprArrayStore store)
            return referencesLocal(store.target(), local)
                    || referencesLocal(store.index(), local)
                    || referencesLocal(store.value(), local);

        if (expression instanceof ASTExprArrayMutation mutation)
            return referencesLocal(mutation.target(), local) || referencesLocal(mutation.index(), local);

        if (expression instanceof ASTExprArrayAccess access)
            return referencesLocal(access.target(), local) || referencesLocal(access.index(), local);

        if (expression instanceof ASTExprSliceAccess access)
            return referencesLocal(access.target(), local)
                    || referencesLocal(access.start(), local)
                    || (access.end() != null && referencesLocal(access.end(), local));

        if (expression instanceof ASTExprSliceStore store)
            return referencesLocal(store.target(), local)
                    || referencesLocal(store.start(), local)
                    || (store.end() != null && referencesLocal(store.end(), local))
                    || referencesLocal(store.value(), local);

        if (expression instanceof ASTExprArrayLiteral arrayLiteral)
            return arrayLiteral.elements().stream().anyMatch(elem -> referencesLocal(elem, local));

        if (expression instanceof ASTExprMappingLiteral mappingLiteral)
            return mappingLiteral.entries().stream()
                    .anyMatch(entry -> referencesLocal(entry.key(), local) || referencesLocal(entry.value(), local));

        if (expression instanceof ASTExprOpUnary unary)
            return referencesLocal(unary.right(), local);

        if (expression instanceof ASTExprOpBinary binary)
            return referencesLocal(binary.left(), local) || referencesLocal(binary.right(), local);

        if (expression instanceof ASTExprSequence sequence)
            return sequence.expressions().stream().anyMatch(expr -> referencesLocal(expr, local));

        if (expression instanceof ASTExprProtectedEval protectedEval)
            return referencesLocal(protectedEval.body(), local);

        if (expression instanceof ASTExprTernary ternary) {
            return referencesLocal(ternary.condition(), local)
                    || referencesLocal(ternary.thenBranch(), local)
                    || referencesLocal(ternary.elseBranch(), local);
        }

        return false;
    }

    private boolean referencesArguments(ASTArguments arguments, ASTLocal local) {
        if (arguments == null)
            return false;

        for (ASTArgument argument : arguments)
            if (referencesLocal(argument.expression(), local))
                return true;

        return false;
    }

    private void validateInheritance(ASTObject astObject, List<CompilationProblem> problems) {
        List<ASTInherit> inherits = astObject.inherits();
        if (inherits.isEmpty())
            return;

        int firstPropertyOrder = firstPropertyOrder(astObject);
        if (firstPropertyOrder == Integer.MAX_VALUE)
            return;

        for (ASTInherit inherit : inherits) {
            if (orderOf(inherit) > firstPropertyOrder) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "inherit statements must appear before any variable or function declarations.",
                                inherit.line()));
            }
        }
    }

    private int firstPropertyOrder(ASTObject astObject) {
        int firstOrder = Integer.MAX_VALUE;

        for (ASTField field : astObject.fields())
            firstOrder = Math.min(firstOrder, orderOf(field));

        for (ASTMethod method : astObject.methods())
            firstOrder = Math.min(firstOrder, orderOf(method));

        return firstOrder;
    }

    private int orderOf(io.github.protasm.jvmud.compiler.parser.ast.ASTNode node) {
        return node.sourceOrder() >= 0 ? node.sourceOrder() : node.line();
    }

    private void resolveObjectSignatures(ASTObject astObject, List<CompilationProblem> problems) {
        for (ASTField field : astObject.fields())
            resolveSymbolType(field.symbol(), field.line(), problems);

        for (ASTMethod method : astObject.methods()) {
            if (method.symbol().declaredTypeName() == null) {
                method.symbol().resolveDeclaredType(LPCType.LPCMIXED);
            }
            resolveSymbolType(method.symbol(), method.line(), problems);
            if (method.parameters() != null) {
                for (ASTParameter parameter : method.parameters()) {
                    if (parameter.symbol().declaredTypeName() == null) {
                        parameter.symbol().resolveDeclaredType(LPCType.LPCMIXED);
                    }
                    resolveSymbolType(parameter.symbol(), parameter.line(), problems);
                }
            }
        }
    }

    private void validateDefinitionsHaveDeclarations(
            ASTObject astObject, List<CompilationProblem> problems) {
        for (ASTField field : astObject.fields()) {
            if (field.isDefined() && !field.isDeclared()) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Field '" + field.symbol().name() + "' is defined without a prior declaration.",
                                field.line()));
            }
        }

        for (ASTMethod method : astObject.methods()) {
            if (method.isDefined() && !method.isDeclared()) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Method '" + method.symbol().name() + "' is defined without a prior declaration.",
                                method.line()));
            }
        }
    }

    private <T> void validateDuplicates(ASTMapNode<T> nodes, String kind, List<CompilationProblem> problems) {
        for (Map.Entry<String, List<T>> entry : nodes.nodes().entrySet()) {
            List<T> occurrences = entry.getValue();
            if (occurrences.size() > 1) {
                // Emit once per duplicate cluster to surface user-facing name collisions.
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Duplicate " + kind + " '" + entry.getKey() + "' in object",
                                nodes.line()));
            }
        }
    }

    private void validateDuplicateMethods(ASTMapNode<ASTMethod> nodes, List<CompilationProblem> problems) {
        for (Map.Entry<String, List<ASTMethod>> entry : nodes.nodes().entrySet()) {
            List<ASTMethod> occurrences = entry.getValue();
            if (occurrences.size() <= 1)
                continue;

            Set<Integer> arities = new HashSet<>();
            for (ASTMethod method : occurrences) {
                int arity = parameterCount(method);
                if (!arities.add(arity)) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Duplicate method '" + entry.getKey() + "' with arity " + arity + " in object",
                                    method.line()));
                }
            }
        }
    }

    private void validateDuplicateInheritedMembers(
            ASTObject astObject, List<CompilationUnit> directParentUnits, List<CompilationProblem> problems) {
        if (directParentUnits.size() < 2)
            return;

        Map<String, List<ScopedSymbol>> fieldsByName = new LinkedHashMap<>();
        Map<MethodKey, List<ScopedSymbol>> methodsBySignature = new LinkedHashMap<>();

        for (CompilationUnit parentUnit : directParentUnits) {
            if (parentUnit == null || parentUnit.semanticModel() == null)
                continue;

            ASTObject parent = parentUnit.semanticModel().astObject();
            for (ASTField field : parent.fields()) {
                fieldsByName.computeIfAbsent(field.symbol().name(), ignored -> new ArrayList<>())
                        .add(new ScopedSymbol(field.symbol(), parentUnit, field, null));
            }
            for (ASTMethod method : parent.methods()) {
                MethodKey key = new MethodKey(method.symbol().name(), parameterCount(method));
                methodsBySignature.computeIfAbsent(key, ignored -> new ArrayList<>())
                        .add(new ScopedSymbol(method.symbol(), parentUnit, null, method));
            }
        }

        for (Map.Entry<String, List<ScopedSymbol>> entry : fieldsByName.entrySet()) {
            if (entry.getValue().size() < 2 || declaresField(astObject, entry.getKey()))
                continue;

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Ambiguous inherited field '" + entry.getKey() + "' from multiple direct parents.",
                            inheritedConflictLine(entry.getValue())));
        }

        for (Map.Entry<MethodKey, List<ScopedSymbol>> entry : methodsBySignature.entrySet()) {
            MethodKey key = entry.getKey();
            if (entry.getValue().size() < 2 || declaresMethod(astObject, key.name(), key.arity()))
                continue;

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Ambiguous inherited method '" + key.name() + "' with arity " + key.arity()
                                    + " from multiple direct parents.",
                            inheritedConflictLine(entry.getValue())));
        }
    }

    private boolean declaresField(ASTObject astObject, String name) {
        for (ASTField field : astObject.fields())
            if (Objects.equals(field.symbol().name(), name))
                return true;
        return false;
    }

    private boolean declaresMethod(ASTObject astObject, String name, int arity) {
        for (ASTMethod method : astObject.methods())
            if (Objects.equals(method.symbol().name(), name) && parameterCount(method) == arity)
                return true;
        return false;
    }

    private int inheritedConflictLine(List<ScopedSymbol> inheritedSymbols) {
        for (ScopedSymbol symbol : inheritedSymbols) {
            if (symbol.field() != null)
                return symbol.field().line();
            if (symbol.method() != null)
                return symbol.method().line();
        }
        return -1;
    }

    private void mergeParentSymbols(SemanticScope objectScope, List<CompilationUnit> parentUnits) {
        for (CompilationUnit parentUnit : parentUnits) {
            if (parentUnit == null || parentUnit.semanticModel() == null)
                continue;

            ASTObject parent = parentUnit.semanticModel().astObject();
            for (ASTField field : parent.fields())
                objectScope.importSymbol(new ScopedSymbol(field.symbol(), parentUnit, field, null));
            for (ASTMethod method : parent.methods())
                objectScope.importSymbol(new ScopedSymbol(method.symbol(), parentUnit, null, method));
        }
    }

    private boolean hasInheritedField(SemanticScope parentScope, String name) {
        if (parentScope == null)
            return false;

        return parentScope.resolveAll(name).stream().anyMatch(s -> s.field() != null);
    }

    private ASTMethod findOverriddenMethod(List<CompilationUnit> directParentUnits, ASTMethod method) {
        if (directParentUnits == null || directParentUnits.isEmpty())
            return null;

        ASTMethod overridden = null;
        for (CompilationUnit parentUnit : directParentUnits) {
            if (parentUnit == null || parentUnit.semanticModel() == null)
                continue;

            SemanticScope parentScope = parentUnit.semanticModel().objectScope();
            ASTMethod candidate = parentScope.resolveAll(method.symbol().name()).stream()
                    .map(ScopedSymbol::method)
                    .filter(Objects::nonNull)
                    .filter(parentMethod -> parameterCount(parentMethod) == parameterCount(method))
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (candidate != null)
                overridden = candidate;
        }
        return overridden;
    }

    private boolean isSignatureCompatible(ASTMethod child, ASTMethod parent) {
        if (child == null || parent == null)
            return false;

        LPCType childReturn = child.symbol().lpcType();
        LPCType parentReturn = parent.symbol().lpcType();
        if (childReturn != null && parentReturn != null && childReturn != parentReturn)
            return false;

        List<LPCType> childParams = parameterTypes(child);
        List<LPCType> parentParams = parameterTypes(parent);

        if (childParams.size() != parentParams.size())
            return false;

        for (int i = 0; i < childParams.size(); i++) {
            LPCType childType = childParams.get(i);
            LPCType parentType = parentParams.get(i);
            if (childType != null && parentType != null && childType != parentType)
                return false;
        }

        return true;
    }

    private record MethodKey(String name, int arity) {}

    private List<LPCType> parameterTypes(ASTMethod method) {
        if (method.parameters() == null)
            return List.of();

        List<LPCType> types = new ArrayList<>(method.parameters().size());
        method.parameters().forEach(param -> types.add(param.symbol().lpcType()));
        return types;
    }

    private void resolveIdentifiers(
            ASTObject astObject, SemanticScope objectScope, CompilationUnit parentUnit, List<CompilationProblem> problems) {
        IdentifierResolver resolver =
                new IdentifierResolver(astObject.name(), objectScope, parentUnit, runtimeContext, typeResolver, problems);

        for (ASTField field : astObject.fields()) {
            if (field.initializer() != null)
                field.setInitializer(resolver.resolveExpression(field.initializer(), null));
        }

        for (ASTMethod method : astObject.methods())
            resolver.resolveMethod(method);
    }

        private static final class LocalResolutionContext {
            private final Deque<List<ASTLocal>> scopes = new ArrayDeque<>();
            private int inlineCallableDepth;

        void pushScope() {
            scopes.push(new ArrayList<>());
        }

        void popScope() {
            scopes.pop();
        }

        void declare(List<ASTLocal> locals) {
            if (locals == null || locals.isEmpty())
                return;

            scopes.peek().addAll(locals);
        }

        ASTLocal resolve(String name) {
            for (List<ASTLocal> scope : scopes) {
                for (int i = scope.size() - 1; i >= 0; i--) {
                    ASTLocal local = scope.get(i);
                    if (local.symbol().name().equals(name))
                        return local;
                }
            }

            return null;
        }

        void enterInlineCallable() {
            inlineCallableDepth++;
        }

        void exitInlineCallable() {
            if (inlineCallableDepth > 0)
                inlineCallableDepth--;
        }

        boolean insideInlineCallable() {
            return inlineCallableDepth > 0;
        }
    }

    private static final class IdentifierResolver {
        private final String currentObjectName;
        private final SemanticScope objectScope;
        private final CompilationUnit parentUnit;
        private final RuntimeContext runtimeContext;
        private final TypeResolver typeResolver;
        private final List<CompilationProblem> problems;

        IdentifierResolver(
                String currentObjectName,
                SemanticScope objectScope,
                CompilationUnit parentUnit,
                RuntimeContext runtimeContext,
                TypeResolver typeResolver,
                List<CompilationProblem> problems) {
            this.currentObjectName = currentObjectName;
            this.objectScope = objectScope;
            this.parentUnit = parentUnit;
            this.runtimeContext = runtimeContext;
            this.typeResolver = typeResolver;
            this.problems = problems;
        }

        void resolveMethod(ASTMethod method) {
            if (method.body() == null)
                return;

            LocalResolutionContext context = new LocalResolutionContext();
            context.pushScope();
            context.declare(localsAtDepth(method.locals(), 0));
            method.setBody(resolveBlock(method.body(), context));
            context.popScope();
        }

        private List<ASTLocal> localsAtDepth(List<ASTLocal> locals, int depth) {
            if (locals == null || locals.isEmpty())
                return List.of();

            List<ASTLocal> scoped = new ArrayList<>();
            for (ASTLocal local : locals) {
                if (local.scopeDepth() == depth)
                    scoped.add(local);
            }
            return scoped;
        }

        ASTExpression resolveExpression(ASTExpression expression, LocalResolutionContext context) {
            if (expression == null)
                return null;

            if (expression instanceof ASTExprUnresolvedIdentifier unresolvedIdentifier)
                return resolveIdentifier(unresolvedIdentifier, context);

            if (expression instanceof ASTExprUnresolvedAssignment unresolvedAssignment)
                return resolveAssignment(unresolvedAssignment, context);

            if (expression instanceof ASTExprUnresolvedMutation unresolvedMutation)
                return resolveMutation(unresolvedMutation, context);

            if (expression instanceof ASTExprUnresolvedCall unresolvedCall)
                return resolveCall(unresolvedCall, context);

            if (expression instanceof ASTExprUnresolvedParentCall unresolvedParentCall)
                return resolveParentCall(unresolvedParentCall, context);

            if (expression instanceof ASTExprUnresolvedQualifiedCall unresolvedQualifiedCall)
                return resolveQualifiedCall(unresolvedQualifiedCall, context);

            if (expression instanceof ASTExprUnresolvedInvoke unresolvedInvoke)
                return resolveInvoke(unresolvedInvoke, context);

            if (expression instanceof ASTExprFromEndIndex fromEnd) {
                ASTExpression resolvedDistance = resolveExpression(fromEnd.distance(), context);
                if (resolvedDistance == fromEnd.distance())
                    return fromEnd;
                return new ASTExprFromEndIndex(fromEnd.line(), resolvedDistance);
            }

            if (expression instanceof ASTExprClosureArgument closureArgument) {
                if (!context.insideInlineCallable()) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Closure argument $" + closureArgument.index()
                                            + " can only be used inside an inline callable.",
                                    closureArgument.line()));
                }
                return closureArgument;
            }

            if (expression instanceof ASTExprInlineCallable inlineCallable) {
                context.enterInlineCallable();
                ASTExpression resolvedBody = resolveExpression(inlineCallable.body(), context);
                context.exitInlineCallable();
                if (resolvedBody == inlineCallable.body())
                    return inlineCallable;
                return new ASTExprInlineCallable(inlineCallable.line(), resolvedBody);
            }

            if (expression instanceof ASTExprTypedFunctionLiteral typedFunction) {
                resolveTypedFunctionSymbol(typedFunction.returnSymbol(), typedFunction.line());
                context.pushScope();
                List<ASTLocal> parameterLocals = new ArrayList<>();
                for (ASTParameter parameter : typedFunction.parameters()) {
                    resolveTypedFunctionSymbol(parameter.symbol(), parameter.line());
                    parameterLocals.add(new ASTLocal(parameter.line(), parameter.symbol()));
                }
                context.declare(parameterLocals);
                ASTExpression resolvedBody = resolveExpression(typedFunction.body(), context);
                context.popScope();
                if (resolvedBody == typedFunction.body())
                    return typedFunction;
                return new ASTExprTypedFunctionLiteral(
                        typedFunction.line(),
                        typedFunction.returnSymbol(),
                        typedFunction.parameters(),
                        resolvedBody);
            }

            if (expression instanceof ASTExprCollectionTransform transform) {
                ASTExpression resolvedSource = resolveExpression(transform.source(), context);
                context.enterInlineCallable();
                ASTExpression resolvedBody = resolveExpression(transform.callback().body(), context);
                context.exitInlineCallable();
                List<ASTExpression> resolvedExtras = new ArrayList<>();
                boolean changed = resolvedSource != transform.source() || resolvedBody != transform.callback().body();
                for (ASTExpression extra : transform.extraArguments()) {
                    ASTExpression resolved = resolveExpression(extra, context);
                    changed |= resolved != extra;
                    resolvedExtras.add(resolved);
                }
                if (!changed)
                    return transform;
                return new ASTExprCollectionTransform(
                        transform.line(),
                        transform.operation(),
                        resolvedSource,
                        new ASTExprInlineCallable(transform.callback().line(), resolvedBody),
                        resolvedExtras);
            }

            if (expression instanceof ASTExprSortArray sortArray) {
                ASTExpression resolvedSource = resolveExpression(sortArray.source(), context);
                List<ASTExpression> resolvedExtras = new ArrayList<>();
                boolean extrasChanged = false;
                for (ASTExpression extra : sortArray.extraArguments()) {
                    ASTExpression resolvedExtra = resolveExpression(extra, context);
                    resolvedExtras.add(resolvedExtra);
                    if (resolvedExtra != extra)
                        extrasChanged = true;
                }
                context.enterInlineCallable();
                ASTExpression resolvedBody = resolveExpression(sortArray.comparator().body(), context);
                context.exitInlineCallable();
                if (resolvedSource == sortArray.source()
                        && resolvedBody == sortArray.comparator().body()
                        && !extrasChanged)
                    return sortArray;
                return new ASTExprSortArray(
                        sortArray.line(),
                        resolvedSource,
                        new ASTExprInlineCallable(sortArray.comparator().line(), resolvedBody),
                        resolvedExtras);
            }

            if (expression instanceof ASTExprDynamicInvoke dynamicInvoke) {
                ASTExpression resolvedTarget = resolveExpression(dynamicInvoke.target(), context);
                ASTArguments resolvedArgs = resolveArguments(dynamicInvoke.arguments(), context);
                if (resolvedTarget == dynamicInvoke.target() && resolvedArgs == dynamicInvoke.arguments())
                    return dynamicInvoke;
                return new ASTExprDynamicInvoke(
                        dynamicInvoke.line(),
                        resolvedTarget,
                        dynamicInvoke.methodName(),
                        resolvedArgs);
            }

            if (expression instanceof ASTExprLocalStore store) {
                ASTExpression resolvedValue = resolveExpression(store.value(), context);
                if (resolvedValue == store.value())
                    return store;
                return new ASTExprLocalStore(store.line(), store.local(), resolvedValue, store.isDeclarationInitializer());
            }

            if (expression instanceof ASTExprFieldStore store) {
                ASTExpression resolvedValue = resolveExpression(store.value(), context);
                if (resolvedValue == store.value())
                    return store;
                return new ASTExprFieldStore(store.line(), store.field(), resolvedValue);
            }

            if (expression instanceof ASTExprArrayStore store) {
                ASTExpression resolvedTarget = resolveExpression(store.target(), context);
                ASTExpression resolvedIndex = resolveExpression(store.index(), context);
                ASTExpression resolvedValue = resolveExpression(store.value(), context);
                ASTExpression value = buildIndexStoreValue(store, resolvedTarget, resolvedIndex, resolvedValue);

                if (resolvedTarget == store.target()
                        && resolvedIndex == store.index()
                        && value == store.value())
                    return store;

                return new ASTExprArrayStore(store.line(), resolvedTarget, resolvedIndex, AssignOpType.SET, value);
            }

            if (expression instanceof ASTExprArrayMutation mutation) {
                ASTExpression resolvedTarget = resolveExpression(mutation.target(), context);
                ASTExpression resolvedIndex = resolveExpression(mutation.index(), context);
                if (resolvedTarget == mutation.target() && resolvedIndex == mutation.index())
                    return mutation;
                return new ASTExprArrayMutation(
                        mutation.line(), resolvedTarget, resolvedIndex, mutation.delta(), mutation.isPrefix());
            }

            if (expression instanceof ASTExprArrayAccess access) {
                ASTExpression resolvedTarget = resolveExpression(access.target(), context);
                ASTExpression resolvedIndex = resolveExpression(access.index(), context);
                if (resolvedTarget == access.target() && resolvedIndex == access.index())
                    return access;
                return new ASTExprArrayAccess(access.line(), resolvedTarget, resolvedIndex);
            }

            if (expression instanceof ASTExprSliceAccess access) {
                ASTExpression resolvedTarget = resolveExpression(access.target(), context);
                ASTExpression resolvedStart = resolveExpression(access.start(), context);
                ASTExpression resolvedEnd = access.end() == null ? null : resolveExpression(access.end(), context);
                if (resolvedTarget == access.target() && resolvedStart == access.start() && resolvedEnd == access.end())
                    return access;
                return new ASTExprSliceAccess(access.line(), resolvedTarget, resolvedStart, resolvedEnd);
            }

            if (expression instanceof ASTExprSliceStore store) {
                ASTExpression resolvedTarget = resolveExpression(store.target(), context);
                ASTExpression resolvedStart = resolveExpression(store.start(), context);
                ASTExpression resolvedEnd = store.end() == null ? null : resolveExpression(store.end(), context);
                ASTExpression resolvedValue = resolveExpression(store.value(), context);
                if (resolvedTarget == store.target()
                        && resolvedStart == store.start()
                        && resolvedEnd == store.end()
                        && resolvedValue == store.value())
                    return store;
                return new ASTExprSliceStore(
                        store.line(), resolvedTarget, resolvedStart, resolvedEnd, resolvedValue);
            }

            if (expression instanceof ASTExprLocalAccess access) {
                return access;
            }

            if (expression instanceof ASTExprFieldAccess access) {
                return access;
            }

            if (expression instanceof ASTExprOpUnary unary) {
                ASTExpression resolvedRight = resolveExpression(unary.right(), context);
                if (resolvedRight == unary.right())
                    return unary;
                return new ASTExprOpUnary(unary.line(), resolvedRight, unary.operator());
            }

            if (expression instanceof ASTExprOpBinary binary) {
                ASTExpression resolvedLeft = resolveExpression(binary.left(), context);
                ASTExpression resolvedRight = resolveExpression(binary.right(), context);
                if (resolvedLeft == binary.left() && resolvedRight == binary.right())
                    return binary;
                return new ASTExprOpBinary(binary.line(), resolvedLeft, resolvedRight, binary.operator());
            }

            if (expression instanceof ASTExprSequence sequence) {
                List<ASTExpression> resolvedExpressions = new ArrayList<>();
                boolean changed = false;
                for (ASTExpression nested : sequence.expressions()) {
                    ASTExpression resolved = resolveExpression(nested, context);
                    changed |= resolved != nested;
                    resolvedExpressions.add(resolved);
                }
                if (!changed)
                    return sequence;
                return new ASTExprSequence(sequence.line(), resolvedExpressions);
            }

            if (expression instanceof ASTExprProtectedEval protectedEval) {
                ASTExpression resolvedBody = resolveExpression(protectedEval.body(), context);
                if (resolvedBody == protectedEval.body())
                    return protectedEval;
                return new ASTExprProtectedEval(
                        protectedEval.line(), resolvedBody, protectedEval.suppressLogging());
            }

            if (expression instanceof ASTExprTernary ternary) {
                ASTExpression resolvedCondition = resolveExpression(ternary.condition(), context);
                ASTExpression resolvedThen = resolveExpression(ternary.thenBranch(), context);
                ASTExpression resolvedElse = resolveExpression(ternary.elseBranch(), context);
                if (resolvedCondition == ternary.condition()
                        && resolvedThen == ternary.thenBranch()
                        && resolvedElse == ternary.elseBranch())
                    return ternary;
                return new ASTExprTernary(ternary.line(), resolvedCondition, resolvedThen, resolvedElse);
            }

            if (expression instanceof ASTExprCallMethod callMethod) {
                ASTArguments resolvedArgs = resolveArguments(callMethod.arguments(), context);
                if (resolvedArgs == callMethod.arguments())
                    return callMethod;
                return new ASTExprCallMethod(
                        callMethod.line(), callMethod.method(), resolvedArgs, callMethod.isParentDispatch());
            }

            if (expression instanceof ASTExprCallEfun callEfun) {
                ASTArguments resolvedArgs = resolveArguments(callEfun.arguments(), context);
                if (resolvedArgs == callEfun.arguments())
                    return callEfun;
                return new ASTExprCallEfun(callEfun.line(), callEfun.efun(), resolvedArgs);
            }

            if (expression instanceof ASTExprInvokeLocal invokeLocal) {
                ASTArguments resolvedArgs = resolveArguments(invokeLocal.arguments(), context);
                if (resolvedArgs == invokeLocal.arguments())
                    return invokeLocal;
                return new ASTExprInvokeLocal(invokeLocal.line(), invokeLocal.local(), invokeLocal.methodName(), resolvedArgs);
            }

            if (expression instanceof ASTExprInvokeField invokeField) {
                ASTArguments resolvedArgs = resolveArguments(invokeField.arguments(), context);
                if (resolvedArgs == invokeField.arguments())
                    return invokeField;
                return new ASTExprInvokeField(
                        invokeField.line(), invokeField.field(), invokeField.methodName(), resolvedArgs);
            }

            if (expression instanceof ASTExprArrayLiteral arrayLiteral) {
                List<ASTExpression> resolvedElements = new ArrayList<>();
                boolean changed = false;
                for (ASTExpression element : arrayLiteral.elements()) {
                    ASTExpression resolved = resolveExpression(element, context);
                    changed |= resolved != element;
                    resolvedElements.add(resolved);
                }
                if (!changed)
                    return arrayLiteral;
                return new ASTExprArrayLiteral(arrayLiteral.line(), resolvedElements);
            }

            if (expression instanceof ASTExprMappingLiteral mappingLiteral) {
                List<ASTExprMappingEntry> resolvedEntries = new ArrayList<>();
                boolean changed = false;
                for (ASTExprMappingEntry entry : mappingLiteral.entries()) {
                    ASTExpression resolvedKey = resolveExpression(entry.key(), context);
                    ASTExpression resolvedValue = resolveExpression(entry.value(), context);
                    changed |= resolvedKey != entry.key() || resolvedValue != entry.value();
                    resolvedEntries.add(new ASTExprMappingEntry(resolvedKey, resolvedValue));
                }
                if (!changed)
                    return mappingLiteral;
                return new ASTExprMappingLiteral(mappingLiteral.line(), resolvedEntries);
            }

            return expression;
        }

        private ASTExpression resolveInvoke(ASTExprUnresolvedInvoke unresolvedInvoke, LocalResolutionContext context) {
            ASTArguments resolvedArgs = resolveArguments(unresolvedInvoke.arguments(), context);
            ASTLocal local = resolveLocal(context, unresolvedInvoke.targetName());

            if (local != null)
                return new ASTExprInvokeLocal(unresolvedInvoke.line(), local, unresolvedInvoke.methodName(), resolvedArgs);

            ScopedSymbol scopedSymbol = resolveScopedSymbol(unresolvedInvoke.targetName());
            if (scopedSymbol != null && scopedSymbol.field() != null) {
                return new ASTExprInvokeField(
                        unresolvedInvoke.line(), scopedSymbol.field(), unresolvedInvoke.methodName(), resolvedArgs);
            }

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized invoke target '" + unresolvedInvoke.targetName() + "'",
                            unresolvedInvoke.line()));
            return new ASTExprError(unresolvedInvoke.line());
        }

        private ASTExpression resolveCall(ASTExprUnresolvedCall unresolvedCall, LocalResolutionContext context) {
            ASTArguments resolvedArgs = resolveArguments(unresolvedCall.arguments(), context);
            ASTExprCollectionTransform transform = collectionTransform(unresolvedCall, resolvedArgs);
            if (transform != null)
                return transform;

            ASTExprSortArray sortArray = sortArrayTransform(unresolvedCall, resolvedArgs);
            if (sortArray != null)
                return sortArray;

            ASTMethod method = resolveMethod(unresolvedCall.name(), resolvedArgs.size());

            if (method != null)
                return new ASTExprCallMethod(unresolvedCall.line(), method, resolvedArgs);

            Efun efun = resolveDirectEfun(unresolvedCall.name(), resolvedArgs.size());

            if (efun != null)
                return new ASTExprCallEfun(unresolvedCall.line(), efun, resolvedArgs);

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized method or function '" + unresolvedCall.name() + "'.",
                            unresolvedCall.line()));
            return new ASTExprError(unresolvedCall.line());
        }

        private void resolveTypedFunctionSymbol(Symbol symbol, int line) {
            if (symbol == null || symbol.lpcType() != null)
                return;

            LPCType resolved = typeResolver.resolve(symbol.declaredTypeName());
            if (resolved != null) {
                symbol.resolveDeclaredType(resolved);
                return;
            }

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unknown type '" + symbol.declaredTypeName() + "' in typed function literal.",
                            line));
            symbol.resolveDeclaredType(LPCType.LPCMIXED);
        }

        private ASTExprCollectionTransform collectionTransform(ASTExprUnresolvedCall unresolvedCall, ASTArguments args) {
            if (!"filter".equals(unresolvedCall.name()) && !"map".equals(unresolvedCall.name()))
                return null;

            if (args == null || args.size() < 2)
                return null;

            ASTExpression callback = args.get(1).expression();
            if (!(callback instanceof ASTExprInlineCallable inlineCallable))
                return null;

            List<ASTExpression> extras = new ArrayList<>();
            for (int i = 2; i < args.size(); i++)
                extras.add(args.get(i).expression());

            ASTExprCollectionTransform.Operation operation = "filter".equals(unresolvedCall.name())
                    ? ASTExprCollectionTransform.Operation.FILTER
                    : ASTExprCollectionTransform.Operation.MAP;
            return new ASTExprCollectionTransform(
                    unresolvedCall.line(), operation, args.get(0).expression(), inlineCallable, extras);
        }

        private ASTExprSortArray sortArrayTransform(ASTExprUnresolvedCall unresolvedCall, ASTArguments args) {
            if (!"sort_array".equals(unresolvedCall.name()))
                return null;

            if (args == null || args.size() < 2)
                return null;

            ASTExpression callback = args.get(1).expression();
            if (!(callback instanceof ASTExprInlineCallable inlineCallable))
                return null;

            List<ASTExpression> extras = new ArrayList<>();
            for (int i = 2; i < args.size(); i++)
                extras.add(args.get(i).expression());

            return new ASTExprSortArray(unresolvedCall.line(), args.get(0).expression(), inlineCallable, extras);
        }

        private ASTExpression resolveQualifiedCall(
                ASTExprUnresolvedQualifiedCall unresolvedCall, LocalResolutionContext context) {
            ASTArguments resolvedArgs = resolveArguments(unresolvedCall.arguments(), context);

            if ("efun".equals(unresolvedCall.qualifier())) {
                Efun efun = resolveDirectEfun(unresolvedCall.name(), resolvedArgs.size());

                if (efun != null)
                    return new ASTExprCallEfun(unresolvedCall.line(), efun, resolvedArgs);

                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Unrecognized efun '" + unresolvedCall.name() + "'.",
                                unresolvedCall.line()));
                return new ASTExprError(unresolvedCall.line());
            }

            ASTExpression inheritedCall = resolveQualifiedPrimaryParentCall(unresolvedCall, resolvedArgs);
            if (inheritedCall != null)
                return inheritedCall;

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unsupported qualified call prefix '" + unresolvedCall.qualifier() + "'.",
                            unresolvedCall.line()));
            return new ASTExprError(unresolvedCall.line());
        }

        /**
         * Resolves {@code parentName::method()} when {@code parentName} names the primary inherited
         * object, which can be emitted as a JVM {@code invokespecial} dispatch.
         */
        private ASTExpression resolveQualifiedPrimaryParentCall(
                ASTExprUnresolvedQualifiedCall unresolvedCall, ASTArguments resolvedArgs) {
            if (parentUnit == null || parentUnit.semanticModel() == null)
                return null;

            ASTObject parentObject = parentUnit.semanticModel().astObject();
            if (parentObject == null || !matchesObjectQualifier(unresolvedCall.qualifier(), parentObject.name()))
                return null;

            SemanticScope parentScope = parentUnit.semanticModel().objectScope();
            ASTMethod parentMethod = parentScope.resolveAll(unresolvedCall.name()).stream()
                    .map(ScopedSymbol::method)
                    .filter(Objects::nonNull)
                    .filter(method -> parameterCount(method) >= resolvedArgs.size())
                    .min(Comparator.comparingInt(SemanticAnalyzer::parameterCount))
                    .orElse(null);
            if (parentMethod != null)
                return new ASTExprCallMethod(unresolvedCall.line(), parentMethod, resolvedArgs, true);

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Inherited method '" + unresolvedCall.name() + "' is not defined in qualified parent '"
                                    + unresolvedCall.qualifier() + "'.",
                            unresolvedCall.line()));
            return new ASTExprError(unresolvedCall.line());
        }

        private boolean matchesObjectQualifier(String qualifier, String objectName) {
            if (Objects.equals(qualifier, objectName))
                return true;

            int slash = objectName.lastIndexOf('/');
            String simpleName = slash == -1 ? objectName : objectName.substring(slash + 1);
            return Objects.equals(qualifier, simpleName);
        }

        private Efun resolveDirectEfun(String name, int arity) {
            Efun efun = runtimeContext.resolveEfun(name, arity);
            if (efun != null)
                return efun;

            String engineName = runtimeContext.directEfunName(name);
            return name.equals(engineName) ? null : runtimeContext.resolveEfun(engineName, arity);
        }

        private ASTExpression resolveParentCall(
                ASTExprUnresolvedParentCall unresolvedParentCall, LocalResolutionContext context) {
            ASTArguments resolvedArgs = resolveArguments(unresolvedParentCall.arguments(), context);
            SemanticScope parentScope = (objectScope != null) ? objectScope.parent() : null;

            if (parentScope == null || parentUnit == null || parentUnit.semanticModel() == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Cannot call inherited method '" + unresolvedParentCall.name() + "' without a parent object.",
                                unresolvedParentCall.line()));
                return new ASTExprError(unresolvedParentCall.line());
            }

            ScopedSymbol parentSymbol = parentScope.resolveLocally(unresolvedParentCall.name());
            ASTMethod parentMethod = (parentSymbol != null) ? parentSymbol.method() : null;
            if (parentMethod != null)
                return new ASTExprCallMethod(unresolvedParentCall.line(), parentMethod, resolvedArgs, true);

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Inherited method '" + unresolvedParentCall.name() + "' is not defined in the parent object.",
                            unresolvedParentCall.line()));
            return new ASTExprError(unresolvedParentCall.line());
        }

        private ASTExpression resolveAssignment(
                ASTExprUnresolvedAssignment unresolvedAssignment, LocalResolutionContext context) {
            ASTExpression resolvedValue = resolveExpression(unresolvedAssignment.value(), context);
            ASTLocal local = resolveLocal(context, unresolvedAssignment.name());

            if (local != null)
                return buildLocalStore(unresolvedAssignment, local, resolvedValue);

            ScopedSymbol scopedSymbol = resolveFieldSymbol(unresolvedAssignment.name());
            if (scopedSymbol != null && scopedSymbol.field() != null)
                return buildFieldStore(unresolvedAssignment, scopedSymbol.field(), resolvedValue);

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized local or field '" + unresolvedAssignment.name() + "'.",
                            unresolvedAssignment.line()));
            return new ASTExprError(unresolvedAssignment.line());
        }

        private ASTExpression resolveMutation(ASTExprUnresolvedMutation mutation, LocalResolutionContext context) {
            ASTLocal local = resolveLocal(context, mutation.name());
            if (local != null)
                return new ASTExprLocalMutation(
                        mutation.line(), local, mutation.delta(), mutation.isPrefix());

            ScopedSymbol scopedSymbol = resolveFieldSymbol(mutation.name());
            if (scopedSymbol != null && scopedSymbol.field() != null)
                return new ASTExprFieldMutation(
                        mutation.line(), scopedSymbol.field(), mutation.delta(), mutation.isPrefix());

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized local or field '" + mutation.name() + "'.",
                            mutation.line()));
            return new ASTExprError(mutation.line());
        }

        private ASTExpression resolveIdentifier(
                ASTExprUnresolvedIdentifier unresolvedIdentifier, LocalResolutionContext context) {
            ASTLocal local = resolveLocal(context, unresolvedIdentifier.name());
            if (local != null)
                return new ASTExprLocalAccess(unresolvedIdentifier.line(), local);

            ScopedSymbol scopedSymbol = resolveFieldSymbol(unresolvedIdentifier.name());
            if (scopedSymbol != null && scopedSymbol.field() != null)
                return new ASTExprFieldAccess(unresolvedIdentifier.line(), scopedSymbol.field());

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized local or field '" + unresolvedIdentifier.name() + "'.",
                            unresolvedIdentifier.line()));
            return new ASTExprError(unresolvedIdentifier.line());
        }

        private ASTExprFieldStore buildFieldStore(
                ASTExprUnresolvedAssignment assignment, ASTField field, ASTExpression resolvedValue) {
            ASTExpression value = resolvedValue;
            BinaryOpType compoundOp = compoundAssignmentOperator(assignment.operator());
            if (compoundOp != null)
                value = new ASTExprOpBinary(
                        assignment.line(), new ASTExprFieldAccess(assignment.line(), field), resolvedValue, compoundOp);

            return new ASTExprFieldStore(assignment.line(), field, value);
        }

        private ASTExprLocalStore buildLocalStore(
                ASTExprUnresolvedAssignment assignment, ASTLocal local, ASTExpression resolvedValue) {
            ASTExpression value = resolvedValue;
            BinaryOpType compoundOp = compoundAssignmentOperator(assignment.operator());
            if (compoundOp != null)
                value = new ASTExprOpBinary(
                        assignment.line(), new ASTExprLocalAccess(assignment.line(), local), resolvedValue, compoundOp);

            return new ASTExprLocalStore(assignment.line(), local, value);
        }

        private ASTExpression buildIndexStoreValue(
                ASTExprArrayStore store,
                ASTExpression resolvedTarget,
                ASTExpression resolvedIndex,
                ASTExpression resolvedValue) {
            BinaryOpType compoundOp = compoundAssignmentOperator(store.operator());
            if (compoundOp == null)
                return resolvedValue;

            return new ASTExprOpBinary(
                    store.line(),
                    new ASTExprArrayAccess(store.line(), resolvedTarget, resolvedIndex),
                    resolvedValue,
                    compoundOp);
        }

        private BinaryOpType compoundAssignmentOperator(AssignOpType op) {
            return switch (op) {
            case SET -> null;
            case ADD -> BinaryOpType.BOP_ADD;
            case SUB -> BinaryOpType.BOP_SUB;
            case MULT -> BinaryOpType.BOP_MULT;
            case DIV -> BinaryOpType.BOP_DIV;
            case BIT_OR -> BinaryOpType.BOP_BIT_OR;
            case BIT_AND -> BinaryOpType.BOP_BIT_AND;
            case BIT_XOR -> BinaryOpType.BOP_BIT_XOR;
            case LOGICAL_OR -> BinaryOpType.BOP_OR;
            case LOGICAL_AND -> BinaryOpType.BOP_AND;
            case SHL -> BinaryOpType.BOP_SHL;
            case SHR -> BinaryOpType.BOP_SHR;
            };
        }

        private ASTMethod resolveMethod(String name, int arity) {
            if (objectScope == null)
                return null;

            return objectScope.resolveAll(name).stream()
                    .map(ScopedSymbol::method)
                    .filter(Objects::nonNull)
                    .filter(method -> parameterCount(method) >= arity)
                    .min(Comparator.comparingInt(SemanticAnalyzer::parameterCount)
                            .thenComparingInt(this::currentObjectPreference))
                    .orElse(null);
        }

        private int currentObjectPreference(ASTMethod method) {
            return Objects.equals(method.ownerName(), currentObjectName) ? 0 : 1;
        }

        private ScopedSymbol resolveScopedSymbol(String name) {
            if (objectScope == null)
                return null;

            return objectScope.resolve(name);
        }

        private ScopedSymbol resolveFieldSymbol(String name) {
            if (objectScope == null)
                return null;

            return objectScope.resolveAll(name).stream()
                    .filter(symbol -> symbol.field() != null)
                    .reduce((first, second) -> second)
                    .orElse(null);
        }

        private ASTLocal resolveLocal(LocalResolutionContext context, String name) {
            if (context == null)
                return null;

            return context.resolve(name);
        }

        private ASTArguments resolveArguments(ASTArguments arguments, LocalResolutionContext context) {
            if (arguments == null)
                return null;

            ASTArguments resolvedArgs = new ASTArguments(arguments.line());
            boolean changed = false;
            for (ASTArgument argument : arguments) {
                ASTExpression resolvedExpr = resolveExpression(argument.expression(), context);
                changed |= resolvedExpr != argument.expression();
                resolvedArgs.add(new ASTArgument(argument.line(), resolvedExpr));
            }

            return changed ? resolvedArgs : arguments;
        }

        private ASTStmtBlock resolveBlock(ASTStmtBlock block, LocalResolutionContext context) {
            if (block == null)
                return null;

            context.pushScope();
            List<ASTStatement> resolvedStatements = new ArrayList<>(block.statements().size());
            List<ASTStmtBlock.BlockLocalDeclaration> localDeclarations = block.localDeclarations();
            int declarationCursor = 0;

            for (int i = 0; i <= block.statements().size(); i++) {
                while (declarationCursor < localDeclarations.size()
                        && localDeclarations.get(declarationCursor).statementIndex() == i) {
                    context.declare(localDeclarations.get(declarationCursor).locals());
                    declarationCursor++;
                }

                if (i == block.statements().size())
                    break;

                resolvedStatements.add(resolveStatement(block.statements().get(i), context));
            }

            context.popScope();
            return new ASTStmtBlock(block.line(), resolvedStatements, localDeclarations);
        }

        private ASTStatement resolveStatement(ASTStatement statement, LocalResolutionContext context) {
            if (statement == null)
                return null;

            if (statement instanceof ASTStmtBlock block)
                return resolveBlock(block, context);

            if (statement instanceof ASTStmtExpression stmtExpression) {
                ASTExpression resolved = resolveExpression(stmtExpression.expression(), context);
                if (resolved == stmtExpression.expression())
                    return stmtExpression;
                return new ASTStmtExpression(stmtExpression.line(), resolved);
            }

            if (statement instanceof ASTStmtEmpty)
                return statement;

            if (statement instanceof ASTStmtIfThenElse stmtIf) {
                ASTExpression resolvedCondition = resolveExpression(stmtIf.condition(), context);
                ASTStatement resolvedThen = resolveStatement(stmtIf.thenBranch(), context);
                ASTStatement resolvedElse = resolveStatement(stmtIf.elseBranch(), context);
                if (resolvedCondition == stmtIf.condition()
                        && resolvedThen == stmtIf.thenBranch()
                        && resolvedElse == stmtIf.elseBranch())
                    return stmtIf;
                return new ASTStmtIfThenElse(stmtIf.line(), resolvedCondition, resolvedThen, resolvedElse);
            }

            if (statement instanceof ASTStmtFor stmtFor) {
                context.pushScope();
                context.declare(stmtFor.initializerLocals());
                ASTExpression resolvedInit = resolveExpression(stmtFor.initializer(), context);
                ASTExpression resolvedCondition = resolveExpression(stmtFor.condition(), context);
                ASTExpression resolvedUpdate = resolveExpression(stmtFor.update(), context);
                ASTStatement resolvedBody = resolveStatement(stmtFor.body(), context);
                context.popScope();
                if (resolvedInit == stmtFor.initializer()
                        && resolvedCondition == stmtFor.condition()
                        && resolvedUpdate == stmtFor.update()
                        && resolvedBody == stmtFor.body())
                    return stmtFor;
                return new ASTStmtFor(
                        stmtFor.line(),
                        stmtFor.initializerLocals(),
                        resolvedInit,
                        resolvedCondition,
                        resolvedUpdate,
                        resolvedBody);
            }

            if (statement instanceof ASTStmtForeach stmtForeach) {
                ASTExpression resolvedIterable = resolveExpression(stmtForeach.iterable(), context);
                context.pushScope();
                context.declare(List.of(stmtForeach.keyLocal()));
                if (stmtForeach.valueLocal() != null)
                    context.declare(List.of(stmtForeach.valueLocal()));
                ASTStatement resolvedBody = resolveStatement(stmtForeach.body(), context);
                context.popScope();
                if (resolvedIterable == stmtForeach.iterable() && resolvedBody == stmtForeach.body())
                    return stmtForeach;
                return new ASTStmtForeach(
                        stmtForeach.line(),
                        stmtForeach.keyLocal(),
                        stmtForeach.valueLocal(),
                        resolvedIterable,
                        resolvedBody);
            }

            if (statement instanceof ASTStmtWhile stmtWhile) {
                ASTExpression resolvedCondition = resolveExpression(stmtWhile.condition(), context);
                ASTStatement resolvedBody = resolveStatement(stmtWhile.body(), context);
                if (resolvedCondition == stmtWhile.condition() && resolvedBody == stmtWhile.body())
                    return stmtWhile;
                return new ASTStmtWhile(stmtWhile.line(), resolvedCondition, resolvedBody);
            }

            if (statement instanceof ASTStmtDoWhile stmtDoWhile) {
                ASTStatement resolvedBody = resolveStatement(stmtDoWhile.body(), context);
                ASTExpression resolvedCondition = resolveExpression(stmtDoWhile.condition(), context);
                if (resolvedBody == stmtDoWhile.body() && resolvedCondition == stmtDoWhile.condition())
                    return stmtDoWhile;
                return new ASTStmtDoWhile(stmtDoWhile.line(), resolvedBody, resolvedCondition);
            }

            if (statement instanceof ASTStmtSwitch stmtSwitch) {
                ASTExpression resolvedExpression = resolveExpression(stmtSwitch.expression(), context);
                List<ASTStmtSwitch.SwitchCase> resolvedCases = new ArrayList<>();
                boolean changed = resolvedExpression != stmtSwitch.expression();
                for (ASTStmtSwitch.SwitchCase switchCase : stmtSwitch.cases()) {
                    ASTExpression resolvedCaseExpression = switchCase.isDefault()
                            ? null
                            : resolveExpression(switchCase.expression(), context);
                    ASTExpression resolvedRangeEndExpression = switchCase.isRange()
                            ? resolveExpression(switchCase.rangeEndExpression(), context)
                            : null;
                    List<ASTStatement> resolvedStatements = new ArrayList<>();
                    for (ASTStatement nested : switchCase.statements()) {
                        ASTStatement resolvedNested = resolveStatement(nested, context);
                        resolvedStatements.add(resolvedNested);
                        if (resolvedNested != nested)
                            changed = true;
                    }
                    if (resolvedCaseExpression != switchCase.expression())
                        changed = true;
                    if (resolvedRangeEndExpression != switchCase.rangeEndExpression())
                        changed = true;
                    resolvedCases.add(new ASTStmtSwitch.SwitchCase(
                            switchCase.line(),
                            resolvedCaseExpression,
                            resolvedRangeEndExpression,
                            switchCase.isDefault(),
                            resolvedStatements));
                }
                if (!changed)
                    return stmtSwitch;
                return new ASTStmtSwitch(stmtSwitch.line(), resolvedExpression, resolvedCases);
            }

        if (statement instanceof ASTStmtBreak stmtBreak)
            return stmtBreak;

        if (statement instanceof ASTStmtContinue stmtContinue)
            return stmtContinue;

        if (statement instanceof ASTStmtReturn stmtReturn) {
                ASTExpression resolved = resolveExpression(stmtReturn.returnValue(), context);
                if (resolved == stmtReturn.returnValue())
                    return stmtReturn;
                return new ASTStmtReturn(stmtReturn.line(), resolved);
            }

            return statement;
        }
    }

    private static final class LocalSlotAllocator {
        private final Deque<ScopeFrame> scopes = new ArrayDeque<>();
        private final Deque<Integer> freeSlots = new ArrayDeque<>();
        private int currentDepth = 0;
        private int nextSlot;

        LocalSlotAllocator(int parameterCount) {
            nextSlot = parameterCount + 1; // slot 0 reserved for "this"
            scopes.push(new ScopeFrame());
        }

        void place(ASTLocal local, List<CompilationProblem> problems) {
            int targetDepth = Math.max(local.scopeDepth(), 0);
            alignScopes(targetDepth);

            ScopeFrame frame = scopes.peek();
            List<ASTLocal> namedLocals = frame.locals.computeIfAbsent(local.symbol().name(), ignored -> new ArrayList<>());
            boolean duplicate = namedLocals.stream()
                    .anyMatch(existing -> existing != local && existing.scopeId() == local.scopeId());
            if (duplicate) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Duplicate local '" + local.symbol().name() + "' in scope",
                                local.line()));
            }

            int slot = (!freeSlots.isEmpty()) ? freeSlots.pop() : nextSlot++;
            local.setSlot(slot);
            local.setScopeDepth(targetDepth);
            namedLocals.add(local);
        }

        private void alignScopes(int targetDepth) {
            while (currentDepth > targetDepth)
                releaseScope();

            while (currentDepth < targetDepth)
                openScope();
        }

        private void openScope() {
            scopes.push(new ScopeFrame());
            currentDepth++;
        }

        private void releaseScope() {
            ScopeFrame expired = scopes.pop();
            for (List<ASTLocal> locals : expired.locals.values()) {
                for (ASTLocal local : locals) {
                    if (local.slot() >= 0)
                        freeSlots.push(local.slot());
                }
            }
            currentDepth--;
        }

        private static final class ScopeFrame {
            private final Map<String, List<ASTLocal>> locals = new HashMap<>();
        }
    }

}
