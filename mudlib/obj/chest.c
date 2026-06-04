int local_weight;
int chest_is_open;

void init() {
    add_action("open"); add_verb("open");
    add_action("close"); add_verb("close");
}

status id(str) { return str == "chest"; }

string short() {
    return "chest";
}

void long() {
    write("A chest that seems to be of a high value.\n");
    if (chest_is_open)
        write("It is open.\n");
    else
        write("It is closed.\n");
}

int query_value() { return 200; }

int query_weight() { return 8; }

int get() { return 1; }

int can_put_and_get() { return chest_is_open; }

int add_weight(w) {
    if (w + local_weight > 8)
        return 0;
    local_weight += w;
}

int close(str)
{
    if (!id(str))
        return 0;
    chest_is_open = 0;
    write("Ok.\n");
    return 1;
}

int open(str)
{
    if (!id(str))
        return 0;
    chest_is_open = 1;
    write("Ok.\n");
    return 1;
}

void reset(arg) {
    if (arg)
        return;
    chest_is_open = 0;
}
