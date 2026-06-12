//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

protected void Setup()
{
    addSpecification("name", "Beauty Attunement");
    addSpecification("source", "Wrathguard");
    addSpecification("description", "Attunes the disciple to the seductive "
        "power of Seilyndria's divine beauty.");
    addPrerequisite("level",
        (["type": "level", "guild": "Wrathguard", "value": 1]));
    addPrerequisite("/guilds/wrathguard/combat/vain-beauty-root.c",
        (["type": "research"]));
    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);
    addSpecification("bonus manipulation", 2);
    addSpecification("bonus charisma", 1);
    addSpecification("bonus spell points", 10);
}
