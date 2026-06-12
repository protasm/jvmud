//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Serpent Scales");
    addSpecification("source", "druid");
    addSpecification("description", "This research gradually roughens the druid's skin with serpentine resilience.");

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 11
    ]));

    addPrerequisite("/guilds/druid/wild-shape/wolves-ferocity.c",
        ([ "type": "research" ]));

    addSpecification("limited by", (["research active": ({ "/guilds/druid/wild-shape/serpent-aspect.c" })]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus defense", 2);
    addSpecification("bonus soak", 1);
}
