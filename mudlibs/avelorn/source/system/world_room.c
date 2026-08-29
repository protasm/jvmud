int room_index;
int x;
int y;
int region_x;
int region_y;
string room_name;
string area_name;
string terrain;
string feature;
string aspect;

void initialize(mixed first_load) {
  room_index = -1;
  room_name = "Unformed Reach";
  area_name = "Uncharted Avelorn";
  terrain = "reach";
  feature = "trail";
  aspect = "weathered";
}

void configure(int index) {
  room_index = index;
  x = index % 253;
  y = index / 253;
  region_x = x / 23;
  region_y = y / 20;
  area_name = choose_area_name();
  terrain = choose_terrain();
  feature = choose_feature();
  aspect = choose_aspect();
  room_name = choose_room_name();
  populate();
}

void offer_interactions() {
  if (y > 0) { jvmud_add_action("north", "north"); jvmud_add_action("north", "n"); }
  if (x < 252) { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
  if (y < 394 && room_index != 0) { jvmud_add_action("south", "south"); jvmud_add_action("south", "s"); }
  if (x > 0) { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
  if (room_index == 0) { jvmud_add_action("old_west", "south"); jvmud_add_action("old_west", "s"); }
  if ((in_gloamhold() || in_deep_concord()) && y % 20 == 4) {
    jvmud_add_action("down", "down"); jvmud_add_action("down", "d");
  }
  if ((in_gloamhold() || in_deep_concord()) && y % 20 == 15) {
    jvmud_add_action("up", "up"); jvmud_add_action("up", "u");
  }
}

string short() { return room_name; }
string query_area() { return area_name; }
int query_world_index() { return room_index; }

string query_brief_exits() {
  string exits;
  exits = "";
  if (y > 0) { exits = append_exit(exits, "n"); }
  if (x < 252) { exits = append_exit(exits, "e"); }
  if (y < 394 || room_index == 0) { exits = append_exit(exits, "s"); }
  if (x > 0) { exits = append_exit(exits, "w"); }
  if ((in_gloamhold() || in_deep_concord()) && y % 20 == 15) { exits = append_exit(exits, "u"); }
  if ((in_gloamhold() || in_deep_concord()) && y % 20 == 4) { exits = append_exit(exits, "d"); }
  return exits;
}

string append_exit(string exits, string direction) {
  if (jvmud_size(exits) == 0) { return direction; }
  return exits + " " + direction;
}

void describe(object viewer) {
  object entity;

  write(room_name + "\n");
  write(room_description() + "\n\n");
  write(exit_description() + "\n");
  entity = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (entity) {
    if (entity != viewer && jvmud_method_exists("short", entity)) {
      write(jvmud_invoke_lpc_object(entity, "short") + " is here.\n");
    }
    entity = jvmud_next_entity_at(entity);
  }
}

int examine_detail(mixed value) {
  string target;

  if (!value) { return 0; }
  target = jvmud_lowercase_text(value);
  if (target == terrain || target == "ground" || target == "land" || target == "stonework") {
    write(terrain_detail() + "\n");
    return 1;
  }
  if (target == feature || target == aspect + " " + feature
      || target == "landmark" || target == "feature") {
    write(feature_detail() + "\n");
    return 1;
  }
  if (target == "road" || target == "trail" || target == "path" || target == "street") {
    write(path_detail() + "\n");
    return 1;
  }
  if (target == "sky" || target == "weather") {
    write("High weather crosses Avelorn in long processions of cloud and clean light; smoke, mist, or sea haze gives bearings toward the next region.\n");
    return 1;
  }
  if (in_gloamhold()) { return examine_gloamhold(target); }
  if (in_deep_concord()) { return examine_concord(target); }
  return 0;
}

int north(mixed ignored) { return travel("north", room_index - 253); }
int east(mixed ignored) { return travel("east", room_index + 1); }
int south(mixed ignored) { return travel("south", room_index + 253); }
int west(mixed ignored) { return travel("west", room_index - 1); }

int old_west(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(), "travel_to", "south", "place/ashenwatch/crown_lantern");
}

int up(mixed ignored) { return travel("up", room_index - 11 * 253); }
int down(mixed ignored) { return travel("down", room_index + 11 * 253); }

int travel(string direction, int destination) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(), "travel_to", direction, world_path(destination));
}

string world_path(int index) {
  return "place/world/r" + jvmud_format_text("%05d", index);
}

