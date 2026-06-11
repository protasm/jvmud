//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/world-rune-lore.c");

    addChild("/guilds/runeskald/world-rune/world-rune-lore.c",
        "/guilds/runeskald/world-rune/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/rune-of-unmaking.c");
    addResearchElement("/guilds/runeskald/world-rune/rune-of-forging.c");
    addResearchElement("/guilds/runeskald/world-rune/world-rune-attunement.c");

    addChild("/guilds/runeskald/world-rune/rune-of-unmaking.c",
        "/guilds/runeskald/world-rune/world-rune-lore.c");
    addChild("/guilds/runeskald/world-rune/rune-of-forging.c",
        "/guilds/runeskald/world-rune/world-rune-lore.c");
    addChild("/guilds/runeskald/world-rune/world-rune-attunement.c",
        "/guilds/runeskald/world-rune/world-rune-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/rune-of-sundering.c");
    addResearchElement("/guilds/runeskald/world-rune/rune-of-binding-flesh.c");
    addResearchElement("/guilds/runeskald/world-rune/rune-of-the-world-flame.c");
    addResearchElement("/guilds/runeskald/world-rune/world-rune-conduit.c");

    addChild("/guilds/runeskald/world-rune/rune-of-sundering.c",
        "/guilds/runeskald/world-rune/rune-of-unmaking.c");
    addChild("/guilds/runeskald/world-rune/rune-of-binding-flesh.c",
        "/guilds/runeskald/world-rune/rune-of-forging.c");
    addChild("/guilds/runeskald/world-rune/rune-of-the-world-flame.c",
        "/guilds/runeskald/world-rune/world-rune-attunement.c");
    addChild("/guilds/runeskald/world-rune/world-rune-conduit.c",
        "/guilds/runeskald/world-rune/world-rune-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/world-rune-aegis.c");
    addResearchElement("/guilds/runeskald/world-rune/world-rune-permanence.c");
    addResearchElement("/guilds/runeskald/world-rune/rune-of-the-world-frost.c");

    addChild("/guilds/runeskald/world-rune/world-rune-aegis.c",
        "/guilds/runeskald/world-rune/rune-of-sundering.c");
    addChild("/guilds/runeskald/world-rune/world-rune-permanence.c",
        "/guilds/runeskald/world-rune/rune-of-binding-flesh.c");
    addChild("/guilds/runeskald/world-rune/rune-of-the-world-frost.c",
        "/guilds/runeskald/world-rune/world-rune-conduit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/world-rune-devastation.c");
    addResearchElement("/guilds/runeskald/world-rune/world-rune-resilience.c");

    addChild("/guilds/runeskald/world-rune/world-rune-devastation.c",
        "/guilds/runeskald/world-rune/world-rune-aegis.c");
    addChild("/guilds/runeskald/world-rune/world-rune-resilience.c",
        "/guilds/runeskald/world-rune/world-rune-aegis.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/world-rune-torrent.c");

    addChild("/guilds/runeskald/world-rune/world-rune-torrent.c",
        "/guilds/runeskald/world-rune/world-rune-devastation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/primal-rune-mastery.c");

    addChild("/guilds/runeskald/world-rune/primal-rune-mastery.c",
        "/guilds/runeskald/world-rune/world-rune-torrent.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/runeskald/world-rune/world-singers-transcendence.c");

    addChild("/guilds/runeskald/world-rune/world-singers-transcendence.c",
        "/guilds/runeskald/world-rune/primal-rune-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("World Rune");
    Source("runeskald");
    Description("This tree covers the most powerful runic inscriptions, "
        "tapping into the primal forces of the world itself.");

    addResearchElement("/guilds/runeskald/world-rune/root.c");
    TreeRoot("/guilds/runeskald/world-rune/root.c");

    FirstLevel();
    SecondLevel();
    ThirdLevel();
    FourthLevel();
    FifthLevel();
    SixthLevel();
    SeventhLevel();
    EighthLevel();
}
