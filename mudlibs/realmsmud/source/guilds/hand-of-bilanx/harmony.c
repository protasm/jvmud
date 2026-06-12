//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/harmony/peaceful-aura.c");
    addChild("/guilds/hand-of-bilanx/harmony/peaceful-aura.c",
        "/guilds/hand-of-bilanx/harmony/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/harmony/soothing-presence.c");
    addChild("/guilds/hand-of-bilanx/harmony/soothing-presence.c",
        "/guilds/hand-of-bilanx/harmony/peaceful-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/harmony/harmonious-shield.c");
    addChild("/guilds/hand-of-bilanx/harmony/harmonious-shield.c",
        "/guilds/hand-of-bilanx/harmony/soothing-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/harmony/tranquility.c");
    addChild("/guilds/hand-of-bilanx/harmony/tranquility.c",
        "/guilds/hand-of-bilanx/harmony/harmonious-shield.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/harmony/perfect-harmony.c");
    addChild("/guilds/hand-of-bilanx/harmony/perfect-harmony.c",
        "/guilds/hand-of-bilanx/harmony/tranquility.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Harmony");
    Description("This research tree focuses on creating harmony and peace, "
        "with supportive and protective abilities.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/harmony/root.c");
    TreeRoot("/guilds/hand-of-bilanx/harmony/root.c");

    ThirdLevel();
    EleventhLevel();
    NineteenthLevel();
    TwentyNinthLevel();
    FortyFirstLevel();
}
