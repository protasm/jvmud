//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Primal Dominance");
    addSpecification("source", "druid");
    addSpecification("description", "This research awakens the bear's apex status, granting it commanding strength that grows with each blow it lands.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 43
    ]));

    addPrerequisite("/guilds/druid/companions/bear/might-v.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
