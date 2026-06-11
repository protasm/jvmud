//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/cryomancer/functions/cold.c");
    addResearchElement("/guilds/cryomancer/functions/frost.c");

    addChild("/guilds/cryomancer/functions/cold.c",
        "/guilds/cryomancer/functions/root.c");
    addChild("/guilds/cryomancer/functions/frost.c",
        "/guilds/cryomancer/functions/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/cryomancer/functions/ice.c");
    addResearchElement("/guilds/cryomancer/functions/chill.c");

    addChild("/guilds/cryomancer/functions/ice.c",
        "/guilds/cryomancer/functions/frost.c");
    addChild("/guilds/cryomancer/functions/chill.c",
        "/guilds/cryomancer/functions/cold.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/cryomancer/functions/sleet.c");
    addResearchElement("/guilds/cryomancer/functions/freeze.c");

    addChild("/guilds/cryomancer/functions/sleet.c",
        "/guilds/cryomancer/functions/chill.c");
    addChild("/guilds/cryomancer/functions/freeze.c",
        "/guilds/cryomancer/functions/ice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/cryomancer/functions/hail.c");
    addResearchElement("/guilds/cryomancer/functions/glacial.c");

    addChild("/guilds/cryomancer/functions/hail.c",
        "/guilds/cryomancer/functions/sleet.c");
    addChild("/guilds/cryomancer/functions/glacial.c",
        "/guilds/cryomancer/functions/freeze.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/cryomancer/functions/blizzard.c");
    addResearchElement("/guilds/cryomancer/functions/permafrost.c");

    addChild("/guilds/cryomancer/functions/blizzard.c",
        "/guilds/cryomancer/functions/hail.c");
    addChild("/guilds/cryomancer/functions/permafrost.c",
        "/guilds/cryomancer/functions/glacial.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/cryomancer/functions/void-cold.c");

    addChild("/guilds/cryomancer/functions/void-cold.c",
        "/guilds/cryomancer/functions/permafrost.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Spell Functions");
    Description("This research tree provides knowledge of the various "
        "elemental functions that constructed cryomancer spells can employ.");
    Source("cryomancer");
    addResearchElement("/guilds/cryomancer/functions/root.c");
    TreeRoot("/guilds/cryomancer/functions/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
}
