//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/cryomancer/winter/flurry.c");

    addChild("/guilds/cryomancer/winter/flurry.c",
        "/guilds/cryomancer/winter/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/cryomancer/winter/sleet.c");
    addResearchElement("/guilds/cryomancer/winter/chilling-mist.c");

    addChild("/guilds/cryomancer/winter/sleet.c",
        "/guilds/cryomancer/winter/flurry.c");
    addChild("/guilds/cryomancer/winter/chilling-mist.c",
        "/guilds/cryomancer/winter/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/snow-squall.c");
    addResearchElement("/guilds/cryomancer/winter/obscuring-snow.c");

    addChild("/guilds/cryomancer/winter/snow-squall.c",
        "/guilds/cryomancer/winter/root.c");
    addChild("/guilds/cryomancer/winter/obscuring-snow.c",
        "/guilds/cryomancer/winter/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/cryomancer/winter/arctic-focus.c");
    addResearchElement("/guilds/cryomancer/winter/numbing-grasp.c");
    addResearchElement("/guilds/cryomancer/winter/whiteout.c");

    addChild("/guilds/cryomancer/winter/arctic-focus.c",
        "/guilds/cryomancer/winter/snow-squall.c");
    addChild("/guilds/cryomancer/winter/numbing-grasp.c",
        "/guilds/cryomancer/winter/flurry.c");
    addChild("/guilds/cryomancer/winter/whiteout.c",
        "/guilds/cryomancer/winter/flurry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/winter-current.c");
    addResearchElement("/guilds/cryomancer/winter/winter-blades.c");
    addResearchElement("/guilds/cryomancer/winter/frostweaving.c");
    addResearchElement("/guilds/cryomancer/winter/talons-of-ice.c");

    addChild("/guilds/cryomancer/winter/winter-current.c",
        "/guilds/cryomancer/winter/flurry.c");
    addChild("/guilds/cryomancer/winter/winter-blades.c",
        "/guilds/cryomancer/winter/root.c");
    addChild("/guilds/cryomancer/winter/frostweaving.c",
        "/guilds/cryomancer/winter/snow-squall.c");
    addChild("/guilds/cryomancer/winter/talons-of-ice.c",
        "/guilds/cryomancer/winter/flurry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/cryomancer/winter/blizzard-force.c");
    addResearchElement("/guilds/cryomancer/winter/ice-fog.c");
    addResearchElement("/guilds/cryomancer/winter/battering-hail.c");

    addChild("/guilds/cryomancer/winter/blizzard-force.c",
        "/guilds/cryomancer/winter/whiteout.c");
    addChild("/guilds/cryomancer/winter/ice-fog.c",
        "/guilds/cryomancer/winter/obscuring-snow.c");
    addChild("/guilds/cryomancer/winter/battering-hail.c",
        "/guilds/cryomancer/winter/flurry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/ice-barrier.c");
    addResearchElement("/guilds/cryomancer/winter/frozen-gale.c");
    addResearchElement("/guilds/cryomancer/winter/ice-storm.c");

    addChild("/guilds/cryomancer/winter/ice-barrier.c",
        "/guilds/cryomancer/winter/whiteout.c");
    addChild("/guilds/cryomancer/winter/frozen-gale.c",
        "/guilds/cryomancer/winter/sleet.c");
    addChild("/guilds/cryomancer/winter/ice-storm.c",
        "/guilds/cryomancer/winter/whiteout.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/glacial-advance.c");
    addResearchElement("/guilds/cryomancer/winter/numbing-cold.c");

    addChild("/guilds/cryomancer/winter/glacial-advance.c",
        "/guilds/cryomancer/winter/arctic-focus.c");
    addChild("/guilds/cryomancer/winter/numbing-cold.c",
        "/guilds/cryomancer/winter/numbing-grasp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/glacial-convergence.c");
    addResearchElement("/guilds/cryomancer/winter/frozen-chains.c");

    addChild("/guilds/cryomancer/winter/glacial-convergence.c",
        "/guilds/cryomancer/winter/glacial-advance.c");
    addChild("/guilds/cryomancer/winter/frozen-chains.c",
        "/guilds/cryomancer/winter/numbing-cold.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/ice-channeling.c");
    addResearchElement("/guilds/cryomancer/winter/arctic-blast.c");

    addChild("/guilds/cryomancer/winter/ice-channeling.c",
        "/guilds/cryomancer/winter/frostweaving.c");
    addChild("/guilds/cryomancer/winter/arctic-blast.c",
        "/guilds/cryomancer/winter/winter-current.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/winter/storm-call.c");
    addResearchElement("/guilds/cryomancer/winter/encircling-ice.c");

    addChild("/guilds/cryomancer/winter/storm-call.c",
        "/guilds/cryomancer/winter/ice-storm.c");
    addChild("/guilds/cryomancer/winter/encircling-ice.c",
        "/guilds/cryomancer/winter/frozen-chains.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/winter/ice-shield.c");
    addResearchElement("/guilds/cryomancer/winter/suffocating-cold.c");

    addChild("/guilds/cryomancer/winter/ice-shield.c",
        "/guilds/cryomancer/winter/ice-barrier.c");
    addChild("/guilds/cryomancer/winter/suffocating-cold.c",
        "/guilds/cryomancer/winter/numbing-cold.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/winter-walk.c");

    addChild("/guilds/cryomancer/winter/winter-walk.c",
        "/guilds/cryomancer/winter/ice-channeling.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/winter/glacial-convergence-mastery.c");
    addResearchElement("/guilds/cryomancer/winter/disrupt-blizzard.c");

    addChild("/guilds/cryomancer/winter/glacial-convergence-mastery.c",
        "/guilds/cryomancer/winter/glacial-convergence.c");
    addChild("/guilds/cryomancer/winter/disrupt-blizzard.c",
        "/guilds/cryomancer/winter/storm-call.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/crushing-sleet.c");

    addChild("/guilds/cryomancer/winter/crushing-sleet.c",
        "/guilds/cryomancer/winter/battering-hail.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/winter/winter-mastery.c");
    addResearchElement("/guilds/cryomancer/winter/gather-the-storm.c");

    addChild("/guilds/cryomancer/winter/winter-mastery.c",
        "/guilds/cryomancer/winter/glacial-convergence-mastery.c");
    addChild("/guilds/cryomancer/winter/gather-the-storm.c",
        "/guilds/cryomancer/winter/encircling-ice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/winter/eye-of-the-blizzard.c");
    addResearchElement("/guilds/cryomancer/winter/frozen-embrace.c");

    addChild("/guilds/cryomancer/winter/eye-of-the-blizzard.c",
        "/guilds/cryomancer/winter/storm-call.c");
    addChild("/guilds/cryomancer/winter/frozen-embrace.c",
        "/guilds/cryomancer/winter/suffocating-cold.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/cryomantic-insight.c");
    addResearchElement("/guilds/cryomancer/winter/withering-cold.c");

    addChild("/guilds/cryomancer/winter/cryomantic-insight.c",
        "/guilds/cryomancer/winter/winter-mastery.c");
    addChild("/guilds/cryomancer/winter/withering-cold.c",
        "/guilds/cryomancer/winter/frozen-embrace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/cryomancer/winter/winter-dominion.c");
    addResearchElement("/guilds/cryomancer/winter/whirling-sleet.c");

    addChild("/guilds/cryomancer/winter/winter-dominion.c",
        "/guilds/cryomancer/winter/eye-of-the-blizzard.c");
    addChild("/guilds/cryomancer/winter/whirling-sleet.c",
        "/guilds/cryomancer/winter/disrupt-blizzard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/winter/frost-veil.c");

    addChild("/guilds/cryomancer/winter/frost-veil.c",
        "/guilds/cryomancer/winter/crushing-sleet.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/winter/arctic-mastery.c");
    addResearchElement("/guilds/cryomancer/winter/gather-the-storm-mastery.c");
    addResearchElement("/guilds/cryomancer/winter/void-ice.c");

    addChild("/guilds/cryomancer/winter/arctic-mastery.c",
        "/guilds/cryomancer/winter/winter-dominion.c");
    addChild("/guilds/cryomancer/winter/gather-the-storm-mastery.c",
        "/guilds/cryomancer/winter/gather-the-storm.c");
    addChild("/guilds/cryomancer/winter/void-ice.c",
        "/guilds/cryomancer/winter/frozen-embrace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/winter/cryomancer-insight.c");
    addResearchElement("/guilds/cryomancer/winter/withering-ice.c");

    addChild("/guilds/cryomancer/winter/cryomancer-insight.c",
        "/guilds/cryomancer/winter/arctic-mastery.c");
    addChild("/guilds/cryomancer/winter/withering-ice.c",
        "/guilds/cryomancer/winter/whirling-sleet.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/winter/ice-dominion.c");

    addChild("/guilds/cryomancer/winter/ice-dominion.c",
        "/guilds/cryomancer/winter/cryomancer-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/winter/winter-cataclysm.c");

    addChild("/guilds/cryomancer/winter/winter-cataclysm.c",
        "/guilds/cryomancer/winter/withering-ice.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Path of Winter");
    Description("This research tree provides knowledge of winter storm-based "
        "spells and abilities for the cryomancer.");
    Source("cryomancer");
    addResearchElement("/guilds/cryomancer/winter/root.c");
    TreeRoot("/guilds/cryomancer/winter/root.c");

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
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
}
