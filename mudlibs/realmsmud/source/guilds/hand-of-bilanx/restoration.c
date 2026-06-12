//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/restoration/heal-wounds.c");
    addChild("/guilds/hand-of-bilanx/restoration/heal-wounds.c",
        "/guilds/hand-of-bilanx/restoration/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/restoration/restore-balance.c");
    addChild("/guilds/hand-of-bilanx/restoration/restore-balance.c",
        "/guilds/hand-of-bilanx/restoration/heal-wounds.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/restoration/mend-body.c");
    addChild("/guilds/hand-of-bilanx/restoration/mend-body.c",
        "/guilds/hand-of-bilanx/restoration/restore-balance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/restoration/renewal.c");
    addChild("/guilds/hand-of-bilanx/restoration/renewal.c",
        "/guilds/hand-of-bilanx/restoration/mend-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/restoration/complete-restoration.c");
    addChild("/guilds/hand-of-bilanx/restoration/complete-restoration.c",
        "/guilds/hand-of-bilanx/restoration/renewal.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Restoration");
    Description("This research tree focuses on healing and restoration, "
        "restoring balance by mending what has been broken.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/restoration/root.c");
    TreeRoot("/guilds/hand-of-bilanx/restoration/root.c");

    FirstLevel();
    SeventhLevel();
    FifteenthLevel();
    TwentyFifthLevel();
    ThirtySeventhLevel();
}
