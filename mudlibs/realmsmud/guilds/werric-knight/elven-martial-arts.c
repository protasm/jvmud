//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/elven/graceful-movement.c");
    addChild("/guilds/werric-knight/elven/graceful-movement.c",
        "/guilds/werric-knight/elven/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/elven/swift-strike.c");
    addChild("/guilds/werric-knight/elven/swift-strike.c",
        "/guilds/werric-knight/elven/graceful-movement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/elven/elven-precision.c");
    addChild("/guilds/werric-knight/elven/elven-precision.c",
        "/guilds/werric-knight/elven/swift-strike.c");

    addResearchElement("/guilds/werric-knight/elven/dancing-blade.c");
    addChild("/guilds/werric-knight/elven/dancing-blade.c",
        "/guilds/werric-knight/elven/elven-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/elven/flowing-water-stance.c");
    addChild("/guilds/werric-knight/elven/flowing-water-stance.c",
        "/guilds/werric-knight/elven/dancing-blade.c");

    addResearchElement("/guilds/werric-knight/elven/swift-blade-technique.c");
    addChild("/guilds/werric-knight/elven/swift-blade-technique.c",
        "/guilds/werric-knight/elven/swift-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/elven/hybrid-mastery.c");
    addChild("/guilds/werric-knight/elven/hybrid-mastery.c",
        "/guilds/werric-knight/elven/flowing-water-stance.c");

    addResearchElement("/guilds/werric-knight/elven/whirlwind-assault.c");
    addChild("/guilds/werric-knight/elven/whirlwind-assault.c",
        "/guilds/werric-knight/elven/hybrid-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/elven/eneldhor-technique.c");
    addChild("/guilds/werric-knight/elven/eneldhor-technique.c",
        "/guilds/werric-knight/elven/whirlwind-assault.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/elven/phantom-strike.c");
    addChild("/guilds/werric-knight/elven/phantom-strike.c",
        "/guilds/werric-knight/elven/eneldhor-technique.c");

    addResearchElement("/guilds/werric-knight/elven/phantom-technique.c");
    addChild("/guilds/werric-knight/elven/phantom-technique.c",
        "/guilds/werric-knight/elven/phantom-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/elven/moonlight-steps.c");
    addChild("/guilds/werric-knight/elven/moonlight-steps.c",
        "/guilds/werric-knight/elven/phantom-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/elven/perfect-balance.c");
    addChild("/guilds/werric-knight/elven/perfect-balance.c",
        "/guilds/werric-knight/elven/moonlight-steps.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/elven/sevenfold-strike.c");
    addChild("/guilds/werric-knight/elven/sevenfold-strike.c",
        "/guilds/werric-knight/elven/perfect-balance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/elven/elven-grace.c");
    addChild("/guilds/werric-knight/elven/elven-grace.c",
        "/guilds/werric-knight/elven/sevenfold-strike.c");

    addResearchElement("/guilds/werric-knight/elven/ancient-bladework.c");
    addChild("/guilds/werric-knight/elven/ancient-bladework.c",
        "/guilds/werric-knight/elven/sevenfold-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/elven/blade-of-wind.c");
    addChild("/guilds/werric-knight/elven/blade-of-wind.c",
        "/guilds/werric-knight/elven/elven-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/elven/dance-of-blades.c");
    addChild("/guilds/werric-knight/elven/dance-of-blades.c",
        "/guilds/werric-knight/elven/blade-of-wind.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/elven/daedruns-heir.c");
    addChild("/guilds/werric-knight/elven/daedruns-heir.c",
        "/guilds/werric-knight/elven/dance-of-blades.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Elven Martial Arts");
    Description("This research tree represents the elven combat techniques brought "
        "to Hillgarth by Daedrun, the elven master who trained King Werra. These "
        "graceful, precise methods blend elven artistry with human discipline.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/elven/root.c");
    TreeRoot("/guilds/werric-knight/elven/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
}
