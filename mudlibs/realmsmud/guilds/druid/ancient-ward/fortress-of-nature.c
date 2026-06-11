//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Fortress of Nature");
    addSpecification("source", "druid");
    addSpecification("description", "This research permanently transforms the druid into a living fortress of natural protection.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "druid",
            "value": 51
        ]));

    addPrerequisite("/guilds/druid/ancient-ward/root.c",
        ([ "type": "research" ]));

    addPrerequisite("/guilds/druid/ancient-ward/arcane-permanence.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus defense", 7);
    addSpecification("bonus soak", 6);
    addSpecification("bonus constitution", 2);
    addSpecification("bonus wisdom", 2);
}
