//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Mountain Grip");
    addSpecification("source", "Guardian of Khazurath");
    addSpecification("description", "This skill provides the user with knowledge of a grip as unyielding as a mountain face.");

    addPrerequisite("/guilds/guardian-of-khazurath/strength/crushing-blow.c",
        (["type": "research"]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "Guardian of Khazurath",
            "value": 9
        ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus attack", 3);
    addSpecification("bonus damage", 2);
}
