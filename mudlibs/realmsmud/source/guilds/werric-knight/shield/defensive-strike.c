//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Defensive Strike");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique enhances your ability to "
        "strike effectively while maintaining a defensive posture.");

    addPrerequisite("/guilds/werric-knight/shield/counter-block.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":7]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Shield Bash": 25,
        "Counter Block": 25,
        "Crushing Blow": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
