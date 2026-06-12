//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Subterfuge Mastery II");
    addSpecification("source", "rogue");
    addSpecification("description", "This skill provides the rogue "
        "with enhanced deception and misdirection abilities.");

    addPrerequisite("/guilds/rogue/subterfuge/subterfuge-mastery.c",
        (["type": "research"]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "rogue",
            "value": 35
        ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus hide", 7);
    addSpecification("bonus move silently", 5);
}
