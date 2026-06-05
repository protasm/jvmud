/*
* Just make a noisy entrance for a wizard.
*/
void reset() {
    shout("You hear a distant rumble.\n" +
        call_other(this_player(), "query_name") +
    " has entered the game.\n");

    set_heart_beat(1);
}

void heart_beat() {
    destruct(this_object());
}
