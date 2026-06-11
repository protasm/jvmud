//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

protected void Setup()
{
    addSpecification("name", "Vengeance Skin");
    addSpecification("source", "Wrathguard");
    addSpecification("description", "The Queen's retributive power "
        "hardens the Wrathguard's flesh, granting soak and armor.");
    addSpecification("research type", "points");
    addSpecification("scope", "self");
    addSpecification("research cost", 1);
    addSpecification("bonus soak", 3);
    addSpecification("bonus armor class", 2);
    addPrerequisite("/guilds/wrathguard/vengeance/retributive-spirit.c",
        (["type": "research"]));
    addPrerequisite("level",
        (["type": "level", "guild": "Wrathguard", "value": 7]));
}
