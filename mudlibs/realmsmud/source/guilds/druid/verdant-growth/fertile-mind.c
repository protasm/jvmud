//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Fertile Mind");
    addSpecification("source", "druid");
    addSpecification("description", "This research broadens the druid's connection to natural wisdom.");

    addPrerequisite("/guilds/druid/verdant-growth/root.c",
        ([ "type": "research" ]));

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 7
    ]));

    addPrerequisite("/guilds/druid/verdant-growth/wild-vigor.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus wisdom", 1);
    addSpecification("bonus spell points", 25);
}
