//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/runic-binding/bind-rune-of-warding.c");
    addResearchElement("/guilds/runeskald/runic-binding/bind-rune-of-fury.c");
    addResearchElement("/guilds/runeskald/runic-binding/bind-rune-of-endurance.c");

    addChild("/guilds/runeskald/runic-binding/bind-rune-of-warding.c",
        "/guilds/runeskald/runic-binding/root.c");
    addChild("/guilds/runeskald/runic-binding/bind-rune-of-fury.c",
        "/guilds/runeskald/runic-binding/root.c");
    addChild("/guilds/runeskald/runic-binding/bind-rune-of-endurance.c",
        "/guilds/runeskald/runic-binding/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/runeskald/runic-binding/runic-flesh.c");
    addResearchElement("/guilds/runeskald/runic-binding/runic-striking.c");
    addResearchElement("/guilds/runeskald/runic-binding/runic-endurance-mastery.c");
    addResearchElement("/guilds/runeskald/runic-binding/spell-rune-inscription.c");

    addChild("/guilds/runeskald/runic-binding/runic-flesh.c",
        "/guilds/runeskald/runic-binding/bind-rune-of-warding.c");
    addChild("/guilds/runeskald/runic-binding/runic-striking.c",
        "/guilds/runeskald/runic-binding/bind-rune-of-fury.c");
    addChild("/guilds/runeskald/runic-binding/runic-endurance-mastery.c",
        "/guilds/runeskald/runic-binding/bind-rune-of-endurance.c");
    addChild("/guilds/runeskald/runic-binding/spell-rune-inscription.c",
        "/guilds/runeskald/runic-binding/bind-rune-of-warding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/runic-binding/rune-locked-ward.c");
    addResearchElement("/guilds/runeskald/runic-binding/runic-battle-fury.c");
    addResearchElement("/guilds/runeskald/runic-binding/spell-rune-mastery.c");

    addChild("/guilds/runeskald/runic-binding/rune-locked-ward.c",
        "/guilds/runeskald/runic-binding/runic-flesh.c");
    addChild("/guilds/runeskald/runic-binding/runic-battle-fury.c",
        "/guilds/runeskald/runic-binding/runic-striking.c");
    addChild("/guilds/runeskald/runic-binding/spell-rune-mastery.c",
        "/guilds/runeskald/runic-binding/spell-rune-inscription.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/runeskald/runic-binding/binding-mastery.c");

    addChild("/guilds/runeskald/runic-binding/binding-mastery.c",
        "/guilds/runeskald/runic-binding/rune-locked-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/runeskald/runic-binding/rune-scarred-hide.c");
    addResearchElement("/guilds/runeskald/runic-binding/rune-burst.c");

    addChild("/guilds/runeskald/runic-binding/rune-scarred-hide.c",
        "/guilds/runeskald/runic-binding/binding-mastery.c");
    addChild("/guilds/runeskald/runic-binding/rune-burst.c",
        "/guilds/runeskald/runic-binding/binding-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/runeskald/runic-binding/rune-body-convergence.c");

    addChild("/guilds/runeskald/runic-binding/rune-body-convergence.c",
        "/guilds/runeskald/runic-binding/rune-scarred-hide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/runic-binding/full-runic-awakening.c");

    addChild("/guilds/runeskald/runic-binding/full-runic-awakening.c",
        "/guilds/runeskald/runic-binding/rune-body-convergence.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Runic Binding");
    Source("runeskald");
    Description("This tree covers the art of inscribing sustained runes "
        "directly onto the body, granting ongoing effects of protection, "
        "fury, and endurance.");

    addResearchElement("/guilds/runeskald/runic-binding/root.c");
    TreeRoot("/guilds/runeskald/runic-binding/root.c");

    FirstLevel();
    SecondLevel();
    ThirdLevel();
    FourthLevel();
    FifthLevel();
    SixthLevel();
    SeventhLevel();
}
