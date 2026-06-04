#include "living.h"

void reset(arg) {
    if (arg)
        return;
    set_heart_beat(1);
    name = "beggar";
    cap_name = "Beggar";
    msgin = "enters";
    msgout = "leaves";
    max_hp = 30;
    hit_point = 30;
    level = 3;
    experience = 2283;
    weapon_class = 5;
    armor_class = 0;
    alignment = 200;
    is_npc = 1;
    enable_commands();
}

string short() { return name; }

void long() {
    write("A really filthy looking poor beggar.\n");
}

status id(str) {
    return str == name;
}

void catch_tell(str) {
    string who, what;
}

void heart_beat()
{
    age += 1;
    attack();
}
