//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Fortress Stance");
    addSpecification("source", "druid");
    addSpecification("description", "This research makes the bear an immovable guardian, its defenses impenetrable when it plants itself in combat.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 63
    ]));

    addPrerequisite("/guilds/druid/companions/bear/stone-skin.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
