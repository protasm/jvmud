/**
 * Opt-in translation of legacy LPC into explicitly typed JVMud compiler input.
 * Field and untyped-method translations operate on tokens; local and checked varargs
 * adaptations operate on parsed declarations. Implicit self calls require name resolution
 * and translate unknown bare calls into required dynamic invocations. Translations operate
 * in memory and preserve source locations. Ordinary declared-call checks remain unchanged unless a method is explicitly selected
 * for the existing varargs calling convention.
 * Mudlib manifests select transformations separately from compiler language features.
 */
package io.github.protasm.jvmud.compiler.transpiler;
