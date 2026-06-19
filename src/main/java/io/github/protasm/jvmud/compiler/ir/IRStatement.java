package io.github.protasm.jvmud.compiler.ir;

public sealed interface IRStatement extends IRNode permits IRExpressionStatement, IRTerminator {}
