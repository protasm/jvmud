inherit "lib/room";

void create() {
  title = "Municipal Herb Garden";

  description = "Mint grows with no regard for municipal boundaries. A goose guards the\nparsley as though it contains the crown jewels.";

  routes = ([ "south": "room/square" ]);

  exit_text = "south";

  resident("goose", "A goose in a tiny helmet. Its expression says NO REFUNDS.", "HONK. (This appears to be the entire constitution.)", 16, 4);
}
