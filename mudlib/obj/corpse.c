#define DECAY_TIME    100

string name;
int decay;

int prevent_insert() {
    write("The corpse is too big.\n");
    return 1;
}

void init() {
    add_action("search"); add_verb("search");
}

void reset() {
    set_heart_beat(1);
}

void set_name(n)
{
    name = n;
    decay = DECAY_TIME;
}

string short() {
    if (decay < 20)
        return "the somewhat decayed remains of " + capitalize(name);
    return "corpse of " + capitalize(name);
}

void long() {
    write("This is the dead body of " + capitalize(name) + ".\n");
}

status id(str) {
    return str == "corpse" || str == "corpse of " + name ||
    str == "remains";
}

void heart_beat()
{
    decay -= 1;
    if (decay > 0)
        return;
    destruct(this_object());
}

int can_put_and_get() { return 1; }

int search(str)
{
    object ob;
    if (!str || !id(str))
        return 0;
    write("You search " + str + ", and find:\n");
    say(call_other(this_player(), "query_name") + " searches " + str + ".\n");
    if ( ! search_obj(this_object()))
        {
            write("\tNothing.\n");
    }
    else
        {
            write("\n");
    }
    return 1;
}

mixed search_obj(cont)
{
    object ob;
    int total;
    string item;

    if (!call_other(cont, "can_put_and_get"))
        return 0;
    ob = first_inventory(cont);
    while(ob) {
        total += 1;
        item = call_other(ob, "short");
        write(item + ", ");
        ob = next_inventory(ob);
    }
    return total;
}

int get() {
    return 1;
}

int query_weight() {
    return 5;
}
