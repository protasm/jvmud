//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/battlefield-awareness.c");
    addChild("/guilds/werric-knight/tactics/battlefield-awareness.c",
        "/guilds/werric-knight/tactics/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/coordinated-strike.c");
    addChild("/guilds/werric-knight/tactics/coordinated-strike.c",
        "/guilds/werric-knight/tactics/battlefield-awareness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/tactical-mind.c");
    addChild("/guilds/werric-knight/tactics/tactical-mind.c",
        "/guilds/werric-knight/tactics/coordinated-strike.c");

    addResearchElement("/guilds/werric-knight/tactics/flanking-maneuver.c");
    addChild("/guilds/werric-knight/tactics/flanking-maneuver.c",
        "/guilds/werric-knight/tactics/tactical-mind.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/battle-command.c");
    addChild("/guilds/werric-knight/tactics/battle-command.c",
        "/guilds/werric-knight/tactics/flanking-maneuver.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/strategic-positioning.c");
    addChild("/guilds/werric-knight/tactics/strategic-positioning.c",
        "/guilds/werric-knight/tactics/battle-command.c");

    addResearchElement("/guilds/werric-knight/tactics/exploit-weakness.c");
    addChild("/guilds/werric-knight/tactics/exploit-weakness.c",
        "/guilds/werric-knight/tactics/strategic-positioning.c");

    addResearchElement("/guilds/werric-knight/tactics/tactical-precision.c");
    addChild("/guilds/werric-knight/tactics/tactical-precision.c",
        "/guilds/werric-knight/tactics/coordinated-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/defensive-formation.c");
    addChild("/guilds/werric-knight/tactics/defensive-formation.c",
        "/guilds/werric-knight/tactics/exploit-weakness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/master-tactician.c");
    addChild("/guilds/werric-knight/tactics/master-tactician.c",
        "/guilds/werric-knight/tactics/defensive-formation.c");

    addResearchElement("/guilds/werric-knight/tactics/press-the-advantage.c");
    addChild("/guilds/werric-knight/tactics/press-the-advantage.c",
        "/guilds/werric-knight/tactics/master-tactician.c");

    addResearchElement("/guilds/werric-knight/tactics/art-of-war.c");
    addChild("/guilds/werric-knight/tactics/art-of-war.c",
        "/guilds/werric-knight/tactics/exploit-weakness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/offensive-formation.c");
    addChild("/guilds/werric-knight/tactics/offensive-formation.c",
        "/guilds/werric-knight/tactics/press-the-advantage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/tactical-strike.c");
    addChild("/guilds/werric-knight/tactics/tactical-strike.c",
        "/guilds/werric-knight/tactics/offensive-formation.c");

    addResearchElement("/guilds/werric-knight/tactics/exploitation-technique.c");
    addChild("/guilds/werric-knight/tactics/exploitation-technique.c",
        "/guilds/werric-knight/tactics/exploit-weakness.c");

    addResearchElement("/guilds/werric-knight/tactics/supreme-tactics.c");
    addChild("/guilds/werric-knight/tactics/supreme-tactics.c",
        "/guilds/werric-knight/tactics/tactical-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/strategic-genius.c");
    addChild("/guilds/werric-knight/tactics/strategic-genius.c",
        "/guilds/werric-knight/tactics/tactical-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/devastating-combination.c");
    addChild("/guilds/werric-knight/tactics/devastating-combination.c",
        "/guilds/werric-knight/tactics/strategic-genius.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/supreme-command.c");
    addChild("/guilds/werric-knight/tactics/supreme-command.c",
        "/guilds/werric-knight/tactics/devastating-combination.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/perfect-coordination.c");
    addChild("/guilds/werric-knight/tactics/perfect-coordination.c",
        "/guilds/werric-knight/tactics/supreme-command.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/werras-gambit.c");
    addChild("/guilds/werric-knight/tactics/werras-gambit.c",
        "/guilds/werric-knight/tactics/perfect-coordination.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/tactics/legendary-commander.c");
    addChild("/guilds/werric-knight/tactics/legendary-commander.c",
        "/guilds/werric-knight/tactics/werras-gambit.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Tactical Warfare");
    Description("This research tree represents the tactical and strategic mastery "
        "that made Hillgarath's armies legendary. Formation combat, coordinated "
        "strikes, and battlefield command transform individual warriors into an "
        "unstoppable tactical instrument.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/tactics/root.c");
    TreeRoot("/guilds/werric-knight/tactics/root.c");

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
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
}
