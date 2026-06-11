//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved.
//                      See the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Wolf's Ferocity");
    addSpecification("source", "druid");
    addSpecification("description", "This research channels the wolf's savage ferocity into the druid's attacks.");

    addPrerequisite("/guilds/druid/wild-shape/root.c",
        ([ "type": "research" ]));
    addPrerequisite("level", ([
        "type": "level", "guild": "druid", "value": 7
    ]));

    addPrerequisite("/guilds/druid/wild-shape/feral-awareness.c",
        ([ "type": "research" ]));

    addSpecification("limited by", (["research active": ({ "/guilds/druid/wild-shape/wolf-aspect.c" })]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus attack", 2);
    addSpecification("bonus damage", 1);
}
