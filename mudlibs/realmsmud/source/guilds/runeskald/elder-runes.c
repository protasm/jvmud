//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/fuse-rune.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-burst.c");

    addChild("/guilds/runeskald/elder-runes/fuse-rune.c",
        "/guilds/runeskald/elder-runes/root.c");
    addChild("/guilds/runeskald/elder-runes/rune-burst.c",
        "/guilds/runeskald/elder-runes/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/inscribe-rune.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-pulse.c");

    addChild("/guilds/runeskald/elder-runes/inscribe-rune.c",
        "/guilds/runeskald/elder-runes/fuse-rune.c");
    addChild("/guilds/runeskald/elder-runes/rune-pulse.c",
        "/guilds/runeskald/elder-runes/rune-burst.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-ward.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-armor.c");

    addChild("/guilds/runeskald/elder-runes/rune-ward.c",
        "/guilds/runeskald/elder-runes/inscribe-rune.c");
    addChild("/guilds/runeskald/elder-runes/rune-armor.c",
        "/guilds/runeskald/elder-runes/rune-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-bolt.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-sight.c");

    addChild("/guilds/runeskald/elder-runes/rune-bolt.c",
        "/guilds/runeskald/elder-runes/rune-pulse.c");
    addChild("/guilds/runeskald/elder-runes/rune-sight.c",
        "/guilds/runeskald/elder-runes/rune-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/sundering-rune.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-warding.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-lash.c");

    addChild("/guilds/runeskald/elder-runes/sundering-rune.c",
        "/guilds/runeskald/elder-runes/rune-bolt.c");
    addChild("/guilds/runeskald/elder-runes/rune-of-warding.c",
        "/guilds/runeskald/elder-runes/rune-sight.c");
    addChild("/guilds/runeskald/elder-runes/rune-lash.c",
        "/guilds/runeskald/elder-runes/rune-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/chain-rune.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-reading.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-attunement.c");

    addChild("/guilds/runeskald/elder-runes/chain-rune.c",
        "/guilds/runeskald/elder-runes/sundering-rune.c");
    addChild("/guilds/runeskald/elder-runes/rune-reading.c",
        "/guilds/runeskald/elder-runes/rune-sight.c");
    addChild("/guilds/runeskald/elder-runes/rune-attunement.c",
        "/guilds/runeskald/elder-runes/rune-reading.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-shatter.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-swiftness.c");

    addChild("/guilds/runeskald/elder-runes/rune-shatter.c",
        "/guilds/runeskald/elder-runes/rune-of-warding.c");
    addChild("/guilds/runeskald/elder-runes/rune-of-swiftness.c",
        "/guilds/runeskald/elder-runes/rune-reading.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-volley.c");

    addChild("/guilds/runeskald/elder-runes/rune-volley.c",
        "/guilds/runeskald/elder-runes/chain-rune.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/elder-rune-blast.c");
    addResearchElement("/guilds/runeskald/elder-runes/elder-rune-lore.c");

    addChild("/guilds/runeskald/elder-runes/elder-rune-blast.c",
        "/guilds/runeskald/elder-runes/rune-volley.c");
    addChild("/guilds/runeskald/elder-runes/elder-rune-lore.c",
        "/guilds/runeskald/elder-runes/rune-reading.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/concussive-rune.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-fortitude.c");
    addResearchElement("/guilds/runeskald/elder-runes/elder-rune-mantle.c");

    addChild("/guilds/runeskald/elder-runes/concussive-rune.c",
        "/guilds/runeskald/elder-runes/elder-rune-blast.c");
    addChild("/guilds/runeskald/elder-runes/rune-of-fortitude.c",
        "/guilds/runeskald/elder-runes/rune-shatter.c");
    addChild("/guilds/runeskald/elder-runes/elder-rune-mantle.c",
        "/guilds/runeskald/elder-runes/rune-shatter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/twin-rune-strike.c");

    addChild("/guilds/runeskald/elder-runes/twin-rune-strike.c",
        "/guilds/runeskald/elder-runes/rune-volley.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-clarity.c");

    addChild("/guilds/runeskald/elder-runes/rune-of-clarity.c",
        "/guilds/runeskald/elder-runes/elder-rune-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-barrage.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-power.c");

    addChild("/guilds/runeskald/elder-runes/rune-barrage.c",
        "/guilds/runeskald/elder-runes/rune-shatter.c");
    addChild("/guilds/runeskald/elder-runes/rune-of-power.c",
        "/guilds/runeskald/elder-runes/rune-of-clarity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourteenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/ancient-script.c");

    addChild("/guilds/runeskald/elder-runes/ancient-script.c",
        "/guilds/runeskald/elder-runes/elder-rune-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-inferno.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-resilience.c");

    addChild("/guilds/runeskald/elder-runes/rune-inferno.c",
        "/guilds/runeskald/elder-runes/twin-rune-strike.c");
    addChild("/guilds/runeskald/elder-runes/rune-of-resilience.c",
        "/guilds/runeskald/elder-runes/rune-of-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-detonation.c");
    addResearchElement("/guilds/runeskald/elder-runes/rune-tempest.c");

    addChild("/guilds/runeskald/elder-runes/rune-detonation.c",
        "/guilds/runeskald/elder-runes/rune-barrage.c");
    addChild("/guilds/runeskald/elder-runes/rune-tempest.c",
        "/guilds/runeskald/elder-runes/rune-inferno.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-mastery.c");

    addChild("/guilds/runeskald/elder-runes/rune-of-mastery.c",
        "/guilds/runeskald/elder-runes/ancient-script.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/ancient-rune-blast.c");
    addResearchElement("/guilds/runeskald/elder-runes/ancient-rune-mastery.c");

    addChild("/guilds/runeskald/elder-runes/ancient-rune-blast.c",
        "/guilds/runeskald/elder-runes/rune-inferno.c");
    addChild("/guilds/runeskald/elder-runes/ancient-rune-mastery.c",
        "/guilds/runeskald/elder-runes/rune-of-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/ancient-rune-nova.c");

    addChild("/guilds/runeskald/elder-runes/ancient-rune-nova.c",
        "/guilds/runeskald/elder-runes/elder-rune-blast.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-aegis.c");

    addChild("/guilds/runeskald/elder-runes/rune-aegis.c",
        "/guilds/runeskald/elder-runes/rune-of-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/primal-rune-burst.c");

    addChild("/guilds/runeskald/elder-runes/primal-rune-burst.c",
        "/guilds/runeskald/elder-runes/ancient-rune-blast.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/rune-of-dominion.c");

    addChild("/guilds/runeskald/elder-runes/rune-of-dominion.c",
        "/guilds/runeskald/elder-runes/rune-of-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/primal-rune-nova.c");

    addChild("/guilds/runeskald/elder-runes/primal-rune-nova.c",
        "/guilds/runeskald/elder-runes/ancient-rune-nova.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-precision.c");

    addChild("/guilds/runeskald/elder-runes/runic-precision.c",
        "/guilds/runeskald/elder-runes/rune-of-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-efficiency.c");

    addChild("/guilds/runeskald/elder-runes/runic-efficiency.c",
        "/guilds/runeskald/elder-runes/rune-of-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-alacrity.c");

    addChild("/guilds/runeskald/elder-runes/runic-alacrity.c",
        "/guilds/runeskald/elder-runes/rune-of-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-precision-ii.c");

    addChild("/guilds/runeskald/elder-runes/runic-precision-ii.c",
        "/guilds/runeskald/elder-runes/runic-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-efficiency-ii.c");

    addChild("/guilds/runeskald/elder-runes/runic-efficiency-ii.c",
        "/guilds/runeskald/elder-runes/runic-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-alacrity-ii.c");

    addChild("/guilds/runeskald/elder-runes/runic-alacrity-ii.c",
        "/guilds/runeskald/elder-runes/runic-alacrity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-precision-iii.c");

    addChild("/guilds/runeskald/elder-runes/runic-precision-iii.c",
        "/guilds/runeskald/elder-runes/runic-precision-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-efficiency-iii.c");

    addChild("/guilds/runeskald/elder-runes/runic-efficiency-iii.c",
        "/guilds/runeskald/elder-runes/runic-efficiency-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-alacrity-iii.c");

    addChild("/guilds/runeskald/elder-runes/runic-alacrity-iii.c",
        "/guilds/runeskald/elder-runes/runic-alacrity-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-precision-iv.c");

    addChild("/guilds/runeskald/elder-runes/runic-precision-iv.c",
        "/guilds/runeskald/elder-runes/runic-precision-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-efficiency-iv.c");

    addChild("/guilds/runeskald/elder-runes/runic-efficiency-iv.c",
        "/guilds/runeskald/elder-runes/runic-efficiency-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-alacrity-iv.c");

    addChild("/guilds/runeskald/elder-runes/runic-alacrity-iv.c",
        "/guilds/runeskald/elder-runes/runic-alacrity-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-precision-v.c");

    addChild("/guilds/runeskald/elder-runes/runic-precision-v.c",
        "/guilds/runeskald/elder-runes/runic-precision-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-efficiency-v.c");

    addChild("/guilds/runeskald/elder-runes/runic-efficiency-v.c",
        "/guilds/runeskald/elder-runes/runic-efficiency-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-alacrity-v.c");

    addChild("/guilds/runeskald/elder-runes/runic-alacrity-v.c",
        "/guilds/runeskald/elder-runes/runic-alacrity-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-precision-vi.c");

    addChild("/guilds/runeskald/elder-runes/runic-precision-vi.c",
        "/guilds/runeskald/elder-runes/runic-precision-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-alacrity-vi.c");

    addChild("/guilds/runeskald/elder-runes/runic-alacrity-vi.c",
        "/guilds/runeskald/elder-runes/runic-alacrity-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/elder-runes/runic-efficiency-vi.c");

    addChild("/guilds/runeskald/elder-runes/runic-efficiency-vi.c",
        "/guilds/runeskald/elder-runes/runic-efficiency-v.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Elder Runes");
    Source("runeskald");
    Description("This tree covers the ritual binding of runes into equipment, "
        "body inscription techniques, and the channelling of rune energy "
        "as devastating elemental attacks.");

    addResearchElement("/guilds/runeskald/elder-runes/root.c");
    TreeRoot("/guilds/runeskald/elder-runes/root.c");

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
    FourteenthLevel();
    FifteenthLevel();
    SixteenthLevel();
    SeventeenthLevel();
    EighteenthLevel();
    NineteenthLevel();
    TwentiethLevel();
    TwentySecondLevel();
    TwentyFifthLevel();
    TwentyEighthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
}
