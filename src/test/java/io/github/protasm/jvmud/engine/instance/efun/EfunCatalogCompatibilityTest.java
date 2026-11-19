package io.github.protasm.jvmud.engine.instance.efun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.engine.model.mudlib.EngineCapability;
import io.github.protasm.jvmud.language.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.efun.Efun;
import io.github.protasm.jvmud.language.efun.EfunRegistry;
import io.github.protasm.jvmud.language.efun.EfunSignature;
import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/** Preserves the authored LPC contract while callable implementations move to their proper owners. */
final class EfunCatalogCompatibilityTest {
    @Test
    void everyCapabilitySelectionPreservesThePreviousCatalogAndOverloadResolution() throws Exception {
        List<Declaration> baseline = baseline();
        EngineCapability[] capabilities = EngineCapability.values();
        for (int mask = 0; mask < 1 << capabilities.length; mask++) {
            Set<EngineCapability> selected = EnumSet.noneOf(EngineCapability.class);
            for (int bit = 0; bit < capabilities.length; bit++) {
                if ((mask & 1 << bit) != 0) selected.add(capabilities[bit]);
            }
            MudlibRuntimeContext context = new MudlibRuntimeContext(null);
            EngineEfuns.registerCore(context, selected);
            EfunRegistry registry = context.efunRegistry();
            List<Declaration> expected = baseline.stream()
                    .filter(declaration -> declaration.capability().map(selected::contains).orElse(true))
                    .toList();
            List<String> actual = registry.names().stream()
                    .flatMap(name -> registry.signatures(name).stream())
                    .map(EfunCatalogCompatibilityTest::signatureKey)
                    .sorted().toList();

            assertEquals(expected.stream().map(Declaration::signature).sorted().toList(), actual,
                    "The complete callable catalog must remain stable for " + selected);
            assertCompatibleLookups(registry, expected);
        }
    }

    @Test
    void unrestrictedRegistrationStillIncludesTheCompleteCatalog() throws Exception {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        EngineEfuns.registerCore(context);
        List<String> actual = context.efunRegistry().names().stream()
                .flatMap(name -> context.efunRegistry().signatures(name).stream())
                .map(EfunCatalogCompatibilityTest::signatureKey).sorted().toList();

        assertEquals(baseline().stream().map(Declaration::signature).sorted().toList(), actual);
    }

    @Test
    void hostRegistrationPreservesCallbacksAndLexicalMethodComparators() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        EngineEfuns.registerCore(context);
        RuntimeCallable multiply = (active, arguments) -> {
            assertSame(context, active);
            return ((Number) arguments[0]).intValue() * ((Number) arguments[1]).intValue();
        };
        RuntimeCallable above = (active, arguments) -> {
            assertSame(context, active);
            return ((Number) arguments[0]).intValue() > ((Number) arguments[1]).intValue();
        };
        RuntimeCallable replace = (active, arguments) -> {
            assertSame(context, active);
            return "$" + arguments[0];
        };

