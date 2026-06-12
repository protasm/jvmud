//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Shadow of the Wild");
    addSpecification("source", "druid");
    addSpecification("description", "This research makes the panther a living embodiment of shadow and predation, moving through combat as an extension of darkness itself.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 63
    ]));

    addPrerequisite("/guilds/druid/companions/cat/phantom-form.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/cat/root.c",
    }));
}
