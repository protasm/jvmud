package io.github.protasm.jvmud.language.ir;

public sealed interface IRStatement extends IRNode permits IRExpressionStatement, IRTerminator {}
