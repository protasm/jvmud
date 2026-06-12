//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/constructedActiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Constructed Cryomancer Spells");
    addSpecification("source", "cryomancer");
    addSpecification("description", "This skill provides the user with the "
        "knowledge of constructing spells that can provide "
        "complex effects.");

    addSpecification("spell point cost", 30);
    addSpecification("spell point cost modifiers", ([
        "/guilds/cryomancer/construct/efficient-casting.c": 10,
        "/guilds/cryomancer/construct/improved-efficiency.c": 10,
        "/guilds/cryomancer/construct/expert-efficiency.c": 10,
        "/guilds/cryomancer/construct/master-efficiency.c": 10,
        "/guilds/cryomancer/construct/grandmaster-efficiency.c": 5,
        "/guilds/cryomancer/construct/legendary-efficiency.c": 5,
    ]));

    addSpecification("constructed rules", ([
        "form must include only one of": ({
            "/guilds/cryomancer/forms/arc.c",
            "/guilds/cryomancer/forms/droplet.c",
            "/guilds/cryomancer/forms/touch.c",
            "/guilds/cryomancer/forms/bolt.c",
            "/guilds/cryomancer/forms/shard.c",
            "/guilds/cryomancer/forms/spear.c",
            "/guilds/cryomancer/forms/spray.c",
            "/guilds/cryomancer/forms/needle.c",
            "/guilds/cryomancer/forms/blast.c",
            "/guilds/cryomancer/forms/mist.c",
            "/guilds/cryomancer/forms/razor.c",
            "/guilds/cryomancer/forms/wave.c",
            "/guilds/cryomancer/forms/tendril.c",
            "/guilds/cryomancer/forms/orb.c",
            "/guilds/cryomancer/forms/ring.c",
            "/guilds/cryomancer/forms/wall.c",
            "/guilds/cryomancer/forms/cage.c",
            "/guilds/cryomancer/forms/lance.c",
            "/guilds/cryomancer/forms/glacier.c",
            "/guilds/cryomancer/forms/strike.c",
            "/guilds/cryomancer/forms/coil.c",
            "/guilds/cryomancer/forms/vortex.c",
            "/guilds/cryomancer/forms/pulse.c",
            "/guilds/cryomancer/forms/avalanche.c",
            "/guilds/cryomancer/forms/blizzard.c",
            "/guilds/cryomancer/forms/icestorm.c",
        }),
        "function must include only one of": ({
            "/guilds/cryomancer/functions/cold.c",
            "/guilds/cryomancer/functions/frost.c",
            "/guilds/cryomancer/functions/ice.c",
            "/guilds/cryomancer/functions/chill.c",
            "/guilds/cryomancer/functions/sleet.c",
            "/guilds/cryomancer/functions/freeze.c",
            "/guilds/cryomancer/functions/hail.c",
            "/guilds/cryomancer/functions/glacial.c",
            "/guilds/cryomancer/functions/blizzard.c",
            "/guilds/cryomancer/functions/permafrost.c",
            "/guilds/cryomancer/functions/void-cold.c",
        }),
        "effect must include any of": ({
            "/guilds/cryomancer/effects/damage-hp.c",
            "/guilds/cryomancer/effects/damage-sp.c",
            "/guilds/cryomancer/effects/damage-stamina.c",
            "/guilds/cryomancer/effects/add-hp.c",
            "/guilds/cryomancer/effects/add-sp.c",
            "/guilds/cryomancer/effects/add-stamina.c",
            "/guilds/cryomancer/effects/siphon-hp.c",
            "/guilds/cryomancer/effects/siphon-sp.c",
            "/guilds/cryomancer/effects/siphon-stamina.c",
            "/guilds/cryomancer/effects/enhance-attack.c",
            "/guilds/cryomancer/effects/enhance-damage.c",
            "/guilds/cryomancer/effects/enhance-defense.c",
            "/guilds/cryomancer/effects/enhance-soak.c",
            "/guilds/cryomancer/effects/enhance-dodge.c",
            "/guilds/cryomancer/effects/enhance-parry.c",
            "/guilds/cryomancer/effects/reduce-attack.c",
            "/guilds/cryomancer/effects/reduce-damage.c",
            "/guilds/cryomancer/effects/reduce-defense.c",
            "/guilds/cryomancer/effects/reduce-soak.c",
            "/guilds/cryomancer/effects/reduce-dodge.c",
            "/guilds/cryomancer/effects/reduce-parry.c",
            "/guilds/cryomancer/effects/apply-slow.c",
            "/guilds/cryomancer/effects/apply-frozen.c",
            "/guilds/cryomancer/effects/staggering-damage.c",
            "/guilds/cryomancer/effects/debilitating-damage.c",
            "/guilds/cryomancer/effects/disorienting-damage.c",
            "/guilds/cryomancer/effects/hampering-damage.c",
            "/guilds/cryomancer/effects/exhausting-damage.c",
            "/guilds/cryomancer/effects/invigorating-siphon.c",
            "/guilds/cryomancer/effects/fortifying-siphon.c",
        }),
    ]));

    addSpecification("cooldown", 60);
    addSpecification("cooldown modifiers", ([
        "/guilds/cryomancer/construct/efficient-casting.c": 10,
        "/guilds/cryomancer/construct/improved-efficiency.c": 10,
        "/guilds/cryomancer/construct/expert-efficiency.c": 10,
        "/guilds/cryomancer/construct/master-efficiency.c": 10,
        "/guilds/cryomancer/construct/grandmaster-efficiency.c": 5,
        "/guilds/cryomancer/construct/legendary-efficiency.c": 5,
    ]));

    addSpecification("scope", "area");
    addSpecification("research type", "tree root");
    addSpecification("command name", "cast");
    addSpecification("command template", "cast cryomancer ##Value##");
    addSpecification("use ability message", "##InitiatorName## "
        "##Infinitive::cast## a spell...");
}
