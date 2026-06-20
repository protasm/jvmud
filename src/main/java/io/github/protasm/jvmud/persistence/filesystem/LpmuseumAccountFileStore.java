package io.github.protasm.jvmud.persistence.filesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Filesystem persistence for LPMuseum account records. */
public final class LpmuseumAccountFileStore {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /** Loads an account record from the mudlib's account directory. */
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

    /** Saves an account record to the mudlib's account directory. */
    public void save(Path mudlibRoot, Account account) {
        Path path = accountPath(mudlibRoot, account.accountId());
        ObjectNode root = JSON.createObjectNode();
        ObjectNode fields = JSON.createObjectNode();
        root.put("format", "jvmud.lpc-object-state");
        root.put("version", 1);
        root.set("fields", fields);
        putString(fields, "lpmuseum.account.account_id", account.accountId());
        putString(fields, "lpmuseum.account.password_hash", account.passwordHash());
        putString(fields, "lpmuseum.account.email", account.email());
        putString(fields, "lpmuseum.account.gender", account.gender());
        putString(fields, "lpmuseum.account.persona_name", account.personaName().toLowerCase());
        putInt(fields, "lpmuseum.account.account_created", 1);
        try {
            Files.createDirectories(path.getParent());
            JSON.writeValue(path.toFile(), root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save LPMuseum account " + account.accountId(), e);
        }
    }

    private Path accountPath(Path mudlibRoot, String accountId) {
        return mudlibRoot.resolve("accounts").resolve(accountId + ".o").normalize();
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

    /** Durable LPMuseum account data independent of any live player session. */
    public record Account(
            String accountId,
            String personaName,
            String gender,
            String email,
            String passwordHash) {}
}
