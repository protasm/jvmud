//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Crushing Blow");
    addSpecification("source", "druid");
    addSpecification("description", "This research channels the bear's crushing power into the druid's strikes.");

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 17
    ]));

    addPrerequisite("/guilds/druid/wild-shape/primal-hide.c",
        ([ "type": "research" ]));

    addSpecification("limited by", (["research active": ({ "/guilds/druid/wild-shape/bear-aspect.c" })]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus strength", 1);
    addSpecification("bonus damage", 3);
}
