//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Iron Jaw");
    addSpecification("source", "druid");
    addSpecification("description", "This research infuses the wolf's bite with primal power, its jaws capable of rending through armor and bone.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 31
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/alpha-presence.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
