package io.github.protasm.jvmud.language.compiler.ir;

public sealed interface IRNode permits IRExpression, IRStatement {
    int line();
}
