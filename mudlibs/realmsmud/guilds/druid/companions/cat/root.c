//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/summoningResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Call Panther Companion");
    addSpecification("source", "druid");

    addSpecification("description", "This skill provides the druid with the "
        "knowledge of how to call forth a panther companion from the wilds. "
        "The panther is bound to the druid through a pact of shadow and "
        "predation, growing from a deadly hunter into an avatar of the "
        "primal hunt as the druid deepens their bond with the wild.");

    addSpecification("number to summon", 1);
    addSpecification("maximum that can be summoned", 1);
    addSpecification("persona", "panther");
    addSpecification("persona level", 5);

    addSpecification("modifiers", ([
        "/guilds/druid/companions/cat/predator-i.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/shadow-i.c":({
            (["modifier": "bonus dodge",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/cat/sinew-i.c":({
            (["modifier": "bonus hit points",
               "value": 75 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/razor-claws.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/cat/rake-spell.c" ]),
        }),
        "/guilds/druid/companions/cat/silent-step.c":({
            (["modifier": "bonus dodge",
               "value": 3 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/lithe-frame.c":({
            (["modifier": "bonus parry",
               "value": 3 ]),
            (["modifier": "bonus defense",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/predator-ii.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/shadow-ii.c":({
            (["modifier": "bonus dodge",
               "value": 4 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/sinew-ii.c":({
            (["modifier": "bonus hit points",
               "value": 100 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/ambush-instinct.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/ghost-step.c":({
            (["modifier": "bonus dodge",
               "value": 3 ]),
            (["modifier": "bonus parry",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/iron-sinew.c":({
            (["modifier": "bonus hit points",
               "value": 100 ]),
            (["modifier": "bonus soak",
               "value": 1 ]),
        }),
        "/guilds/druid/companions/cat/predator-iii.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/shadow-iii.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/sinew-iii.c":({
            (["modifier": "bonus hit points",
               "value": 100 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/death-mark.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/void-step.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/cat/stalk-and-strike-spell.c" ]),
        }),
        "/guilds/druid/companions/cat/feral-endurance.c":({
            (["modifier": "bonus hit points",
               "value": 125 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/predator-iv.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/phantom-agility.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus parry",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/sinew-iv.c":({
            (["modifier": "bonus hit points",
               "value": 125 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/eviscerate.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/cat/death-pounce-spell.c" ]),
        }),
        "/guilds/druid/companions/cat/shadow-iv.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/primal-sinew.c":({
            (["modifier": "bonus hit points",
               "value": 150 ]),
            (["modifier": "bonus heal hit points",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/cat/predator-v.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/shadow-v.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/sinew-v.c":({
            (["modifier": "bonus hit points",
               "value": 150 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/lethal-grace.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/shadow-walk.c":({
            (["modifier": "bonus dodge",
               "value": 4 ]),
            (["modifier": "bonus defense",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/cat/predator-vi.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/cat/evasive-mastery.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus parry",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/cat/apex-strike.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/cat/eviscerate-spell.c" ]),
        }),
        "/guilds/druid/companions/cat/ghost-form.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus defense",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/cat/sinew-vi.c":({
            (["modifier": "bonus hit points",
               "value": 175 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/killing-edge.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/cat/spectral-evasion.c":({
            (["modifier": "bonus dodge",
               "value": 5 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/primal-vitality.c":({
            (["modifier": "bonus hit points",
               "value": 175 ]),
            (["modifier": "bonus heal hit points",
               "value": 6 ]),
        }),
        "/guilds/druid/companions/cat/apex-predator.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/phantom-form.c":({
            (["modifier": "bonus dodge",
               "value": 6 ]),
            (["modifier": "bonus defense",
               "value": 6 ]),
        }),
        "/guilds/druid/companions/cat/undying-grace.c":({
            (["modifier": "bonus hit points",
               "value": 175 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/cat/perfect-killer.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/cat/shadow-of-the-wild.c":({
            (["modifier": "bonus dodge",
               "value": 6 ]),
            (["modifier": "bonus parry",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/cat/primal-grace.c":({
            (["modifier": "bonus hit points",
               "value": 200 ]),
            (["modifier": "bonus heal hit points",
               "value": 8 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/cat/death-incarnate.c":({
            (["modifier": "bonus attack",
               "value": 8 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/eternal-shadow.c":({
            (["modifier": "bonus dodge",
               "value": 6 ]),
            (["modifier": "bonus defense",
               "value": 6 ]),
            (["modifier": "bonus dexterity",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/cat/spirit-of-the-hunt.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/cat/phantom-strike-spell.c" ]),
            (["modifier": "bonus attack",
               "value": 8 ]),
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

    addSpecification("event handler", "pantherCompanionEvent");
    addSpecification("command template", "call panther");
    addSpecification("use composite message", "##InitiatorName## "
        "##Infinitive::whisper## a low, rhythmic call into the darkness. "
        "A moment passes in silence before a sleek panther melts out of "
        "the shadows, golden eyes fixed on ##InitiatorName## with calm, "
        "predatory intelligence.");
}
