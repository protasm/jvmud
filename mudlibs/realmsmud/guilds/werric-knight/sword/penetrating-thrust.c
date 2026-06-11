//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Penetrating Thrust");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique teaches precise thrusting "
        "attacks that penetrate armor and defenses.");

    addPrerequisite("/guilds/werric-knight/sword/devastating-combo.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":13]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Critical Strike": 25,
        "Devastating Combo": 25,
        "Surgical Strike": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
