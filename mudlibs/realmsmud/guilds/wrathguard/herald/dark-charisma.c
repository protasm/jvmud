//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

protected void Setup()
{
    addSpecification("name", "dark charisma");
    addSpecification("source", "Wrathguard");
    addSpecification("description", "Seilyndria's divine authority flows "
        "through the Herald, lending a terrible, compelling presence.");
    addSpecification("research type", "points");
    addSpecification("scope", "self");
    addSpecification("research cost", 1);
    addSpecification("bonus charisma", 4);
    addSpecification("bonus shadow attack", 2);
    addPrerequisite("/guilds/wrathguard/herald/shadow-power.c",
        (["type": "research"]));
    addPrerequisite("level",
        ([
            "type": "level",
            "guild": "Wrathguard",
            "value": 32
        ]));
}
