//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Death Incarnate");
    addSpecification("source", "druid");
    addSpecification("description", "This research channels the raw essence of predation through the panther, every movement a perfect expression of lethal intent.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 67
    ]));

    addPrerequisite("/guilds/druid/companions/cat/perfect-killer.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/cat/root.c",
    }));
}
