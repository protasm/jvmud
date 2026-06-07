inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Shaft";
  long_desc = "In a shaft going straight down.\n";
  dest_dir = ({
    "room/mine/tunnel3", "up",
    "room/mine/tunnel9", "down"
  });
}
