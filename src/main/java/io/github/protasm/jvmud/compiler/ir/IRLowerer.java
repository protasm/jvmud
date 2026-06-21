package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArgument;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
import io.github.protasm.jvmud.compiler.parser.ast.ASTInherit;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameters;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprClosureArgument;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCollectionTransform;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFromEndIndex;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFunctionReference;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInlineCallable;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeField;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralFloat;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralTrue;
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
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSymbolLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprTernary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprTypedFunctionLiteral;
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
import io.github.protasm.jvmud.compiler.parser.type.BinaryOpType;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.parser.type.UnaryOpType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeTypes;
import io.github.protasm.jvmud.compiler.runtime.RuntimeValueKind;
import io.github.protasm.jvmud.compiler.pipeline.CompilationUnit;
import io.github.protasm.jvmud.compiler.semantic.SemanticModel;
import io.github.protasm.jvmud.compiler.semantic.SemanticScope;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Lowers typed AST nodes into the JVM-oriented IR model. */
public final class IRLowerer {
    private final String defaultParentInternalName;

    public IRLowerer(String defaultParentInternalName) {
        this.defaultParentInternalName = Objects.requireNonNull(defaultParentInternalName, "defaultParentInternalName");
    }

    public IRLoweringResult lower(SemanticModel semanticModel) {
        if (semanticModel == null)
            throw new IllegalArgumentException("SemanticModel cannot be null.");

        List<CompilationProblem> problems = new ArrayList<>();
        ASTObject astObject = semanticModel.astObject();
        String objectInternalName = astObject.name();
        String parentInternalName =
                (astObject.parentName() != null) ? astObject.parentName() : defaultParentInternalName;
        SemanticScope objectScope = semanticModel.objectScope();
        Set<String> primaryParentLineage = primaryParentLineage(semanticModel.compilationUnit(), parentInternalName);

        Map<Symbol, IRField> fieldsBySymbol = new HashMap<>();
        List<IRField> flattenedInheritedFields = new ArrayList<>();
        Set<String> flattenedInheritedMethodOwners = new HashSet<>();
        importInheritedFields(
                objectScope,
                objectScope,
                declaredFieldSymbols(astObject),
                fieldsBySymbol,
                flattenedInheritedFields,
                objectInternalName,
                parentInternalName,
                primaryParentLineage,
                problems);
        importSecondaryParentScopeFields(
                objectScope,
                declaredFieldSymbols(astObject),
                fieldsBySymbol,
                flattenedInheritedFields,
                objectInternalName,
                parentInternalName,
                primaryParentLineage,
                problems);
        List<IRField> fields = lowerFields(astObject.fields(), fieldsBySymbol, problems, objectInternalName);
        fields.addAll(0, flattenedInheritedFields);
        List<IRMethod> methods = lowerMethods(
                astObject,
                objectScope,
                fieldsBySymbol,
                flattenedInheritedMethodOwners,
                problems,
                objectInternalName,
                parentInternalName,
                primaryParentLineage);

        TypedIR typedIr = new TypedIR(new IRObject(
                astObject.line(),
                astObject.name(),
                parentInternalName,
                inheritedProgramPaths(astObject, semanticModel.compilationUnit()),
                fields,
                methods));

        return new IRLoweringResult(typedIr, problems);
    }

    private List<String> inheritedProgramPaths(ASTObject astObject, CompilationUnit unit) {
        Set<String> paths = new LinkedHashSet<>();
        collectInheritedProgramPaths(astObject, unit, paths);
        return new ArrayList<>(paths);
    }

    private void collectInheritedProgramPaths(ASTObject astObject, CompilationUnit unit, Set<String> paths) {
        if (astObject == null) {
            return;
        }
        List<CompilationUnit> parentUnits = unit != null ? unit.directParentUnits() : List.of();
        int parentIndex = 0;
        for (ASTInherit inherit : astObject.inherits()) {
            String path = normalizeInheritPath(inherit.path());
            if (path != null && !path.isBlank()) {
                paths.add(path);
            }

            CompilationUnit parentUnit = parentIndex < parentUnits.size() ? parentUnits.get(parentIndex) : null;
            parentIndex++;
            if (parentUnit != null && parentUnit.astObject() != null) {
                collectInheritedProgramPaths(parentUnit.astObject(), parentUnit, paths);
            }
        }
    }

    private String normalizeInheritPath(String rawPath) {
        if (rawPath == null) {
            return null;
        }
        String path = rawPath.trim();
        if (path.length() >= 2 && path.startsWith("\"") && path.endsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isBlank()) {
            return null;
        }
        if (!path.endsWith(".c")) {
            path += ".c";
        }
        return "/" + path;
    }

    private List<IRField> lowerFields(
            Iterable<ASTField> astFields,
            Map<Symbol, IRField> fieldsBySymbol,
            List<CompilationProblem> problems,
            String ownerInternalName) {
        List<IRField> fields = new ArrayList<>();

        for (ASTField field : astFields) {
            RuntimeType fieldType = runtimeType(field.symbol().lpcType());
            IRExpression initializer =
                    lowerFieldInitializer(field.initializer(), fieldType, fieldsBySymbol, problems, ownerInternalName);
            IRField irField = new IRField(
                    field.line(), ownerInternalName, field.symbol().name(), fieldType, initializer);
            fields.add(irField);
            fieldsBySymbol.put(field.symbol(), irField);
        }

        return fields;
    }

    private Set<Symbol> declaredFieldSymbols(ASTObject astObject) {
        Set<Symbol> symbols = new HashSet<>();
        if (astObject == null)
            return symbols;

        for (ASTField field : astObject.fields())
            symbols.add(field.symbol());
        return symbols;
    }

    private IRExpression lowerFieldInitializer(
            ASTExpression initializer,
            RuntimeType fieldType,
            Map<Symbol, IRField> fieldsBySymbol,
            List<CompilationProblem> problems,
            String ownerInternalName) {
        if (initializer == null)
            return null;

        MethodContext context = new MethodContext(fieldType, fieldsBySymbol, ownerInternalName);
        IRExpression lowered = lowerExpression(initializer, context, problems);
        return coerceIfNeeded(lowered, fieldType);
    }

    /**
     * Imports fields already resolved by semantic analysis. Fields from secondary
     * parents are flattened onto the child because JVM bytecode can only inherit
     * storage from one superclass; the primary parent lineage stays on the JVM
     * superclass chain so overridden members continue to resolve through Java's
     * normal dispatch.
     */
    private void importInheritedFields(
            SemanticScope scope,
            SemanticScope objectScope,
            Set<Symbol> declaredFieldSymbols,
            Map<Symbol, IRField> fieldsBySymbol,
            List<IRField> flattenedInheritedFields,
            String objectInternalName,
            String parentInternalName,
            Set<String> primaryParentLineage,
            List<CompilationProblem> problems) {
        if (scope == null)
            return;

        for (List<SemanticScope.ScopedSymbol> scopedSymbols : scope.symbols().values()) {
            for (SemanticScope.ScopedSymbol scopedSymbol : scopedSymbols) {
                if (scopedSymbol == null || scopedSymbol.field() == null)
                    continue;

                Symbol symbol = scopedSymbol.symbol();
                if (declaredFieldSymbols.contains(symbol))
                    continue;

                if (fieldsBySymbol.containsKey(symbol))
                    continue;

                RuntimeType type = runtimeType(symbol.lpcType());
                String ownerInternalName = scopedSymbol.field().ownerName();
                if (shouldFlattenInheritedField(scope, objectScope, ownerInternalName, primaryParentLineage)) {
                    IRField flattenedField = new IRField(
                            scopedSymbol.field().line(),
                            objectInternalName,
                            symbol.name(),
                            type,
                            lowerFieldInitializer(
                                    scopedSymbol.field().initializer(),
                                    type,
                                    fieldsBySymbol,
                                    problems,
                                    objectInternalName));
                    fieldsBySymbol.put(symbol, flattenedField);
                    flattenedInheritedFields.add(flattenedField);
                    continue;
                }

                IRField inheritedField = new IRField(
                        scopedSymbol.field().line(),
                        (ownerInternalName != null) ? ownerInternalName : defaultParentInternalName,
                        symbol.name(),
                        type,
                        null);
                fieldsBySymbol.put(symbol, inheritedField);
            }
        }

        importInheritedFields(
                scope.parent(),
                objectScope,
                declaredFieldSymbols,
                fieldsBySymbol,
                flattenedInheritedFields,
                objectInternalName,
                parentInternalName,
                primaryParentLineage,
                problems);
    }

    private boolean shouldFlattenInheritedField(
            SemanticScope scope, SemanticScope objectScope, String ownerInternalName, Set<String> primaryParentLineage) {
        if (scope != objectScope)
            return false;

        if (ownerInternalName == null)
            return false;

        return !primaryParentLineage.contains(ownerInternalName);
    }

    /**
     * Imports the analyzed field symbols visible to secondary direct parents. Flattened secondary
     * methods keep references to those exact symbols, so name-equivalent fields imported through the
     * child scope are not enough for method-body lowering.
     */
    private void importSecondaryParentScopeFields(
            SemanticScope objectScope,
            Set<Symbol> declaredFieldSymbols,
            Map<Symbol, IRField> fieldsBySymbol,
            List<IRField> flattenedInheritedFields,
            String objectInternalName,
            String parentInternalName,
            Set<String> primaryParentLineage,
            List<CompilationProblem> problems) {
        if (objectScope == null)
            return;

        Set<CompilationUnit> secondaryParentUnits =
                visibleSecondaryParentUnits(objectScope, objectInternalName, primaryParentLineage);
        for (CompilationUnit parentUnit : secondaryParentUnits) {
            for (ASTField field : parentUnit.semanticModel().astObject().fields()) {
                importVisibleInheritedField(
                        field,
                        declaredFieldSymbols,
                        fieldsBySymbol,
                        flattenedInheritedFields,
                        objectInternalName,
                        parentInternalName,
                        primaryParentLineage,
                        problems);
            }

            SemanticScope parentScope = parentUnit.semanticModel().objectScope();
            for (List<SemanticScope.ScopedSymbol> scopedSymbols : parentScope.symbols().values()) {
                for (SemanticScope.ScopedSymbol scopedSymbol : scopedSymbols) {
                    if (scopedSymbol == null || scopedSymbol.field() == null)
                        continue;

                    importVisibleInheritedField(
                            scopedSymbol,
                            declaredFieldSymbols,
                            fieldsBySymbol,
                            flattenedInheritedFields,
                            objectInternalName,
                            parentInternalName,
                            primaryParentLineage,
                            problems);
                }
            }
        }
    }

