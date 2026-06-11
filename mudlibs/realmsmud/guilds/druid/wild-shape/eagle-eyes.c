//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Eagle Eyes");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the hawk-eyed perception of an eagle.");

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 15
    ]));

    addPrerequisite("/guilds/druid/wild-shape/serpent-scales.c",
        ([ "type": "research" ]));

    addSpecification("limited by", (["research active": ({ "/guilds/druid/wild-shape/eagle-aspect.c" })]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus perception", 3);
    addSpecification("bonus attack", 2);
}
