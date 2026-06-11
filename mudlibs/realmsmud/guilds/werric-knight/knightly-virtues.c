//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/code-of-honor.c");
    addChild("/guilds/werric-knight/virtues/code-of-honor.c",
        "/guilds/werric-knight/virtues/root.c");

    addResearchElement("/guilds/werric-knight/virtues/courage.c");
    addChild("/guilds/werric-knight/virtues/courage.c",
        "/guilds/werric-knight/virtues/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/strike-of-justice.c");
    addChild("/guilds/werric-knight/virtues/strike-of-justice.c",
        "/guilds/werric-knight/virtues/code-of-honor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/righteous-focus.c");
    addChild("/guilds/werric-knight/virtues/righteous-focus.c",
        "/guilds/werric-knight/virtues/strike-of-justice.c");

    addResearchElement("/guilds/werric-knight/virtues/virtuous-strike.c");
    addChild("/guilds/werric-knight/virtues/virtuous-strike.c",
        "/guilds/werric-knight/virtues/strike-of-justice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/courage-unwavering.c");
    addChild("/guilds/werric-knight/virtues/courage-unwavering.c",
        "/guilds/werric-knight/virtues/righteous-focus.c");

    addResearchElement("/guilds/werric-knight/virtues/aura-of-protection.c");
    addChild("/guilds/werric-knight/virtues/aura-of-protection.c",
        "/guilds/werric-knight/virtues/courage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/merciful-protection.c");
    addChild("/guilds/werric-knight/virtues/merciful-protection.c",
        "/guilds/werric-knight/virtues/courage-unwavering.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/noble-bearing.c");
    addChild("/guilds/werric-knight/virtues/noble-bearing.c",
        "/guilds/werric-knight/virtues/merciful-protection.c");

    addResearchElement("/guilds/werric-knight/virtues/inspiring-presence.c");
    addChild("/guilds/werric-knight/virtues/inspiring-presence.c",
        "/guilds/werric-knight/virtues/noble-bearing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/champions-challenge.c");
    addChild("/guilds/werric-knight/virtues/champions-challenge.c",
        "/guilds/werric-knight/virtues/noble-bearing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/justice-magnified.c");
    addChild("/guilds/werric-knight/virtues/justice-magnified.c",
        "/guilds/werric-knight/virtues/champions-challenge.c");

    addResearchElement("/guilds/werric-knight/virtues/paragon-of-virtue.c");
    addChild("/guilds/werric-knight/virtues/paragon-of-virtue.c",
        "/guilds/werric-knight/virtues/inspiring-presence.c");

    addResearchElement("/guilds/werric-knight/virtues/righteous-power.c");
    addChild("/guilds/werric-knight/virtues/righteous-power.c",
        "/guilds/werric-knight/virtues/champions-challenge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/honorable-combat.c");
    addChild("/guilds/werric-knight/virtues/honorable-combat.c",
        "/guilds/werric-knight/virtues/justice-magnified.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/virtuous-strength.c");
    addChild("/guilds/werric-knight/virtues/virtuous-strength.c",
        "/guilds/werric-knight/virtues/honorable-combat.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/virtuous-power.c");
    addChild("/guilds/werric-knight/virtues/virtuous-power.c",
        "/guilds/werric-knight/virtues/virtuous-strength.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/judgment-strike.c");
    addChild("/guilds/werric-knight/virtues/judgment-strike.c",
        "/guilds/werric-knight/virtues/virtuous-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/exemplar-of-virtue.c");
    addChild("/guilds/werric-knight/virtues/exemplar-of-virtue.c",
        "/guilds/werric-knight/virtues/judgment-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/aura-of-virtue.c");
    addChild("/guilds/werric-knight/virtues/aura-of-virtue.c",
        "/guilds/werric-knight/virtues/exemplar-of-virtue.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/perfect-virtue.c");
    addChild("/guilds/werric-knight/virtues/perfect-virtue.c",
        "/guilds/werric-knight/virtues/aura-of-virtue.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/righteous-fury.c");
    addChild("/guilds/werric-knight/virtues/righteous-fury.c",
        "/guilds/werric-knight/virtues/perfect-virtue.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/legendary-virtue.c");
    addChild("/guilds/werric-knight/virtues/legendary-virtue.c",
        "/guilds/werric-knight/virtues/righteous-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/transcendent-virtue.c");
    addChild("/guilds/werric-knight/virtues/transcendent-virtue.c",
        "/guilds/werric-knight/virtues/legendary-virtue.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/champion-of-virtue.c");
    addChild("/guilds/werric-knight/virtues/champion-of-virtue.c",
        "/guilds/werric-knight/virtues/transcendent-virtue.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/werric-knight/virtues/eternal-virtue.c");
    addChild("/guilds/werric-knight/virtues/eternal-virtue.c",
        "/guilds/werric-knight/virtues/champion-of-virtue.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Knightly Virtues");
    Description("This research tree represents the principles of honor, courage, "
        "justice, and mercy that define what it means to be a true knight. These "
        "virtues are sources of tangible power for those who embody them.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/virtues/root.c");
    TreeRoot("/guilds/werric-knight/virtues/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    TwentiethLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    ThirtiethLevel();
    ThirtyFifthLevel();
    FortiethLevel();
    FortyFifthLevel();
    FiftiethLevel();
    FiftyFifthLevel();
    SixtiethLevel();
    SeventiethLevel();
}

