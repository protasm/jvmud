//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Avatar of the Storm");
    addSpecification("source", "druid");
    addSpecification("description", "This research transforms the druid into a living avatar of primal storm fury.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "druid",
            "value": 65
        ]));

    addPrerequisite("/guilds/druid/primal-storm/root.c",
        ([ "type": "research" ]));

    addPrerequisite("/guilds/druid/primal-storm/storm-incarnate.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus cold attack", 7);
    addSpecification("bonus air attack", 7);
    addSpecification("bonus intelligence", 2);
    addSpecification("bonus dexterity", 2);
}
