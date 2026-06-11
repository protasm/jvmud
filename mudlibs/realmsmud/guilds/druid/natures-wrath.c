//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/thorn-whip.c");
    addResearchElement("/guilds/druid/natures-wrath/wrath-focus.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/primal-wrath-passive.c");

    addChild("/guilds/druid/natures-wrath/thorn-whip.c",
        "/guilds/druid/natures-wrath/root.c");
    addChild("/guilds/druid/natures-wrath/wrath-focus.c",
        "/guilds/druid/natures-wrath/root.c");
    addChild("/guilds/druid/natures-wrath/primal-wrath-passive.c",
        "/guilds/druid/natures-wrath/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/wild-affinity.c");
    addResearchElement("/guilds/druid/natures-wrath/stone-fist.c");
    addResearchElement("/guilds/druid/natures-wrath/earthen-body.c");

    addChild("/guilds/druid/natures-wrath/wild-affinity.c",
        "/guilds/druid/natures-wrath/root.c");
    addChild("/guilds/druid/natures-wrath/stone-fist.c",
        "/guilds/druid/natures-wrath/thorn-whip.c");
    addChild("/guilds/druid/natures-wrath/earthen-body.c",
        "/guilds/druid/natures-wrath/primal-wrath-passive.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/earthen-power.c");
    addResearchElement("/guilds/druid/natures-wrath/burning-anger.c");
    addResearchElement("/guilds/druid/natures-wrath/wildfire.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/earthen-attunement.c");
    addResearchElement("/guilds/druid/natures-wrath/volcanic-veins.c");

    addChild("/guilds/druid/natures-wrath/earthen-power.c",
        "/guilds/druid/natures-wrath/wild-affinity.c");
    addChild("/guilds/druid/natures-wrath/burning-anger.c",
        "/guilds/druid/natures-wrath/wild-affinity.c");
    addChild("/guilds/druid/natures-wrath/wildfire.c",
        "/guilds/druid/natures-wrath/thorn-whip.c");
    addChild("/guilds/druid/natures-wrath/earthen-attunement.c",
        "/guilds/druid/natures-wrath/wrath-focus.c");
    addChild("/guilds/druid/natures-wrath/volcanic-veins.c",
        "/guilds/druid/natures-wrath/earthen-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/natures-conduit.c");
    addResearchElement("/guilds/druid/natures-wrath/eruption.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/volcanic-knowledge.c");
    addResearchElement("/guilds/druid/natures-wrath/stone-bones.c");

    addChild("/guilds/druid/natures-wrath/natures-conduit.c",
        "/guilds/druid/natures-wrath/earthen-power.c");
    addChild("/guilds/druid/natures-wrath/eruption.c",
        "/guilds/druid/natures-wrath/stone-fist.c");
    addChild("/guilds/druid/natures-wrath/volcanic-knowledge.c",
        "/guilds/druid/natures-wrath/earthen-attunement.c");
    addChild("/guilds/druid/natures-wrath/stone-bones.c",
        "/guilds/druid/natures-wrath/volcanic-veins.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/molten-core.c");
    addResearchElement("/guilds/druid/natures-wrath/magma-surge.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/earth-attunement.c");

    addChild("/guilds/druid/natures-wrath/molten-core.c",
        "/guilds/druid/natures-wrath/natures-conduit.c");
    addChild("/guilds/druid/natures-wrath/magma-surge.c",
        "/guilds/druid/natures-wrath/eruption.c");
    addChild("/guilds/druid/natures-wrath/earth-attunement.c",
        "/guilds/druid/natures-wrath/stone-bones.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/primal-fury.c");
    addResearchElement("/guilds/druid/natures-wrath/scorched-earth.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/primal-destructivity.c");
    addResearchElement("/guilds/druid/natures-wrath/geomancer-body.c");

    addChild("/guilds/druid/natures-wrath/primal-fury.c",
        "/guilds/druid/natures-wrath/molten-core.c");
    addChild("/guilds/druid/natures-wrath/scorched-earth.c",
        "/guilds/druid/natures-wrath/wildfire.c");
    addChild("/guilds/druid/natures-wrath/primal-destructivity.c",
        "/guilds/druid/natures-wrath/volcanic-knowledge.c");
    addChild("/guilds/druid/natures-wrath/geomancer-body.c",
        "/guilds/druid/natures-wrath/primal-wrath-passive.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/druid/natures-wrath/fire-in-the-blood.c");

    addChild("/guilds/druid/natures-wrath/fire-in-the-blood.c",
        "/guilds/druid/natures-wrath/earth-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/natures-wrath/tectonic-mastery.c");
    addResearchElement("/guilds/druid/natures-wrath/earthen-grasp.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/earth-fire-mastery.c");

    addChild("/guilds/druid/natures-wrath/tectonic-mastery.c",
        "/guilds/druid/natures-wrath/primal-fury.c");
    addChild("/guilds/druid/natures-wrath/earthen-grasp.c",
        "/guilds/druid/natures-wrath/magma-surge.c");
    addChild("/guilds/druid/natures-wrath/earth-fire-mastery.c",
        "/guilds/druid/natures-wrath/primal-destructivity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/druid/natures-wrath/tectonic-resilience.c");

    addChild("/guilds/druid/natures-wrath/tectonic-resilience.c",
        "/guilds/druid/natures-wrath/fire-in-the-blood.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/infernal-wild.c");
    addResearchElement("/guilds/druid/natures-wrath/conflagration.c");
    addResearchElement("/guilds/druid/natures-wrath/deep-earth-bond.c");

    addChild("/guilds/druid/natures-wrath/infernal-wild.c",
        "/guilds/druid/natures-wrath/tectonic-mastery.c");
    addChild("/guilds/druid/natures-wrath/conflagration.c",
        "/guilds/druid/natures-wrath/scorched-earth.c");
    addChild("/guilds/druid/natures-wrath/deep-earth-bond.c",
        "/guilds/druid/natures-wrath/geomancer-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/magma-heart.c");

    addChild("/guilds/druid/natures-wrath/magma-heart.c",
        "/guilds/druid/natures-wrath/tectonic-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/wrath-mastery.c");
    addResearchElement("/guilds/druid/natures-wrath/earthquake.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/tectonic-knowledge.c");

    addChild("/guilds/druid/natures-wrath/wrath-mastery.c",
        "/guilds/druid/natures-wrath/infernal-wild.c");
    addChild("/guilds/druid/natures-wrath/earthquake.c",
        "/guilds/druid/natures-wrath/earthen-grasp.c");
    addChild("/guilds/druid/natures-wrath/tectonic-knowledge.c",
        "/guilds/druid/natures-wrath/earth-fire-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/natures-wrath/natures-destroyer.c");

    addChild("/guilds/druid/natures-wrath/natures-destroyer.c",
        "/guilds/druid/natures-wrath/magma-heart.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/volcanic-fury.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/volcanic-mastery-passive.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/wrath-of-nature-ki.c");

    addChild("/guilds/druid/natures-wrath/volcanic-fury.c",
        "/guilds/druid/natures-wrath/conflagration.c");
    addChild(
        "/guilds/druid/natures-wrath/volcanic-mastery-passive.c",
        "/guilds/druid/natures-wrath/natures-destroyer.c");
    addChild("/guilds/druid/natures-wrath/wrath-of-nature-ki.c",
        "/guilds/druid/natures-wrath/tectonic-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/living-volcano.c");

    addChild("/guilds/druid/natures-wrath/living-volcano.c",
        "/guilds/druid/natures-wrath/volcanic-mastery-passive.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/natures-wrath/primal-geomancer.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/volcanic-supremacy.c");

    addChild("/guilds/druid/natures-wrath/primal-geomancer.c",
        "/guilds/druid/natures-wrath/wrath-of-nature-ki.c");
    addChild("/guilds/druid/natures-wrath/volcanic-supremacy.c",
        "/guilds/druid/natures-wrath/living-volcano.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/natures-wrath/earth-fire-supremacy.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/molten-champion.c");

    addChild("/guilds/druid/natures-wrath/earth-fire-supremacy.c",
        "/guilds/druid/natures-wrath/primal-geomancer.c");
    addChild("/guilds/druid/natures-wrath/molten-champion.c",
        "/guilds/druid/natures-wrath/volcanic-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/druid/natures-wrath/natures-wrath-mastery.c");
    addResearchElement(
        "/guilds/druid/natures-wrath/avatar-of-wrath.c");

    addChild("/guilds/druid/natures-wrath/natures-wrath-mastery.c",
        "/guilds/druid/natures-wrath/earth-fire-supremacy.c");
    addChild("/guilds/druid/natures-wrath/avatar-of-wrath.c",
        "/guilds/druid/natures-wrath/molten-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/druid/natures-wrath/wrath-incarnate.c");

    addChild("/guilds/druid/natures-wrath/wrath-incarnate.c",
        "/guilds/druid/natures-wrath/avatar-of-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Nature's Wrath");
    Description("This research tree grants the druid mastery over "
        "nature's most destructive forces - the burning fury of "
        "wildfire and the crushing power of the living earth. "
        "As the druid progresses, these spells grow from simple "
        "conjurations into cataclysmic expressions of primal power.");
    Source("druid");
    addResearchElement("/guilds/druid/natures-wrath/root.c");
    TreeRoot("/guilds/druid/natures-wrath/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    NinthLevel();
    ThirteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    FortyThirdLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtyNinthLevel();
}
