//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

protected void Setup()
{
    addSpecification("name", "Beauty Mastery");
    addSpecification("source", "Wrathguard");
    addSpecification("description", "The practitioner achieves mastery over "
        "the magic of divine beauty, their enchantments becoming near-irresistible.");
    addPrerequisite("level",
        (["type": "level", "guild": "Wrathguard", "value": 19]));
    addPrerequisite("/guilds/wrathguard/combat/beauty-power.c",
        (["type": "research"]));
    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus charisma", 2);
    addSpecification("bonus intelligence", 1);
    addSpecification("bonus spell points", 20);
}
