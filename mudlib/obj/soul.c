string cap_name;

int get() {
    cap_name = call_other(this_player(), "query_name", 0);

    return 1;
}

int drop() { return 1; }

status id(str) { return str == "soul" || str == "ND"; }

void long() {
    write("It is transparent.\n");
}

mixed ghost() {
    cap_name = call_other(this_player(), "query_name", 0);

    return call_other(this_player(), "query_ghost");
}


void init() { soul_init();}

#include "soul_com.c"
