//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Primal Geomancer");
    addSpecification("source", "druid");
    addSpecification("description", "This research marks the druid as a primal geomancer, deeply amplifying all wrath spells.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "druid",
            "value": 51
        ]));

    addPrerequisite("/guilds/druid/natures-wrath/root.c",
        ([ "type": "research" ]));

    addPrerequisite("/guilds/druid/natures-wrath/wrath-of-nature-ki.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Thorn Whip": 25,
        "Stone Fist": 25,
        "Wildfire": 25,
        "Eruption": 25,
        "Magma Surge": 25,
        "Scorched Earth": 25,
        "Earthen Grasp": 25,
        "Conflagration": 25,
        "Earthquake": 25,
        "Volcanic Fury": 25,
        "Infernal Wild": 25,
    ]));
}
