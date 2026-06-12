//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Extended Reach");
    addSpecification("source", "necromancer");
    addSpecification("description", "This research teaches the necromancer to greatly extend the range of constructed necromantic spells.");

    addPrerequisite("level",
        (["type": "level",
            "guild": "necromancer",
            "value": 9
        ]));
    addPrerequisite("/guilds/necromancer/construct/necromancers-reach.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("affected research", ([
        "Necromancer Spell": 12,
    ]));
    addSpecification("affected research type", "percentage");
}
