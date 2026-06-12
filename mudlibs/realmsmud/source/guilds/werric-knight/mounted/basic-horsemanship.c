//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Basic Horsemanship");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have learned the fundamentals of "
        "horsemanship necessary for mounted combat. This includes balance, "
        "control, and coordination with your mount.");

    addPrerequisite("guilds/werric-knight/mounted/root.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":1]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus riding", 3);
    addSpecification("bonus dexterity", 1);
}
