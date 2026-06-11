//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Aura of Protection");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "Your virtuous presence creates an aura "
        "that protects nearby allies, strengthening their defenses.");

    addPrerequisite("/guilds/werric-knight/virtues/strike-of-justice.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":7]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus charisma", 2);
    addSpecification("bonus defense", 2);
    addSpecification("bonus wisdom", 1);
}
