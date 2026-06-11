//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/instantaneousActiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Shattering Arc");
    addSpecification("source", "Guardian of Khazurath");
    addSpecification("description", "This research provides the user with the "
        "knowledge of a wide sweeping hammer blow that carries enough force "
        "to shatter bone and buckle armor.");

    addPrerequisite("/guilds/guardian-of-khazurath/hammer/thunderstrike.c",
        (["type": "research"]));

    addSpecification("limited by", (["equipment": ({ "hammer" })]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "Guardian of Khazurath",
            "value": 21
        ]));

    addSpecification("scope", "targeted");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("damage hit points", ({ ([
            "probability": 90,
            "base damage": 60,
            "range": 120
        ]),
        ([
            "probability": 10,
            "base damage": 120,
            "range": 300
        ])
    }));

    addSpecification("damage type", "bludgeon");

    addSpecification("modifiers", ({
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/hammer/concussive-knowledge.c",
            "name": "Concussive Knowledge",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/hammer/bone-deep.c",
            "name": "Bone Deep",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/hammer/shatter-deep.c",
            "name": "Shatter Deep",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/hammer/khazurath-impact.c",
            "name": "Khazurath Impact",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/hammer/endless-wrath.c",
            "name": "Endless Wrath",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.5
        ]),
        ([
            "type": "weapon damage",
            "name": "hammer damage",
            "types": ({ "hammer" }),
            "formula": "additive",
            "rate": 0.75
        ]),
        ([
            "type": "skill",
            "name": "hammer",
            "formula": "additive",
            "rate": 0.5
        ]),
        ([
            "type": "skill",
            "name": "anatomy and physiology",
            "formula": "additive",
            "rate": 0.10
        ]),
        ([
            "type": "skill",
            "name": "physics",
            "formula": "additive",
            "rate": 0.15
        ]),
        ([
            "type": "attribute",
            "name": "strength",
            "formula": "additive",
            "rate": 0.35
        ]),
        ([
            "type": "attribute",
            "name": "constitution",
            "formula": "additive",
            "rate": 0.15
        ]),
    }));

    addSpecification("stamina point cost", 150);
    addSpecification("stamina point cost modifiers", ([
        "/guilds/guardian-of-khazurath/hammer/pain-for-pain.c": 10,
        "/guilds/guardian-of-khazurath/hammer/iron-will.c": 10,
        "/guilds/guardian-of-khazurath/hammer/battle-worn.c": 10,
        "/guilds/guardian-of-khazurath/hammer/hammer-economy.c": 10,
    ]));

    addSpecification("cooldown", 75);
    addSpecification("cooldown group", "guardian weapon level 21");
    addSpecification("cooldown modifiers", ([
        "/guilds/guardian-of-khazurath/hammer/crushing-tempo.c": 10,
        "/guilds/guardian-of-khazurath/hammer/hammers-cadence.c": 10,
        "/guilds/guardian-of-khazurath/hammer/dwarven-cadence.c": 10,
        "/guilds/guardian-of-khazurath/hammer/hammer-haste.c": 10,
        "/guilds/guardian-of-khazurath/hammer/hammer-flow.c": 10,
    ]));

    addSpecification("event handler", "shatteringArcEvent");
    addSpecification("command template", "shattering arc [at ##Target##]");
    addSpecification("use ability message", "##InitiatorName## "
        "##Infinitive::sweep## ##InitiatorPossessive## "
        "##InitiatorWeapon## in a shattering arc at ##TargetName##.");
}
