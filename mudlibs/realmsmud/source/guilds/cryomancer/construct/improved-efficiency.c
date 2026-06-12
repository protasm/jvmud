//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Improved Efficiency");
    addSpecification("source", "cryomancer");
    addSpecification("description", "This research improves the efficiency of constructed cryomancer spells.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "cryomancer",
            "value": 13
        ]));
    addPrerequisite("/guilds/cryomancer/construct/efficient-casting.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Constructed Cryomancer Spells": 10,
    ]));
    addSpecification("affected research type", "decrease cooldown");
}
