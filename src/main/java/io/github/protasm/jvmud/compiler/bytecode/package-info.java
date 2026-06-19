/**
 * Bytecode generation for LPC programs.
 *
 * <p>Translates typed IR into JVM bytecode using ASM, wiring LPC object methods, field access,
 * control flow, efun invocations, runtime helper calls, and boxing or unboxing according to the
 * already-lowered runtime types.</p>
 *
 * <p>Relies on earlier stages to supply a coherent semantic model and typed IR. Unexpected shapes
 * raise {@link io.github.protasm.jvmud.compiler.bytecode.BytecodeCompileException} rather than being
 * silently corrected in the backend.</p>
 *
 * <p>This package emits class bytes only. Host-facing class loading and object execution belong to
 * {@code io.github.protasm.jvmud.compiler.exec}.</p>
 */
package io.github.protasm.jvmud.compiler.bytecode;
