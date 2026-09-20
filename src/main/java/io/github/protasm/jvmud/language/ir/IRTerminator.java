package io.github.protasm.jvmud.language.ir;

public sealed interface IRTerminator extends IRStatement permits IRConditionalJump, IRJump, IRReturn {}
