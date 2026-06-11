//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Apex Predator");
    addSpecification("source", "druid");
    addSpecification("description", "This research embodies the apex predator in the druid's wild shape practice.");

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 27
    ]));

    addPrerequisite("/guilds/druid/wild-shape/primal-surge.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Wolf Aspect": 25,
        "Bear Aspect": 25,
        "Cat Aspect": 25,
        "Eagle Aspect": 25,
        "Primal Fury": 25,
        "Serpent Aspect": 25,
        "Avatar of the Wild": 25,
    ]));
}
