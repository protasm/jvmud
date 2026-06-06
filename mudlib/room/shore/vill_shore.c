#include "room/room.h"
#undef EXTRA_RESET
#define EXTRA_RESET no_castle_flag=1;
FOUR_EXIT("room/village/vill_road2","west",
"room/shore/jetty","east",
"room/shore/eastroad1","north",
"room/shore/crop","south",
"Road",
"You are on a road going out of the village. Eastroad runs north from here,\n"+
"along the eastern perimeter of the city, and to the south are some fields\n"+
"planted with all the crops that the city needs. The main road runs towards\n"+
"the shore to the east, and into the city to the west.\n",
1)
