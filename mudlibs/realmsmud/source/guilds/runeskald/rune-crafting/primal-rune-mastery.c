//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Primal Rune Mastery");
    addSpecification("source", "runeskald");
    addSpecification("description", "The ultimate mastery of primal rune "
        "craft, granting the runeskald the ability to create the most "
        "powerful rune-inscribed weapons and armor in the world.");

    addPrerequisite("level",
        (["type": "level", "guild": "runeskald", "value": 28]));
    addPrerequisite("/guilds/runeskald/rune-crafting/primal-rune-lore.c",
        (["type": "research"]));
    addPrerequisite("/guilds/runeskald/rune-crafting/primal-blade-rune.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus gem crafting", 6);
    addSpecification("bonus spellcraft", 5);
    addSpecification("bonus intelligence", 3);
    addSpecification("bonus local history", 3);
}