string choose_area_name() {
  string *first;
  string *second;

  if (in_crownspire()) { return crownspire_area(); }
  if (in_gloamhold()) { return gloamhold_area(); }
  if (in_deep_concord()) { return concord_area(); }
  if (region_x >= 8 && region_y <= 4) { return "Irongate " + district_word(); }
  if (region_x <= 2 && region_y >= 14) { return "Saltmere " + district_word(); }
  if (region_x <= 2 && region_y >= 5 && region_y <= 10) { return "Elderwild " + wild_word(); }
  if (region_x >= 8 && region_y >= 6 && region_y <= 11) { return "Dawn Coast " + wild_word(); }
  first = ({ "Amber", "Birch", "Cloud", "Dun", "Elder", "Fox", "Green", "Heron", "Iron", "Juniper", "Kings" });
  second = ({ "March", "Vale", "Reach", "Downs", "Weald", "Fells", "Moor", "Hollows", "Fields", "Waste", "Highlands", "Fen", "Wood", "Coast", "Heath", "Basin", "Ridge", "Wilds", "Plain", "Border" });
  return first[region_x] + " " + second[region_y];
}

string crownspire_area() {
  string *districts;
  districts = ({ "South Commons", "South Sewers", "River Quays", "Canalworks", "Artisans Ward", "Foundry Sewers", "Old Market", "Guild Vaults", "Temple Ward", "Lantern Catacombs", "Scholars Ward", "Archive Tunnels", "High Borough", "Palace Gardens", "Royal Castle Lower", "Royal Castle Upper", "Citadel Ward", "Citadel Barracks", "North Borough", "North Sewers", "Garden Ward", "Glasshouse Quarter", "Eastgate Ward", "Westgate Ward" });
  return "Crownspire " + districts[(region_y - 7) * 4 + region_x - 4];
}

string gloamhold_area() {
  string *layers;
  layers = ({ "Ruined Demesne", "Outer Works", "Lower Castle", "Hollow Courts", "Upper Keep", "Broken Towers", "Servants Labyrinth", "Undercroft", "Prison Levels", "Buried Palace", "Flooded Vaults", "Moonless Grotto" });
  return "Gloamhold: " + layers[((region_y - 13) * 3 + region_x - 7) % 12];
}

string concord_area() {
  string *strata;
  string *districts;
  strata = ({ "Z-1 Envoy Galleries", "Z-2 Makers Tier", "Z-3 Civic and Archive Tier", "Z-4 Noble and Temple Deeps", "Z-5 Sovereign River City", "Z-6 Founding Chasm" });
  districts = ({ "Western Galleries", "Central Descent", "Eastern Galleries" });
  return "Deep Concord: " + strata[region_y - 13] + ", " + districts[region_x - 3];
}

string district_word() {
  string *words;
  words = ({ "Gate Ward", "Foundry Ward", "Old Borough", "Market Ward", "Citadel", "Dock Ward", "Temple Ward", "Commons" });
  return words[(region_x + region_y) % 8];
}

string wild_word() {
  string *words;
  words = ({ "Deepwood", "Greenways", "Old Growth", "Moss March", "Hidden Vale", "Hunter Paths" });
  return words[(region_x + region_y) % 6];
}

string choose_terrain() {
  string *terrains;
  if (in_crownspire_underways()) { return "sewer channel"; }
  if (in_crownspire_castle()) { return "castle passage"; }
  if (in_crownspire()) { return "street"; }
  if (in_gloamhold() || in_deep_concord()) { return "stonework"; }
  if (region_x <= 2 && region_y >= 5 && region_y <= 10) { return "forest"; }
  if (region_x <= 2 && region_y >= 14) { return "marsh"; }
  if (region_x >= 8 && region_y >= 6 && region_y <= 11) { return "coast"; }
  terrains = ({ "meadow", "forest", "heath", "ridge", "marsh", "farmland", "moor", "riverbank" });
  return terrains[(region_x * 3 + region_y * 5) % 8];
}

string choose_feature() {
  string *features;
  if (in_gloamhold()) {
    features = ({ "fallen arch", "ward mosaic", "sealed door", "dry fountain", "shattered stair", "memorial carving", "iron grille", "black pool", "bell rope", "mirror frame", "empty cradle", "oath dais", "collapsed gallery", "servants door", "drowned ledger", "lantern niche", "burial recess", "siege stair", "astronomer's mark", "grotto shelf" });
  } else if (in_deep_concord()) {
    features = ({ "depth seal", "lift shaft", "echo market", "rank stair", "canal lock", "crystal garden", "ancestor niche", "riser mark", "guild wheel", "public ledger", "resonance arch", "flood bell", "boat stair", "fungus court", "ore exchange", "assembly vault", "prayer well", "survey cut", "equal hall", "founding tablet" });
  } else if (in_crownspire()) {
    features = ({ "street shrine", "canal bridge", "public cistern", "guild sign", "watch lantern", "carved milestone", "market arcade", "garden wall", "sewer grille", "castle postern", "palace stair", "stable arch", "court fountain", "archive door", "barracks gate", "quay crane", "bread market", "clock tower", "covered passage", "ward office" });
  } else {
    features = ({ "waystone", "ruined croft", "watch mound", "pilgrim shrine", "abandoned camp", "old well", "standing stone", "hunter blind", "collapsed tower", "weathered bridge", "forest gate", "sinkhole", "reed island", "burned barn", "hill fort", "mine mouth", "ferry stair", "hermit cell", "barrow door", "signal cairn" });
  }
  return features[y % 20];
}

