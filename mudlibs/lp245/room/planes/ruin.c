#include "room/std.h"
id(str) {
  if (str == "ruin")
    return 1;

  else
    return 0;
}
FOUR_EXIT("room/planes/plane4", "south",
"room/planes/plane8", "north",
"room/planes/plane9", "east",
"room/planes/plane3", "west",
"Ruin",
"A very old looking ruin. There is no roof, and no door.\n",
1)
