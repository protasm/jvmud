package io.github.protasm.jvmud.language.compiler.ir;

public sealed interface IRTerminator extends IRStatement permits IRConditionalJump, IRJump, IRReturn {}
