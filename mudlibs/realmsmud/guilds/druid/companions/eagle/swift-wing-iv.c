//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Swift Wing IV");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the eagle the evasive grace of a creature that barely occupies the physical world during combat.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 37
    ]));

    addPrerequisite("/guilds/druid/companions/eagle/sky-dancer.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/eagle/root.c",
    }));
}
