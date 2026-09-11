inherit "lib/room";

void create() {
  title = "Cellar of Mild Peril";

  description = "Cheese wheels loom in the gloom. Something small and overconfident\nrattles a spoon against a saucepan shield.";

  routes = ([ "up": "room/lane" ]);

  exit_text = "up";

  resident("rat", "A pantry rat has declared itself Duke of Cheddar.", "Squeak! The cheese tax is one hundred percent.", 20, 5);
}
