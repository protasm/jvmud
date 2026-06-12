//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/ferocity-i.c");
    addResearchElement("/guilds/druid/companions/wolf/resilience-i.c");
    addResearchElement("/guilds/druid/companions/wolf/agility-i.c");

    addChild("/guilds/druid/companions/wolf/ferocity-i.c",
        "/guilds/druid/companions/wolf/root.c");
    addChild("/guilds/druid/companions/wolf/resilience-i.c",
        "/guilds/druid/companions/wolf/root.c");
    addChild("/guilds/druid/companions/wolf/agility-i.c",
        "/guilds/druid/companions/wolf/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/pack-instinct-i.c");
    addResearchElement("/guilds/druid/companions/wolf/thick-hide-i.c");
    addResearchElement("/guilds/druid/companions/wolf/keen-nose.c");

    addChild("/guilds/druid/companions/wolf/pack-instinct-i.c",
        "/guilds/druid/companions/wolf/root.c");
    addChild("/guilds/druid/companions/wolf/thick-hide-i.c",
        "/guilds/druid/companions/wolf/root.c");
    addChild("/guilds/druid/companions/wolf/keen-nose.c",
        "/guilds/druid/companions/wolf/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/ferocity-ii.c");
    addResearchElement("/guilds/druid/companions/wolf/resilience-ii.c");
    addResearchElement("/guilds/druid/companions/wolf/agility-ii.c");

    addChild("/guilds/druid/companions/wolf/ferocity-ii.c",
        "/guilds/druid/companions/wolf/ferocity-i.c");
    addChild("/guilds/druid/companions/wolf/resilience-ii.c",
        "/guilds/druid/companions/wolf/resilience-i.c");
    addChild("/guilds/druid/companions/wolf/agility-ii.c",
        "/guilds/druid/companions/wolf/agility-i.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/pack-instinct-ii.c");
    addResearchElement("/guilds/druid/companions/wolf/thick-hide-ii.c");
    addResearchElement("/guilds/druid/companions/wolf/savage-bite.c");

    addChild("/guilds/druid/companions/wolf/pack-instinct-ii.c",
        "/guilds/druid/companions/wolf/pack-instinct-i.c");
    addChild("/guilds/druid/companions/wolf/thick-hide-ii.c",
        "/guilds/druid/companions/wolf/thick-hide-i.c");
    addChild("/guilds/druid/companions/wolf/savage-bite.c",
        "/guilds/druid/companions/wolf/keen-nose.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/ferocity-iii.c");
    addResearchElement("/guilds/druid/companions/wolf/resilience-iii.c");
    addResearchElement("/guilds/druid/companions/wolf/agility-iii.c");

    addChild("/guilds/druid/companions/wolf/ferocity-iii.c",
        "/guilds/druid/companions/wolf/ferocity-ii.c");
    addChild("/guilds/druid/companions/wolf/resilience-iii.c",
        "/guilds/druid/companions/wolf/resilience-ii.c");
    addChild("/guilds/druid/companions/wolf/agility-iii.c",
        "/guilds/druid/companions/wolf/agility-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/pack-instinct-iii.c");
    addResearchElement("/guilds/druid/companions/wolf/thick-hide-iii.c");
    addResearchElement("/guilds/druid/companions/wolf/primal-vigor.c");

    addChild("/guilds/druid/companions/wolf/pack-instinct-iii.c",
        "/guilds/druid/companions/wolf/pack-instinct-ii.c");
    addChild("/guilds/druid/companions/wolf/thick-hide-iii.c",
        "/guilds/druid/companions/wolf/thick-hide-ii.c");
    addChild("/guilds/druid/companions/wolf/primal-vigor.c",
        "/guilds/druid/companions/wolf/savage-bite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/alpha-presence.c");
    addResearchElement("/guilds/druid/companions/wolf/predators-endurance.c");
    addResearchElement("/guilds/druid/companions/wolf/lightning-reflexes.c");

    addChild("/guilds/druid/companions/wolf/alpha-presence.c",
        "/guilds/druid/companions/wolf/ferocity-iii.c");
    addChild("/guilds/druid/companions/wolf/predators-endurance.c",
        "/guilds/druid/companions/wolf/resilience-iii.c");
    addChild("/guilds/druid/companions/wolf/lightning-reflexes.c",
        "/guilds/druid/companions/wolf/agility-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/iron-jaw.c");
    addResearchElement("/guilds/druid/companions/wolf/pack-instinct-iv.c");
    addResearchElement("/guilds/druid/companions/wolf/natures-fury.c");

    addChild("/guilds/druid/companions/wolf/iron-jaw.c",
        "/guilds/druid/companions/wolf/alpha-presence.c");
    addChild("/guilds/druid/companions/wolf/pack-instinct-iv.c",
        "/guilds/druid/companions/wolf/pack-instinct-iii.c");
    addChild("/guilds/druid/companions/wolf/natures-fury.c",
        "/guilds/druid/companions/wolf/primal-vigor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/ferocity-iv.c");
    addResearchElement("/guilds/druid/companions/wolf/resilience-iv.c");
    addResearchElement("/guilds/druid/companions/wolf/agility-iv.c");

    addChild("/guilds/druid/companions/wolf/ferocity-iv.c",
        "/guilds/druid/companions/wolf/iron-jaw.c");
    addChild("/guilds/druid/companions/wolf/resilience-iv.c",
        "/guilds/druid/companions/wolf/predators-endurance.c");
    addChild("/guilds/druid/companions/wolf/agility-iv.c",
        "/guilds/druid/companions/wolf/lightning-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/thick-hide-iv.c");
    addResearchElement("/guilds/druid/companions/wolf/undying-loyalty.c");

    addChild("/guilds/druid/companions/wolf/thick-hide-iv.c",
        "/guilds/druid/companions/wolf/pack-instinct-iv.c");
    addChild("/guilds/druid/companions/wolf/undying-loyalty.c",
        "/guilds/druid/companions/wolf/resilience-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/ferocity-v.c");
    addResearchElement("/guilds/druid/companions/wolf/agility-v.c");
    addResearchElement("/guilds/druid/companions/wolf/savage-howl.c");

    addChild("/guilds/druid/companions/wolf/ferocity-v.c",
        "/guilds/druid/companions/wolf/ferocity-iv.c");
    addChild("/guilds/druid/companions/wolf/agility-v.c",
        "/guilds/druid/companions/wolf/agility-iv.c");
    addChild("/guilds/druid/companions/wolf/savage-howl.c",
        "/guilds/druid/companions/wolf/natures-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/pack-instinct-v.c");
    addResearchElement("/guilds/druid/companions/wolf/primal-resilience.c");

    addChild("/guilds/druid/companions/wolf/pack-instinct-v.c",
        "/guilds/druid/companions/wolf/thick-hide-iv.c");
    addChild("/guilds/druid/companions/wolf/primal-resilience.c",
        "/guilds/druid/companions/wolf/undying-loyalty.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/ferocity-vi.c");
    addResearchElement("/guilds/druid/companions/wolf/resilience-v.c");
    addResearchElement("/guilds/druid/companions/wolf/thick-hide-v.c");

    addChild("/guilds/druid/companions/wolf/ferocity-vi.c",
        "/guilds/druid/companions/wolf/ferocity-v.c");
    addChild("/guilds/druid/companions/wolf/resilience-v.c",
        "/guilds/druid/companions/wolf/primal-resilience.c");
    addChild("/guilds/druid/companions/wolf/thick-hide-v.c",
        "/guilds/druid/companions/wolf/pack-instinct-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/apex-predator.c");
    addResearchElement("/guilds/druid/companions/wolf/predatory-instinct.c");
    addResearchElement("/guilds/druid/companions/wolf/indomitable-will.c");

    addChild("/guilds/druid/companions/wolf/apex-predator.c",
        "/guilds/druid/companions/wolf/ferocity-vi.c");
    addChild("/guilds/druid/companions/wolf/predatory-instinct.c",
        "/guilds/druid/companions/wolf/savage-howl.c");
    addChild("/guilds/druid/companions/wolf/indomitable-will.c",
        "/guilds/druid/companions/wolf/resilience-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/primal-fury.c");
    addResearchElement("/guilds/druid/companions/wolf/spirit-ward.c");
    addResearchElement("/guilds/druid/companions/wolf/relentless-hunter.c");

    addChild("/guilds/druid/companions/wolf/primal-fury.c",
        "/guilds/druid/companions/wolf/apex-predator.c");
    addChild("/guilds/druid/companions/wolf/spirit-ward.c",
        "/guilds/druid/companions/wolf/indomitable-will.c");
    addChild("/guilds/druid/companions/wolf/relentless-hunter.c",
        "/guilds/druid/companions/wolf/predatory-instinct.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/wrath-of-the-pack.c");
    addResearchElement("/guilds/druid/companions/wolf/eternal-guardian.c");

    addChild("/guilds/druid/companions/wolf/wrath-of-the-pack.c",
        "/guilds/druid/companions/wolf/primal-fury.c");
    addChild("/guilds/druid/companions/wolf/eternal-guardian.c",
        "/guilds/druid/companions/wolf/spirit-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/druid/companions/wolf/spirit-of-the-wild.c");

    addChild("/guilds/druid/companions/wolf/spirit-of-the-wild.c",
        "/guilds/druid/companions/wolf/wrath-of-the-pack.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Pack of the Wolf");
    Description("This research tree deepens the druid's bond with their "
        "wolf companion, strengthening it through primal magic and shared "
        "experience. As the druid progresses, the wolf grows from a "
        "capable hunter into an avatar of the untamed wild.");
    Source("druid");
    addResearchElement("/guilds/druid/companions/wolf/root.c");
    TreeRoot("/guilds/druid/companions/wolf/root.c");

    FifthLevel();
    SeventhLevel();
    EleventhLevel();
    ThirteenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtySeventhLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    FiftyNinthLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
    SeventiethLevel();
}
