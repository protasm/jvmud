#include "../std.h"
#undef EXTRA_MOVE1
#define EXTRA_MOVE1\
#undef EXTRA_RESET
#define EXTRA_RESET\
object dwarf;
if (dwarf && present(dwarf)) {\
  write("The dwarf bars the way !\n");\

  return 1;\
}

if (!dwarf || !living(dwarf)) {\
  dwarf = clone_object("obj/monster");\

  call_other(dwarf, "set_name", "dwarf");\
  call_other(dwarf, "set_level", 10);\
  call_other(dwarf, "set_al", -100);\
  call_other(dwarf, "set_short", "A short and sturdy dwarf");\
  call_other(dwarf, "set_wc", 10);\
  call_other(dwarf, "set_ac", 1);\
  move_object(dwarf, this_object());\
}

TWO_EXIT("room/mine/tunnel17", "north",
"room/mine/tunnel15", "west",
"Tunnel",
"In the tunnel into the mines.\n", 0)
