package io.github.protasm.jvmud.transpiler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reads strict, bridge-owned JSON override declarations; unknown operations and keys are errors. */
public final class TranspilationConfigReader {
    private TranspilationConfigReader() {}

    /** Reads and validates rules once at manifest load; files are relative to the active mudlib root. */
    public static List<FieldTypeOverride> read(Path config, Path mudlibRoot) throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        JsonNode root = mapper.readTree(Files.readString(config));
        requireKeys(root, Set.of("field_type_overrides"), config.toString());
        JsonNode rules = root.get("field_type_overrides");
        if (!rules.isArray()) throw new IllegalArgumentException("field_type_overrides must be an array: " + config);
        List<FieldTypeOverride> result = new ArrayList<>();
        Set<String> targets = new HashSet<>();
        for (JsonNode rule : rules) {
            requireKeys(rule, Set.of("file", "field", "expected_type", "replacement_type"), config.toString());
            FieldTypeOverride override = new FieldTypeOverride(text(rule, "file"), text(rule, "field"),
                    text(rule, "expected_type"), text(rule, "replacement_type"));
            if (!targets.add(override.file() + ":" + override.field()))
                throw new IllegalArgumentException("Duplicate field override: " + override.file() + ":" + override.field());
            Path target = mudlibRoot.resolve(override.file());
            if (!Files.isRegularFile(target))
                throw new IllegalArgumentException("Override source file does not exist: " + target);
            result.add(override);
        }
        return List.copyOf(result);
    }

    private static void requireKeys(JsonNode node, Set<String> keys, String location) {
        if (node == null || !node.isObject()) throw new IllegalArgumentException("Expected JSON object in " + location);
        Set<String> actual = new HashSet<>();
        node.fieldNames().forEachRemaining(actual::add);
        if (!actual.equals(keys)) throw new IllegalArgumentException("Expected keys " + keys + " but found " + actual + " in " + location);
    }

    private static String text(JsonNode node, String key) {
        if (!node.get(key).isTextual()) throw new IllegalArgumentException(key + " must be a string");
        return node.get(key).textValue();
    }
}
