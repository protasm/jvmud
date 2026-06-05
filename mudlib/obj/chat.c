object next;
object monster;
string chat_str;

void link(ob) {
    next = ob;
}

void load_chat(str) {
    chat_str = str;
}

void set_monster(m) {
    monster = m;
}

mixed chat(nr) {
    object room;

    if (nr == 0){
        room = environment(monster);

        if(room)
            return tell_room(room,chat_str);
    }

    nr -= 1;

    if (next)
        return call_other(next, "chat", nr);
    else
        return 0;
}

mixed remove_chat(str) {
    if (str == chat_str) {
        destruct(this_object());

        return next;
    }

    if (next)
        next = call_other(next, "remove_chat", str);

    return this_object();
}

void collaps() {
    if(next)
        call_other(next, "collaps");

    destruct(this_object());
}

