//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "World Rune Resilience");
    addSpecification("source", "runeskald");
    addSpecification("description", "The runeskald's deep knowledge of world "
        "runes has made their body nearly indestructible. Grants a large "
        "permanent bonus to soak, hit points, and armor class.");

    addPrerequisite("/guilds/runeskald/world-rune/world-rune-aegis.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus soak", 8);
    addSpecification("bonus armor class", 6);
    addSpecification("bonus hit points", 70);

}
