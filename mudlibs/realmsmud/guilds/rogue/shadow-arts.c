//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/cloak-of-shadows.c");
    addResearchElement("/guilds/rogue/shadow-arts/lingering-shadow.c");

    addChild("/guilds/rogue/shadow-arts/cloak-of-shadows.c",
        "/guilds/rogue/shadow-arts/root.c");
    addChild("/guilds/rogue/shadow-arts/lingering-shadow.c",
        "/guilds/rogue/shadow-arts/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-meld.c");

    addChild("/guilds/rogue/shadow-arts/shadow-meld.c",
        "/guilds/rogue/shadow-arts/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/deeper-darkness.c");
    addResearchElement("/guilds/rogue/shadow-arts/penumbral-focus.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-step.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-strike.c");

    addChild("/guilds/rogue/shadow-arts/deeper-darkness.c",
        "/guilds/rogue/shadow-arts/shadow-meld.c");
    addChild("/guilds/rogue/shadow-arts/penumbral-focus.c",
        "/guilds/rogue/shadow-arts/lingering-shadow.c");
    addChild("/guilds/rogue/shadow-arts/shadow-step.c",
        "/guilds/rogue/shadow-arts/shadow-meld.c");
    addChild("/guilds/rogue/shadow-arts/shadow-strike.c",
        "/guilds/rogue/shadow-arts/shadow-meld.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/umbral-assault.c");
    addResearchElement("/guilds/rogue/shadow-arts/fade.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-veil.c");

    addChild("/guilds/rogue/shadow-arts/umbral-assault.c",
        "/guilds/rogue/shadow-arts/cloak-of-shadows.c");
    addChild("/guilds/rogue/shadow-arts/fade.c",
        "/guilds/rogue/shadow-arts/deeper-darkness.c");
    addChild("/guilds/rogue/shadow-arts/shadow-veil.c",
        "/guilds/rogue/shadow-arts/deeper-darkness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/umbral-precision.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-dance.c");
    addResearchElement("/guilds/rogue/shadow-arts/phantom-presence.c");

    addChild("/guilds/rogue/shadow-arts/umbral-precision.c",
        "/guilds/rogue/shadow-arts/penumbral-focus.c");
    addChild("/guilds/rogue/shadow-arts/shadow-dance.c",
        "/guilds/rogue/shadow-arts/fade.c");
    addChild("/guilds/rogue/shadow-arts/phantom-presence.c",
        "/guilds/rogue/shadow-arts/fade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-weaving.c");

    addChild("/guilds/rogue/shadow-arts/shadow-weaving.c",
        "/guilds/rogue/shadow-arts/umbral-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-burst.c");
    addResearchElement("/guilds/rogue/shadow-arts/void-strike.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-mastery.c");

    addChild("/guilds/rogue/shadow-arts/shadow-burst.c",
        "/guilds/rogue/shadow-arts/shadow-weaving.c");
    addChild("/guilds/rogue/shadow-arts/void-strike.c",
        "/guilds/rogue/shadow-arts/shadow-weaving.c");
    addChild("/guilds/rogue/shadow-arts/shadow-mastery.c",
        "/guilds/rogue/shadow-arts/shadow-weaving.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-storm.c");
    addResearchElement("/guilds/rogue/shadow-arts/living-shadow.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-annihilation.c");
    addResearchElement("/guilds/rogue/shadow-arts/absolute-darkness.c");

    addChild("/guilds/rogue/shadow-arts/shadow-storm.c",
        "/guilds/rogue/shadow-arts/phantom-presence.c");
    addChild("/guilds/rogue/shadow-arts/living-shadow.c",
        "/guilds/rogue/shadow-arts/phantom-presence.c");
    addChild("/guilds/rogue/shadow-arts/shadow-annihilation.c",
        "/guilds/rogue/shadow-arts/shadow-mastery.c");
    addChild("/guilds/rogue/shadow-arts/absolute-darkness.c",
        "/guilds/rogue/shadow-arts/shadow-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-mastery-ii.c");
    addResearchElement("/guilds/rogue/shadow-arts/ethereal-shadow.c");

    addChild("/guilds/rogue/shadow-arts/shadow-mastery-ii.c",
        "/guilds/rogue/shadow-arts/shadow-mastery.c");
    addChild("/guilds/rogue/shadow-arts/ethereal-shadow.c",
        "/guilds/rogue/shadow-arts/phantom-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-mastery-iii.c");
    addResearchElement("/guilds/rogue/shadow-arts/greater-shadow-step.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-lord.c");

    addChild("/guilds/rogue/shadow-arts/shadow-mastery-iii.c",
        "/guilds/rogue/shadow-arts/shadow-mastery-ii.c");
    addChild("/guilds/rogue/shadow-arts/greater-shadow-step.c",
        "/guilds/rogue/shadow-arts/shadow-mastery-ii.c");
    addChild("/guilds/rogue/shadow-arts/shadow-lord.c",
        "/guilds/rogue/shadow-arts/ethereal-shadow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-mastery-iv.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-transcendence.c");

    addChild("/guilds/rogue/shadow-arts/shadow-mastery-iv.c",
        "/guilds/rogue/shadow-arts/shadow-mastery-iii.c");
    addChild("/guilds/rogue/shadow-arts/shadow-transcendence.c",
        "/guilds/rogue/shadow-arts/shadow-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-mastery-v.c");
    addResearchElement("/guilds/rogue/shadow-arts/penumbral-mastery.c");

    addChild("/guilds/rogue/shadow-arts/shadow-mastery-v.c",
        "/guilds/rogue/shadow-arts/shadow-mastery-iv.c");
    addChild("/guilds/rogue/shadow-arts/penumbral-mastery.c",
        "/guilds/rogue/shadow-arts/shadow-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-mastery-vi.c");
    addResearchElement("/guilds/rogue/shadow-arts/shadow-perfection.c");

    addChild("/guilds/rogue/shadow-arts/shadow-mastery-vi.c",
        "/guilds/rogue/shadow-arts/shadow-mastery-v.c");
    addChild("/guilds/rogue/shadow-arts/shadow-perfection.c",
        "/guilds/rogue/shadow-arts/penumbral-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-mastery-vii.c");
    addResearchElement("/guilds/rogue/shadow-arts/one-with-shadow.c");

    addChild("/guilds/rogue/shadow-arts/shadow-mastery-vii.c",
        "/guilds/rogue/shadow-arts/shadow-mastery-vi.c");
    addChild("/guilds/rogue/shadow-arts/one-with-shadow.c",
        "/guilds/rogue/shadow-arts/shadow-perfection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/umbral-transcendence.c");

    addChild("/guilds/rogue/shadow-arts/umbral-transcendence.c",
        "/guilds/rogue/shadow-arts/one-with-shadow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/living-darkness.c");

    addChild("/guilds/rogue/shadow-arts/living-darkness.c",
        "/guilds/rogue/shadow-arts/umbral-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/rogue/shadow-arts/shadow-incarnate.c");

    addChild("/guilds/rogue/shadow-arts/shadow-incarnate.c",
        "/guilds/rogue/shadow-arts/living-darkness.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Shadow Arts");
    Description("This research tree provides the rogue with "
        "shadow manipulation techniques.");
    Source("rogue");
    addResearchElement("/guilds/rogue/shadow-arts/root.c");
    TreeRoot("/guilds/rogue/shadow-arts/root.c");

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
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
}
