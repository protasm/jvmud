// A defeated opponent rests for twenty seconds; its one challenger owns the bout.
string name;
string prose;
string reply;
int health;
int maximum;
int damage;
int resting;
object challenger;

void configure(string label, string detail, string speech, int hp, int power) {
  name = label;

  prose = detail;

  reply = speech;

  health = hp;

  maximum = hp;

  damage = power;
}

string short() {
  if (resting) return name + " (taking a dignified breather)";

  return name;
}

int id(string value) {
  return value == name;
}

void describe(object viewer) {
  jvmud_write_to_lpc_object(viewer, prose + "\n" + name + " looks " + health_description(health, maximum) + ".\n");
}

string talk() {
  return name + " says: " + reply + "\n";
}

int engage(object actor) {
  if (!maximum || resting) return 0;

  if (challenger && challenger != actor) return 0;

  challenger = actor;

  return 1;
}

void release(object actor) {
  if (challenger == actor) challenger = 0;
}

int attack_power() {
  return damage;
}

int hit(object actor, int amount) {
  if (challenger != actor || resting) return 0;

  health = health - amount;

  if (health > 0) return 0;

  resting = 1;

  challenger = 0;

  jvmud_schedule_deferred_callback("recover", 20);

  return 1;
}

void recover() {
  health = maximum;

  resting = 0;

  announce_near(jvmud_current_lpc_object(), name + " returns to duty, pretending nothing happened.");
}
