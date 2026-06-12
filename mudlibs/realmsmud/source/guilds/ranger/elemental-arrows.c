//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/fire-attunement.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/frost-attunement.c");

    addChild("/guilds/ranger/elemental-arrows/fire-attunement.c",
        "/guilds/ranger/elemental-arrows/root.c");
    addChild("/guilds/ranger/elemental-arrows/frost-attunement.c",
        "/guilds/ranger/elemental-arrows/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/storm-attunement.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/earth-attunement.c");

    addChild("/guilds/ranger/elemental-arrows/storm-attunement.c",
        "/guilds/ranger/elemental-arrows/root.c");
    addChild("/guilds/ranger/elemental-arrows/earth-attunement.c",
        "/guilds/ranger/elemental-arrows/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/flame-arrow.c");

    addChild("/guilds/ranger/elemental-arrows/flame-arrow.c",
        "/guilds/ranger/elemental-arrows/fire-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/frost-arrow.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/burning-focus.c");

    addChild("/guilds/ranger/elemental-arrows/frost-arrow.c",
        "/guilds/ranger/elemental-arrows/frost-attunement.c");
    addChild("/guilds/ranger/elemental-arrows/burning-focus.c",
        "/guilds/ranger/elemental-arrows/fire-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/shock-arrow.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/freezing-focus.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/flame-lore.c");

    addChild("/guilds/ranger/elemental-arrows/shock-arrow.c",
        "/guilds/ranger/elemental-arrows/storm-attunement.c");
    addChild("/guilds/ranger/elemental-arrows/freezing-focus.c",
        "/guilds/ranger/elemental-arrows/frost-attunement.c");
    addChild("/guilds/ranger/elemental-arrows/flame-lore.c",
        "/guilds/ranger/elemental-arrows/burning-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/acid-arrow.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/crackling-focus.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/frost-lore.c");

    addChild("/guilds/ranger/elemental-arrows/acid-arrow.c",
        "/guilds/ranger/elemental-arrows/earth-attunement.c");
    addChild("/guilds/ranger/elemental-arrows/crackling-focus.c",
        "/guilds/ranger/elemental-arrows/storm-attunement.c");
    addChild("/guilds/ranger/elemental-arrows/frost-lore.c",
        "/guilds/ranger/elemental-arrows/freezing-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/corroding-focus.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/storm-lore.c");

    addChild("/guilds/ranger/elemental-arrows/corroding-focus.c",
        "/guilds/ranger/elemental-arrows/earth-attunement.c");
    addChild("/guilds/ranger/elemental-arrows/storm-lore.c",
        "/guilds/ranger/elemental-arrows/crackling-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/venom-lore.c");

    addChild("/guilds/ranger/elemental-arrows/venom-lore.c",
        "/guilds/ranger/elemental-arrows/corroding-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/elemental-precision.c");

    addChild("/guilds/ranger/elemental-arrows/elemental-precision.c",
        "/guilds/ranger/elemental-arrows/burning-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/blazing-volley.c");

    addChild("/guilds/ranger/elemental-arrows/blazing-volley.c",
        "/guilds/ranger/elemental-arrows/burning-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/glacial-shot.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/natures-conduit.c");

    addChild("/guilds/ranger/elemental-arrows/glacial-shot.c",
        "/guilds/ranger/elemental-arrows/freezing-focus.c");
    addChild("/guilds/ranger/elemental-arrows/natures-conduit.c",
        "/guilds/ranger/elemental-arrows/freezing-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/thunderbolt-arrow.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/primal-infusion.c");

    addChild("/guilds/ranger/elemental-arrows/thunderbolt-arrow.c",
        "/guilds/ranger/elemental-arrows/crackling-focus.c");
    addChild("/guilds/ranger/elemental-arrows/primal-infusion.c",
        "/guilds/ranger/elemental-arrows/crackling-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/venomous-barb.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/greater-flame-lore.c");

    addChild("/guilds/ranger/elemental-arrows/venomous-barb.c",
        "/guilds/ranger/elemental-arrows/corroding-focus.c");
    addChild("/guilds/ranger/elemental-arrows/greater-flame-lore.c",
        "/guilds/ranger/elemental-arrows/elemental-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/greater-frost-lore.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/toxic-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/greater-frost-lore.c",
        "/guilds/ranger/elemental-arrows/natures-conduit.c");
    addChild("/guilds/ranger/elemental-arrows/toxic-mastery.c",
        "/guilds/ranger/elemental-arrows/corroding-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/greater-storm-lore.c");

    addChild("/guilds/ranger/elemental-arrows/greater-storm-lore.c",
        "/guilds/ranger/elemental-arrows/primal-infusion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/greater-venom-lore.c");

    addChild("/guilds/ranger/elemental-arrows/greater-venom-lore.c",
        "/guilds/ranger/elemental-arrows/toxic-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/elemental-convergence.c");

    addChild("/guilds/ranger/elemental-arrows/elemental-convergence.c",
        "/guilds/ranger/elemental-arrows/elemental-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/inferno-shot.c");

    addChild("/guilds/ranger/elemental-arrows/inferno-shot.c",
        "/guilds/ranger/elemental-arrows/elemental-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/blizzard-arrow.c");

    addChild("/guilds/ranger/elemental-arrows/blizzard-arrow.c",
        "/guilds/ranger/elemental-arrows/natures-conduit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/storm-bolt.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/primal-archery.c");

    addChild("/guilds/ranger/elemental-arrows/storm-bolt.c",
        "/guilds/ranger/elemental-arrows/primal-infusion.c");
    addChild("/guilds/ranger/elemental-arrows/primal-archery.c",
        "/guilds/ranger/elemental-arrows/elemental-convergence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/dissolving-shot.c");

    addChild("/guilds/ranger/elemental-arrows/dissolving-shot.c",
        "/guilds/ranger/elemental-arrows/toxic-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/searing-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/searing-mastery.c",
        "/guilds/ranger/elemental-arrows/primal-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/glacial-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/glacial-mastery.c",
        "/guilds/ranger/elemental-arrows/primal-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/tempest-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/tempest-mastery.c",
        "/guilds/ranger/elemental-arrows/primal-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/dissolution-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/dissolution-mastery.c",
        "/guilds/ranger/elemental-arrows/primal-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/elemental-supremacy.c");

    addChild("/guilds/ranger/elemental-arrows/elemental-supremacy.c",
        "/guilds/ranger/elemental-arrows/primal-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/phoenix-arrow.c");

    addChild("/guilds/ranger/elemental-arrows/phoenix-arrow.c",
        "/guilds/ranger/elemental-arrows/elemental-convergence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/absolute-zero.c");

    addChild("/guilds/ranger/elemental-arrows/absolute-zero.c",
        "/guilds/ranger/elemental-arrows/elemental-convergence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/cataclysm-bolt.c");

    addChild("/guilds/ranger/elemental-arrows/cataclysm-bolt.c",
        "/guilds/ranger/elemental-arrows/elemental-convergence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/annihilating-arrow.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/force-of-elements.c");

    addChild("/guilds/ranger/elemental-arrows/annihilating-arrow.c",
        "/guilds/ranger/elemental-arrows/elemental-convergence.c");
    addChild("/guilds/ranger/elemental-arrows/force-of-elements.c",
        "/guilds/ranger/elemental-arrows/elemental-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/inferno-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/inferno-mastery.c",
        "/guilds/ranger/elemental-arrows/elemental-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/blizzard-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/blizzard-mastery.c",
        "/guilds/ranger/elemental-arrows/elemental-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/cataclysm-mastery.c");

    addChild("/guilds/ranger/elemental-arrows/cataclysm-mastery.c",
        "/guilds/ranger/elemental-arrows/elemental-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/annihilation-mastery.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/avatar-of-nature.c");

    addChild("/guilds/ranger/elemental-arrows/annihilation-mastery.c",
        "/guilds/ranger/elemental-arrows/force-of-elements.c");
    addChild("/guilds/ranger/elemental-arrows/avatar-of-nature.c",
        "/guilds/ranger/elemental-arrows/force-of-elements.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyLevel()
{
    addResearchElement(
        "/guilds/ranger/elemental-arrows/suns-wrath.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/winters-end.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/skys-fury.c");
    addResearchElement(
        "/guilds/ranger/elemental-arrows/natures-wrath-arrow.c");

    addChild("/guilds/ranger/elemental-arrows/suns-wrath.c",
        "/guilds/ranger/elemental-arrows/force-of-elements.c");
    addChild("/guilds/ranger/elemental-arrows/winters-end.c",
        "/guilds/ranger/elemental-arrows/force-of-elements.c");
    addChild("/guilds/ranger/elemental-arrows/skys-fury.c",
        "/guilds/ranger/elemental-arrows/force-of-elements.c");
    addChild("/guilds/ranger/elemental-arrows/natures-wrath-arrow.c",
        "/guilds/ranger/elemental-arrows/force-of-elements.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Elemental Arrows");
    Description("This research tree provides the ranger with the "
        "ability to infuse arrows with elemental energy - fire, "
        "frost, storm, and acid.");
    Source("ranger");
    addResearchElement("/guilds/ranger/elemental-arrows/root.c");
    TreeRoot("/guilds/ranger/elemental-arrows/root.c");

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
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
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
    SeventyLevel();
}
