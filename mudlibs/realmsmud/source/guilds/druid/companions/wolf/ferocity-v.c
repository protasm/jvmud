//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Ferocity V");
    addSpecification("source", "druid");
    addSpecification("description", "This research drives the wolf into a state of relentless aggression, each attack building upon the last in a torrent of primal violence.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 47
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/ferocity-iv.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
