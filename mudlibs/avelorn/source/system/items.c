void initialize(mixed first_load) {
}

object create(string blueprint_id) {
  object item;

  item = jvmud_clone_lpc_object("item/item");
  if (blueprint_id == "weapon/crown-arming-sword") {
    configure_item(
        item,
        blueprint_id,
        "Crown arming sword",
        "sword",
        "A practical double-edged sword stamped with the Crown's assay mark.",
        "weapon",
        6,
        160,
        3,
        "strength",
        12,
        0);
  } else if (blueprint_id == "weapon/ashwood-shortbow") {
    configure_item(
        item,
        blueprint_id,
        "ashwood shortbow",
        "bow",
        "A compact roadwarden's bow made from layered ash and horn.",
        "weapon",
        4,
        145,
        1,
        "dexterity",
        11,
        0);
  } else if (blueprint_id == "weapon/oak-focus-staff") {
    configure_item(
        item,
        blueprint_id,
        "oak focus staff",
        "staff",
        "Silver wire winds around a Collegium-cut crystal at the staff's head.",
        "weapon",
        5,
        150,
        1,
        "intelligence",
        11,
        0);
  } else if (blueprint_id == "weapon/temple-mace") {
    configure_item(
        item,
        blueprint_id,
        "temple mace",
        "mace",
        "Seven small lamps are engraved around this sturdy steel mace.",
        "weapon",
        6,
        150,
        1,
        "wisdom",
        11,
        0);
  } else if (blueprint_id == "armor/travel-cloak") {
    configure_item(
        item,
        blueprint_id,
        "blue wool travel cloak",
        "cloak",
        "A warm Company cloak with a small brass lantern clasp.",
        "body",
        3,
        80,
        1,
        "constitution",
        10,
        0);
  } else if (blueprint_id == "consumable/healing-draught") {
    configure_item(
        item,
        blueprint_id,
        "minor healing draught",
        "draught",
        "A stoppered clay vial of Sister Elara's sharp-scented red cordial.",
        "none",
        1,
        35,
        1,
        "none",
        0,
        20);
  } else if (blueprint_id == "consumable/field-bread") {
    configure_item(
        item,
        blueprint_id,
        "loaf of field bread",
        "bread",
        "A dense oat-and-honey loaf baked to travel well on the royal roads.",
        "none",
        1,
        12,
        1,
        "none",
        0,
        5);
  } else {
    jvmud_destroy_lpc_object(item);
    return 0;
  }
  return item;
}

void configure_item(
    object item,
    string id,
    string name,
    string identity,
    string description,
    string slot,
    int weight,
    int value,
    int level,
    string stat,
    int minimum,
    int healing) {
  jvmud_invoke_lpc_object(
      item,
      "configure",
      id,
      name,
      identity,
      description,
      slot,
      weight,
      value,
      level,
      stat,
      minimum,
      healing);
}
