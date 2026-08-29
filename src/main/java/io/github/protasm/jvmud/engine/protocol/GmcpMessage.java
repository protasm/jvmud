package io.github.protasm.jvmud.engine.protocol;

/** A decoded Generic Mud Communication Protocol message. */
public record GmcpMessage(String packageName, Object payload, boolean hasPayload) {}
