string short() {
    return "The local prison";
}

void long() {
    write("You are in the local prison.\n");
    write("There are no exits.\n");
}

void reset(arg) {
    if (arg)
        return;

    set_light(1);
}

void init() {
    add_action("quit"); add_verb("quit");
}

int quit() { return 1; }

string realm() {return "NT";}
