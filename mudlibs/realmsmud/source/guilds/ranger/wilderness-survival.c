//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/hardy-constitution.c");
    addResearchElement(
        "/guilds/ranger/wilderness-survival/endurance-training.c");

    addChild(
        "/guilds/ranger/wilderness-survival/hardy-constitution.c",
        "/guilds/ranger/wilderness-survival/root.c");
    addChild(
        "/guilds/ranger/wilderness-survival/endurance-training.c",
        "/guilds/ranger/wilderness-survival/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/thick-skin.c");
    addResearchElement(
        "/guilds/ranger/wilderness-survival/weather-hardened.c");

    addChild("/guilds/ranger/wilderness-survival/thick-skin.c",
        "/guilds/ranger/wilderness-survival/hardy-constitution.c");
    addChild(
        "/guilds/ranger/wilderness-survival/weather-hardened.c",
        "/guilds/ranger/wilderness-survival/hardy-constitution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/frost-hardened.c");
    addResearchElement(
        "/guilds/ranger/wilderness-survival/iron-will.c");

    addChild(
        "/guilds/ranger/wilderness-survival/frost-hardened.c",
        "/guilds/ranger/wilderness-survival/hardy-constitution.c");
    addChild("/guilds/ranger/wilderness-survival/iron-will.c",
        "/guilds/ranger/wilderness-survival/endurance-training.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/tough-as-nails.c");
    addResearchElement(
        "/guilds/ranger/wilderness-survival/rugged-endurance.c");

    addChild(
        "/guilds/ranger/wilderness-survival/tough-as-nails.c",
        "/guilds/ranger/wilderness-survival/thick-skin.c");
    addChild(
        "/guilds/ranger/wilderness-survival/rugged-endurance.c",
        "/guilds/ranger/wilderness-survival/endurance-training.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/natural-resistance.c");
    addResearchElement(
        "/guilds/ranger/wilderness-survival/storm-resistance.c");

    addChild(
        "/guilds/ranger/wilderness-survival/natural-resistance.c",
        "/guilds/ranger/wilderness-survival/weather-hardened.c");
    addChild(
        "/guilds/ranger/wilderness-survival/storm-resistance.c",
        "/guilds/ranger/wilderness-survival/frost-hardened.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/survivors-grit.c");

    addChild(
        "/guilds/ranger/wilderness-survival/survivors-grit.c",
        "/guilds/ranger/wilderness-survival/iron-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/stone-body.c");

    addChild("/guilds/ranger/wilderness-survival/stone-body.c",
        "/guilds/ranger/wilderness-survival/tough-as-nails.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/fire-resistance.c");
    addResearchElement(
        "/guilds/ranger/wilderness-survival/cold-resistance.c");

    addChild(
        "/guilds/ranger/wilderness-survival/fire-resistance.c",
        "/guilds/ranger/wilderness-survival/natural-resistance.c");
    addChild(
        "/guilds/ranger/wilderness-survival/cold-resistance.c",
        "/guilds/ranger/wilderness-survival/storm-resistance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/pain-tolerance.c");

    addChild(
        "/guilds/ranger/wilderness-survival/pain-tolerance.c",
        "/guilds/ranger/wilderness-survival/survivors-grit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/unbreakable.c");

    addChild("/guilds/ranger/wilderness-survival/unbreakable.c",
        "/guilds/ranger/wilderness-survival/survivors-grit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/iron-body.c");

    addChild("/guilds/ranger/wilderness-survival/iron-body.c",
        "/guilds/ranger/wilderness-survival/stone-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/elemental-ward.c");

    addChild(
        "/guilds/ranger/wilderness-survival/elemental-ward.c",
        "/guilds/ranger/wilderness-survival/fire-resistance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/natures-shield.c");

    addChild(
        "/guilds/ranger/wilderness-survival/natures-shield.c",
        "/guilds/ranger/wilderness-survival/cold-resistance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/undying-resolve.c");

    addChild(
        "/guilds/ranger/wilderness-survival/undying-resolve.c",
        "/guilds/ranger/wilderness-survival/unbreakable.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/adamantine-body.c");

    addChild(
        "/guilds/ranger/wilderness-survival/adamantine-body.c",
        "/guilds/ranger/wilderness-survival/iron-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/battle-hardened.c");

    addChild(
        "/guilds/ranger/wilderness-survival/battle-hardened.c",
        "/guilds/ranger/wilderness-survival/adamantine-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/primal-fortitude.c");

    addChild(
        "/guilds/ranger/wilderness-survival/primal-fortitude.c",
        "/guilds/ranger/wilderness-survival/undying-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/greater-elemental-ward.c");

    addChild(
        "/guilds/ranger/wilderness-survival/greater-elemental-ward.c",
        "/guilds/ranger/wilderness-survival/elemental-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/greater-natures-shield.c");

    addChild(
        "/guilds/ranger/wilderness-survival/greater-natures-shield.c",
        "/guilds/ranger/wilderness-survival/natures-shield.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/force-of-nature.c");

    addChild(
        "/guilds/ranger/wilderness-survival/force-of-nature.c",
        "/guilds/ranger/wilderness-survival/adamantine-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/wardens-resilience.c");

    addChild(
        "/guilds/ranger/wilderness-survival/wardens-resilience.c",
        "/guilds/ranger/wilderness-survival/force-of-nature.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/apex-predator.c");

    addChild(
        "/guilds/ranger/wilderness-survival/apex-predator.c",
        "/guilds/ranger/wilderness-survival/primal-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/legendary-endurance.c");

    addChild(
        "/guilds/ranger/wilderness-survival/legendary-endurance.c",
        "/guilds/ranger/wilderness-survival/apex-predator.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/immortal-vigor.c");

    addChild(
        "/guilds/ranger/wilderness-survival/immortal-vigor.c",
        "/guilds/ranger/wilderness-survival/apex-predator.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/natures-chosen.c");

    addChild(
        "/guilds/ranger/wilderness-survival/natures-chosen.c",
        "/guilds/ranger/wilderness-survival/force-of-nature.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/supreme-resilience.c");

    addChild(
        "/guilds/ranger/wilderness-survival/supreme-resilience.c",
        "/guilds/ranger/wilderness-survival/greater-elemental-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/supreme-ward.c");

    addChild(
        "/guilds/ranger/wilderness-survival/supreme-ward.c",
        "/guilds/ranger/wilderness-survival/greater-natures-shield.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/primeval-resilience.c");

    addChild(
        "/guilds/ranger/wilderness-survival/primeval-resilience.c",
        "/guilds/ranger/wilderness-survival/immortal-vigor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/indomitable.c");

    addChild(
        "/guilds/ranger/wilderness-survival/indomitable.c",
        "/guilds/ranger/wilderness-survival/natures-chosen.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/eternal-warden.c");

    addChild(
        "/guilds/ranger/wilderness-survival/eternal-warden.c",
        "/guilds/ranger/wilderness-survival/primeval-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/perfected-body.c");

    addChild(
        "/guilds/ranger/wilderness-survival/perfected-body.c",
        "/guilds/ranger/wilderness-survival/indomitable.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/absolute-immunity.c");

    addChild(
        "/guilds/ranger/wilderness-survival/absolute-immunity.c",
        "/guilds/ranger/wilderness-survival/supreme-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/absolute-ward.c");

    addChild(
        "/guilds/ranger/wilderness-survival/absolute-ward.c",
        "/guilds/ranger/wilderness-survival/supreme-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/wilderness-mastery.c");

    addChild(
        "/guilds/ranger/wilderness-survival/wilderness-mastery.c",
        "/guilds/ranger/wilderness-survival/eternal-warden.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wilderness-survival/immortal-body.c");

    addChild(
        "/guilds/ranger/wilderness-survival/immortal-body.c",
        "/guilds/ranger/wilderness-survival/perfected-body.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Wilderness Survival");
    Description("This research tree provides the ranger with "
        "extraordinary physical resilience, resistance to the "
        "elements, and the toughness to endure any hardship.");
    Source("ranger");
    addResearchElement(
        "/guilds/ranger/wilderness-survival/root.c");
    TreeRoot("/guilds/ranger/wilderness-survival/root.c");

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
}
