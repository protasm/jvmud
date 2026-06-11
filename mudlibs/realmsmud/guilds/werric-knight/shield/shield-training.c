//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Shield Training");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have begun mastering the fundamentals of "
        "shield work. Weight distribution, angle management, impact absorption - these "
        "basics form the foundation upon which all defensive mastery is built.");

    addPrerequisite("guilds/werric-knight/shield/root.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":1]));
    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus shield", 2);
    addSpecification("bonus parry", 1);
    addSpecification("bonus defense", 2);
}
