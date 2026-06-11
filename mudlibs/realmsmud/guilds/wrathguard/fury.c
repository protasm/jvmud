//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ritual-power-mastery.c");
    addChild("/guilds/wrathguard/rituals/ritual-power-mastery.c",
        "/guilds/wrathguard/rituals/keepers-rituals-root.c");

    addResearchElement("/guilds/wrathguard/rituals/blood-rite-mastery.c");
    addChild("/guilds/wrathguard/rituals/blood-rite-mastery.c",
        "/guilds/wrathguard/rituals/keepers-rituals-root.c");

    addResearchElement("/guilds/wrathguard/rituals/ritual-attunement.c");
    addChild("/guilds/wrathguard/rituals/ritual-attunement.c",
        "/guilds/wrathguard/rituals/keepers-rituals-root.c");

    addResearchElement("/guilds/wrathguard/rituals/sacred-knowledge.c");
    addChild("/guilds/wrathguard/rituals/sacred-knowledge.c",
        "/guilds/wrathguard/rituals/keepers-rituals-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/blood-rite.c");
    addChild("/guilds/wrathguard/rituals/blood-rite.c",
        "/guilds/wrathguard/rituals/ritual-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/sacred-mind.c");
    addChild("/guilds/wrathguard/rituals/sacred-mind.c",
        "/guilds/wrathguard/rituals/ritual-attunement.c");

    addResearchElement("/guilds/wrathguard/rituals/blood-ward.c");
    addChild("/guilds/wrathguard/rituals/blood-ward.c",
        "/guilds/wrathguard/rituals/sacred-knowledge.c");

    addResearchElement("/guilds/wrathguard/rituals/ancient-wisdom.c");
    addChild("/guilds/wrathguard/rituals/ancient-wisdom.c",
        "/guilds/wrathguard/rituals/sacred-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/dark-invocation.c");
    addChild("/guilds/wrathguard/rituals/dark-invocation.c",
        "/guilds/wrathguard/rituals/blood-rite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/dark-ceremony-mastery.c");
    addChild("/guilds/wrathguard/rituals/dark-ceremony-mastery.c",
        "/guilds/wrathguard/rituals/ritual-power-mastery.c");

    addResearchElement("/guilds/wrathguard/rituals/sacred-blood-amplifier.c");
    addChild("/guilds/wrathguard/rituals/sacred-blood-amplifier.c",
        "/guilds/wrathguard/rituals/blood-rite-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ritual-resilience.c");
    addChild("/guilds/wrathguard/rituals/ritual-resilience.c",
        "/guilds/wrathguard/rituals/sacred-mind.c");

    addResearchElement("/guilds/wrathguard/rituals/ritual-skin.c");
    addChild("/guilds/wrathguard/rituals/ritual-skin.c",
        "/guilds/wrathguard/rituals/blood-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ritual-strike.c");
    addChild("/guilds/wrathguard/rituals/ritual-strike.c",
        "/guilds/wrathguard/rituals/dark-invocation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/dark-ceremony.c");
    addChild("/guilds/wrathguard/rituals/dark-ceremony.c",
        "/guilds/wrathguard/rituals/ritual-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ritual-vigor.c");
    addChild("/guilds/wrathguard/rituals/ritual-vigor.c",
        "/guilds/wrathguard/rituals/dark-ceremony.c");

    addResearchElement("/guilds/wrathguard/rituals/ritual-endurance.c");
    addChild("/guilds/wrathguard/rituals/ritual-endurance.c",
        "/guilds/wrathguard/rituals/ritual-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ceremony-of-pain.c");
    addChild("/guilds/wrathguard/rituals/ceremony-of-pain.c",
        "/guilds/wrathguard/rituals/ritual-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/blood-constitution.c");
    addChild("/guilds/wrathguard/rituals/blood-constitution.c",
        "/guilds/wrathguard/rituals/ritual-vigor.c");

    addResearchElement("/guilds/wrathguard/rituals/rites-of-power.c");
    addChild("/guilds/wrathguard/rituals/rites-of-power.c",
        "/guilds/wrathguard/rituals/ritual-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ancient-rite-mastery.c");
    addChild("/guilds/wrathguard/rituals/ancient-rite-mastery.c",
        "/guilds/wrathguard/rituals/dark-ceremony-mastery.c");

    addResearchElement("/guilds/wrathguard/rituals/profane-amplifier.c");
    addChild("/guilds/wrathguard/rituals/profane-amplifier.c",
        "/guilds/wrathguard/rituals/sacred-blood-amplifier.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/rite-of-binding.c");
    addChild("/guilds/wrathguard/rituals/rite-of-binding.c",
        "/guilds/wrathguard/rituals/ceremony-of-pain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ritual-potency.c");
    addChild("/guilds/wrathguard/rituals/ritual-potency.c",
        "/guilds/wrathguard/rituals/rites-of-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/blood-sacrifice.c");
    addChild("/guilds/wrathguard/rituals/blood-sacrifice.c",
        "/guilds/wrathguard/rituals/rite-of-binding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ritual-power.c");
    addChild("/guilds/wrathguard/rituals/ritual-power.c",
        "/guilds/wrathguard/rituals/ritual-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/void-ritual-mastery.c");
    addChild("/guilds/wrathguard/rituals/void-ritual-mastery.c",
        "/guilds/wrathguard/rituals/ancient-rite-mastery.c");

    addResearchElement("/guilds/wrathguard/rituals/blood-sovereign-mastery.c");
    addChild("/guilds/wrathguard/rituals/blood-sovereign-mastery.c",
        "/guilds/wrathguard/rituals/profane-amplifier.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ancient-rite.c");
    addChild("/guilds/wrathguard/rituals/ancient-rite.c",
        "/guilds/wrathguard/rituals/blood-sacrifice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/ritual-mastery.c");
    addChild("/guilds/wrathguard/rituals/ritual-mastery.c",
        "/guilds/wrathguard/rituals/ritual-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/seilyndrias-ritual-mastery.c");
    addChild("/guilds/wrathguard/rituals/seilyndrias-ritual-mastery.c",
        "/guilds/wrathguard/rituals/void-ritual-mastery.c");

    addResearchElement("/guilds/wrathguard/rituals/queens-blood-mastery.c");
    addChild("/guilds/wrathguard/rituals/queens-blood-mastery.c",
        "/guilds/wrathguard/rituals/blood-sovereign-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/dark-sacrament.c");
    addChild("/guilds/wrathguard/rituals/dark-sacrament.c",
        "/guilds/wrathguard/rituals/ancient-rite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/void-ritual.c");
    addChild("/guilds/wrathguard/rituals/void-ritual.c",
        "/guilds/wrathguard/rituals/dark-sacrament.c");

    addResearchElement("/guilds/wrathguard/rituals/rite-of-ruin.c");
    addChild("/guilds/wrathguard/rituals/rite-of-ruin.c",
        "/guilds/wrathguard/rituals/ritual-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/seilyndrias-rite.c");
    addChild("/guilds/wrathguard/rituals/seilyndrias-rite.c",
        "/guilds/wrathguard/rituals/void-ritual.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/keepers-ritual.c");
    addChild("/guilds/wrathguard/rituals/keepers-ritual.c",
        "/guilds/wrathguard/rituals/seilyndrias-rite.c");

    addResearchElement("/guilds/wrathguard/rituals/ritual-annihilation.c");
    addChild("/guilds/wrathguard/rituals/ritual-annihilation.c",
        "/guilds/wrathguard/rituals/rite-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/wrathguard/rituals/blood-of-the-queen.c");
    addChild("/guilds/wrathguard/rituals/blood-of-the-queen.c",
        "/guilds/wrathguard/rituals/keepers-ritual.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Keeper's Rituals");
    Description("The ancient blood rites and dark ceremonies of Seilyndria's "
        "chosen, handed down through the Wrathguard's most devoted servants. "
        "Each ritual draws on sacred and profane power alike to inflict "
        "terrible suffering upon those who oppose the Queen.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/rituals/keepers-rituals-root.c");
    TreeRoot("/guilds/wrathguard/rituals/keepers-rituals-root.c");

    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    EleventhLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtiethLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortyNinthLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    SixtyFirstLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SeventhiethLevel();
}
