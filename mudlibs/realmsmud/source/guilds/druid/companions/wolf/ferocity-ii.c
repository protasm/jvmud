//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Ferocity II");
    addSpecification("source", "druid");
    addSpecification("description", "This research further deepens the primal bond, driving the wolf to strike with greater force and precision.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 11
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/ferocity-i.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
