package io.github.protasm.jvmud.language.compiler.ir;

public sealed interface IRStatement extends IRNode permits IRExpressionStatement, IRTerminator {}
