package io.github.protasm.jvmud.compiler.parser.type;

import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.IXOR;

public enum BinaryOpType {
    BOP_ADD(IADD), BOP_SUB(ISUB), BOP_MULT(IMUL), BOP_DIV(IDIV), BOP_GT(IF_ICMPGT), BOP_LT(IF_ICMPLT),
    BOP_EQ(IF_ICMPEQ), BOP_NE(IF_ICMPNE), BOP_GE(IF_ICMPGE), BOP_LE(IF_ICMPLE), BOP_OR(-1), BOP_AND(-1),
    BOP_BIT_OR(IOR), BOP_BIT_AND(IAND), BOP_BIT_XOR(IXOR), BOP_SHL(ISHL), BOP_SHR(ISHR);

    private final int opcode;

    BinaryOpType(int opcode) {
        this.opcode = opcode;
    }

    public int opcode() {
        return opcode;
    }

}
