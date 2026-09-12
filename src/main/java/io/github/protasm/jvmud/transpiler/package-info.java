/**
 * Opt-in translation of legacy LPC into explicitly typed JVMud compiler input.
 * Declaration translations operate on tokens; implicit self calls require name resolution
 * and translate unknown bare calls into required dynamic invocations. Translations operate
 * in memory and preserve source locations. Ordinary declared-call checks remain unchanged.
 * Mudlib manifests select transformations separately from compiler language features.
 */
package io.github.protasm.jvmud.transpiler;
