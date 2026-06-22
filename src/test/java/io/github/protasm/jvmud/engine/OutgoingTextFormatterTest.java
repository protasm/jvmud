package io.github.protasm.jvmud.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.protasm.jvmud.engine.output.OutgoingTextFormatter;
import org.junit.jupiter.api.Test;

final class OutgoingTextFormatterTest {
    @Test
    void wrapsAtWhitespaceNearestToConfiguredLineLength() {
        assertEquals(
                "one two three four\nfive six\nseven eight nine",
                OutgoingTextFormatter.wrap("one two three four five six\nseven eight nine", 20));
    }

    @Test
    void ignoresAnsiColorSequencesWhenWrapping() {
        assertEquals(
                "\u001B[0;32mSer Osis of D'Liver can kiss my arse.\u001B[0m",
                OutgoingTextFormatter.wrap("\u001B[0;32mSer Osis of D'Liver can kiss my arse.\u001B[0m", 40));
    }

    @Test
    void wrapsColoredTextAtVisibleWhitespace() {
        assertEquals(
                "\u001B[0;32mone two three four\u001B[0m\nfive six seven",
                OutgoingTextFormatter.wrap("\u001B[0;32mone two three four\u001B[0m five six seven", 20));
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
