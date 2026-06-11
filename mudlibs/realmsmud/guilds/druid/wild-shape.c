//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/druid/wild-shape/wolf-aspect.c");
    addResearchElement("/guilds/druid/wild-shape/primal-instinct.c");
    addResearchElement("/guilds/druid/wild-shape/feral-awareness.c");

    addChild("/guilds/druid/wild-shape/wolf-aspect.c",
        "/guilds/druid/wild-shape/root.c");
    addChild("/guilds/druid/wild-shape/primal-instinct.c",
        "/guilds/druid/wild-shape/root.c");
    addChild("/guilds/druid/wild-shape/feral-awareness.c",
        "/guilds/druid/wild-shape/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/wild-shape/beast-bond.c");
    addResearchElement("/guilds/druid/wild-shape/beast-toughness.c");

    addChild("/guilds/druid/wild-shape/beast-bond.c",
        "/guilds/druid/wild-shape/primal-instinct.c");
    addChild("/guilds/druid/wild-shape/beast-toughness.c",
        "/guilds/druid/wild-shape/feral-awareness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/druid/wild-shape/bear-aspect.c");
    addResearchElement("/guilds/druid/wild-shape/wolves-ferocity.c");
    addResearchElement("/guilds/druid/wild-shape/natural-strikes.c");

    addChild("/guilds/druid/wild-shape/bear-aspect.c",
        "/guilds/druid/wild-shape/wolf-aspect.c");
    addChild("/guilds/druid/wild-shape/wolves-ferocity.c",
        "/guilds/druid/wild-shape/beast-bond.c");
    addChild("/guilds/druid/wild-shape/natural-strikes.c",
        "/guilds/druid/wild-shape/feral-awareness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/druid/wild-shape/wild-ferocity.c");
    addResearchElement("/guilds/druid/wild-shape/predators-grace.c");

    addChild("/guilds/druid/wild-shape/wild-ferocity.c",
        "/guilds/druid/wild-shape/beast-bond.c");
    addChild("/guilds/druid/wild-shape/predators-grace.c",
        "/guilds/druid/wild-shape/beast-toughness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/druid/wild-shape/cat-aspect.c");
    addResearchElement("/guilds/druid/wild-shape/serpent-scales.c");
    addResearchElement("/guilds/druid/wild-shape/primal-hide.c");

    addChild("/guilds/druid/wild-shape/cat-aspect.c",
        "/guilds/druid/wild-shape/wolf-aspect.c");
    addChild("/guilds/druid/wild-shape/serpent-scales.c",
        "/guilds/druid/wild-shape/natural-strikes.c");
    addChild("/guilds/druid/wild-shape/primal-hide.c",
        "/guilds/druid/wild-shape/natural-strikes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/wild-shape/hunters-body.c");
    addResearchElement("/guilds/druid/wild-shape/pack-instincts.c");

    addChild("/guilds/druid/wild-shape/hunters-body.c",
        "/guilds/druid/wild-shape/wild-ferocity.c");
    addChild("/guilds/druid/wild-shape/pack-instincts.c",
        "/guilds/druid/wild-shape/predators-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/druid/wild-shape/eagle-aspect.c");
    addResearchElement("/guilds/druid/wild-shape/eagle-eyes.c");

    addChild("/guilds/druid/wild-shape/eagle-aspect.c",
        "/guilds/druid/wild-shape/cat-aspect.c");
    addChild("/guilds/druid/wild-shape/eagle-eyes.c",
        "/guilds/druid/wild-shape/pack-instincts.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/druid/wild-shape/bears-endurance.c");
    addResearchElement("/guilds/druid/wild-shape/crushing-blow.c");

    addChild("/guilds/druid/wild-shape/bears-endurance.c",
        "/guilds/druid/wild-shape/pack-instincts.c");
    addChild("/guilds/druid/wild-shape/crushing-blow.c",
        "/guilds/druid/wild-shape/primal-hide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/wild-shape/primal-surge.c");
    addResearchElement("/guilds/druid/wild-shape/bear-strength.c");

    addChild("/guilds/druid/wild-shape/primal-surge.c",
        "/guilds/druid/wild-shape/hunters-body.c");
    addChild("/guilds/druid/wild-shape/bear-strength.c",
        "/guilds/druid/wild-shape/bears-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/primal-fury-shape.c");
    addResearchElement("/guilds/druid/wild-shape/cats-reflexes.c");

    addChild("/guilds/druid/wild-shape/primal-fury-shape.c",
        "/guilds/druid/wild-shape/bear-aspect.c");
    addChild("/guilds/druid/wild-shape/cats-reflexes.c",
        "/guilds/druid/wild-shape/bears-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/druid/wild-shape/cats-grace.c");
    addResearchElement("/guilds/druid/wild-shape/fluid-motion.c");

    addChild("/guilds/druid/wild-shape/cats-grace.c",
        "/guilds/druid/wild-shape/cats-reflexes.c");
    addChild("/guilds/druid/wild-shape/fluid-motion.c",
        "/guilds/druid/wild-shape/crushing-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/eagles-precision.c");

    addChild("/guilds/druid/wild-shape/eagles-precision.c",
        "/guilds/druid/wild-shape/cats-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/wild-shape/serpent-aspect.c");
    addResearchElement("/guilds/druid/wild-shape/apex-predator.c");
    addResearchElement("/guilds/druid/wild-shape/primal-body.c");

    addChild("/guilds/druid/wild-shape/serpent-aspect.c",
        "/guilds/druid/wild-shape/eagle-aspect.c");
    addChild("/guilds/druid/wild-shape/apex-predator.c",
        "/guilds/druid/wild-shape/primal-surge.c");
    addChild("/guilds/druid/wild-shape/primal-body.c",
        "/guilds/druid/wild-shape/apex-predator.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/serpents-patience.c");

    addChild("/guilds/druid/wild-shape/serpents-patience.c",
        "/guilds/druid/wild-shape/eagles-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/wild-shape/predator-supreme.c");

    addChild("/guilds/druid/wild-shape/predator-supreme.c",
        "/guilds/druid/wild-shape/fluid-motion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/druid/wild-shape/avatar-might.c");

    addChild("/guilds/druid/wild-shape/avatar-might.c",
        "/guilds/druid/wild-shape/primal-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/druid/wild-shape/one-with-the-beast.c");
    addResearchElement("/guilds/druid/wild-shape/primal-flesh.c");

    addChild("/guilds/druid/wild-shape/one-with-the-beast.c",
        "/guilds/druid/wild-shape/apex-predator.c");
    addChild("/guilds/druid/wild-shape/primal-flesh.c",
        "/guilds/druid/wild-shape/serpents-patience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/avatar-of-the-wild.c");

    addChild("/guilds/druid/wild-shape/avatar-of-the-wild.c",
        "/guilds/druid/wild-shape/primal-fury-shape.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/beast-form-mastery.c");

    addChild("/guilds/druid/wild-shape/beast-form-mastery.c",
        "/guilds/druid/wild-shape/avatar-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/druid/wild-shape/beast-mastery.c");
    addResearchElement(
        "/guilds/druid/wild-shape/wild-shape-mastery.c");

    addChild("/guilds/druid/wild-shape/beast-mastery.c",
        "/guilds/druid/wild-shape/primal-flesh.c");
    addChild("/guilds/druid/wild-shape/wild-shape-mastery.c",
        "/guilds/druid/wild-shape/one-with-the-beast.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/primal-ascension.c");

    addChild("/guilds/druid/wild-shape/primal-ascension.c",
        "/guilds/druid/wild-shape/beast-form-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/supreme-predator.c");
    addResearchElement(
        "/guilds/druid/wild-shape/primal-beast-lord.c");

    addChild("/guilds/druid/wild-shape/supreme-predator.c",
        "/guilds/druid/wild-shape/primal-ascension.c");
    addChild("/guilds/druid/wild-shape/primal-beast-lord.c",
        "/guilds/druid/wild-shape/wild-shape-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/druid/wild-shape/beast-champion.c");
    addResearchElement(
        "/guilds/druid/wild-shape/avatar-supremacy.c");

    addChild("/guilds/druid/wild-shape/beast-champion.c",
        "/guilds/druid/wild-shape/supreme-predator.c");
    addChild("/guilds/druid/wild-shape/avatar-supremacy.c",
        "/guilds/druid/wild-shape/primal-beast-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/druid/wild-shape/avatar-eternal.c");
    addResearchElement(
        "/guilds/druid/wild-shape/beast-apotheosis.c");

    addChild("/guilds/druid/wild-shape/avatar-eternal.c",
        "/guilds/druid/wild-shape/beast-champion.c");
    addChild("/guilds/druid/wild-shape/beast-apotheosis.c",
        "/guilds/druid/wild-shape/avatar-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/druid/wild-shape/primal-beast-eternal.c");

    addChild("/guilds/druid/wild-shape/primal-beast-eternal.c",
        "/guilds/druid/wild-shape/avatar-eternal.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Wild Shape");
    Description("This research tree grants the druid mastery over "
        "the primal art of channeling the essence of wild creatures. "
        "Each aspect is a sustained combat stance that can be "
        "activated and deactivated at will, channeling the fighting "
        "spirit of a different creature through the druid's own form.");
    Source("druid");
    addResearchElement("/guilds/druid/wild-shape/root.c");
    TreeRoot("/guilds/druid/wild-shape/root.c");

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
    FortyThirdLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtyNinthLevel();
}
