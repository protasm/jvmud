inherit "room/room";

string messages;
int new_mail;

exit() {
  object ob;

  if (ob = present("mailread", this_player()))
    destruct(ob);
}

init() {
  ::init();

  move_object(clone_object("obj/mail_reader"), this_player());
}

query_mail(silent) {
  string name;
  string new;

  name = lower_case(call_other(this_player(), "query_name"));

  if (!restore_object("room/village/post_dir/" + name) || messages == "") return 0;
  if (silent) return 1;
  new = "";

  if (new_mail)
    new = " NEW";

  write("\nThere is" + new + " mail for you in the post office\n"+
  "   (south from village road).\n\n");

  return 1;
}

reset(arg) {
  if (arg)
    return;

  set_light(1);

  dest_dir = ({ "room/village/narr_alley", "north" });
  short_desc = "The post office";

  long_desc = "You are in the post office. Commands:\n" +
  "read         Read from the mailbox.\n" +
  "mail <name>  Mail to player 'name'.\n" +
  "from         List all headers.\n";

  no_castle_flag = 1;
}
