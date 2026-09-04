package io.github.protasm.jvmud.persistence.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemAccountStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsNeutralAccountFields() throws Exception {
        FilesystemAccountStore store = new FilesystemAccountStore();
        FilesystemAccountStore.Account account = new FilesystemAccountStore.Account(
                "reader", "Museum Reader", "neutral", "reader@example.test", "hash");

        store.save(tempDir, account);

        assertEquals(account, store.load(tempDir, "reader").orElseThrow());
        String saved = Files.readString(tempDir.resolve("accounts/reader.o"));
        assertTrue(saved.contains("\"account.account_id\""), saved);
        assertTrue(saved.contains("\"account.persona_name\""), saved);
    }

    @Test
    void readsPreviouslyNamespacedAccountFields() throws Exception {
        Files.createDirectories(tempDir.resolve("accounts"));
        Files.writeString(tempDir.resolve("accounts/reader.o"), """
                {
                  "format" : "jvmud.lpc-object-state",
                  "version" : 1,
                  "fields" : {
                    "lpmuseum.account.account_id" : { "type" : "string", "value" : "reader" },
                    "lpmuseum.account.persona_name" : { "type" : "string", "value" : "archivist" },
                    "lpmuseum.account.gender" : { "type" : "string", "value" : "none" },
                    "lpmuseum.account.email" : { "type" : "string", "value" : "" },
                    "lpmuseum.account.password_hash" : { "type" : "string", "value" : "old-hash" }
                  }
                }
                """);

        FilesystemAccountStore.Account account =
                new FilesystemAccountStore().load(tempDir, "reader").orElseThrow();

        assertEquals("reader", account.accountId());
        assertEquals("archivist", account.personaName());
        assertEquals("old-hash", account.passwordHash());
    }

    @Test
    void rejectsAccountPathsOutsideTheAccountDirectory() {
        FilesystemAccountStore store = new FilesystemAccountStore();

        assertThrows(IllegalArgumentException.class, () -> store.load(tempDir, "../outside"));
        assertThrows(
                IllegalArgumentException.class,
                () -> store.save(
                        tempDir,
                        new FilesystemAccountStore.Account("../outside", "name", "none", "", "hash")));
    }
}
