//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/mantle-of-authority.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/sovereigns-resolve.c");

    addChild("/guilds/phaedra/crowns-mandate/mantle-of-authority.c",
        "/guilds/phaedra/crowns-mandate/crowns-mandate-root.c");
    addChild("/guilds/phaedra/crowns-mandate/sovereigns-resolve.c",
        "/guilds/phaedra/crowns-mandate/crowns-mandate-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/phaedras-aegis.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/unshakeable-command.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/enduring-pillar.c");

    addChild("/guilds/phaedra/crowns-mandate/phaedras-aegis.c",
        "/guilds/phaedra/crowns-mandate/mantle-of-authority.c");
    addChild("/guilds/phaedra/crowns-mandate/unshakeable-command.c",
        "/guilds/phaedra/crowns-mandate/mantle-of-authority.c");
    addChild("/guilds/phaedra/crowns-mandate/enduring-pillar.c",
        "/guilds/phaedra/crowns-mandate/sovereigns-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/radiant-authority.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/stalwart-commander.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/oathkeepers-strength.c");

    addChild("/guilds/phaedra/crowns-mandate/radiant-authority.c",
        "/guilds/phaedra/crowns-mandate/phaedras-aegis.c");
    addChild("/guilds/phaedra/crowns-mandate/stalwart-commander.c",
        "/guilds/phaedra/crowns-mandate/unshakeable-command.c");
    addChild("/guilds/phaedra/crowns-mandate/oathkeepers-strength.c",
        "/guilds/phaedra/crowns-mandate/enduring-pillar.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/mandates-strike.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/voice-of-the-crown.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/iron-discipline.c");

    addChild("/guilds/phaedra/crowns-mandate/mandates-strike.c",
        "/guilds/phaedra/crowns-mandate/radiant-authority.c");
    addChild("/guilds/phaedra/crowns-mandate/voice-of-the-crown.c",
        "/guilds/phaedra/crowns-mandate/stalwart-commander.c");
    addChild("/guilds/phaedra/crowns-mandate/iron-discipline.c",
        "/guilds/phaedra/crowns-mandate/stalwart-commander.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/sovereigns-blade.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/wardens-fortitude.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/aura-of-authority.c");

    addChild("/guilds/phaedra/crowns-mandate/sovereigns-blade.c",
        "/guilds/phaedra/crowns-mandate/mandates-strike.c");
    addChild("/guilds/phaedra/crowns-mandate/wardens-fortitude.c",
        "/guilds/phaedra/crowns-mandate/oathkeepers-strength.c");
    addChild("/guilds/phaedra/crowns-mandate/aura-of-authority.c",
        "/guilds/phaedra/crowns-mandate/voice-of-the-crown.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/crowns-light.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/champions-vigil.c");

    addChild("/guilds/phaedra/crowns-mandate/crowns-light.c",
        "/guilds/phaedra/crowns-mandate/mandates-strike.c");
    addChild("/guilds/phaedra/crowns-mandate/champions-vigil.c",
        "/guilds/phaedra/crowns-mandate/sovereigns-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/immovable-will.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/undying-flame.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/high-companions-bearing.c");

    addChild("/guilds/phaedra/crowns-mandate/immovable-will.c",
        "/guilds/phaedra/crowns-mandate/wardens-fortitude.c");
    addChild("/guilds/phaedra/crowns-mandate/undying-flame.c",
        "/guilds/phaedra/crowns-mandate/crowns-light.c");
    addChild("/guilds/phaedra/crowns-mandate/high-companions-bearing.c",
        "/guilds/phaedra/crowns-mandate/aura-of-authority.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/masterful-blade.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/inviolable-shield.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/wardens-sight.c");

    addChild("/guilds/phaedra/crowns-mandate/masterful-blade.c",
        "/guilds/phaedra/crowns-mandate/undying-flame.c");
    addChild("/guilds/phaedra/crowns-mandate/inviolable-shield.c",
        "/guilds/phaedra/crowns-mandate/immovable-will.c");
    addChild("/guilds/phaedra/crowns-mandate/wardens-sight.c",
        "/guilds/phaedra/crowns-mandate/champions-vigil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/sovereign-smite.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/pillar-of-faith.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/indomitable-resolve.c");

    addChild("/guilds/phaedra/crowns-mandate/sovereign-smite.c",
        "/guilds/phaedra/crowns-mandate/masterful-blade.c");
    addChild("/guilds/phaedra/crowns-mandate/pillar-of-faith.c",
        "/guilds/phaedra/crowns-mandate/wardens-sight.c");
    addChild("/guilds/phaedra/crowns-mandate/indomitable-resolve.c",
        "/guilds/phaedra/crowns-mandate/iron-discipline.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/bulwark-of-the-crown.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/wardenship-supreme.c");

    addChild("/guilds/phaedra/crowns-mandate/bulwark-of-the-crown.c",
        "/guilds/phaedra/crowns-mandate/inviolable-shield.c");
    addChild("/guilds/phaedra/crowns-mandate/wardenship-supreme.c",
        "/guilds/phaedra/crowns-mandate/wardens-sight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/ordained-purpose.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/living-legend.c");

    addChild("/guilds/phaedra/crowns-mandate/ordained-purpose.c",
        "/guilds/phaedra/crowns-mandate/sovereign-smite.c");
    addChild("/guilds/phaedra/crowns-mandate/living-legend.c",
        "/guilds/phaedra/crowns-mandate/pillar-of-faith.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/eternal-covenant.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/crowns-champion.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/absolute-conviction.c");

    addChild("/guilds/phaedra/crowns-mandate/eternal-covenant.c",
        "/guilds/phaedra/crowns-mandate/bulwark-of-the-crown.c");
    addChild("/guilds/phaedra/crowns-mandate/crowns-champion.c",
        "/guilds/phaedra/crowns-mandate/ordained-purpose.c");
    addChild("/guilds/phaedra/crowns-mandate/absolute-conviction.c",
        "/guilds/phaedra/crowns-mandate/indomitable-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/transcendent-guard.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/realms-guardian.c");

    addChild("/guilds/phaedra/crowns-mandate/transcendent-guard.c",
        "/guilds/phaedra/crowns-mandate/eternal-covenant.c");
    addChild("/guilds/phaedra/crowns-mandate/realms-guardian.c",
        "/guilds/phaedra/crowns-mandate/living-legend.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/radiance-of-phaedra.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/undying-covenant.c");

    addChild("/guilds/phaedra/crowns-mandate/radiance-of-phaedra.c",
        "/guilds/phaedra/crowns-mandate/crowns-champion.c");
    addChild("/guilds/phaedra/crowns-mandate/undying-covenant.c",
        "/guilds/phaedra/crowns-mandate/eternal-covenant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/first-companions-mastery.c");

    addChild("/guilds/phaedra/crowns-mandate/first-companions-mastery.c",
        "/guilds/phaedra/crowns-mandate/crowns-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/phaedras-legacy.c");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/eternal-mandate.c");

    addChild("/guilds/phaedra/crowns-mandate/phaedras-legacy.c",
        "/guilds/phaedra/crowns-mandate/radiance-of-phaedra.c");
    addChild("/guilds/phaedra/crowns-mandate/eternal-mandate.c",
        "/guilds/phaedra/crowns-mandate/first-companions-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/mandate-supreme.c");

    addChild("/guilds/phaedra/crowns-mandate/mandate-supreme.c",
        "/guilds/phaedra/crowns-mandate/eternal-mandate.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/sovereigns-will.c");

    addChild("/guilds/phaedra/crowns-mandate/sovereigns-will.c",
        "/guilds/phaedra/crowns-mandate/mandate-supreme.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/indestructible-guard.c");

    addChild("/guilds/phaedra/crowns-mandate/indestructible-guard.c",
        "/guilds/phaedra/crowns-mandate/sovereigns-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/avatar-of-phaedra.c");

    addChild("/guilds/phaedra/crowns-mandate/avatar-of-phaedra.c",
        "/guilds/phaedra/crowns-mandate/indestructible-guard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/sovereigns-perfection.c");

    addChild("/guilds/phaedra/crowns-mandate/sovereigns-perfection.c",
        "/guilds/phaedra/crowns-mandate/avatar-of-phaedra.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/immortal-purpose.c");

    addChild("/guilds/phaedra/crowns-mandate/immortal-purpose.c",
        "/guilds/phaedra/crowns-mandate/sovereigns-perfection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyFirstLevel()
{
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/crowns-mandate-eternal.c");

    addChild("/guilds/phaedra/crowns-mandate/crowns-mandate-eternal.c",
        "/guilds/phaedra/crowns-mandate/immortal-purpose.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Crown's Mandate");
    Description("The highest teaching of the Order of Phaedra: "
        "to unite all four pillars - Courage, Fidelity, "
        "Stewardship, and Justice - under the sovereign "
        "authority of the Crown, and to stand as the "
        "realm's eternal guardian.");
    Source("Order of Phaedra");
    addResearchElement(
        "/guilds/phaedra/crowns-mandate/crowns-mandate-root.c");
    TreeRoot("/guilds/phaedra/crowns-mandate/crowns-mandate-root.c");

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
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyFifthLevel();
    FortyFirstLevel();
    FortySeventhLevel();
    FiftyFifthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
    SeventyFirstLevel();
}
