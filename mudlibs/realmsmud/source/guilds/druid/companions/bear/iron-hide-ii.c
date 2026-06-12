//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Iron Hide II");
    addSpecification("source", "druid");
    addSpecification("description", "This research further toughens the bear's natural armor, making it resistant to sword and claw alike.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 11
    ]));

    addPrerequisite("/guilds/druid/companions/bear/iron-hide-i.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/bear/root.c",
    }));
}
