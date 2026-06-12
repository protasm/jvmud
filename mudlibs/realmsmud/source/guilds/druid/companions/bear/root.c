//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/summoningResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Call Bear Companion");
    addSpecification("source", "druid");

    addSpecification("description", "This skill provides the druid with the "
        "knowledge of how to call forth a bear companion from the wilds. "
        "The bear is bound to the druid through a pact of primal strength, "
        "growing into an unstoppable force of nature as the druid deepens "
        "their connection to the ancient wild.");

    addSpecification("number to summon", 1);
    addSpecification("maximum that can be summoned", 1);
    addSpecification("persona", "black bear");
    addSpecification("persona level", 5);

    addSpecification("modifiers", ([
        "/guilds/druid/companions/bear/might-i.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/endurance-i.c":({
            (["modifier": "bonus hit points",
               "value": 100 ]),
        }),
        "/guilds/druid/companions/bear/iron-hide-i.c":({
            (["modifier": "bonus soak",
               "value": 2 ]),
            (["modifier": "bonus no armor",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/heavy-blow.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/bear/crushing-blow-spell.c" ]),
        }),
        "/guilds/druid/companions/bear/thick-skull.c":({
            (["modifier": "bonus defense",
               "value": 4 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/mountain-stance.c":({
            (["modifier": "bonus soak",
               "value": 1 ]),
            (["modifier": "bonus defense",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/might-ii.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/endurance-ii.c":({
            (["modifier": "bonus hit points",
               "value": 125 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/iron-hide-ii.c":({
            (["modifier": "bonus soak",
               "value": 2 ]),
            (["modifier": "bonus no armor",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/heavy-blow-ii.c":({
            (["modifier": "bonus damage",
               "value": 3 ]),
            (["modifier": "bonus attack",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/thick-skull-ii.c":({
            (["modifier": "bonus defense",
               "value": 4 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/crushing-charge.c":({
            (["modifier": "bonus attack",
               "value": 3 ]),
            (["modifier": "bonus damage",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/might-iii.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/endurance-iii.c":({
            (["modifier": "bonus hit points",
               "value": 150 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/iron-hide-iii.c":({
            (["modifier": "bonus soak",
               "value": 2 ]),
            (["modifier": "bonus no armor",
               "value": 4 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/heavy-blow-iii.c":({
            (["modifier": "bonus damage",
               "value": 3 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/thick-skull-iii.c":({
            (["modifier": "bonus defense",
               "value": 5 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/primal-roar.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/bear/maul-spell.c" ]),
        }),
        "/guilds/druid/companions/bear/might-iv.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/ursine-fortitude.c":({
            (["modifier": "bonus hit points",
               "value": 150 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/iron-hide-iv.c":({
            (["modifier": "bonus soak",
               "value": 3 ]),
            (["modifier": "bonus no armor",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/bear/bear-hug.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/bear/bear-hug-spell.c" ]),
        }),
        "/guilds/druid/companions/bear/unstoppable.c":({
            (["modifier": "bonus hit points",
               "value": 150 ]),
            (["modifier": "bonus heal hit points",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/earthen-resilience.c":({
            (["modifier": "bonus soak",
               "value": 3 ]),
            (["modifier": "bonus defense",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/might-v.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/endurance-iv.c":({
            (["modifier": "bonus hit points",
               "value": 175 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/iron-hide-v.c":({
            (["modifier": "bonus soak",
               "value": 3 ]),
            (["modifier": "bonus no armor",
               "value": 5 ]),
            (["modifier": "bonus defense",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/mountain-heart.c":({
            (["modifier": "bonus hit points",
               "value": 175 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 3 ]),
        }),
        "/guilds/druid/companions/bear/primal-dominance.c":({
            (["modifier": "bonus attack",
               "value": 4 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/might-vi.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/unyielding-hide.c":({
            (["modifier": "bonus soak",
               "value": 3 ]),
            (["modifier": "bonus no armor",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/earthshaking-blow.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/bear/earthshaking-roar-spell.c" ]),
        }),
        "/guilds/druid/companions/bear/endurance-v.c":({
            (["modifier": "bonus hit points",
               "value": 200 ]),
            (["modifier": "bonus heal hit points",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/iron-will.c":({
            (["modifier": "bonus defense",
               "value": 6 ]),
            (["modifier": "bonus constitution",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/titanic-strength.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/bear/living-fortress.c":({
            (["modifier": "bonus soak",
               "value": 4 ]),
            (["modifier": "bonus no armor",
               "value": 6 ]),
            (["modifier": "bonus defense",
               "value": 6 ]),
        }),
        "/guilds/druid/companions/bear/primal-regeneration.c":({
            (["modifier": "bonus heal hit points",
               "value": 8 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/apex-predator.c":({
            (["modifier": "bonus attack",
               "value": 5 ]),
            (["modifier": "bonus damage",
               "value": 4 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/eternal-endurance.c":({
            (["modifier": "bonus hit points",
               "value": 200 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/stone-skin.c":({
            (["modifier": "bonus soak",
               "value": 4 ]),
            (["modifier": "bonus no armor",
               "value": 6 ]),
        }),
        "/guilds/druid/companions/bear/ursine-rampage.c":({
            (["modifier": "apply research",
               "value":
                "/guilds/druid/companions/bear/primal-slam-spell.c" ]),
        }),
        "/guilds/druid/companions/bear/ancient-resilience.c":({
            (["modifier": "bonus hit points",
               "value": 200 ]),
            (["modifier": "bonus heal hit points",
               "value": 8 ]),
        }),
        "/guilds/druid/companions/bear/fortress-stance.c":({
            (["modifier": "bonus defense",
               "value": 8 ]),
            (["modifier": "bonus soak",
               "value": 4 ]),
        }),
        "/guilds/druid/companions/bear/wrath-of-the-wilds.c":({
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
            (["modifier": "bonus strength",
               "value": 2 ]),
        }),
        "/guilds/druid/companions/bear/eternal-guardian.c":({
            (["modifier": "bonus hit points",
               "value": 250 ]),
            (["modifier": "bonus heal hit points",
               "value": 8 ]),
            (["modifier": "bonus heal hit points rate",
               "value": 5 ]),
        }),
        "/guilds/druid/companions/bear/spirit-of-the-ancient.c":({
            (["modifier": "bonus hit points",
               "value": 250 ]),
            (["modifier": "bonus attack",
               "value": 6 ]),
            (["modifier": "bonus damage",
               "value": 5 ]),
            (["modifier": "bonus defense",
               "value": 8 ]),
            (["modifier": "bonus soak",
               "value": 5 ]),
        }),
    ]));

    addSpecification("scope", "area");
    addSpecification("research type", "tree root");

    addSpecification("spell point cost", 75);
    addSpecification("stamina point cost", 50);

    addSpecification("event handler", "bearCompanionEvent");
    addSpecification("command template", "call bear");
    addSpecification("use composite message", "##InitiatorName## "
        "##Infinitive::raise## ##InitiatorPossessive## hands and "
        "##Infinitive::let## out a deep, rumbling call. The earth "
        "shudders as a great bear emerges from the treeline, its "
        "eyes gleaming with primal awareness as it stands beside "
        "##InitiatorName##.");
}
