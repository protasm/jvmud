//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-precision.c");
    addResearchElement("/guilds/ranger/ranger-lord/lords-resilience.c");
    addResearchElement("/guilds/ranger/ranger-lord/lords-swiftness.c");

    addChild("/guilds/ranger/ranger-lord/lords-precision.c",
        "/guilds/ranger/ranger-lord/root.c");
    addChild("/guilds/ranger/ranger-lord/lords-resilience.c",
        "/guilds/ranger/ranger-lord/root.c");
    addChild("/guilds/ranger/ranger-lord/lords-swiftness.c",
        "/guilds/ranger/ranger-lord/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-arrow.c");

    addChild("/guilds/ranger/ranger-lord/lords-arrow.c",
        "/guilds/ranger/ranger-lord/lords-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-might.c");
    addResearchElement("/guilds/ranger/ranger-lord/lords-endurance.c");

    addChild("/guilds/ranger/ranger-lord/lords-might.c",
        "/guilds/ranger/ranger-lord/lords-precision.c");
    addChild("/guilds/ranger/ranger-lord/lords-endurance.c",
        "/guilds/ranger/ranger-lord/lords-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-grace.c");

    addChild("/guilds/ranger/ranger-lord/lords-grace.c",
        "/guilds/ranger/ranger-lord/lords-swiftness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-focus.c");

    addChild("/guilds/ranger/ranger-lord/lords-focus.c",
        "/guilds/ranger/ranger-lord/lords-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-eye.c");

    addChild("/guilds/ranger/ranger-lord/lords-eye.c",
        "/guilds/ranger/ranger-lord/lords-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lordly-volley.c");

    addChild("/guilds/ranger/ranger-lord/lordly-volley.c",
        "/guilds/ranger/ranger-lord/lords-eye.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-wrath.c");

    addChild("/guilds/ranger/ranger-lord/lords-wrath.c",
        "/guilds/ranger/ranger-lord/lords-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-aegis.c");

    addChild("/guilds/ranger/ranger-lord/lords-aegis.c",
        "/guilds/ranger/ranger-lord/lords-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-celerity.c");
    addResearchElement("/guilds/ranger/ranger-lord/lords-art.c");

    addChild("/guilds/ranger/ranger-lord/lords-celerity.c",
        "/guilds/ranger/ranger-lord/lords-grace.c");
    addChild("/guilds/ranger/ranger-lord/lords-art.c",
        "/guilds/ranger/ranger-lord/lords-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/sovereign-strike.c");

    addChild("/guilds/ranger/ranger-lord/sovereign-strike.c",
        "/guilds/ranger/ranger-lord/lords-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-devastation.c");

    addChild("/guilds/ranger/ranger-lord/lords-devastation.c",
        "/guilds/ranger/ranger-lord/lords-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-immortality.c");
    addResearchElement("/guilds/ranger/ranger-lord/lords-mastery-lore.c");

    addChild("/guilds/ranger/ranger-lord/lords-immortality.c",
        "/guilds/ranger/ranger-lord/lords-aegis.c");
    addChild("/guilds/ranger/ranger-lord/lords-mastery-lore.c",
        "/guilds/ranger/ranger-lord/lords-devastation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-judgment.c");

    addChild("/guilds/ranger/ranger-lord/lords-judgment.c",
        "/guilds/ranger/ranger-lord/lords-devastation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-perfection.c");

    addChild("/guilds/ranger/ranger-lord/lords-perfection.c",
        "/guilds/ranger/ranger-lord/lords-devastation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/supreme-lord-lore.c");

    addChild("/guilds/ranger/ranger-lord/supreme-lord-lore.c",
        "/guilds/ranger/ranger-lord/lords-perfection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-supremacy.c");

    addChild("/guilds/ranger/ranger-lord/lords-supremacy.c",
        "/guilds/ranger/ranger-lord/lords-perfection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/divine-arrow.c");

    addChild("/guilds/ranger/ranger-lord/divine-arrow.c",
        "/guilds/ranger/ranger-lord/lords-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-transcendence.c");

    addChild("/guilds/ranger/ranger-lord/lords-transcendence.c",
        "/guilds/ranger/ranger-lord/lords-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/eternal-lord-lore.c");

    addChild("/guilds/ranger/ranger-lord/eternal-lord-lore.c",
        "/guilds/ranger/ranger-lord/lords-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/lords-apotheosis.c");

    addChild("/guilds/ranger/ranger-lord/lords-apotheosis.c",
        "/guilds/ranger/ranger-lord/lords-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/apocalypse-shot.c");

    addChild("/guilds/ranger/ranger-lord/apocalypse-shot.c",
        "/guilds/ranger/ranger-lord/lords-apotheosis.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/eternal-lord.c");
    addResearchElement("/guilds/ranger/ranger-lord/peerless-lord-lore.c");

    addChild("/guilds/ranger/ranger-lord/eternal-lord.c",
        "/guilds/ranger/ranger-lord/lords-apotheosis.c");
    addChild("/guilds/ranger/ranger-lord/peerless-lord-lore.c",
        "/guilds/ranger/ranger-lord/eternal-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyLevel()
{
    addResearchElement("/guilds/ranger/ranger-lord/peerless-lord.c");
    addResearchElement("/guilds/ranger/ranger-lord/rangers-judgment.c");

    addChild("/guilds/ranger/ranger-lord/peerless-lord.c",
        "/guilds/ranger/ranger-lord/eternal-lord.c");
    addChild("/guilds/ranger/ranger-lord/rangers-judgment.c",
        "/guilds/ranger/ranger-lord/peerless-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Ranger Lord");
    Description("This research tree provides the ranger lord with "
        "the ultimate techniques of ranger mastery - the "
        "pinnacle of archery and combat.");
    Source("ranger");
    addResearchElement("/guilds/ranger/ranger-lord/root.c");
    TreeRoot("/guilds/ranger/ranger-lord/root.c");

    FifthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
    SeventyLevel();
}
