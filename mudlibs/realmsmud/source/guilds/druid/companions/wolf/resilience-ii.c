//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Resilience II");
    addSpecification("source", "druid");
    addSpecification("description", "This research strengthens the wolf's constitution, allowing it to endure punishment that would fell lesser beasts.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 11
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/resilience-i.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
