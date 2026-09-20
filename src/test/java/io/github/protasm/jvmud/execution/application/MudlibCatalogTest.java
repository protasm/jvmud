package io.github.protasm.jvmud.execution.application;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MudlibCatalogTest {
    @TempDir Path temporary;

    @Test void listsNamesAndRejectsPathsAndEscapingSymlinks() throws IOException {
        Path root = temporary.resolve("mudlibs");
        Path manifest = root.resolve("sample/jvmud/sample.config");
        Files.createDirectories(manifest.getParent());
        Files.writeString(manifest, "game_id = sample\n");
        MudlibCatalog catalog = new MudlibCatalog(root);
        assertEquals(List.of("sample"), catalog.names());
        assertEquals(root.resolve("sample"), catalog.resolve("sample").root());
        for (String name : List.of("../sample", "sample/../sample", "/sample", "sample\\other", ".", "..", ""))
            assertThrows(IllegalArgumentException.class, () -> catalog.resolve(name));
        assertThrows(IOException.class, () -> catalog.resolve("missing"));
        Path outside = temporary.resolve("outside");
        Files.createDirectories(outside.resolve("jvmud"));
        Files.writeString(outside.resolve("jvmud/escape.config"), "game_id = escape\n");
        Files.createSymbolicLink(root.resolve("escape"), outside);
        assertThrows(IOException.class, () -> catalog.resolve("escape"));
        var spec = catalog.resolve("sample");
        Files.delete(manifest);
        Files.createSymbolicLink(manifest, outside.resolve("jvmud/escape.config"));
        assertThrows(IOException.class, () -> catalog.validate(spec));
        assertTrue(catalog.names().isEmpty());
    }

    @Test void rejectsEscapingIntermediateDirectoryAndAllowsContainedSymlinks() throws IOException {
        Path root = temporary.resolve("mudlibs");
        Path sample = root.resolve("sample");
        Path outside = temporary.resolve("outside");
        Files.createDirectories(sample);
        Files.createDirectories(outside);
        Files.writeString(outside.resolve("sample.config"), "game_id = sample\n");
        Files.createSymbolicLink(sample.resolve("jvmud"), outside);
        MudlibCatalog catalog = new MudlibCatalog(root);
        assertThrows(IOException.class, () -> catalog.resolve("sample"));
        Files.delete(sample.resolve("jvmud"));
        Files.createDirectory(sample.resolve("config"));
        Files.writeString(sample.resolve("config/sample.config"), "game_id = sample\n");
        Files.createSymbolicLink(sample.resolve("jvmud"), sample.resolve("config"));
        assertEquals(List.of("sample"), catalog.names());
    }

    @Test void emptyEngineDoesNotRequireAnInstalledDirectory() throws IOException {
        MudlibCatalog catalog = new MudlibCatalog(temporary.resolve("missing"));
        assertTrue(catalog.names().isEmpty());
        assertThrows(IOException.class, () -> catalog.resolve("sample"));
    }
}
