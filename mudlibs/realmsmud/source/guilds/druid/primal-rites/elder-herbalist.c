//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Elder Herbalist");
    addSpecification("source", "druid");
    addSpecification("description", "This research represents elder herbalist knowledge, permanently amplifying all ritual effects.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "druid",
            "value": 59
        ]));

    addPrerequisite("/guilds/druid/primal-rites/root.c",
        ([ "type": "research" ]));

    addPrerequisite("/guilds/druid/primal-rites/ritual-mastery.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus wisdom", 2);
    addSpecification("bonus intelligence", 1);
    addSpecification("bonus spell points", 100);
}
