//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/keen-senses.c");
    addResearchElement(
        "/guilds/ranger/pathfinding/light-step.c");

    addChild("/guilds/ranger/pathfinding/keen-senses.c",
        "/guilds/ranger/pathfinding/root.c");
    addChild("/guilds/ranger/pathfinding/light-step.c",
        "/guilds/ranger/pathfinding/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/nimble-feet.c");
    addResearchElement(
        "/guilds/ranger/pathfinding/shadow-walk.c");

    addChild("/guilds/ranger/pathfinding/nimble-feet.c",
        "/guilds/ranger/pathfinding/keen-senses.c");
    addChild("/guilds/ranger/pathfinding/shadow-walk.c",
        "/guilds/ranger/pathfinding/light-step.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/trail-sense.c");
    addResearchElement(
        "/guilds/ranger/pathfinding/scouts-training.c");

    addChild("/guilds/ranger/pathfinding/trail-sense.c",
        "/guilds/ranger/pathfinding/keen-senses.c");
    addChild("/guilds/ranger/pathfinding/scouts-training.c",
        "/guilds/ranger/pathfinding/light-step.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/evasion.c");
    addResearchElement(
        "/guilds/ranger/pathfinding/silent-movement.c");

    addChild("/guilds/ranger/pathfinding/evasion.c",
        "/guilds/ranger/pathfinding/nimble-feet.c");
    addChild("/guilds/ranger/pathfinding/silent-movement.c",
        "/guilds/ranger/pathfinding/shadow-walk.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/forest-eyes.c");

    addChild("/guilds/ranger/pathfinding/forest-eyes.c",
        "/guilds/ranger/pathfinding/trail-sense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/cat-reflexes.c");
    addResearchElement(
        "/guilds/ranger/pathfinding/ghost-step.c");

    addChild("/guilds/ranger/pathfinding/cat-reflexes.c",
        "/guilds/ranger/pathfinding/evasion.c");
    addChild("/guilds/ranger/pathfinding/ghost-step.c",
        "/guilds/ranger/pathfinding/silent-movement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/danger-sense.c");

    addChild("/guilds/ranger/pathfinding/danger-sense.c",
        "/guilds/ranger/pathfinding/forest-eyes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/uncanny-dodge.c");
    addResearchElement(
        "/guilds/ranger/pathfinding/trackers-instinct.c");

    addChild("/guilds/ranger/pathfinding/uncanny-dodge.c",
        "/guilds/ranger/pathfinding/cat-reflexes.c");
    addChild("/guilds/ranger/pathfinding/trackers-instinct.c",
        "/guilds/ranger/pathfinding/danger-sense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/wraith-walk.c");

    addChild("/guilds/ranger/pathfinding/wraith-walk.c",
        "/guilds/ranger/pathfinding/ghost-step.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/predator-awareness.c");

    addChild("/guilds/ranger/pathfinding/predator-awareness.c",
        "/guilds/ranger/pathfinding/danger-sense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/preternatural-agility.c");

    addChild(
        "/guilds/ranger/pathfinding/preternatural-agility.c",
        "/guilds/ranger/pathfinding/uncanny-dodge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/phantom-stride.c");

    addChild("/guilds/ranger/pathfinding/phantom-stride.c",
        "/guilds/ranger/pathfinding/wraith-walk.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/sixth-sense.c");

    addChild("/guilds/ranger/pathfinding/sixth-sense.c",
        "/guilds/ranger/pathfinding/predator-awareness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/wind-dancer.c");

    addChild("/guilds/ranger/pathfinding/wind-dancer.c",
        "/guilds/ranger/pathfinding/preternatural-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/masters-grace.c");

    addChild("/guilds/ranger/pathfinding/masters-grace.c",
        "/guilds/ranger/pathfinding/phantom-stride.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/all-seeing-eye.c");

    addChild("/guilds/ranger/pathfinding/all-seeing-eye.c",
        "/guilds/ranger/pathfinding/sixth-sense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/blur-of-motion.c");

    addChild("/guilds/ranger/pathfinding/blur-of-motion.c",
        "/guilds/ranger/pathfinding/wind-dancer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/shadow-master.c");

    addChild("/guilds/ranger/pathfinding/shadow-master.c",
        "/guilds/ranger/pathfinding/masters-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/omniscient-awareness.c");

    addChild(
        "/guilds/ranger/pathfinding/omniscient-awareness.c",
        "/guilds/ranger/pathfinding/all-seeing-eye.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/impossible-evasion.c");

    addChild(
        "/guilds/ranger/pathfinding/impossible-evasion.c",
        "/guilds/ranger/pathfinding/blur-of-motion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/void-step.c");

    addChild("/guilds/ranger/pathfinding/void-step.c",
        "/guilds/ranger/pathfinding/shadow-master.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/true-sight.c");

    addChild("/guilds/ranger/pathfinding/true-sight.c",
        "/guilds/ranger/pathfinding/omniscient-awareness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/untouchable.c");

    addChild("/guilds/ranger/pathfinding/untouchable.c",
        "/guilds/ranger/pathfinding/impossible-evasion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/ethereal-presence.c");

    addChild("/guilds/ranger/pathfinding/ethereal-presence.c",
        "/guilds/ranger/pathfinding/void-step.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/supreme-awareness.c");

    addChild("/guilds/ranger/pathfinding/supreme-awareness.c",
        "/guilds/ranger/pathfinding/true-sight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/perfect-evasion.c");

    addChild("/guilds/ranger/pathfinding/perfect-evasion.c",
        "/guilds/ranger/pathfinding/untouchable.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/one-with-shadow.c");

    addChild("/guilds/ranger/pathfinding/one-with-shadow.c",
        "/guilds/ranger/pathfinding/ethereal-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/rangers-intuition.c");

    addChild("/guilds/ranger/pathfinding/rangers-intuition.c",
        "/guilds/ranger/pathfinding/supreme-awareness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/legendary-agility.c");

    addChild("/guilds/ranger/pathfinding/legendary-agility.c",
        "/guilds/ranger/pathfinding/perfect-evasion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/invisible-hunter.c");

    addChild("/guilds/ranger/pathfinding/invisible-hunter.c",
        "/guilds/ranger/pathfinding/one-with-shadow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/absolute-awareness.c");

    addChild("/guilds/ranger/pathfinding/absolute-awareness.c",
        "/guilds/ranger/pathfinding/rangers-intuition.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/transcendent-agility.c");

    addChild(
        "/guilds/ranger/pathfinding/transcendent-agility.c",
        "/guilds/ranger/pathfinding/legendary-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/phantom-hunter.c");

    addChild("/guilds/ranger/pathfinding/phantom-hunter.c",
        "/guilds/ranger/pathfinding/invisible-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/pathfinding/supreme-pathfinder.c");

    addChild("/guilds/ranger/pathfinding/supreme-pathfinder.c",
        "/guilds/ranger/pathfinding/transcendent-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Pathfinding");
    Description("This research tree provides the ranger with "
        "supernatural awareness, stealth, and agility - the "
        "ability to move unseen and strike without warning.");
    Source("ranger");
    addResearchElement("/guilds/ranger/pathfinding/root.c");
    TreeRoot("/guilds/ranger/pathfinding/root.c");

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
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
