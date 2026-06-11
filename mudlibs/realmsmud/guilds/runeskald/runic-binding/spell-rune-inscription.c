//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Spell-Rune Inscription");
    addSpecification("source", "runeskald");
    addSpecification("description", "The runeskald inscribes spell-enhancing "
        "runes along their forearms, passively increasing maximum spell "
        "points and spell point recovery.");

    addPrerequisite("/guilds/runeskald/runic-binding/bind-rune-of-warding.c",
        (["type": "research"]));

    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus spell points", 30);
    addSpecification("bonus heal spell points", 3);

}
