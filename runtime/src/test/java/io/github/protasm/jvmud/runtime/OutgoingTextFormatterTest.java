package io.github.protasm.jvmud.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class OutgoingTextFormatterTest {
    @Test
    void wrapsAtWhitespaceNearestToConfiguredLineLength() {
        assertEquals(
                "one two three four\nfive six\nseven eight nine",
                OutgoingTextFormatter.wrap("one two three four five six\nseven eight nine", 20));
    }

    @Test
    void leavesLongWordsIntactWhenNoWhitespaceCanBeUsed() {
        assertEquals(
                "supercalifragilistic",
                OutgoingTextFormatter.wrap("supercalifragilistic", 20));
    }

    @Test
    void buildsRulerAtConfiguredLength() {
        assertEquals("+---------+---------+---------+---------", OutgoingTextFormatter.ruler(40));
        assertEquals("+---------+---------+-", OutgoingTextFormatter.ruler(22));
    }
}
