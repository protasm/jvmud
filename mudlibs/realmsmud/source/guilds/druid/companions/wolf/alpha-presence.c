//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Alpha Presence");
    addSpecification("source", "druid");
    addSpecification("description", "This research awakens the spirit of the alpha within the wolf, granting it commanding strength and ferocity.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 27
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/ferocity-iii.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
