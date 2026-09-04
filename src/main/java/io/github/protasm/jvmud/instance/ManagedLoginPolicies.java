package io.github.protasm.jvmud.instance;

/** Registry of optional host-managed login adapters; ordinary mudlibs need no entry here. */
final class ManagedLoginPolicies {
    private ManagedLoginPolicies() {}

    /** Creates the explicitly named policy, or returns {@code null} when none was selected. */
    static ManagedLoginSession create(
            String policy, MudInstance mud, String sessionId, String remoteAddress) {
        if (policy == null) {
            return null;
        }
        return switch (policy) {
            case "filesystem_accounts" -> new FilesystemAccountLoginSession(mud, sessionId, remoteAddress);
            default -> throw new IllegalStateException("Unknown host session policy: " + policy);
        };
    }
}
