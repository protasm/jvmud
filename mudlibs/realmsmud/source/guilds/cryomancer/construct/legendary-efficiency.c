//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Legendary Efficiency");
    addSpecification("source", "cryomancer");
    addSpecification("description", "This research achieves legendary efficiency, the pinnacle of casting speed.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "cryomancer",
            "value": 45
        ]));
    addPrerequisite("/guilds/cryomancer/construct/grandmaster-efficiency.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Constructed Cryomancer Spells": 5,
    ]));
    addSpecification("affected research type", "decrease cooldown");
}
