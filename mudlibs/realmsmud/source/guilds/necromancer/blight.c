//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/blight/wither.c");
    addResearchElement("/guilds/necromancer/blight/death-shroud.c");

    addChild("/guilds/necromancer/blight/wither.c",
        "/guilds/necromancer/blight/root.c");
    addChild("/guilds/necromancer/blight/death-shroud.c",
        "/guilds/necromancer/blight/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/blight/necrotic-aura.c");

    addChild("/guilds/necromancer/blight/necrotic-aura.c",
        "/guilds/necromancer/blight/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/blight/bone-chill.c");
    addResearchElement("/guilds/necromancer/blight/shadow-veil.c");

    addChild("/guilds/necromancer/blight/bone-chill.c",
        "/guilds/necromancer/blight/wither.c");
    addChild("/guilds/necromancer/blight/shadow-veil.c",
        "/guilds/necromancer/blight/death-shroud.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/blight/curse-of-weakness.c");

    addChild("/guilds/necromancer/blight/curse-of-weakness.c",
        "/guilds/necromancer/blight/necrotic-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/blight/decrepify.c");
    addResearchElement("/guilds/necromancer/blight/blight-insight.c");

    addChild("/guilds/necromancer/blight/decrepify.c",
        "/guilds/necromancer/blight/bone-chill.c");
    addChild("/guilds/necromancer/blight/blight-insight.c",
        "/guilds/necromancer/blight/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/blight/death-mark.c");
    addResearchElement("/guilds/necromancer/blight/blight-mastery.c");

    addChild("/guilds/necromancer/blight/death-mark.c",
        "/guilds/necromancer/blight/curse-of-weakness.c");
    addChild("/guilds/necromancer/blight/blight-mastery.c",
        "/guilds/necromancer/blight/blight-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/blight/plague-touch.c");
    addResearchElement("/guilds/necromancer/blight/shadow-embrace.c");

    addChild("/guilds/necromancer/blight/plague-touch.c",
        "/guilds/necromancer/blight/decrepify.c");
    addChild("/guilds/necromancer/blight/shadow-embrace.c",
        "/guilds/necromancer/blight/shadow-veil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/blight/corruption.c");
    addResearchElement("/guilds/necromancer/blight/blight-dominion.c");

    addChild("/guilds/necromancer/blight/corruption.c",
        "/guilds/necromancer/blight/death-mark.c");
    addChild("/guilds/necromancer/blight/blight-dominion.c",
        "/guilds/necromancer/blight/blight-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/blight/wail-of-the-damned.c");
    addResearchElement("/guilds/necromancer/blight/gravewarden.c");

    addChild("/guilds/necromancer/blight/wail-of-the-damned.c",
        "/guilds/necromancer/blight/plague-touch.c");
    addChild("/guilds/necromancer/blight/gravewarden.c",
        "/guilds/necromancer/blight/shadow-embrace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/blight/necrotic-plague.c");
    addResearchElement("/guilds/necromancer/blight/blight-cataclysm.c");

    addChild("/guilds/necromancer/blight/necrotic-plague.c",
        "/guilds/necromancer/blight/corruption.c");
    addChild("/guilds/necromancer/blight/blight-cataclysm.c",
        "/guilds/necromancer/blight/blight-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/blight/wasting-curse.c");
    addResearchElement("/guilds/necromancer/blight/necromancers-insight.c");

    addChild("/guilds/necromancer/blight/wasting-curse.c",
        "/guilds/necromancer/blight/wail-of-the-damned.c");
    addChild("/guilds/necromancer/blight/necromancers-insight.c",
        "/guilds/necromancer/blight/blight-cataclysm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/necromancer/blight/death-ward.c");
    addResearchElement("/guilds/necromancer/blight/blighted-strike.c");

    addChild("/guilds/necromancer/blight/death-ward.c",
        "/guilds/necromancer/blight/gravewarden.c");
    addChild("/guilds/necromancer/blight/blighted-strike.c",
        "/guilds/necromancer/blight/necrotic-plague.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/necromancer/blight/entropy-aura.c");
    addResearchElement("/guilds/necromancer/blight/gathering-decay.c");

    addChild("/guilds/necromancer/blight/entropy-aura.c",
        "/guilds/necromancer/blight/wasting-curse.c");
    addChild("/guilds/necromancer/blight/gathering-decay.c",
        "/guilds/necromancer/blight/necromancers-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/necromancer/blight/undead-resilience.c");
    addResearchElement("/guilds/necromancer/blight/consuming-blight.c");

    addChild("/guilds/necromancer/blight/undead-resilience.c",
        "/guilds/necromancer/blight/death-ward.c");
    addChild("/guilds/necromancer/blight/consuming-blight.c",
        "/guilds/necromancer/blight/blighted-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/necromancer/blight/death-convergence.c");
    addResearchElement("/guilds/necromancer/blight/decay-mastery.c");

    addChild("/guilds/necromancer/blight/death-convergence.c",
        "/guilds/necromancer/blight/entropy-aura.c");
    addChild("/guilds/necromancer/blight/decay-mastery.c",
        "/guilds/necromancer/blight/gathering-decay.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/necromancer/blight/lord-of-blight.c");
    addResearchElement("/guilds/necromancer/blight/death-convergence-mastery.c");

    addChild("/guilds/necromancer/blight/lord-of-blight.c",
        "/guilds/necromancer/blight/undead-resilience.c");
    addChild("/guilds/necromancer/blight/death-convergence-mastery.c",
        "/guilds/necromancer/blight/death-convergence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/necromancer/blight/dread-visage.c");
    addResearchElement("/guilds/necromancer/blight/shadow-conduit.c");

    addChild("/guilds/necromancer/blight/dread-visage.c",
        "/guilds/necromancer/blight/consuming-blight.c");
    addChild("/guilds/necromancer/blight/shadow-conduit.c",
        "/guilds/necromancer/blight/decay-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/necromancer/blight/soul-drain-aura.c");

    addChild("/guilds/necromancer/blight/soul-drain-aura.c",
        "/guilds/necromancer/blight/lord-of-blight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/necromancer/blight/void-corruption.c");

    addChild("/guilds/necromancer/blight/void-corruption.c",
        "/guilds/necromancer/blight/dread-visage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/necromancer/blight/shadow-dominion.c");

    addChild("/guilds/necromancer/blight/shadow-dominion.c",
        "/guilds/necromancer/blight/shadow-conduit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/necromancer/blight/plague-of-ages.c");

    addChild("/guilds/necromancer/blight/plague-of-ages.c",
        "/guilds/necromancer/blight/void-corruption.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/necromancer/blight/absolute-decay.c");

    addChild("/guilds/necromancer/blight/absolute-decay.c",
        "/guilds/necromancer/blight/soul-drain-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/necromancer/blight/death-dominion.c");

    addChild("/guilds/necromancer/blight/death-dominion.c",
        "/guilds/necromancer/blight/plague-of-ages.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Path of Blight");
    Description("This research tree provides knowledge of curses, debuffs, "
        "and the withering power of decay for the necromancer.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/blight/root.c");
    TreeRoot("/guilds/necromancer/blight/root.c");

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
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyFifthLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
}
