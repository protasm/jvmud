package io.github.protasm.jvmud.language.compiler.ir;

public record IRReturn(int line, IRExpression returnValue) implements IRTerminator {}
