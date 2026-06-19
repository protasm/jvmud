package io.github.protasm.jvmud.engine;

/** A location that can immediately contain entities in a JVMud world. */
public sealed interface Location permits Entity, Place {
    String id();

    String displayName();
}
