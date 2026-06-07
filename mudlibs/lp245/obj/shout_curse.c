int start_time;
/*
* This is a curse that the player can't get rid of.
* It prevents you from shouting.
*/
status do_shout() {
  if (time() < start_time + 3600) {
    write("You can't shout with a sore throat !\n");

    say(call_other(this_player(), "query_name") +
    " makes croaking sounds.\n");

    return 1;
  } else {
    destruct(this_object());

    return 0;
  }
}

string extra_look() {
  return "the throat seems to be sore";
}

status id(string str) {
  return str == "shout_curse";
}
status drop() { return 1; }

void init() {
  add_action("do_shout", "shout");
}

void init_arg(string str) {
  sscanf(str, "%d", start_time);
}

void long() {
  write("How can you look at a curse ?\n");
}

string query_auto_load() {
  return "obj/shout_curse:" + start_time;
}

void start(object ob) {
  move_object(this_object(), ob);

  start_time = time();

  tell_object(ob, "You get a sore throat suddenly, without any reason.\n");
}