    /**
     * Imports a concrete field declared by a secondary parent unit. Flattened methods may reference
     * private fields that are not visible through child semantic scope, but their storage still has
     * to travel with the method body when JVMud lowers multiple LPC inherits onto one Java class.
     */
    private void importVisibleInheritedField(
            ASTField field,
            Set<Symbol> declaredFieldSymbols,
            Map<Symbol, IRField> fieldsBySymbol,
            List<IRField> flattenedInheritedFields,
            String objectInternalName,
            String parentInternalName,
            Set<String> primaryParentLineage,
            List<CompilationProblem> problems) {
        if (field == null)
            return;

        Symbol symbol = field.symbol();
        if (declaredFieldSymbols.contains(symbol))
            return;

        RuntimeType type = runtimeType(symbol.lpcType());
        String ownerInternalName = field.ownerName();
        if (ownerInternalName != null && !primaryParentLineage.contains(ownerInternalName)) {
            IRField mapped = fieldsBySymbol.get(symbol);
            if (mapped != null && Objects.equals(mapped.ownerInternalName(), objectInternalName))
                return;

            IRField existing = findField(fieldsBySymbol, objectInternalName, symbol.name());
            if (existing != null) {
                fieldsBySymbol.put(symbol, existing);
                return;
            }
            IRField flattenedField = new IRField(
                    field.line(),
                    objectInternalName,
                    symbol.name(),
                    type,
                    lowerFieldInitializer(field.initializer(), type, fieldsBySymbol, problems, objectInternalName));
            fieldsBySymbol.put(symbol, flattenedField);
            flattenedInheritedFields.add(flattenedField);
            return;
        }

        if (fieldsBySymbol.containsKey(symbol))
            return;

        IRField existing = findField(
                fieldsBySymbol,
                (ownerInternalName != null) ? ownerInternalName : defaultParentInternalName,
                symbol.name());
        if (existing != null) {
            fieldsBySymbol.put(symbol, existing);
            return;
        }

        fieldsBySymbol.put(
                symbol,
                new IRField(
                        field.line(),
                        (ownerInternalName != null) ? ownerInternalName : defaultParentInternalName,
                        symbol.name(),
                        type,
                        null));
    }

    private Set<CompilationUnit> secondaryParentUnits(
            SemanticScope objectScope,
            String objectInternalName,
            Set<String> primaryParentLineage) {
        Set<CompilationUnit> units = new HashSet<>();
        for (List<SemanticScope.ScopedSymbol> scopedSymbols : objectScope.symbols().values()) {
            for (SemanticScope.ScopedSymbol scopedSymbol : scopedSymbols) {
                CompilationUnit originUnit = scopedSymbol.originUnit();
                if (originUnit == null || originUnit.semanticModel() == null)
                    continue;

                String originName = originUnit.semanticModel().astObject().name();
                if (!Objects.equals(originName, objectInternalName) && !primaryParentLineage.contains(originName))
                    units.add(originUnit);
            }
        }
        return units;
    }

    private Set<CompilationUnit> visibleSecondaryParentUnits(
            SemanticScope objectScope,
            String objectInternalName,
            Set<String> primaryParentLineage) {
        Set<CompilationUnit> units = new HashSet<>();
        for (CompilationUnit unit : secondaryParentUnits(objectScope, objectInternalName, primaryParentLineage))
            collectVisibleParentUnits(unit, units);
        return units;
    }

    private Set<String> primaryParentLineage(CompilationUnit unit, String parentInternalName) {
        Set<String> lineage = new HashSet<>();
        collectLineageNames(primaryParentUnit(unit), lineage);
        if (parentInternalName != null)
            lineage.add(parentInternalName);
        return lineage;
    }

    private CompilationUnit primaryParentUnit(CompilationUnit unit) {
        if (unit == null)
            return null;

        if (unit.parentUnit() != null)
            return unit.parentUnit();

        return unit.directParentUnits().isEmpty() ? null : unit.directParentUnits().get(0);
    }

    private void collectLineageNames(CompilationUnit unit, Set<String> lineage) {
        if (unit == null || unit.semanticModel() == null)
            return;

        String name = unit.semanticModel().astObject().name();
        if (!lineage.add(name))
            return;

        collectLineageNames(primaryParentUnit(unit), lineage);
    }

    private void collectVisibleParentUnits(CompilationUnit unit, Set<CompilationUnit> units) {
        if (unit == null || unit.semanticModel() == null || !units.add(unit))
            return;

        for (CompilationUnit parentUnit : unit.directParentUnits())
            collectVisibleParentUnits(parentUnit, units);
    }

    private void importVisibleInheritedField(
            SemanticScope.ScopedSymbol scopedSymbol,
            Set<Symbol> declaredFieldSymbols,
            Map<Symbol, IRField> fieldsBySymbol,
            List<IRField> flattenedInheritedFields,
            String objectInternalName,
            String parentInternalName,
            Set<String> primaryParentLineage,
            List<CompilationProblem> problems) {
        Symbol symbol = scopedSymbol.symbol();
        if (declaredFieldSymbols.contains(symbol))
            return;

        RuntimeType type = runtimeType(symbol.lpcType());
        String ownerInternalName = scopedSymbol.field().ownerName();
        if (ownerInternalName != null && !primaryParentLineage.contains(ownerInternalName)) {
            IRField mapped = fieldsBySymbol.get(symbol);
            if (mapped != null && Objects.equals(mapped.ownerInternalName(), objectInternalName))
                return;

            IRField existing = findField(fieldsBySymbol, objectInternalName, symbol.name());
            if (existing != null) {
                fieldsBySymbol.put(symbol, existing);
                return;
            }
            IRField flattenedField = new IRField(
                    scopedSymbol.field().line(),
                    objectInternalName,
                    symbol.name(),
                    type,
                    lowerFieldInitializer(
                            scopedSymbol.field().initializer(),
                            type,
                            fieldsBySymbol,
                            problems,
                            objectInternalName));
            fieldsBySymbol.put(symbol, flattenedField);
            flattenedInheritedFields.add(flattenedField);
            return;
        }

        if (fieldsBySymbol.containsKey(symbol))
            return;

        IRField existing = findField(
                fieldsBySymbol,
                (ownerInternalName != null) ? ownerInternalName : defaultParentInternalName,
                symbol.name());
        if (existing != null) {
            fieldsBySymbol.put(symbol, existing);
            return;
        }

        fieldsBySymbol.put(
                symbol,
                new IRField(
                        scopedSymbol.field().line(),
                        (ownerInternalName != null) ? ownerInternalName : defaultParentInternalName,
                        symbol.name(),
                        type,
                        null));
    }

    private IRField findField(Map<Symbol, IRField> fieldsBySymbol, String ownerInternalName, String name) {
        for (IRField field : fieldsBySymbol.values()) {
            if (Objects.equals(field.ownerInternalName(), ownerInternalName) && Objects.equals(field.name(), name))
                return field;
        }
        return null;
    }

    private List<IRMethod> lowerMethods(
            ASTObject astObject,
            SemanticScope objectScope,
            Map<Symbol, IRField> fieldsBySymbol,
            Set<String> flattenedInheritedMethodOwners,
            List<CompilationProblem> problems,
            String objectInternalName,
            String parentInternalName,
            Set<String> primaryParentLineage) {
        List<IRMethod> methods = new ArrayList<>();
        Set<MethodKey> declaredMethodKeys = declaredMethodKeys(astObject);
        importSecondaryInheritedMethods(
                objectScope,
                fieldsBySymbol,
                flattenedInheritedMethodOwners,
                declaredMethodKeys,
                methods,
                problems,
                objectInternalName,
                parentInternalName,
                primaryParentLineage);

        for (ASTMethod method : astObject.methods()) {
            if (!method.isDefined())
                continue;
            methods.add(
                    lowerMethod(
                            method,
                            fieldsBySymbol,
                            flattenedInheritedMethodOwners,
                            primaryParentLineage,
                            problems,
                            objectInternalName));
        }

        return methods;
    }

    private Set<MethodKey> declaredMethodKeys(ASTObject astObject) {
        Set<MethodKey> keys = new HashSet<>();
        if (astObject == null)
            return keys;

        for (ASTMethod method : astObject.methods())
            keys.add(new MethodKey(method.symbol().name(), parameterCount(method)));
        return keys;
    }

    /**
     * Copies concrete methods from secondary inherit branches onto the child IR. The primary parent
     * lineage remains a JVM superclass chain, but secondary branches must be represented as
     * child-owned bytecode because Java has no second superclass slot.
     */
    private void importSecondaryInheritedMethods(
            SemanticScope objectScope,
            Map<Symbol, IRField> fieldsBySymbol,
            Set<String> flattenedInheritedMethodOwners,
            Set<MethodKey> declaredMethodKeys,
            List<IRMethod> methods,
            List<CompilationProblem> problems,
            String objectInternalName,
            String parentInternalName,
            Set<String> primaryParentLineage) {
        if (objectScope == null)
            return;

        List<ASTMethod> methodsToFlatten = new ArrayList<>();
        Set<MethodKey> imported = new HashSet<>();
        collectSecondaryInheritedMethods(
                objectScope.symbols().values(),
                fieldsBySymbol,
                flattenedInheritedMethodOwners,
                declaredMethodKeys,
                imported,
                methodsToFlatten,
                objectInternalName,
                parentInternalName,
                primaryParentLineage);

        for (CompilationUnit parentUnit :
                visibleSecondaryParentUnits(objectScope, objectInternalName, primaryParentLineage)) {
            collectSecondaryInheritedMethods(
                    parentUnit.semanticModel().objectScope().symbols().values(),
                    fieldsBySymbol,
                    flattenedInheritedMethodOwners,
                    declaredMethodKeys,
                    imported,
                    methodsToFlatten,
                    objectInternalName,
                    parentInternalName,
                    primaryParentLineage);
        }

        for (ASTMethod method : methodsToFlatten) {
            methods.add(
                    lowerMethod(
                            method,
                            fieldsBySymbol,
                            flattenedInheritedMethodOwners,
                            primaryParentLineage,
                            problems,
                            objectInternalName));
        }
    }

