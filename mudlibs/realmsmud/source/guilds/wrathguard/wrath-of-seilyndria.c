//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

private void FirstLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/shadow-attunement.c");
    addResearchElement("/guilds/wrathguard/wrath/shadow-bolt.c");
    addChild("/guilds/wrathguard/wrath/shadow-attunement.c",
        "/guilds/wrathguard/wrath/shadow-magic-root.c");
    addChild("/guilds/wrathguard/wrath/shadow-bolt.c",
        "/guilds/wrathguard/wrath/shadow-magic-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/void-bolt-mastery.c");
    addResearchElement("/guilds/wrathguard/wrath/void-resilience.c");
    addResearchElement("/guilds/wrathguard/wrath/veil-of-night.c");
    addChild("/guilds/wrathguard/wrath/void-bolt-mastery.c",
        "/guilds/wrathguard/wrath/shadow-attunement.c");
    addChild("/guilds/wrathguard/wrath/void-resilience.c",
        "/guilds/wrathguard/wrath/shadow-attunement.c");
    addChild("/guilds/wrathguard/wrath/veil-of-night.c",
        "/guilds/wrathguard/wrath/shadow-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/shadow-strike-mastery.c");
    addResearchElement("/guilds/wrathguard/wrath/void-veil-mastery.c");
    addResearchElement("/guilds/wrathguard/wrath/dark-mind.c");
    addResearchElement("/guilds/wrathguard/wrath/void-strike.c");
    addChild("/guilds/wrathguard/wrath/shadow-strike-mastery.c",
        "/guilds/wrathguard/wrath/void-bolt-mastery.c");
    addChild("/guilds/wrathguard/wrath/void-veil-mastery.c",
        "/guilds/wrathguard/wrath/void-bolt-mastery.c");
    addChild("/guilds/wrathguard/wrath/dark-mind.c",
        "/guilds/wrathguard/wrath/void-resilience.c");
    addChild("/guilds/wrathguard/wrath/void-strike.c",
        "/guilds/wrathguard/wrath/veil-of-night.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/dark-resonance.c");
    addResearchElement("/guilds/wrathguard/wrath/shadow-corruption.c");
    addResearchElement("/guilds/wrathguard/wrath/twilight-skin.c");
    addResearchElement("/guilds/wrathguard/wrath/shadow-corruption-active.c");
    addChild("/guilds/wrathguard/wrath/dark-resonance.c",
        "/guilds/wrathguard/wrath/shadow-strike-mastery.c");
    addChild("/guilds/wrathguard/wrath/shadow-corruption.c",
        "/guilds/wrathguard/wrath/void-veil-mastery.c");
    addChild("/guilds/wrathguard/wrath/twilight-skin.c",
        "/guilds/wrathguard/wrath/dark-mind.c");
    addChild("/guilds/wrathguard/wrath/shadow-corruption-active.c",
        "/guilds/wrathguard/wrath/void-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/umbral-focus.c");
    addResearchElement("/guilds/wrathguard/wrath/twilight-mastery.c");
    addResearchElement("/guilds/wrathguard/wrath/umbral-senses.c");
    addResearchElement("/guilds/wrathguard/wrath/umbral-lance.c");
    addChild("/guilds/wrathguard/wrath/umbral-focus.c",
        "/guilds/wrathguard/wrath/dark-resonance.c");
    addChild("/guilds/wrathguard/wrath/twilight-mastery.c",
        "/guilds/wrathguard/wrath/shadow-corruption.c");
    addChild("/guilds/wrathguard/wrath/umbral-senses.c",
        "/guilds/wrathguard/wrath/twilight-skin.c");
    addChild("/guilds/wrathguard/wrath/umbral-lance.c",
        "/guilds/wrathguard/wrath/shadow-corruption-active.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/seilyndrias-reach.c");
    addResearchElement("/guilds/wrathguard/wrath/dark-shroud-mastery.c");
    addResearchElement("/guilds/wrathguard/wrath/shadow-ward.c");
    addResearchElement("/guilds/wrathguard/wrath/dark-tendrils.c");
    addChild("/guilds/wrathguard/wrath/seilyndrias-reach.c",
        "/guilds/wrathguard/wrath/umbral-focus.c");
    addChild("/guilds/wrathguard/wrath/dark-shroud-mastery.c",
        "/guilds/wrathguard/wrath/twilight-mastery.c");
    addChild("/guilds/wrathguard/wrath/shadow-ward.c",
        "/guilds/wrathguard/wrath/umbral-senses.c");
    addChild("/guilds/wrathguard/wrath/dark-tendrils.c",
        "/guilds/wrathguard/wrath/umbral-lance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/entropy-focus.c");
    addResearchElement("/guilds/wrathguard/wrath/void-potency.c");
    addResearchElement("/guilds/wrathguard/wrath/shadow-blind.c");
    addChild("/guilds/wrathguard/wrath/entropy-focus.c",
        "/guilds/wrathguard/wrath/dark-shroud-mastery.c");
    addChild("/guilds/wrathguard/wrath/void-potency.c",
        "/guilds/wrathguard/wrath/shadow-ward.c");
    addChild("/guilds/wrathguard/wrath/shadow-blind.c",
        "/guilds/wrathguard/wrath/dark-tendrils.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/darkness-embrace.c");
    addResearchElement("/guilds/wrathguard/wrath/void-pulse.c");
    addResearchElement("/guilds/wrathguard/wrath/twilight-shroud.c");
    addChild("/guilds/wrathguard/wrath/darkness-embrace.c",
        "/guilds/wrathguard/wrath/void-potency.c");
    addChild("/guilds/wrathguard/wrath/void-pulse.c",
        "/guilds/wrathguard/wrath/entropy-focus.c");
    addChild("/guilds/wrathguard/wrath/twilight-shroud.c",
        "/guilds/wrathguard/wrath/shadow-blind.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/shadow-vigor.c");
    addResearchElement("/guilds/wrathguard/wrath/shadow-drain.c");
    addChild("/guilds/wrathguard/wrath/shadow-vigor.c",
        "/guilds/wrathguard/wrath/darkness-embrace.c");
    addChild("/guilds/wrathguard/wrath/shadow-drain.c",
        "/guilds/wrathguard/wrath/twilight-shroud.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/void-endurance.c");
    addResearchElement("/guilds/wrathguard/wrath/darkness-wave.c");
    addChild("/guilds/wrathguard/wrath/void-endurance.c",
        "/guilds/wrathguard/wrath/shadow-vigor.c");
    addChild("/guilds/wrathguard/wrath/darkness-wave.c",
        "/guilds/wrathguard/wrath/shadow-drain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/seilyndrias-blessing.c");
    addResearchElement("/guilds/wrathguard/wrath/void-shatter.c");
    addChild("/guilds/wrathguard/wrath/seilyndrias-blessing.c",
        "/guilds/wrathguard/wrath/void-endurance.c");
    addChild("/guilds/wrathguard/wrath/void-shatter.c",
        "/guilds/wrathguard/wrath/darkness-wave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/shadow-fortitude.c");
    addResearchElement("/guilds/wrathguard/wrath/seilyndrias-gaze.c");
    addChild("/guilds/wrathguard/wrath/shadow-fortitude.c",
        "/guilds/wrathguard/wrath/seilyndrias-blessing.c");
    addChild("/guilds/wrathguard/wrath/seilyndrias-gaze.c",
        "/guilds/wrathguard/wrath/void-shatter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/umbral-power.c");
    addResearchElement("/guilds/wrathguard/wrath/umbral-storm.c");
    addChild("/guilds/wrathguard/wrath/umbral-power.c",
        "/guilds/wrathguard/wrath/shadow-fortitude.c");
    addChild("/guilds/wrathguard/wrath/umbral-storm.c",
        "/guilds/wrathguard/wrath/seilyndrias-gaze.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/dark-constitution.c");
    addChild("/guilds/wrathguard/wrath/dark-constitution.c",
        "/guilds/wrathguard/wrath/umbral-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/shadow-annihilation.c");
    addChild("/guilds/wrathguard/wrath/shadow-annihilation.c",
        "/guilds/wrathguard/wrath/umbral-storm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/wrath/void-mastery.c");
    addChild("/guilds/wrathguard/wrath/void-mastery.c",
        "/guilds/wrathguard/wrath/dark-constitution.c");
}
protected void Setup()
{
    Name("Shadow Magic");
    Description("This research tree provides knowledge of shadow and void "
        "magic - the darkness that precedes Seilyndria's ruin. Through study "
        "of the spaces between light, practitioners learn to hurl bolts of "
        "void energy, corrupt their enemies with shadow, and ultimately "
        "command the darkness that falls before the goddess herself.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/wrath/shadow-magic-root.c");
    TreeRoot("/guilds/wrathguard/wrath/shadow-magic-root.c");
    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    NineteenthLevel();
    TwentyThirdLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyFifthLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortySeventhLevel();
}