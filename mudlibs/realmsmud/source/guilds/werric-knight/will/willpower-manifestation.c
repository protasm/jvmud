//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Willpower Manifestation");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This technique teaches you to manifest "
        "your willpower as a tangible force.");

    addPrerequisite("/guilds/werric-knight/will/force-of-will.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":21]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Force of Will": 25,
        "Shatter Will": 25,
    ]));
    addSpecification("affected research type", "percentage");
}
