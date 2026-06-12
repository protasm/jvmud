//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/spirit-war/call-warrior-spirit.c");
    addResearchElement("/guilds/runeskald/spirit-war/call-shield-spirit.c");

    addChild("/guilds/runeskald/spirit-war/call-warrior-spirit.c",
        "/guilds/runeskald/spirit-war/root.c");
    addChild("/guilds/runeskald/spirit-war/call-shield-spirit.c",
        "/guilds/runeskald/spirit-war/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/runeskald/spirit-war/spirit-weapon-strike.c");
    addResearchElement("/guilds/runeskald/spirit-war/spirit-hardening.c");
    addResearchElement("/guilds/runeskald/spirit-war/spirit-armor.c");

    addChild("/guilds/runeskald/spirit-war/spirit-weapon-strike.c",
        "/guilds/runeskald/spirit-war/call-warrior-spirit.c");
    addChild("/guilds/runeskald/spirit-war/spirit-hardening.c",
        "/guilds/runeskald/spirit-war/call-shield-spirit.c");
    addChild("/guilds/runeskald/spirit-war/spirit-armor.c",
        "/guilds/runeskald/spirit-war/call-shield-spirit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/spirit-war/spirit-rend.c");
    addResearchElement("/guilds/runeskald/spirit-war/guardian-spirit-surge.c");
    addResearchElement("/guilds/runeskald/spirit-war/ancestors-war-cry.c");

    addChild("/guilds/runeskald/spirit-war/spirit-rend.c",
        "/guilds/runeskald/spirit-war/spirit-weapon-strike.c");
    addChild("/guilds/runeskald/spirit-war/guardian-spirit-surge.c",
        "/guilds/runeskald/spirit-war/spirit-hardening.c");
    addChild("/guilds/runeskald/spirit-war/ancestors-war-cry.c",
        "/guilds/runeskald/spirit-war/spirit-weapon-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/runeskald/spirit-war/warrior-spirit-bond.c");
    addResearchElement("/guilds/runeskald/spirit-war/spirit-stamina.c");
    addResearchElement("/guilds/runeskald/spirit-war/spirit-storm.c");

    addChild("/guilds/runeskald/spirit-war/warrior-spirit-bond.c",
        "/guilds/runeskald/spirit-war/spirit-rend.c");
    addChild("/guilds/runeskald/spirit-war/spirit-stamina.c",
        "/guilds/runeskald/spirit-war/warrior-spirit-bond.c");
    addChild("/guilds/runeskald/spirit-war/spirit-storm.c",
        "/guilds/runeskald/spirit-war/ancestors-war-cry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/runeskald/spirit-war/spirit-war-mastery.c");

    addChild("/guilds/runeskald/spirit-war/spirit-war-mastery.c",
        "/guilds/runeskald/spirit-war/spirit-storm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/runeskald/spirit-war/twin-spirit-aspect.c");
    addResearchElement("/guilds/runeskald/spirit-war/spirit-volley.c");

    addChild("/guilds/runeskald/spirit-war/twin-spirit-aspect.c",
        "/guilds/runeskald/spirit-war/spirit-war-mastery.c");
    addChild("/guilds/runeskald/spirit-war/spirit-volley.c",
        "/guilds/runeskald/spirit-war/spirit-war-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/spirit-war/eternal-war-spirit.c");

    addChild("/guilds/runeskald/spirit-war/eternal-war-spirit.c",
        "/guilds/runeskald/spirit-war/twin-spirit-aspect.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Spirit War");
    Source("runeskald");
    Description("This tree covers the summoning and commanding of "
        "ancestral warrior spirits to fight alongside the runeskald.");

    addResearchElement("/guilds/runeskald/spirit-war/root.c");
    TreeRoot("/guilds/runeskald/spirit-war/root.c");

    FirstLevel();
    SecondLevel();
    ThirdLevel();
    FourthLevel();
    FifthLevel();
    SixthLevel();
    SeventhLevel();
}
