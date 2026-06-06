#include "room/std.h"
#undef EXTRA_INIT
#define EXTRA_INIT\
#undef EXTRA_RESET
#define EXTRA_RESET\
add_action("climb", "climb");
if (!present("rope"))\
  move_object(clone_object("obj/rope"), this_object());

TWO_EXIT("room/planes/plane7", "east",
"room/giant/giant_path", "west",
"Big tree",
"A big single tree on the plain.\n", 1)
climb(str) {
  if (!id(str))
    return 0;

  write("There are no low branches.\n");

  return 1;
}

id(str) {
  if (str == "tree" || str == "big tree")
    return 1;

  return 0;
}

tie(str) {
  if (str == "tree" || str == "big tree") {
    write("The branches are very high up.\n");

    return 0;
  }

  return 0;
}
