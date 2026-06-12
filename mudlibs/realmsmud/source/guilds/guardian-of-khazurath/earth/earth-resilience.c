//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Earth Resilience");
    addSpecification("source", "Guardian of Khazurath");
    addSpecification("description", "This skill provides the user with the "
        "knowledge of drawing the resilience of the earth into the body, "
        "providing improved soak and constitution.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "Guardian of Khazurath",
            "value": 17
        ]));
    addPrerequisite("/guilds/guardian-of-khazurath/earth/stone-skin-passive.c",
        (["type": "research"]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus soak", 2);
    addSpecification("bonus constitution", 1);
}
