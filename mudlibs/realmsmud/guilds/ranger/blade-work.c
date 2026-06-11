//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/quick-blade.c");
    addResearchElement(
        "/guilds/ranger/blade-work/defensive-stance.c");

    addChild("/guilds/ranger/blade-work/quick-blade.c",
        "/guilds/ranger/blade-work/root.c");
    addChild("/guilds/ranger/blade-work/defensive-stance.c",
        "/guilds/ranger/blade-work/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/twin-fang.c");
    addResearchElement(
        "/guilds/ranger/blade-work/close-quarters.c");

    addChild("/guilds/ranger/blade-work/twin-fang.c",
        "/guilds/ranger/blade-work/quick-blade.c");
    addChild("/guilds/ranger/blade-work/close-quarters.c",
        "/guilds/ranger/blade-work/defensive-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/riposte-form.c");
    addResearchElement(
        "/guilds/ranger/blade-work/quick-slash.c");

    addChild("/guilds/ranger/blade-work/riposte-form.c",
        "/guilds/ranger/blade-work/defensive-stance.c");
    addChild("/guilds/ranger/blade-work/quick-slash.c",
        "/guilds/ranger/blade-work/quick-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/blade-dancer.c");
    addResearchElement(
        "/guilds/ranger/blade-work/knife-fighter.c");

    addChild("/guilds/ranger/blade-work/blade-dancer.c",
        "/guilds/ranger/blade-work/twin-fang.c");
    addChild("/guilds/ranger/blade-work/knife-fighter.c",
        "/guilds/ranger/blade-work/quick-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/parrying-mastery.c");
    addResearchElement(
        "/guilds/ranger/blade-work/blade-focus.c");

    addChild("/guilds/ranger/blade-work/parrying-mastery.c",
        "/guilds/ranger/blade-work/riposte-form.c");
    addChild("/guilds/ranger/blade-work/blade-focus.c",
        "/guilds/ranger/blade-work/blade-dancer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/hunters-slash.c");
    addResearchElement(
        "/guilds/ranger/blade-work/twin-strike.c");

    addChild("/guilds/ranger/blade-work/hunters-slash.c",
        "/guilds/ranger/blade-work/blade-dancer.c");
    addChild("/guilds/ranger/blade-work/twin-strike.c",
        "/guilds/ranger/blade-work/twin-fang.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/ambush-strike.c");

    addChild("/guilds/ranger/blade-work/ambush-strike.c",
        "/guilds/ranger/blade-work/knife-fighter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/whirlwind-defense.c");
    addResearchElement(
        "/guilds/ranger/blade-work/advanced-technique.c");

    addChild("/guilds/ranger/blade-work/whirlwind-defense.c",
        "/guilds/ranger/blade-work/parrying-mastery.c");
    addChild("/guilds/ranger/blade-work/advanced-technique.c",
        "/guilds/ranger/blade-work/ambush-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/flanking-blow.c");

    addChild("/guilds/ranger/blade-work/flanking-blow.c",
        "/guilds/ranger/blade-work/ambush-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/lethal-edge.c");

    addChild("/guilds/ranger/blade-work/lethal-edge.c",
        "/guilds/ranger/blade-work/hunters-slash.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/shadow-strike.c");

    addChild("/guilds/ranger/blade-work/shadow-strike.c",
        "/guilds/ranger/blade-work/ambush-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/masters-technique.c");

    addChild("/guilds/ranger/blade-work/masters-technique.c",
        "/guilds/ranger/blade-work/lethal-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/blade-mastery.c");
    addResearchElement(
        "/guilds/ranger/blade-work/eviscerate.c");

    addChild("/guilds/ranger/blade-work/blade-mastery.c",
        "/guilds/ranger/blade-work/whirlwind-defense.c");
    addChild("/guilds/ranger/blade-work/eviscerate.c",
        "/guilds/ranger/blade-work/lethal-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/rangers-flurry.c");

    addChild("/guilds/ranger/blade-work/rangers-flurry.c",
        "/guilds/ranger/blade-work/lethal-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/killing-blow.c");
    addResearchElement(
        "/guilds/ranger/blade-work/whirlwind-focus.c");

    addChild("/guilds/ranger/blade-work/killing-blow.c",
        "/guilds/ranger/blade-work/shadow-strike.c");
    addChild("/guilds/ranger/blade-work/whirlwind-focus.c",
        "/guilds/ranger/blade-work/blade-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/whirlwind-strike.c");

    addChild("/guilds/ranger/blade-work/whirlwind-strike.c",
        "/guilds/ranger/blade-work/blade-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/steel-tempest.c");

    addChild("/guilds/ranger/blade-work/steel-tempest.c",
        "/guilds/ranger/blade-work/blade-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/death-dealer.c");

    addChild("/guilds/ranger/blade-work/death-dealer.c",
        "/guilds/ranger/blade-work/rangers-flurry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/lethal-art.c");
    addResearchElement(
        "/guilds/ranger/blade-work/rangers-edge.c");

    addChild("/guilds/ranger/blade-work/lethal-art.c",
        "/guilds/ranger/blade-work/death-dealer.c");
    addChild("/guilds/ranger/blade-work/rangers-edge.c",
        "/guilds/ranger/blade-work/steel-tempest.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/death-blossom.c");

    addChild("/guilds/ranger/blade-work/death-blossom.c",
        "/guilds/ranger/blade-work/death-dealer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/blade-lord.c");

    addChild("/guilds/ranger/blade-work/blade-lord.c",
        "/guilds/ranger/blade-work/killing-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/dual-mastery.c");

    addChild("/guilds/ranger/blade-work/dual-mastery.c",
        "/guilds/ranger/blade-work/blade-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/whirlwind-mastery.c");

    addChild("/guilds/ranger/blade-work/whirlwind-mastery.c",
        "/guilds/ranger/blade-work/steel-tempest.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/supreme-technique.c");
    addResearchElement("/guilds/ranger/blade-work/supreme-blade.c");

    addChild("/guilds/ranger/blade-work/supreme-technique.c",
        "/guilds/ranger/blade-work/supreme-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/soul-rend.c");

    addChild("/guilds/ranger/blade-work/soul-rend.c",
        "/guilds/ranger/blade-work/supreme-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addChild("/guilds/ranger/blade-work/supreme-blade.c",
        "/guilds/ranger/blade-work/death-dealer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/transcendent-technique.c");
    addResearchElement("/guilds/ranger/blade-work/transcendent-edge.c");

    addChild(
        "/guilds/ranger/blade-work/transcendent-technique.c",
        "/guilds/ranger/blade-work/transcendent-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addChild("/guilds/ranger/blade-work/transcendent-edge.c",
        "/guilds/ranger/blade-work/blade-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/blade-storm.c");

    addChild("/guilds/ranger/blade-work/blade-storm.c",
        "/guilds/ranger/blade-work/transcendent-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/blade-work/peerless-blade.c");

    addChild("/guilds/ranger/blade-work/peerless-blade.c",
        "/guilds/ranger/blade-work/whirlwind-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Blade Work");
    Description("This research tree provides the ranger with "
        "mastery of close combat with bladed weapons - "
        "swords and daggers wielded with deadly precision.");
    Source("ranger");
    addResearchElement("/guilds/ranger/blade-work/root.c");
    TreeRoot("/guilds/ranger/blade-work/root.c");

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
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
