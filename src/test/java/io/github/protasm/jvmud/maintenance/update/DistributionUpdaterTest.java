package io.github.protasm.jvmud.maintenance.update;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DistributionUpdaterTest {
    @TempDir Path temp;
    @Test void preservesLocalConfigurationAndRejectsConflictingAdapterChanges() throws Exception {
        Path old = temp.resolve("old"), next = temp.resolve("next");
        String config = "mudlibs/example/jvmud/example.config";
        Files.createDirectories(old.resolve(config).getParent()); Files.createDirectories(next.resolve(config).getParent());
        Files.writeString(old.resolve(config), "original"); Files.writeString(next.resolve(config), "original");
        String original = DistributionUpdater.digest(old.resolve(config));
        var oldIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", Map.of(config, original)));
        Files.writeString(old.resolve(config), "local port and password environment");
        assertTrue(DistributionUpdater.adapterChanges(old, next, oldIndex, oldIndex).isEmpty());
        Files.writeString(next.resolve(config), "new vendor settings");
        var newIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", Map.of(config, DistributionUpdater.digest(next.resolve(config)))));
        assertThrows(java.io.IOException.class, () -> DistributionUpdater.adapterChanges(old, next, oldIndex, newIndex));
        Files.writeString(old.resolve(config), "original");
        assertEquals(List.of(config), DistributionUpdater.adapterChanges(old, next, oldIndex, newIndex));
    }
    @Test void commentOnlyConfigurationEditsAllowNewReleaseSettings() throws Exception {
        String name = "mudlibs/example/jvmud/example.config";
        Path old = temp.resolve("old"), next = temp.resolve("next");
        Files.createDirectories(old.resolve(name).getParent());
        Files.createDirectories(next.resolve(name).getParent());
        String baseline = "# Shipped comment\nlifecycle.perception_delivery = deliver_perception\nplayer_prompt = \"> \"\n";
        Files.writeString(old.resolve(name), baseline);
        String hash = DistributionUpdater.digest(old.resolve(name));
        var oldIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", Map.of(name, hash), "configBaselines", Map.of(name, baseline)));
        Files.writeString(old.resolve(name), "  lifecycle.perception_delivery:deliver_perception # local comment\r\nplayer_prompt=\"> \"\r\n");
        Files.writeString(next.resolve(name), baseline + "compiler.dynamic_types = true\n");
        var newIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", Map.of(name, DistributionUpdater.digest(next.resolve(name)))));
        assertEquals(List.of(name), DistributionUpdater.adapterChanges(old, next, oldIndex, newIndex));
        // Whitespace inside a quoted prompt changes actual behavior.
        Files.writeString(old.resolve(name), baseline.replace("> ", ">"));
        assertThrows(java.io.IOException.class, () -> DistributionUpdater.adapterChanges(old, next, oldIndex, newIndex));
    }

    @Test void backsUpEditedDocumentationAndReportsEveryRealConflict() throws Exception {
        Path old = temp.resolve("old"), next = temp.resolve("next");
        Map<String, String> oldHashes = new HashMap<>(), newHashes = new HashMap<>();
        for (String file : List.of("README.md", "example.config", "mfuns.c")) {
            String name = "mudlibs/example/jvmud/" + file;
            Files.createDirectories(old.resolve(name).getParent());
            Files.createDirectories(next.resolve(name).getParent());
            Files.writeString(old.resolve(name), "original");
            oldHashes.put(name, DistributionUpdater.digest(old.resolve(name)));
            Files.writeString(old.resolve(name), "local changes");
            Files.writeString(next.resolve(name), "new release");
            newHashes.put(name, DistributionUpdater.digest(next.resolve(name)));
        }
        var oldIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", oldHashes));
        var newIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", newHashes));
        var failure = assertThrows(java.io.IOException.class, () -> DistributionUpdater.adapterChanges(old, next, oldIndex, newIndex));
        assertTrue(failure.getMessage().contains("example.config"));
        assertTrue(failure.getMessage().contains("mfuns.c"));
        assertFalse(failure.getMessage().contains("README.md"));
        for (String file : List.of("example.config", "mfuns.c"))
            Files.writeString(old.resolve("mudlibs/example/jvmud/" + file), "original");
        assertEquals(3, DistributionUpdater.adapterChanges(old, next, oldIndex, newIndex).size());
        assertEquals("local changes", Files.readString(old.resolve("mudlibs/example/jvmud/README.md")));
    }

    @Test void refusesUnverifiedBaselineAndPreservesRepeatedSettingOrder() throws Exception {
        String name = "mudlibs/example/jvmud/example.config";
        Path old = temp.resolve("old"), next = temp.resolve("next");
        Files.createDirectories(old.resolve(name).getParent());
        Files.createDirectories(next.resolve(name).getParent());
        Files.writeString(old.resolve(name), "game_id = local\n");
        Files.writeString(next.resolve(name), "game_id = vendor\n");
        var oldIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", Map.of(name, "invalid"),
                "configBaselines", Map.of(name, Files.readString(old.resolve(name)))));
        var newIndex = InstallationServers.JSON.valueToTree(Map.of("adapters", Map.of(name, DistributionUpdater.digest(next.resolve(name)))));
        assertThrows(java.io.IOException.class, () -> DistributionUpdater.adapterChanges(old, next, oldIndex, newIndex));
        assertNotEquals(DistributionUpdater.configurationLines("a=1\na=2"), DistributionUpdater.configurationLines("a=2\na=1"));
        assertNull(DistributionUpdater.configurationLines("invalid line"));
    }

    @Test void backupPreservesFilesAndSymlinksWithoutFollowingThem() throws Exception {
        Path source = temp.resolve("installation"), backup = temp.resolve("backup/version-timestamp");
        Files.createDirectories(source.resolve("mudlibs/example/players"));
        Files.writeString(source.resolve("mudlibs/example/players/alice.o"), "saved player");
        Files.createSymbolicLink(source.resolve("link"), Path.of("mudlibs/example/players/alice.o"));
        DistributionUpdater.copyTree(source, backup);
        DistributionUpdater.verifyCopy(source, backup);
        assertEquals("saved player", Files.readString(backup.resolve("link")));
        assertTrue(Files.isSymbolicLink(backup.resolve("link")));
        Files.writeString(backup.resolve("mudlibs/example/players/alice.o"), "broken");
        assertThrows(java.io.IOException.class, () -> DistributionUpdater.verifyCopy(source, backup));
    }
    @Test void rejectsAdapterPathsOutsideTheExplicitUpdateBoundary() throws Exception {
        var bad = InstallationServers.JSON.valueToTree(Map.of("adapters", Map.of("mudlibs/example/players/alice.o", "abc")));
        assertThrows(java.io.IOException.class, () -> DistributionUpdater.adapterChanges(temp, temp, bad, bad));
        assertFalse(InstallationServers.isSameProcess(ProcessHandle.current().pid(), "wrong-start-time"));
    }
}
