//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Counter Mastery");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique perfects your counter-attack "
        "abilities, allowing devastating responses to enemy strikes.");

    addPrerequisite("/guilds/werric-knight/shield/devastating-counter.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":13]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Counter Block": 25,
        "Devastating Counter": 25,
        "Retribution Strike": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
