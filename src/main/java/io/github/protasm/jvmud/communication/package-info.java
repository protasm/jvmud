/**
 * Human-facing communication, command interpretation, and text presentation.
 *
 * <p>{@code transport} owns connection protocols and listeners; {@code admin} interprets
 * administrative commands; {@code console} is the terminal client. {@code protocol} models
 * structured messages such as GMCP, while {@code output} formats outgoing text.
 * Mudlibs retain ownership of player command interpretation.</p>
 */
package io.github.protasm.jvmud.communication;
