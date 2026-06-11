//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/duality/twin-forces.c");
    addChild("/guilds/hand-of-bilanx/duality/twin-forces.c",
        "/guilds/hand-of-bilanx/duality/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/duality/opposing-currents.c");
    addChild("/guilds/hand-of-bilanx/duality/opposing-currents.c",
        "/guilds/hand-of-bilanx/duality/twin-forces.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/duality/complementary-opposites.c");
    addChild("/guilds/hand-of-bilanx/duality/complementary-opposites.c",
        "/guilds/hand-of-bilanx/duality/opposing-currents.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/duality/paradox.c");
    addChild("/guilds/hand-of-bilanx/duality/paradox.c",
        "/guilds/hand-of-bilanx/duality/complementary-opposites.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/duality/unity-of-opposites.c");
    addChild("/guilds/hand-of-bilanx/duality/unity-of-opposites.c",
        "/guilds/hand-of-bilanx/duality/paradox.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Duality");
    Description("This research tree embraces the dual nature of existence, "
        "granting abilities that embody opposing forces working in concert.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/duality/root.c");
    TreeRoot("/guilds/hand-of-bilanx/duality/root.c");

    FirstLevel();
    NinthLevel();
    SeventeenthLevel();
    TwentySeventhLevel();
    ThirtyNinthLevel();
}
