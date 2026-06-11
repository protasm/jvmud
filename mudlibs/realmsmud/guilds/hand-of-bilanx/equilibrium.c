//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/equilibrium/balanced-stance.c");
    addChild("/guilds/hand-of-bilanx/equilibrium/balanced-stance.c",
        "/guilds/hand-of-bilanx/equilibrium/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/equilibrium/inner-balance.c");
    addChild("/guilds/hand-of-bilanx/equilibrium/inner-balance.c",
        "/guilds/hand-of-bilanx/equilibrium/balanced-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/equilibrium/perfect-balance.c");
    addChild("/guilds/hand-of-bilanx/equilibrium/perfect-balance.c",
        "/guilds/hand-of-bilanx/equilibrium/inner-balance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/equilibrium/unwavering-equilibrium.c");
    addChild("/guilds/hand-of-bilanx/equilibrium/unwavering-equilibrium.c",
        "/guilds/hand-of-bilanx/equilibrium/perfect-balance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/equilibrium/absolute-equilibrium.c");
    addChild("/guilds/hand-of-bilanx/equilibrium/absolute-equilibrium.c",
        "/guilds/hand-of-bilanx/equilibrium/unwavering-equilibrium.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Equilibrium");
    Description("This research tree teaches the fundamental principles of balance, "
        "granting abilities that maintain equilibrium in all things.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/equilibrium/root.c");
    TreeRoot("/guilds/hand-of-bilanx/equilibrium/root.c");

    FirstLevel();
    SeventhLevel();
    FifteenthLevel();
    TwentyFifthLevel();
    ThirtySeventhLevel();
}
