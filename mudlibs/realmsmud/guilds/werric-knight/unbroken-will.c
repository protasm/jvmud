//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/will/iron-discipline.c");
    addChild("/guilds/werric-knight/will/iron-discipline.c",
        "/guilds/werric-knight/will/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/will/ignore-pain.c");
    addChild("/guilds/werric-knight/will/ignore-pain.c",
        "/guilds/werric-knight/will/iron-discipline.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/will/mind-fortress.c");
    addChild("/guilds/werric-knight/will/mind-fortress.c",
        "/guilds/werric-knight/will/ignore-pain.c");

    addResearchElement("/guilds/werric-knight/will/indomitable-spirit.c");
    addChild("/guilds/werric-knight/will/indomitable-spirit.c",
        "/guilds/werric-knight/will/mind-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/will/beyond-mortal-limits.c");
    addChild("/guilds/werric-knight/will/beyond-mortal-limits.c",
        "/guilds/werric-knight/will/indomitable-spirit.c");

    addResearchElement("/guilds/werric-knight/will/force-of-will.c");
    addChild("/guilds/werric-knight/will/force-of-will.c",
        "/guilds/werric-knight/will/beyond-mortal-limits.c");

    addResearchElement("/guilds/werric-knight/will/willpower-manifestation.c");
    addChild("/guilds/werric-knight/will/willpower-manifestation.c",
        "/guilds/werric-knight/will/force-of-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/will/unyielding-endurance.c");
    addChild("/guilds/werric-knight/will/unyielding-endurance.c",
        "/guilds/werric-knight/will/force-of-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/will/will-made-manifest.c");
    addChild("/guilds/werric-knight/will/will-made-manifest.c",
        "/guilds/werric-knight/will/unyielding-endurance.c");

    addResearchElement("/guilds/werric-knight/will/absolute-determination.c");
    addChild("/guilds/werric-knight/will/absolute-determination.c",
        "/guilds/werric-knight/will/will-made-manifest.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/will/shatter-will.c");
    addChild("/guilds/werric-knight/will/shatter-will.c",
        "/guilds/werric-knight/will/absolute-determination.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/werric-knight/will/transcendent-resolve.c");
    addChild("/guilds/werric-knight/will/transcendent-resolve.c",
        "/guilds/werric-knight/will/shatter-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/will/impose-will.c");
    addChild("/guilds/werric-knight/will/impose-will.c",
        "/guilds/werric-knight/will/transcendent-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/will/immortal-will.c");
    addChild("/guilds/werric-knight/will/immortal-will.c",
        "/guilds/werric-knight/will/impose-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/will/avatar-of-determination.c");
    addChild("/guilds/werric-knight/will/avatar-of-determination.c",
        "/guilds/werric-knight/will/immortal-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/will/will-eternal.c");
    addChild("/guilds/werric-knight/will/will-eternal.c",
        "/guilds/werric-knight/will/avatar-of-determination.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Unbroken Will");
    Description("This research tree is available only to Sworn Knights who have "
        "proven their dedication beyond question. It represents advanced disciplines "
        "of willpower and determination that push beyond mortal limits.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/will/root.c");
    TreeRoot("/guilds/werric-knight/will/root.c");

    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
}
