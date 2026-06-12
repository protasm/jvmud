//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/balance/balanced-bolt.c");
    addChild("/guilds/hand-of-bilanx/balance/balanced-bolt.c",
        "/guilds/hand-of-bilanx/balance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/balance/equilibrium-strike.c");
    addChild("/guilds/hand-of-bilanx/balance/equilibrium-strike.c",
        "/guilds/hand-of-bilanx/balance/balanced-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/balance/dual-nature.c");
    addChild("/guilds/hand-of-bilanx/balance/dual-nature.c",
        "/guilds/hand-of-bilanx/balance/equilibrium-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/balance/perfect-symmetry.c");
    addChild("/guilds/hand-of-bilanx/balance/perfect-symmetry.c",
        "/guilds/hand-of-bilanx/balance/dual-nature.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/balance/ultimate-balance.c");
    addChild("/guilds/hand-of-bilanx/balance/ultimate-balance.c",
        "/guilds/hand-of-bilanx/balance/perfect-symmetry.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Balance Magic");
    Description("This research tree provides spells that embody perfect balance, "
        "capable of both healing and harm, creation and destruction.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/balance/root.c");
    TreeRoot("/guilds/hand-of-bilanx/balance/root.c");

    FirstLevel();
    NinthLevel();
    SeventeenthLevel();
    TwentySeventhLevel();
    ThirtyNinthLevel();
}
