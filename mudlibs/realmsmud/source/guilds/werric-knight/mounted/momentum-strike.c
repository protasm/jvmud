//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Momentum Strike");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique teaches you to maximize "
        "the momentum of your mount in devastating charges.");

    addPrerequisite("/guilds/werric-knight/mounted/devastating-charge.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":15]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Charge Attack": 25,
        "Devastating Charge": 25,
        "Trampling Charge": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
