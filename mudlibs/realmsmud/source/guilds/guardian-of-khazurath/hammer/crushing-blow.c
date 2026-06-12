//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/instantaneousActiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Crushing Blow");
    addSpecification("source", "Guardian of Khazurath");
    addSpecification("description", "This research provides the user with the "
        "knowledge of a blow delivered with the full weight of the hammer, "
        "crushing through armor and bone alike.");

    addPrerequisite("/guilds/guardian-of-khazurath/hammer/smash.c",
        (["type": "research"]));

    addSpecification("limited by", (["equipment": ({ "hammer" })]));

    addPrerequisite("level",
        (["type": "level",
            "guild": "Guardian of Khazurath",
            "value": 5
        ]));

    addSpecification("scope", "targeted");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("damage hit points", ({ ([
            "probability": 90,
            "base damage": 10,
            "range": 15
        ]),
        ([
            "probability": 10,
            "base damage": 25,
            "range": 50
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
            "name": "constitution",
            "formula": "additive",
            "rate": 0.15
        ]),
    }));

    addSpecification("stamina point cost", 50);
    addSpecification("stamina point cost modifiers", ([
        "/guilds/guardian-of-khazurath/hammer/pain-for-pain.c": 5,
        "/guilds/guardian-of-khazurath/hammer/iron-will.c": 5,
        "/guilds/guardian-of-khazurath/hammer/battle-worn.c": 5,
        "/guilds/guardian-of-khazurath/hammer/hammer-economy.c": 5,
    ]));

    addSpecification("cooldown", 35);
    addSpecification("cooldown group", "guardian weapon level 5");
    addSpecification("cooldown modifiers", ([
        "/guilds/guardian-of-khazurath/hammer/crushing-tempo.c": 5,
        "/guilds/guardian-of-khazurath/hammer/hammers-cadence.c": 5,
        "/guilds/guardian-of-khazurath/hammer/dwarven-cadence.c": 5,
        "/guilds/guardian-of-khazurath/hammer/hammer-haste.c": 5,
    ]));

    addSpecification("event handler", "crushingBlowEvent");
    addSpecification("command template", "crushing blow [at ##Target##]");
    addSpecification("use ability message", "##InitiatorName## "
        "##Infinitive::bring## ##InitiatorPossessive## "
        "##InitiatorWeapon## down in a bone-crushing blow on ##TargetName##.");
}
