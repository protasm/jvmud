//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Terrain Advantage");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have learned to use terrain to your "
        "defensive advantage. High ground, narrow passages, obstacles - all become "
        "tools in your defensive arsenal. A knight who understands terrain is worth "
        "three who don't.");

    addPrerequisite("guilds/werric-knight/defense/tactical-defense.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":7]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus defense", 3);
    addSpecification("bonus dodge", 2);
    addSpecification("bonus intelligence", 2);
}
