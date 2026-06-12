//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Primal Regeneration");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the bear accelerated healing that borders on the supernatural.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 55
    ]));

    addPrerequisite("/guilds/druid/companions/bear/endurance-v.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
