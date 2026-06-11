//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/guilds/baseGuild.c";

/////////////////////////////////////////////////////////////////////////////
public void SetupGuild()
{
    guildName("Guardian of Khazurath");
    guildClass("combat");
    addPreferredSkillType("combat");

    prohibitedGuildCombinations(({ "Scion of Dhuras", "fighter", "monk",
        "Disciple of Argloth", "Drambor Edlothiad", "Aegis Guard",
        "League of Assassins", "phaedra", "ranger", "rogue", "Werric Knight",
        "Wrathguard", "necromancer", "Hand of Bilanx" }));

    addCriteria("hit points", ([
        "type": "modifier",
        "apply" : "6 every level",
        "begin at level" : 1
    ]));
    addCriteria("spell points", ([
        "type": "modifier",
        "apply" : "1 every level",
        "begin at level" : 1
    ]));
    addCriteria("stamina points", ([
        "type": "modifier",
        "apply" : "4 every level",
        "begin at level" : 1
    ]));

    addCriteria("weapon attack", ([
        "type": "attack",
        "apply" : "1 every 8 levels"
    ]));

    addCriteria("attribute points", ([
        "type": "attribute points",
        "apply": "1 every 5 levels"
    ]));

    addCriteria("skill points", ([
        "type": "skill points",
        "apply" : "5 every level"
    ]));

    addCriteria("initial research points", ([
        "type": "research points",
        "apply": "at level 1"
    ]));

    addCriteria("research points", ([
        "type": "research points",
        "apply" : "1 every level"
    ]));

    addCriteria("supplemental research points", ([
        "type": "research points",
        "apply" : "2 every 5 levels"
    ]));

    //addResearchTree("/guilds/guardian-of-khazurath/axe-mastery.c");
    //addResearchTree("/guilds/guardian-of-khazurath/stone-defense.c");
    //addResearchTree("/guilds/guardian-of-khazurath/dwarven-resilience.c");
    //addResearchTree("/guilds/guardian-of-khazurath/mountain-strength.c");
    //addResearchTree("/guilds/guardian-of-khazurath/keeper-of-khazurath.c");
    //addResearchTree("/guilds/guardian-of-khazurath/unbreakable.c");
    //addResearchTree("/guilds/guardian-of-khazurath/earth-magic.c");
    //addResearchTree("/guilds/guardian-of-khazurath/ancient-oaths.c");

    //addRank("stone guard", ([
    //    "title" : "Stone Guard",
    //    "new rank prerequisite" : ([ "level" : 1 ]),
    //    "hit point bonus" : 30,
    //    "stamina point bonus" : 20,
    //    "description" : "You have joined the defenders of Khazurath."
    //]));

    //addRank("keeper", ([
    //    "title" : "Keeper",
    //    "new rank prerequisite" : ([ "level" : 15, "guilds/guardian-of-khazurath/keeper-of-khazurath.c": 15 ]),
    //    "hit point bonus" : 60,
    //    "stamina point bonus" : 35,
    //    "weapon attack bonus" : 2,
    //    "description" : "You are a sworn Keeper of the ancient dwarven fortress."
    //]));

    //addRank("mountain lord", ([
    //    "title" : "Mountain Lord",
    //    "new rank prerequisite" : ([ "level" : 30, "guilds/guardian-of-khazurath/unbreakable.c":25 ]),
    //    "hit point bonus" : 90,
    //    "stamina point bonus" : 50,
    //    "weapon attack bonus" : 4,
    //    "description" : "As enduring as the mountains themselves, you command respect."
    //]));

    //addRank("warden of khazurath", ([
    //    "title" : "Warden of Khazurath",
    //    "new rank prerequisite" : ([ "level" : 50, "guilds/guardian-of-khazurath/ancient-oaths.c":40 ]),
    //    "hit point bonus" : 120,
    //    "stamina point bonus" : 65,
    //    "weapon attack bonus" : 6,
    //    "description" : "You stand as the ultimate defender, Warden of Khazurath."
    //]));
}
