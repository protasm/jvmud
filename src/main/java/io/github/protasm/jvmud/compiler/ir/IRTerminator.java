package io.github.protasm.jvmud.compiler.ir;

public sealed interface IRTerminator extends IRStatement permits IRConditionalJump, IRJump, IRReturn {}
