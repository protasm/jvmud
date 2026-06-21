//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************

/////////////////////////////////////////////////////////////////////////////
public void CreateAegisEquipment(object user)
{
    if (objectp(user))
    {
        object equipment = clone_object("/lib/instances/items/armor/medium-armor/chainmail.c");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("craftsmanship", 50);
        equipment->set("craftsmanship", 50);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("name", "aegis chainmail");
        equipment->set("name", "aegis chainmail");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("short", "Aegis Chainmail");
        equipment->set("short", "Aegis Chainmail");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("material", "steel");
        equipment->set("material", "steel");
        move_object(equipment, user);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: user.equip(equipment, 1);
        user->equip(equipment, 1);

        equipment = clone_object("/lib/instances/items/armor/accessories/boots.c");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("craftsmanship", 50);
        equipment->set("craftsmanship", 50);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("armor class", 1);
        equipment->set("armor class", 1);
        move_object(equipment, user);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: user.equip(equipment, 1);
        user->equip(equipment, 1);

        equipment = clone_object("/lib/instances/items/armor/clothing/cloak.c");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("name", "aegis cloak");
        equipment->set("name", "aegis cloak");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("short", "Aegis Cloak");
        equipment->set("short", "Aegis Cloak");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("craftsmanship", 50);
        equipment->set("craftsmanship", 50);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("armor class", 1);
        equipment->set("armor class", 1);
        move_object(equipment, user);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: user.equip(equipment, 1);
        user->equip(equipment, 1);

        equipment = clone_object("/lib/instances/items/armor/light-armor/leather-arm-greaves.c");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("craftsmanship", 50);
        equipment->set("craftsmanship", 50);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("armor class", 1);
        equipment->set("armor class", 1);
        move_object(equipment, user);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: user.equip(equipment, 1);
        user->equip(equipment, 1);

        equipment = clone_object("/lib/instances/items/armor/light-armor/leather-leg-greaves.c");
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("craftsmanship", 50);
        equipment->set("craftsmanship", 50);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: equipment.set("armor class", 1);
        equipment->set("armor class", 1);
        move_object(equipment, user);
        // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
        // Original bad LPC: user.equip(equipment, 1);
        user->equip(equipment, 1);
    }
}
