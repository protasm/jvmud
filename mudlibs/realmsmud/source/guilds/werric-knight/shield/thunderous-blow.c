//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Thunderous Blow");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique magnifies the power of your "
        "most devastating shield strikes.");

    addPrerequisite("/guilds/werric-knight/shield/thunderous-impact.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":19]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Shield Slam": 25,
        "Thunderous Impact": 25,
        "Retribution Strike": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
