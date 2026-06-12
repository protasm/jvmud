//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Sky Sovereign");
    addSpecification("source", "druid");
    addSpecification("description", "This research establishes the eagle as an unchallenged master of aerial combat, its evasiveness a force of nature.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 63
    ]));

    addPrerequisite("/guilds/druid/companions/eagle/eternal-wind.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/eagle/root.c",
    }));
}
