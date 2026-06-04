string name, alias, short_desc, long_desc, value, weight;
string type;
int worn, ac;
object worn_by;
object next;

void link(ob)
{
    next = ob;
}

mixed remove_link(str)
{
    if (str == name) {
        return next;
    }
    if (next)
        next = call_other(next, "remove_link", str);
    return this_object();
}

void init() {
    add_action("wear"); add_verb("wear");
    add_action("remove"); add_verb("remove");
}

string short() {
    if (worn)
        return short_desc + " (worn)";
    return short_desc;
}

void long(str) {
    write(long_desc);
}

status id(str)
{
    return str == name || str == alias || str == type;
}

mixed test_type(str)
{
    if(str == type)
        return this_object();
    if(next)
        return call_other(next, "test_type", str);
    return 0;
}

mixed tot_ac()
{
    if(next)
        return ac + call_other(next, "tot_ac");
    return ac;
}

string query_type() { return type; }

int query_value() { return value; }

int query_worn() { return worn; }

string query_name() { return name; }

mixed armor_class() { return ac; }

int wear(str)
{
    object ob;

    if (!id(str))
        return 0;
    if (environment() != this_player()) {
        write("You must get it first!\n");
        return 1;
    }
    if (worn) {
        write("You already wear it!\n");
        return 1;
    }
    ob = call_other(this_player(), "wear", this_object());
    if(!ob) {
        worn_by = this_player();
        worn = 1;
        return 1;
    }
    write("You already have an armor of class " + type + ".\n");
    return 1;
}

int get() { return 1; }

int drop(silently) {
    if (worn) {
        call_other(worn_by, "stop_wearing",name);
        worn = 0;
        worn_by = 0;
        if (!silently)
            write("You drop your worn armor.\n");
    }
    return 0;
}

int remove(str) {
    if (!id(str))
        return 0;
    if (!worn) {
        return 0;
    }
    call_other(worn_by, "stop_wearing",name);
    worn_by = 0;
    worn = 0;
    return 1;
}

int query_weight() { return weight; }

void set_name(n) { name = n; }
void set_short(s) { short_desc = s; long_desc = s + ".\n"; }
void set_value(v) { value = v; }
void set_weight(w) { weight = w; }
void set_ac(a) { ac = a; }
void set_alias(a) { alias = a; }
void set_long(l) { long_desc = l; }
void set_type(t) { type = t; }
void set_arm_light(l) { set_light(l); }
