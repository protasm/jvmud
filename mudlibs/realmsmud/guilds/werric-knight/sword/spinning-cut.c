//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Spinning Cut");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique incorporates spinning motions "
        "that add momentum and power to your blade techniques.");

    addPrerequisite("/guilds/werric-knight/sword/crossed-swords.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":9]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Whirling Blade": 25,
        "Crossed Swords": 25,
        "Counterattack": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
