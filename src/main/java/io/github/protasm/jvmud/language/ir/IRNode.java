package io.github.protasm.jvmud.language.ir;

public sealed interface IRNode permits IRExpression, IRStatement {
    int line();
}
