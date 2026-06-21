//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
virtual inherit "/lib/realizations/npc.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("jerith");
    Gender("male");
    Race("maegenstryd");
    SetUpPersonaOfLevel("swordsman", 1);

    object equipment = clone_object("/lib/instances/items/weapons/swords/long-sword.c");
    // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
    // Original bad LPC: equipment.set("craftsmanship", 25);
    equipment->set("craftsmanship", 25);
    // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
    // Original bad LPC: equipment.set("material", "iron");
    equipment->set("material", "iron");
    move_object(equipment, this_object());
    this_object()->equip(equipment);

    object generator = load_object("/tutorial/characters/aegis-equipment.c");
    // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
    // Original bad LPC: generator.CreateAegisEquipment(this_object());
    generator->CreateAegisEquipment(this_object());
}
