//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Beast Champion");
    addSpecification("source", "druid");
    addSpecification("description", "This research marks the druid as a champion of the beast - permanently enhanced in all combat arts.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "druid",
            "value": 57
        ]));

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));

    addPrerequisite("/guilds/druid/wild-shape/supreme-predator.c",
        ([ "type": "research" ]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus attack", 5);
    addSpecification("bonus damage", 4);
    addSpecification("bonus constitution", 2);
    addSpecification("bonus dexterity", 2);
}
