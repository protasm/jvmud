//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/perfect/symmetry.c");
    addChild("/guilds/hand-of-bilanx/perfect/symmetry.c",
        "/guilds/hand-of-bilanx/perfect/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/perfect/measured-response.c");
    addChild("/guilds/hand-of-bilanx/perfect/measured-response.c",
        "/guilds/hand-of-bilanx/perfect/symmetry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/perfect/equilibrium-aura.c");
    addChild("/guilds/hand-of-bilanx/perfect/equilibrium-aura.c",
        "/guilds/hand-of-bilanx/perfect/measured-response.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/perfect/bilanxs-blessing.c");
    addChild("/guilds/hand-of-bilanx/perfect/bilanxs-blessing.c",
        "/guilds/hand-of-bilanx/perfect/equilibrium-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/perfect/perfect-equilibrium.c");
    addChild("/guilds/hand-of-bilanx/perfect/perfect-equilibrium.c",
        "/guilds/hand-of-bilanx/perfect/bilanxs-blessing.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Perfect Balance");
    Description("This research tree represents the pinnacle of balance mastery, "
        "granting ultimate abilities that embody Bilanx's perfect equilibrium.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/perfect/root.c");
    TreeRoot("/guilds/hand-of-bilanx/perfect/root.c");

    FirstLevel();
    ThirteenthLevel();
    TwentyFifthLevel();
    ThirtySeventhLevel();
    FiftyFirstLevel();
}
