inherit "lib/room";

void create() {
  title = "The Resting Hero";

  description = "A fire crackles beside chairs with heroic dents. A sign offers free rest;\nthe innkeeper considers this cheaper than hiring a healer.";

  routes = ([ "east": "room/square" ]);

  exit_text = "east";

  resident("innkeeper", "The innkeeper polishes a mug that has already surrendered.", "Type rest here for full health. Heroics are thirsty work.", 0, 0);
}
