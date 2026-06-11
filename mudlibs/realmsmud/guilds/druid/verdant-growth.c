//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/bark-skin.c");
    addResearchElement("/guilds/druid/verdant-growth/growth-lore.c");

    addChild("/guilds/druid/verdant-growth/bark-skin.c",
        "/guilds/druid/verdant-growth/root.c");
    addChild("/guilds/druid/verdant-growth/growth-lore.c",
        "/guilds/druid/verdant-growth/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/green-thumb.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/roots-endurance.c");

    addChild("/guilds/druid/verdant-growth/green-thumb.c",
        "/guilds/druid/verdant-growth/growth-lore.c");
    addChild("/guilds/druid/verdant-growth/roots-endurance.c",
        "/guilds/druid/verdant-growth/bark-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/verdant-mastery.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/living-wood-skin.c");

    addChild("/guilds/druid/verdant-growth/verdant-mastery.c",
        "/guilds/druid/verdant-growth/growth-lore.c");
    addChild("/guilds/druid/verdant-growth/living-wood-skin.c",
        "/guilds/druid/verdant-growth/green-thumb.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/deep-roots.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/bark-skin-passive.c");

    addChild("/guilds/druid/verdant-growth/deep-roots.c",
        "/guilds/druid/verdant-growth/bark-skin.c");
    addChild("/guilds/druid/verdant-growth/bark-skin-passive.c",
        "/guilds/druid/verdant-growth/living-wood-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/forest-wisdom.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/chlorophyll-body.c");
    addResearchElement("/guilds/druid/verdant-growth/living-bark.c");

    addChild("/guilds/druid/verdant-growth/forest-wisdom.c",
        "/guilds/druid/verdant-growth/verdant-mastery.c");
    addChild("/guilds/druid/verdant-growth/chlorophyll-body.c",
        "/guilds/druid/verdant-growth/bark-skin-passive.c");
    addChild("/guilds/druid/verdant-growth/living-bark.c",
        "/guilds/druid/verdant-growth/deep-roots.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/regeneration.c");
    addResearchElement("/guilds/druid/verdant-growth/wildbloom.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/forest-resilience.c");

    addChild("/guilds/druid/verdant-growth/regeneration.c",
        "/guilds/druid/verdant-growth/living-bark.c");
    addChild("/guilds/druid/verdant-growth/wildbloom.c",
        "/guilds/druid/verdant-growth/chlorophyll-body.c");
    addChild("/guilds/druid/verdant-growth/forest-resilience.c",
        "/guilds/druid/verdant-growth/forest-wisdom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/overgrowth-lore.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/deepwood-focus.c");

    addChild("/guilds/druid/verdant-growth/overgrowth-lore.c",
        "/guilds/druid/verdant-growth/forest-resilience.c");
    addChild("/guilds/druid/verdant-growth/deepwood-focus.c",
        "/guilds/druid/verdant-growth/wildbloom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/verdant-vigor.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/natures-toughness.c");

    addChild("/guilds/druid/verdant-growth/verdant-vigor.c",
        "/guilds/druid/verdant-growth/wildbloom.c");
    addChild("/guilds/druid/verdant-growth/natures-toughness.c",
        "/guilds/druid/verdant-growth/regeneration.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/one-with-nature.c");

    addChild("/guilds/druid/verdant-growth/one-with-nature.c",
        "/guilds/druid/verdant-growth/regeneration.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/thorn-skin.c");

    addChild("/guilds/druid/verdant-growth/thorn-skin.c",
        "/guilds/druid/verdant-growth/deepwood-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/natures-strength.c");
    addResearchElement("/guilds/druid/verdant-growth/fertile-mind.c");

    addChild("/guilds/druid/verdant-growth/natures-strength.c",
        "/guilds/druid/verdant-growth/verdant-vigor.c");
    addChild("/guilds/druid/verdant-growth/fertile-mind.c",
        "/guilds/druid/verdant-growth/one-with-nature.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/primal-root.c");
    addResearchElement("/guilds/druid/verdant-growth/primal-body.c");

    addChild("/guilds/druid/verdant-growth/primal-root.c",
        "/guilds/druid/verdant-growth/overgrowth-lore.c");
    addChild("/guilds/druid/verdant-growth/primal-body.c",
        "/guilds/druid/verdant-growth/natures-strength.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/verdant-toughness.c");

    addChild("/guilds/druid/verdant-growth/verdant-toughness.c",
        "/guilds/druid/verdant-growth/thorn-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/ironwood-body.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/ancient-wisdom.c");

    addChild("/guilds/druid/verdant-growth/ironwood-body.c",
        "/guilds/druid/verdant-growth/verdant-toughness.c");
    addChild("/guilds/druid/verdant-growth/ancient-wisdom.c",
        "/guilds/druid/verdant-growth/primal-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/verdant-champion.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/old-growth-power.c");

    addChild("/guilds/druid/verdant-growth/verdant-champion.c",
        "/guilds/druid/verdant-growth/ancient-wisdom.c");
    addChild("/guilds/druid/verdant-growth/old-growth-power.c",
        "/guilds/druid/verdant-growth/ironwood-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/wild-vigor.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/spirit-of-nature.c");

    addChild("/guilds/druid/verdant-growth/wild-vigor.c",
        "/guilds/druid/verdant-growth/old-growth-power.c");
    addChild("/guilds/druid/verdant-growth/spirit-of-nature.c",
        "/guilds/druid/verdant-growth/verdant-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/sylvan-mastery.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/verdant-immortal.c");

    addChild("/guilds/druid/verdant-growth/sylvan-mastery.c",
        "/guilds/druid/verdant-growth/verdant-champion.c");
    addChild("/guilds/druid/verdant-growth/verdant-immortal.c",
        "/guilds/druid/verdant-growth/old-growth-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/verdant-ascendant.c");
    addResearchElement("/guilds/druid/verdant-growth/primal-flora.c");

    addChild("/guilds/druid/verdant-growth/verdant-ascendant.c",
        "/guilds/druid/verdant-growth/sylvan-mastery.c");
    addChild("/guilds/druid/verdant-growth/primal-flora.c",
        "/guilds/druid/verdant-growth/verdant-immortal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/primal-wisdom.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/body-of-the-wild.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/natures-clarity.c");

    addChild("/guilds/druid/verdant-growth/primal-wisdom.c",
        "/guilds/druid/verdant-growth/verdant-ascendant.c");
    addChild("/guilds/druid/verdant-growth/body-of-the-wild.c",
        "/guilds/druid/verdant-growth/primal-flora.c");
    addChild("/guilds/druid/verdant-growth/natures-clarity.c",
        "/guilds/druid/verdant-growth/verdant-ascendant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/elder-growth.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/verdant-eternal.c");

    addChild("/guilds/druid/verdant-growth/elder-growth.c",
        "/guilds/druid/verdant-growth/primal-wisdom.c");
    addChild("/guilds/druid/verdant-growth/verdant-eternal.c",
        "/guilds/druid/verdant-growth/body-of-the-wild.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/verdant-sage.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/world-tree-form.c");

    addChild("/guilds/druid/verdant-growth/verdant-sage.c",
        "/guilds/druid/verdant-growth/verdant-ascendant.c");
    addChild("/guilds/druid/verdant-growth/world-tree-form.c",
        "/guilds/druid/verdant-growth/verdant-eternal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement(
        "/guilds/druid/verdant-growth/voice-of-the-forest.c");
    addResearchElement(
        "/guilds/druid/verdant-growth/verdant-apotheosis.c");

    addChild("/guilds/druid/verdant-growth/voice-of-the-forest.c",
        "/guilds/druid/verdant-growth/verdant-sage.c");
    addChild("/guilds/druid/verdant-growth/verdant-apotheosis.c",
        "/guilds/druid/verdant-growth/world-tree-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/druid/verdant-growth/living-world.c");

    addChild("/guilds/druid/verdant-growth/living-world.c",
        "/guilds/druid/verdant-growth/verdant-apotheosis.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Verdant Growth");
    Description("This research tree grants the druid mastery over "
        "the power of living plants - spells and passives that draw "
        "on the relentless vitality of the forest, from simple "
        "barkskin to the enduring power of the oldest trees.");
    Source("druid");
    addResearchElement("/guilds/druid/verdant-growth/root.c");
    TreeRoot("/guilds/druid/verdant-growth/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    ThirteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyFifthLevel();
    SixtyNinthLevel();
}
