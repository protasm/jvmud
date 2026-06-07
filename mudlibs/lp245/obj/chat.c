string chat_str;
object monster;
object next;
/*
short() { return chat_str; }

*/
chat(nr) {
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

collaps() {
  if(next)
    call_other(next, "collaps");

  destruct(this_object());
}

link(ob) {
  next = ob;
}

load_chat(str) {
  chat_str = str;
}

remove_chat(str) {
  if (str == chat_str) {
    destruct(this_object());

    return next;
  }

  if (next)
    next = call_other(next, "remove_chat", str);

  return this_object();
}

set_monster(m) {
  monster = m;
}
drop() { return 1; }
