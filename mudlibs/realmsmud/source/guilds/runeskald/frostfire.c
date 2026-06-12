//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/glacial-brand.c");
    addResearchElement("/guilds/runeskald/frostfire/runic-flame.c");
    addResearchElement("/guilds/runeskald/frostfire/frostfire-lore.c");
    addResearchElement("/guilds/runeskald/frostfire/ice-shard.c");
    addResearchElement("/guilds/runeskald/frostfire/ember-rune.c");

    addChild("/guilds/runeskald/frostfire/glacial-brand.c",
        "/guilds/runeskald/frostfire/root.c");
    addChild("/guilds/runeskald/frostfire/runic-flame.c",
        "/guilds/runeskald/frostfire/root.c");
    addChild("/guilds/runeskald/frostfire/frostfire-lore.c",
        "/guilds/runeskald/frostfire/root.c");
    addChild("/guilds/runeskald/frostfire/ice-shard.c",
        "/guilds/runeskald/frostfire/glacial-brand.c");
    addChild("/guilds/runeskald/frostfire/ember-rune.c",
        "/guilds/runeskald/frostfire/runic-flame.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frost-bolt.c");
    addResearchElement("/guilds/runeskald/frostfire/fire-bolt.c");

    addChild("/guilds/runeskald/frostfire/frost-bolt.c",
        "/guilds/runeskald/frostfire/glacial-brand.c");
    addChild("/guilds/runeskald/frostfire/fire-bolt.c",
        "/guilds/runeskald/frostfire/runic-flame.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frost-nova.c");
    addResearchElement("/guilds/runeskald/frostfire/fire-nova.c");

    addChild("/guilds/runeskald/frostfire/frost-nova.c",
        "/guilds/runeskald/frostfire/frost-bolt.c");
    addChild("/guilds/runeskald/frostfire/fire-nova.c",
        "/guilds/runeskald/frostfire/fire-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/glacial-armor.c");
    addResearchElement("/guilds/runeskald/frostfire/flame-shroud.c");
    addResearchElement("/guilds/runeskald/frostfire/frost-mastery.c");
    addResearchElement("/guilds/runeskald/frostfire/fire-mastery.c");

    addChild("/guilds/runeskald/frostfire/glacial-armor.c",
        "/guilds/runeskald/frostfire/frost-bolt.c");
    addChild("/guilds/runeskald/frostfire/flame-shroud.c",
        "/guilds/runeskald/frostfire/fire-bolt.c");
    addChild("/guilds/runeskald/frostfire/frost-mastery.c",
        "/guilds/runeskald/frostfire/frost-bolt.c");
    addChild("/guilds/runeskald/frostfire/fire-mastery.c",
        "/guilds/runeskald/frostfire/fire-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frost-lance.c");
    addResearchElement("/guilds/runeskald/frostfire/flame-strike.c");

    addChild("/guilds/runeskald/frostfire/frost-lance.c",
        "/guilds/runeskald/frostfire/frost-nova.c");
    addChild("/guilds/runeskald/frostfire/flame-strike.c",
        "/guilds/runeskald/frostfire/fire-nova.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-attunement.c");
    addResearchElement("/guilds/runeskald/frostfire/frost-chain.c");
    addResearchElement("/guilds/runeskald/frostfire/fire-chain.c");

    addChild("/guilds/runeskald/frostfire/elemental-attunement.c",
        "/guilds/runeskald/frostfire/frostfire-lore.c");
    addChild("/guilds/runeskald/frostfire/frost-chain.c",
        "/guilds/runeskald/frostfire/frost-lance.c");
    addChild("/guilds/runeskald/frostfire/fire-chain.c",
        "/guilds/runeskald/frostfire/flame-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frostfire-bolt.c");
    addResearchElement("/guilds/runeskald/frostfire/frostfire-mantle.c");

    addChild("/guilds/runeskald/frostfire/frostfire-bolt.c",
        "/guilds/runeskald/frostfire/elemental-attunement.c");
    addChild("/guilds/runeskald/frostfire/frostfire-mantle.c",
        "/guilds/runeskald/frostfire/glacial-armor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/ice-storm.c");
    addResearchElement("/guilds/runeskald/frostfire/firestorm.c");
    addResearchElement("/guilds/runeskald/frostfire/blizzard-strike.c");
    addResearchElement("/guilds/runeskald/frostfire/inferno-surge.c");

    addChild("/guilds/runeskald/frostfire/ice-storm.c",
        "/guilds/runeskald/frostfire/frost-chain.c");
    addChild("/guilds/runeskald/frostfire/firestorm.c",
        "/guilds/runeskald/frostfire/fire-chain.c");
    addChild("/guilds/runeskald/frostfire/blizzard-strike.c",
        "/guilds/runeskald/frostfire/frost-chain.c");
    addChild("/guilds/runeskald/frostfire/inferno-surge.c",
        "/guilds/runeskald/frostfire/fire-chain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frostfire-burst.c");

    addChild("/guilds/runeskald/frostfire/frostfire-burst.c",
        "/guilds/runeskald/frostfire/glacial-brand.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frostfire-torrent.c");
    addResearchElement("/guilds/runeskald/frostfire/dual-element-mastery.c");
    addResearchElement("/guilds/runeskald/frostfire/frostfire-mastery.c");

    addChild("/guilds/runeskald/frostfire/frostfire-torrent.c",
        "/guilds/runeskald/frostfire/frostfire-bolt.c");
    addChild("/guilds/runeskald/frostfire/dual-element-mastery.c",
        "/guilds/runeskald/frostfire/elemental-attunement.c");
    addChild("/guilds/runeskald/frostfire/frostfire-mastery.c",
        "/guilds/runeskald/frostfire/dual-element-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/glacier-crash.c");
    addResearchElement("/guilds/runeskald/frostfire/infernal-pyre.c");

    addChild("/guilds/runeskald/frostfire/glacier-crash.c",
        "/guilds/runeskald/frostfire/ice-storm.c");
    addChild("/guilds/runeskald/frostfire/infernal-pyre.c",
        "/guilds/runeskald/frostfire/firestorm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frostfire-wave.c");
    addResearchElement("/guilds/runeskald/frostfire/frostfire-weapon.c");
    addResearchElement("/guilds/runeskald/frostfire/frostfire-barrage.c");

    addChild("/guilds/runeskald/frostfire/frostfire-wave.c",
        "/guilds/runeskald/frostfire/frostfire-torrent.c");
    addChild("/guilds/runeskald/frostfire/frostfire-weapon.c",
        "/guilds/runeskald/frostfire/dual-element-mastery.c");
    addChild("/guilds/runeskald/frostfire/frostfire-barrage.c",
        "/guilds/runeskald/frostfire/frostfire-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frostfire-aura.c");
    addResearchElement("/guilds/runeskald/frostfire/frost-fire-aegis.c");

    addChild("/guilds/runeskald/frostfire/frostfire-aura.c",
        "/guilds/runeskald/frostfire/frostfire-burst.c");
    addChild("/guilds/runeskald/frostfire/frost-fire-aegis.c",
        "/guilds/runeskald/frostfire/frostfire-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/frostfire-cataclysm.c");

    addChild("/guilds/runeskald/frostfire/frostfire-cataclysm.c",
        "/guilds/runeskald/frostfire/frostfire-wave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reckoning.c");

    addChild("/guilds/runeskald/frostfire/elemental-reckoning.c",
        "/guilds/runeskald/frostfire/frostfire-cataclysm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-dominion.c");

    addChild("/guilds/runeskald/frostfire/elemental-dominion.c",
        "/guilds/runeskald/frostfire/frostfire-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/twin-elemental-form.c");

    addChild("/guilds/runeskald/frostfire/twin-elemental-form.c",
        "/guilds/runeskald/frostfire/elemental-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency.c",
        "/guilds/runeskald/frostfire/twin-elemental-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve.c",
        "/guilds/runeskald/frostfire/twin-elemental-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-tempo.c");

    addChild("/guilds/runeskald/frostfire/elemental-tempo.c",
        "/guilds/runeskald/frostfire/twin-elemental-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency-ii.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency-ii.c",
        "/guilds/runeskald/frostfire/elemental-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve-ii.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve-ii.c",
        "/guilds/runeskald/frostfire/elemental-reserve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-tempo-ii.c");

    addChild("/guilds/runeskald/frostfire/elemental-tempo-ii.c",
        "/guilds/runeskald/frostfire/elemental-tempo.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency-iii.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency-iii.c",
        "/guilds/runeskald/frostfire/elemental-potency-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve-iii.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve-iii.c",
        "/guilds/runeskald/frostfire/elemental-reserve-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-tempo-iii.c");

    addChild("/guilds/runeskald/frostfire/elemental-tempo-iii.c",
        "/guilds/runeskald/frostfire/elemental-tempo-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency-iv.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency-iv.c",
        "/guilds/runeskald/frostfire/elemental-potency-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve-iv.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve-iv.c",
        "/guilds/runeskald/frostfire/elemental-reserve-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-tempo-iv.c");

    addChild("/guilds/runeskald/frostfire/elemental-tempo-iv.c",
        "/guilds/runeskald/frostfire/elemental-tempo-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency-v.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency-v.c",
        "/guilds/runeskald/frostfire/elemental-potency-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve-v.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve-v.c",
        "/guilds/runeskald/frostfire/elemental-reserve-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-tempo-v.c");

    addChild("/guilds/runeskald/frostfire/elemental-tempo-v.c",
        "/guilds/runeskald/frostfire/elemental-tempo-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency-vi.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency-vi.c",
        "/guilds/runeskald/frostfire/elemental-potency-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve-vi.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve-vi.c",
        "/guilds/runeskald/frostfire/elemental-reserve-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-tempo-vi.c");

    addChild("/guilds/runeskald/frostfire/elemental-tempo-vi.c",
        "/guilds/runeskald/frostfire/elemental-tempo-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency-vii.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency-vii.c",
        "/guilds/runeskald/frostfire/elemental-potency-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve-vii.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve-vii.c",
        "/guilds/runeskald/frostfire/elemental-reserve-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-tempo-vii.c");

    addChild("/guilds/runeskald/frostfire/elemental-tempo-vii.c",
        "/guilds/runeskald/frostfire/elemental-tempo-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-potency-viii.c");

    addChild("/guilds/runeskald/frostfire/elemental-potency-viii.c",
        "/guilds/runeskald/frostfire/elemental-potency-vii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/frostfire/elemental-reserve-viii.c");

    addChild("/guilds/runeskald/frostfire/elemental-reserve-viii.c",
        "/guilds/runeskald/frostfire/elemental-reserve-vii.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Frostfire");
    Source("runeskald");
    Description("This tree covers the runeskaldic frostfire tradition: "
        "glacial ice bolts, runic fire bolts, combined frostfire explosions, "
        "sustained elemental auras, and the ultimate elemental dominion.");

    addResearchElement("/guilds/runeskald/frostfire/root.c");
    TreeRoot("/guilds/runeskald/frostfire/root.c");

    FirstLevel();
    SecondLevel();
    ThirdLevel();
    FourthLevel();
    FifthLevel();
    SixthLevel();
    SeventhLevel();
    EighthLevel();
    NinthLevel();
    TenthLevel();
    EleventhLevel();
    TwelfthLevel();
    ThirteenthLevel();
    SixteenthLevel();
    EighteenthLevel();
    TwentiethLevel();
    TwentySecondLevel();
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
