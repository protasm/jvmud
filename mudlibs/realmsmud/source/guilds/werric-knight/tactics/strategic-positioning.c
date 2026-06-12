//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Strategic Positioning");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "You have learned that position is often more "
        "important than prowess. Proper positioning multiplies effectiveness, "
        "while poor positioning negates even superior skill. This understanding "
        "makes you a more dangerous combatant and a more effective commander.");

    addPrerequisite("guilds/werric-knight/tactics/battle-command.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":9]));

    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus intelligence", 3);
    addSpecification("bonus attack", 2);
    addSpecification("bonus defense", 2);
    addSpecification("bonus dodge", 2);
}