        assertEquals(List.of(6, 12), context.invokeEfun("jvmud_map", 3,
                new Object[] {List.of(2, 4), multiply, 3}));
        assertEquals(List.of(4), context.invokeEfun("jvmud_filter", 3,
                new Object[] {List.of(2, 4), above, 3}));
        assertEquals("$a $a", context.invokeEfun("jvmud_regex_replace", 4,
                new Object[] {"a a", "a", replace, 1}));
        assertEquals(List.of(1, 2, 3), context.withCurrentObject(new ComparatorObject(), () ->
                context.invokeEfun("jvmud_sort_array", 3,
                        new Object[] {List.of(3, 1, 2), "compare", 0})));
    }

    @Test
    void engineServiceAdaptersRejectAContextWithoutMudlibServices() {
        MudlibRuntimeContext engine = new MudlibRuntimeContext(null);
        EngineEfuns.registerCore(engine);
        Efun users = engine.efunRegistry().lookup("jvmud_users", 0);

        assertNotNull(users);
        assertThrows(IllegalArgumentException.class, () -> users.invoke(new LanguageOnlyContext(), new Object[0]));
    }

    @Test
    void registryNameInventoryIncludesCompilerOnlyDeclarationsAndIsAnImmutableSnapshot() {
        EfunRegistry registry = new EfunRegistry();
        registry.registerSignature(new EfunSignature(new Symbol(LPCType.LPCINT, "first"), List.of()));
        Set<String> before = registry.names();
        registry.registerSignature(new EfunSignature(new Symbol(LPCType.LPCINT, "second"), List.of()));

        assertEquals(Set.of("first"), before);
        assertEquals(Set.of("first", "second"), registry.names());
        assertThrows(UnsupportedOperationException.class, () -> before.add("third"));
    }

    private static void assertCompatibleLookups(EfunRegistry registry, List<Declaration> declarations) {
        for (String name : registry.names()) {
            List<Declaration> overloads = declarations.stream().filter(row -> row.name().equals(name)).toList();
            int maximum = overloads.stream().mapToInt(row -> row.parameters().size()).max().orElseThrow() + 5;
            for (int arity = 0; arity <= maximum; arity++) {
                int argumentCount = arity;
                List<Declaration> exact = overloads.stream()
                        .filter(row -> row.tail().isEmpty() && row.parameters().size() == argumentCount).toList();
                List<Declaration> tails = overloads.stream()
                        .filter(row -> row.tail().isPresent() && row.parameters().size() <= argumentCount).toList();
                List<Declaration> candidates = exact.isEmpty() ? tails : exact;
                assertTrue(candidates.size() <= 1, "Baseline overloads must be unambiguous: " + name + "/" + arity);
                Efun implementation = registry.lookup(name, arity);
                EfunSignature signature = registry.lookupSignature(name, arity);
                if (candidates.isEmpty()) {
                    assertNull(implementation, name + "/" + arity);
                    assertNull(signature, name + "/" + arity);
                } else {
                    assertNotNull(implementation, name + "/" + arity);
                    assertNotNull(signature, name + "/" + arity);
                    String expected = candidates.getFirst().signature();
                    assertEquals(expected, signatureKey(implementation.signature()), name + "/" + arity);
                    assertEquals(expected, signatureKey(signature), name + "/" + arity);
                }
            }
        }
    }

    private static List<Declaration> baseline() throws Exception {
        try (var input = EfunCatalogCompatibilityTest.class.getResourceAsStream("/efuns/core-catalog.tsv")) {
            assertNotNull(input, "The pre-refactor catalog fixture must be packaged with tests");
            try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().filter(line -> !line.isBlank() && !line.startsWith("#"))
                        .map(Declaration::parse).toList();
            }
        }
    }

    private static String signatureKey(EfunSignature signature) {
        return signatureKey(signature.name(), signature.returnType(), signature.parameterTypes(),
                Optional.ofNullable(signature.varargsParameterType()));
    }

    private static String signatureKey(String name, LPCType result, List<LPCType> parameters, Optional<LPCType> tail) {
        return name + "\t" + result.name() + "\t"
                + parameters.stream().map(Enum::name).collect(Collectors.joining(",")) + "\t"
                + tail.map(Enum::name).orElse("-");
    }

    private record Declaration(String name, LPCType result, List<LPCType> parameters,
            Optional<LPCType> tail, Optional<EngineCapability> capability) {
        static Declaration parse(String line) {
            String[] cells = line.split("\t", -1);
            if (cells.length != 5) throw new IllegalArgumentException("Invalid efun fixture row: " + line);
            List<LPCType> parameters = cells[2].isEmpty() ? List.of()
                    : Arrays.stream(cells[2].split(",")).map(LPCType::valueOf).toList();
            return new Declaration(cells[0], LPCType.valueOf(cells[1]), parameters,
                    "-".equals(cells[3]) ? Optional.empty() : Optional.of(LPCType.valueOf(cells[3])),
                    "-".equals(cells[4]) ? Optional.empty() : Optional.of(EngineCapability.valueOf(cells[4])));
        }

        String signature() {
            return signatureKey(name, result, parameters, tail);
        }
    }

    /** A named LPC-style comparator exposes the lexical-object form retained by sort_array. */
    public static final class ComparatorObject {
        public int compare(int left, int right, int descending) {
            return descending == 0 ? (left > right ? 1 : 0) : (left < right ? 1 : 0);
        }
    }

    /** No world, connection or storage services are available through this language contract. */
    private static final class LanguageOnlyContext extends RuntimeContext {
        @Override public Object invokeEfun(String name, int arity, Object[] arguments) { throw new UnsupportedOperationException(); }
        @Override public Object invokeEngineEfun(String name, int arity, Object[] arguments) { throw new UnsupportedOperationException(); }
        @Override public Object invokeObject(Object target, String name, Object... arguments) { throw new UnsupportedOperationException(); }
        @Override public Object invokeOptionalObject(Object target, String name, Object... arguments) { throw new UnsupportedOperationException(); }
        @Override public boolean hasObjectMethod(Object target, String name, int arity) { return false; }
        @Override public boolean isDestroyedObject(Object value) { return false; }
    }
}
