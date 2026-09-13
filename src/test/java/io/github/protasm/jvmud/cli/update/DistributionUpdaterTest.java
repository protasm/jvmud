package io.github.protasm.jvmud.cli.update;

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
