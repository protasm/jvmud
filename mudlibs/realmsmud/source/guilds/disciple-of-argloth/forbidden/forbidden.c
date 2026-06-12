//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-sacrifice.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/dark-ritual.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/flesh-pact.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-sacrifice.c",
        "/guilds/disciple-of-argloth/forbidden/root.c");
    addChild("/guilds/disciple-of-argloth/forbidden/dark-ritual.c",
        "/guilds/disciple-of-argloth/forbidden/root.c");
    addChild("/guilds/disciple-of-argloth/forbidden/flesh-pact.c",
        "/guilds/disciple-of-argloth/forbidden/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/essence-siphon.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/life-tap.c");

    addChild("/guilds/disciple-of-argloth/forbidden/essence-siphon.c",
        "/guilds/disciple-of-argloth/forbidden/flesh-pact.c");
    addChild("/guilds/disciple-of-argloth/forbidden/life-tap.c",
        "/guilds/disciple-of-argloth/forbidden/blood-sacrifice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/necrotic-sacrifice.c");

    addChild("/guilds/disciple-of-argloth/forbidden/necrotic-sacrifice.c",
        "/guilds/disciple-of-argloth/forbidden/dark-ritual.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-rite.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-rite.c",
        "/guilds/disciple-of-argloth/forbidden/life-tap.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/forbidden-knowledge.c");

    addChild("/guilds/disciple-of-argloth/forbidden/forbidden-knowledge.c",
        "/guilds/disciple-of-argloth/forbidden/essence-siphon.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/dark-offering.c");

    addChild("/guilds/disciple-of-argloth/forbidden/dark-offering.c",
        "/guilds/disciple-of-argloth/forbidden/necrotic-sacrifice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/soul-harvest.c");

    addChild("/guilds/disciple-of-argloth/forbidden/soul-harvest.c",
        "/guilds/disciple-of-argloth/forbidden/blood-rite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-corruption.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-corruption.c",
        "/guilds/disciple-of-argloth/forbidden/forbidden-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/rite-of-binding.c");

    addChild("/guilds/disciple-of-argloth/forbidden/rite-of-binding.c",
        "/guilds/disciple-of-argloth/forbidden/dark-offering.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/consuming-flame.c");

    addChild("/guilds/disciple-of-argloth/forbidden/consuming-flame.c",
        "/guilds/disciple-of-argloth/forbidden/soul-harvest.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/rite-of-unmaking.c");

    addChild("/guilds/disciple-of-argloth/forbidden/rite-of-unmaking.c",
        "/guilds/disciple-of-argloth/forbidden/blood-corruption.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/forbidden-power.c");

    addChild("/guilds/disciple-of-argloth/forbidden/forbidden-power.c",
        "/guilds/disciple-of-argloth/forbidden/rite-of-binding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-transcendence.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/death-transcendence.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-transcendence.c",
        "/guilds/disciple-of-argloth/forbidden/consuming-flame.c");
    addChild("/guilds/disciple-of-argloth/forbidden/death-transcendence.c",
        "/guilds/disciple-of-argloth/forbidden/rite-of-unmaking.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/void-transcendence.c");

    addChild("/guilds/disciple-of-argloth/forbidden/void-transcendence.c",
        "/guilds/disciple-of-argloth/forbidden/forbidden-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/rite-of-argloth.c");

    addChild("/guilds/disciple-of-argloth/forbidden/rite-of-argloth.c",
        "/guilds/disciple-of-argloth/forbidden/blood-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-heresy.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/forbidden-resilience.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/ritual-mastery.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-heresy.c",
        "/guilds/disciple-of-argloth/forbidden/blood-transcendence.c");
    addChild("/guilds/disciple-of-argloth/forbidden/forbidden-resilience.c",
        "/guilds/disciple-of-argloth/forbidden/void-transcendence.c");
    addChild("/guilds/disciple-of-argloth/forbidden/ritual-mastery.c",
        "/guilds/disciple-of-argloth/forbidden/death-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/corruption-mastery.c");

    addChild("/guilds/disciple-of-argloth/forbidden/corruption-mastery.c",
        "/guilds/disciple-of-argloth/forbidden/void-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/sacrifice-mastery.c");

    addChild("/guilds/disciple-of-argloth/forbidden/sacrifice-mastery.c",
        "/guilds/disciple-of-argloth/forbidden/rite-of-argloth.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/forbidden-amplification.c");

    addChild("/guilds/disciple-of-argloth/forbidden/forbidden-amplification.c",
        "/guilds/disciple-of-argloth/forbidden/ritual-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-void.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/ultimate-corruption.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-void.c",
        "/guilds/disciple-of-argloth/forbidden/sacrifice-mastery.c");
    addChild("/guilds/disciple-of-argloth/forbidden/ultimate-corruption.c",
        "/guilds/disciple-of-argloth/forbidden/corruption-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-annihilation.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/dark-apotheosis.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-annihilation.c",
        "/guilds/disciple-of-argloth/forbidden/blood-void.c");
    addChild("/guilds/disciple-of-argloth/forbidden/dark-apotheosis.c",
        "/guilds/disciple-of-argloth/forbidden/forbidden-amplification.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/rite-of-ruin.c");

    addChild("/guilds/disciple-of-argloth/forbidden/rite-of-ruin.c",
        "/guilds/disciple-of-argloth/forbidden/ultimate-corruption.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/forbidden-mastery.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/the-last-rite.c");

    addChild("/guilds/disciple-of-argloth/forbidden/forbidden-mastery.c",
        "/guilds/disciple-of-argloth/forbidden/dark-apotheosis.c");
    addChild("/guilds/disciple-of-argloth/forbidden/the-last-rite.c",
        "/guilds/disciple-of-argloth/forbidden/blood-void.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/argloth-manifest.c");

    addChild("/guilds/disciple-of-argloth/forbidden/argloth-manifest.c",
        "/guilds/disciple-of-argloth/forbidden/dark-apotheosis.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/void-obliteration.c");

    addChild("/guilds/disciple-of-argloth/forbidden/void-obliteration.c",
        "/guilds/disciple-of-argloth/forbidden/rite-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/blood-armageddon.c");

    addChild("/guilds/disciple-of-argloth/forbidden/blood-armageddon.c",
        "/guilds/disciple-of-argloth/forbidden/argloth-manifest.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/absolute-ending.c");

    addChild("/guilds/disciple-of-argloth/forbidden/absolute-ending.c",
        "/guilds/disciple-of-argloth/forbidden/the-last-rite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/the-covenant-complete.c");

    addChild("/guilds/disciple-of-argloth/forbidden/the-covenant-complete.c",
        "/guilds/disciple-of-argloth/forbidden/void-obliteration.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/forbidden/argloth-will.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/end-of-all-things.c");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/the-blood-god-speaks.c");

    addChild("/guilds/disciple-of-argloth/forbidden/argloth-will.c",
        "/guilds/disciple-of-argloth/forbidden/the-covenant-complete.c");
    addChild("/guilds/disciple-of-argloth/forbidden/end-of-all-things.c",
        "/guilds/disciple-of-argloth/forbidden/blood-armageddon.c");
    addChild("/guilds/disciple-of-argloth/forbidden/the-blood-god-speaks.c",
        "/guilds/disciple-of-argloth/forbidden/end-of-all-things.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Forbidden Arts");
    Description("The most forbidden of Argloth's techniques - those he kept secret even from his own disciples, techniques that unmake the boundary between life and death entirely.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/forbidden/root.c");
    TreeRoot("/guilds/disciple-of-argloth/forbidden/root.c");

    FifteenthLevel();
    EighteenthLevel();
    TwentiethLevel();
    TwentySecondLevel();
    TwentyFourthLevel();
    TwentySixthLevel();
    TwentyEighthLevel();
    ThirtiethLevel();
    ThirtySecondLevel();
    ThirtyFourthLevel();
    ThirtySixthLevel();
    ThirtyEighthLevel();
    FortiethLevel();
    FortySecondLevel();
    FortyFourthLevel();
    FortySixthLevel();
    FortyEighthLevel();
    FiftiethLevel();
    FiftySecondLevel();
    FiftyFifthLevel();
    FiftyEighthLevel();
    SixtiethLevel();
    SixtySecondLevel();
    SixtyFourthLevel();
    SixtyFifthLevel();
    SixtySixthLevel();
    SixtySeventhLevel();
    SixtyEighthLevel();
    SeventiethLevel();
}
