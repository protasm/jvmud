//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Holy Light");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You channel divine light through your being, "
        "granting bonuses to your faith-based abilities.");

    addPrerequisite("/guilds/werric-knight/light/root.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":1]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus wisdom", 2);
    addSpecification("bonus charisma", 1);
    addSpecification("bonus magical essence", 2);
}
