//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Wild Affinity");
    addSpecification("source", "druid");
    addSpecification("description", "This research deepens the druid's connection to the destructive forces of nature, amplifying all nature's wrath spells.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 3
    ]));

    addPrerequisite("/guilds/druid/natures-wrath/root.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Thorn Whip": 25,
        "Stone Fist": 25,
        "Wildfire": 25,
        "Eruption": 25,
        "Magma Surge": 25,
        "Scorched Earth": 25,
        "Earthen Grasp": 25,
        "Conflagration": 25,
        "Earthquake": 25,
        "Volcanic Fury": 25,
    ]));
}
