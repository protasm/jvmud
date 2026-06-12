//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Positional Awareness");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have learned to use positioning as a "
        "defensive tool. By being aware of terrain, angles of attack, and tactical "
        "advantages, you can make yourself significantly harder to hit.");

    addPrerequisite("guilds/werric-knight/defense/root.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":1]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus defense", 2);
    addSpecification("bonus dodge", 1);
    addSpecification("bonus intelligence", 1);
}
