package io.github.protasm.jvmud.execution.model.world;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** An in-world observation, independent of any transport or mudlib vocabulary. */
public record PerceptionEvent(Object actor, Object source, Object location,
                              String kind, Object content, String text, boolean directed) {
    public PerceptionEvent {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(text, "text");
    }

    /** A fresh mapping for each LPC observer; absent references use LPC zero. */
    public Map<Object, Object> toMapping() {
        Map<Object, Object> result = new LinkedHashMap<>();
        result.put("actor", actor == null ? 0 : actor);
        result.put("source", source == null ? 0 : source);
        result.put("location", location == null ? 0 : location);
        result.put("kind", kind);
        result.put("content", content == null ? 0 : content);
        result.put("text", text);
        result.put("directed", directed ? 1 : 0);
        return result;
    }
}
