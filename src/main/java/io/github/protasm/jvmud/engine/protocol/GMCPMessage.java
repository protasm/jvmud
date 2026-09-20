package io.github.protasm.jvmud.engine.protocol;

/** A decoded Generic Mud Communication Protocol message. */
public record GMCPMessage(String packageName, Object payload, boolean hasPayload) {}
