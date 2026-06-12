//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Talon V");
    addSpecification("source", "druid");
    addSpecification("description", "This research drives the eagle's offensive capability to its apex, each strike a precise and devastating expression of predatory power.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 37
    ]));

    addPrerequisite("/guilds/druid/companions/eagle/screaming-dive.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/eagle/root.c",
    }));
}
