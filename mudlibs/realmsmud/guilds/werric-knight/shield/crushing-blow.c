//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/instantaneousActiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Crushing Blow");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have learned to deliver devastating strikes "
        "with your shield, using its full weight and your body's momentum to crush "
        "your enemy. This technique can shatter armor, break bones, and leave foes "
        "reeling from the sheer force of impact.");

    addPrerequisite("guilds/werric-knight/shield/fortified-defense.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":9]));
    addSpecification("scope", "targeted");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("cooldown", 50);
    addSpecification("stamina point cost", 65);
    addSpecification("command template", "crushing blow at ##Target##");
    addSpecification("use ability message", "##InitiatorName## ##Infinitive::deliver## "
        "a crushing blow with ##InitiatorPossessive## shield, the impact reverberating "
        "through ##TargetName##'s body!");

    addSpecification("damage type", "physical");
    addSpecification("damage hit points", ({ ([
        "probability":85,
        "base damage":15,
        "range":25
    ]),
    ([
        "probability":15,
        "base damage":30,
        "range":50
    ]) }));

    addSpecification("modifiers", ({
        ([
            "type": "research",
            "research item": "/guilds/werric-knight/shield/shield-impact.c",
            "name": "Shield Impact",
            "formula": "multiplicative",
            "base value":1,
            "rate":1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/werric-knight/shield/defensive-strike.c",
            "name": "Defensive Strike",
            "formula": "multiplicative",
            "base value":1,
            "rate":1.25
        ]),
        ([
            "type": "research",
            "research item": "/guilds/werric-knight/shield/crushing-force.c",
            "name": "Crushing Force",
            "formula": "multiplicative",
            "base value":1,
            "rate":1.25
        ]),
        ([
            "type": "skill",
            "name": "shield",
            "formula": "additive",
            "rate":0.50
        ]),
        ([
            "type": "skill",
            "name": "physics",
            "formula": "additive",
            "rate":0.10
        ]),
        ([
            "type": "skill",
            "name": "anatomy and physiology",
            "formula": "additive",
            "rate":0.10
        ]),
        ([
            "type": "attribute",
            "name": "strength",
            "formula": "additive",
            "rate":0.25
        ])
    }));
}
