#define EXTRA_MOVE /* nothing */
string name,short_desc,long_desc,dir1,dir2,dest;

void set_dest(string s) { if(s) dest=s; }
status enter(string s) {
  if(!id(s)) return;
  EXTRA_MOVE

  this_player()->move_player("into the "+name+"#"+dest);

  return 1;
}

status go_room() {
  EXTRA_MOVE

  this_player()->move_player(dir1+"#"+dest);

  return 1;
}

status id(string s) {
  if(!s) return;
  return(s==name);
}
string short() { return short_desc; }

void long() { write(long_desc); }

void init() {
  if(name) add_action("enter","enter");
  if(dir1) add_action("go_room",dir1);
  if(dir2) add_action("go_room",dir2);
}

void reset(mixed arg) {
  if(arg) return;
  dest="room/village/church";
  long_desc="You see nothing special.\n";
}
void set_name(string s) { name=s; }

void set_short(string s) { short_desc=s; }

void set_long(string s) { if(s) long_desc=s; }

void set_dirs(string d1, string d2) {
  if(d1) {
    dir1=d1;
    dir2=d2;
  }
}