string choose_aspect() {
  string *aspects;
  aspects = ({ "ancient", "ash-streaked", "briar-bound", "copper-marked", "deep-cut", "eastward", "fern-grown", "grey", "heron-carved", "ivy-clad", "juniper-shadowed", "king's", "lichened", "mossed", "north-facing", "oath-marked", "rain-dark", "silvered", "thorn-ringed", "upland", "vale-facing", "weathered", "yew-shaded" });
  return aspects[x % 23];
}

string choose_room_name() {
  if (x % 23 == 11 || y % 20 == 10) { return area_name + " Royal Road - " + jvmud_capitalize_text(aspect + " " + feature); }
  return area_name + " - " + jvmud_capitalize_text(aspect + " " + feature);
}

string street_name() {
  string *names;
  names = ({ "Bell Street", "Candle Lane", "Heron Street", "King's Walk", "Mason Row", "Rainmarket Lane", "Silver Street", "Wardens Way" });
  return names[(x + y) % 8];
}

string room_description() {
  if (in_gloamhold()) { return gloamhold_description(); }
  if (in_deep_concord()) { return concord_description(); }
  if (in_crownspire_underways()) {
    return "A navigable undercity passage crosses " + area_name + ". A " + aspect + " " + feature + " stands beside brick drains, flood doors, maintenance walks, forgotten cellars, and older masonry beneath the capital.";
  }
  if (in_crownspire_castle()) {
    return "This " + terrain + " belongs to " + area_name + ". A " + aspect + " " + feature + " divides royal apartments, kitchens, guard floors, audience chambers, service stairs, gardens, and the Crown's administrative maze.";
  }
  if (in_crownspire()) {
    return "A maintained city street crosses " + area_name + ". Permanent paving and a " + aspect + " " + feature + " give this block its character; doorways, courts, canals, shops, dwellings, and service passages spread beyond the ceremonial avenues.";
  }
  return "The " + terrain + " of " + area_name + " stretches around a " + feature + ". A traversable way continues through changing ground, with distant roofs, smoke, water, or high ridges offering bearings across the wider realm.";
}

string gloamhold_description() {
  string *story;
  story = ({
    "Gloamhold's abandoned approaches retain the geometry of orchards and siege lines. No army destroyed the castle; its own household barred the gates during the Long Vigil.",
    "The outer works are split by roots and rain. Chiseled lanterns were deliberately turned face-down, the first sign of the oath that consumed the garrison.",
    "Lower halls descend through kitchens, armories, guest courts, and roofless galleries. Empty settings show that the inhabitants expected to return after one final ceremony.",
    "Hollow courts carry whispers between disconnected windows. Regent Maelin ordered every bell silenced when the astronomer-priests announced that something beneath the hill had answered them.",
    "The upper keep preserves maps, nurseries, council rooms, and sealed apartments. Scratched marginalia names the thing below only as the Listener in the Water.",
    "Broken towers lean over impossible drops. Signal mirrors aimed inward show how the defenders tried to imprison a light rising through the castle rather than repel an enemy outside.",
    "A servants' labyrinth links hidden stairs, laundries, warming passages, and forgotten chapels. Records here contradict the official account of an orderly evacuation.",
    "The undercroft predates Gloamhold. Its pillars surround a buried royal road and doors carrying the erased arms of a kingdom that preceded Avelorn.",
    "Prison levels descend past cells, guard posts, and an execution chapel. The final prisoners were castle officers who refused the regent's midnight oath.",
    "A buried palace lies beneath the dungeon, built around luminous mineral seams. Here astronomers mistook natural resonance for prophecy and taught it human speech.",
    "Flooded vaults preserve drowned archives and mechanisms that regulated the water below. Their failure let the Listener's voice reach every cistern in the keep.",
    "The moonless grotto surrounds black water and pale crystal. The Listener is an echo made sentient by generations of vows, secrets, and fear."
  });
  return story[((region_y - 13) * 3 + region_x - 7) % 12] + " A " + feature + " marks this part of the complex.";
}

