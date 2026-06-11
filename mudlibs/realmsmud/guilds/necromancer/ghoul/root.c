//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/summoningResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Call Ghoul");
    addSpecification("source", "necromancer");

    addSpecification("description", "This skill provides the necromancer with "
        "the knowledge of how to call a ghoul from the deathly realm. "
        "More feral than a zombie yet retaining predatory cunning, the "
        "ghoul hunts in packs and paralyzes its prey, growing from a "
        "savage scavenger into an apex predator of undeath as the "
        "necromancer deepens their mastery of the grave.");

    addSpecification("number to summon", 1);
    addSpecification("maximum that can be summoned", 3);
    addSpecification("persona", "ghoul");
    addSpecification("persona level", 4);

    addSpecification("consumables", (["graveyard dirt": 1]));

    addSpecification("modifiers", ([
        "/guilds/necromancer/ghoul/rending-claws.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
        }),
        "/guilds/necromancer/ghoul/necrotic-stench.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus evil attack",
               "value": 3 ]),
        }),
        "/guilds/necromancer/ghoul/paralyzing-bite.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
        }),
        "/guilds/necromancer/ghoul/carrion-hide.c":({
            (["modifier": "bonus hit points",
               "value": 20 ]),
            (["modifier": "bonus armor class",
               "value": 3 ]),
        }),
        "/guilds/necromancer/ghoul/grave-hunger.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/necromancer/ghoul/venom-claws.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
        }),
        "/guilds/necromancer/ghoul/corpse-hide.c":({
            (["modifier": "bonus hit points",
               "value": 35 ]),
            (["modifier": "bonus soak",
               "value": 3 ]),
        }),
        "/guilds/necromancer/ghoul/feeding-frenzy.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
        }),
        "/guilds/necromancer/ghoul/pack-hunter.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus hit points",
               "value": 20 ]),
        }),
        "/guilds/necromancer/ghoul/toxic-bite.c":({
            (["modifier": "bonus attack",
               "value": 7 ]),
            (["modifier": "bonus damage",
               "value": 6 ]),
        }),
        "/guilds/necromancer/ghoul/iron-hide.c":({
            (["modifier": "bonus hit points",
               "value": 50 ]),
            (["modifier": "bonus armor class",
               "value": 6 ]),
        }),
        "/guilds/necromancer/ghoul/blood-frenzy.c":({
            (["modifier": "bonus attack",
               "value": 8 ]),
            (["modifier": "bonus damage",
               "value": 7 ]),
        }),
        "/guilds/necromancer/ghoul/extra-ghoul.c":({
            (["modifier": "bonus hit points",
               "value": 30 ]),
            (["modifier": "bonus attack",
               "value": 3 ]),
        }),
        "/guilds/necromancer/ghoul/plague-bite.c":({
            (["modifier": "bonus damage",
               "value": 8 ]),
            (["modifier": "bonus evil attack",
               "value": 7 ]),
        }),
        "/guilds/necromancer/ghoul/necrotic-armor.c":({
            (["modifier": "bonus hit points",
               "value": 70 ]),
            (["modifier": "bonus soak",
               "value": 5 ]),
        }),
        "/guilds/necromancer/ghoul/savage-frenzy.c":({
            (["modifier": "bonus attack",
               "value": 10 ]),
            (["modifier": "bonus damage",
               "value": 8 ]),
        }),
        "/guilds/necromancer/ghoul/pack-coordination.c":({
            (["modifier": "bonus hit points",
               "value": 50 ]),
            (["modifier": "bonus attack",
               "value": 5 ]),
        }),
        "/guilds/necromancer/ghoul/death-leap.c":({
            (["modifier": "bonus attack",
               "value": 8 ]),
            (["modifier": "bonus dexterity",
               "value": 3 ]),
        }),
        "/guilds/necromancer/ghoul/virulent-plague-bite.c":({
            (["modifier": "bonus damage",
               "value": 10 ]),
            (["modifier": "bonus evil attack",
               "value": 10 ]),
        }),
        "/guilds/necromancer/ghoul/bone-plating.c":({
            (["modifier": "bonus armor class",
               "value": 10 ]),
            (["modifier": "bonus hit points",
               "value": 90 ]),
        }),
        "/guilds/necromancer/ghoul/berserker-frenzy.c":({
            (["modifier": "bonus attack",
               "value": 12 ]),
            (["modifier": "bonus damage",
               "value": 10 ]),
        }),
        "/guilds/necromancer/ghoul/pack-ambush.c":({
            (["modifier": "bonus hit points",
               "value": 70 ]),
            (["modifier": "bonus attack",
               "value": 6 ]),
        }),
        "/guilds/necromancer/ghoul/rending-leap.c":({
            (["modifier": "bonus attack",
               "value": 10 ]),
            (["modifier": "bonus damage",
               "value": 9 ]),
        }),
        "/guilds/necromancer/ghoul/death-plague.c":({
            (["modifier": "bonus damage",
               "value": 12 ]),
            (["modifier": "bonus evil attack",
               "value": 12 ]),
        }),
        "/guilds/necromancer/ghoul/titanium-hide.c":({
            (["modifier": "bonus armor class",
               "value": 14 ]),
            (["modifier": "bonus hit points",
               "value": 110 ]),
        }),
        "/guilds/necromancer/ghoul/slaughter-frenzy.c":({
            (["modifier": "bonus attack",
               "value": 14 ]),
            (["modifier": "bonus damage",
               "value": 12 ]),
        }),
        "/guilds/necromancer/ghoul/pack-devastation.c":({
            (["modifier": "bonus hit points",
               "value": 90 ]),
            (["modifier": "bonus attack",
               "value": 7 ]),
        }),
        "/guilds/necromancer/ghoul/apocalyptic-plague.c":({
            (["modifier": "bonus damage",
               "value": 15 ]),
            (["modifier": "bonus evil attack",
               "value": 15 ]),
        }),
        "/guilds/necromancer/ghoul/ghoul-lord.c":({
            (["modifier": "bonus attack",
               "value": 14 ]),
            (["modifier": "bonus strength",
               "value": 7 ]),
        }),
        "/guilds/necromancer/ghoul/massacre-frenzy.c":({
            (["modifier": "bonus attack",
               "value": 16 ]),
            (["modifier": "bonus damage",
               "value": 14 ]),
        }),
        "/guilds/necromancer/ghoul/plague-lord.c":({
            (["modifier": "bonus damage",
               "value": 18 ]),
            (["modifier": "bonus evil attack",
               "value": 18 ]),
        }),
        "/guilds/necromancer/ghoul/undead-champion.c":({
            (["modifier": "bonus attack",
               "value": 16 ]),
            (["modifier": "bonus strength",
               "value": 9 ]),
        }),
        "/guilds/necromancer/ghoul/death-frenzy.c":({
            (["modifier": "bonus attack",
               "value": 18 ]),
            (["modifier": "bonus damage",
               "value": 16 ]),
        }),
        "/guilds/necromancer/ghoul/plague-sovereign.c":({
            (["modifier": "bonus damage",
               "value": 20 ]),
            (["modifier": "bonus evil attack",
               "value": 20 ]),
        }),
        "/guilds/necromancer/ghoul/pack-supreme.c":({
            (["modifier": "bonus hit points",
               "value": 160 ]),
            (["modifier": "bonus attack",
               "value": 10 ]),
        }),
        "/guilds/necromancer/ghoul/lord-of-the-pack.c":({
            (["modifier": "bonus attack",
               "value": 20 ]),
            (["modifier": "bonus damage",
               "value": 20 ]),
            (["modifier": "bonus evil attack",
               "value": 20 ]),
        }),
    ]));

    addSpecification("scope", "area");
    addSpecification("research type", "tree root");

    addSpecification("spell point cost", 60);
    addSpecification("stamina point cost", 45);

    addSpecification("event handler", "callGhoulEvent");
    addSpecification("command template", "call ghoul");
    addSpecification("use composite message", "##InitiatorName## "
        "##Infinitive::press## ##InitiatorPossessive## hands into the "
        "cold earth and ##Infinitive::speak## guttural words of "
        "necromantic power. The ground trembles as a feral ghoul bursts "
        "from the soil, its pale eyes wild with hunger as it crouches "
        "at ##InitiatorPossessive## feet, ready to hunt.");
}
