//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/spirit-bond.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/primal-connection.c");

    addChild("/guilds/ranger/natures-ally/spirit-bond.c",
        "/guilds/ranger/natures-ally/root.c");
    addChild("/guilds/ranger/natures-ally/primal-connection.c",
        "/guilds/ranger/natures-ally/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/wolfs-cunning.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/wild-empathy.c");

    addChild("/guilds/ranger/natures-ally/wolfs-cunning.c",
        "/guilds/ranger/natures-ally/root.c");
    addChild("/guilds/ranger/natures-ally/wild-empathy.c",
        "/guilds/ranger/natures-ally/spirit-bond.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/hawks-eye.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/beast-empathy.c");

    addChild("/guilds/ranger/natures-ally/hawks-eye.c",
        "/guilds/ranger/natures-ally/root.c");
    addChild("/guilds/ranger/natures-ally/beast-empathy.c",
        "/guilds/ranger/natures-ally/spirit-bond.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/bears-fortitude.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/natures-call.c");

    addChild("/guilds/ranger/natures-ally/bears-fortitude.c",
        "/guilds/ranger/natures-ally/root.c");
    addChild("/guilds/ranger/natures-ally/natures-call.c",
        "/guilds/ranger/natures-ally/primal-connection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/serpents-reflexes.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/natures-strength.c");

    addChild("/guilds/ranger/natures-ally/serpents-reflexes.c",
        "/guilds/ranger/natures-ally/root.c");
    addChild("/guilds/ranger/natures-ally/natures-strength.c",
        "/guilds/ranger/natures-ally/primal-connection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/feral-instinct.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/enhanced-channeling.c");

    addChild("/guilds/ranger/natures-ally/feral-instinct.c",
        "/guilds/ranger/natures-ally/beast-empathy.c");
    addChild("/guilds/ranger/natures-ally/enhanced-channeling.c",
        "/guilds/ranger/natures-ally/beast-empathy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/stags-swiftness.c");

    addChild("/guilds/ranger/natures-ally/stags-swiftness.c",
        "/guilds/ranger/natures-ally/wolfs-cunning.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/owls-wisdom.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/spirit-guide.c");

    addChild("/guilds/ranger/natures-ally/owls-wisdom.c",
        "/guilds/ranger/natures-ally/hawks-eye.c");
    addChild("/guilds/ranger/natures-ally/spirit-guide.c",
        "/guilds/ranger/natures-ally/natures-call.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/boars-fury.c");

    addChild("/guilds/ranger/natures-ally/boars-fury.c",
        "/guilds/ranger/natures-ally/bears-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/panthers-stealth.c");

    addChild("/guilds/ranger/natures-ally/panthers-stealth.c",
        "/guilds/ranger/natures-ally/serpents-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/primal-fury.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/deepened-bond.c");

    addChild("/guilds/ranger/natures-ally/primal-fury.c",
        "/guilds/ranger/natures-ally/feral-instinct.c");
    addChild("/guilds/ranger/natures-ally/deepened-bond.c",
        "/guilds/ranger/natures-ally/feral-instinct.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/eagles-might.c");

    addChild("/guilds/ranger/natures-ally/eagles-might.c",
        "/guilds/ranger/natures-ally/stags-swiftness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/pack-tactics.c");

    addChild("/guilds/ranger/natures-ally/pack-tactics.c",
        "/guilds/ranger/natures-ally/spirit-guide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/tigers-ferocity.c");

    addChild("/guilds/ranger/natures-ally/tigers-ferocity.c",
        "/guilds/ranger/natures-ally/boars-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/apex-bond.c");

    addChild("/guilds/ranger/natures-ally/apex-bond.c",
        "/guilds/ranger/natures-ally/primal-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/dragons-presence.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/spirit-communion.c");

    addChild("/guilds/ranger/natures-ally/dragons-presence.c",
        "/guilds/ranger/natures-ally/eagles-might.c");
    addChild("/guilds/ranger/natures-ally/spirit-communion.c",
        "/guilds/ranger/natures-ally/apex-bond.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/natures-wrath.c");

    addChild("/guilds/ranger/natures-ally/natures-wrath.c",
        "/guilds/ranger/natures-ally/pack-tactics.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/phoenix-spirit.c");

    addChild("/guilds/ranger/natures-ally/phoenix-spirit.c",
        "/guilds/ranger/natures-ally/owls-wisdom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/spirit-mastery.c");

    addChild("/guilds/ranger/natures-ally/spirit-mastery.c",
        "/guilds/ranger/natures-ally/apex-bond.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/hydras-regeneration.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/primal-mastery.c");

    addChild(
        "/guilds/ranger/natures-ally/hydras-regeneration.c",
        "/guilds/ranger/natures-ally/bears-fortitude.c");
    addChild("/guilds/ranger/natures-ally/primal-mastery.c",
        "/guilds/ranger/natures-ally/spirit-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/primal-dominion.c");

    addChild("/guilds/ranger/natures-ally/primal-dominion.c",
        "/guilds/ranger/natures-ally/natures-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/griffons-valor.c");

    addChild("/guilds/ranger/natures-ally/griffons-valor.c",
        "/guilds/ranger/natures-ally/dragons-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/natures-champion.c");

    addChild("/guilds/ranger/natures-ally/natures-champion.c",
        "/guilds/ranger/natures-ally/spirit-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/leviathans-power.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/transcendent-bond.c");

    addChild("/guilds/ranger/natures-ally/leviathans-power.c",
        "/guilds/ranger/natures-ally/tigers-ferocity.c");
    addChild("/guilds/ranger/natures-ally/transcendent-bond.c",
        "/guilds/ranger/natures-ally/primal-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/primeval-bond.c");

    addChild("/guilds/ranger/natures-ally/primeval-bond.c",
        "/guilds/ranger/natures-ally/primal-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/natures-avatar.c");
    addResearchElement(
        "/guilds/ranger/natures-ally/supreme-channeling.c");

    addChild("/guilds/ranger/natures-ally/natures-avatar.c",
        "/guilds/ranger/natures-ally/griffons-valor.c");
    addChild("/guilds/ranger/natures-ally/supreme-channeling.c",
        "/guilds/ranger/natures-ally/natures-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/avatar-mastery.c");

    addChild("/guilds/ranger/natures-ally/avatar-mastery.c",
        "/guilds/ranger/natures-ally/natures-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/natures-ally/eternal-spirits.c");

    addChild("/guilds/ranger/natures-ally/eternal-spirits.c",
        "/guilds/ranger/natures-ally/avatar-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Nature's Ally");
    Description("This research tree provides the ranger with "
        "the ability to channel the spirits of nature's "
        "creatures, assuming stances that grant powerful "
        "combat enhancements.");
    Source("ranger");
    addResearchElement("/guilds/ranger/natures-ally/root.c");
    TreeRoot("/guilds/ranger/natures-ally/root.c");

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
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    SixtyFirstLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
