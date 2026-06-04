int money;

void reset(arg) {
    if (arg)
        return;
    money = 1;
}

int query_weight() { return 0; }

string short() {
    if (money == 0)
        return 0;
    return money + " gold coins";
}

int get() {
    call_other(this_player(), "add_money", money);
    money = 0;
    set_heart_beat(1);
    return 1;
}

void set_money(m) {
    money = m;
}

int id(str) {
    if (str == "coins")
        return 1;
    if (str == "money")
        return 1;
}

void heart_beat() {
    if (money == 0)
        destruct(this_object());
}

