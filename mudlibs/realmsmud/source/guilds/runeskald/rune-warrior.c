//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/rune-gripped-blade.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-bound-axe.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-struck-hammer.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-struck-mace.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-etched-spear.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-strength.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-iron.c");

    addChild("/guilds/runeskald/rune-warrior/rune-gripped-blade.c",
        "/guilds/runeskald/rune-warrior/root.c");
    addChild("/guilds/runeskald/rune-warrior/rune-bound-axe.c",
        "/guilds/runeskald/rune-warrior/root.c");
    addChild("/guilds/runeskald/rune-warrior/rune-struck-hammer.c",
        "/guilds/runeskald/rune-warrior/root.c");
    addChild("/guilds/runeskald/rune-warrior/rune-struck-mace.c",
        "/guilds/runeskald/rune-warrior/root.c");
    addChild("/guilds/runeskald/rune-warrior/rune-etched-spear.c",
        "/guilds/runeskald/rune-warrior/root.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-strength.c",
        "/guilds/runeskald/rune-warrior/root.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-iron.c",
        "/guilds/runeskald/rune-warrior/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-speed.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-weapon-lore.c");

    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-speed.c",
        "/guilds/runeskald/rune-warrior/root.c");
    addChild("/guilds/runeskald/rune-warrior/rune-weapon-lore.c",
        "/guilds/runeskald/rune-warrior/rune-gripped-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-fury.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-warding.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-wrath.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-swiftness.c");

    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-fury.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-strength.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-warding.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-iron.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-wrath.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-speed.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-swiftness.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-iron.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/rune-thrust.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-bash.c");
    addResearchElement("/guilds/runeskald/rune-warrior/body-rune-mastery.c");

    addChild("/guilds/runeskald/rune-warrior/rune-thrust.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-speed.c");
    addChild("/guilds/runeskald/rune-warrior/rune-bash.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-fury.c");
    addChild("/guilds/runeskald/rune-warrior/body-rune-mastery.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-strength.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/rune-strike.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-hammer-strike.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-axe-cleave.c");

    addChild("/guilds/runeskald/rune-warrior/rune-strike.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-strength.c");
    addChild("/guilds/runeskald/rune-warrior/rune-hammer-strike.c",
        "/guilds/runeskald/rune-warrior/rune-struck-hammer.c");
    addChild("/guilds/runeskald/rune-warrior/rune-axe-cleave.c",
        "/guilds/runeskald/rune-warrior/rune-bound-axe.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-alacrity.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-endurance.c");

    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-alacrity.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-speed.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-endurance.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-warding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/rune-lunge.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-smash.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-rend.c");

    addChild("/guilds/runeskald/rune-warrior/rune-lunge.c",
        "/guilds/runeskald/rune-warrior/rune-thrust.c");
    addChild("/guilds/runeskald/rune-warrior/rune-smash.c",
        "/guilds/runeskald/rune-warrior/rune-bash.c");
    addChild("/guilds/runeskald/rune-warrior/rune-rend.c",
        "/guilds/runeskald/rune-warrior/rune-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-resilience.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-crater.c");

    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-resilience.c",
        "/guilds/runeskald/rune-warrior/body-rune-mastery.c");
    addChild("/guilds/runeskald/rune-warrior/rune-crater.c",
        "/guilds/runeskald/rune-warrior/rune-smash.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/rune-cleave.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-vitality.c");

    addChild("/guilds/runeskald/rune-warrior/rune-cleave.c",
        "/guilds/runeskald/rune-warrior/rune-strike.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-vitality.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/rune-flurry.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-surge.c");

    addChild("/guilds/runeskald/rune-warrior/rune-flurry.c",
        "/guilds/runeskald/rune-warrior/rune-lunge.c");
    addChild("/guilds/runeskald/rune-warrior/rune-surge.c",
        "/guilds/runeskald/rune-warrior/rune-flurry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/runic-whirlwind.c");

    addChild("/guilds/runeskald/rune-warrior/runic-whirlwind.c",
        "/guilds/runeskald/rune-warrior/rune-cleave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-devastation.c");
    addResearchElement("/guilds/runeskald/rune-warrior/paint-rune-of-the-berserker.c");

    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-devastation.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-fury.c");
    addChild("/guilds/runeskald/rune-warrior/paint-rune-of-the-berserker.c",
        "/guilds/runeskald/rune-warrior/paint-rune-of-devastation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourteenthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/runic-execution.c");

    addChild("/guilds/runeskald/rune-warrior/runic-execution.c",
        "/guilds/runeskald/rune-warrior/rune-flurry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/runic-devastation.c");

    addChild("/guilds/runeskald/rune-warrior/runic-devastation.c",
        "/guilds/runeskald/rune-warrior/runic-whirlwind.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/rune-warrior-stance.c");
    addResearchElement("/guilds/runeskald/rune-warrior/rune-warriors-resolve.c");

    addChild("/guilds/runeskald/rune-warrior/rune-warrior-stance.c",
        "/guilds/runeskald/rune-warrior/runic-whirlwind.c");
    addChild("/guilds/runeskald/rune-warrior/rune-warriors-resolve.c",
        "/guilds/runeskald/rune-warrior/rune-warrior-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/runic-rampage.c");

    addChild("/guilds/runeskald/rune-warrior/runic-rampage.c",
        "/guilds/runeskald/rune-warrior/runic-execution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/runic-war-form.c");

    addChild("/guilds/runeskald/rune-warrior/runic-war-form.c",
        "/guilds/runeskald/rune-warrior/runic-rampage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might.c",
        "/guilds/runeskald/rune-warrior/runic-war-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance.c",
        "/guilds/runeskald/rune-warrior/runic-war-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo.c",
        "/guilds/runeskald/rune-warrior/runic-war-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might-ii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might-ii.c",
        "/guilds/runeskald/rune-warrior/warriors-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance-ii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance-ii.c",
        "/guilds/runeskald/rune-warrior/warriors-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo-ii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo-ii.c",
        "/guilds/runeskald/rune-warrior/warriors-tempo.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might-iii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might-iii.c",
        "/guilds/runeskald/rune-warrior/warriors-might-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance-iii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance-iii.c",
        "/guilds/runeskald/rune-warrior/warriors-endurance-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo-iii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo-iii.c",
        "/guilds/runeskald/rune-warrior/warriors-tempo-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might-iv.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might-iv.c",
        "/guilds/runeskald/rune-warrior/warriors-might-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance-iv.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance-iv.c",
        "/guilds/runeskald/rune-warrior/warriors-endurance-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo-iv.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo-iv.c",
        "/guilds/runeskald/rune-warrior/warriors-tempo-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might-v.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might-v.c",
        "/guilds/runeskald/rune-warrior/warriors-might-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance-v.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance-v.c",
        "/guilds/runeskald/rune-warrior/warriors-endurance-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo-v.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo-v.c",
        "/guilds/runeskald/rune-warrior/warriors-tempo-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might-vi.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might-vi.c",
        "/guilds/runeskald/rune-warrior/warriors-might-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance-vi.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance-vi.c",
        "/guilds/runeskald/rune-warrior/warriors-endurance-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo-vi.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo-vi.c",
        "/guilds/runeskald/rune-warrior/warriors-tempo-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might-vii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might-vii.c",
        "/guilds/runeskald/rune-warrior/warriors-might-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance-vii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance-vii.c",
        "/guilds/runeskald/rune-warrior/warriors-endurance-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo-vii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo-vii.c",
        "/guilds/runeskald/rune-warrior/warriors-tempo-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-might-viii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-might-viii.c",
        "/guilds/runeskald/rune-warrior/warriors-might-vii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-endurance-viii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-endurance-viii.c",
        "/guilds/runeskald/rune-warrior/warriors-endurance-vii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-warrior/warriors-tempo-viii.c");

    addChild("/guilds/runeskald/rune-warrior/warriors-tempo-viii.c",
        "/guilds/runeskald/rune-warrior/warriors-tempo-vii.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Rune Warrior");
    Source("runeskald");
    Description("This tree covers the runeskaldic warrior tradition: "
        "painting runes on flesh and steel, delivering rune-enhanced "
        "weapon strikes, and sweeping rune-lit arcs through groups of foes.");

    addResearchElement("/guilds/runeskald/rune-warrior/root.c");
    TreeRoot("/guilds/runeskald/rune-warrior/root.c");

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
    FourteenthLevel();
    FifteenthLevel();
    SixteenthLevel();
    EighteenthLevel();
    TwentiethLevel();
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
