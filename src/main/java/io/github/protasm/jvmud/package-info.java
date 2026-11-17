/**
 * JVMud's LPC language facilities, text-world engine, communication, and durable storage.
 *
 * <p>The four package families are the source reader's map of the product. {@code language}
 * describes and executes LPC without hosting a world. {@code engine} operates the application,
 * defines world invariants, and assembles one mudlib in each worker. {@code communication} owns
 * connections, messages, administration, and presentation. {@code storage} owns durable formats
 * and adapters. Mudlib source supplies authored fiction and game policy outside this Java tree.</p>
 *
 * <p>The engine's running instances connect these responsibilities through language contracts.
 * Start with the package documentation, then follow the named entry points into the source.
 * Readability, deliberate ownership, and documented invariants are product qualities alongside
 * correct behavior.</p>
 */
package io.github.protasm.jvmud;
