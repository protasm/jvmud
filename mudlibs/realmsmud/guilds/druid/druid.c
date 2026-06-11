//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/guilds/baseGuild.c";

/////////////////////////////////////////////////////////////////////////////
public void SetupGuild()
{
    guildName("druid");
    guildClass("combat");
    addPreferredSkillType("magic");
    prohibitedGuildCombinations(({ "Scion of Dhuras", "monk" }));

    addCriteria("hit points", ([
        "type": "modifier",
        "apply" : "4 every level",
        "begin at level" : 1
    ]));

    addCriteria("spell points", ([
        "type": "modifier",
        "apply" : "3 every level",
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

    addCriteria("wolf companion", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/companions/wolf-companion.c"
    ]));

    addCriteria("bear companion", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/companions/bear-companion.c"
    ]));

    addCriteria("panther companion", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/companions/cat-companion.c"
    ]));

    addCriteria("eagle companion", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/companions/eagle-companion.c"
    ]));

    addCriteria("natures wrath", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/natures-wrath.c"
    ]));

    addCriteria("primal storm", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/primal-storm.c"
    ]));

    addCriteria("wild shape", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/wild-shape.c"
    ]));

    addCriteria("ancient ward", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/ancient-ward.c"
    ]));

    addCriteria("verdant growth", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/verdant-growth.c"
    ]));

    addCriteria("primal rites", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/druid/primal-rites.c"
    ]));
}
