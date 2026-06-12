//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Might VI");
    addSpecification("source", "druid");
    addSpecification("description", "This research channels the full destructive power of nature through the bear's strikes, each blow a force of the wild.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 47
    ]));

    addPrerequisite("/guilds/druid/companions/bear/primal-dominance.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
