//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Enhanced Elements");
    addSpecification("source", "ranger");
    addSpecification("description", "This skill provides the ranger "
        "with the knowledge of warden-enhanced elemental power.");

    addPrerequisite("/guilds/ranger/wardens-call/wardens-insight.c",
        (["type": "research"]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "ranger",
            "value": 13
        ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus spellcraft", 3);
    addSpecification("bonus evocation", 3);
}
