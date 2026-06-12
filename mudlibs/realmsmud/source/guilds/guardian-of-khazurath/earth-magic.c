//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-shard.c");
    addChild("/guilds/guardian-of-khazurath/earth/stone-shard.c",
        "/guilds/guardian-of-khazurath/earth/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-skin-passive.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-affinity.c");

    addChild("/guilds/guardian-of-khazurath/earth/stone-skin-passive.c",
        "/guilds/guardian-of-khazurath/earth/root.c");
    addChild("/guilds/guardian-of-khazurath/earth/earth-affinity.c",
        "/guilds/guardian-of-khazurath/earth/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/rock-burst.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-skin.c");

    addChild("/guilds/guardian-of-khazurath/earth/rock-burst.c",
        "/guilds/guardian-of-khazurath/earth/stone-shard.c");
    addChild("/guilds/guardian-of-khazurath/earth/stone-skin.c",
        "/guilds/guardian-of-khazurath/earth/stone-skin-passive.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/earthen-tremor.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/earthen-power.c");

    addChild("/guilds/guardian-of-khazurath/earth/earthen-tremor.c",
        "/guilds/guardian-of-khazurath/earth/stone-shard.c");
    addChild("/guilds/guardian-of-khazurath/earth/earthen-power.c",
        "/guilds/guardian-of-khazurath/earth/stone-shard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-flow.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-efficiency.c");

    addChild("/guilds/guardian-of-khazurath/earth/earth-flow.c",
        "/guilds/guardian-of-khazurath/earth/stone-shard.c");
    addChild("/guilds/guardian-of-khazurath/earth/stone-efficiency.c",
        "/guilds/guardian-of-khazurath/earth/stone-shard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-attunement.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-communion.c");

    addChild("/guilds/guardian-of-khazurath/earth/earth-attunement.c",
        "/guilds/guardian-of-khazurath/earth/earth-affinity.c");
    addChild("/guilds/guardian-of-khazurath/earth/stone-communion.c",
        "/guilds/guardian-of-khazurath/earth/earth-affinity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-fortitude.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-mastery.c");

    addChild("/guilds/guardian-of-khazurath/earth/earth-fortitude.c",
        "/guilds/guardian-of-khazurath/earth/stone-skin.c");
    addChild("/guilds/guardian-of-khazurath/earth/stone-mastery.c",
        "/guilds/guardian-of-khazurath/earth/earthen-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/crystal-lance.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-cadence.c");

    addChild("/guilds/guardian-of-khazurath/earth/crystal-lance.c",
        "/guilds/guardian-of-khazurath/earth/rock-burst.c");
    addChild("/guilds/guardian-of-khazurath/earth/stone-cadence.c",
        "/guilds/guardian-of-khazurath/earth/earth-flow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-resilience.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-reserve.c");

    addChild("/guilds/guardian-of-khazurath/earth/earth-resilience.c",
        "/guilds/guardian-of-khazurath/earth/stone-skin-passive.c");
    addChild("/guilds/guardian-of-khazurath/earth/earth-reserve.c",
        "/guilds/guardian-of-khazurath/earth/stone-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-storm.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/deep-earth.c");

    addChild("/guilds/guardian-of-khazurath/earth/stone-storm.c",
        "/guilds/guardian-of-khazurath/earth/earthen-tremor.c");
    addChild("/guilds/guardian-of-khazurath/earth/deep-earth.c",
        "/guilds/guardian-of-khazurath/earth/stone-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/seismic-tempo.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/deep-stone-lore.c");

    addChild("/guilds/guardian-of-khazurath/earth/seismic-tempo.c",
        "/guilds/guardian-of-khazurath/earth/stone-cadence.c");
    addChild("/guilds/guardian-of-khazurath/earth/deep-stone-lore.c",
        "/guilds/guardian-of-khazurath/earth/stone-communion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/granite-shell.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/dwarven-focus.c");

    addChild("/guilds/guardian-of-khazurath/earth/granite-shell.c",
        "/guilds/guardian-of-khazurath/earth/earth-fortitude.c");
    addChild("/guilds/guardian-of-khazurath/earth/dwarven-focus.c",
        "/guilds/guardian-of-khazurath/earth/earth-reserve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/seismic-smash.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/granite-hide.c");

    addChild("/guilds/guardian-of-khazurath/earth/seismic-smash.c",
        "/guilds/guardian-of-khazurath/earth/crystal-lance.c");
    addChild("/guilds/guardian-of-khazurath/earth/granite-hide.c",
        "/guilds/guardian-of-khazurath/earth/earth-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/mountain-force.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/mountain-tempo.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-recovery.c");

    addChild("/guilds/guardian-of-khazurath/earth/mountain-force.c",
        "/guilds/guardian-of-khazurath/earth/deep-earth.c");
    addChild("/guilds/guardian-of-khazurath/earth/mountain-tempo.c",
        "/guilds/guardian-of-khazurath/earth/seismic-tempo.c");
    addChild("/guilds/guardian-of-khazurath/earth/stone-recovery.c",
        "/guilds/guardian-of-khazurath/earth/granite-hide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/shattering-quake.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/mountain-conservation.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-mastery-passive.c");

    addChild("/guilds/guardian-of-khazurath/earth/shattering-quake.c",
        "/guilds/guardian-of-khazurath/earth/stone-storm.c");
    addChild("/guilds/guardian-of-khazurath/earth/mountain-conservation.c",
        "/guilds/guardian-of-khazurath/earth/dwarven-focus.c");
    addChild("/guilds/guardian-of-khazurath/earth/earth-mastery-passive.c",
        "/guilds/guardian-of-khazurath/earth/earth-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/mountain-heart.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/mountain-endurance.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/khazurath-dominion.c");

    addChild("/guilds/guardian-of-khazurath/earth/mountain-heart.c",
        "/guilds/guardian-of-khazurath/earth/granite-shell.c");
    addChild("/guilds/guardian-of-khazurath/earth/mountain-endurance.c",
        "/guilds/guardian-of-khazurath/earth/stone-recovery.c");
    addChild("/guilds/guardian-of-khazurath/earth/khazurath-dominion.c",
        "/guilds/guardian-of-khazurath/earth/mountain-force.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/mountain-wrath.c");
    addResearchElement("/guilds/guardian-of-khazurath/earth/earth-renewal.c");

    addChild("/guilds/guardian-of-khazurath/earth/mountain-wrath.c",
        "/guilds/guardian-of-khazurath/earth/seismic-smash.c");
    addChild("/guilds/guardian-of-khazurath/earth/earth-renewal.c",
        "/guilds/guardian-of-khazurath/earth/mountain-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/khazurath-ward.c");

    addChild("/guilds/guardian-of-khazurath/earth/khazurath-ward.c",
        "/guilds/guardian-of-khazurath/earth/mountain-heart.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/stone-cataclysm.c");

    addChild("/guilds/guardian-of-khazurath/earth/stone-cataclysm.c",
        "/guilds/guardian-of-khazurath/earth/shattering-quake.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/earth/heart-of-khazurath.c");

    addChild("/guilds/guardian-of-khazurath/earth/heart-of-khazurath.c",
        "/guilds/guardian-of-khazurath/earth/stone-cataclysm.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Earth Magic");
    Description("The earth magic of Khazurath, drawn from the bones of the "
        "mountain itself. Guardians who walk this path learn to hurl stone, "
        "raise barriers of living rock, and fortify themselves with the "
        "unyielding endurance of the deep earth.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/earth/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/earth/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    TwentiethLevel();
    TwentyThirdLevel();
    TwentySixthLevel();
    ThirtiethLevel();
    ThirtyFifthLevel();
    FortiethLevel();
    FortyFifthLevel();
    FiftyFifthLevel();
    SixtiethLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
