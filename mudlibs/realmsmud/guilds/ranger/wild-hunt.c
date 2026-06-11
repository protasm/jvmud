//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunters-mark.c");

    addChild("/guilds/ranger/wild-hunt/hunters-mark.c",
        "/guilds/ranger/wild-hunt/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/relentless-pursuit.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunt-endurance.c");

    addChild("/guilds/ranger/wild-hunt/relentless-pursuit.c",
        "/guilds/ranger/wild-hunt/root.c");
    addChild("/guilds/ranger/wild-hunt/hunt-endurance.c",
        "/guilds/ranger/wild-hunt/relentless-pursuit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/pack-hunter.c");

    addChild("/guilds/ranger/wild-hunt/pack-hunter.c",
        "/guilds/ranger/wild-hunt/hunters-mark.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/blood-scent.c");

    addChild("/guilds/ranger/wild-hunt/blood-scent.c",
        "/guilds/ranger/wild-hunt/relentless-pursuit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunters-volley.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/prey-weakness.c");

    addChild("/guilds/ranger/wild-hunt/hunters-volley.c",
        "/guilds/ranger/wild-hunt/pack-hunter.c");
    addChild("/guilds/ranger/wild-hunt/prey-weakness.c",
        "/guilds/ranger/wild-hunt/blood-scent.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/predators-edge.c");

    addChild("/guilds/ranger/wild-hunt/predators-edge.c",
        "/guilds/ranger/wild-hunt/pack-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/swift-hunter.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunt-focus.c");

    addChild("/guilds/ranger/wild-hunt/swift-hunter.c",
        "/guilds/ranger/wild-hunt/blood-scent.c");
    addChild("/guilds/ranger/wild-hunt/hunt-focus.c",
        "/guilds/ranger/wild-hunt/predators-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/pursuit-shot.c");

    addChild("/guilds/ranger/wild-hunt/pursuit-shot.c",
        "/guilds/ranger/wild-hunt/predators-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-fury.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-speed.c");

    addChild("/guilds/ranger/wild-hunt/wild-fury.c",
        "/guilds/ranger/wild-hunt/predators-edge.c");
    addChild("/guilds/ranger/wild-hunt/wild-speed.c",
        "/guilds/ranger/wild-hunt/swift-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunters-instinct.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-technique.c");

    addChild("/guilds/ranger/wild-hunt/hunters-instinct.c",
        "/guilds/ranger/wild-hunt/swift-hunter.c");
    addChild("/guilds/ranger/wild-hunt/wild-technique.c",
        "/guilds/ranger/wild-hunt/wild-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/feral-barrage.c");

    addChild("/guilds/ranger/wild-hunt/feral-barrage.c",
        "/guilds/ranger/wild-hunt/wild-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunt-mastery.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/savage-precision.c");

    addChild("/guilds/ranger/wild-hunt/hunt-mastery.c",
        "/guilds/ranger/wild-hunt/wild-fury.c");
    addChild("/guilds/ranger/wild-hunt/savage-precision.c",
        "/guilds/ranger/wild-hunt/hunters-instinct.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/primal-hunter.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/predator-art.c");

    addChild("/guilds/ranger/wild-hunt/primal-hunter.c",
        "/guilds/ranger/wild-hunt/hunters-instinct.c");
    addChild("/guilds/ranger/wild-hunt/predator-art.c",
        "/guilds/ranger/wild-hunt/primal-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/alpha-strike.c");

    addChild("/guilds/ranger/wild-hunt/alpha-strike.c",
        "/guilds/ranger/wild-hunt/hunt-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunters-fury.c");

    addChild("/guilds/ranger/wild-hunt/hunters-fury.c",
        "/guilds/ranger/wild-hunt/hunt-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-hunt-mastery.c");

    addChild("/guilds/ranger/wild-hunt/wild-hunt-mastery.c",
        "/guilds/ranger/wild-hunt/hunt-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/alpha-predator.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunt-lord-technique.c");

    addChild("/guilds/ranger/wild-hunt/alpha-predator.c",
        "/guilds/ranger/wild-hunt/primal-hunter.c");
    addChild(
        "/guilds/ranger/wild-hunt/hunt-lord-technique.c",
        "/guilds/ranger/wild-hunt/wild-hunt-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-hunt-arrow.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-dominion.c");

    addChild("/guilds/ranger/wild-hunt/wild-hunt-arrow.c",
        "/guilds/ranger/wild-hunt/wild-hunt-mastery.c");
    addChild("/guilds/ranger/wild-hunt/wild-dominion.c",
        "/guilds/ranger/wild-hunt/alpha-predator.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunt-lord.c");

    addChild("/guilds/ranger/wild-hunt/hunt-lord.c",
        "/guilds/ranger/wild-hunt/wild-hunt-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/hunt-perfection.c");

    addChild("/guilds/ranger/wild-hunt/hunt-perfection.c",
        "/guilds/ranger/wild-hunt/hunt-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/eternal-hunt-lore.c");

    addChild("/guilds/ranger/wild-hunt/eternal-hunt-lore.c",
        "/guilds/ranger/wild-hunt/hunt-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/eternal-hunter.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/annihilation-volley.c");

    addChild("/guilds/ranger/wild-hunt/eternal-hunter.c",
        "/guilds/ranger/wild-hunt/alpha-predator.c");
    addChild("/guilds/ranger/wild-hunt/annihilation-volley.c",
        "/guilds/ranger/wild-hunt/hunt-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/eternal-pursuit.c");

    addChild("/guilds/ranger/wild-hunt/eternal-pursuit.c",
        "/guilds/ranger/wild-hunt/eternal-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/supreme-hunter.c");

    addChild("/guilds/ranger/wild-hunt/supreme-hunter.c",
        "/guilds/ranger/wild-hunt/hunt-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-transcendence.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/transcendent-hunt-lore.c");

    addChild("/guilds/ranger/wild-hunt/wild-transcendence.c",
        "/guilds/ranger/wild-hunt/supreme-hunter.c");
    addChild(
        "/guilds/ranger/wild-hunt/transcendent-hunt-lore.c",
        "/guilds/ranger/wild-hunt/supreme-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-hunt-lord.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/wild-hunt-cataclysm.c");

    addChild("/guilds/ranger/wild-hunt/wild-hunt-lord.c",
        "/guilds/ranger/wild-hunt/eternal-hunter.c");
    addChild("/guilds/ranger/wild-hunt/wild-hunt-cataclysm.c",
        "/guilds/ranger/wild-hunt/wild-hunt-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wild-hunt/transcendent-hunter.c");
    addResearchElement(
        "/guilds/ranger/wild-hunt/ultimate-hunter.c");

    addChild("/guilds/ranger/wild-hunt/transcendent-hunter.c",
        "/guilds/ranger/wild-hunt/supreme-hunter.c");
    addChild("/guilds/ranger/wild-hunt/ultimate-hunter.c",
        "/guilds/ranger/wild-hunt/transcendent-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Wild Hunt");
    Description("This research tree provides the ranger with "
        "the legendary techniques of the Wild Hunt - "
        "devastating combined attacks and relentless pursuit "
        "that mark the warden rank.");
    Source("ranger");
    addResearchElement("/guilds/ranger/wild-hunt/root.c");
    TreeRoot("/guilds/ranger/wild-hunt/root.c");

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
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
