package io.github.protasm.jvmud.compiler.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class LpcFormatterTest {
    private final LpcFormatter formatter = new LpcFormatter();

    @Test
    void formatsLegacyLpcWithFourSpaceIndentsAndBlankStatementGroups() {
        String source = """
                long() { //untyped methods are a compiler error in JVMud, not a formatter issue.
                \tif (east_door_open) //replace tabs with 4 spaces
                \t  write("An empty room with an open door to the east.\\n");
                \tif (!east_door_open) //indenting is four spaces per indent
                \t  write("An empty room with a closed door to the east.\\n");
                \tif (amiga_present) {
                \t\tif (!amiga_power)
                \t\t    write("There is an amiga here.\\n");
                \t\tif (amiga_power)
                \t\t    write("There is a powered on amiga here.\\n");
                \t}
                }
                open_door() {
                \teast_door_open = 1;
                \twrite("Ok.\\n");
                }
                close_door() {
                \teast_door_open = 0;
                \twrite("Ok.\\n");
                }
                go_east() {
                \tif (!east_door_open)
                \t\twrite("The door is closed\\n");
                \tif (east_door_open)
                \t\tmove_object(this_player(), "room/rum2");
                }
                sesam() {
                \twrite("An amiga materialises!\\n");
                \tamiga_present = 1;
                \tadd_action("power", "power");
                }
                power() {
                \tamiga_power = 1;
                \twrite("The screen lights up.\\n");
                }
                door_open() {
                \treturn east_door_open;
                }
                summon() {
                \tname = clone_object("obj/player");
                \twrite("Summoning a player...\\n");
                \twrite(name);
                \twrite(", His hp is ");
                \twrite(call_other(name, "condition", 0));
                \twrite("\\n");
                }
                hit() {
                \tif (!name) {
                \t\twrite("Hit what ?\\n");
                \t\treturn;
                \t}
                \tcall_other(name, "hit_player", 3);
                }
                fac(n) {
                \tif (n <= 0)
                \t\treturn 1;
                \treturn n * fac(n-1);
                }
                test() {
                \ta = a + 1;
                \t//this is okay
                \twrite("Fac "); write(a); write(" is "); write(fac(a)); write("\\n");
                }
                short() {
                \twrite("Computer room\\n");
                \tif (amiga_present) {
                \t\tif (amiga_power)
                \t\t\twrite("A powered amiga.\\n");
                \t\tif (!amiga_power)
                \t\t\twrite("An amiga.\\n");
                \t}
                \treturn 0;
                }
                """;

        assertEquals(expectedFormattedRoom(), formatter.format(source));
    }

    @Test
    void formattingIsIdempotent() {
        String formatted = expectedFormattedRoom();

        assertEquals(formatted, formatter.format(formatted));
    }

    @Test
    void sortsTopLevelMethodsAlphabeticallyAfterPreamble() {
        String source = """
                int counter;
                #define LIMIT 3

                zed() {
                \treturn 3;
                }

                // comment for alpha
                alpha() {
                \treturn 1;
                }

                mixed *middle() {
                \treturn ({ 2 });
                }
                """;

        String expected = """
                int counter;
                #define LIMIT 3

                // comment for alpha
                alpha() {
                    return 1;
                }

                mixed *middle() {
                    return ({ 2 });
                }

                zed() {
                    return 3;
                }
                """;

        assertEquals(expected, formatter.format(source));
    }

    @Test
    void movesTopLevelFieldsToBeginningSortedByFieldName() {
        String source = """
                zed() {
                    object local;

                    local = this_object();
                    return local;
                }

                string title;
                int amount;

                alpha() {
                    return amount;
                }

                object owner;
                """;

        String expected = """
                int amount;
                object owner;
                string title;

                alpha() {
                    return amount;
                }

                zed() {
                    object local;

                    local = this_object();
                    return local;
                }
                """;

        assertEquals(expected, formatter.format(source));
    }

    @Test
    void movesSplitMethodOpeningBraceToDeclarationLineAndRemovesFollowingBlank() {
        String source = """
                status drink(mixed str)
                { //move this to the end of the method declaration

                    if (str && str != "beer" && str != "from bottle")
                        return 0;

                    if (!full)
                        return 0;

                    if (!call_other(this_player(), "drink_alcohol", 2))
                        return 1;

                    full = 0;

                    write("It is really good beer!\\n");

                    say(call_other(this_player(), "query_name", 0) +
                    " drinks a bottle of beer.\\n");

                    return 1;
                }
                """;

        String expected = """
                status drink(mixed str) { //move this to the end of the method declaration
                    if (str && str != "beer" && str != "from bottle")
                        return 0;

                    if (!full)
                        return 0;

                    if (!call_other(this_player(), "drink_alcohol", 2))
                        return 1;

                    full = 0;

                    write("It is really good beer!\\n");

                    say(call_other(this_player(), "query_name", 0) +
                    " drinks a bottle of beer.\\n");

                    return 1;
                }
                """;

        assertEquals(expected, formatter.format(source));
    }

    @Test
    void ignoresBracesInStringsAndCommentsWhenIndenting() {
        String source = """
                value() {
                \twrite("{ not a block }"); // } not a close brace
                \t/* { not an open brace */
                \tif (ready) {
                \t\twrite("ok");
                \t}
                }
                """;

        String expected = """
                value() {
                    write("{ not a block }"); // } not a close brace
                    /* { not an open brace */
                    if (ready) {
                        write("ok");
                    }
                }
                """;

        assertEquals(expected, formatter.format(source));
    }

    private String expectedFormattedRoom() {
        return """
                close_door() {
                    east_door_open = 0;

                    write("Ok.\\n");
                }

                door_open() {
                    return east_door_open;
                }

                fac(n) {
                    if (n <= 0)
                        return 1;

                    return n * fac(n-1);
                }

                go_east() {
                    if (!east_door_open)
                        write("The door is closed\\n");

                    if (east_door_open)
                        move_object(this_player(), "room/rum2");
                }

                hit() {
                    if (!name) {
                        write("Hit what ?\\n");

                        return;
                    }

                    call_other(name, "hit_player", 3);
                }

                long() { //untyped methods are a compiler error in JVMud, not a formatter issue.
                    if (east_door_open) //replace tabs with 4 spaces
                        write("An empty room with an open door to the east.\\n");

                    if (!east_door_open) //indenting is four spaces per indent
                        write("An empty room with a closed door to the east.\\n");

                    if (amiga_present) {
                        if (!amiga_power)
                            write("There is an amiga here.\\n");

                        if (amiga_power)
                            write("There is a powered on amiga here.\\n");
                    }
                }

                open_door() {
                    east_door_open = 1;

                    write("Ok.\\n");
                }

                power() {
                    amiga_power = 1;

                    write("The screen lights up.\\n");
                }

                sesam() {
                    write("An amiga materialises!\\n");

                    amiga_present = 1;

                    add_action("power", "power");
                }

                short() {
                    write("Computer room\\n");

                    if (amiga_present) {
                        if (amiga_power)
                            write("A powered amiga.\\n");

                        if (!amiga_power)
                            write("An amiga.\\n");
                    }

                    return 0;
                }

                summon() {
                    name = clone_object("obj/player");

                    write("Summoning a player...\\n");
                    write(name);
                    write(", His hp is ");
                    write(call_other(name, "condition", 0));
                    write("\\n");
                }

                test() {
                    a = a + 1;

                    //this is okay
                    write("Fac "); write(a); write(" is "); write(fac(a)); write("\\n");
                }
                """;
    }
}
