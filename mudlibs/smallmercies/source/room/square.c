inherit "lib/room";

void create() {
  title = "Village Square";

  description = "A fountain commemorates the founding of the fountain. The entire village\nfits on a postcard, provided nobody writes very large.";

  routes = ([ "west": "room/inn", "north": "room/garden", "east": "room/lane" ]);

  exit_text = "west, north, east";

  resident("mayor", "The mayor wears a sash reading TEMPORARILY IMPORTANT.", "Welcome! Our greatest export is manageable expectations.", 0, 0);
}
