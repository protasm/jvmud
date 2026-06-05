status wielded;
object wielded_by;
string name_of_weapon;
string cap_name;
string alt_name;
string alias_name;
string short_desc;
string long_desc;
string read_msg;
int class_of_weapon;
int value;
int local_weight;
object hit_func;
object wield_func;

string query_name() { return name_of_weapon; }

void long() {
    write(long_desc);
}

void reset(arg) {
    if (arg)
        return;

    wielded = 0; value = 0;
}

void init() {
    if (read_msg) {
        add_action("read"); add_verb("read");
    }

    add_action("wield"); add_verb("wield");
}

int wield(str) {
    if (!id(str))
        return 0;

    if (environment() != this_player()) {
        write("You must get it first!\n");

        return 1;
    }

    if (wielded) {
        write("You're already wielding it!\n");

        return 1;
    }

    if(wield_func)
        if(!call_other(wield_func,"wield",this_object()))
            return 1;

    wielded_by = this_player();

    call_other(this_player(), "wield", this_object());

    wielded = 1;

    return 1;
}

string short() {
    if (wielded)
        return short_desc + " (wielded)";

    return short_desc;
}

mixed weapon_class() {
    return class_of_weapon;
}

status id(str) {
    return str == name_of_weapon || str == alt_name || str == alias_name;
}

int drop(silently) {
    if (wielded) {
        call_other(wielded_by, "stop_wielding");

        wielded = 0;

        if (!silently)
            write("You drop your wielded weapon.\n");
    }

    return 0;
}

void un_wield() {
    if (wielded)
        wielded = 0;
}

mixed hit(attacker) {
    if (hit_func) {
        return call_other(hit_func,"weapon_hit",attacker);
    }

    return 0;
}

void set_name(n) {
    name_of_weapon = n;
    cap_name = capitalize(n);
    short_desc = cap_name;
    long_desc = "You see nothing special.\n";
}

int read(str) {
    if (!id(str))
        return 0;

    write(read_msg);

    return 1;
}

int query_value() {
    return value;
}

int get() { return 1; }

int query_weight() { return local_weight; }

void set_class(c) { class_of_weapon = c; }

void set_weight(w) { local_weight = w; }

void set_value(v) { value = v; }

void set_alt_name(n) { alt_name = n; }

void set_hit_func(ob) { hit_func = ob; }

void set_wield_func(ob) { wield_func = ob; }

void set_alias(n) { alias_name = n; }

void set_short(sh) { short_desc = sh; long_desc = short_desc + "\n";}

void set_long(long) { long_desc = long; }

void set_read(str) { read_msg = str; }
