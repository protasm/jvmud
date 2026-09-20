package io.github.protasm.jvmud.persistence.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.util.*;

/** Engine-owned administrator identities and grants. Only hashes of random access tokens are stored. */
public final class AdminRegistry {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Path file;
    private Map<String, Entry> entries;

    /** Loads a private registry; an empty registry is bootstrapped through the local console. */
    public AdminRegistry(Path directory) throws IOException {
        privateDirectory(directory);
        file = directory.resolve("administrators.json");
        entries = Files.exists(file)
                ? new TreeMap<>(JSON.readValue(Files.readAllBytes(file), Store.class).administrators()) : new TreeMap<>();
    }

    /** Creates an identity and returns its one-time-displayed 256-bit token. */
    public synchronized String create(String name) throws IOException {
        validateName(name);
        if (entries.containsKey(name)) throw new IllegalArgumentException("Administrator already exists.");
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        update(name, new Entry(hash(token), Set.of()));
        return token;
    }

    /** Rotates an identity's token, invalidating existing sessions on their next authorization check. */
    public synchronized String rotate(String name) throws IOException {
        Entry previous = require(name);
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        update(name, new Entry(hash(token), previous.grants()));
        return token;
    }

    /** Grants or revokes engine or {@code mudlib:<id>} authority; player permissions are unrelated. */
    public synchronized void grant(String name, String scope, boolean allowed) throws IOException {
        if (!scope.equals("engine") && !scope.matches("mudlib:[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("Scope must be engine or mudlib:<id>.");
        Entry previous = require(name);
        Set<String> grants = new TreeSet<>(previous.grants());
        if (allowed) grants.add(scope); else grants.remove(scope);
        update(name, new Entry(previous.digest(), Set.copyOf(grants)));
    }

    /** Deletes an identity and all its grants. */
    public synchronized void remove(String name) throws IOException { require(name); update(name, null); }

    /** Authenticates a token without revealing whether the identity exists. */
    public synchronized boolean authenticate(String name, String token) {
        Entry entry = entries.get(name);
        return MessageDigest.isEqual(hash(token).getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                (entry == null ? "0".repeat(64) : entry.digest()).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    /** Rechecks both credential validity and scope so revocation affects existing connections. */
    public synchronized boolean allows(String name, String token, String scope) {
        if (!authenticate(name, token)) return false;
        Set<String> grants = entries.get(name).grants();
        return grants.contains("engine") || grants.contains(scope);
    }

    /** Lists identities and grants without disclosing credential hashes. */
    public synchronized String describe() {
        StringBuilder text = new StringBuilder();
        entries.forEach((name, entry) -> text.append(name).append(" ").append(new TreeSet<>(entry.grants())).append('\n'));
        return text.toString();
    }

    private Entry require(String name) {
        Entry entry = entries.get(name);
        if (entry == null) throw new IllegalArgumentException("Unknown administrator.");
        return entry;
    }

    private void update(String name, Entry entry) throws IOException {
        Map<String, Entry> next = new TreeMap<>(entries);
        if (entry == null) next.remove(name); else next.put(name, entry);
        Path temporary = Files.createTempFile(file.getParent(), ".administrators-", ".tmp");
        try {
            Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-------"));
            JSON.writeValue(temporary.toFile(), new Store(next));
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            entries = next;
        } finally { Files.deleteIfExists(temporary); }
    }

    /** Creates an owner-only directory and rejects symlinks and directories owned by another account. */
    public static void privateDirectory(Path path) throws IOException {
        Files.createDirectories(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
        if (Files.isSymbolicLink(path) || !Files.getOwner(path).equals(
                path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(System.getProperty("user.name"))))
            throw new IOException("Engine directory must be owned by the current OS account: " + path);
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwx------"));
    }

    private static void validateName(String value) {
        if (!value.matches("[A-Za-z0-9_-]{1,64}")) throw new IllegalArgumentException("Use 1–64 letters, digits, underscores or hyphens.");
    }

    private static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    /** Serialized registry entry; token material never appears here. */
    public record Entry(String digest, Set<String> grants) {}
    /** Version-one registry document. */
    public record Store(Map<String, Entry> administrators) {}
}
