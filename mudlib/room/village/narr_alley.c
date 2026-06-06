#include "room/std.h"
#undef EXTRA_LONG
#define EXTRA_LONG\
#undef EXTRA_INIT
#define EXTRA_INIT add_action("go_down", "down");
if (str == "well") {\
  write("You look down the well, but see only darkness.\n");\
  write("There are some iron handles on the inside.\n");\

  return;\
}

THREE_EXIT("room/village/vill_road1","north",
"room/village/bank", "east",
"room/village/post", "south",
"Narrow alley",
"A narrow alley. There is a well in the middle.\n", 1)
go_down() {
  call_other(this_player(), "move_player", "down#room/well");

  return 1;
}

id(str) {
  if (str == "well")
    return 1;
}
