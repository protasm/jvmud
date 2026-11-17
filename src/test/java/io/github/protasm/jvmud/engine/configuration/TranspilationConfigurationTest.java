package io.github.protasm.jvmud.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks that manifest selection preserves strict defaults for source transformations. */
class TranspilationConfigurationTest {
    @TempDir Path temp;

    @Test void honorsManifestDefaultsAndRejectsInvalidSwitch() throws Exception {
        Path config = temp.resolve("bridge.config");
        Files.writeString(config, "transpilation.untyped_methods = true\n");
        assertTrue(MudlibConfigurationReader.read(temp, "bridge.config").transpileUntypedMethods());
        Files.writeString(config, "transpilation.untyped_methods = false\n");
        assertFalse(MudlibConfigurationReader.read(temp, "bridge.config").transpileUntypedMethods());
        Files.writeString(config, "");
        assertFalse(MudlibConfigurationReader.read(temp, "bridge.config").transpileUntypedMethods());
        Files.writeString(config, "transpilation.untyped_methods = perhaps\n");
        assertThrows(IllegalArgumentException.class, () -> MudlibConfigurationReader.read(temp, "bridge.config"));
    }

}