    private void collectSecondaryInheritedMethods(
            Iterable<List<SemanticScope.ScopedSymbol>> symbolLists,
            Map<Symbol, IRField> fieldsBySymbol,
            Set<String> flattenedInheritedMethodOwners,
            Set<MethodKey> declaredMethodKeys,
            Set<MethodKey> imported,
            List<ASTMethod> methodsToFlatten,
            String objectInternalName,
            String parentInternalName,
            Set<String> primaryParentLineage) {
        for (List<SemanticScope.ScopedSymbol> scopedSymbols : symbolLists) {
            for (SemanticScope.ScopedSymbol scopedSymbol : scopedSymbols) {
                ASTMethod method = scopedSymbol.method();
                if (method == null || !method.isDefined())
                    continue;

                String ownerInternalName = method.ownerName();
                if (ownerInternalName == null || Objects.equals(ownerInternalName, objectInternalName))
                    continue;
                if (primaryParentLineage.contains(ownerInternalName))
                    continue;

                MethodKey key = new MethodKey(method.symbol().name(), parameterCount(method));
                if (declaredMethodKeys.contains(key) || !imported.add(key))
                    continue;

                flattenedInheritedMethodOwners.add(ownerInternalName);
                methodsToFlatten.add(method);
            }
        }
    }

    private IRMethod lowerMethod(
            ASTMethod method,
            Map<Symbol, IRField> fieldsBySymbol,
            Set<String> flattenedInheritedMethodOwners,
            Set<String> primaryParentLineage,
            List<CompilationProblem> problems,
            String objectInternalName) {
        MethodContext context =
                new MethodContext(
                        runtimeType(method.symbol().lpcType()),
                        fieldsBySymbol,
                        objectInternalName,
                        flattenedInheritedMethodOwners,
                        primaryParentLineage);

        lowerParameters(method, context);
        lowerLocals(method, context);

        BlockBuilder entryBlock = context.newBlock("entry");
        BlockBuilder tail = lowerStatement(method.body(), entryBlock, context, problems);

        if (tail != null && !tail.isTerminated())
            tail.terminate(new IRReturn(method.line(), defaultReturnExpression(method.line(), context.returnType)));

        List<IRBlock> blocks = context.buildBlocks(new IRReturn(method.line(), defaultReturnExpression(method.line(), context.returnType)));
        boolean overridesParent = method.overrides() != null;
        String overriddenOwnerInternalName = (method.overrides() != null) ? method.overrides().ownerName() : null;

        return new IRMethod(
                method.line(),
                method.symbol().name(),
                context.returnType,
                context.parameters,
                context.locals,
                blocks,
                entryBlock.label(),
                overridesParent,
                overriddenOwnerInternalName);
    }

    private void lowerParameters(ASTMethod method, MethodContext context) {
        if (method.parameters() == null)
            return;

        int slot = 1; // slot 0 reserved for "this"
        for (ASTParameter parameter : method.parameters()) {
            RuntimeType type = runtimeType(parameter.symbol().lpcType());
            IRLocal local = new IRLocal(parameter.line(), parameter.symbol().name(), type, slot++, true);
            context.registerLocal(parameter.symbol(), local);
            context.parameters.add(new IRParameter(parameter.line(), parameter.symbol().name(), type, local));
        }
    }

    private boolean isParameterSymbol(Symbol symbol, ASTParameters parameters) {
        if (symbol == null || parameters == null)
            return false;

        for (ASTParameter parameter : parameters) {
            if (parameter.symbol() == symbol)
                return true;
        }

        return false;
    }

    private void lowerLocals(ASTMethod method, MethodContext context) {
        int nextSlot = context.parameters.size() + 1; // include "this"

        for (ASTLocal local : method.locals()) {
            if (isParameterSymbol(local.symbol(), method.parameters()))
                continue;

            RuntimeType type = runtimeType(local.symbol().lpcType());
            int slot = (local.slot() >= 0) ? local.slot() : nextSlot++;
            IRLocal irLocal = new IRLocal(local.line(), local.symbol().name(), type, slot, false);
            context.locals.add(irLocal);
            context.registerLocal(local.symbol(), irLocal);
        }
    }

