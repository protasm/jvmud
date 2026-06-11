//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Living Volcano");
    addSpecification("source", "druid");
    addSpecification("description", "This research transforms the druid into a living conduit of volcanic power.");

    addPrerequisite("/guilds/druid/natures-wrath/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 49
    ]));

    addPrerequisite("/guilds/druid/natures-wrath/volcanic-mastery-passive.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus fire attack", 6);
    addSpecification("bonus earth attack", 5);
    addSpecification("bonus intelligence", 2);
    addSpecification("bonus constitution", 2);
}
