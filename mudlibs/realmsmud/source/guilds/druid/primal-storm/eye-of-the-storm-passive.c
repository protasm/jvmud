//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Eye of the Storm");
    addSpecification("source", "druid");
    addSpecification("description", "This research places the druid at the calm and devastating center of any storm.");

    addPrerequisite("/guilds/druid/primal-storm/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 49
    ]));

    addPrerequisite("/guilds/druid/primal-storm/storm-mastery-passive.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus cold attack", 6);
    addSpecification("bonus air attack", 5);
    addSpecification("bonus intelligence", 2);
    addSpecification("bonus wisdom", 2);
}
