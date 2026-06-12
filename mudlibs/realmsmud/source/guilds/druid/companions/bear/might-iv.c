//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Might IV");
    addSpecification("source", "druid");
    addSpecification("description", "This research pushes the bear's strength toward legendary proportions, its strikes capable of crumbling stone.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 27
    ]));

    addPrerequisite("/guilds/druid/companions/bear/might-iii.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
