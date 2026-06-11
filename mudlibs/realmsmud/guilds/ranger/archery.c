//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/ranger/archery/steady-aim.c");
    addResearchElement("/guilds/ranger/archery/quick-draw.c");

    addChild("/guilds/ranger/archery/steady-aim.c",
        "/guilds/ranger/archery/root.c");
    addChild("/guilds/ranger/archery/quick-draw.c",
        "/guilds/ranger/archery/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/ranger/archery/archers-stance.c");
    addResearchElement("/guilds/ranger/archery/power-shot.c");

    addChild("/guilds/ranger/archery/archers-stance.c",
        "/guilds/ranger/archery/steady-aim.c");
    addChild("/guilds/ranger/archery/power-shot.c",
        "/guilds/ranger/archery/steady-aim.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/ranger/archery/steady-shot-knowledge.c");
    addResearchElement("/guilds/ranger/archery/focused-draw.c");

    addChild("/guilds/ranger/archery/steady-shot-knowledge.c",
        "/guilds/ranger/archery/steady-aim.c");
    addChild("/guilds/ranger/archery/focused-draw.c",
        "/guilds/ranger/archery/archers-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/ranger/archery/eagle-eye.c");

    addChild("/guilds/ranger/archery/eagle-eye.c",
        "/guilds/ranger/archery/steady-aim.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/ranger/archery/aimed-shot.c");
    addResearchElement("/guilds/ranger/archery/wind-reading.c");

    addChild("/guilds/ranger/archery/aimed-shot.c",
        "/guilds/ranger/archery/eagle-eye.c");
    addChild("/guilds/ranger/archery/wind-reading.c",
        "/guilds/ranger/archery/eagle-eye.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/ranger/archery/rapid-reload.c");

    addChild("/guilds/ranger/archery/rapid-reload.c",
        "/guilds/ranger/archery/quick-draw.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/ranger/archery/archers-reflexes.c");

    addChild("/guilds/ranger/archery/archers-reflexes.c",
        "/guilds/ranger/archery/rapid-reload.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/ranger/archery/rapid-shot.c");

    addChild("/guilds/ranger/archery/rapid-shot.c",
        "/guilds/ranger/archery/rapid-reload.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/ranger/archery/penetrating-focus.c");
    addResearchElement("/guilds/ranger/archery/quick-shot-knowledge.c");

    addChild("/guilds/ranger/archery/penetrating-focus.c",
        "/guilds/ranger/archery/eagle-eye.c");
    addChild("/guilds/ranger/archery/quick-shot-knowledge.c",
        "/guilds/ranger/archery/penetrating-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/ranger/archery/vital-targeting.c");

    addChild("/guilds/ranger/archery/vital-targeting.c",
        "/guilds/ranger/archery/penetrating-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/ranger/archery/piercing-shot.c");
    addResearchElement("/guilds/ranger/archery/bowyers-wisdom.c");

    addChild("/guilds/ranger/archery/piercing-shot.c",
        "/guilds/ranger/archery/penetrating-focus.c");
    addChild("/guilds/ranger/archery/bowyers-wisdom.c",
        "/guilds/ranger/archery/archers-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/ranger/archery/arrow-mastery.c");

    addChild("/guilds/ranger/archery/arrow-mastery.c",
        "/guilds/ranger/archery/bowyers-wisdom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/ranger/archery/dead-eye.c");
    addResearchElement("/guilds/ranger/archery/supreme-accuracy.c");

    addChild("/guilds/ranger/archery/dead-eye.c",
        "/guilds/ranger/archery/supreme-accuracy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addChild("/guilds/ranger/archery/supreme-accuracy.c",
        "/guilds/ranger/archery/penetrating-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/ranger/archery/heart-seeker.c");
    addResearchElement("/guilds/ranger/archery/piercing-knowledge.c");

    addChild("/guilds/ranger/archery/heart-seeker.c",
        "/guilds/ranger/archery/bowyers-wisdom.c");
    addChild("/guilds/ranger/archery/piercing-knowledge.c",
        "/guilds/ranger/archery/supreme-accuracy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/ranger/archery/killing-precision.c");

    addChild("/guilds/ranger/archery/killing-precision.c",
        "/guilds/ranger/archery/supreme-accuracy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/ranger/archery/legendary-marksman.c");

    addChild("/guilds/ranger/archery/legendary-marksman.c",
        "/guilds/ranger/archery/supreme-accuracy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/ranger/archery/war-bow.c");

    addChild("/guilds/ranger/archery/war-bow.c",
        "/guilds/ranger/archery/legendary-marksman.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/ranger/archery/double-shot.c");

    addChild("/guilds/ranger/archery/double-shot.c",
        "/guilds/ranger/archery/legendary-marksman.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/ranger/archery/sniper.c");

    addChild("/guilds/ranger/archery/sniper.c",
        "/guilds/ranger/archery/legendary-marksman.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/ranger/archery/peerless-archer.c");

    addChild("/guilds/ranger/archery/peerless-archer.c",
        "/guilds/ranger/archery/legendary-marksman.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/ranger/archery/lethal-focus.c");

    addChild("/guilds/ranger/archery/lethal-focus.c",
        "/guilds/ranger/archery/peerless-archer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/ranger/archery/death-dealer.c");

    addChild("/guilds/ranger/archery/death-dealer.c",
        "/guilds/ranger/archery/peerless-archer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/ranger/archery/rain-of-arrows.c");

    addChild("/guilds/ranger/archery/rain-of-arrows.c",
        "/guilds/ranger/archery/peerless-archer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/ranger/archery/transcendent-bow.c");

    addChild("/guilds/ranger/archery/transcendent-bow.c",
        "/guilds/ranger/archery/peerless-archer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/ranger/archery/storm-of-arrows.c");

    addChild("/guilds/ranger/archery/storm-of-arrows.c",
        "/guilds/ranger/archery/transcendent-bow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/ranger/archery/arcing-mastery.c");

    addChild("/guilds/ranger/archery/arcing-mastery.c",
        "/guilds/ranger/archery/transcendent-bow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/ranger/archery/eternal-marksman.c");
    addResearchElement("/guilds/ranger/archery/death-shot.c");

    addChild("/guilds/ranger/archery/eternal-marksman.c",
        "/guilds/ranger/archery/transcendent-bow.c");
    addChild("/guilds/ranger/archery/death-shot.c",
        "/guilds/ranger/archery/transcendent-bow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/ranger/archery/gods-aim.c");

    addChild("/guilds/ranger/archery/gods-aim.c",
        "/guilds/ranger/archery/eternal-marksman.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyLevel()
{
    addResearchElement("/guilds/ranger/archery/sky-shot.c");
    addResearchElement("/guilds/ranger/archery/perfected-archery.c");

    addChild("/guilds/ranger/archery/sky-shot.c",
        "/guilds/ranger/archery/eternal-marksman.c");
    addChild("/guilds/ranger/archery/perfected-archery.c",
        "/guilds/ranger/archery/eternal-marksman.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Archery");
    Description("This research tree provides the ranger with "
        "advanced physical archery techniques.");
    Source("ranger");
    addResearchElement("/guilds/ranger/archery/root.c");
    TreeRoot("/guilds/ranger/archery/root.c");

    FirstLevel();
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
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
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
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SeventyLevel();
}
