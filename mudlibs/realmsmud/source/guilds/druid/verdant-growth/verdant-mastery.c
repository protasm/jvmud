//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Verdant Mastery");
    addSpecification("source", "druid");
    addSpecification("description", "This research improves the druid's ability to channel verdant growth energy.");

    addPrerequisite("/guilds/druid/verdant-growth/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 5
    ]));

    addPrerequisite("/guilds/druid/verdant-growth/growth-lore.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Barkskin": 25,
        "Regeneration": 25,
        "Call of the Wilds": 25,
        "Entangle": 25,
        "Thornwall": 25,
        "Natures Grasp": 25,
        "Living Wood": 25,
        "Verdant Bloom": 25,
        "Sylvan Surge": 25,
        "Nature's Wrath Strike": 25,
        "Overgrowth": 25,
        "Ancient Growth": 25,
        "Primal Bloom": 25,
    ]));
}
