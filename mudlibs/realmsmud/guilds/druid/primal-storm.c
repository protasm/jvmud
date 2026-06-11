//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/druid/primal-storm/gust.c");
    addResearchElement("/guilds/druid/primal-storm/storm-focus.c");
    addResearchElement("/guilds/druid/primal-storm/storm-born.c");

    addChild("/guilds/druid/primal-storm/gust.c",
        "/guilds/druid/primal-storm/root.c");
    addChild("/guilds/druid/primal-storm/storm-focus.c",
        "/guilds/druid/primal-storm/root.c");
    addChild("/guilds/druid/primal-storm/storm-born.c",
        "/guilds/druid/primal-storm/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/druid/primal-storm/storm-affinity.c");
    addResearchElement("/guilds/druid/primal-storm/ice-shard.c");
    addResearchElement("/guilds/druid/primal-storm/cold-veins.c");

    addChild("/guilds/druid/primal-storm/storm-affinity.c",
        "/guilds/druid/primal-storm/root.c");
    addChild("/guilds/druid/primal-storm/ice-shard.c",
        "/guilds/druid/primal-storm/gust.c");
    addChild("/guilds/druid/primal-storm/cold-veins.c",
        "/guilds/druid/primal-storm/storm-born.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/primal-storm/sleet.c");
    addResearchElement("/guilds/druid/primal-storm/wind-attunement.c");
    addResearchElement("/guilds/druid/primal-storm/wind-touched.c");

    addChild("/guilds/druid/primal-storm/sleet.c",
        "/guilds/druid/primal-storm/ice-shard.c");
    addChild("/guilds/druid/primal-storm/wind-attunement.c",
        "/guilds/druid/primal-storm/storm-focus.c");
    addChild("/guilds/druid/primal-storm/wind-touched.c",
        "/guilds/druid/primal-storm/cold-veins.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/druid/primal-storm/frost-nova.c");
    addResearchElement("/guilds/druid/primal-storm/frozen-channel.c");
    addResearchElement("/guilds/druid/primal-storm/frost-knowledge.c");
    addResearchElement("/guilds/druid/primal-storm/frost-resilience.c");

    addChild("/guilds/druid/primal-storm/frost-nova.c",
        "/guilds/druid/primal-storm/sleet.c");
    addChild("/guilds/druid/primal-storm/frozen-channel.c",
        "/guilds/druid/primal-storm/storm-affinity.c");
    addChild("/guilds/druid/primal-storm/frost-knowledge.c",
        "/guilds/druid/primal-storm/wind-attunement.c");
    addChild("/guilds/druid/primal-storm/frost-resilience.c",
        "/guilds/druid/primal-storm/wind-touched.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/primal-storm/hailstorm.c");
    addResearchElement("/guilds/druid/primal-storm/arctic-power.c");
    addResearchElement("/guilds/druid/primal-storm/storm-body.c");

    addChild("/guilds/druid/primal-storm/hailstorm.c",
        "/guilds/druid/primal-storm/frost-nova.c");
    addChild("/guilds/druid/primal-storm/arctic-power.c",
        "/guilds/druid/primal-storm/frozen-channel.c");
    addChild("/guilds/druid/primal-storm/storm-body.c",
        "/guilds/druid/primal-storm/frost-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/druid/primal-storm/arctic-gale.c");
    addResearchElement("/guilds/druid/primal-storm/lightning-body.c");
    addResearchElement("/guilds/druid/primal-storm/tempest-lore.c");

    addChild("/guilds/druid/primal-storm/arctic-gale.c",
        "/guilds/druid/primal-storm/hailstorm.c");
    addChild("/guilds/druid/primal-storm/lightning-body.c",
        "/guilds/druid/primal-storm/storm-born.c");
    addChild("/guilds/druid/primal-storm/tempest-lore.c",
        "/guilds/druid/primal-storm/frost-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/primal-storm/glacial-focus.c");

    addChild("/guilds/druid/primal-storm/glacial-focus.c",
        "/guilds/druid/primal-storm/storm-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/druid/primal-storm/blizzard.c");
    addResearchElement(
        "/guilds/druid/primal-storm/blizzard-mastery.c");

    addChild("/guilds/druid/primal-storm/blizzard.c",
        "/guilds/druid/primal-storm/arctic-gale.c");
    addChild("/guilds/druid/primal-storm/blizzard-mastery.c",
        "/guilds/druid/primal-storm/tempest-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/druid/primal-storm/tempest-resilience.c");
    addResearchElement("/guilds/druid/primal-storm/arctic-bond.c");

    addChild("/guilds/druid/primal-storm/tempest-resilience.c",
        "/guilds/druid/primal-storm/glacial-focus.c");
    addChild("/guilds/druid/primal-storm/arctic-bond.c",
        "/guilds/druid/primal-storm/lightning-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/primal-storm/cyclone.c");
    addResearchElement("/guilds/druid/primal-storm/winter-mastery.c");

    addChild("/guilds/druid/primal-storm/cyclone.c",
        "/guilds/druid/primal-storm/blizzard.c");
    addChild("/guilds/druid/primal-storm/winter-mastery.c",
        "/guilds/druid/primal-storm/frozen-channel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/primal-storm/winter-heart.c");

    addChild("/guilds/druid/primal-storm/winter-heart.c",
        "/guilds/druid/primal-storm/tempest-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/druid/primal-storm/storms-heart.c");
    addResearchElement("/guilds/druid/primal-storm/wind-mastery.c");
    addResearchElement(
        "/guilds/druid/primal-storm/storm-core-knowledge.c");

    addChild("/guilds/druid/primal-storm/storms-heart.c",
        "/guilds/druid/primal-storm/cyclone.c");
    addChild("/guilds/druid/primal-storm/wind-mastery.c",
        "/guilds/druid/primal-storm/winter-mastery.c");
    addChild("/guilds/druid/primal-storm/storm-core-knowledge.c",
        "/guilds/druid/primal-storm/blizzard-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/druid/primal-storm/stormcaller.c");

    addChild("/guilds/druid/primal-storm/stormcaller.c",
        "/guilds/druid/primal-storm/winter-heart.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/druid/primal-storm/frozen-storm.c");

    addChild("/guilds/druid/primal-storm/frozen-storm.c",
        "/guilds/druid/primal-storm/storms-heart.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/druid/primal-storm/tempest.c");
    addResearchElement(
        "/guilds/druid/primal-storm/storm-mastery-passive.c");
    addResearchElement("/guilds/druid/primal-storm/storm-apex.c");

    addChild("/guilds/druid/primal-storm/tempest.c",
        "/guilds/druid/primal-storm/frozen-storm.c");
    addChild("/guilds/druid/primal-storm/storm-mastery-passive.c",
        "/guilds/druid/primal-storm/stormcaller.c");
    addChild("/guilds/druid/primal-storm/storm-apex.c",
        "/guilds/druid/primal-storm/storm-core-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/druid/primal-storm/tempest-fury.c");
    addResearchElement("/guilds/druid/primal-storm/eye-of-the-storm.c");
    addResearchElement(
        "/guilds/druid/primal-storm/eye-of-the-storm-passive.c");

    addChild("/guilds/druid/primal-storm/tempest-fury.c",
        "/guilds/druid/primal-storm/tempest.c");
    addChild("/guilds/druid/primal-storm/eye-of-the-storm.c",
        "/guilds/druid/primal-storm/tempest.c");
    addChild(
        "/guilds/druid/primal-storm/eye-of-the-storm-passive.c",
        "/guilds/druid/primal-storm/storm-mastery-passive.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/primal-storm/primal-stormcaller.c");
    addResearchElement(
        "/guilds/druid/primal-storm/blizzard-champion.c");

    addChild("/guilds/druid/primal-storm/primal-stormcaller.c",
        "/guilds/druid/primal-storm/storm-apex.c");
    addChild("/guilds/druid/primal-storm/blizzard-champion.c",
        "/guilds/druid/primal-storm/eye-of-the-storm-passive.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/primal-storm/storm-supremacy.c");
    addResearchElement(
        "/guilds/druid/primal-storm/storm-incarnate.c");

    addChild("/guilds/druid/primal-storm/storm-supremacy.c",
        "/guilds/druid/primal-storm/primal-stormcaller.c");
    addChild("/guilds/druid/primal-storm/storm-incarnate.c",
        "/guilds/druid/primal-storm/blizzard-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/druid/primal-storm/primal-storm-mastery.c");
    addResearchElement(
        "/guilds/druid/primal-storm/avatar-of-the-storm.c");

    addChild("/guilds/druid/primal-storm/primal-storm-mastery.c",
        "/guilds/druid/primal-storm/storm-supremacy.c");
    addChild("/guilds/druid/primal-storm/avatar-of-the-storm.c",
        "/guilds/druid/primal-storm/storm-incarnate.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/druid/primal-storm/eye-of-eternity.c");

    addChild("/guilds/druid/primal-storm/eye-of-eternity.c",
        "/guilds/druid/primal-storm/avatar-of-the-storm.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Primal Storm");
    Description("This research tree grants the druid mastery over "
        "the storm and the frozen north - spells that conjure "
        "biting winds, shards of ice, and the howling fury of the "
        "blizzard against the druid's foes.");
    Source("druid");
    addResearchElement("/guilds/druid/primal-storm/root.c");
    TreeRoot("/guilds/druid/primal-storm/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    NinthLevel();
    ThirteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtyNinthLevel();
}
