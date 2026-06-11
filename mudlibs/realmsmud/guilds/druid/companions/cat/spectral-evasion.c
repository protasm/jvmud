//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Spectral Evasion");
    addSpecification("source", "druid");
    addSpecification("description", "This research makes the panther nearly untouchable, its evasive ability approaching the supernatural.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 55
    ]));

    addPrerequisite("/guilds/druid/companions/cat/ghost-form.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/cat/root.c",
    }));
}
