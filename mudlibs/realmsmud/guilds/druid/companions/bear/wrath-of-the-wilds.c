//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Wrath of the Wilds");
    addSpecification("source", "druid");
    addSpecification("description", "This research channels the raw destructive fury of nature through every blow the bear delivers.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 67
    ]));

    addPrerequisite("/guilds/druid/companions/bear/ursine-rampage.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
