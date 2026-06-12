//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/guilds/baseGuild.c";

/////////////////////////////////////////////////////////////////////////////
public void SetupGuild()
{
    guildName("ranger");
    guildClass("combat");
    addPreferredSkillType("combat");

    prohibitedGuildCombinations(({ "Scion of Dhuras", "fighter",
        "monk", "Aegis Guard" }));

    addRank("scout", ([
        "name": "scout",
        "title": "the Ranger",
        "pretitle": "Scout",
        "next rank": "pathfinder",
        "delay for next promotion": 1200
    ]));

    addRank("pathfinder", ([
        "name": "pathfinder",
        "title": "the Ranger",
        "pretitle": "Pathfinder",
        "previous rank": "scout",
        "next rank": "warden",
        "delay for next promotion": 3600
    ]));

    addRank("warden", ([
        "name": "warden",
        "title": ", Warden of the Wild",
        "pretitle": "Warden",
        "previous rank": "pathfinder",
        "next rank": "ranger lord",
        "delay for next promotion": 7200
    ]));

    addRank("ranger lord", ([
        "name": "ranger lord",
        "title": ", Ranger Lord",
        "pretitle": "Ranger Lord",
        "previous rank": "warden"
    ]));

    setDefaultRank("scout");

    addCriteria("hit points", ([
        "type": "modifier",
        "apply" : "5 every level",
        "begin at level" : 1
    ]));

    addCriteria("spell points", ([
        "type": "modifier",
        "apply" : "2 every level",
        "begin at level" : 1
    ]));

    addCriteria("stamina points", ([
        "type": "modifier",
        "apply" : "3 every level",
        "begin at level" : 1
    ]));

    addCriteria("weapon attack", ([
        "type": "attack",
        "apply" : "1 every 10 levels"
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

    addCriteria("archery", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/ranger/archery.c"
    ]));

    addCriteria("elemental arrows", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/ranger/elemental-arrows.c"
    ]));

    addCriteria("wilderness survival", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/ranger/wilderness-survival.c"
    ]));

    addCriteria("pathfinding", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/ranger/pathfinding.c"
    ]));

    addCriteria("natures ally", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/ranger/natures-ally.c"
    ]));

    addCriteria("blade work", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/ranger/blade-work.c"
    ]));

    addCriteria("wardens call", ([
        "type": "research tree",
        "apply": "at rank pathfinder",
        "research tree": "/guilds/ranger/wardens-call.c"
    ]));

    addCriteria("wild hunt", ([
        "type": "research tree",
        "apply": "at rank warden",
        "research tree": "/guilds/ranger/wild-hunt.c"
    ]));

    addCriteria("ranger lord", ([
        "type": "research tree",
        "apply": "at rank ranger lord",
        "research tree": "/guilds/ranger/ranger-lord.c"
    ]));
}
