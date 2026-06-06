#include "room/room.h"
#undef EXTRA_RESET
#define EXTRA_RESET\
object giant;
if (!giant || !living(giant)) {\
  giant = clone_object("obj/monster");\

  call_other(giant, "set_name", "giant");\
  call_other(giant, "set_level", 15);\
  call_other(giant, "set_short", "A giant");\
  call_other(giant, "set_wc", 20);\
  call_other(giant, "set_ac", 2);\
  call_other(giant, "set_aggressive", 1);\
  move_object(giant, this_object());\
}

TWO_EXIT("room/giant/giant_path", "east",
"room/giant/giant_conf", "west",
"Lair of the Giant",
"There are mountains all around you.\n", 1)
