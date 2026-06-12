//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Enduring Will");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the druid the enduring will of ancient stones.");

    addPrerequisite("/guilds/druid/ancient-ward/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 27
    ]));

    addPrerequisite("/guilds/druid/ancient-ward/ancient-skin.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus constitution", 1);
    addSpecification("bonus wisdom", 1);
    addSpecification("bonus defense", 3);
}
