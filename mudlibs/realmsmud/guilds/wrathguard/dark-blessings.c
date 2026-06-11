//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-bolt-mastery.c");
    addChild("/guilds/wrathguard/blessings/will-bolt-mastery.c",
        "/guilds/wrathguard/blessings/queens-will-root.c");

    addResearchElement("/guilds/wrathguard/blessings/compulsion-mastery.c");
    addChild("/guilds/wrathguard/blessings/compulsion-mastery.c",
        "/guilds/wrathguard/blessings/queens-will-root.c");

    addResearchElement("/guilds/wrathguard/blessings/will-attunement.c");
    addChild("/guilds/wrathguard/blessings/will-attunement.c",
        "/guilds/wrathguard/blessings/queens-will-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/iron-will.c");
    addChild("/guilds/wrathguard/blessings/iron-will.c",
        "/guilds/wrathguard/blessings/queens-will-root.c");

    addResearchElement("/guilds/wrathguard/blessings/divine-word.c");
    addChild("/guilds/wrathguard/blessings/divine-word.c",
        "/guilds/wrathguard/blessings/will-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/commanding-presence.c");
    addChild("/guilds/wrathguard/blessings/commanding-presence.c",
        "/guilds/wrathguard/blessings/will-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-mind.c");
    addChild("/guilds/wrathguard/blessings/will-mind.c",
        "/guilds/wrathguard/blessings/will-attunement.c");

    addResearchElement("/guilds/wrathguard/blessings/will-ward.c");
    addChild("/guilds/wrathguard/blessings/will-ward.c",
        "/guilds/wrathguard/blessings/iron-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/edict-amplifier.c");
    addChild("/guilds/wrathguard/blessings/edict-amplifier.c",
        "/guilds/wrathguard/blessings/will-bolt-mastery.c");

    addResearchElement("/guilds/wrathguard/blessings/subjugation-mastery.c");
    addChild("/guilds/wrathguard/blessings/subjugation-mastery.c",
        "/guilds/wrathguard/blessings/compulsion-mastery.c");

    addResearchElement("/guilds/wrathguard/blessings/command.c");
    addChild("/guilds/wrathguard/blessings/command.c",
        "/guilds/wrathguard/blessings/divine-word.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/domineering-spirit.c");
    addChild("/guilds/wrathguard/blessings/domineering-spirit.c",
        "/guilds/wrathguard/blessings/will-mind.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-resilience.c");
    addChild("/guilds/wrathguard/blessings/will-resilience.c",
        "/guilds/wrathguard/blessings/domineering-spirit.c");

    addResearchElement("/guilds/wrathguard/blessings/will-skin.c");
    addChild("/guilds/wrathguard/blessings/will-skin.c",
        "/guilds/wrathguard/blessings/will-ward.c");

    addResearchElement("/guilds/wrathguard/blessings/decree.c");
    addChild("/guilds/wrathguard/blessings/decree.c",
        "/guilds/wrathguard/blessings/command.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-vigor.c");
    addChild("/guilds/wrathguard/blessings/will-vigor.c",
        "/guilds/wrathguard/blessings/will-resilience.c");

    addResearchElement("/guilds/wrathguard/blessings/will-endurance.c");
    addChild("/guilds/wrathguard/blessings/will-endurance.c",
        "/guilds/wrathguard/blessings/will-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/shatter-will.c");
    addChild("/guilds/wrathguard/blessings/shatter-will.c",
        "/guilds/wrathguard/blessings/decree.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/divine-word-amplifier.c");
    addChild("/guilds/wrathguard/blessings/divine-word-amplifier.c",
        "/guilds/wrathguard/blessings/edict-amplifier.c");

    addResearchElement("/guilds/wrathguard/blessings/breaking-mastery.c");
    addChild("/guilds/wrathguard/blessings/breaking-mastery.c",
        "/guilds/wrathguard/blessings/subjugation-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/queens-authority.c");
    addChild("/guilds/wrathguard/blessings/queens-authority.c",
        "/guilds/wrathguard/blessings/will-vigor.c");

    addResearchElement("/guilds/wrathguard/blessings/will-of-steel.c");
    addChild("/guilds/wrathguard/blessings/will-of-steel.c",
        "/guilds/wrathguard/blessings/will-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/divine-compulsion.c");
    addChild("/guilds/wrathguard/blessings/divine-compulsion.c",
        "/guilds/wrathguard/blessings/shatter-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-potency.c");
    addChild("/guilds/wrathguard/blessings/will-potency.c",
        "/guilds/wrathguard/blessings/queens-authority.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-constitution.c");
    addChild("/guilds/wrathguard/blessings/will-constitution.c",
        "/guilds/wrathguard/blessings/will-of-steel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/absolute-command-mastery.c");
    addChild("/guilds/wrathguard/blessings/absolute-command-mastery.c",
        "/guilds/wrathguard/blessings/divine-word-amplifier.c");

    addResearchElement("/guilds/wrathguard/blessings/queens-decree-mastery.c");
    addChild("/guilds/wrathguard/blessings/queens-decree-mastery.c",
        "/guilds/wrathguard/blessings/breaking-mastery.c");

    addResearchElement("/guilds/wrathguard/blessings/bound-by-will.c");
    addChild("/guilds/wrathguard/blessings/bound-by-will.c",
        "/guilds/wrathguard/blessings/divine-compulsion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-power.c");
    addChild("/guilds/wrathguard/blessings/will-power.c",
        "/guilds/wrathguard/blessings/will-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/edict-of-pain.c");
    addChild("/guilds/wrathguard/blessings/edict-of-pain.c",
        "/guilds/wrathguard/blessings/bound-by-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/total-dominion-mastery.c");
    addChild("/guilds/wrathguard/blessings/total-dominion-mastery.c",
        "/guilds/wrathguard/blessings/absolute-command-mastery.c");

    addResearchElement("/guilds/wrathguard/blessings/seilyndrias-edict.c");
    addChild("/guilds/wrathguard/blessings/seilyndrias-edict.c",
        "/guilds/wrathguard/blessings/queens-decree-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/voice-of-ruin.c");
    addChild("/guilds/wrathguard/blessings/voice-of-ruin.c",
        "/guilds/wrathguard/blessings/edict-of-pain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/will-mastery.c");
    addChild("/guilds/wrathguard/blessings/will-mastery.c",
        "/guilds/wrathguard/blessings/will-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/queens-will-strike.c");
    addChild("/guilds/wrathguard/blessings/queens-will-strike.c",
        "/guilds/wrathguard/blessings/will-mastery.c");

    addResearchElement("/guilds/wrathguard/blessings/word-of-annihilation.c");
    addChild("/guilds/wrathguard/blessings/word-of-annihilation.c",
        "/guilds/wrathguard/blessings/voice-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/absolute-dominion.c");
    addChild("/guilds/wrathguard/blessings/absolute-dominion.c",
        "/guilds/wrathguard/blessings/word-of-annihilation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/the-queens-command.c");
    addChild("/guilds/wrathguard/blessings/the-queens-command.c",
        "/guilds/wrathguard/blessings/absolute-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/wrathguard/blessings/queens-will-annihilation.c");
    addChild("/guilds/wrathguard/blessings/queens-will-annihilation.c",
        "/guilds/wrathguard/blessings/the-queens-command.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("The Queen's Will");
    Description("The direct manifestation of Seilyndria's divine will, "
        "used to break minds, issue edicts of pain, and compel absolute "
        "obedience from enemies and servants alike.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/blessings/queens-will-root.c");
    TreeRoot("/guilds/wrathguard/blessings/queens-will-root.c");

    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    ThirteenthLevel();
    SeventeenthLevel();
    TwentyFirstLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtiethLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
    SeventhiethLevel();
}
