//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Lightning Body");
    addSpecification("source", "druid");
    addSpecification("description", "This research charges the druid's body with a faint current of lightning.");

    addPrerequisite("/guilds/druid/primal-storm/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 17
    ]));

    addPrerequisite("/guilds/druid/primal-storm/storm-born.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus dexterity", 1);
    addSpecification("bonus wisdom", 1);
    addSpecification("bonus spell points", 40);
}
