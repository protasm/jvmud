void initialize(mixed first_load) {
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", "counter");
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", "outfitter");
}

void offer_interactions() {
  jvmud_add_action("list_stock", "list");
  jvmud_add_action("buy", "buy");
  jvmud_add_action("sell", "sell");
}

int id(mixed value) {
  return value == "counter" || value == "shop" || value == "outfitter";
}

string short() {
  return "Brindleford outfitter's counter";
}

void describe(object viewer) {
  jvmud_write("The outfitter's oak counter bears the royal scales-and-measures seal. ");
  jvmud_write("Goods are plainly priced in Crown coin, and a posted charter promises ");
  jvmud_write("half value for serviceable equipment sold back to the shop.\n");
  list_stock(0);
}

int list_stock(mixed ignored) {
  jvmud_write("Brindleford Outfitter stock:\n");
  jvmud_write("  bread     1 silver, 2 copper\n");
  jvmud_write("  draught   3 silver, 5 copper\n");
  jvmud_write("  cloak     8 silver\n");
  jvmud_write("  sword     1 gold, 6 silver  (recommended level 3)\n");
  return 1;
}

int buy(mixed target) {
  string blueprint;

  blueprint = blueprint_for(target);
  if (!blueprint) {
    jvmud_write("The outfitter does not stock that. Try: list.\n");
    return 1;
  }
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "purchase_blueprint",
      blueprint);
}

int sell(mixed target) {
  if (!target) {
    jvmud_write("Sell what?\n");
    return 1;
  }
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "sell_item", target);
}

string blueprint_for(mixed target) {
  string normalized;

  if (!target) {
    return 0;
  }
  normalized = jvmud_lowercase_text(target);
  if (normalized == "bread" || normalized == "field bread") {
    return "consumable/field-bread";
  }
  if (normalized == "draught" || normalized == "healing draught") {
    return "consumable/healing-draught";
  }
  if (normalized == "cloak" || normalized == "travel cloak") {
    return "armor/travel-cloak";
  }
  if (normalized == "sword" || normalized == "arming sword") {
    return "weapon/crown-arming-sword";
  }
  return 0;
}
