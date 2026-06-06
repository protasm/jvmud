/**
 * Bytecode generation for LPC programs.
 *
 * <p>Translates parsed AST structures into JVM bytecode using ASM, wiring engine function invocations, field
 * access, control flow, and basic boxing/unboxing according to inferred types.</p>
 *
 * <p>Relies on prior parsing and type inference to supply consistent symbols and method descriptors;
 * unexpected shapes raise {@link io.github.protasm.jvmud.compiler.bytecode.BytecodeCompileException} rather than being
 * silently corrected.</p>
 *
 * <p>This package assumes an external runtime provides any referenced engine functions and superclass; it does
 * not manage class loading or execution.</p>
 */
package io.github.protasm.jvmud.compiler.bytecode;
