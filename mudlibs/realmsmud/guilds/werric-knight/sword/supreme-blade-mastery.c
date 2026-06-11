//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Supreme Blade Mastery");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "This represents the pinnacle of sword "
        "technique knowledge, dramatically enhancing all your blade abilities.");

    addPrerequisite("/guilds/werric-knight/sword/legendary-swordsman.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":31]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Blade of Kings": 50,
        "Ultimate Strike": 50,
    ]));
    addSpecification("affected research type", "percentage");
}
