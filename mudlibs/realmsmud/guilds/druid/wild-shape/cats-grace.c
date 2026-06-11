//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Cat's Grace");
    addSpecification("source", "druid");
    addSpecification("description", "This research grants the liquid grace of a great cat to the druid.");

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 23
    ]));

    addPrerequisite("/guilds/druid/wild-shape/bear-strength.c",
        ([ "type": "research" ]));

    addSpecification("limited by", ([
        "research active": ({ "/guilds/druid/wild-shape/cat-aspect.c" })]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus dexterity", 2);
    addSpecification("bonus dodge", 3);
}
