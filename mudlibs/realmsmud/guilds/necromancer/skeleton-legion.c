//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bleached-bones.c");
    addResearchElement("/guilds/necromancer/skeleton/stripped-flesh.c");

    addChild("/guilds/necromancer/skeleton/bleached-bones.c",
    "/guilds/necromancer/skeleton/root.c");
    addChild("/guilds/necromancer/skeleton/stripped-flesh.c",
    "/guilds/necromancer/skeleton/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/razor-bones.c");
    addResearchElement("/guilds/necromancer/skeleton/reinforced-skeleton.c");
    addResearchElement("/guilds/necromancer/skeleton/skeletal-speed.c");

    addChild("/guilds/necromancer/skeleton/razor-bones.c",
    "/guilds/necromancer/skeleton/bleached-bones.c");
    addChild("/guilds/necromancer/skeleton/reinforced-skeleton.c",
    "/guilds/necromancer/skeleton/stripped-flesh.c");
    addChild("/guilds/necromancer/skeleton/skeletal-speed.c",
    "/guilds/necromancer/skeleton/stripped-flesh.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-spear.c");
    addResearchElement("/guilds/necromancer/skeleton/iron-skeleton.c");
    addResearchElement("/guilds/necromancer/skeleton/undying-speed.c");
    addResearchElement("/guilds/necromancer/skeleton/skeletal-legion.c");

    addChild("/guilds/necromancer/skeleton/bone-spear.c",
        "/guilds/necromancer/skeleton/razor-bones.c");
    addChild("/guilds/necromancer/skeleton/iron-skeleton.c",
        "/guilds/necromancer/skeleton/reinforced-skeleton.c");
    addChild("/guilds/necromancer/skeleton/undying-speed.c",
        "/guilds/necromancer/skeleton/skeletal-speed.c");
    addChild("/guilds/necromancer/skeleton/skeletal-legion.c",
        "/guilds/necromancer/skeleton/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-shatter.c");
    addResearchElement("/guilds/necromancer/skeleton/adamantine-bones.c");
    addResearchElement("/guilds/necromancer/skeleton/blinding-speed.c");
    addResearchElement("/guilds/necromancer/skeleton/extra-skeleton.c");

    addChild("/guilds/necromancer/skeleton/bone-shatter.c",
        "/guilds/necromancer/skeleton/bone-spear.c");
    addChild("/guilds/necromancer/skeleton/adamantine-bones.c",
        "/guilds/necromancer/skeleton/iron-skeleton.c");
    addChild("/guilds/necromancer/skeleton/blinding-speed.c",
        "/guilds/necromancer/skeleton/undying-speed.c");
    addChild("/guilds/necromancer/skeleton/extra-skeleton.c",
        "/guilds/necromancer/skeleton/skeletal-legion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-storm.c");
    addResearchElement("/guilds/necromancer/skeleton/skeletal-commander.c");
    addResearchElement("/guilds/necromancer/skeleton/death-march.c");
    addResearchElement("/guilds/necromancer/skeleton/marrow-shield.c");
    addResearchElement("/guilds/necromancer/skeleton/bone-cage.c");

    addChild("/guilds/necromancer/skeleton/bone-storm.c",
        "/guilds/necromancer/skeleton/bone-shatter.c");
    addChild("/guilds/necromancer/skeleton/skeletal-commander.c",
        "/guilds/necromancer/skeleton/extra-skeleton.c");
    addChild("/guilds/necromancer/skeleton/death-march.c",
        "/guilds/necromancer/skeleton/blinding-speed.c");
    addChild("/guilds/necromancer/skeleton/marrow-shield.c",
        "/guilds/necromancer/skeleton/adamantine-bones.c");
    addChild("/guilds/necromancer/skeleton/bone-cage.c",
        "/guilds/necromancer/skeleton/bone-spear.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-lance.c");
    addResearchElement("/guilds/necromancer/skeleton/undead-general.c");
    addResearchElement("/guilds/necromancer/skeleton/relentless-march.c");
    addResearchElement("/guilds/necromancer/skeleton/skeletal-fortitude.c");
    addResearchElement("/guilds/necromancer/skeleton/bone-fortress.c");

    addChild("/guilds/necromancer/skeleton/bone-lance.c",
        "/guilds/necromancer/skeleton/bone-storm.c");
    addChild("/guilds/necromancer/skeleton/undead-general.c",
        "/guilds/necromancer/skeleton/skeletal-commander.c");
    addChild("/guilds/necromancer/skeleton/relentless-march.c",
        "/guilds/necromancer/skeleton/death-march.c");
    addChild("/guilds/necromancer/skeleton/skeletal-fortitude.c",
        "/guilds/necromancer/skeleton/marrow-shield.c");
    addChild("/guilds/necromancer/skeleton/bone-fortress.c",
        "/guilds/necromancer/skeleton/bone-cage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-barrage.c");
    addResearchElement("/guilds/necromancer/skeleton/warlord-of-bones.c");
    addResearchElement("/guilds/necromancer/skeleton/eternal-march.c");
    addResearchElement("/guilds/necromancer/skeleton/skeletal-dominion.c");

    addChild("/guilds/necromancer/skeleton/bone-barrage.c",
        "/guilds/necromancer/skeleton/bone-lance.c");
    addChild("/guilds/necromancer/skeleton/warlord-of-bones.c",
        "/guilds/necromancer/skeleton/undead-general.c");
    addChild("/guilds/necromancer/skeleton/eternal-march.c",
        "/guilds/necromancer/skeleton/relentless-march.c");
    addChild("/guilds/necromancer/skeleton/skeletal-dominion.c",
        "/guilds/necromancer/skeleton/bone-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-avalanche.c");
    addResearchElement("/guilds/necromancer/skeleton/skull-lord.c");
    addResearchElement("/guilds/necromancer/skeleton/death-legion.c");

    addChild("/guilds/necromancer/skeleton/bone-avalanche.c",
        "/guilds/necromancer/skeleton/bone-barrage.c");
    addChild("/guilds/necromancer/skeleton/skull-lord.c",
        "/guilds/necromancer/skeleton/warlord-of-bones.c");
    addChild("/guilds/necromancer/skeleton/death-legion.c",
        "/guilds/necromancer/skeleton/eternal-march.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-hurricane.c");
    addResearchElement("/guilds/necromancer/skeleton/death-king.c");
    addResearchElement("/guilds/necromancer/skeleton/eternal-legion.c");

    addChild("/guilds/necromancer/skeleton/bone-hurricane.c",
        "/guilds/necromancer/skeleton/bone-avalanche.c");
    addChild("/guilds/necromancer/skeleton/death-king.c",
        "/guilds/necromancer/skeleton/skull-lord.c");
    addChild("/guilds/necromancer/skeleton/eternal-legion.c",
        "/guilds/necromancer/skeleton/death-legion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/bone-maelstrom.c");
    addResearchElement("/guilds/necromancer/skeleton/lich-king.c");

    addChild("/guilds/necromancer/skeleton/bone-maelstrom.c",
        "/guilds/necromancer/skeleton/bone-hurricane.c");
    addChild("/guilds/necromancer/skeleton/lich-king.c",
        "/guilds/necromancer/skeleton/death-king.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/skeleton/lord-of-the-legion.c");

    addChild("/guilds/necromancer/skeleton/lord-of-the-legion.c",
        "/guilds/necromancer/skeleton/lich-king.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Skeleton Legion");
    Description("This research tree provides the knowledge necessary for "
        "raising and enhancing skeletal minions.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/skeleton/root.c");
    TreeRoot("/guilds/necromancer/skeleton/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
}
