//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-clarity.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-vigor.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/blood-initiation.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/blood-mark.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/first-rite.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-awakening.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-clarity.c",
        "/guilds/disciple-of-argloth/blood-rites/acolyte-vigor.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-vigor.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/blood-initiation.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/blood-mark.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/first-rite.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-awakening.c",
        "/guilds/disciple-of-argloth/blood-rites/first-rite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-devotion.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-warding.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-devotion.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-warding.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-cunning.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-attunement.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-cunning.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-attunement.c",
        "/guilds/disciple-of-argloth/blood-rites/rite-of-awakening.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-resilience.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/blood-sealing.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-pain.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-resilience.c",
        "/guilds/disciple-of-argloth/blood-rites/acolyte-clarity.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/blood-sealing.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-pain.c",
        "/guilds/disciple-of-argloth/blood-rites/rite-of-devotion.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/blood-mark.c",
        "/guilds/disciple-of-argloth/blood-rites/rite-of-pain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-binding-rank.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-suffering.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-binding-rank.c",
        "/guilds/disciple-of-argloth/blood-rites/rite-of-attunement.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-suffering.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-focus.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-might.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-focus.c",
        "/guilds/disciple-of-argloth/blood-rites/acolyte-resilience.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-might.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-strike.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-consecration.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-strike.c",
        "/guilds/disciple-of-argloth/blood-rites/acolyte-focus.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-consecration.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-potency.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/covenant-attunement.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-potency.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/covenant-attunement.c",
        "/guilds/disciple-of-argloth/blood-rites/blood-mark.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/dark-mark.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-the-covenant.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/dark-mark.c",
        "/guilds/disciple-of-argloth/blood-rites/acolyte-strike.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-the-covenant.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-awakening.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-lore.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-awakening.c",
        "/guilds/disciple-of-argloth/blood-rites/dark-mark.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-lore.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/blood-covenant-rite.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/blood-covenant-rite.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-power.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-endurance.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-power.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-endurance.c",
        "/guilds/disciple-of-argloth/blood-rites/scholar-awakening.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-fortitude.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-fortitude.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/acolyte-stamina.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-attunement.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/acolyte-stamina.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-attunement.c",
        "/guilds/disciple-of-argloth/blood-rites/rite-of-binding-rank.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/blood-rite-mastery.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-strike.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/blood-rite-mastery.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-strike.c",
        "/guilds/disciple-of-argloth/blood-rites/scholar-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/rite-of-reckoning.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-depth.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/rite-of-reckoning.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-depth.c",
        "/guilds/disciple-of-argloth/blood-rites/covenant-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-potency.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-potency.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-dominion.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-fury.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-dominion.c",
        "/guilds/disciple-of-argloth/blood-rites/scholar-endurance.c");
    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-fury.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/scholar-mastery.c");

    addChild("/guilds/disciple-of-argloth/blood-rites/scholar-mastery.c",
        "/guilds/disciple-of-argloth/blood-rites/root.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Blood Rites");
    Description("The foundational blood rites performed by every acolyte of Argloth - ceremonies that mark the body and attune the initiate to the blood covenant.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/blood-rites/root.c");
    TreeRoot("/guilds/disciple-of-argloth/blood-rites/root.c");

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
}
