int exit_num;

string short() {
    return "A maze";
}

void long() {
    write("In a maze.\n");
    write("There are four obvious exits: north, south, west and east.\n");
}

void reset() {
    exit_num = random(4);    /* "grin" */
}

void init() {
    add_action("e0"); add_verb("north");
    add_action("e1"); add_verb("south");
    add_action("e2"); add_verb("east");
    add_action("e3"); add_verb("west");
}

int e0() {
    if (exit_num == 0)
        call_other(this_player(), "move_player", "north#room/maze1/maze4");
    else
        call_other(this_player(), "move_player", "north#room/maze1/maze2");

    return 1;
}

int e1() {
    if (exit_num == 1)
        call_other(this_player(), "move_player", "south#room/maze1/maze4");
    else
        call_other(this_player(), "move_player", "south#room/maze1/maze1");

    return 1;
}

int e2() {
    if (exit_num == 2)
        call_other(this_player(), "move_player", "east#room/maze1/maze4");
    else
        call_other(this_player(), "move_player", "east#room/well");

    return 1;
}

int e3() {
    if (exit_num == 3)
        call_other(this_player(), "move_player", "west#room/maze1/maze4");
    else
        call_other(this_player(), "move_player", "west#room/well");

    return 1;
}
