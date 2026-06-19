package io.github.protasm.jvmud.compiler.ir;

public record IRReturn(int line, IRExpression returnValue) implements IRTerminator {}
