//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Pack Instinct V");
    addSpecification("source", "druid");
    addSpecification("description", "This research perfects the wolf's combat awareness to a supernatural degree, anticipating threats before they materialize.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 51
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/thick-hide-iv.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
