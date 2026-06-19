package io.github.protasm.jvmud.compiler.semantic;

import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArgument;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
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
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFromEndIndex;
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
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprMappingLiteral;
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
import java.util.List;
import java.util.Objects;

/**
 * Semantic type checker that validates expressions, arguments, and returns while refining symbol
 * types when they remain unspecified or {@code mixed}.
 */
public final class SemanticTypeChecker {
    private final List<CompilationProblem> problems;

    public SemanticTypeChecker(List<CompilationProblem> problems) {
        this.problems = Objects.requireNonNull(problems, "problems");
    }

    public void check(ASTObject object) {
        for (ASTField field : object.fields())
            checkField(field);

        for (ASTMethod method : object.methods())
            checkMethod(method);
    }

    private void checkField(ASTField field) {
        if (field.initializer() == null)
            return;

        LPCType fieldType = valueType(field.symbol());
        LPCType valueType = inferExpressionType(field.initializer(), null, explicitExpectedType(field.symbol()));
        valueType = coerceZeroLiteralFalse(fieldType, field.initializer(), valueType);
        ensureAssignable(fieldType, valueType, field.line(), "Field initializer type mismatch");
    }

    private void checkMethod(ASTMethod method) {
        if (method.body() == null)
            return;

        MethodContext context = new MethodContext(method);
        checkStatement(method.body(), context);
        context.finalizeReturn();
    }