string concord_description() {
  string *history;
  history = ({
    "Envoy galleries nearest the surface are deliberately modest: in the Deep Concord, proximity to daylight signals low ceremonial rank and public obligation.",
    "The makers' tier rings with mills, kilns, lifts, and workshops. Skilled guilds own their labor but petition downward when civic law touches deeper privilege.",
    "The civic tier contains courts, schools, clinics, markets, and assembly vaults. Addresses include depth because descent is both geography and honor.",
    "Archive vaults preserve contracts on fired leaves. Librarians may descend farther than nobles while carrying sealed knowledge, an exception that unsettles the hierarchy.",
    "Noble deeps occupy pressure-warmed halls below the common city. Rank staircases narrow downward so formal processions must shed attendants as status rises.",
    "Temple deeps surround resonant stone where choirs measure civic promises. Priests claim gravity pulls truth downward and leaves falsehood near the surface.",
    "The sovereign deep houses the Descending Council. Its lowest chair belongs to the First Below, though flood engineers can overrule it during emergencies.",
    "An underground river city moves food and citizens between strata. Boat crews possess unusual freedom because water ignores the ordained stairways.",
    "Crystal farms turn mineral light into pale crops and medicine. Farmers quietly trade upward without the permits demanded by depth law.",
    "Rootward mines extend below official society. Surveyors return with evidence that the oldest tunnels climb toward distant lands rather than descend.",
    "The dissident Rise shelters citizens who reject depth as destiny. They build ramps, communal lifts, and meeting halls where no seat stands lower than another.",
    "At the Founding Chasm, inscriptions reveal that the hierarchy began as an evacuation plan during an ancient surface catastrophe, then hardened into sacred rank."
  });
  return history[(region_y - 13) * 2 + (region_x - 3) % 2] + " An " + aspect + " " + feature + " anchors the surrounding passage.";
}

string terrain_detail() {
  if (in_crownspire()) { return "Royal granite, guild brick, and rain channels record generations of city repair. Sewer access marks are numbered by ward and flood basin."; }
  if (in_gloamhold()) { return "The stone bears an ancient foundation, Gloamhold's rebuilding, and desperate alterations made during the Long Vigil."; }
  if (in_deep_concord()) { return "Depth figures and civic seals are cut into the stone. Lower numbers once marked emergency strata; later generations transformed them into hereditary status."; }
  return "Close study shows wheel ruts, animal passages, drainage, and seasonal growth. This is traveled country rather than an empty backdrop.";
}

string feature_detail() {
  if (in_gloamhold()) { return "The " + feature + " bears a lantern reflected in dark water. Later hands scored through the reflection but spared the flame."; }
  if (in_deep_concord()) { return "The " + feature + " carries both a practical depth measurement and a ceremonial rank seal. The two numbers no longer agree."; }
  if (in_crownspire()) { return "The " + feature + " is maintained by its ward. A maker's mark and dated repair tally reward close examination."; }
  return "The " + feature + " predates the newest roadwork. Tool marks and repairs reveal repeated use by earlier travelers.";
}

string path_detail() {
  if (in_deep_concord()) { return "Public arrows privilege descent; smaller chalk marks left by the dissident Risers identify level routes and forbidden ways upward."; }
  if (x % 23 == 11 || y % 20 == 10) { return "A Crown survey mark identifies the royal road lattice joining the realm's major cities. Smaller tracks leave it for settlements and ruins."; }
  return "The local way bends between nearby landmarks and eventually rejoins a surveyed road. Its wear suggests regular traffic.";
}

string exit_description() {
  string exits;
  exits = "Passages lead";
  if (y > 0) { exits += " north"; }
  if (x < 252) { exits += " east"; }
  if (y < 394 && room_index != 0) { exits += " south"; }
  if (x > 0) { exits += " west"; }
  if (room_index == 0) { exits += " south toward the old western chapter"; }
  if ((in_gloamhold() || in_deep_concord()) && y % 20 == 4) { exits += " down"; }
  if ((in_gloamhold() || in_deep_concord()) && y % 20 == 15) { exits += " up"; }
  return exits + ".";
}

int examine_gloamhold(string target) {
  if (target == "lantern" || target == "reflection" || target == "carving" || target == "mosaic") {
    write("The reflected lantern predates the Crown. Gloamhold's last household believed every oath cast a second, enduring shape into the water below.\n"); return 1;
  }
  if (target == "water" || target == "pool" || target == "listener") {
    write("The darkness offers no face. Sounds return subtly altered, assembled from fragments of old promises; the effect strengthens toward the grotto.\n"); return 1;
  }
  if (target == "walls" || target == "wall" || target == "stone") { write(terrain_detail() + "\n"); return 1; }
  return 0;
}

