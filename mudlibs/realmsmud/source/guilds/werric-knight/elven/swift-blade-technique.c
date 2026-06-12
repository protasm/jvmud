//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Swift Blade Technique");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique teaches the flowing grace "
        "of swift elven blade techniques.");

    addPrerequisite("/guilds/werric-knight/elven/swift-strike.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":7]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Swift Strike": 25,
        "Dancing Blade": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
