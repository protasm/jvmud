//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Perfect Positioning");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have achieved perfect positioning - always "
        "occupying the optimal tactical location for defense. This isn't luck but "
        "the culmination of years of studying terrain, angles of attack, and "
        "battlefield geometry.");

    addPrerequisite("guilds/werric-knight/defense/tactical-fortress.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":9]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus defense", 6);
    addSpecification("bonus dodge", 5);
    addSpecification("bonus intelligence", 5);
    addSpecification("bonus wisdom", 4);
}
