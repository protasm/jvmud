package io.github.protasm.jvmud.compiler.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class JsonValueCodecTest {
    @Test
    void parsesOrdinaryJsonAsLpcValues() {
        Object parsed = JsonValueCodec.parse("""
                {
                  "name": "ridge",
                  "priority": 812,
                  "ratio": 1.5,
                  "walkable": true,
                  "hidden": false,
                  "unused": null,
                  "tags": ["alpine", 7]
                }
                """);

        assertEquals(Map.of(
                "name", "ridge",
                "priority", 812,
                "ratio", 1.5d,
                "walkable", 1,
                "hidden", 0,
                "unused", 0,
                "tags", List.of("alpine", 7)), parsed);
    }

    @Test
    void streamsOnlyRequestedJsonPointerArraySlice() {
        String document = """
                {
                  "metadata": {"ignored": [1, 2, 3]},
                  "items": [
                    {"item_id": "i0000", "enabled": true},
                    {"item_id": "i0001", "enabled": false},
                    {"item_id": "i0002", "enabled": true},
                    {"item_id": "i0003", "enabled": true}
                  ]
                }
                """;

        assertEquals(List.of(
                Map.of("item_id", "i0001", "enabled", 0),
                Map.of("item_id", "i0002", "enabled", 1)),
                JsonValueCodec.readArraySlice(input(document), "/items", 1, 2));
    }

    @Test
    void supportsEscapedAndArrayIndexJsonPointerSegments() {
        String document = """
                {"groups/by~name": [{"values": [10, 11, 12]}]}
                """;

        assertEquals(List.of(11, 12),
                JsonValueCodec.readArraySlice(input(document), "/groups~1by~0name/0/values", 1, 9));
    }

    @Test
    void rejectsInvalidJsonAndNonArrayTargets() {
        assertThrows(IllegalArgumentException.class, () -> JsonValueCodec.parse("{"));
        assertThrows(IllegalArgumentException.class, () -> JsonValueCodec.parse(""));
        assertThrows(IllegalArgumentException.class,
                () -> JsonValueCodec.readArraySlice(input("{\"items\": {}}"), "/items", 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> JsonValueCodec.readArraySlice(input("{\"items\": [1"), "/items", 0, 2));
        assertThrows(IllegalArgumentException.class,
                () -> JsonValueCodec.readArraySlice(input("[]"), "items", 0, 1));
    }

    @Test
    void formatsMappingsAndArraysAsOrdinaryJson() {
        LinkedHashMap<String, Object> value = new LinkedHashMap<>();
        value.put("name", "ridge");
        value.put("values", List.of(1, 2));
        assertEquals("{\"name\":\"ridge\",\"values\":[1,2]}",
                JsonValueCodec.format(value));
        assertThrows(IllegalArgumentException.class, () -> JsonValueCodec.format(Map.of(1, "not a JSON key")));
    }

    private static ByteArrayInputStream input(String document) {
        return new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));
    }
}
