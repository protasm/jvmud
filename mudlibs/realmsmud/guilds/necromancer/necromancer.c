//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/guilds/baseGuild.c";

/////////////////////////////////////////////////////////////////////////////
public void SetupGuild()
{
    guildName("necromancer");
    guildClass("combat");
    addPreferredSkillType("magic");

    addCriteria("hit points", ([
        "type": "modifier",
        "apply" : "2 every level",
        "begin at level" : 1
    ]));

    addCriteria("spell points", ([
        "type": "modifier",
        "apply" : "6 every level",
        "begin at level" : 1
    ]));

    addCriteria("stamina points", ([
        "type": "modifier",
        "apply" : "2 every level",
        "begin at level" : 1
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

    addCriteria("death", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/necromancer/death.c"
    ]));

    addCriteria("blight", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/necromancer/blight.c"
    ]));

    addCriteria("forms", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/necromancer/forms.c"
    ]));

    addCriteria("functions", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/necromancer/functions.c"
    ]));

    addCriteria("effects", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/necromancer/effects.c"
    ]));

    addCriteria("construct", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/necromancer/construct.c"
    ]));

    addCriteria("zombie horde", ([
        "type": "research tree",
        "apply": "at level 5",
        "research tree": "/guilds/necromancer/zombie-horde.c"
    ]));

    addCriteria("skeleton legion", ([
        "type": "research tree",
        "apply": "at level 5",
        "research tree": "/guilds/necromancer/skeleton-legion.c"
    ]));

    addCriteria("wraith haunt", ([
        "type": "research tree",
        "apply": "at level 10",
        "research tree": "/guilds/necromancer/wraith-haunt.c"
    ]));

    addCriteria("ghoul pack", ([
        "type": "research tree",
        "apply": "at level 10",
        "research tree": "/guilds/necromancer/ghoul-pack.c"
    ]));
}
