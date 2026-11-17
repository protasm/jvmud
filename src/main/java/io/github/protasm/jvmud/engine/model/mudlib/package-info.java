/**
 * Engine-owned lifecycle events, capability grants, and opaque mudlib behavior projections.
 *
 * <p>The model names the events and roles a running mudlib participates in. Manifest parsing and
 * language policy belong to {@code engine.configuration}; executing mapped LPC methods belongs
 * to {@code engine.instance}. These contracts have no dependency on either implementation.</p>
 */
package io.github.protasm.jvmud.engine.model.mudlib;
