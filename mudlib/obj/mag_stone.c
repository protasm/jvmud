int gived;

status id(str) {
    return str == "stone" || str == "black stone";
}

string short() {
    return "A black stone";
}

void long() {
    write("The stone is completely black, and feels warm to the touch.\n");
    write("There seems to be somthing magic with it.\n");
}

int query_weight() { return 1; }

/* Prevent giving away this object */
int drop() {
    gived += 1;

    if (gived == 2)
        return 1;
    else
        return 0;
}

int get() { return 1; }

void init() {
    add_action("list_peoples"); add_verb("people");
    add_action("list_files"); add_verb("ls");
    add_action("cat_file"); add_verb("cat");
    add_action("drop_object"); add_verb("drop");
}

int list_files(path) {
    ls(path);

    return 1;
}

int cat_file(path) {
    if (!path)
        return 0;

    cat(path);

    return 1;
}

int list_peoples() {
    people();

    return 1;
}

int drop_object(str) {
    if (str == "all") {
        drop_object("black stone");

        return 0;
    }

    if (!str || !id(str))
        return 0;

    write("The stone dissapears.\n");

    say(call_other(this_player(), "query_name") + " drops a black stone. It dissapears.\n");

    call_other(this_player(), "add_weight", -1);

    destruct(this_object());

    return 1;
}
