//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/summoningResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Raise Zombie");
    addSpecification("source", "necromancer");

    addSpecification("description", "This skill provides the necromancer with "
        "the knowledge of how to raise a shambling zombie from a nearby "
        "corpse. The zombie is bound to the necromancer through dark rituals, "
        "growing in power as the necromancer learns more of the dark arts of "
        "undeath.");

    addSpecification("number to summon", 1);
    addSpecification("maximum that can be summoned", 3);
    addSpecification("persona", "zombie");
    addSpecification("persona level", 3);

    addSpecification("consumables", (["grave dust": 1]));

    addSpecification("modifiers", ([
        "/guilds/necromancer/zombie/rotting-flesh.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/necromancer/zombie/decomposing-body.c":({
            (["modifier": "bonus hit points",
               "value": 20 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/necromancer/zombie/crushing-blow.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/necromancer/zombie/festering-wounds.c":({
            (["modifier": "bonus damage",
               "value": 4 ]),
            (["modifier": "bonus evil attack",
               "value": 3 ]),
        }),
        "/guilds/necromancer/zombie/undead-resilience.c":({
            (["modifier": "bonus hit points",
               "value": 30 ]),
            (["modifier": "bonus soak",
               "value": 3 ]),
        }),
        "/guilds/necromancer/zombie/plague-carrier.c":({
            (["modifier": "bonus damage",
               "value": 5 ]),
            (["modifier": "bonus evil attack",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/bone-crushing-strike.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/necrotic-hide.c":({
            (["modifier": "bonus armor class",
               "value": 5 ]),
            (["modifier": "bonus hit points",
               "value": 40 ]),
        }),
        "/guilds/necromancer/zombie/shambling-horde.c":({
            (["modifier": "bonus attack",
               "value": 2 ]),
            (["modifier": "bonus hit points",
               "value": 15 ]),
        }),
        "/guilds/necromancer/zombie/virulent-plague.c":({
            (["modifier": "bonus damage",
               "value": 6 ]),
            (["modifier": "bonus evil attack",
               "value": 6 ]),
        }),
        "/guilds/necromancer/zombie/infectious-strike.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/armored-corpse.c":({
            (["modifier": "bonus armor class",
               "value": 8 ]),
            (["modifier": "bonus soak",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/extra-zombie.c":({
            (["modifier": "bonus hit points",
               "value": 50 ]),
            (["modifier": "bonus attack",
               "value": 3 ]),
        }),
        "/guilds/necromancer/zombie/rampaging-corpse.c":({
            (["modifier": "bonus attack",
               "value": 7 ]),
            (["modifier": "bonus damage",
               "value": 7 ]),
        }),
        "/guilds/necromancer/zombie/death-grip.c":({
            (["modifier": "bonus attack",
               "value": 8 ]),
            (["modifier": "bonus strength",
               "value": 3 ]),
        }),
        "/guilds/necromancer/zombie/rot-aura.c":({
            (["modifier": "bonus damage",
               "value": 8 ]),
            (["modifier": "bonus evil attack",
               "value": 8 ]),
        }),
        "/guilds/necromancer/zombie/bloated-corpse.c":({
            (["modifier": "bonus hit points",
               "value": 75 ]),
            (["modifier": "bonus armor class",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/mass-grave.c":({
            (["modifier": "bonus hit points",
               "value": 60 ]),
            (["modifier": "bonus constitution",
               "value": 3 ]),
        }),
        "/guilds/necromancer/zombie/relentless-assault.c":({
            (["modifier": "bonus attack",
               "value": 10 ]),
            (["modifier": "bonus damage",
               "value": 8 ]),
        }),
        "/guilds/necromancer/zombie/overwhelming-numbers.c":({
            (["modifier": "bonus hit points",
               "value": 80 ]),
            (["modifier": "bonus attack",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/necrotic-explosion.c":({
            (["modifier": "bonus damage",
               "value": 10 ]),
            (["modifier": "bonus evil attack",
               "value": 10 ]),
        }),
        "/guilds/necromancer/zombie/undead-fortitude.c":({
            (["modifier": "bonus hit points",
               "value": 100 ]),
            (["modifier": "bonus soak",
               "value": 8 ]),
        }),
        "/guilds/necromancer/zombie/flesh-golem.c":({
            (["modifier": "bonus armor class",
               "value": 12 ]),
            (["modifier": "bonus strength",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/unstoppable-shamble.c":({
            (["modifier": "bonus attack",
               "value": 12 ]),
            (["modifier": "bonus damage",
               "value": 10 ]),
        }),
        "/guilds/necromancer/zombie/zombie-tide.c":({
            (["modifier": "bonus hit points",
               "value": 100 ]),
            (["modifier": "bonus attack",
               "value": 6 ]),
        }),
        "/guilds/necromancer/zombie/corpse-explosion.c":({
            (["modifier": "bonus damage",
               "value": 12 ]),
            (["modifier": "bonus evil attack",
               "value": 12 ]),
        }),
        "/guilds/necromancer/zombie/iron-corpse.c":({
            (["modifier": "bonus armor class",
               "value": 15 ]),
            (["modifier": "bonus hit points",
               "value": 120 ]),
        }),
        "/guilds/necromancer/zombie/death-march.c":({
            (["modifier": "bonus attack",
               "value": 14 ]),
            (["modifier": "bonus damage",
               "value": 12 ]),
        }),
        "/guilds/necromancer/zombie/risen-champion.c":({
            (["modifier": "bonus attack",
               "value": 15 ]),
            (["modifier": "bonus strength",
               "value": 8 ]),
        }),
        "/guilds/necromancer/zombie/plague-bearer.c":({
            (["modifier": "bonus damage",
               "value": 15 ]),
            (["modifier": "bonus evil attack",
               "value": 15 ]),
        }),
        "/guilds/necromancer/zombie/lords-of-undeath.c":({
            (["modifier": "bonus attack",
               "value": 16 ]),
            (["modifier": "bonus damage",
               "value": 14 ]),
        }),
        "/guilds/necromancer/zombie/eternal-horde.c":({
            (["modifier": "bonus hit points",
               "value": 150 ]),
            (["modifier": "bonus constitution",
               "value": 5 ]),
        }),
        "/guilds/necromancer/zombie/undying-champion.c":({
            (["modifier": "bonus attack",
               "value": 18 ]),
            (["modifier": "bonus strength",
               "value": 10 ]),
        }),
        "/guilds/necromancer/zombie/army-of-the-dead.c":({
            (["modifier": "bonus hit points",
               "value": 200 ]),
            (["modifier": "bonus attack",
               "value": 10 ]),
        }),
        "/guilds/necromancer/zombie/zombie-apocalypse.c":({
            (["modifier": "bonus damage",
               "value": 20 ]),
            (["modifier": "bonus evil attack",
               "value": 20 ]),
        }),
        "/guilds/necromancer/zombie/lord-of-the-horde.c":({
            (["modifier": "bonus attack",
               "value": 20 ]),
            (["modifier": "bonus damage",
               "value": 20 ]),
            (["modifier": "bonus hit points",
               "value": 250 ]),
        }),
    ]));

    addSpecification("scope", "area");
    addSpecification("research type", "tree root");

    addSpecification("spell point cost", 60);
    addSpecification("stamina point cost", 40);

    addSpecification("event handler", "raiseZombieEvent");
    addSpecification("command template", "raise zombie");
    addSpecification("use composite message", "##InitiatorName## "
        "##Infinitive::raise## ##InitiatorPossessive## hands as a chill "
        "spreads through the air. Dark energy flows from ##InitiatorPossessive## "
        "fingertips into the earth, and moments later rotting hands claw "
        "their way upward as a shambling zombie pulls itself free of the "
        "ground and takes its place at ##InitiatorPossessive## side.");
}
