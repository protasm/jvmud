inherit "lib/room";

void create() {
  title = "Very Short Lane";

  description = "The road ends at a hedge. Beyond it lies the rest of the world, which is\nclosed for lunch. Steps lead down to the village cellar.";

  routes = ([ "west": "room/square", "down": "room/cellar" ]);

  exit_text = "west, down";
}
