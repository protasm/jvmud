//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Spirit Ward");
    addSpecification("source", "druid");
    addSpecification("description", "This research cloaks the wolf in protective spirits of the wild, granting it potent magical defenses and elemental resistances.");

    addPrerequisite("level", ([
        "type": "level",
        "guild": "druid",
        "value": 63
    ]));

    addPrerequisite("/guilds/druid/companions/wolf/indomitable-will.c",
        ([ "type": "research" ]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("related research", ({
        "/guilds/druid/companions/wolf/root.c",
    }));
}
