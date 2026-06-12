//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/cosmic/universal-law.c");
    addChild("/guilds/hand-of-bilanx/cosmic/universal-law.c",
        "/guilds/hand-of-bilanx/cosmic/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/cosmic/cosmic-awareness.c");
    addChild("/guilds/hand-of-bilanx/cosmic/cosmic-awareness.c",
        "/guilds/hand-of-bilanx/cosmic/universal-law.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/cosmic/reality-stabilization.c");
    addChild("/guilds/hand-of-bilanx/cosmic/reality-stabilization.c",
        "/guilds/hand-of-bilanx/cosmic/cosmic-awareness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/cosmic/enforce-order.c");
    addChild("/guilds/hand-of-bilanx/cosmic/enforce-order.c",
        "/guilds/hand-of-bilanx/cosmic/reality-stabilization.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/cosmic/cosmic-balance.c");
    addChild("/guilds/hand-of-bilanx/cosmic/cosmic-balance.c",
        "/guilds/hand-of-bilanx/cosmic/enforce-order.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Cosmic Order");
    Description("This research tree taps into the fundamental order of the cosmos, "
        "granting powerful abilities that maintain universal balance.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/cosmic/root.c");
    TreeRoot("/guilds/hand-of-bilanx/cosmic/root.c");

    FirstLevel();
    EleventhLevel();
    TwentyFirstLevel();
    ThirtyThirdLevel();
    FortyNinthLevel();
}
