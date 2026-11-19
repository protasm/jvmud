package io.github.protasm.jvmud.language.efun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Language functions execute with LPC values and a minimal invocation context, without an engine. */
final class LanguageEfunsTest {
    private final ValueContext context = new ValueContext();

    @Test
    void textFormattingSplittingAndScanningPreserveLPCConventions() {
        assertEquals(List.of(""), call("jvmud_split_text", "", ""));
        assertEquals(List.of("a", "b"), call("jvmud_split_text", "ab", ""));
        assertEquals(List.of("a", ""), call("jvmud_split_text", "a::", "::"));
        assertEquals("a\nb\n", call("jvmud_wrap_text", "a b", 1));
        assertEquals("", call("jvmud_wrap_text", ""));
        assertEquals("[1, 2]", call("jvmud_format_text", "%O", List.of(1, 2)));
        assertEquals("   a\n   b", call("jvmud_format_text", "%=4.1s", "a b"));
        assertEquals("cd", call("jvmud_extract_text", "abcd", 2));
        assertEquals("bc", call("jvmud_extract_text", "abcd", 1, 2));
        assertEquals(2, call("jvmud_sscanf", "A=7", "%s=%d", 0, 0));
    }

    @Test
    void regexOperationsPreservePatternTranslationReplacementAndCallbackRules() {
        assertEquals(List.of("abba"), call("jvmud_regex_match", List.of("abba", "cc"), "b+"));
        assertEquals(0, call("jvmud_regex_match", List.of("cc"), "b+"));
        assertEquals("ba ab", call("jvmud_regex_replace", "ab ab", "(a)(b)", "\\2\\1", 0));
        assertEquals("ba ba", call("jvmud_regex_replace", "ab ab", "(a)(b)", "\\2\\1", 1));
        RuntimeCallable uppercase = (runtime, arguments) -> {
            assertSame(context, runtime);
            return arguments[0].toString().toUpperCase();
        };
        assertEquals("AB AB", call("jvmud_regex_replace", "ab ab", "ab", uppercase, 1));
        assertEquals(List.of("? -v"), call("jvmud_regex_match", List.of("? -v", "x"), "^?( -v)*$"));
        assertEquals(List.of("a", "[", "bc"), call("jvmud_regex_explode", "a[bc", "[[]"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectionQueriesKeepMappingSlotsAndMutableResults() {
        Map<Object, Object> source = new LinkedHashMap<>();
        source.put("first", RuntimeMapping.multiValue(1, 2));
        source.put("second", RuntimeMapping.multiValue(3, 4));
        assertEquals(List.of(1, 3), call("jvmud_mapping_values", source));
        assertEquals(List.of(2, 4), call("jvmud_mapping_values", source, 1));
        assertEquals(List.of("first", "second"), call("jvmud_mapping_keys", source));
        assertSame(source, call("jvmud_mapping_delete", source, "first"));
        assertEquals(List.of("second"), new ArrayList<>(source.keySet()));
        assertEquals(Map.of("x", 1, "y", 1), call("jvmud_mapping_from_keys", List.of("x", "y")));
        assertEquals(Map.of(), call("jvmud_mapping_from_keys", 0));
        List<Object> allocated = (List<Object>) call("jvmud_allocate", 3);
        allocated.set(0, 7);
        assertEquals(List.of(7, 0, 0), allocated);
        assertEquals(1, call("jvmud_member", "abc", (int) 'b'));
        assertEquals(3, call("jvmud_size", "abc"));
        assertEquals(0, call("jvmud_size", (Object) null));
    }

    @Test
    void collectionCallbacksForwardExtraArgumentsAndNamedComparatorsUseTheLexicalObject() {
        RuntimeCallable above = (runtime, arguments) ->
                ((Number) arguments[0]).intValue() > ((Number) arguments[1]).intValue() ? 1 : 0;
        assertEquals(List.of(3), call("jvmud_filter", List.of(1, 2, 3), above, 2));
        RuntimeCallable mapValue = (runtime, arguments) ->
                ((Number) arguments[1]).intValue() + ((Number) arguments[2]).intValue();
        assertEquals(Map.of("a", 11), call("jvmud_map", Map.of("a", 1), mapValue, 10));
        RuntimeCallable longKey = (runtime, arguments) ->
                arguments[0].toString().length() > ((Number) arguments[1]).intValue() ? 1 : 0;
        assertEquals(Map.of("long", 2), call("jvmud_filter_indices", Map.of("a", 1, "long", 2), longKey, 1));
        RuntimeCallable comparator = (runtime, arguments) ->
                (((Number) arguments[0]).intValue() - ((Number) arguments[1]).intValue())
                        * ((Number) arguments[2]).intValue() > 0 ? 1 : 0;
        List<Integer> source = List.of(3, 1, 2);
        assertEquals(List.of(3, 2, 1), call("jvmud_sort_array", source, comparator, -1));
        assertEquals(List.of(1, 2, 3), call("jvmud_sort_array", source, "compare", 1));
        assertEquals(List.of(3, 1, 2), source);
        assertTrue(context.namedComparisons > 0);
    }

    @Test
    void callableApplicationExpandsOnlyItsFinalArrayArgument() {
        RuntimeCallable collect = (runtime, arguments) -> List.of(arguments);
        assertEquals(List.of("first", "second", "third"),
                call("jvmud_apply_callable", collect, "first", List.of("second", "third")));
        assertEquals(List.of(List.of("kept"), "tail"),
                call("jvmud_apply_callable", collect, List.of("kept"), "tail"));
        assertEquals(0, call("jvmud_apply_callable", 0, "ignored"));
    }

    @Test
    void valuesRoundTripWithoutWorldOrStorageServices() {
        Map<String, Object> value = Map.of("coins", 7, "names", List.of("Ada", "Ben"));
        assertEquals(value, call("jvmud_deserialize_lpc_value", call("jvmud_serialize_lpc_value", value)));
        assertEquals(value, call("jvmud_parse_json", call("jvmud_format_json", value)));
        assertEquals(9, call("jvmud_to_int", " 9 "));
        assertEquals(0, call("jvmud_to_int", "not a number"));
        assertEquals(3.0d, call("jvmud_sqrt", "9"));
        assertEquals(1, call("jvmud_is_string", "text"));
        assertEquals(0, call("jvmud_is_int", 1.5d));
        assertEquals(1, call("jvmud_is_float", 1.5d));
        assertEquals(1, call("jvmud_is_array", List.of()));
        assertEquals(1, call("jvmud_is_mapping", Map.of()));
        assertNull(context.functions.lookup("jvmud_is_object", 1));
        assertNull(context.functions.lookup("jvmud_current_actor", 0));
        assertNull(context.functions.lookup("jvmud_time", 0));
    }

    @Test
    void invalidArityAndValuesRetainTheirErrors() {
        Efun toInt = context.functions.lookup("jvmud_to_int", 1);
        assertNotNull(toInt);
        assertThrows(IllegalArgumentException.class, () -> toInt.invoke(context, new Object[0]));
        assertThrows(IllegalArgumentException.class, () -> call("jvmud_mapping_values", "not a mapping"));
        assertThrows(IllegalArgumentException.class, () -> call("jvmud_map", List.of(1), "not a callable"));
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> call("jvmud_raise_error", "authored failure"));
        assertEquals("authored failure", failure.getMessage());
    }

    private Object call(String name, Object... arguments) {
        return context.invokeEfun(name, arguments.length, arguments);
    }

    /** Supplies only declaration lookup and lexical method invocation; no engine state exists. */
    private static final class ValueContext extends RuntimeContext {
        private final EfunRegistry functions = new EfunRegistry();
        private final Object lexicalObject = new Object();
        private int namedComparisons;

        private ValueContext() {
            LanguageEfuns.register(functions);
        }

        @Override
        public Object currentObject() {
            return lexicalObject;
        }

        @Override
        public Object invokeEfun(String name, int arity, Object[] arguments) {
            Efun function = functions.lookup(name, arity);
            assertNotNull(function, name);
            return function.invoke(this, arguments);
        }

        @Override
        public Object invokeEngineEfun(String name, int arity, Object[] arguments) {
            return invokeEfun(name, arity, arguments);
        }

        @Override
        public Object invokeObject(Object target, String methodName, Object... arguments) {
            return invokeOptionalObject(target, methodName, arguments);
        }

        @Override
        public Object invokeOptionalObject(Object target, String methodName, Object... arguments) {
            assertSame(lexicalObject, target);
            assertEquals("compare", methodName);
            namedComparisons++;
            return (((Number) arguments[0]).intValue() - ((Number) arguments[1]).intValue())
                    * ((Number) arguments[2]).intValue() > 0 ? 1 : 0;
        }

        @Override
        public boolean hasObjectMethod(Object target, String methodName, int arity) {
            return target == lexicalObject && methodName.equals("compare") && arity == 3;
        }

        @Override
        public boolean isDestroyedObject(Object value) {
            return false;
        }
    }
}
