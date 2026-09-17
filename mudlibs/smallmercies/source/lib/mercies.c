// Mudlib functions: ordinary typed LPC, available throughout Small Mercies.
// Keep these helpers stateless: every caller shares this one object.

// Roll count dice with sides faces each. Invalid requests return zero.
int roll_dice(int count, int sides) {
  int total;
  int i;

  if (count < 1 || count > 100 || sides < 1 || sides > 1000) return 0;

  total = 0;

  for (i = 0; i < count; i = i + 1) {
    total = total + 1 + jvmud_random(sides);
  }

  return total;
}

// Turn shared combat numbers into prose for players and villagers alike.
string health_description(int health, int maximum) {
  if (maximum <= 0) return "blissfully above the fray";

  if (health <= 0) return "ready for a restorative cup of tea";

  if (health >= maximum) return "in excellent spirits";

  if (health > maximum / 2) return "a little ruffled";

  return "heroically in need of a sit-down";
}

// Name the source explicitly so delivery never depends on an implicit actor.
// Deliver to everyone at its location, including the source when connected.
void announce_near(object source, string message) {
  object place;

  if (!source) return;

  place = jvmud_entity_location(source);

  if (place) jvmud_emit_perceivable_at(place, message + "\n");
}

// A small mercy: roll twice and keep the lower result, favoring dodge checks.
// Game policy has its own name; jvmud_random always means the engine primitive.
int mercy_random(int limit) {
  int first;
  int second;

  if (limit <= 0) return 0;

  first = jvmud_random(limit);

  second = jvmud_random(limit);

  if (first < second) return first;

  return second;
}
