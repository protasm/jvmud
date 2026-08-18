package io.github.protasm.jvmud.persistence.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class RuntimeDatabaseServiceTest {
    @Test
    void convertsJdbcBinaryTextToAnLpcString() {
        String serialized = "{\"type\":\"mapping\",\"name\":\"Lumiere\"}";

        assertEquals(
                serialized,
                RuntimeDatabaseService.toLpcValue(serialized.getBytes(StandardCharsets.UTF_8)));
    }
}
