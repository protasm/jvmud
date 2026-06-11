//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Tempest Lore");
    addSpecification("source", "druid");
    addSpecification("description", "This research deepens knowledge of tempest and cyclone techniques.");

    addPrerequisite("/guilds/druid/primal-storm/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 15
    ]));

    addPrerequisite("/guilds/druid/primal-storm/frost-knowledge.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Gust": 25,
        "Ice Shard": 25,
        "Sleet": 25,
        "Frost Nova": 25,
        "Hailstorm": 25,
        "Arctic Gale": 25,
        "Blizzard": 25,
        "Cyclone": 25,
        "Tempest": 25,
        "Frozen Storm": 25,
    ]));
}
