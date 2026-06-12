//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Verdant Vigor");
    addSpecification("source", "druid");
    addSpecification("description", "This research channels constant verdant energy into the druid's stamina reserves.");

    addPrerequisite("/guilds/druid/verdant-growth/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 19
    ]));

    addPrerequisite("/guilds/druid/verdant-growth/wildbloom.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus constitution", 1);
    addSpecification("bonus stamina points", 40);
}