    private BlockBuilder lowerStatement(
            ASTStatement statement, BlockBuilder current, MethodContext context, List<CompilationProblem> problems) {
        if (current == null || current.isTerminated())
            return current;

        if (statement == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "Encountered null statement while lowering method body.",
                            (Integer) null));
            return current;
        }

        if (statement instanceof ASTStmtBlock block) {
            for (ASTStatement nested : block) {
                if (current == null)
                    break;
                current = lowerStatement(nested, current, context, problems);
            }

            return current;
        }

        if (statement instanceof ASTStmtExpression stmtExpression) {
            IRExpression expression = lowerExpression(stmtExpression.expression(), context, problems);
            current.addStatement(new IRExpressionStatement(statement.line(), expression));
            return current;
        }

        if (statement instanceof ASTStmtEmpty)
            return current;

        if (statement instanceof ASTStmtIfThenElse ifStmt) {
            return lowerIfStatement(ifStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtFor forStmt) {
            return lowerForStatement(forStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtForeach foreachStmt) {
            return lowerForeachStatement(foreachStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtWhile whileStmt) {
            return lowerWhileStatement(whileStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtDoWhile doWhileStmt) {
            return lowerDoWhileStatement(doWhileStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtSwitch switchStmt) {
            return lowerSwitchStatement(switchStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtBreak) {
            String breakTarget = context.currentBreakTarget();
            if (breakTarget == null) {
                problems.add(
                            new CompilationProblem(
                                    CompilationStage.LOWER,
                                    "Encountered break statement outside of a loop or switch.",
                                    statement.line()));
                return current;
            }

            current.terminate(new IRJump(statement.line(), breakTarget));
            return null;
        }

        if (statement instanceof ASTStmtContinue) {
            String continueTarget = context.currentContinueTarget();
            if (continueTarget == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.LOWER,
                                "Encountered continue statement outside of a loop.",
                                statement.line()));
                return current;
            }

            current.terminate(new IRJump(statement.line(), continueTarget));
            return null;
        }

        if (statement instanceof ASTStmtReturn stmtReturn) {
            IRExpression returnValue =
                    (stmtReturn.returnValue() != null)
                            ? coerceIfNeeded(
                                    lowerExpression(stmtReturn.returnValue(), context, problems), context.returnType)
                            : defaultReturnExpression(statement.line(), context.returnType);

            current.terminate(new IRReturn(statement.line(), returnValue));
            return null;
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.LOWER,
                        "Unsupported statement kind: " + statement.getClass().getSimpleName(),
                        statement.line()));
        return current;
    }

    private BlockBuilder lowerForStatement(
            ASTStmtFor forStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        if (forStmt.initializer() != null) {
            IRExpression initExpression = lowerExpression(forStmt.initializer(), context, problems);
            current.addStatement(new IRExpressionStatement(forStmt.line(), initExpression));
        }

        BlockBuilder conditionBlock = context.newBlock("for_cond");
        BlockBuilder bodyBlock = context.newBlock("for_body");
        BlockBuilder mergeBlock = context.newBlock("for_end");
        BlockBuilder updateBlock = (forStmt.update() != null) ? context.newBlock("for_update") : null;

        current.terminate(new IRJump(forStmt.line(), conditionBlock.label()));

        if (forStmt.condition() != null) {
            IRExpression condition = coerceIfNeeded(
                    lowerExpression(forStmt.condition(), context, problems), RuntimeTypes.STATUS);
            conditionBlock.terminate(
                    new IRConditionalJump(forStmt.line(), condition, bodyBlock.label(), mergeBlock.label()));
        } else {
            conditionBlock.terminate(new IRJump(forStmt.line(), bodyBlock.label()));
        }

        String continueTarget = (updateBlock != null) ? updateBlock.label() : conditionBlock.label();
        context.pushLoop(mergeBlock.label(), continueTarget);
        BlockBuilder bodyTail = lowerStatement(forStmt.body(), bodyBlock, context, problems);
        context.popLoop();
        if (bodyTail != null && !bodyTail.isTerminated()) {
            String nextLabel = (updateBlock != null) ? updateBlock.label() : conditionBlock.label();
            bodyTail.terminate(new IRJump(forStmt.line(), nextLabel));
        }

        if (updateBlock != null) {
            IRExpression updateExpression = lowerExpression(forStmt.update(), context, problems);
            updateBlock.addStatement(new IRExpressionStatement(forStmt.line(), updateExpression));
            updateBlock.terminate(new IRJump(forStmt.line(), conditionBlock.label()));
        }

        return mergeBlock;
    }

    private BlockBuilder lowerForeachStatement(
            ASTStmtForeach foreachStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        IRLocal sourceLocal = context.addSyntheticLocal(foreachStmt.line(), "$foreach_source", RuntimeTypes.MIXED);
        IRLocal itemsLocal = context.addSyntheticLocal(
                foreachStmt.line(), "$foreach_items", RuntimeTypes.arrayOf(RuntimeTypes.MIXED));
        IRLocal indexLocal = context.addSyntheticLocal(foreachStmt.line(), "$foreach_index", RuntimeTypes.INT);

        IRExpression source = lowerExpression(foreachStmt.iterable(), context, problems);
        current.addStatement(new IRExpressionStatement(
                foreachStmt.line(),
                new IRLocalStore(foreachStmt.line(), sourceLocal, coerceIfNeeded(source, RuntimeTypes.MIXED))));

        boolean keyValueLoop = foreachStmt.hasValueLocal();
        current.addStatement(new IRExpressionStatement(
                foreachStmt.line(),
                new IRLocalStore(
                        foreachStmt.line(),
                        itemsLocal,
                        new IRForeachItems(
                                foreachStmt.line(),
                                new IRLocalLoad(foreachStmt.line(), sourceLocal),
                                keyValueLoop,
                                RuntimeTypes.arrayOf(RuntimeTypes.MIXED)))));

        current.addStatement(new IRExpressionStatement(
                foreachStmt.line(),
                new IRLocalStore(
                        foreachStmt.line(),
                        indexLocal,
                        new IRConstant(foreachStmt.line(), 0, RuntimeTypes.INT))));

        BlockBuilder conditionBlock = context.newBlock("foreach_cond");
        BlockBuilder bodyBlock = context.newBlock("foreach_body");
        BlockBuilder updateBlock = context.newBlock("foreach_update");
        BlockBuilder mergeBlock = context.newBlock("foreach_end");

        current.terminate(new IRJump(foreachStmt.line(), conditionBlock.label()));

        IRExpression condition = new IRBinaryOperation(
                foreachStmt.line(),
                BinaryOpType.BOP_LT,
                new IRLocalLoad(foreachStmt.line(), indexLocal),
                new IRForeachSize(
                        foreachStmt.line(),
                        new IRLocalLoad(foreachStmt.line(), itemsLocal),
                        RuntimeTypes.INT),
                RuntimeTypes.STATUS);
        conditionBlock.terminate(
                new IRConditionalJump(foreachStmt.line(), condition, bodyBlock.label(), mergeBlock.label()));

        IRLocal keyLocal = context.requireLocal(foreachStmt.keyLocal(), problems);
        IRExpression item = new IRArrayGet(
                foreachStmt.line(),
                new IRLocalLoad(foreachStmt.line(), itemsLocal),
                new IRLocalLoad(foreachStmt.line(), indexLocal),
                RuntimeTypes.MIXED);
        bodyBlock.addStatement(new IRExpressionStatement(
                foreachStmt.line(),
                new IRLocalStore(foreachStmt.line(), keyLocal, coerceIfNeeded(item, keyLocal.type()))));

        if (keyValueLoop) {
            IRLocal valueLocal = context.requireLocal(foreachStmt.valueLocal(), problems);
            IRExpression value = new IRForeachValue(
                    foreachStmt.line(),
                    new IRLocalLoad(foreachStmt.line(), sourceLocal),
                    new IRLocalLoad(foreachStmt.line(), keyLocal),
                    RuntimeTypes.MIXED);
            bodyBlock.addStatement(new IRExpressionStatement(
                    foreachStmt.line(),
                    new IRLocalStore(foreachStmt.line(), valueLocal, coerceIfNeeded(value, valueLocal.type()))));
        }

        context.pushLoop(mergeBlock.label(), updateBlock.label());
        BlockBuilder bodyTail = lowerStatement(foreachStmt.body(), bodyBlock, context, problems);
        context.popLoop();
        if (bodyTail != null && !bodyTail.isTerminated())
            bodyTail.terminate(new IRJump(foreachStmt.line(), updateBlock.label()));

        IRExpression increment = new IRBinaryOperation(
                foreachStmt.line(),
                BinaryOpType.BOP_ADD,
                new IRLocalLoad(foreachStmt.line(), indexLocal),
                new IRConstant(foreachStmt.line(), 1, RuntimeTypes.INT),
                RuntimeTypes.INT);
        updateBlock.addStatement(new IRExpressionStatement(
                foreachStmt.line(), new IRLocalStore(foreachStmt.line(), indexLocal, increment)));
        updateBlock.terminate(new IRJump(foreachStmt.line(), conditionBlock.label()));

        return mergeBlock;
    }

    private BlockBuilder lowerWhileStatement(
            ASTStmtWhile whileStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        BlockBuilder conditionBlock = context.newBlock("while_cond");
        BlockBuilder bodyBlock = context.newBlock("while_body");
        BlockBuilder mergeBlock = context.newBlock("while_end");

        current.terminate(new IRJump(whileStmt.line(), conditionBlock.label()));

        IRExpression condition = coerceIfNeeded(
                lowerExpression(whileStmt.condition(), context, problems), RuntimeTypes.STATUS);
        conditionBlock.terminate(
                new IRConditionalJump(whileStmt.line(), condition, bodyBlock.label(), mergeBlock.label()));

        context.pushLoop(mergeBlock.label(), conditionBlock.label());
        BlockBuilder bodyTail = lowerStatement(whileStmt.body(), bodyBlock, context, problems);
        context.popLoop();
        if (bodyTail != null && !bodyTail.isTerminated())
            bodyTail.terminate(new IRJump(whileStmt.line(), conditionBlock.label()));

        return mergeBlock;
    }

    private BlockBuilder lowerDoWhileStatement(
            ASTStmtDoWhile doWhileStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        BlockBuilder bodyBlock = context.newBlock("do_body");
        BlockBuilder conditionBlock = context.newBlock("do_cond");
        BlockBuilder mergeBlock = context.newBlock("do_end");

        current.terminate(new IRJump(doWhileStmt.line(), bodyBlock.label()));

        context.pushLoop(mergeBlock.label(), conditionBlock.label());
        BlockBuilder bodyTail = lowerStatement(doWhileStmt.body(), bodyBlock, context, problems);
        context.popLoop();
        if (bodyTail != null && !bodyTail.isTerminated())
            bodyTail.terminate(new IRJump(doWhileStmt.line(), conditionBlock.label()));

        IRExpression condition = coerceIfNeeded(
                lowerExpression(doWhileStmt.condition(), context, problems), RuntimeTypes.STATUS);
        conditionBlock.terminate(
                new IRConditionalJump(doWhileStmt.line(), condition, bodyBlock.label(), mergeBlock.label()));

        return mergeBlock;
    }

    private BlockBuilder lowerIfStatement(
            ASTStmtIfThenElse ifStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        BlockBuilder thenBlock = context.newBlock("then");
        BlockBuilder mergeBlock = context.newBlock("endif");
        BlockBuilder elseBlock = (ifStmt.elseBranch() != null) ? context.newBlock("else") : mergeBlock;

        IRExpression condition = coerceIfNeeded(
                lowerExpression(ifStmt.condition(), context, problems), RuntimeTypes.STATUS);

        current.terminate(new IRConditionalJump(ifStmt.line(), condition, thenBlock.label(), elseBlock.label()));

        BlockBuilder thenTail = lowerStatement(ifStmt.thenBranch(), thenBlock, context, problems);
        if (thenTail != null && !thenTail.isTerminated())
            thenTail.terminate(new IRJump(ifStmt.line(), mergeBlock.label()));

        if (ifStmt.elseBranch() != null) {
            BlockBuilder elseTail = lowerStatement(ifStmt.elseBranch(), elseBlock, context, problems);
            if (elseTail != null && !elseTail.isTerminated())
                elseTail.terminate(new IRJump(ifStmt.line(), mergeBlock.label()));
        }

        return mergeBlock;
    }

    private BlockBuilder lowerSwitchStatement(
            ASTStmtSwitch switchStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        IRLocal valueLocal = context.addSyntheticLocal(switchStmt.line(), "$switch_value", RuntimeTypes.MIXED);
        IRExpression value = lowerExpression(switchStmt.expression(), context, problems);
        current.addStatement(new IRExpressionStatement(
                switchStmt.line(),
                new IRLocalStore(switchStmt.line(), valueLocal, coerceIfNeeded(value, RuntimeTypes.MIXED))));

        BlockBuilder mergeBlock = context.newBlock("switch_end");
        if (switchStmt.cases().isEmpty()) {
            current.terminate(new IRJump(switchStmt.line(), mergeBlock.label()));
            return mergeBlock;
        }

        List<ASTStmtSwitch.SwitchCase> cases = switchStmt.cases();
        List<BlockBuilder> bodyBlocks = new ArrayList<>();
        for (int i = 0; i < cases.size(); i++)
            bodyBlocks.add(context.newBlock("switch_case"));

        List<Integer> testCaseIndexes = new ArrayList<>();
        int defaultIndex = -1;
        for (int i = 0; i < cases.size(); i++) {
            ASTStmtSwitch.SwitchCase switchCase = cases.get(i);
            if (switchCase.isDefault() && defaultIndex < 0)
                defaultIndex = i;
            else if (!switchCase.isDefault())
                testCaseIndexes.add(i);
        }

        List<BlockBuilder> testBlocks = new ArrayList<>();
        for (int ignored : testCaseIndexes)
            testBlocks.add(context.newBlock("switch_test"));

        String defaultOrEndLabel = defaultIndex >= 0 ? bodyBlocks.get(defaultIndex).label() : mergeBlock.label();
        current.terminate(new IRJump(
                switchStmt.line(), testBlocks.isEmpty() ? defaultOrEndLabel : testBlocks.get(0).label()));

        for (int i = 0; i < testCaseIndexes.size(); i++) {
            int caseIndex = testCaseIndexes.get(i);
            ASTStmtSwitch.SwitchCase switchCase = cases.get(caseIndex);
            BlockBuilder testBlock = testBlocks.get(i);
            IRExpression condition = lowerSwitchCaseCondition(switchCase, valueLocal, context, problems);
            String falseLabel = (i + 1 < testBlocks.size()) ? testBlocks.get(i + 1).label() : defaultOrEndLabel;
            testBlock.terminate(
                    new IRConditionalJump(switchCase.line(), condition, bodyBlocks.get(caseIndex).label(), falseLabel));
        }

        context.pushBreakTarget(mergeBlock.label());
        for (int i = 0; i < cases.size(); i++) {
            BlockBuilder bodyTail = bodyBlocks.get(i);
            for (ASTStatement nested : cases.get(i).statements()) {
                if (bodyTail == null || bodyTail.isTerminated())
                    break;
                bodyTail = lowerStatement(nested, bodyTail, context, problems);
            }

            if (bodyTail != null && !bodyTail.isTerminated()) {
                String nextLabel = (i + 1 < cases.size()) ? bodyBlocks.get(i + 1).label() : mergeBlock.label();
                bodyTail.terminate(new IRJump(cases.get(i).line(), nextLabel));
            }
        }
        context.popBreakTarget();

        return mergeBlock;
    }

    /**
     * Lowers an exact {@code case value:} or inclusive {@code case start..end:} label into the
     * boolean condition used by the explicit switch-test blocks.
     */
    private IRExpression lowerSwitchCaseCondition(
            ASTStmtSwitch.SwitchCase switchCase,
            IRLocal valueLocal,
            MethodContext context,
            List<CompilationProblem> problems) {
        IRExpression caseValue = coerceIfNeeded(
                lowerExpression(switchCase.expression(), context, problems), RuntimeTypes.MIXED);
        IRExpression switchValue = new IRLocalLoad(switchCase.line(), valueLocal);
        if (!switchCase.isRange()) {
            return new IRBinaryOperation(
                    switchCase.line(),
                    BinaryOpType.BOP_EQ,
                    switchValue,
                    caseValue,
                    RuntimeTypes.STATUS);
        }

        IRExpression rangeEndValue = coerceIfNeeded(
                lowerExpression(switchCase.rangeEndExpression(), context, problems), RuntimeTypes.MIXED);
        IRExpression lowerBound = new IRBinaryOperation(
                switchCase.line(),
                BinaryOpType.BOP_GE,
                switchValue,
                caseValue,
                RuntimeTypes.STATUS);
        IRExpression upperBound = new IRBinaryOperation(
                switchCase.line(),
                BinaryOpType.BOP_LE,
                new IRLocalLoad(switchCase.line(), valueLocal),
                rangeEndValue,
                RuntimeTypes.STATUS);
        return new IRBinaryOperation(
                switchCase.line(),
                BinaryOpType.BOP_AND,
                lowerBound,
                upperBound,
                RuntimeTypes.STATUS);
    }

    private IRExpression lowerExpression(
            ASTExpression expression, MethodContext context, List<CompilationProblem> problems) {
        if (expression == null)
            return new IRConstant(0, null, RuntimeTypes.INTERNAL_NULL);

        if (expression instanceof ASTExprLiteralInteger literal)
            return new IRConstant(literal.line(), literal.value(), RuntimeTypes.INT);

        if (expression instanceof ASTExprLiteralFloat literal)
            return new IRConstant(literal.line(), literal.value(), RuntimeTypes.FLOAT);

        if (expression instanceof ASTExprLiteralString literal)
            return new IRConstant(literal.line(), literal.value(), RuntimeTypes.STRING);

        if (expression instanceof ASTExprSymbolLiteral symbolLiteral)
            return new IRConstant(symbolLiteral.line(), symbolLiteral.name(), RuntimeTypes.MIXED);

        if (expression instanceof ASTExprFunctionReference functionReference)
            return new IRFunctionReferenceLiteral(
                    functionReference.line(), functionReference.name(), RuntimeTypes.CALLABLE);

        if (expression instanceof ASTExprTypedFunctionLiteral typedFunction)
            return new IRTypedFunctionLiteral(
                    typedFunction.line(), typedFunctionSignature(typedFunction), RuntimeTypes.CALLABLE);

        if (expression instanceof ASTExprInlineCallable inlineCallable)
            return lowerInlineCallableLiteral(inlineCallable, context, problems);

        if (expression instanceof ASTExprClosureArgument closureArgument) {
            IRLocal local = context.closureArgumentLocal(closureArgument.index());
            if (local == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.LOWER,
                                "Closure argument $" + closureArgument.index()
                                        + " is not available in this lowering context.",
                                closureArgument.line()));
                return new IRConstant(closureArgument.line(), null, RuntimeTypes.MIXED);
            }
            return new IRLocalLoad(closureArgument.line(), local);
        }

        if (expression instanceof ASTExprCollectionTransform transform)
            return lowerCollectionTransform(transform, context, problems);

        if (expression instanceof ASTExprSortArray sortArray)
            return lowerSortArray(sortArray, context, problems);

        if (expression instanceof ASTExprArrayLiteral arrayLiteral) {
            List<IRExpression> elements = new ArrayList<>();
            for (ASTExpression element : arrayLiteral.elements())
                elements.add(lowerExpression(element, context, problems));
            return new IRArrayLiteral(
                    arrayLiteral.line(), elements, RuntimeTypes.arrayOf(RuntimeTypes.MIXED));
        }

        if (expression instanceof ASTExprMappingLiteral mappingLiteral) {
            List<IRMappingEntry> entries = new ArrayList<>();
            for (ASTExprMappingEntry entry : mappingLiteral.entries()) {
                IRExpression key = lowerExpression(entry.key(), context, problems);
                IRExpression value = lowerExpression(entry.value(), context, problems);
                entries.add(new IRMappingEntry(key, value));
            }
            return new IRMappingLiteral(mappingLiteral.line(), entries, RuntimeTypes.MAPPING);
        }

        if (expression instanceof ASTExprLiteralTrue literal)
            return new IRConstant(literal.line(), Boolean.TRUE, RuntimeTypes.STATUS);

        if (expression instanceof ASTExprLiteralFalse literal)
            return new IRConstant(literal.line(), Boolean.FALSE, RuntimeTypes.STATUS);

        if (expression instanceof ASTExprError literalError) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "Compiler error recovery expression reached IR lowering.",
                            literalError.line()));
            return new IRConstant(literalError.line(), null, RuntimeTypes.ERROR);
        }

        if (expression instanceof ASTExprLocalAccess access)
            return new IRLocalLoad(access.line(), context.requireLocal(access.local(), problems));

        if (expression instanceof ASTExprLocalStore store) {
            IRLocal target = context.requireLocal(store.local(), problems);
            IRExpression value = coerceIfNeeded(lowerExpression(store.value(), context, problems), target.type());
            return new IRLocalStore(store.line(), target, value);
        }

        if (expression instanceof ASTExprLocalMutation mutation)
            return new IRLocalMutation(
                    mutation.line(),
                    context.requireLocal(mutation.local(), problems),
                    mutation.delta(),
                    mutation.isPrefix());

        if (expression instanceof ASTExprFieldAccess fieldAccess) {
            IRField field = context.requireField(fieldAccess.field(), problems);
            return new IRFieldLoad(fieldAccess.line(), field);
        }

        if (expression instanceof ASTExprFieldStore fieldStore) {
            IRField field = context.requireField(fieldStore.field(), problems);
            IRExpression value =
                    coerceIfNeeded(lowerExpression(fieldStore.value(), context, problems), field.type());
            return new IRFieldStore(fieldStore.line(), field, value);
        }

        if (expression instanceof ASTExprFieldMutation mutation)
            return new IRFieldMutation(
                    mutation.line(),
                    context.requireField(mutation.field(), problems),
                    mutation.delta(),
                    mutation.isPrefix());

        if (expression instanceof ASTExprFromEndIndex fromEnd) {
            IRExpression distance = coerceIfNeeded(lowerExpression(fromEnd.distance(), context, problems), RuntimeTypes.INT);
            return new IRFromEndIndex(fromEnd.line(), distance);
        }

        if (expression instanceof ASTExprArrayAccess arrayAccess) {
            IRExpression target = lowerExpression(arrayAccess.target(), context, problems);
            RuntimeType targetType = runtimeType(arrayAccess.target().lpcType());
            if (targetType != null && targetType.kind() == RuntimeValueKind.MAPPING) {
                IRExpression key = lowerExpression(arrayAccess.index(), context, problems);
                return new IRMappingGet(arrayAccess.line(), target, key, RuntimeTypes.MIXED);
            }

            IRExpression rawIndex = lowerExpression(arrayAccess.index(), context, problems);
            IRExpression index = indexExpression(rawIndex);
            if (targetType != null && targetType.kind() == RuntimeValueKind.STRING)
                return new IRStringGet(arrayAccess.line(), target, index, RuntimeTypes.INT);

            if (targetType != null && targetType.kind() == RuntimeValueKind.MIXED)
                return new IRArrayGet(
                        arrayAccess.line(),
                        target,
                        coerceIfNeeded(rawIndex, RuntimeTypes.MIXED),
                        RuntimeTypes.MIXED);

            return new IRArrayGet(arrayAccess.line(), target, index, RuntimeTypes.MIXED);
        }

        if (expression instanceof ASTExprSliceAccess sliceAccess) {
            IRExpression target = lowerExpression(sliceAccess.target(), context, problems);
            IRExpression start = indexExpression(lowerExpression(sliceAccess.start(), context, problems));
            IRExpression end = sliceAccess.end() == null
                    ? new IRConstant(sliceAccess.line(), null, RuntimeTypes.INTERNAL_NULL)
                    : indexExpression(lowerExpression(sliceAccess.end(), context, problems));
            RuntimeType targetType = runtimeType(sliceAccess.target().lpcType());
            RuntimeType resultType = RuntimeTypes.MIXED;
            if (targetType != null && targetType.kind() == RuntimeValueKind.STRING)
                resultType = RuntimeTypes.STRING;
            else if (targetType != null && targetType.kind() == RuntimeValueKind.ARRAY)
                resultType = RuntimeTypes.arrayOf(RuntimeTypes.MIXED);
            return new IRSlice(sliceAccess.line(), target, start, end, resultType);
        }

        if (expression instanceof ASTExprArrayStore arrayStore) {
            IRExpression target = lowerExpression(arrayStore.target(), context, problems);
            RuntimeType targetType = runtimeType(arrayStore.target().lpcType());
            if (targetType != null && targetType.kind() == RuntimeValueKind.MAPPING) {
                IRExpression key = lowerExpression(arrayStore.index(), context, problems);
                IRExpression value = lowerExpression(arrayStore.value(), context, problems);
                return new IRMappingSet(
                        arrayStore.line(), target, key, coerceIfNeeded(value, RuntimeTypes.MIXED), value.type());
            }

            IRExpression rawIndex = lowerExpression(arrayStore.index(), context, problems);
            IRExpression value = lowerExpression(arrayStore.value(), context, problems);
            if (targetType != null && targetType.kind() == RuntimeValueKind.MIXED)
                return new IRArraySet(
                        arrayStore.line(),
                        target,
                        coerceIfNeeded(rawIndex, RuntimeTypes.MIXED),
                        coerceIfNeeded(value, RuntimeTypes.MIXED),
                        value.type());
            IRExpression index = indexExpression(rawIndex);
            return new IRArraySet(
                    arrayStore.line(), target, index, coerceIfNeeded(value, RuntimeTypes.MIXED), value.type());
        }

        if (expression instanceof ASTExprSliceStore sliceStore) {
            IRExpression target = lowerExpression(sliceStore.target(), context, problems);
            IRExpression start = indexExpression(lowerExpression(sliceStore.start(), context, problems));
            IRExpression end = sliceStore.end() == null
                    ? new IRConstant(sliceStore.line(), null, RuntimeTypes.INTERNAL_NULL)
                    : indexExpression(lowerExpression(sliceStore.end(), context, problems));
            RuntimeType targetType = runtimeType(sliceStore.target().lpcType());
            RuntimeType valueType = sliceReplacementType(targetType);
            IRExpression value = coerceIfNeeded(lowerExpression(sliceStore.value(), context, problems), valueType);
            RuntimeType resultType = sliceSetResultType(targetType);
            IRExpression sliceSet = new IRSliceSet(sliceStore.line(), target, start, end, value, resultType);
            if (targetType != null && targetType.kind() == RuntimeValueKind.STRING)
                return storeStringSliceResult(sliceStore, context, problems, sliceSet);
            return sliceSet;
        }

        if (expression instanceof ASTExprArrayMutation mutation) {
            IRExpression target = lowerExpression(mutation.target(), context, problems);
            IRExpression index = indexExpression(lowerExpression(mutation.index(), context, problems));
            return new IRArrayMutation(
                    mutation.line(), target, index, mutation.delta(), mutation.isPrefix(), RuntimeTypes.MIXED);
        }

        if (expression instanceof ASTExprOpUnary unary) {
            RuntimeType type = (unary.operator() == UnaryOpType.UOP_NOT)
                    ? RuntimeTypes.STATUS
                    : runtimeType(unary.lpcType());
            IRExpression operand = lowerExpression(unary.right(), context, problems);
            return new IRUnaryOperation(unary.line(), unary.operator(), operand, type);
        }

        if (expression instanceof ASTExprOpBinary binary) {
            if (binary.operator() == io.github.protasm.jvmud.compiler.parser.type.BinaryOpType.BOP_ADD
                    && binary.lpcType() == LPCType.LPCARRAY) {
                RuntimeType arrayType = RuntimeTypes.arrayOf(RuntimeTypes.MIXED);
                IRExpression left = coerceIfNeeded(lowerExpression(binary.left(), context, problems), arrayType);
                IRExpression right = coerceIfNeeded(lowerExpression(binary.right(), context, problems), arrayType);
                return new IRArrayConcat(binary.line(), left, right, arrayType);
            }
            if (binary.operator() == io.github.protasm.jvmud.compiler.parser.type.BinaryOpType.BOP_SUB
                    && binary.lpcType() == LPCType.LPCARRAY) {
                RuntimeType arrayType = RuntimeTypes.arrayOf(RuntimeTypes.MIXED);
                IRExpression left = coerceIfNeeded(lowerExpression(binary.left(), context, problems), arrayType);
                IRExpression right = coerceIfNeeded(lowerExpression(binary.right(), context, problems), arrayType);
                return new IRArrayDifference(binary.line(), left, right, arrayType);
            }
            if (binary.operator() == io.github.protasm.jvmud.compiler.parser.type.BinaryOpType.BOP_SUB
                    && binary.lpcType() == LPCType.LPCSTRING) {
                IRExpression left = lowerExpression(binary.left(), context, problems);
                IRExpression right = lowerExpression(binary.right(), context, problems);
                return new IRStringDifference(binary.line(), left, right, RuntimeTypes.STRING);
            }
            if (binary.operator() == io.github.protasm.jvmud.compiler.parser.type.BinaryOpType.BOP_ADD
                    && binary.lpcType() == LPCType.LPCMAPPING) {
                IRExpression left = coerceIfNeeded(lowerExpression(binary.left(), context, problems), RuntimeTypes.MAPPING);
                IRExpression right = coerceIfNeeded(lowerExpression(binary.right(), context, problems), RuntimeTypes.MAPPING);
                return new IRMappingMerge(binary.line(), left, right, RuntimeTypes.MAPPING);
            }
            RuntimeType type = runtimeType(binary.lpcType());
            IRExpression left = lowerExpression(binary.left(), context, problems);
            IRExpression right = lowerExpression(binary.right(), context, problems);
            return new IRBinaryOperation(binary.line(), binary.operator(), left, right, type);
        }

        if (expression instanceof ASTExprSequence sequence) {
            List<IRExpression> expressions = new ArrayList<>();
            for (ASTExpression nested : sequence.expressions())
                expressions.add(lowerExpression(nested, context, problems));
            return new IRSequence(sequence.line(), expressions, runtimeType(sequence.lpcType()));
        }

        if (expression instanceof ASTExprProtectedEval protectedEval) {
            IRExpression body = lowerExpression(protectedEval.body(), context, problems);
            return new IRProtectedEval(
                    protectedEval.line(), body, protectedEval.suppressLogging(), RuntimeTypes.MIXED);
        }

        if (expression instanceof ASTExprTernary ternary) {
            RuntimeType targetType = runtimeType(ternary.lpcType());
            IRExpression condition = coerceIfNeeded(
                    lowerExpression(ternary.condition(), context, problems), RuntimeTypes.STATUS);
            IRExpression thenBranch =
                    coerceIfNeeded(lowerExpression(ternary.thenBranch(), context, problems), targetType);
            IRExpression elseBranch =
                    coerceIfNeeded(lowerExpression(ternary.elseBranch(), context, problems), targetType);
            return new IRConditionalExpression(ternary.line(), condition, thenBranch, elseBranch, targetType);
        }

        if (expression instanceof ASTExprCallEfun callEfun) {
            List<IRExpression> args = lowerArguments(callEfun.arguments(), context, problems);
            RuntimeType returnType = runtimeType(callEfun.lpcType());
            return new IREfunCall(callEfun.line(), callEfun.signature().name(), args, returnType);
        }

        if (expression instanceof ASTExprCallMethod callMethod) {
            List<IRExpression> args = lowerArguments(callMethod.arguments(), context, problems);
            RuntimeType returnType = runtimeType(callMethod.lpcType());
            String ownerInternalName = callMethod.method().ownerName();
            if (ownerInternalName == null && !callMethod.isParentDispatch())
                ownerInternalName = defaultParentInternalName;
            if (!callMethod.isParentDispatch() && context.shouldCallThroughCurrent(ownerInternalName))
                ownerInternalName = context.currentInternalName;
            List<RuntimeType> parameterTypes = parameterTypes(callMethod.method());
            return new IRInstanceCall(
                    callMethod.line(),
                    ownerInternalName,
                    callMethod.method().symbol().name(),
                    callMethod.isParentDispatch(),
                    args,
                    parameterTypes,
                    returnType);
        }

        if (expression instanceof ASTExprInvokeLocal invokeLocal) {
            IRLocal target = context.requireLocal(invokeLocal.local(), problems);
            List<IRExpression> args = lowerArguments(invokeLocal.args(), context, problems);
            RuntimeType returnType = runtimeType(invokeLocal.lpcType());
            return new IRDynamicInvoke(invokeLocal.line(), target, invokeLocal.methodName(), args, returnType);
        }

        if (expression instanceof ASTExprInvokeField invokeField) {
            IRField target = context.requireField(invokeField.field(), problems);
            List<IRExpression> args = lowerArguments(invokeField.args(), context, problems);
            RuntimeType returnType = runtimeType(invokeField.lpcType());
            return new IRDynamicInvokeField(invokeField.line(), target, invokeField.methodName(), args, returnType);
        }

        if (expression instanceof ASTExprDynamicInvoke dynamicInvoke) {
            IRExpression target = lowerExpression(dynamicInvoke.target(), context, problems);
            List<IRExpression> args = lowerArguments(dynamicInvoke.arguments(), context, problems);
            return new IRDynamicInvokeExpression(
                    dynamicInvoke.line(),
                    target,
                    dynamicInvoke.methodName(),
                    args,
                    RuntimeTypes.MIXED);
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.LOWER,
                        "Unsupported expression kind: " + expression.getClass().getSimpleName(),
                        expression.line()));
        return new IRConstant(expression.line(), null, RuntimeTypes.MIXED);
    }

    private IRExpression lowerCollectionTransform(
            ASTExprCollectionTransform transform, MethodContext context, List<CompilationProblem> problems) {
        IRExpression source = lowerExpression(transform.source(), context, problems);
        List<IRExpression> extras = new ArrayList<>();
        for (ASTExpression extra : transform.extraArguments())
            extras.add(lowerExpression(extra, context, problems));

        IRLocal sourceLocal = context.addSyntheticLocal(transform.line(), "closure_source", RuntimeTypes.MIXED);
        IRLocal itemsLocal = context.addSyntheticLocal(transform.line(), "closure_items", RuntimeTypes.MIXED);
        IRLocal resultLocal = context.addSyntheticLocal(transform.line(), "closure_result", RuntimeTypes.MIXED);
        IRLocal indexLocal = context.addSyntheticLocal(transform.line(), "closure_index", RuntimeTypes.INT);
        IRExpression callback = lowerExpression(transform.callback(), context, problems);

        IRCollectionTransform.Operation operation = transform.operation() == ASTExprCollectionTransform.Operation.FILTER
                ? IRCollectionTransform.Operation.FILTER
                : IRCollectionTransform.Operation.MAP;
        RuntimeType resultType = runtimeType(transform.lpcType());
        if (resultType == null)
            resultType = RuntimeTypes.MIXED;
        return new IRCollectionTransform(
                transform.line(),
                operation,
                coerceIfNeeded(source, RuntimeTypes.MIXED),
                extras,
                callback,
                sourceLocal,
                itemsLocal,
                resultLocal,
                indexLocal,
                resultType);
    }

    private IRExpression lowerInlineCallableLiteral(
            ASTExprInlineCallable inlineCallable, MethodContext context, List<CompilationProblem> problems) {
        int argumentCount = maxClosureArgumentIndex(inlineCallable.body());
        List<IRLocal> argumentLocals = new ArrayList<>();
        for (int i = 1; i <= argumentCount; i++)
            argumentLocals.add(new IRLocal(inlineCallable.line(), "callable_arg" + i, RuntimeTypes.MIXED, 100 + i, false));

        context.pushClosureArguments(argumentLocals);
        IRExpression body = lowerExpression(inlineCallable.body(), context, problems);
        context.popClosureArguments();
        return new IRInlineCallableLiteral(
                inlineCallable.line(),
                body,
                argumentCount,
                argumentLocals,
                captureLocals(body, argumentLocals),
                RuntimeTypes.CALLABLE);
    }

    private List<IRLocal> captureLocals(IRExpression expression, List<IRLocal> argumentLocals) {
        Set<IRLocal> arguments = new HashSet<>(argumentLocals);
        Map<Integer, IRLocal> capturesBySlot = new LinkedHashMap<>();
        collectCaptureLocals(expression, arguments, capturesBySlot);
        return List.copyOf(capturesBySlot.values());
    }

    private void collectCaptureLocals(
            IRExpression expression, Set<IRLocal> argumentLocals, Map<Integer, IRLocal> capturesBySlot) {
        if (expression == null)
            return;

        if (expression instanceof IRLocalLoad localLoad) {
            IRLocal local = localLoad.local();
            if (!argumentLocals.contains(local))
                capturesBySlot.putIfAbsent(local.slot(), local);
            return;
        }
        if (expression instanceof IRLocalStore localStore) {
            IRLocal local = localStore.local();
            if (!argumentLocals.contains(local))
                capturesBySlot.putIfAbsent(local.slot(), local);
            collectCaptureLocals(localStore.value(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IRLocalMutation mutation) {
            IRLocal local = mutation.local();
            if (!argumentLocals.contains(local))
                capturesBySlot.putIfAbsent(local.slot(), local);
            return;
        }
        if (expression instanceof IRFieldLoad || expression instanceof IRConstant || expression instanceof IRFromEndIndex
                || expression instanceof IRFunctionReferenceLiteral || expression instanceof IRTypedFunctionLiteral
                || expression instanceof IRInlineCallableLiteral)
            return;
        if (expression instanceof IRCoerce coerce) {
            collectCaptureLocals(coerce.value(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IRUnaryOperation unary) {
            collectCaptureLocals(unary.operand(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IRBinaryOperation binary) {
            collectCaptureLocals(binary.left(), argumentLocals, capturesBySlot);
            collectCaptureLocals(binary.right(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IRSequence sequence) {
            sequence.expressions().forEach(nested -> collectCaptureLocals(nested, argumentLocals, capturesBySlot));
            return;
        }
        if (expression instanceof IRConditionalExpression conditional) {
            collectCaptureLocals(conditional.condition(), argumentLocals, capturesBySlot);
            collectCaptureLocals(conditional.thenBranch(), argumentLocals, capturesBySlot);
            collectCaptureLocals(conditional.elseBranch(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IREfunCall call) {
            call.arguments().forEach(argument -> collectCaptureLocals(argument, argumentLocals, capturesBySlot));
            return;
        }
        if (expression instanceof IRInstanceCall call) {
            call.arguments().forEach(argument -> collectCaptureLocals(argument, argumentLocals, capturesBySlot));
            return;
        }
        if (expression instanceof IRDynamicInvoke invoke) {
            invoke.arguments().forEach(argument -> collectCaptureLocals(argument, argumentLocals, capturesBySlot));
            return;
        }
        if (expression instanceof IRDynamicInvokeExpression invoke) {
            collectCaptureLocals(invoke.target(), argumentLocals, capturesBySlot);
            invoke.arguments().forEach(argument -> collectCaptureLocals(argument, argumentLocals, capturesBySlot));
            return;
        }
        if (expression instanceof IRDynamicInvokeField invoke) {
            invoke.arguments().forEach(argument -> collectCaptureLocals(argument, argumentLocals, capturesBySlot));
            return;
        }
        if (expression instanceof IRArrayLiteral literal) {
            literal.elements().forEach(element -> collectCaptureLocals(element, argumentLocals, capturesBySlot));
            return;
        }
        if (expression instanceof IRMappingLiteral literal) {
            for (IRMappingEntry entry : literal.entries()) {
                collectCaptureLocals(entry.key(), argumentLocals, capturesBySlot);
                collectCaptureLocals(entry.value(), argumentLocals, capturesBySlot);
            }
            return;
        }
        if (expression instanceof IRArrayGet get) {
            collectCaptureLocals(get.array(), argumentLocals, capturesBySlot);
            collectCaptureLocals(get.index(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IRMappingGet get) {
            collectCaptureLocals(get.mapping(), argumentLocals, capturesBySlot);
            collectCaptureLocals(get.key(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IRStringGet get) {
            collectCaptureLocals(get.string(), argumentLocals, capturesBySlot);
            collectCaptureLocals(get.index(), argumentLocals, capturesBySlot);
            return;
        }
        if (expression instanceof IRSlice slice) {
            collectCaptureLocals(slice.target(), argumentLocals, capturesBySlot);
            collectCaptureLocals(slice.start(), argumentLocals, capturesBySlot);
            collectCaptureLocals(slice.end(), argumentLocals, capturesBySlot);
        }
    }

    private IRExpression lowerSortArray(
            ASTExprSortArray sortArray, MethodContext context, List<CompilationProblem> problems) {
        IRExpression source = lowerExpression(sortArray.source(), context, problems);
        List<IRExpression> extras = new ArrayList<>();
        for (ASTExpression extra : sortArray.extraArguments())
            extras.add(lowerExpression(extra, context, problems));

        IRLocal itemsLocal = context.addSyntheticLocal(sortArray.line(), "sort_items", RuntimeTypes.MIXED);
        IRLocal indexLocal = context.addSyntheticLocal(sortArray.line(), "sort_index", RuntimeTypes.INT);
        IRLocal innerIndexLocal = context.addSyntheticLocal(sortArray.line(), "sort_inner_index", RuntimeTypes.INT);
        IRLocal swapLocal = context.addSyntheticLocal(sortArray.line(), "sort_swap", RuntimeTypes.MIXED);

        IRExpression comparator = lowerExpression(sortArray.comparator(), context, problems);

        return new IRSortArray(
                sortArray.line(),
                coerceIfNeeded(source, RuntimeTypes.MIXED),
                comparator,
                itemsLocal,
                indexLocal,
                innerIndexLocal,
                swapLocal,
                extras,
                RuntimeTypes.arrayOf(RuntimeTypes.MIXED));
    }

    private int maxClosureArgumentIndex(ASTExpression expression) {
        if (expression == null)
            return 0;
        if (expression instanceof ASTExprClosureArgument closureArgument)
            return closureArgument.index();
        if (expression instanceof ASTExprArrayAccess access)
            return Math.max(maxClosureArgumentIndex(access.target()), maxClosureArgumentIndex(access.index()));
        if (expression instanceof ASTExprArrayStore store)
            return Math.max(
                    Math.max(maxClosureArgumentIndex(store.target()), maxClosureArgumentIndex(store.index())),
                    maxClosureArgumentIndex(store.value()));
        if (expression instanceof ASTExprArrayMutation mutation)
            return Math.max(maxClosureArgumentIndex(mutation.target()), maxClosureArgumentIndex(mutation.index()));
        if (expression instanceof ASTExprSliceAccess access)
            return Math.max(
                    Math.max(maxClosureArgumentIndex(access.target()), maxClosureArgumentIndex(access.start())),
                    maxClosureArgumentIndex(access.end()));
        if (expression instanceof ASTExprSliceStore store)
            return Math.max(
                    Math.max(
                            Math.max(maxClosureArgumentIndex(store.target()), maxClosureArgumentIndex(store.start())),
                            maxClosureArgumentIndex(store.end())),
                    maxClosureArgumentIndex(store.value()));
        if (expression instanceof ASTExprOpUnary unary)
            return maxClosureArgumentIndex(unary.right());
        if (expression instanceof ASTExprOpBinary binary)
            return Math.max(maxClosureArgumentIndex(binary.left()), maxClosureArgumentIndex(binary.right()));
        if (expression instanceof ASTExprSequence sequence) {
            int max = 0;
            for (ASTExpression nested : sequence.expressions())
                max = Math.max(max, maxClosureArgumentIndex(nested));
            return max;
        }
        if (expression instanceof ASTExprProtectedEval protectedEval)
            return maxClosureArgumentIndex(protectedEval.body());
        if (expression instanceof ASTExprTernary ternary)
            return Math.max(
                    Math.max(maxClosureArgumentIndex(ternary.condition()), maxClosureArgumentIndex(ternary.thenBranch())),
                    maxClosureArgumentIndex(ternary.elseBranch()));
        if (expression instanceof ASTExprInlineCallable)
            return 0;
        if (expression instanceof ASTExprCallEfun callEfun)
            return maxClosureArgumentIndex(callEfun.arguments());
        if (expression instanceof ASTExprCallMethod callMethod)
            return maxClosureArgumentIndex(callMethod.arguments());
        if (expression instanceof ASTExprDynamicInvoke dynamicInvoke)
            return Math.max(
                    maxClosureArgumentIndex(dynamicInvoke.target()),
                    maxClosureArgumentIndex(dynamicInvoke.arguments()));
        if (expression instanceof ASTExprInvokeLocal invokeLocal)
            return maxClosureArgumentIndex(invokeLocal.arguments());
        if (expression instanceof ASTExprInvokeField invokeField)
            return maxClosureArgumentIndex(invokeField.arguments());
        if (expression instanceof ASTExprArrayLiteral arrayLiteral) {
            int max = 0;
            for (ASTExpression element : arrayLiteral.elements())
                max = Math.max(max, maxClosureArgumentIndex(element));
            return max;
        }
        if (expression instanceof ASTExprMappingLiteral mappingLiteral) {
            int max = 0;
            for (ASTExprMappingEntry entry : mappingLiteral.entries()) {
                max = Math.max(max, maxClosureArgumentIndex(entry.key()));
                max = Math.max(max, maxClosureArgumentIndex(entry.value()));
            }
            return max;
        }
        return 0;
    }

    private String typedFunctionSignature(ASTExprTypedFunctionLiteral functionLiteral) {
        List<String> parameters = new ArrayList<>();
        functionLiteral.parameters().forEach(parameter -> parameters.add(
                parameter.symbol().declaredTypeName() + " " + parameter.symbol().name()));
        return functionLiteral.returnSymbol().declaredTypeName() + " (" + String.join(", ", parameters) + ")";
    }

    private int maxClosureArgumentIndex(ASTArguments arguments) {
        if (arguments == null)
            return 0;
        int max = 0;
        for (ASTArgument argument : arguments)
            max = Math.max(max, maxClosureArgumentIndex(argument.expression()));
        return max;
    }

    private List<IRExpression> lowerArguments(
            ASTArguments arguments, MethodContext context, List<CompilationProblem> problems) {
        List<IRExpression> lowered = new ArrayList<>();

        if (arguments == null)
            return lowered;

        for (ASTArgument argument : arguments)
            lowered.add(lowerExpression(argument.expression(), context, problems));

        return lowered;
    }

    private List<RuntimeType> parameterTypes(ASTMethod method) {
        List<RuntimeType> types = new ArrayList<>();

        if (method.parameters() == null)
            return types;

        for (ASTParameter parameter : method.parameters())
            types.add(runtimeType(parameter.symbol().lpcType()));

        return types;
    }

    private int parameterCount(ASTMethod method) {
        if (method == null || method.parameters() == null)
            return 0;
        int count = 0;
        for (ASTParameter ignored : method.parameters())
            count++;
        return count;
    }

    private record MethodKey(String name, int arity) {}

    private RuntimeType sliceReplacementType(RuntimeType targetType) {
        if (targetType == null || targetType.kind() == RuntimeValueKind.MIXED)
            return RuntimeTypes.MIXED;
        if (targetType.kind() == RuntimeValueKind.STRING)
            return RuntimeTypes.STRING;
        if (targetType.kind() == RuntimeValueKind.ARRAY)
            return RuntimeTypes.arrayOf(RuntimeTypes.MIXED);
        return RuntimeTypes.MIXED;
    }

    private RuntimeType sliceSetResultType(RuntimeType targetType) {
        if (targetType == null || targetType.kind() == RuntimeValueKind.MIXED)
            return RuntimeTypes.MIXED;
        if (targetType.kind() == RuntimeValueKind.STRING)
            return RuntimeTypes.STRING;
        if (targetType.kind() == RuntimeValueKind.ARRAY)
            return RuntimeTypes.arrayOf(RuntimeTypes.MIXED);
        return RuntimeTypes.MIXED;
    }

    private IRExpression storeStringSliceResult(
            ASTExprSliceStore sliceStore, MethodContext context, List<CompilationProblem> problems, IRExpression value) {
        if (sliceStore.target() instanceof ASTExprLocalAccess access) {
            IRLocal local = context.requireLocal(access.local(), problems);
            return new IRLocalStore(sliceStore.line(), local, coerceIfNeeded(value, local.type()));
        }
        if (sliceStore.target() instanceof ASTExprFieldAccess access) {
            IRField field = context.requireField(access.field(), problems);
            return new IRFieldStore(sliceStore.line(), field, coerceIfNeeded(value, field.type()));
        }

        problems.add(new CompilationProblem(
                CompilationStage.LOWER,
                "String slice assignment requires a local or field target",
                sliceStore.line()));
        return value;
    }

    private IRExpression coerceIfNeeded(IRExpression value, RuntimeType targetType) {
        if (value == null)
            return new IRConstant(0, null, targetType != null ? targetType : RuntimeTypes.MIXED);

        if (targetType == null || targetType.equals(value.type()))
            return value;

        if (value instanceof IRConstant constant
                && constant.type() == RuntimeTypes.INT
                && Integer.valueOf(0).equals(constant.value())) {
            return switch (targetType.kind()) {
            case STRING -> new IRConstant(value.line(), "", RuntimeTypes.STRING);
            case STATUS -> new IRConstant(value.line(), 0, RuntimeTypes.STATUS);
            case OBJECT -> new IRConstant(value.line(), null, RuntimeTypes.OBJECT);
            case ARRAY -> new IRArrayLiteral(value.line(), List.of(), targetType);
            case MAPPING -> new IRMappingLiteral(value.line(), List.of(), targetType);
            default -> new IRCoerce(value.line(), value, targetType);
            };
        }

        return new IRCoerce(value.line(), value, targetType);
    }

    /** Keeps from-end index markers boxed while coercing ordinary bounds to integer indexes. */
    private IRExpression indexExpression(IRExpression value) {
        if (value instanceof IRFromEndIndex)
            return value;
        return coerceIfNeeded(value, RuntimeTypes.INT);
    }

    private RuntimeType runtimeType(LPCType lpcType) {
        return RuntimeTypes.fromLpcType(lpcType);
    }

    private IRExpression defaultReturnExpression(int line, RuntimeType returnType) {
        if (returnType == null || returnType == RuntimeTypes.VOID)
            return null;

        return switch (returnType.kind()) {
        case INT, STATUS -> new IRConstant(line, 0, returnType);
        case FLOAT -> new IRConstant(line, 0.0f, returnType);
        case STRING, OBJECT, CALLABLE, MAPPING, MIXED, ARRAY, EFUN, INTERNAL_NULL, ERROR ->
            new IRConstant(line, null, returnType);
        case VOID -> null;
        };
    }

    private static final class BlockBuilder {
        private final String label;
        private final List<IRStatement> statements = new ArrayList<>();
        private IRTerminator terminator;

        private BlockBuilder(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public void addStatement(IRStatement statement) {
            statements.add(statement);
        }

        public boolean isTerminated() {
            return terminator != null;
        }

        public void terminate(IRTerminator terminator) {
            this.terminator = terminator;
        }

        public IRBlock build(IRTerminator defaultTerminator) {
            return new IRBlock(label, statements, terminator != null ? terminator : defaultTerminator);
        }
    }

    private static final class MethodContext {
        private final RuntimeType returnType;
        private final Map<Symbol, IRField> fieldsBySymbol;
        private final String currentInternalName;
        private final Set<String> flattenedInheritedMethodOwners;
        private final Set<String> primaryParentLineage;
        private final Map<Symbol, IRLocal> localsBySymbol = new HashMap<>();
        private final Map<Integer, IRLocal> localsBySlot = new HashMap<>();
        private final List<IRParameter> parameters = new ArrayList<>();
        private final List<IRLocal> locals = new ArrayList<>();
        private final List<BlockBuilder> blocks = new ArrayList<>();
        private final Deque<String> breakTargets = new ArrayDeque<>();
        private final Deque<String> continueTargets = new ArrayDeque<>();
        private final Deque<List<IRLocal>> closureArgumentLocals = new ArrayDeque<>();

        private int blockCounter = 0;
        private int syntheticLocalCounter = 0;

        private MethodContext(RuntimeType returnType, Map<Symbol, IRField> fieldsBySymbol, String currentInternalName) {
            this(returnType, fieldsBySymbol, currentInternalName, Set.of(), Set.of());
        }

        private MethodContext(
                RuntimeType returnType,
                Map<Symbol, IRField> fieldsBySymbol,
                String currentInternalName,
                Set<String> flattenedInheritedMethodOwners,
                Set<String> primaryParentLineage) {
            this.returnType = returnType != null ? returnType : RuntimeTypes.MIXED;
            this.fieldsBySymbol = fieldsBySymbol;
            this.currentInternalName = currentInternalName;
            this.flattenedInheritedMethodOwners =
                    flattenedInheritedMethodOwners != null ? flattenedInheritedMethodOwners : Set.of();
            this.primaryParentLineage = primaryParentLineage != null ? primaryParentLineage : Set.of();
        }

        public boolean isFlattenedOwner(String ownerInternalName) {
            return ownerInternalName != null && flattenedInheritedMethodOwners.contains(ownerInternalName);
        }

        /**
         * Returns true when an implicit {@code this} call should be emitted against the generated
         * current class instead of the LPC-resolved owner. Secondary LPC inherits are flattened into
         * classes on the primary JVM superclass chain, so their source owner names are not necessarily
         * assignable Java receivers.
         */
        public boolean shouldCallThroughCurrent(String ownerInternalName) {
            if (ownerInternalName == null || Objects.equals(ownerInternalName, currentInternalName))
                return false;

            return isFlattenedOwner(ownerInternalName) || !primaryParentLineage.contains(ownerInternalName);
        }

        public BlockBuilder newBlock(String prefix) {
            BlockBuilder builder = new BlockBuilder(prefix + "_" + blockCounter++);
            blocks.add(builder);
            return builder;
        }

        public void registerLocal(Symbol symbol, IRLocal local) {
            localsBySymbol.put(symbol, local);
            localsBySlot.put(local.slot(), local);
        }

        public IRLocal addSyntheticLocal(int line, String prefix, RuntimeType type) {
            int slot = 1;
            for (IRParameter parameter : parameters)
                slot = Math.max(slot, parameter.local().slot() + 1);
            for (IRLocal local : locals)
                slot = Math.max(slot, local.slot() + 1);

            IRLocal local = new IRLocal(line, prefix + "_" + syntheticLocalCounter++, type, slot, false);
            locals.add(local);
            localsBySlot.put(local.slot(), local);
            return local;
        }

        public void pushLoop(String breakTarget, String continueTarget) {
            breakTargets.push(breakTarget);
            continueTargets.push(continueTarget);
        }

        public void popLoop() {
            if (!breakTargets.isEmpty())
                breakTargets.pop();
            if (!continueTargets.isEmpty())
                continueTargets.pop();
        }

        public void pushBreakTarget(String breakTarget) {
            breakTargets.push(breakTarget);
        }

        public void popBreakTarget() {
            if (!breakTargets.isEmpty())
                breakTargets.pop();
        }

        public String currentBreakTarget() {
            return breakTargets.peek();
        }

        public String currentContinueTarget() {
            return continueTargets.peek();
        }

        public void pushClosureArguments(List<IRLocal> argumentLocals) {
            closureArgumentLocals.push(argumentLocals);
        }

        public void popClosureArguments() {
            if (!closureArgumentLocals.isEmpty())
                closureArgumentLocals.pop();
        }

        public IRLocal closureArgumentLocal(int index) {
            if (index < 1 || closureArgumentLocals.isEmpty())
                return null;

            List<IRLocal> locals = closureArgumentLocals.peek();
            return index <= locals.size() ? locals.get(index - 1) : null;
        }

        public IRLocal requireLocal(ASTLocal astLocal, List<CompilationProblem> problems) {
            if (astLocal == null || astLocal.symbol() == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.LOWER,
                                "Encountered null local reference during lowering.",
                                (astLocal != null) ? astLocal.line() : null));
                return new IRLocal(0, "<invalid>", RuntimeTypes.MIXED, -1, false);
            }

            IRLocal local = localsBySymbol.get(astLocal.symbol());

            if (local != null)
                return local;

            IRLocal synthesized =
                    new IRLocal(astLocal.line(), astLocal.symbol().name(), RuntimeTypes.MIXED, astLocal.slot(), false);
            registerLocal(astLocal.symbol(), synthesized);
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "Synthesizing missing local '" + astLocal.symbol().name() + "' during lowering.",
                            astLocal.line()));
            return synthesized;
        }

        public IRLocal localBySlot(int slot, List<CompilationProblem> problems) {
            IRLocal local = localsBySlot.get(slot);
            if (local != null)
                return local;

            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "No local found at slot " + slot + " for dynamic invocation.",
                            (Integer) null));
            return new IRLocal(0, "<invalid>", RuntimeTypes.MIXED, slot, false);
        }

        public IRField requireField(ASTField astField, List<CompilationProblem> problems) {
            IRField field = fieldsBySymbol.get(astField.symbol());
            if (field != null && !isFlattenedOwner(field.ownerInternalName()))
                return field;

            RuntimeType type = RuntimeTypes.fromLpcType(astField.symbol().lpcType());
            String owner = (astField.ownerName() != null) ? astField.ownerName() : currentInternalName;
            if (isFlattenedOwner(owner)) {
                IRField flattened = findCurrentField(astField.symbol().name());
                if (flattened != null) {
                    fieldsBySymbol.put(astField.symbol(), flattened);
                    return flattened;
                }
                owner = currentInternalName;
            }
            IRField synthesized = new IRField(astField.line(), owner, astField.symbol().name(), type, null);
            fieldsBySymbol.put(astField.symbol(), synthesized);
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "Synthesizing missing field '" + astField.symbol().name() + "' during lowering.",
                            astField.line()));
            return synthesized;
        }

        private IRField findCurrentField(String name) {
            for (IRField field : fieldsBySymbol.values()) {
                if (Objects.equals(field.ownerInternalName(), currentInternalName) && Objects.equals(field.name(), name))
                    return field;
            }
            return null;
        }

        public List<IRBlock> buildBlocks(IRTerminator defaultTerminator) {
            List<IRBlock> built = new ArrayList<>();
            for (BlockBuilder builder : blocks)
                built.add(builder.build(defaultTerminator));
            return built;
        }
    }
}
