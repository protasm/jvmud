//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/summoningResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Call Eagle Companion");
    addSpecification("source", "druid");

    addSpecification("description", "This skill provides the druid with the "
        "knowledge of how to call forth an eagle companion from the high "
        "peaks. The eagle is bound to the druid through a pact of sky and "
        "storm, growing from a keen predator into an avatar of the "
        "tempest as the druid deepens their bond with the wild.");

    addSpecification("number to summon", 1);
    addSpecification("maximum that can be summoned", 1);
    addSpecification("persona", "eagle");
    addSpecification("persona level", 5);

    addSpecification("modifiers", ([
        "/guilds/druid/companions/eagle/talon-i.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/swift-wing-i.c":({
            (["modifier": "bonus dodge",
               "value": 4 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/keen-eye-i.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus perception",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/aerial-grace.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/eagle/talon-rake-spell.c" ]),
        }),
        "/guilds/druid/companions/eagle/piercing-gaze.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus perception",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/feather-light.c":({
            (["modifier": "bonus dodge",
               "value": 3 ]),
            (["modifier": "bonus parry",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/talon-ii.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/swift-wing-ii.c":({
            (["modifier": "bonus dodge",
               "value": 4 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/keen-eye-ii.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/aerial-strike.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/iron-feathers-i.c":({
            (["modifier": "bonus soak",
               "value": 1 ]),
            (["modifier": "bonus no armor",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/hunters-focus-i.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus parry",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/talon-iii.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/swift-wing-iii.c":({
            (["modifier": "bonus dodge",
               "value": 4 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/keen-eye-iii.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/iron-feathers-ii.c":({
            (["modifier": "bonus soak",
               "value": 2 ]),
            (["modifier": "bonus no armor",
               "value": 3 ]),
            (["modifier": "bonus defense",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/hunters-focus-ii.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus parry",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/dive-bomb.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/eagle/screaming-dive-spell.c" ]),
        }),
        "/guilds/druid/companions/eagle/talon-iv.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/sky-dancer.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/keen-eye-iv.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/screaming-dive.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/iron-feathers-iii.c":({
            (["modifier": "bonus soak",
               "value": 2 ]),
            (["modifier": "bonus no armor",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/eagle/hunters-focus-iii.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus parry",
               "value": 3 ]),
            (["modifier": "bonus defense",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/talon-v.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/swift-wing-iv.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus parry",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/keen-eye-v.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/lords-mastery.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/wind-dancer.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/talon-vi.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/cyclone-evasion.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus defense",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/eagle/death-from-above.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/eagle/death-from-above-spell.c" ]),
        }),
        "/guilds/druid/companions/eagle/swift-wing-v.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/iron-feathers-iv.c":({
            (["modifier": "bonus soak",
               "value": 3 ]),
            (["modifier": "bonus no armor",
               "value": 5 ]),
            (["modifier": "bonus defense",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/eagle/killing-strike.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/eagle/phantom-flight.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus parry",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/primal-vitality.c":({
            (["modifier": "bonus hit points",
               "value": 150 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/apex-hunter.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/eternal-wind.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus defense",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/eagle/undying-bond.c":({
            (["modifier": "bonus hit points",
               "value": 175 ]),
            (["modifier": "bonus heal hit points",
               "value": 6 ]),
        }),
        "/guilds/druid/companions/eagle/storm-diver.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/eagle/cyclone-strike-spell.c" ]),
        }),
        "/guilds/druid/companions/eagle/sky-sovereign.c":({
            (["modifier": "bonus dodge",
               "value": 6 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
            (["modifier": "bonus defense",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/eagle/primal-endurance.c":({
            (["modifier": "bonus hit points",
               "value": 175 ]),
            (["modifier": "bonus heal hit points",
               "value": 6 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/eagle/wrath-of-the-sky.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/eagle/eternal-guardian.c":({
            (["modifier": "bonus hit points",
               "value": 200 ]),
            (["modifier": "bonus heal hit points",
               "value": 8 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/eagle/spirit-of-the-storm.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/eagle/storm-dive-spell.c" ]),
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
            (["modifier": "bonus dodge",
               "value": 6 ]),
            (["modifier": "bonus hit points",
               "value": 200 ]),
        }),
    ]));

    addSpecification("scope", "area");
    addSpecification("research type", "tree root");

    addSpecification("spell point cost", 75);
    addSpecification("stamina point cost", 50);

    addSpecification("event handler", "eagleCompanionEvent");
    addSpecification("command template", "call eagle");
    addSpecification("use composite message", "##InitiatorName## "
        "##Infinitive::raise## ##InitiatorPossessive## arm and "
        "##Infinitive::emit## a sharp, piercing cry. A moment later "
        "a great eagle descends from the sky in a rush of wings, "
        "landing with casual grace beside ##InitiatorName##, its "
        "golden eyes bright with fierce intelligence.");
}
