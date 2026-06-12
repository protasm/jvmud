//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Runic Striking");
    addSpecification("source", "runeskald");
    addSpecification("description", "The runeskald's weapon-arm inscriptions "
        "deepen, granting a passive bonus to attack rating that compounds "
        "with the rune of fury.");

    addPrerequisite("/guilds/runeskald/runic-binding/bind-rune-of-fury.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus attack", 4);

}
