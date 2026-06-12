//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

protected void Setup()
{
    addSpecification("name", "Zealot Vigor");
    addSpecification("source", "Wrathguard");
    addSpecification("description", "Divine fervor replenishes the "
        "Zealot's stamina and quickens recovery.");
    addSpecification("research type", "points");
    addSpecification("scope", "self");
    addSpecification("research cost", 1);
    addSpecification("bonus stamina points", 25);
    addSpecification("bonus heal stamina rate", 1);
    addPrerequisite("/guilds/wrathguard/zealotry/zealot-resilience.c",
        (["type": "research"]));
    addPrerequisite("level",
        (["type": "level", "guild": "Wrathguard", "value": 23]));
}
