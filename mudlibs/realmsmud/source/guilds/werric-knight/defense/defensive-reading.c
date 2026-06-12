//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Defensive Reading");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have learned to read your opponent's "
        "attacks before they fully develop, allowing you to position your defenses "
        "more effectively. This anticipation is the key to advanced defensive tactics.");

    addPrerequisite("guilds/werric-knight/defense/positional-awareness.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":3]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus defense", 2);
    addSpecification("bonus parry", 1);
    addSpecification("bonus perception", 2);
}
