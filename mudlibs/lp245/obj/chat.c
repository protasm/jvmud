string chat_str;
object monster;
object next;
/*
short() { return chat_str; }

*/
status chat(int nr) {
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

void collaps() {
  if(next)
    call_other(next, "collaps");

  destruct(this_object());
}

void link(object ob) {
  next = ob;
}

void load_chat(string str) {
  chat_str = str;
}

object remove_chat(string str) {
  if (str == chat_str) {
    destruct(this_object());

    return next;
  }

  if (next)
    next = call_other(next, "remove_chat", str);

  return this_object();
}

void set_monster(object m) {
  monster = m;
}
status drop() { return 1; }
