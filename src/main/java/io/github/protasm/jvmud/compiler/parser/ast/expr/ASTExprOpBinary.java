package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.BinaryOpType;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;

/** Binary operator expression, including semantic refinements for context-sensitive LPC operators. */
public final class ASTExprOpBinary extends ASTExpression {
    private final ASTExpression left;
    private final ASTExpression right;
    private final BinaryOpType operator;
    private LPCType inferredType;

    public ASTExprOpBinary(int line, ASTExpression left, ASTExpression right, BinaryOpType operator) {
        super(line);

        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public ASTExpression left() {
        return left;
    }

    public ASTExpression right() {
        return right;
    }

    public BinaryOpType operator() {
        return operator;
    }

    /**
     * Records the type selected by semantic analysis when an operator is ambiguous without context.
     *
     * <p>For example, {@code mixed + mixed} in an explicit string destination is treated as LPC
     * string concatenation, while the same expression without that expected type remains numeric by
     * default. JVMud uses this for narrow LPMud compatibility idioms such as legacy text efuns that
     * return {@code mixed}, not as a general license to infer arbitrary operator meanings from
     * context.</p>
     *
     * @param inferredType resolved expression type, or {@code null} to fall back to structural
     *     inference
     */
    public void setInferredType(LPCType inferredType) {
        this.inferredType = inferredType;
    }

    @Override
    public LPCType lpcType() {
        if (inferredType != null)
            return inferredType;

        return switch (operator) {
        case BOP_ADD -> {
            if (left.lpcType() == LPCType.LPCARRAY || right.lpcType() == LPCType.LPCARRAY)
                yield LPCType.LPCARRAY;
            if (left.lpcType() == LPCType.LPCMAPPING || right.lpcType() == LPCType.LPCMAPPING)
                yield LPCType.LPCMAPPING;
            if (left.lpcType() == LPCType.LPCSTRING || right.lpcType() == LPCType.LPCSTRING)
                yield LPCType.LPCSTRING;
            yield numericResultType();
        }
        case BOP_SUB -> {
            if (left.lpcType() == LPCType.LPCARRAY || right.lpcType() == LPCType.LPCARRAY)
                yield LPCType.LPCARRAY;
            if (left.lpcType() == LPCType.LPCSTRING || right.lpcType() == LPCType.LPCSTRING)
                yield LPCType.LPCSTRING;
            yield numericResultType();
        }
        case BOP_MULT, BOP_DIV, BOP_MOD -> numericResultType();
        case BOP_BIT_OR, BOP_BIT_AND, BOP_BIT_XOR, BOP_SHL, BOP_SHR -> LPCType.LPCINT;
        case BOP_GT, BOP_GE, BOP_LT, BOP_LE, BOP_EQ, BOP_NE, BOP_OR, BOP_AND -> LPCType.LPCSTATUS;
        };
    }

    private LPCType numericResultType() {
        if (left.lpcType() == LPCType.LPCFLOAT || right.lpcType() == LPCType.LPCFLOAT)
            return LPCType.LPCFLOAT;
        return LPCType.LPCINT;
    }
}
