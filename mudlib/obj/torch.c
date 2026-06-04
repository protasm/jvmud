string amount_of_fuel;
string name;
string long_lit_desc;
string long_unlit_desc;
status is_lit;
int weight;

void long() {
    if (is_lit) write(long_lit_desc);
    else write(long_unlit_desc);
}

void reset(arg) {
    if (arg)
        return;
    amount_of_fuel = 0; name = 0; is_lit = 0; weight = 0;
}

void set_weight(w) { weight = w; }

int query_weight() { return weight; }

string short() {
    if (is_lit)
        return name + " (lit)";
    if (amount_of_fuel == 0)
        return name + " (burnt out)";
    return name;
}

void set_name(n) {
    name = n;
    long_lit_desc = "A " + name + " (lit)\n";
    long_unlit_desc = "A " + name + "\n";
}
void set_fuel(f) { amount_of_fuel = f; }

void init() {
    add_action("light"); add_verb("light");
    add_action("extinguish"); add_verb("extinguish");
}

int light(str) {
    if (!str || str != name)
        return 0;
    if (amount_of_fuel == 0) {
        write("End of fuel.\n");
        return 1;
    }
    if (is_lit) {
        write("It is already lit.\n");
        return 1;
    }
    is_lit = 1;
    write("Ok.\n");
    set_light(1);
    set_heart_beat(1);
    return 1;
}

int extinguish(str) {
    if (!str || str != name)
        return 0;
    if(!is_lit) {
        write("It is not lit!\n");
        return 1;
    }
    is_lit = 0;
    write("Ok.\n");
    set_light(-1);
    set_heart_beat(0);
    return 1;
}

void heart_beat() {
    object ob;
    if (!is_lit)
        return;
    amount_of_fuel -= 1;
    if (amount_of_fuel > 0)
        return;
    say(name + " goes dark.\n");
    set_heart_beat(0);
    is_lit = 0;
    set_light(-1);
    ob = environment();
    if (call_other(ob, "query_level"))
        call_other(ob, "add_weight", -weight);
    destruct(this_object());
}

status id(str) {
    return str == name;
}

int query_value() {
    return amount_of_fuel/100 + 1;
}

int get() { return 1; }

void set_long_lit(str) { long_lit_desc = str; }
void set_long_unlit(str) { long_unlit_desc = str; }
