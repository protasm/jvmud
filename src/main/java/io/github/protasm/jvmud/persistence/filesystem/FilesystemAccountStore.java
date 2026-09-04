package io.github.protasm.jvmud.persistence.filesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Filesystem persistence for an explicitly selected host-managed account policy. */
public final class FilesystemAccountStore {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /** Loads an account record from the selected mudlib's account directory. */
    public Optional<Account> load(Path mudlibRoot, String accountId) {
        Path path = accountPath(mudlibRoot, accountId);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            JsonNode fields = JSON.readTree(path.toFile()).path("fields");
            String personaName = field(fields, "persona_name", "visitor");
            return Optional.of(new Account(
                    field(fields, "account_id", accountId),
                    personaName.isBlank() ? "visitor" : personaName,
                    field(fields, "gender", "none"),
                    field(fields, "email", ""),
                    field(fields, "password_hash", "")));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /** Saves an account record to the selected mudlib's account directory. */
    public void save(Path mudlibRoot, Account account) {
        Path path = accountPath(mudlibRoot, account.accountId());
        ObjectNode root = JSON.createObjectNode();
        ObjectNode fields = JSON.createObjectNode();
        root.put("format", "jvmud.lpc-object-state");
        root.put("version", 1);
        root.set("fields", fields);
        putString(fields, "account.account_id", account.accountId());
        putString(fields, "account.password_hash", account.passwordHash());
        putString(fields, "account.email", account.email());
        putString(fields, "account.gender", account.gender());
        putString(fields, "account.persona_name", account.personaName());
        putInt(fields, "account.account_created", 1);
        try {
            Files.createDirectories(path.getParent());
            JSON.writeValue(path.toFile(), root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save account " + account.accountId(), e);
        }
    }

    private Path accountPath(Path mudlibRoot, String accountId) {
        Path accountRoot = mudlibRoot.toAbsolutePath().normalize().resolve("accounts");
        Path path = accountRoot.resolve(accountId + ".o").normalize();
        if (!path.getParent().equals(accountRoot)) {
            throw new IllegalArgumentException("accountId must name one account inside the account directory.");
        }
        return path;
    }

    private String field(JsonNode fields, String suffix, String fallback) {
        if (!fields.isObject()) {
            return fallback;
        }
        var names = fields.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (name.endsWith("." + suffix)) {
                JsonNode value = fields.path(name).path("value");
                if (value.isTextual()) {
                    return value.asText();
                }
                if (value.isInt()) {
                    return Integer.toString(value.asInt());
                }
            }
        }
        return fallback;
    }

    private void putString(ObjectNode fields, String name, String value) {
        ObjectNode field = JSON.createObjectNode();
        field.put("type", "string");
        field.put("value", value != null ? value : "");
        fields.set(name, field);
    }

    private void putInt(ObjectNode fields, String name, int value) {
        ObjectNode field = JSON.createObjectNode();
        field.put("type", "int");
        field.put("value", value);
        fields.set(name, field);
    }

    /** Durable account data supplied to the mudlib after host authentication succeeds. */
    public record Account(
            String accountId,
            String personaName,
            String gender,
            String email,
            String passwordHash) {}
}
