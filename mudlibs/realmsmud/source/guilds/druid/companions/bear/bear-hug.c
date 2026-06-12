//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Bear Hug");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the bear a powerful grappling ability, allowing it to seize and crush its foes.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 31
    ]));

    addPrerequisite("/guilds/druid/companions/bear/might-iv.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
