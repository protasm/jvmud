//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Shield Impact");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique teaches you to maximize the "
        "impact force of shield-based strikes.");

    addPrerequisite("/guilds/werric-knight/shield/shield-bash.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":5]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Shield Bash": 25,
        "Crushing Blow": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
