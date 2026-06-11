//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/battle-saga.c");
    addResearchElement("/guilds/runeskald/song-of-power/voice-of-the-skald.c");
    addResearchElement("/guilds/runeskald/song-of-power/wounding-verse.c");
    addResearchElement("/guilds/runeskald/song-of-power/skalds-vigil.c");

    addChild("/guilds/runeskald/song-of-power/battle-saga.c",
        "/guilds/runeskald/song-of-power/root.c");
    addChild("/guilds/runeskald/song-of-power/voice-of-the-skald.c",
        "/guilds/runeskald/song-of-power/root.c");
    addChild("/guilds/runeskald/song-of-power/wounding-verse.c",
        "/guilds/runeskald/song-of-power/root.c");
    addChild("/guilds/runeskald/song-of-power/skalds-vigil.c",
        "/guilds/runeskald/song-of-power/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/rallying-cry.c");
    addResearchElement("/guilds/runeskald/song-of-power/taunt.c");

    addChild("/guilds/runeskald/song-of-power/rallying-cry.c",
        "/guilds/runeskald/song-of-power/battle-saga.c");
    addChild("/guilds/runeskald/song-of-power/taunt.c",
        "/guilds/runeskald/song-of-power/wounding-verse.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/dirge-of-ruin.c");
    addResearchElement("/guilds/runeskald/song-of-power/saga-lore.c");
    addResearchElement("/guilds/runeskald/song-of-power/curse-of-ruin.c");

    addChild("/guilds/runeskald/song-of-power/dirge-of-ruin.c",
        "/guilds/runeskald/song-of-power/root.c");
    addChild("/guilds/runeskald/song-of-power/saga-lore.c",
        "/guilds/runeskald/song-of-power/voice-of-the-skald.c");
    addChild("/guilds/runeskald/song-of-power/curse-of-ruin.c",
        "/guilds/runeskald/song-of-power/wounding-verse.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/curse-of-weakness.c");
    addResearchElement("/guilds/runeskald/song-of-power/song-of-endurance.c");
    addResearchElement("/guilds/runeskald/song-of-power/song-of-the-shield.c");

    addChild("/guilds/runeskald/song-of-power/curse-of-weakness.c",
        "/guilds/runeskald/song-of-power/dirge-of-ruin.c");
    addChild("/guilds/runeskald/song-of-power/song-of-endurance.c",
        "/guilds/runeskald/song-of-power/skalds-vigil.c");
    addChild("/guilds/runeskald/song-of-power/song-of-the-shield.c",
        "/guilds/runeskald/song-of-power/song-of-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/war-chant.c");
    addResearchElement("/guilds/runeskald/song-of-power/frost-song.c");
    addResearchElement("/guilds/runeskald/song-of-power/saga-of-the-fallen.c");
    addResearchElement("/guilds/runeskald/song-of-power/ode-of-iron.c");

    addChild("/guilds/runeskald/song-of-power/war-chant.c",
        "/guilds/runeskald/song-of-power/battle-saga.c");
    addChild("/guilds/runeskald/song-of-power/frost-song.c",
        "/guilds/runeskald/song-of-power/dirge-of-ruin.c");
    addChild("/guilds/runeskald/song-of-power/saga-of-the-fallen.c",
        "/guilds/runeskald/song-of-power/battle-saga.c");
    addChild("/guilds/runeskald/song-of-power/ode-of-iron.c",
        "/guilds/runeskald/song-of-power/rallying-cry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/verse-of-doom.c");
    addResearchElement("/guilds/runeskald/song-of-power/saga-of-swiftness.c");
    addResearchElement("/guilds/runeskald/song-of-power/verse-of-agony.c");

    addChild("/guilds/runeskald/song-of-power/verse-of-doom.c",
        "/guilds/runeskald/song-of-power/dirge-of-ruin.c");
    addChild("/guilds/runeskald/song-of-power/saga-of-swiftness.c",
        "/guilds/runeskald/song-of-power/battle-saga.c");
    addChild("/guilds/runeskald/song-of-power/verse-of-agony.c",
        "/guilds/runeskald/song-of-power/verse-of-doom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/elder-verse-lore.c");
    addResearchElement("/guilds/runeskald/song-of-power/shriek-of-ruin.c");
    addResearchElement("/guilds/runeskald/song-of-power/voice-mastery.c");

    addChild("/guilds/runeskald/song-of-power/elder-verse-lore.c",
        "/guilds/runeskald/song-of-power/saga-lore.c");
    addChild("/guilds/runeskald/song-of-power/shriek-of-ruin.c",
        "/guilds/runeskald/song-of-power/curse-of-weakness.c");
    addChild("/guilds/runeskald/song-of-power/voice-mastery.c",
        "/guilds/runeskald/song-of-power/saga-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/song-of-clarity.c");
    addResearchElement("/guilds/runeskald/song-of-power/battle-howl.c");

    addChild("/guilds/runeskald/song-of-power/song-of-clarity.c",
        "/guilds/runeskald/song-of-power/song-of-endurance.c");
    addChild("/guilds/runeskald/song-of-power/battle-howl.c",
        "/guilds/runeskald/song-of-power/verse-of-doom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/saga-of-the-hero.c");
    addResearchElement("/guilds/runeskald/song-of-power/wail-of-the-fallen.c");

    addChild("/guilds/runeskald/song-of-power/saga-of-the-hero.c",
        "/guilds/runeskald/song-of-power/saga-of-the-fallen.c");
    addChild("/guilds/runeskald/song-of-power/wail-of-the-fallen.c",
        "/guilds/runeskald/song-of-power/battle-howl.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/anguished-wail.c");
    addResearchElement("/guilds/runeskald/song-of-power/saga-of-endurance.c");

    addChild("/guilds/runeskald/song-of-power/anguished-wail.c",
        "/guilds/runeskald/song-of-power/battle-howl.c");
    addChild("/guilds/runeskald/song-of-power/saga-of-endurance.c",
        "/guilds/runeskald/song-of-power/saga-of-swiftness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/elder-saga.c");

    addChild("/guilds/runeskald/song-of-power/elder-saga.c",
        "/guilds/runeskald/song-of-power/war-chant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/shriek-of-doom.c");

    addChild("/guilds/runeskald/song-of-power/shriek-of-doom.c",
        "/guilds/runeskald/song-of-power/shriek-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/hymn-of-wrath.c");
    addResearchElement("/guilds/runeskald/song-of-power/saga-of-the-conqueror.c");

    addChild("/guilds/runeskald/song-of-power/hymn-of-wrath.c",
        "/guilds/runeskald/song-of-power/war-chant.c");
    addChild("/guilds/runeskald/song-of-power/saga-of-the-conqueror.c",
        "/guilds/runeskald/song-of-power/saga-of-the-hero.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourteenthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/echo-of-doom.c");
    addResearchElement("/guilds/runeskald/song-of-power/voice-of-ruin.c");

    addChild("/guilds/runeskald/song-of-power/echo-of-doom.c",
        "/guilds/runeskald/song-of-power/shriek-of-ruin.c");
    addChild("/guilds/runeskald/song-of-power/voice-of-ruin.c",
        "/guilds/runeskald/song-of-power/echo-of-doom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/song-of-dominance.c");

    addChild("/guilds/runeskald/song-of-power/song-of-dominance.c",
        "/guilds/runeskald/song-of-power/hymn-of-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/saga-of-annihilation.c");

    addChild("/guilds/runeskald/song-of-power/saga-of-annihilation.c",
        "/guilds/runeskald/song-of-power/anguished-wail.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/song-of-annihilation.c");

    addChild("/guilds/runeskald/song-of-power/song-of-annihilation.c",
        "/guilds/runeskald/song-of-power/saga-of-annihilation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/primal-saga.c");

    addChild("/guilds/runeskald/song-of-power/primal-saga.c",
        "/guilds/runeskald/song-of-power/elder-saga.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFourthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/world-song.c");

    addChild("/guilds/runeskald/song-of-power/world-song.c",
        "/guilds/runeskald/song-of-power/primal-saga.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-power.c");

    addChild("/guilds/runeskald/song-of-power/vocal-power.c",
        "/guilds/runeskald/song-of-power/world-song.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-endurance.c");

    addChild("/guilds/runeskald/song-of-power/vocal-endurance.c",
        "/guilds/runeskald/song-of-power/world-song.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-swiftness.c");

    addChild("/guilds/runeskald/song-of-power/vocal-swiftness.c",
        "/guilds/runeskald/song-of-power/world-song.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-power-ii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-power-ii.c",
        "/guilds/runeskald/song-of-power/vocal-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-endurance-ii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-endurance-ii.c",
        "/guilds/runeskald/song-of-power/vocal-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-swiftness-ii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-swiftness-ii.c",
        "/guilds/runeskald/song-of-power/vocal-swiftness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-power-iii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-power-iii.c",
        "/guilds/runeskald/song-of-power/vocal-power-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-endurance-iii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-endurance-iii.c",
        "/guilds/runeskald/song-of-power/vocal-endurance-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-swiftness-iii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-swiftness-iii.c",
        "/guilds/runeskald/song-of-power/vocal-swiftness-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-power-iv.c");

    addChild("/guilds/runeskald/song-of-power/vocal-power-iv.c",
        "/guilds/runeskald/song-of-power/vocal-power-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-endurance-iv.c");

    addChild("/guilds/runeskald/song-of-power/vocal-endurance-iv.c",
        "/guilds/runeskald/song-of-power/vocal-endurance-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-swiftness-iv.c");

    addChild("/guilds/runeskald/song-of-power/vocal-swiftness-iv.c",
        "/guilds/runeskald/song-of-power/vocal-swiftness-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-power-v.c");

    addChild("/guilds/runeskald/song-of-power/vocal-power-v.c",
        "/guilds/runeskald/song-of-power/vocal-power-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-endurance-v.c");

    addChild("/guilds/runeskald/song-of-power/vocal-endurance-v.c",
        "/guilds/runeskald/song-of-power/vocal-endurance-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-swiftness-v.c");

    addChild("/guilds/runeskald/song-of-power/vocal-swiftness-v.c",
        "/guilds/runeskald/song-of-power/vocal-swiftness-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-power-vi.c");

    addChild("/guilds/runeskald/song-of-power/vocal-power-vi.c",
        "/guilds/runeskald/song-of-power/vocal-power-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-endurance-vi.c");

    addChild("/guilds/runeskald/song-of-power/vocal-endurance-vi.c",
        "/guilds/runeskald/song-of-power/vocal-endurance-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-swiftness-vi.c");

    addChild("/guilds/runeskald/song-of-power/vocal-swiftness-vi.c",
        "/guilds/runeskald/song-of-power/vocal-swiftness-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-power-vii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-power-vii.c",
        "/guilds/runeskald/song-of-power/vocal-power-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-swiftness-vii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-swiftness-vii.c",
        "/guilds/runeskald/song-of-power/vocal-swiftness-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/song-of-power/vocal-endurance-vii.c");

    addChild("/guilds/runeskald/song-of-power/vocal-endurance-vii.c",
        "/guilds/runeskald/song-of-power/vocal-endurance-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Song of Power");
    Source("runeskald");
    Description("This tree covers the vocal tradition of the runeskald: "
        "battle sagas, demoralising dirges, rallying war chants, devastating "
        "battle howls, and the primal saga that channels the power of creation.");

    addResearchElement("/guilds/runeskald/song-of-power/root.c");
    TreeRoot("/guilds/runeskald/song-of-power/root.c");

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
    SixteenthLevel();
    EighteenthLevel();
    NineteenthLevel();
    TwentySecondLevel();
    TwentyFourthLevel();
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
}
