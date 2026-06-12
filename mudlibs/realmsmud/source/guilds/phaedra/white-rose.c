//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/roses-vigor.c");
    addResearchElement("/guilds/phaedra/white-rose/banner-of-phaedra.c");

    addChild("/guilds/phaedra/white-rose/roses-vigor.c",
        "/guilds/phaedra/white-rose/white-rose-root.c");
    addChild("/guilds/phaedra/white-rose/banner-of-phaedra.c",
        "/guilds/phaedra/white-rose/white-rose-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/rose-blooded.c");
    addResearchElement("/guilds/phaedra/white-rose/white-rose-banner.c");
    addResearchElement("/guilds/phaedra/white-rose/petals-of-valor.c");

    addChild("/guilds/phaedra/white-rose/rose-blooded.c",
        "/guilds/phaedra/white-rose/roses-vigor.c");
    addChild("/guilds/phaedra/white-rose/white-rose-banner.c",
        "/guilds/phaedra/white-rose/banner-of-phaedra.c");
    addChild("/guilds/phaedra/white-rose/petals-of-valor.c",
        "/guilds/phaedra/white-rose/banner-of-phaedra.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/steadfast-heart.c");
    addResearchElement("/guilds/phaedra/white-rose/thorned-defense.c");
    addResearchElement("/guilds/phaedra/white-rose/orders-discipline.c");
    addResearchElement("/guilds/phaedra/white-rose/roses-endurance.c");

    addChild("/guilds/phaedra/white-rose/steadfast-heart.c",
        "/guilds/phaedra/white-rose/rose-blooded.c");
    addChild("/guilds/phaedra/white-rose/thorned-defense.c",
        "/guilds/phaedra/white-rose/petals-of-valor.c");
    addChild("/guilds/phaedra/white-rose/orders-discipline.c",
        "/guilds/phaedra/white-rose/petals-of-valor.c");
    addChild("/guilds/phaedra/white-rose/roses-endurance.c",
        "/guilds/phaedra/white-rose/rose-blooded.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/oath-renewed.c");
    addResearchElement("/guilds/phaedra/white-rose/sworn-shield.c");
    addResearchElement("/guilds/phaedra/white-rose/grace-of-service.c");
    addResearchElement("/guilds/phaedra/white-rose/radiant-bearing.c");

    addChild("/guilds/phaedra/white-rose/oath-renewed.c",
        "/guilds/phaedra/white-rose/steadfast-heart.c");
    addChild("/guilds/phaedra/white-rose/sworn-shield.c",
        "/guilds/phaedra/white-rose/thorned-defense.c");
    addChild("/guilds/phaedra/white-rose/grace-of-service.c",
        "/guilds/phaedra/white-rose/orders-discipline.c");
    addChild("/guilds/phaedra/white-rose/radiant-bearing.c",
        "/guilds/phaedra/white-rose/roses-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/companions-mark.c");
    addResearchElement("/guilds/phaedra/white-rose/iron-petals.c");
    addResearchElement("/guilds/phaedra/white-rose/sword-of-the-order.c");

    addChild("/guilds/phaedra/white-rose/companions-mark.c",
        "/guilds/phaedra/white-rose/oath-renewed.c");
    addChild("/guilds/phaedra/white-rose/iron-petals.c",
        "/guilds/phaedra/white-rose/sworn-shield.c");
    addChild("/guilds/phaedra/white-rose/sword-of-the-order.c",
        "/guilds/phaedra/white-rose/radiant-bearing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/roses-healing.c");
    addResearchElement("/guilds/phaedra/white-rose/roses-aegis.c");
    addResearchElement("/guilds/phaedra/white-rose/light-step.c");
    addResearchElement("/guilds/phaedra/white-rose/roses-wrath.c");

    addChild("/guilds/phaedra/white-rose/roses-healing.c",
        "/guilds/phaedra/white-rose/companions-mark.c");
    addChild("/guilds/phaedra/white-rose/roses-aegis.c",
        "/guilds/phaedra/white-rose/iron-petals.c");
    addChild("/guilds/phaedra/white-rose/light-step.c",
        "/guilds/phaedra/white-rose/sword-of-the-order.c");
    addChild("/guilds/phaedra/white-rose/roses-wrath.c",
        "/guilds/phaedra/white-rose/white-rose-banner.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/unbowed-spirit.c");
    addResearchElement("/guilds/phaedra/white-rose/honored-blade.c");
    addResearchElement("/guilds/phaedra/white-rose/white-rose-attunement.c");
    addResearchElement("/guilds/phaedra/white-rose/light-of-the-oath.c");

    addChild("/guilds/phaedra/white-rose/unbowed-spirit.c",
        "/guilds/phaedra/white-rose/roses-healing.c");
    addChild("/guilds/phaedra/white-rose/honored-blade.c",
        "/guilds/phaedra/white-rose/light-step.c");
    addChild("/guilds/phaedra/white-rose/white-rose-attunement.c",
        "/guilds/phaedra/white-rose/radiant-bearing.c");
    addChild("/guilds/phaedra/white-rose/light-of-the-oath.c",
        "/guilds/phaedra/white-rose/roses-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/orders-legacy.c");
    addResearchElement("/guilds/phaedra/white-rose/roses-mastery.c");
    addResearchElement("/guilds/phaedra/white-rose/blazing-conviction.c");

    addChild("/guilds/phaedra/white-rose/orders-legacy.c",
        "/guilds/phaedra/white-rose/unbowed-spirit.c");
    addChild("/guilds/phaedra/white-rose/roses-mastery.c",
        "/guilds/phaedra/white-rose/honored-blade.c");
    addChild("/guilds/phaedra/white-rose/blazing-conviction.c",
        "/guilds/phaedra/white-rose/light-of-the-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/good-smite.c");
    addResearchElement("/guilds/phaedra/white-rose/thorn-and-bloom.c");
    addResearchElement("/guilds/phaedra/white-rose/phaedras-light.c");

    addChild("/guilds/phaedra/white-rose/good-smite.c",
        "/guilds/phaedra/white-rose/white-rose-attunement.c");
    addChild("/guilds/phaedra/white-rose/thorn-and-bloom.c",
        "/guilds/phaedra/white-rose/roses-mastery.c");
    addChild("/guilds/phaedra/white-rose/phaedras-light.c",
        "/guilds/phaedra/white-rose/blazing-conviction.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/eternal-vow.c");
    addResearchElement("/guilds/phaedra/white-rose/petal-strike.c");
    addResearchElement("/guilds/phaedra/white-rose/phaedras-blessing.c");

    addChild("/guilds/phaedra/white-rose/eternal-vow.c",
        "/guilds/phaedra/white-rose/orders-legacy.c");
    addChild("/guilds/phaedra/white-rose/petal-strike.c",
        "/guilds/phaedra/white-rose/thorn-and-bloom.c");
    addChild("/guilds/phaedra/white-rose/phaedras-blessing.c",
        "/guilds/phaedra/white-rose/phaedras-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/enduring-rose.c");
    addResearchElement("/guilds/phaedra/white-rose/white-rose-judgment.c");

    addChild("/guilds/phaedra/white-rose/enduring-rose.c",
        "/guilds/phaedra/white-rose/eternal-vow.c");
    addChild("/guilds/phaedra/white-rose/white-rose-judgment.c",
        "/guilds/phaedra/white-rose/phaedras-blessing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/pinnacle-of-devotion.c");

    addChild("/guilds/phaedra/white-rose/pinnacle-of-devotion.c",
        "/guilds/phaedra/white-rose/enduring-rose.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/champions-radiance.c");

    addChild("/guilds/phaedra/white-rose/champions-radiance.c",
        "/guilds/phaedra/white-rose/pinnacle-of-devotion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/rose-eternal.c");

    addChild("/guilds/phaedra/white-rose/rose-eternal.c",
        "/guilds/phaedra/white-rose/white-rose-judgment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/white-rose-resilience.c");

    addChild("/guilds/phaedra/white-rose/white-rose-resilience.c",
        "/guilds/phaedra/white-rose/champions-radiance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/white-rose-supremacy.c");

    addChild("/guilds/phaedra/white-rose/white-rose-supremacy.c",
        "/guilds/phaedra/white-rose/white-rose-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/roses-radiance.c");

    addChild("/guilds/phaedra/white-rose/roses-radiance.c",
        "/guilds/phaedra/white-rose/white-rose-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/petal-storm.c");

    addChild("/guilds/phaedra/white-rose/petal-storm.c",
        "/guilds/phaedra/white-rose/roses-radiance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/thornwall.c");

    addChild("/guilds/phaedra/white-rose/thornwall.c",
        "/guilds/phaedra/white-rose/petal-storm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/roses-smite.c");

    addChild("/guilds/phaedra/white-rose/roses-smite.c",
        "/guilds/phaedra/white-rose/thornwall.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/eternal-rose-high.c");

    addChild("/guilds/phaedra/white-rose/eternal-rose-high.c",
        "/guilds/phaedra/white-rose/roses-smite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/rose-champion.c");

    addChild("/guilds/phaedra/white-rose/rose-champion.c",
        "/guilds/phaedra/white-rose/eternal-rose-high.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/undying-rose.c");

    addChild("/guilds/phaedra/white-rose/undying-rose.c",
        "/guilds/phaedra/white-rose/rose-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/crown-of-thorns.c");

    addChild("/guilds/phaedra/white-rose/crown-of-thorns.c",
        "/guilds/phaedra/white-rose/undying-rose.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyFirstLevel()
{
    addResearchElement("/guilds/phaedra/white-rose/rose-eternal-first.c");
    addResearchElement("/guilds/phaedra/white-rose/white-rose-ascendant.c");

    addChild("/guilds/phaedra/white-rose/rose-eternal-first.c",
        "/guilds/phaedra/white-rose/crown-of-thorns.c");
    addChild("/guilds/phaedra/white-rose/white-rose-ascendant.c",
        "/guilds/phaedra/white-rose/rose-eternal-first.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("White Rose");
    Description("The rank-gated path of the White Rose companion: those "
        "who have proven themselves worthy of bearing Phaedra's emblem "
        "may unlock deeper knowledge of the Order's most sacred teachings, "
        "combining martial excellence with radiant light.");
    Source("Order of Phaedra");
    addResearchElement("/guilds/phaedra/white-rose/white-rose-root.c");
    TreeRoot("/guilds/phaedra/white-rose/white-rose-root.c");

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
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyFifthLevel();
    ThirtyNinthLevel();
    FortyFifthLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
    SeventyFirstLevel();
}
