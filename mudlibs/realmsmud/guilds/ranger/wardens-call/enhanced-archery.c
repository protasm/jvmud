//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Enhanced Archery");
    addSpecification("source", "ranger");
    addSpecification("description", "This skill provides the ranger "
        "with the knowledge of warden-enhanced bow technique.");

    addPrerequisite("/guilds/ranger/wardens-call/wardens-precision.c",
        (["type": "research"]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "ranger",
            "value": 11
        ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus bow", 3);
    addSpecification("bonus attack", 3);
}
