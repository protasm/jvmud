//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/druid/primal-rites/rite-of-mending.c");

    addChild("/guilds/druid/primal-rites/rite-of-mending.c",
        "/guilds/druid/primal-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/druid/primal-rites/ritual-attunement.c");
    addResearchElement("/guilds/druid/primal-rites/rite-of-vigor.c");
    addResearchElement("/guilds/druid/primal-rites/rite-of-clarity.c");

    addChild("/guilds/druid/primal-rites/ritual-attunement.c",
        "/guilds/druid/primal-rites/root.c");
    addChild("/guilds/druid/primal-rites/rite-of-vigor.c",
        "/guilds/druid/primal-rites/rite-of-mending.c");
    addChild("/guilds/druid/primal-rites/rite-of-clarity.c",
        "/guilds/druid/primal-rites/rite-of-mending.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/primal-rites/herbal-mastery.c");
    addResearchElement("/guilds/druid/primal-rites/rite-of-warding.c");

    addChild("/guilds/druid/primal-rites/herbal-mastery.c",
        "/guilds/druid/primal-rites/ritual-attunement.c");
    addChild("/guilds/druid/primal-rites/rite-of-warding.c",
        "/guilds/druid/primal-rites/rite-of-mending.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/druid/primal-rites/rite-of-swiftness.c");
    addResearchElement(
        "/guilds/druid/primal-rites/rite-of-purification.c");

    addChild("/guilds/druid/primal-rites/rite-of-swiftness.c",
        "/guilds/druid/primal-rites/rite-of-mending.c");
    addChild("/guilds/druid/primal-rites/rite-of-purification.c",
        "/guilds/druid/primal-rites/rite-of-clarity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/druid/primal-rites/primal-channeling.c");
    addResearchElement("/guilds/druid/primal-rites/greater-mending.c");

    addChild("/guilds/druid/primal-rites/primal-channeling.c",
        "/guilds/druid/primal-rites/herbal-mastery.c");
    addChild("/guilds/druid/primal-rites/greater-mending.c",
        "/guilds/druid/primal-rites/rite-of-mending.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/druid/primal-rites/rite-of-endurance.c");
    addResearchElement("/guilds/druid/primal-rites/greater-clarity.c");
    addResearchElement("/guilds/druid/primal-rites/rite-of-wrath.c");

    addChild("/guilds/druid/primal-rites/rite-of-endurance.c",
        "/guilds/druid/primal-rites/rite-of-vigor.c");
    addChild("/guilds/druid/primal-rites/greater-clarity.c",
        "/guilds/druid/primal-rites/rite-of-clarity.c");
    addChild("/guilds/druid/primal-rites/rite-of-wrath.c",
        "/guilds/druid/primal-rites/rite-of-warding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/druid/primal-rites/rite-of-the-hunt.c");

    addChild("/guilds/druid/primal-rites/rite-of-the-hunt.c",
        "/guilds/druid/primal-rites/rite-of-swiftness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/druid/primal-rites/ancient-lore.c");
    addResearchElement("/guilds/druid/primal-rites/rite-of-renewal.c");

    addChild("/guilds/druid/primal-rites/ancient-lore.c",
        "/guilds/druid/primal-rites/primal-channeling.c");
    addChild("/guilds/druid/primal-rites/rite-of-renewal.c",
        "/guilds/druid/primal-rites/rite-of-clarity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/primal-rites/greater-warding.c");

    addChild("/guilds/druid/primal-rites/greater-warding.c",
        "/guilds/druid/primal-rites/rite-of-warding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/druid/primal-rites/primal-healing.c");
    addResearchElement(
        "/guilds/druid/primal-rites/rite-of-the-storm.c");

    addChild("/guilds/druid/primal-rites/primal-healing.c",
        "/guilds/druid/primal-rites/greater-mending.c");
    addChild("/guilds/druid/primal-rites/rite-of-the-storm.c",
        "/guilds/druid/primal-rites/rite-of-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/druid/primal-rites/rite-mastery.c");
    addResearchElement(
        "/guilds/druid/primal-rites/greater-swiftness.c");

    addChild("/guilds/druid/primal-rites/rite-mastery.c",
        "/guilds/druid/primal-rites/ancient-lore.c");
    addChild("/guilds/druid/primal-rites/greater-swiftness.c",
        "/guilds/druid/primal-rites/rite-of-swiftness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/druid/primal-rites/wild-vitality.c");
    addResearchElement("/guilds/druid/primal-rites/primal-attunement.c");

    addChild("/guilds/druid/primal-rites/wild-vitality.c",
        "/guilds/druid/primal-rites/rite-of-endurance.c");
    addChild("/guilds/druid/primal-rites/primal-attunement.c",
        "/guilds/druid/primal-rites/greater-clarity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/primal-rites/greater-hunt.c");

    addChild("/guilds/druid/primal-rites/greater-hunt.c",
        "/guilds/druid/primal-rites/rite-of-the-hunt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/druid/primal-rites/grand-renewal.c");

    addChild("/guilds/druid/primal-rites/grand-renewal.c",
        "/guilds/druid/primal-rites/rite-of-renewal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/druid/primal-rites/elder-rites.c");
    addResearchElement(
        "/guilds/druid/primal-rites/rite-of-the-wilds.c");
    addResearchElement(
        "/guilds/druid/primal-rites/ancient-blessing.c");

    addChild("/guilds/druid/primal-rites/elder-rites.c",
        "/guilds/druid/primal-rites/rite-mastery.c");
    addChild("/guilds/druid/primal-rites/rite-of-the-wilds.c",
        "/guilds/druid/primal-rites/rite-of-the-storm.c");
    addChild("/guilds/druid/primal-rites/ancient-blessing.c",
        "/guilds/druid/primal-rites/greater-hunt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/druid/primal-rites/natures-bounty.c");

    addChild("/guilds/druid/primal-rites/natures-bounty.c",
        "/guilds/druid/primal-rites/primal-healing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/druid/primal-rites/wild-restoration.c");

    addChild("/guilds/druid/primal-rites/wild-restoration.c",
        "/guilds/druid/primal-rites/grand-renewal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(
        "/guilds/druid/primal-rites/primal-rite-mastery.c");
    addResearchElement(
        "/guilds/druid/primal-rites/blessing-of-the-wild.c");

    addChild("/guilds/druid/primal-rites/primal-rite-mastery.c",
        "/guilds/druid/primal-rites/elder-rites.c");
    addChild("/guilds/druid/primal-rites/blessing-of-the-wild.c",
        "/guilds/druid/primal-rites/ancient-blessing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/druid/primal-rites/natures-rite.c");

    addChild("/guilds/druid/primal-rites/natures-rite.c",
        "/guilds/druid/primal-rites/rite-of-the-wilds.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/primal-rites/ancient-ritualist.c");
    addResearchElement(
        "/guilds/druid/primal-rites/ritual-mastery.c");

    addChild("/guilds/druid/primal-rites/ancient-ritualist.c",
        "/guilds/druid/primal-rites/primal-rite-mastery.c");
    addChild("/guilds/druid/primal-rites/ritual-mastery.c",
        "/guilds/druid/primal-rites/primal-rite-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/druid/primal-rites/primal-voice.c");
    addResearchElement(
        "/guilds/druid/primal-rites/elder-herbalist.c");

    addChild("/guilds/druid/primal-rites/primal-voice.c",
        "/guilds/druid/primal-rites/ancient-ritualist.c");
    addChild("/guilds/druid/primal-rites/elder-herbalist.c",
        "/guilds/druid/primal-rites/ritual-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/druid/primal-rites/nature-speaker.c");
    addResearchElement(
        "/guilds/druid/primal-rites/voice-of-the-wild.c");

    addChild("/guilds/druid/primal-rites/nature-speaker.c",
        "/guilds/druid/primal-rites/primal-voice.c");
    addChild("/guilds/druid/primal-rites/voice-of-the-wild.c",
        "/guilds/druid/primal-rites/elder-herbalist.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/druid/primal-rites/primal-incarnate.c");

    addChild("/guilds/druid/primal-rites/primal-incarnate.c",
        "/guilds/druid/primal-rites/voice-of-the-wild.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Primal Rites");
    Description("This research tree grants the druid mastery over "
        "the ancient art of ritual - ceremonies performed with "
        "consumed herbs that channel nature's power to restore "
        "vitality, invoke lasting blessings, and unleash wrath "
        "upon enemies.");
    Source("druid");
    addResearchElement("/guilds/druid/primal-rites/root.c");
    TreeRoot("/guilds/druid/primal-rites/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtyNinthLevel();
}
