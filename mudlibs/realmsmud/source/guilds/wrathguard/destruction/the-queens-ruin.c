//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";
protected void Setup()
{
    addSpecification("name", "The Queen's Ruin");
    addSpecification("source", "Wrathguard");
    addSpecification("description", "Seilyndria's own essence of ruin "
        "flows through the devoted disciple.");
    addPrerequisite("level",
        ([
            "type": "level",
            "guild": "Wrathguard",
            "value": 29
        ]));
    addPrerequisite("/guilds/wrathguard/destruction/ruin-heart.c",
        (["type": "research"]));
    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus magical attack", 8);
    addSpecification("bonus acid attack", 5);
    addSpecification("bonus spell points", 35);
}