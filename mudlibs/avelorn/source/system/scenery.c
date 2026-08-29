void initialize(mixed first_load) { }

int describe(object place, mixed value) {
  string target;
  string place_name;
  string place_id;

  if (!place || !value) { return 0; }
  target = jvmud_lowercase_text(value);
  if (jvmud_size(target) == 0) { return 0; }
  place_name = jvmud_invoke_lpc_object(place, "short");
  place_id = jvmud_lpc_object_id(place);
  if (jvmud_extract_text(place_id, 0, 5) != "place/") { return 0; }

  write(jvmud_capitalize_text(target) + "\n");
  if (jvmud_extract_text(place_id, 0, 17) == "place/brindleford/") {
    write("The " + target + " belongs to Brindleford's practical village fabric. Its repairs, local materials, and Crown inspection marks show how " + place_name + " is maintained rather than merely picturesque.\n");
  } else if (jvmud_extract_text(place_id, 0, 15) == "place/greyhaven/") {
    write("The " + target + " is part of Greyhaven's civic life. A ward tally, maker's mark, or maintenance detail connects it to the officials and guilds responsible for " + place_name + ".\n");
  } else if (jvmud_extract_text(place_id, 0, 16) == "place/ashenwatch/") {
    write("The " + target + " bears evidence of Ashenwatch's layered defenses and the corrupted ward-fire: older Crown workmanship lies beneath hasty alterations made during the keep's fall.\n");
  } else if (jvmud_extract_text(place_id, 0, 16) == "place/blackstone/") {
    write("The " + target + " combines ancient Blackstone wardwork with later repairs. Close study distinguishes the precise original tooling from the rough measures used after the wards began to fail.\n");
  } else {
    write("The " + target + " rewards a closer look. Wear, weathering, and local workmanship tie it specifically to " + place_name + " and the travelers who use this place.\n");
  }
  return 1;
}
