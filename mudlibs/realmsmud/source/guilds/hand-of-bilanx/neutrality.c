//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/neutrality/dispel-magic.c");
    addChild("/guilds/hand-of-bilanx/neutrality/dispel-magic.c",
        "/guilds/hand-of-bilanx/neutrality/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/neutrality/nullify.c");
    addChild("/guilds/hand-of-bilanx/neutrality/nullify.c",
        "/guilds/hand-of-bilanx/neutrality/dispel-magic.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/neutrality/calm-emotions.c");
    addChild("/guilds/hand-of-bilanx/neutrality/calm-emotions.c",
        "/guilds/hand-of-bilanx/neutrality/nullify.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/neutrality/moderate-extremes.c");
    addChild("/guilds/hand-of-bilanx/neutrality/moderate-extremes.c",
        "/guilds/hand-of-bilanx/neutrality/calm-emotions.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/hand-of-bilanx/neutrality/absolute-neutrality.c");
    addChild("/guilds/hand-of-bilanx/neutrality/absolute-neutrality.c",
        "/guilds/hand-of-bilanx/neutrality/moderate-extremes.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Neutrality");
    Description("This research tree embodies true neutrality, granting abilities "
        "that dispel extremes and restore moderation.");
    Source("Hand of Bilanx");
    addResearchElement("/guilds/hand-of-bilanx/neutrality/root.c");
    TreeRoot("/guilds/hand-of-bilanx/neutrality/root.c");

    ThirdLevel();
    EleventhLevel();
    NineteenthLevel();
    TwentyNinthLevel();
    FortyFirstLevel();
}
