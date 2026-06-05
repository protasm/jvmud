string short() {
    return "A list of the top players" ;
}

void long() {
    cat("/SORT_LEVEL");
}

void init() {
    add_action("read"); add_verb("read");
}

status id(str) {
    return str == "list" || str == "top" || str == "top players" ||
    str == "list of top players" || str == "top list";
}

int read(str) {
    if (!id(str))
        return 0;

    say(call_other(this_player(), "query_name") + " reads the top list.\n");

    long();

    return 1;
}

int query_weight() { return 1; }

int get() { return 1; }

int query_value() { return 5; }
