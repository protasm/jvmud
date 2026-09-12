package io.github.protasm.jvmud.cli;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class AdminClientTest {
    @Test
    void choosesTheRequestedServerAndCredential() {
        var options = AdminClient.parseOptions(new String[] {"--port", "4100", "--token-file", "private key"});
        assertEquals(4100, options.port());
        assertEquals(Path.of("private key"), options.tokenFile());
        assertTrue(AdminClient.parseOptions(new String[] {"--port", "4200"}).tokenFile().endsWith("4200.token"));
    }

    @Test
    void rejectsManifestArgumentsMissingPortsAndInvalidPorts() {
        for (String[] args : new String[][] {
                {}, {"world.config"}, {"--port"}, {"--port", "0"}, {"--port", "65536"},
                {"--port", "abc"}, {"--port", "4100", "--token-file"}, {"--unknown", "4100"}
        }) {
            assertThrows(IllegalArgumentException.class, () -> AdminClient.parseOptions(args));
        }
    }
}
