//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Unstoppable");
    addSpecification("source", "druid");
    addSpecification("description", "This research forges the bear's endurance into something transcendent, allowing it to press through any pain.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 31
    ]));

    addPrerequisite("/guilds/druid/companions/bear/ursine-fortitude.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
