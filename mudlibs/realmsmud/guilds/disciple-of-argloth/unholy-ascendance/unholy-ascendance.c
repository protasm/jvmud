//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-vigor.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-vigor.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-vigor.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-vigor.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-awakening.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-cunning.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-darkness.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-awakening.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-cunning.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-darkness.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-endurance.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-mastery.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-endurance.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-awakening.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-mastery.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-attunement.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-resilience.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-attunement.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-awakening.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-resilience.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-vigor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-channeling.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-fury.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-channeling.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-fury.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-insight.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-potency.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-insight.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-attunement.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-potency.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-corruption.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-strike.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-corruption.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-endurance.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-strike.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-potency.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-vigor.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-clarity.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-power.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-clarity.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-corruption.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-power.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-channeling.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-rite.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-channeling.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-rite.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-clarity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-dominion.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-wrath.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-dominion.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-insight.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-wrath.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-lore.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-lore.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-force.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-attunement.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-force.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-dominion.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-attunement.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/unholy-rite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-sorcery.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-sorcery.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-clarity.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-might.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-clarity.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/unholy-vigor.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-might.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-sacrifice.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-fury.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-sacrifice.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-fury.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-covenant.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-endurance.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-covenant.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-endurance.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/unholy-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-power.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-strike.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-power.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-strike.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/unholy-clarity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-fortitude.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-covenant.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-fortitude.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-covenant.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-mastery.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-mastery.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/adept-transcendence.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-corruption.c");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/unholy-lore.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/adept-transcendence.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-corruption.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/unholy-endurance.c");
    addChild("/guilds/disciple-of-argloth/unholy-ascendance/unholy-lore.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/dark-ascendance.c");

    addChild("/guilds/disciple-of-argloth/unholy-ascendance/dark-ascendance.c",
        "/guilds/disciple-of-argloth/unholy-ascendance/adept-force.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Unholy Ascendance");
    Description("The rites and granted powers of Argloth's necromancer adepts - those who have mastered the basics and begun their ascent toward true power in the blood covenant.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/unholy-ascendance/root.c");
    TreeRoot("/guilds/disciple-of-argloth/unholy-ascendance/root.c");

    FirstLevel();
    TwentiethLevel();
    TwentyFirstLevel();
    TwentySecondLevel();
    TwentyThirdLevel();
    TwentyFourthLevel();
    TwentyFifthLevel();
    TwentySixthLevel();
    TwentySeventhLevel();
    TwentyEighthLevel();
    TwentyNinthLevel();
    ThirtiethLevel();
    ThirtyFirstLevel();
    ThirtySecondLevel();
    ThirtyThirdLevel();
    ThirtyFourthLevel();
    ThirtyFifthLevel();
    ThirtySixthLevel();
    ThirtySeventhLevel();
    ThirtyEighthLevel();
    ThirtyNinthLevel();
}
