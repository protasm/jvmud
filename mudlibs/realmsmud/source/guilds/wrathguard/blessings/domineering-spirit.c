//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

protected void Setup()
{
    addSpecification("name", "Domineering Spirit");
    addSpecification("source", "Wrathguard");
    addSpecification("description", "The Wrathguard's spirit grows "
        "commanding and indomitable, raising charisma and spell points.");
    addSpecification("research type", "points");
    addSpecification("scope", "self");
    addSpecification("research cost", 1);
    addSpecification("bonus charisma", 3);
    addSpecification("bonus spell points", 25);
    addPrerequisite("/guilds/wrathguard/blessings/will-mind.c",
        (["type": "research"]));
    addPrerequisite("level",
        (["type": "level", "guild": "Wrathguard", "value": 17]));
}
