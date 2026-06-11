//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Primal Flesh");
    addSpecification("source", "druid");
    addSpecification("description", "This research permanently hardens the druid's body through sustained primal channeling.");

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 35
    ]));

    addPrerequisite("/guilds/druid/wild-shape/serpents-patience.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus constitution", 2);
    addSpecification("bonus strength", 2);
    addSpecification("bonus hit points", 75);
}
