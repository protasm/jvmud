//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/guilds/baseGuild.c";

/////////////////////////////////////////////////////////////////////////////
public void SetupGuild()
{
    guildName("Disciple of Argloth");
    guildClass("combat");
    addPreferredSkillType("magic");

    addRank("acolyte", ([
        "name": "acolyte",
        "title": "the Disciple of Argloth",
        "pretitle": "Acolyte",
        "next rank": "death scholar",
        "delay for next promotion": 1200
    ]));

    addRank("death scholar", ([
        "name": "death scholar",
        "title": "the Disciple of Argloth",
        "pretitle": "Death Scholar",
        "previous rank": "acolyte",
        "next rank": "necromancer adept",
        "delay for next promotion": 3600
    ]));

    addRank("necromancer adept", ([
        "name": "necromancer adept",
        "title": "the Disciple of Argloth",
        "pretitle": "Necromancer Adept",
        "previous rank": "death scholar",
        "next rank": "high necromancer",
        "delay for next promotion": 7200
    ]));

    addRank("high necromancer", ([
        "name": "high necromancer",
        "title": "the Disciple of Argloth",
        "pretitle": "High Necromancer",
        "previous rank": "necromancer adept",
    ]));

    setDefaultRank("acolyte");

    prohibitedGuildCombinations(({ "Scion of Dhuras", "fighter", "monk",
        "Drambor Edlothiad", "Guardian of Khazurath", "Children of Ilyrth",
        "League of Assassins", "phaedra", "ranger", "rogue", "Werric Knight",
        "Wrathguard", "necromancer", "Aegis Guard", "Disciple of Ferianth",
        "druid", "Hand of Bilanx" }));

    addCriteria("hit points", ([
        "type": "modifier",
        "apply": "3 every level",
        "begin at level": 1
    ]));

    addCriteria("spell points", ([
        "type": "modifier",
        "apply": "4 every level",
        "begin at level": 1
    ]));

    addCriteria("stamina points", ([
        "type": "modifier",
        "apply": "2 every level",
        "begin at level": 1
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

    addCriteria("blood veil tree", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/disciple-of-argloth/blood-veil/blood-veil.c"
    ]));

    addCriteria("weapon focus", ([
        "type": "research path",
        "name": "The Weapon of the Blood Covenant",
        "description": "Choose the weapon through which you channel Argloth's "
            "blood magic.",
        "apply": "at level 1",
        "research objects": ({
            "/guilds/disciple-of-argloth/paths/dagger.c",
            "/guilds/disciple-of-argloth/paths/staff.c"
        })
    ]));

    addCriteria("blood path (dagger)", ([
        "type": "research tree",
        "apply": "at level 1",
        "apply if chosen": ({ "/guilds/disciple-of-argloth/paths/dagger.c" }),
        "research tree": "/guilds/disciple-of-argloth/paths/dagger/blood.c"
    ]));

    addCriteria("blood path (staff)", ([
        "type": "research tree",
        "apply": "at level 1",
        "apply if chosen": ({ "/guilds/disciple-of-argloth/paths/staff.c" }),
        "research tree": "/guilds/disciple-of-argloth/paths/staff/blood.c"
    ]));

    addCriteria("necromancy tree", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/disciple-of-argloth/necromancy/necromancy.c"
    ]));

    addCriteria("death tree", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/disciple-of-argloth/death/death.c"
    ]));

    addCriteria("dark scholarship tree", ([
        "type": "research tree",
        "apply": "at level 1",
        "research tree": "/guilds/disciple-of-argloth/dark-scholarship/dark-scholarship.c"
    ]));

    addCriteria("shadow tree", ([
        "type": "research tree",
        "apply": "at level 5",
        "research tree": "/guilds/disciple-of-argloth/shadow/shadow.c"
    ]));

    addCriteria("lich form tree", ([
        "type": "research tree",
        "apply": "at level 5",
        "research tree": "/guilds/disciple-of-argloth/lich-form/lich-form.c"
    ]));

    addCriteria("forbidden tree", ([
        "type": "research tree",
        "apply": "at level 15",
        "research tree": "/guilds/disciple-of-argloth/forbidden/forbidden.c"
    ]));

    addCriteria("blood rites rank tree", ([
        "type": "research tree",
        "apply": "at rank death scholar",
        "research tree": "/guilds/disciple-of-argloth/blood-rites/blood-rites.c"
    ]));

    addCriteria("unholy ascendance rank tree", ([
        "type": "research tree",
        "apply": "at rank necromancer adept",
        "research tree": "/guilds/disciple-of-argloth/unholy-ascendance/unholy-ascendance.c"
    ]));

    addCriteria("argloth ascendant rank tree", ([
        "type": "research tree",
        "apply": "at rank high necromancer",
        "research tree": "/guilds/disciple-of-argloth/argloth-ascendant/argloth-ascendant.c"
    ]));
}