int examine_concord(string target) {
  if (target == "depth" || target == "rank" || target == "seal" || target == "hierarchy") {
    write("The Z-negative rank system places greater authority at greater physical depth. Practical evacuation levels became social stations over centuries of ritual and inheritance.\n"); return 1;
  }
  if (target == "riser" || target == "risers" || target == "chalk") {
    write("An upward arrow inside an open circle is the dissidents' sign. It marks routes that avoid hereditary checkpoints and places where people meet as equals.\n"); return 1;
  }
  if (target == "walls" || target == "wall" || target == "stone") { write(terrain_detail() + "\n"); return 1; }
  return 0;
}

void populate() {
  object entity;
  if (in_gloamhold() && room_index % 37 == 0) {
    entity = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(entity, "configure", "vow-worn echo", "non-binary", "A human outline assembled from whispered promises moves independently of the reflections around it.", 6, 105, 8, 14, 180, 42);
    jvmud_invoke_lpc_object(entity, "add_identity", "echo");
    jvmud_move_entity(entity, jvmud_current_lpc_object());
  }
  if (in_deep_concord() && room_index % 31 == 0) {
    entity = jvmud_clone_lpc_object("npc/citizen");
    jvmud_invoke_lpc_object(entity, "configure", concord_name(), "non-binary", "Concord citizen", "a depth seal, practical tools, and opinions about the rank stair");
    jvmud_invoke_lpc_object(entity, "add_identity", "citizen");
    jvmud_invoke_lpc_object(entity, "add_identity", "concord citizen");
    jvmud_move_entity(entity, jvmud_current_lpc_object());
  }
  if (in_crownspire() && room_index % 29 == 0) {
    entity = jvmud_clone_lpc_object("npc/citizen");
    jvmud_invoke_lpc_object(entity, "configure", crownspire_name(), "non-binary", "ward resident", "a ward token and a clear destination elsewhere in the capital");
    jvmud_invoke_lpc_object(entity, "add_identity", "resident");
    jvmud_move_entity(entity, jvmud_current_lpc_object());
  }
  if (room_index % 211 == 0) {
    entity = jvmud_clone_lpc_object("npc/citizen");
    jvmud_invoke_lpc_object(entity, "configure", traveler_name(), "non-binary", "wayfarer", "a pack, a route ledger, and news from the next region");
    jvmud_invoke_lpc_object(entity, "add_identity", "wayfarer");
    jvmud_move_entity(entity, jvmud_current_lpc_object());
  }
  if (room_index % 307 == 0) {
    entity = jvmud_invoke_lpc_object("system/items", "create", "consumable/healing-draught");
    if (entity) { jvmud_move_entity(entity, jvmud_current_lpc_object()); }
  }
}

string traveler_name() {
  string *names;
  names = ({ "Aster Fen", "Bren Rowan", "Cerys Vale", "Dain Heron", "Elian Moss", "Fara Wren", "Galen Pike", "Hollis Mere" });
  return names[(room_index / 211) % 8];
}

string concord_name() {
  string *names;
  names = ({ "Adra Below", "Bexil Third", "Caro Liftward", "Deren Flow", "Eris Level", "Fenn Riser" });
  return names[(room_index / 31) % 6];
}

string crownspire_name() {
  string *names;
  names = ({ "Anwen Bell", "Corin Slate", "Eda Crane", "Jory Canal", "Maren Guild", "Tallis Ward" });
  return names[(room_index / 29) % 6];
}

int in_crownspire() { return region_x >= 4 && region_x <= 7 && region_y >= 7 && region_y <= 12; }
int in_crownspire_underways() {
  int district;
  if (!in_crownspire()) { return 0; }
  district = (region_y - 7) * 4 + region_x - 4;
  return district == 1 || district == 5 || district == 7 || district == 9
      || district == 11 || district == 19;
}
int in_crownspire_castle() {
  int district;
  if (!in_crownspire()) { return 0; }
  district = (region_y - 7) * 4 + region_x - 4;
  return district == 13 || district == 14 || district == 15 || district == 16
      || district == 17;
}
int in_gloamhold() { return region_x >= 7 && region_x <= 9 && region_y >= 13 && region_y <= 16; }
int in_deep_concord() { return region_x >= 3 && region_x <= 5 && region_y >= 13 && region_y <= 18; }
