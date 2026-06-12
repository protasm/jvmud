//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Pack Instinct III");
    addSpecification("source", "druid");
    addSpecification("description", "This research perfects the wolf's defensive awareness, granting it an almost supernatural ability to deflect incoming attacks.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 21
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/pack-instinct-ii.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
