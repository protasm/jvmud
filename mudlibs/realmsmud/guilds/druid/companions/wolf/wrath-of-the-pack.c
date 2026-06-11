//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Wrath of the Pack");
    addSpecification("source", "druid");
    addSpecification("description", "This research awakens the collective fury of every wolf that has ever hunted, channeling ancestral power through the companion.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 67
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/primal-fury.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
