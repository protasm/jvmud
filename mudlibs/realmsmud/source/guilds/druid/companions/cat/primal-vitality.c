//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Primal Vitality");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the panther the restorative power of wild magic, allowing it to recover from wounds with startling speed.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 55
    ]));

    addPrerequisite("/guilds/druid/companions/cat/sinew-vi.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/cat/root.c",
    }));
}
