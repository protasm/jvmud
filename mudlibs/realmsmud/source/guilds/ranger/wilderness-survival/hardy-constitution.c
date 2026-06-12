//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Hardy Constitution");
    addSpecification("source", "ranger");
    addSpecification("description", "This skill provides the ranger "
        "with the knowledge of toughening through exposure.");

    addPrerequisite("/guilds/ranger/wilderness-survival/root.c",
        (["type": "research"]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "ranger",
            "value": 1
        ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus constitution", 1);
    addSpecification("bonus defense", 1);
}
