//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Predator IV");
    addSpecification("source", "druid");
    addSpecification("description", "This research pushes the panther's predatory ability past the bounds of the natural, its attacks combining speed and force in perfect measure.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 27
    ]));

    addPrerequisite("/guilds/druid/companions/cat/predator-iii.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/cat/root.c",
    }));
}
