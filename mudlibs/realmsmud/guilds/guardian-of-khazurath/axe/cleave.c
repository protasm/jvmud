//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/instantaneousActiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Cleave");
    addSpecification("source", "Guardian of Khazurath");
    addSpecification("description", "This research provides the user with the "
        "knowledge of a powerful downward cleaving strike passed down from the "
        "weaponmasters of Mirost.");

    addPrerequisite("/guilds/guardian-of-khazurath/axe/root.c",
        (["type": "research"]));

    addSpecification("limited by", (["equipment": ({ "axe" })]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "Guardian of Khazurath",
            "value": 1
        ]));

    addSpecification("scope", "targeted");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("damage hit points", ({ ([
            "probability": 90,
            "base damage": 5,
            "range": 10
        ]),
        ([
            "probability": 10,
            "base damage": 15,
            "range": 30
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
            "rate": 0.25
        ]),
        ([
            "type": "attribute",
            "name": "dexterity",
            "formula": "additive",
            "rate": 0.25
        ]),
    }));

    addSpecification("stamina point cost", 30);
    addSpecification("stamina point cost modifiers", ([
        "/guilds/guardian-of-khazurath/axe/iron-endurance.c": 5,
        "/guilds/guardian-of-khazurath/axe/dwarven-reserve.c": 5,
        "/guilds/guardian-of-khazurath/axe/battle-hardened.c": 5,
        "/guilds/guardian-of-khazurath/axe/axe-economy.c": 5,
    ]));

    addSpecification("cooldown", 25);
    addSpecification("cooldown group", "guardian weapon level 1");
    addSpecification("cooldown modifiers", ([
        "/guilds/guardian-of-khazurath/axe/swiftstrike-mastery.c": 5,
        "/guilds/guardian-of-khazurath/axe/battle-tempo.c": 5,
        "/guilds/guardian-of-khazurath/axe/relentless-tempo.c": 5,
        "/guilds/guardian-of-khazurath/axe/dwarven-haste.c": 5,
    ]));

    addSpecification("event handler", "cleaveEvent");
    addSpecification("command template", "cleave [at ##Target##]");
    addSpecification("use ability message", "##InitiatorName## "
        "##Infinitive::bring## ##InitiatorPossessive## "
        "##InitiatorWeapon## down in a powerful cleave at ##TargetName##.");
}
