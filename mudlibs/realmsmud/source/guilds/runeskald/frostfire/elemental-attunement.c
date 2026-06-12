//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Elemental Attunement");
    addSpecification("source", "runeskald");
    addSpecification("description", "The runeskald attunes themselves to "
        "both frost and fire simultaneously, improving their resistance "
        "to both elements and sharpening their elemental attacks.");

    addPrerequisite("level",
        (["type": "level", "guild": "runeskald", "value": 6]));
    addPrerequisite("/guilds/runeskald/frostfire/frostfire-lore.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus resist cold", 3);
    addSpecification("bonus resist fire", 3);
    addSpecification("bonus spellcraft", 1);
}
