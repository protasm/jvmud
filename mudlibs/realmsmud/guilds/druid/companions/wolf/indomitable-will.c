//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Indomitable Will");
    addSpecification("source", "druid");
    addSpecification("description", "This research forges the wolf's spirit into something unbreakable, granting it resistance to magical effects and mental domination.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 59
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/resilience-v.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
