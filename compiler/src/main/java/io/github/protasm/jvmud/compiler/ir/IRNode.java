package io.github.protasm.jvmud.compiler.ir;

public sealed interface IRNode permits IRExpression, IRStatement {
    int line();
}
