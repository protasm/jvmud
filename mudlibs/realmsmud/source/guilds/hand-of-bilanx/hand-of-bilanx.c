//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/guilds/baseGuild.c";

/////////////////////////////////////////////////////////////////////////////
public void SetupGuild()
{
    guildName("Hand of Bilanx");
    guildClass("combat");
    addPreferredSkillType("magic");

    prohibitedGuildCombinations(({ "Scion of Dhuras", "fighter", "monk",
        "Aegis Guard", "Drambor Edlothiad", "Guardian of Khazurath",
        "Werric Knight", "Wrathguard", "Disciple of Argloth",
        "League of Assassins", "necromancer" }));

    addRank("seeker", ([
        "name": "seeker",
        "title": "the Hand of Bilanx",
        "pretitle": "Seeker",
        "next rank": "adept of balance",
        "delay for next promotion": 1200
    ]));

    addRank("adept of balance", ([
        "name": "adept of balance",
        "title": "the Hand of Bilanx",
        "pretitle": "Adept of Balance",
        "previous rank": "seeker",
        "next rank": "master of duality",
        "delay for next promotion": 3600
    ]));

    addRank("master of duality", ([
        "name": "master of duality",
        "title": "the Hand of Bilanx",
        "pretitle": "Master of Duality",
        "previous rank": "adept of balance",
        "next rank": "hand of bilanx",
        "delay for next promotion": 7200
    ]));

    addRank("hand of bilanx", ([
        "name": "hand of bilanx",
        "title": ", Hand of Bilanx",
        "pretitle": "Hand of Bilanx",
        "previous rank": "master of duality"
    ]));

    setDefaultRank("seeker");

    addCriteria("hit points", ([
        "type": "modifier",
        "apply": "4 every level",
        "begin at level": 1
    ]));

    addCriteria("spell points", ([
        "type": "modifier",
        "apply": "4 every level",
        "begin at level": 1
    ]));

    addCriteria("stamina points", ([
        "type": "modifier",
        "apply": "4 every level",
        "begin at level": 1
    ]));

    addCriteria("weapon attack", ([
        "type": "attack",
        "apply": "1 every 12 levels"
    ]));

    addCriteria("attribute points", ([
        "type": "attribute points",
        "apply": "1 every 5 levels"
    ]));

    addCriteria("skill points", ([
        "type": "skill points",
        "apply": "5 every level"
    ]));

    addCriteria("initial research points", ([
        "type": "research points",
        "apply": "5 at level 1"
    ]));

    addCriteria("research points", ([
        "type": "research points",
        "apply": "1 every level"
    ]));

    addCriteria("supplemental research points", ([
        "type": "research points",
        "apply": "2 every 5 levels"
    ]));

    addCriteria("equilibrium", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/hand-of-bilanx/equilibrium.c"
    ]));

    addCriteria("balance magic", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/hand-of-bilanx/balance-magic.c"
    ]));

    addCriteria("harmony", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/hand-of-bilanx/harmony.c"
    ]));

    addCriteria("duality", ([
        "type": "research tree",
        "apply": "at level 3",
        "research tree": "/guilds/hand-of-bilanx/duality.c"
    ]));

    addCriteria("restoration", ([
        "type": "research tree",
        "apply": "at level 3",
        "research tree": "/guilds/hand-of-bilanx/restoration.c"
    ]));

    addCriteria("neutrality", ([
        "type": "research tree",
        "apply": "at level 5",
        "research tree": "/guilds/hand-of-bilanx/neutrality.c"
    ]));

    addCriteria("cosmic order", ([
        "type": "research tree",
        "apply": "at rank adept of balance",
        "research tree": "/guilds/hand-of-bilanx/cosmic-order.c"
    ]));

    addCriteria("perfect balance", ([
        "type": "research tree",
        "apply": "at rank master of duality",
        "research tree": "/guilds/hand-of-bilanx/perfect-balance.c"
    ]));
}