    private void checkStatement(ASTStatement statement, MethodContext context) {
        if (statement == null)
            return;

        if (statement instanceof ASTStmtBlock block) {
            for (ASTStatement nested : block)
                checkStatement(nested, context);
            return;
        }

        if (statement instanceof ASTStmtExpression stmtExpression) {
            inferExpressionType(stmtExpression.expression(), context);
            return;
        }

        if (statement instanceof ASTStmtEmpty)
            return;

        if (statement instanceof ASTStmtIfThenElse stmtIf) {
            // All expressions participate in truthiness; conditions are not restricted to booleans.
            inferExpressionType(stmtIf.condition(), context);
            checkStatement(stmtIf.thenBranch(), context);
            if (stmtIf.elseBranch() != null)
                checkStatement(stmtIf.elseBranch(), context);
            return;
        }

        if (statement instanceof ASTStmtFor stmtFor) {
            inferExpressionType(stmtFor.initializer(), context);
            inferExpressionType(stmtFor.condition(), context);
            inferExpressionType(stmtFor.update(), context);
            checkStatement(stmtFor.body(), context);
            return;
        }

        if (statement instanceof ASTStmtForeach stmtForeach) {
            inferExpressionType(stmtForeach.iterable(), context);
            checkStatement(stmtForeach.body(), context);
            return;
        }

        if (statement instanceof ASTStmtWhile stmtWhile) {
            inferExpressionType(stmtWhile.condition(), context);
            checkStatement(stmtWhile.body(), context);
            return;
        }

        if (statement instanceof ASTStmtDoWhile stmtDoWhile) {
            checkStatement(stmtDoWhile.body(), context);
            inferExpressionType(stmtDoWhile.condition(), context);
            return;
        }

        if (statement instanceof ASTStmtSwitch stmtSwitch) {
            inferExpressionType(stmtSwitch.expression(), context);
            for (ASTStmtSwitch.SwitchCase switchCase : stmtSwitch.cases()) {
                if (!switchCase.isDefault()) {
                    inferExpressionType(switchCase.expression(), context);
                    if (switchCase.isRange())
                        inferExpressionType(switchCase.rangeEndExpression(), context);
                }
                for (ASTStatement nested : switchCase.statements())
                    checkStatement(nested, context);
            }
            return;
        }

        if (statement instanceof ASTStmtBreak)
            return;

        if (statement instanceof ASTStmtContinue)
            return;

        if (statement instanceof ASTStmtReturn stmtReturn) {
            LPCType declaredReturn =
                    (context.method != null && context.method.symbol() != null) ? context.method.symbol().lpcType() : null;
            LPCType valueType = (stmtReturn.returnValue() != null)
                    ? inferExpressionType(stmtReturn.returnValue(), context, explicitExpectedType(context.method.symbol()))
                    : LPCType.LPCVOID;
            if (stmtReturn.returnValue() != null) {
                valueType = coerceZeroLiteralFalse(declaredReturn, stmtReturn.returnValue(), valueType);
            }
            context.recordReturn(valueType, stmtReturn.isSynthetic(), stmtReturn.line());
            return;
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unsupported statement kind: " + statement.getClass().getSimpleName(),
                        statement.line()));
    }

    private LPCType inferExpressionType(ASTExpression expression, MethodContext context) {
        return inferExpressionType(expression, context, null);
    }

    /**
     * Infers an expression type with a narrowly scoped expected type hint from explicit LPC
     * declarations.
     *
     * <p>The expected type is intentionally used only for ambiguous compatibility cases, such as
     * treating {@code mixed + mixed} as string concatenation in an explicitly string-typed
     * destination. That narrow rule reflects a common LPMud idiom where legacy efuns return
     * {@code mixed} because they may produce either text or LPC false ({@code 0}), but mudlib source
     * still combines those results as strings.</p>
     *
     * <p>This is a compatibility rule, not a general philosophy that JVMud should guess types from
     * context. New expected-type propagation should stay tied to specific, demonstrated LPC mudlib
     * idioms with smoke coverage.</p>
     */
    private LPCType inferExpressionType(ASTExpression expression, MethodContext context, LPCType expectedType) {
        if (expression == null)
            return LPCType.LPCERROR;

        if (expression instanceof ASTExprLiteralInteger)
            return LPCType.LPCINT;
        if (expression instanceof ASTExprLiteralFloat)
            return LPCType.LPCFLOAT;
        if (expression instanceof ASTExprLiteralString)
            return LPCType.LPCSTRING;
        if (expression instanceof ASTExprLiteralTrue || expression instanceof ASTExprLiteralFalse)
            return LPCType.LPCSTATUS;
        if (expression instanceof ASTExprError)
            return LPCType.LPCERROR;
        if (expression instanceof ASTExprArrayLiteral)
            return LPCType.LPCARRAY;
        if (expression instanceof ASTExprMappingLiteral)
            return LPCType.LPCMAPPING;
        if (expression instanceof ASTExprClosureArgument)
            return LPCType.LPCMIXED;
        if (expression instanceof ASTExprInlineCallable inlineCallable) {
            inferExpressionType(inlineCallable.body(), context);
            return LPCType.LPCMIXED;
        }
        if (expression instanceof ASTExprTypedFunctionLiteral typedFunction) {
            inferExpressionType(typedFunction.body(), context, typedFunction.returnSymbol().lpcType());
            return LPCType.LPCFUNCTION;
        }
        if (expression instanceof ASTExprCollectionTransform transform) {
            inferExpressionType(transform.source(), context);
            inferExpressionType(transform.callback().body(), context);
            for (ASTExpression extra : transform.extraArguments())
                inferExpressionType(extra, context);
            return transform.lpcType();
        }
        if (expression instanceof ASTExprSortArray sortArray) {
            inferExpressionType(sortArray.source(), context);
            inferExpressionType(sortArray.comparator().body(), context);
            for (ASTExpression extra : sortArray.extraArguments())
                inferExpressionType(extra, context);
            return sortArray.lpcType();
        }
        if (expression instanceof ASTExprSymbolLiteral || expression instanceof ASTExprFunctionReference)
            return LPCType.LPCMIXED;

        if (expression instanceof ASTExprLocalAccess access)
            return valueType(access.local().symbol());

        if (expression instanceof ASTExprFieldAccess access)
            return valueType(access.field().symbol());

        if (expression instanceof ASTExprArrayAccess arrayAccess)
            return inferIndexAccessType(arrayAccess, context);

        if (expression instanceof ASTExprSliceAccess sliceAccess)
            return inferSliceAccessType(sliceAccess, context);

        if (expression instanceof ASTExprSliceStore sliceStore)
            return inferSliceStoreType(sliceStore, context);

        if (expression instanceof ASTExprFromEndIndex fromEnd) {
            LPCType distanceType = inferExpressionType(fromEnd.distance(), context);
            ensureAssignable(LPCType.LPCINT, distanceType, fromEnd.line(), "From-end index expects integer distance");
            return LPCType.LPCINT;
        }

        if (expression instanceof ASTExprFieldStore store) {
            LPCType fieldType = valueType(store.field().symbol());
            LPCType valueType = inferExpressionType(store.value(), context, explicitExpectedType(store.field().symbol()));
            if (fieldType == LPCType.LPCSTRING && valueType == LPCType.LPCARRAY)
                fieldType = promoteStringSymbolToArray(store.field().symbol());
            valueType = coerceZeroLiteralFalse(fieldType, store.value(), valueType);
            ensureAssignable(fieldType, valueType, store.line(), "Field assignment type mismatch");
            return fieldType(store.field().symbol(), valueType);
        }

        if (expression instanceof ASTExprFieldMutation mutation)
            return inferVariableMutationType(
                    valueType(mutation.field().symbol()), mutation.line(), "Field increment expects numeric target");

        if (expression instanceof ASTExprLocalStore store) {
            LPCType localType = valueType(store.local().symbol());
            LPCType valueType = inferExpressionType(store.value(), context, explicitExpectedType(store.local().symbol()));
            if (localType == LPCType.LPCSTRING && valueType == LPCType.LPCARRAY)
                localType = promoteStringSymbolToArray(store.local().symbol());
            valueType = coerceZeroLiteralFalse(localType, store.value(), valueType);
            ensureAssignable(localType, valueType, store.line(), "Local assignment type mismatch");
            return localType != null ? localType : valueType;
        }

        if (expression instanceof ASTExprLocalMutation mutation)
            return inferVariableMutationType(
                    valueType(mutation.local().symbol()), mutation.line(), "Local increment expects numeric target");

        if (expression instanceof ASTExprArrayStore store)
            return inferIndexStoreType(store, context);

        if (expression instanceof ASTExprArrayMutation mutation)
            return inferIndexMutationType(mutation, context);

        if (expression instanceof ASTExprOpUnary unary)
            return inferUnaryType(unary, context);

        if (expression instanceof ASTExprOpBinary binary)
            return inferBinaryType(binary, context, expectedType);

        if (expression instanceof ASTExprSequence sequence)
            return inferSequenceType(sequence, context);

        if (expression instanceof ASTExprProtectedEval protectedEval) {
            inferExpressionType(protectedEval.body(), context);
            return LPCType.LPCMIXED;
        }

        if (expression instanceof ASTExprTernary ternary)
            return inferTernaryType(ternary, context);

        if (expression instanceof ASTExprCallEfun callEfun)
            return inferEfunCall(callEfun, context);

        if (expression instanceof ASTExprCallMethod callMethod)
            return inferMethodCall(callMethod, context);

        if (expression instanceof ASTExprDynamicInvoke dynamicInvoke) {
            inferExpressionType(dynamicInvoke.target(), context);
            inferArguments(dynamicInvoke.arguments(), null, context);
            return LPCType.LPCMIXED;
        }

        if (expression instanceof ASTExprInvokeLocal invokeLocal) {
            inferArguments(invokeLocal.arguments(), null, context);
            invokeLocal.setLPCType(LPCType.LPCMIXED);
            return LPCType.LPCMIXED;
        }

        if (expression instanceof ASTExprInvokeField invokeField) {
            inferArguments(invokeField.arguments(), null, context);
            invokeField.setLPCType(LPCType.LPCMIXED);
            return LPCType.LPCMIXED;
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unsupported expression kind: " + expression.getClass().getSimpleName(),
                        expression.line()));
        return LPCType.LPCMIXED;
    }

    private LPCType inferSequenceType(ASTExprSequence sequence, MethodContext context) {
        LPCType type = LPCType.LPCERROR;
        for (ASTExpression expression : sequence.expressions())
            type = inferExpressionType(expression, context);
        return type;
    }

    private LPCType inferTernaryType(ASTExprTernary expr, MethodContext context) {
        inferExpressionType(expr.condition(), context);
        LPCType thenType = inferExpressionType(expr.thenBranch(), context);
        LPCType elseType = inferExpressionType(expr.elseBranch(), context);

        if (isZeroLiteral(expr.thenBranch()))
            thenType = coerceZeroLiteralFalse(elseType, expr.thenBranch(), thenType);
        if (isZeroLiteral(expr.elseBranch()))
            elseType = coerceZeroLiteralFalse(thenType, expr.elseBranch(), elseType);

        LPCType resolved = resolveTernaryType(thenType, elseType);
        expr.setLPCType(resolved);
        return resolved;
    }

    private LPCType resolveTernaryType(LPCType thenType, LPCType elseType) {
        if (thenType == elseType)
            return thenType;

        if (isTypeAssignable(thenType, elseType))
            return thenType;

        if (isTypeAssignable(elseType, thenType))
            return elseType;

        return LPCType.LPCMIXED;
    }

    private LPCType inferUnaryType(ASTExprOpUnary expr, MethodContext context) {
        // Logical negation is allowed on any type; rely on runtime truthiness.
        LPCType operandType = inferExpressionType(expr.right(), context);

        if (expr.operator() == UnaryOpType.UOP_NOT)
            return LPCType.LPCSTATUS;

        if (expr.operator() == UnaryOpType.UOP_BIT_NOT) {
            ensureAssignable(LPCType.LPCINT, operandType, expr.line(), "Bitwise complement expects integer operand");
            return LPCType.LPCINT;
        }

        ensureAssignable(LPCType.LPCINT, operandType, expr.line(), "Unary operator expects numeric operand");
        return operandType == LPCType.LPCFLOAT ? LPCType.LPCFLOAT : LPCType.LPCINT;
    }

    private LPCType inferBinaryType(ASTExprOpBinary expr, MethodContext context, LPCType expectedType) {
        LPCType leftType = inferExpressionType(expr.left(), context);
        LPCType rightType = inferExpressionType(expr.right(), context);
        BinaryOpType op = expr.operator();

        switch (op) {
        case BOP_ADD -> {
            if (leftType == LPCType.LPCARRAY || rightType == LPCType.LPCARRAY) {
                if (!isArrayLikeForConcat(leftType) || !isArrayLikeForConcat(rightType)) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Array concatenation requires two arrays",
                                    expr.line()));
                }
                return LPCType.LPCARRAY;
            }
            if (leftType == LPCType.LPCSTRING || rightType == LPCType.LPCSTRING)
                return LPCType.LPCSTRING;
            if (expectedType == LPCType.LPCSTRING && isStringLikeForConcat(leftType) && isStringLikeForConcat(rightType)) {
                expr.setInferredType(LPCType.LPCSTRING);
                return LPCType.LPCSTRING;
            }
            if (leftType == LPCType.LPCMAPPING || rightType == LPCType.LPCMAPPING) {
                if (!isMappingLikeForConcat(leftType) || !isMappingLikeForConcat(rightType)) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Mapping concatenation requires two mappings",
                                    expr.line()));
                }
                return LPCType.LPCMAPPING;
            }

            ensureNumericOperands(leftType, rightType, expr.line(), "Addition expects numeric operands");
            return LPCType.LPCINT;
        }
        case BOP_SUB -> {
            if (leftType == LPCType.LPCARRAY || rightType == LPCType.LPCARRAY) {
                if (!isArrayLikeForDifference(leftType) || !isArrayLikeForDifference(rightType)) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Array subtraction requires two arrays",
                                    expr.line()));
                }
                return LPCType.LPCARRAY;
            }
            if (leftType == LPCType.LPCSTRING || rightType == LPCType.LPCSTRING) {
                if (!isStringLikeForDifference(leftType) || !isStringLikeForDifference(rightType)) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "String subtraction requires two strings",
                                    expr.line()));
                }
                return LPCType.LPCSTRING;
            }
            if (expectedType == LPCType.LPCSTRING
                    && isStringLikeForDifference(leftType)
                    && isStringLikeForDifference(rightType)) {
                expr.setInferredType(LPCType.LPCSTRING);
                return LPCType.LPCSTRING;
            }
            ensureNumericOperands(leftType, rightType, expr.line(), op + " expects numeric operands");
            return LPCType.LPCINT;
        }
        case BOP_MULT, BOP_DIV, BOP_MOD -> {
            ensureNumericOperands(leftType, rightType, expr.line(), op + " expects numeric operands");
            return LPCType.LPCINT;
        }
        case BOP_BIT_OR, BOP_BIT_AND, BOP_BIT_XOR, BOP_SHL, BOP_SHR -> {
            ensureNumericOperands(leftType, rightType, expr.line(), op + " expects integer operands");
            return LPCType.LPCINT;
        }
        case BOP_AND, BOP_OR -> {
            return LPCType.LPCSTATUS;
        }
        case BOP_GT, BOP_GE, BOP_LT, BOP_LE -> {
            ensureComparableOperands(leftType, rightType, expr.line());
            return LPCType.LPCSTATUS;
        }
        case BOP_EQ, BOP_NE -> {
            return LPCType.LPCSTATUS;
        }
        }

        return LPCType.LPCMIXED;
    }

    private boolean isArrayLikeForConcat(LPCType type) {
        return type == LPCType.LPCARRAY || type == LPCType.LPCMIXED || type == LPCType.LPCERROR;
    }

    private boolean isMappingLikeForConcat(LPCType type) {
        return type == LPCType.LPCMAPPING || type == LPCType.LPCMIXED || type == LPCType.LPCERROR;
    }

    private boolean isStringLikeForConcat(LPCType type) {
        return type == LPCType.LPCSTRING || type == LPCType.LPCMIXED || type == LPCType.LPCERROR;
    }

    private boolean isArrayLikeForDifference(LPCType type) {
        return type == LPCType.LPCARRAY || type == LPCType.LPCMIXED || type == LPCType.LPCERROR;
    }

    private boolean isStringLikeForDifference(LPCType type) {
        return type == LPCType.LPCSTRING || type == LPCType.LPCMIXED || type == LPCType.LPCERROR;
    }

    private boolean isComparableType(LPCType type) {
        return type == LPCType.LPCINT
                || type == LPCType.LPCFLOAT
                || type == LPCType.LPCSTRING
                || type == LPCType.LPCMIXED
                || type == LPCType.LPCERROR;
    }

    private void ensureComparableOperands(LPCType left, LPCType right, int line) {
        if (isComparableType(left) && isComparableType(right))
            return;

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Comparison expects numeric, string, or mixed operands",
                        line));
    }

    private LPCType inferEfunCall(ASTExprCallEfun expr, MethodContext context) {
        EfunSignature signature = expr.signature();

        if (signature == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Missing engine function signature for call on line " + expr.line(),
                            expr.line()));
            inferArguments(expr.arguments(), null, context);
            return LPCType.LPCMIXED;
        }

        if (isSizeFunction(signature)) {
            inferSizeofArgument(expr.arguments(), context);
        } else {
            inferArguments(expr.arguments(), signature.parameterTypes(), context);
        }
        return signature.returnType();
    }

    private boolean isSizeFunction(EfunSignature signature) {
        return "sizeof".equals(signature.name()) || "jvmud_size".equals(signature.name());
    }

    private void inferSizeofArgument(ASTArguments arguments, MethodContext context) {
        if (arguments == null)
            return;

        if (arguments.size() != 1) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Argument count mismatch: expected 1 but found " + arguments.size(),
                            arguments.line()));
            inferArguments(arguments, null, context);
            return;
        }

        ASTArgument argument = arguments.get(0);
        LPCType actual = inferExpressionType(argument.expression(), context);
        if (actual == LPCType.LPCARRAY
                || actual == LPCType.LPCMAPPING
                || actual == LPCType.LPCSTRING
                || actual == LPCType.LPCMIXED
                || actual == LPCType.LPCERROR) {
            return;
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "sizeof expects array, mapping, or string argument",
                        argument.line()));
    }

    private LPCType inferMethodCall(ASTExprCallMethod expr, MethodContext context) {
        ASTParameters parameters = expr.method().parameters();
        inferArguments(expr.arguments(), parameters != null ? parameters.nodes().stream().map(ASTParameter::symbol).map(Symbol::lpcType).toList() : null, context);
        return valueType(expr.method().symbol());
    }

    private void inferArguments(ASTArguments arguments, List<LPCType> expectedTypes, MethodContext context) {
        if (arguments == null)
            return;

        for (int i = 0; i < arguments.size(); i++) {
            ASTArgument argument = arguments.get(i);
            LPCType expected = (expectedTypes != null && i < expectedTypes.size()) ? expectedTypes.get(i) : null;
            LPCType actual = inferExpressionType(argument.expression(), context, explicitExpectedType(expected));

            if (expected != null) {
                actual = coerceZeroLiteralFalse(expected, argument.expression(), actual);
                ensureAssignable(expected, actual, argument.line(), "Argument " + (i + 1) + " type mismatch");
            }
        }

        if (expectedTypes != null && arguments.size() > expectedTypes.size()) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Argument count mismatch: expected at most " + expectedTypes.size() + " but found " + arguments.size(),
                            arguments.line()));
        }
    }

    private void ensureNumericOperands(LPCType left, LPCType right, int line, String message) {
        ensureAssignable(LPCType.LPCINT, left, line, message);
        ensureAssignable(LPCType.LPCINT, right, line, message);
    }

    private LPCType inferIndexAccessType(ASTExprArrayAccess access, MethodContext context) {
        LPCType targetType = inferExpressionType(access.target(), context);
        LPCType indexType = inferExpressionType(access.index(), context);

        if (targetType == LPCType.LPCARRAY) {
            ensureAssignable(LPCType.LPCINT, indexType, access.line(), "Array index expects integer");
            return LPCType.LPCMIXED;
        }

        if (targetType == LPCType.LPCMAPPING) {
            ensureMappingKey(indexType, access.line());
            return LPCType.LPCMIXED;
        }

        if (targetType == LPCType.LPCSTRING) {
            ensureAssignable(LPCType.LPCINT, indexType, access.line(), "String index expects integer");
            return LPCType.LPCINT;
        }

        if (targetType == LPCType.LPCMIXED || targetType == null)
            return LPCType.LPCMIXED;

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE, "Indexing expects array, mapping, or string target", access.line()));
        return LPCType.LPCMIXED;
    }

    private LPCType inferSliceAccessType(ASTExprSliceAccess access, MethodContext context) {
        LPCType targetType = inferExpressionType(access.target(), context);
        LPCType startType = inferExpressionType(access.start(), context);
        ensureAssignable(LPCType.LPCINT, startType, access.line(), "Slice start expects integer");
        if (access.end() != null) {
            LPCType endType = inferExpressionType(access.end(), context);
            ensureAssignable(LPCType.LPCINT, endType, access.line(), "Slice end expects integer");
        }

        if (targetType == LPCType.LPCSTRING)
            return LPCType.LPCSTRING;

        if (targetType == LPCType.LPCARRAY)
            return LPCType.LPCARRAY;

        if (targetType == LPCType.LPCMIXED || targetType == null)
            return LPCType.LPCMIXED;

        problems.add(new CompilationProblem(
                CompilationStage.ANALYZE, "Slicing expects array or string target", access.line()));
        return LPCType.LPCMIXED;
    }

    private LPCType inferIndexStoreType(ASTExprArrayStore store, MethodContext context) {
        LPCType targetType = inferExpressionType(store.target(), context);
        LPCType valueType = inferExpressionType(store.value(), context);
        LPCType indexType = inferExpressionType(store.index(), context);

        if (targetType == LPCType.LPCARRAY) {
            ensureAssignable(LPCType.LPCINT, indexType, store.line(), "Array index expects integer");
            return valueType;
        }

        if (targetType == LPCType.LPCMAPPING) {
            ensureMappingKey(indexType, store.line());
            return valueType;
        }

        if (targetType == LPCType.LPCSTRING && promoteIndexedStringTarget(store.target())) {
            ensureAssignable(LPCType.LPCINT, indexType, store.line(), "Array index expects integer");
            return valueType;
        }

        if (targetType == LPCType.LPCMIXED || targetType == null)
            return valueType;

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE, "Assignment expects array or mapping target", store.line()));
        return valueType;
    }

    private LPCType inferSliceStoreType(ASTExprSliceStore store, MethodContext context) {
        LPCType targetType = inferExpressionType(store.target(), context);
        LPCType startType = inferExpressionType(store.start(), context);
        LPCType valueType = inferExpressionType(store.value(), context);
        ensureAssignable(LPCType.LPCINT, startType, store.line(), "Slice start expects integer");
        if (store.end() != null) {
            LPCType endType = inferExpressionType(store.end(), context);
            ensureAssignable(LPCType.LPCINT, endType, store.line(), "Slice end expects integer");
        }

        if (targetType == LPCType.LPCARRAY || targetType == LPCType.LPCMIXED || targetType == null) {
            ensureAssignable(LPCType.LPCARRAY, valueType, store.line(), "Array slice assignment expects array value");
            return LPCType.LPCARRAY;
        }

        problems.add(new CompilationProblem(
                CompilationStage.ANALYZE, "Slice assignment expects array target", store.line()));
        return valueType;
    }

    private void ensureMappingKey(LPCType keyType, int line) {
        if (keyType == LPCType.LPCVOID) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Mapping key expects a value",
                            line));
        }
    }

    private LPCType inferIndexMutationType(ASTExprArrayMutation mutation, MethodContext context) {
        LPCType targetType = inferExpressionType(mutation.target(), context);
        LPCType indexType = inferExpressionType(mutation.index(), context);

        if (targetType == LPCType.LPCARRAY) {
            ensureAssignable(LPCType.LPCINT, indexType, mutation.line(), "Array index expects integer");
            return LPCType.LPCMIXED;
        }

        if (targetType == LPCType.LPCMIXED || targetType == null)
            return LPCType.LPCMIXED;

        problems.add(new CompilationProblem(
                CompilationStage.ANALYZE, "Indexed increment expects array target", mutation.line()));
        return LPCType.LPCMIXED;
    }

    private LPCType inferVariableMutationType(LPCType targetType, int line, String message) {
        if (targetType == LPCType.LPCINT || targetType == LPCType.LPCFLOAT || targetType == LPCType.LPCMIXED)
            return targetType;
        if (targetType == null)
            return LPCType.LPCMIXED;

        problems.add(new CompilationProblem(CompilationStage.ANALYZE, message, line));
        return targetType;
    }

    private boolean promoteIndexedStringTarget(ASTExpression target) {
        Symbol symbol = null;

        if (target instanceof ASTExprLocalAccess access)
            symbol = access.local().symbol();
        else if (target instanceof ASTExprFieldAccess access)
            symbol = access.field().symbol();

        if (symbol == null || symbol.lpcType() != LPCType.LPCSTRING)
            return false;

        promoteStringSymbolToArray(symbol);
        return true;
    }

    private LPCType promoteStringSymbolToArray(Symbol symbol) {
        if (symbol != null)
            symbol.setLPCType(LPCType.LPCARRAY);
        return LPCType.LPCARRAY;
    }

    private void ensureAssignable(LPCType expected, LPCType actual, int line, String message) {
        if (expected == null || isTypeAssignable(expected, actual))
            return;

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        message + " (expected " + expected + " but found " + actual + ")",
                        line));
    }

    private boolean isTypeAssignable(LPCType expected, LPCType actual) {
        if (expected == LPCType.LPCMIXED || expected == LPCType.LPCERROR)
            return true;

        if (actual == null)
            return false;

        if (actual == LPCType.LPCMIXED)
            return true;

        if ((expected == LPCType.LPCINT && actual == LPCType.LPCSTATUS)
                || (expected == LPCType.LPCSTATUS && actual == LPCType.LPCINT))
            return true;

        if (actual == LPCType.LPCERROR)
            return true;

        return expected == actual;
    }

    private LPCType coerceZeroLiteralFalse(LPCType expected, ASTExpression expression, LPCType actual) {
        if (expected == null || actual == null)
            return actual;

        if (actual == LPCType.LPCINT && isZeroLiteral(expression) && isZeroAssignable(expected))
            return expected;

        return actual;
    }

    private boolean isZeroLiteral(ASTExpression expression) {
        return expression instanceof ASTExprLiteralInteger literal && literal.value() == 0;
    }

    private boolean isZeroAssignable(LPCType expected) {
        if (expected == null)
            return false;

        return expected == LPCType.LPCOBJECT
                || expected == LPCType.LPCSTRING
                || expected == LPCType.LPCMIXED
                || expected == LPCType.LPCARRAY
                || expected == LPCType.LPCMAPPING;
    }

    private LPCType valueType(Symbol symbol) {
        return (symbol != null) ? symbol.lpcType() : LPCType.LPCMIXED;
    }

    private LPCType explicitExpectedType(Symbol symbol) {
        return symbol != null ? explicitExpectedType(symbol.declaredType()) : null;
    }

    private LPCType explicitExpectedType(LPCType type) {
        if (type == null || type == LPCType.LPCMIXED || type == LPCType.LPCERROR || type == LPCType.LPCVOID)
            return null;

        return type;
    }

    private LPCType fieldType(Symbol symbol, LPCType candidate) {
        if (symbol == null)
            return candidate != null ? candidate : LPCType.LPCMIXED;

        refineSymbol(symbol, candidate);
        return symbol.lpcType();
    }

    private void refineSymbol(Symbol symbol, LPCType candidate) {
        if (symbol == null || candidate == null)
            return;

        LPCType declared = symbol.declaredType();
        LPCType existing = symbol.lpcType();

        if (declared != null && declared != LPCType.LPCMIXED && declared != LPCType.LPCERROR)
            return;

        if (existing == null || existing == LPCType.LPCMIXED)
            symbol.setLPCType(candidate);
    }

    private final class MethodContext {
        private final ASTMethod method;
        private LPCType inferredReturn;

        private MethodContext(ASTMethod method) {
            this.method = method;
        }

        void recordReturn(LPCType valueType, boolean synthetic, int line) {
            LPCType declared = method.symbol().lpcType();

            if (valueType == LPCType.LPCVOID) {
                if (synthetic && declared != null && declared != LPCType.LPCVOID && declared != LPCType.LPCMIXED) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Non-void methods must return a value of type " + declared + ".",
                                    line));
                }
                return;
            }

            if (synthetic && (declared == null || declared == LPCType.LPCMIXED))
                return;

            if (declared != null && declared != LPCType.LPCMIXED && declared != LPCType.LPCERROR) {
                ensureAssignable(declared, valueType, line, "Return type mismatch");
                return;
            }

            inferredReturn = mergeReturn(inferredReturn, valueType);
        }

        void finalizeReturn() {
            LPCType declared = method.symbol().lpcType();
            if ((declared == null || declared == LPCType.LPCMIXED) && inferredReturn != null)
                method.symbol().setLPCType(inferredReturn);
        }

        private LPCType mergeReturn(LPCType existing, LPCType candidate) {
            if (existing == null)
                return candidate;
            if (candidate == null)
                return existing;
            if (existing == candidate)
                return existing;
            return LPCType.LPCMIXED;
        }
    }
}
