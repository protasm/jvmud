//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Paragon of Virtue");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have become the very embodiment of the "
        "knightly virtues, an exemplar that all aspire to emulate.");

    addPrerequisite("/guilds/werric-knight/virtues/inspiring-presence.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":15]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus charisma", 4);
    addSpecification("bonus wisdom", 3);
    addSpecification("bonus hit points", 100);
}
