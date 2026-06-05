/*
* This is a generic valuable object. Clone a copy, and
* setup local values.
*/

string short_desc, long_desc;
int value, local_weight;
string name, alias_name;
string read_msg;

status id(str) {
    return str == name || str == alias_name;
}

string short() {
    return short_desc;
}

void long() {
    write(long_desc);
}

int query_value() { return value; }

void set_id(str) {
    local_weight = 1;
    name = str;
}

void set_alias(str) {
    alias_name = str;
}

void set_short(str) {
    short_desc = str;
    long_desc = "You see nothing special.\n";
}

void set_long(str) {
    long_desc = str;
}

void set_value(v) {
    value = v;
}

void set_weight(w) {
    local_weight = w;
}

void set_read(str) {
    read_msg = str;
}

int get() {
    return 1;
}

int query_weight() {
    return local_weight;
}

void init() {
    if (!read_msg)
        return;

    add_action("read"); add_verb("read");
}

int read(str) {
    if (str != name &&  str != alias_name)
        return 0;

    write(read_msg);

    return 1;
}
