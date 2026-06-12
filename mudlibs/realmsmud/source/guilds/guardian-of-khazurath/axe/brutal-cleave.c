//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/instantaneousActiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Brutal Cleave");
    addSpecification("source", "Guardian of Khazurath");
    addSpecification("description", "This research provides the user with the "
        "knowledge of a savage overhead strike that brings the full weight of "
        "the axe down with crushing force.");

    addPrerequisite("/guilds/guardian-of-khazurath/axe/rending-blow.c",
        (["type": "research"]));

    addSpecification("limited by", (["equipment": ({ "axe" })]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "Guardian of Khazurath",
            "value": 11
        ]));

    addSpecification("scope", "targeted");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("damage hit points", ({ ([
            "probability": 90,
            "base damage": 35,
            "range": 75
        ]),
        ([
            "probability": 10,
            "base damage": 75,
            "range": 150
        ])
    }));

    addSpecification("damage type", "slash");

    addSpecification("modifiers", ({
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/axe/cutting-insight.c",
            "name": "Cutting Insight",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/axe/blood-hunger.c",
            "name": "Blood Hunger",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/axe/deep-rend.c",
            "name": "Deep Rend",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/axe/khazurath-precision.c",
            "name": "Khazurath Precision",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/guardian-of-khazurath/axe/endless-assault.c",
            "name": "Endless Assault",
            "formula": "multiplicative",
            "base value": 1,
            "rate": 1.5
        ]),
        ([
            "type": "weapon damage",
            "name": "axe damage",
            "types": ({ "axe" }),
            "formula": "additive",
            "rate": 0.75
        ]),
        ([
            "type": "skill",
            "name": "axe",
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
            "rate": 0.10
        ]),
        ([
            "type": "attribute",
            "name": "strength",
            "formula": "additive",
            "rate": 0.35
        ]),
        ([
            "type": "attribute",
            "name": "dexterity",
            "formula": "additive",
            "rate": 0.15
        ]),
    }));

    addSpecification("stamina point cost", 125);
    addSpecification("stamina point cost modifiers", ([
        "/guilds/guardian-of-khazurath/axe/iron-endurance.c": 10,
        "/guilds/guardian-of-khazurath/axe/dwarven-reserve.c": 10,
        "/guilds/guardian-of-khazurath/axe/battle-hardened.c": 10,
        "/guilds/guardian-of-khazurath/axe/axe-economy.c": 10,
    ]));

    addSpecification("cooldown", 60);
    addSpecification("cooldown group", "guardian weapon level 11");
    addSpecification("cooldown modifiers", ([
        "/guilds/guardian-of-khazurath/axe/swiftstrike-mastery.c": 10,
        "/guilds/guardian-of-khazurath/axe/battle-tempo.c": 10,
        "/guilds/guardian-of-khazurath/axe/relentless-tempo.c": 10,
        "/guilds/guardian-of-khazurath/axe/dwarven-haste.c": 10,
    ]));

    addSpecification("event handler", "brutalCleaveEvent");
    addSpecification("command template", "brutal cleave [at ##Target##]");
    addSpecification("use ability message", "##InitiatorName## "
        "##Infinitive::bring## ##InitiatorPossessive## "
        "##InitiatorWeapon## down with savage force on ##TargetName##.");
}
