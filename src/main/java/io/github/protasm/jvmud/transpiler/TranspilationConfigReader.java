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

    /** Reads the field rules from a validated configuration; use readConfig for both families. */
    public static List<FieldTypeOverride> read(Path config, Path mudlibRoot) throws IOException {
        return readConfig(config, mudlibRoot).fields();
    }

    /** Both override families, immutable and validated at manifest load. */
    public record Overrides(List<FieldTypeOverride> fields, List<LocalTypeOverride> locals) {
        public Overrides {
            fields = List.copyOf(fields);
            locals = List.copyOf(locals);
        }
    }

    /** Reads field and local overrides together; either family may be omitted. */
    public static Overrides readConfig(Path config, Path mudlibRoot) throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        JsonNode root = mapper.readTree(Files.readString(config));
        if (root == null || !root.isObject() || root.isEmpty())
            throw new IllegalArgumentException("Expected override object: " + config);
        var keys = root.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!Set.of("field_type_overrides", "local_type_overrides").contains(key))
                throw new IllegalArgumentException("Unknown override operation: " + key);
        }
        JsonNode rules = root.has("field_type_overrides") ? root.get("field_type_overrides") : mapper.createArrayNode();
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
        JsonNode localRules = root.has("local_type_overrides") ? root.get("local_type_overrides") : mapper.createArrayNode();
        if (!localRules.isArray()) throw new IllegalArgumentException("local_type_overrides must be an array: " + config);
        List<LocalTypeOverride> locals = new ArrayList<>();
        Set<String> localTargets = new HashSet<>();
        for (JsonNode rule : localRules) {
            requireKeys(rule, Set.of("file", "method", "local", "expected_type", "replacement_type"), config.toString());
            var override = new LocalTypeOverride(text(rule, "file"), text(rule, "method"), text(rule, "local"),
                    text(rule, "expected_type"), text(rule, "replacement_type"));
            String target = override.file() + ":" + override.method() + ":" + override.local();
            if (!localTargets.add(target)) throw new IllegalArgumentException("Duplicate local override: " + target);
            if (!Files.isRegularFile(mudlibRoot.resolve(override.file())))
                throw new IllegalArgumentException("Override source file does not exist: " + override.file());
            locals.add(override);
        }
        return new Overrides(List.copyOf(result), List.copyOf(locals));
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
