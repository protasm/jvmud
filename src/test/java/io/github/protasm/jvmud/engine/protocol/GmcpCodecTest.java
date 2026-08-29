package io.github.protasm.jvmud.engine.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class GmcpCodecTest {
    @Test
    void roundTripsJsonPayloadsUsedByMudlibs() {
        String encoded = GmcpCodec.encode(
                "Room.Info",
                Map.of("num", 42, "name", "Lantern Road", "exits", Map.of("west", 41)),
                true);
        GmcpMessage decoded = GmcpCodec.decode(encoded);

        assertEquals("Room.Info", decoded.packageName());
        assertTrue(decoded.hasPayload());
        assertEquals(42, ((Map<?, ?>) decoded.payload()).get("num"));
        assertEquals(41, ((Map<?, ?>) ((Map<?, ?>) decoded.payload()).get("exits")).get("west"));
    }

    @Test
    void supportsPayloadlessMessagesAndArrays() {
        GmcpMessage ping = GmcpCodec.decode(GmcpCodec.encode("Core.Ping", null, false));
        GmcpMessage supports = GmcpCodec.decode(
                GmcpCodec.encode("Core.Supports.Set", List.of("Room 1", "Char 1"), true));

        assertFalse(ping.hasPayload());
        assertEquals(List.of("Room 1", "Char 1"), supports.payload());
    }

    @Test
    void rejectsInvalidPackageNamesAndJson() {
        assertThrows(IllegalArgumentException.class, () -> GmcpCodec.encode("Room Info", Map.of(), true));
        assertThrows(IllegalArgumentException.class, () -> GmcpCodec.decode("Room.Info {broken"));
    }
}
