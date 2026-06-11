//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/phaedra/courage/steadfast-heart.c");
    addResearchElement("/guilds/phaedra/courage/unyielding-will.c");
    addResearchElement("/guilds/phaedra/courage/rallying-strike.c");

    addChild("/guilds/phaedra/courage/steadfast-heart.c",
        "/guilds/phaedra/courage/courage-root.c");
    addChild("/guilds/phaedra/courage/unyielding-will.c",
        "/guilds/phaedra/courage/courage-root.c");
    addChild("/guilds/phaedra/courage/rallying-strike.c",
        "/guilds/phaedra/courage/courage-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/phaedra/courage/resolve-of-the-founders.c");
    addResearchElement("/guilds/phaedra/courage/stalwart-training.c");
    addResearchElement("/guilds/phaedra/courage/shield-discipline.c");

    addChild("/guilds/phaedra/courage/resolve-of-the-founders.c",
        "/guilds/phaedra/courage/steadfast-heart.c");
    addChild("/guilds/phaedra/courage/stalwart-training.c",
        "/guilds/phaedra/courage/rallying-strike.c");
    addChild("/guilds/phaedra/courage/shield-discipline.c",
        "/guilds/phaedra/courage/unyielding-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/phaedra/courage/fearless-charge.c");
    addResearchElement("/guilds/phaedra/courage/parrying-mastery.c");
    addResearchElement("/guilds/phaedra/courage/sworn-endurance.c");
    addResearchElement("/guilds/phaedra/courage/fighting-stance.c");

    addChild("/guilds/phaedra/courage/fearless-charge.c",
        "/guilds/phaedra/courage/stalwart-training.c");
    addChild("/guilds/phaedra/courage/parrying-mastery.c",
        "/guilds/phaedra/courage/shield-discipline.c");
    addChild("/guilds/phaedra/courage/sworn-endurance.c",
        "/guilds/phaedra/courage/resolve-of-the-founders.c");
    addChild("/guilds/phaedra/courage/fighting-stance.c",
        "/guilds/phaedra/courage/parrying-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/phaedra/courage/light-of-the-rose.c");
    addResearchElement("/guilds/phaedra/courage/combat-readiness.c");
    addResearchElement("/guilds/phaedra/courage/oath-hardened-body.c");

    addChild("/guilds/phaedra/courage/light-of-the-rose.c",
        "/guilds/phaedra/courage/fearless-charge.c");
    addChild("/guilds/phaedra/courage/combat-readiness.c",
        "/guilds/phaedra/courage/stalwart-training.c");
    addChild("/guilds/phaedra/courage/oath-hardened-body.c",
        "/guilds/phaedra/courage/sworn-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/phaedra/courage/unshaken-presence.c");
    addResearchElement("/guilds/phaedra/courage/radiant-attunement.c");
    addResearchElement("/guilds/phaedra/courage/armored-discipline.c");

    addChild("/guilds/phaedra/courage/unshaken-presence.c",
        "/guilds/phaedra/courage/fighting-stance.c");
    addChild("/guilds/phaedra/courage/radiant-attunement.c",
        "/guilds/phaedra/courage/light-of-the-rose.c");
    addChild("/guilds/phaedra/courage/armored-discipline.c",
        "/guilds/phaedra/courage/oath-hardened-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/phaedra/courage/phaedras-judgment.c");
    addResearchElement("/guilds/phaedra/courage/bladesworn-precision.c");
    addResearchElement("/guilds/phaedra/courage/courageous-advance.c");

    addChild("/guilds/phaedra/courage/phaedras-judgment.c",
        "/guilds/phaedra/courage/light-of-the-rose.c");
    addChild("/guilds/phaedra/courage/bladesworn-precision.c",
        "/guilds/phaedra/courage/combat-readiness.c");
    addChild("/guilds/phaedra/courage/courageous-advance.c",
        "/guilds/phaedra/courage/unshaken-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/phaedra/courage/tempered-resolve.c");
    addResearchElement("/guilds/phaedra/courage/shield-wall-mastery.c");

    addChild("/guilds/phaedra/courage/tempered-resolve.c",
        "/guilds/phaedra/courage/sworn-endurance.c");
    addChild("/guilds/phaedra/courage/shield-wall-mastery.c",
        "/guilds/phaedra/courage/armored-discipline.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/phaedra/courage/oath-smite.c");
    addResearchElement("/guilds/phaedra/courage/valors-edge.c");

    addChild("/guilds/phaedra/courage/oath-smite.c",
        "/guilds/phaedra/courage/phaedras-judgment.c");
    addChild("/guilds/phaedra/courage/valors-edge.c",
        "/guilds/phaedra/courage/bladesworn-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/phaedra/courage/indomitable-spirit.c");
    addResearchElement("/guilds/phaedra/courage/shield-mastery.c");

    addChild("/guilds/phaedra/courage/indomitable-spirit.c",
        "/guilds/phaedra/courage/tempered-resolve.c");
    addChild("/guilds/phaedra/courage/shield-mastery.c",
        "/guilds/phaedra/courage/shield-wall-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/phaedra/courage/searing-radiance.c");

    addChild("/guilds/phaedra/courage/searing-radiance.c",
        "/guilds/phaedra/courage/oath-smite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/phaedra/courage/unbreakable-oath.c");
    addResearchElement("/guilds/phaedra/courage/courageous-leader.c");

    addChild("/guilds/phaedra/courage/unbreakable-oath.c",
        "/guilds/phaedra/courage/indomitable-spirit.c");
    addChild("/guilds/phaedra/courage/courageous-leader.c",
        "/guilds/phaedra/courage/courageous-advance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/phaedra/courage/radiant-might.c");
    addResearchElement("/guilds/phaedra/courage/iron-discipline.c");

    addChild("/guilds/phaedra/courage/radiant-might.c",
        "/guilds/phaedra/courage/searing-radiance.c");
    addChild("/guilds/phaedra/courage/iron-discipline.c",
        "/guilds/phaedra/courage/valors-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/phaedra/courage/blaze-of-valor.c");
    addResearchElement("/guilds/phaedra/courage/battle-hardened.c");

    addChild("/guilds/phaedra/courage/blaze-of-valor.c",
        "/guilds/phaedra/courage/searing-radiance.c");
    addChild("/guilds/phaedra/courage/battle-hardened.c",
        "/guilds/phaedra/courage/unbreakable-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/courage/champions-bearing.c");
    addResearchElement("/guilds/phaedra/courage/masters-resolve.c");
    addResearchElement("/guilds/phaedra/courage/blazing-resolve.c");

    addChild("/guilds/phaedra/courage/champions-bearing.c",
        "/guilds/phaedra/courage/courageous-leader.c");
    addChild("/guilds/phaedra/courage/masters-resolve.c",
        "/guilds/phaedra/courage/radiant-might.c");
    addChild("/guilds/phaedra/courage/blazing-resolve.c",
        "/guilds/phaedra/courage/champions-bearing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/courage/dawns-reckoning.c");

    addChild("/guilds/phaedra/courage/dawns-reckoning.c",
        "/guilds/phaedra/courage/blaze-of-valor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/phaedra/courage/legendary-fortitude.c");

    addChild("/guilds/phaedra/courage/legendary-fortitude.c",
        "/guilds/phaedra/courage/battle-hardened.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/phaedra/courage/eternal-courage.c");

    addChild("/guilds/phaedra/courage/eternal-courage.c",
        "/guilds/phaedra/courage/legendary-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/phaedra/courage/founders-resolve.c");

    addChild("/guilds/phaedra/courage/founders-resolve.c",
        "/guilds/phaedra/courage/eternal-courage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/phaedra/courage/dawns-judgment.c");

    addChild("/guilds/phaedra/courage/dawns-judgment.c",
        "/guilds/phaedra/courage/founders-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/courage/invincible-courage.c");

    addChild("/guilds/phaedra/courage/invincible-courage.c",
        "/guilds/phaedra/courage/dawns-judgment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/courage/strike-of-the-founders.c");

    addChild("/guilds/phaedra/courage/strike-of-the-founders.c",
        "/guilds/phaedra/courage/invincible-courage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/phaedra/courage/unyielding-flame.c");

    addChild("/guilds/phaedra/courage/unyielding-flame.c",
        "/guilds/phaedra/courage/strike-of-the-founders.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/courage/courage-eternal.c");

    addChild("/guilds/phaedra/courage/courage-eternal.c",
        "/guilds/phaedra/courage/unyielding-flame.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Courage");
    Description("The first pillar of the Order of Phaedra: the courage "
        "to stand firm, to act righteously, and to press forward "
        "where others falter.");
    Source("Order of Phaedra");
    addResearchElement("/guilds/phaedra/courage/courage-root.c");
    TreeRoot("/guilds/phaedra/courage/courage-root.c");

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
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtyNinthLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FiftyThirdLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
