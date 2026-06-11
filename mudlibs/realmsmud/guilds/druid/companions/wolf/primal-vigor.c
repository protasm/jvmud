//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Primal Vigor");
    addSpecification("source", "druid");
    addSpecification("description", "This research channels the regenerative power of the wild into the wolf, granting it the ability to recover from wounds with remarkable speed.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 21
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/savage-bite.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
