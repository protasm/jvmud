//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Swift Hunter");
    addSpecification("source", "ranger");
    addSpecification("description", "This skill provides the ranger "
        "with the knowledge of hunting with supernatural speed.");

    addPrerequisite("/guilds/ranger/wild-hunt/blood-scent.c",
        (["type": "research"]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "ranger",
            "value": 17
        ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus dexterity", 2);
    addSpecification("bonus attack", 3);
}
