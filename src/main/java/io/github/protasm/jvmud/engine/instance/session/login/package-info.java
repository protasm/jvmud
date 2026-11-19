/**
 * Optional host-managed login policies selected by a mudlib manifest.
 *
 * <p>A flow owns its prompts and credential checks, returning resolved profile data or a disconnect
 * request. It does not create a Persona, replace a transport handle or maintain session indexes.
 * The instance consumes that outcome through its authoritative session owner.</p>
 */
package io.github.protasm.jvmud.engine.instance.session.login;
