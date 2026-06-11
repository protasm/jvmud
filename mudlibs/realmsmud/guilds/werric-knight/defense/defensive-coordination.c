//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Defensive Coordination");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You can coordinate defensive efforts with "
        "nearby allies, creating overlapping fields of defense that are stronger "
        "than the sum of their parts. This is teamwork applied to defensive tactics.");

    addPrerequisite("guilds/werric-knight/defense/defensive-reposition.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":11]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus defense", 3);
    addSpecification("bonus parry", 2);
    addSpecification("bonus intelligence", 2);
}
