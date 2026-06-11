//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Chlorophyll Body");
    addSpecification("source", "druid");
    addSpecification("description", "This research gives the druid a slight photosynthetic quality, recovering energy from sunlight.");

    addPrerequisite("/guilds/druid/verdant-growth/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 11
    ]));

    addPrerequisite("/guilds/druid/verdant-growth/living-wood-skin.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus constitution", 1);
    addSpecification("bonus spell points", 40);
}
