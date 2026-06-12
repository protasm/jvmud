//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Ultimate Judgment");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This represents the pinnacle of divine "
        "judgment technique, dramatically enhancing your ultimate holy abilities.");

    addPrerequisite("/guilds/werric-knight/light/pillar-of-light.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":45]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Smite Damned": 50,
        "Pillar of Light": 50,
    ]));
    addSpecification("affected research type", "percentage");
}
