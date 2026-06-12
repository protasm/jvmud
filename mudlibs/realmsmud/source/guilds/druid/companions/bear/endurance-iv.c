//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Endurance IV");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the bear a constitution rivaling the great beasts of legend.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 37
    ]));

    addPrerequisite("/guilds/druid/companions/bear/unstoppable.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
