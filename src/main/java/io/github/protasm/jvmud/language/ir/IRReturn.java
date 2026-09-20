package io.github.protasm.jvmud.language.ir;

public record IRReturn(int line, IRExpression returnValue) implements IRTerminator {}
