string short() {
    return "A newspaper" ;
}

void long() {
    cat("/NEWSPAPER");
}

void init() {
    add_action("read"); add_verb("read");
}

status id(str) {
    return str == "newspaper" || str == "paper" || str == "news";
}

int read(str) {
    if (!id(str))
        return 0;
    say(call_other(this_player(), "query_name") + " reads the newspaper.\n");
    long();
    return 1;
}

int query_weight() { return 1; }

int get() { return 1; }

int query_value() { return 5; }
