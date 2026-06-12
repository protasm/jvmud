//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

private void FirstLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-attunement.c");
    addResearchElement("/guilds/wrathguard/destruction/shatter.c");
    addChild("/guilds/wrathguard/destruction/ruin-attunement.c",
        "/guilds/wrathguard/destruction/ruin-root.c");
    addChild("/guilds/wrathguard/destruction/shatter.c",
        "/guilds/wrathguard/destruction/ruin-root.c");
}
private void ThirdLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-bolt-mastery.c");
    addResearchElement("/guilds/wrathguard/destruction/ruinous-resilience.c");
    addResearchElement("/guilds/wrathguard/destruction/unmake.c");
    addChild("/guilds/wrathguard/destruction/ruin-bolt-mastery.c",
        "/guilds/wrathguard/destruction/ruin-attunement.c");
    addChild("/guilds/wrathguard/destruction/ruinous-resilience.c",
        "/guilds/wrathguard/destruction/ruin-attunement.c");
    addChild("/guilds/wrathguard/destruction/unmake.c",
        "/guilds/wrathguard/destruction/shatter.c");
}
private void FifthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/devastation-mastery.c");
    addResearchElement("/guilds/wrathguard/destruction/reduce-to-ash-mastery.c");
    addResearchElement("/guilds/wrathguard/destruction/dark-fortitude.c");
    addResearchElement("/guilds/wrathguard/destruction/reduce-to-ash.c");
    addChild("/guilds/wrathguard/destruction/devastation-mastery.c",
        "/guilds/wrathguard/destruction/ruin-bolt-mastery.c");
    addChild("/guilds/wrathguard/destruction/reduce-to-ash-mastery.c",
        "/guilds/wrathguard/destruction/ruin-bolt-mastery.c");
    addChild("/guilds/wrathguard/destruction/dark-fortitude.c",
        "/guilds/wrathguard/destruction/ruinous-resilience.c");
    addChild("/guilds/wrathguard/destruction/reduce-to-ash.c",
        "/guilds/wrathguard/destruction/unmake.c");
}
private void SeventhLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruinous-impact.c");
    addResearchElement("/guilds/wrathguard/destruction/unmaking-mastery.c");
    addResearchElement("/guilds/wrathguard/destruction/ruin-vigor.c");
    addResearchElement("/guilds/wrathguard/destruction/caustic-touch.c");
    addChild("/guilds/wrathguard/destruction/ruinous-impact.c",
        "/guilds/wrathguard/destruction/devastation-mastery.c");
    addChild("/guilds/wrathguard/destruction/unmaking-mastery.c",
        "/guilds/wrathguard/destruction/reduce-to-ash-mastery.c");
    addChild("/guilds/wrathguard/destruction/ruin-vigor.c",
        "/guilds/wrathguard/destruction/dark-fortitude.c");
    addChild("/guilds/wrathguard/destruction/caustic-touch.c",
        "/guilds/wrathguard/destruction/reduce-to-ash.c");
}
private void NinthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/seilyndrias-wrath.c");
    addResearchElement("/guilds/wrathguard/destruction/annihilating-mastery.c");
    addResearchElement("/guilds/wrathguard/destruction/ruin-endurance.c");
    addResearchElement("/guilds/wrathguard/destruction/devastate.c");
    addChild("/guilds/wrathguard/destruction/seilyndrias-wrath.c",
        "/guilds/wrathguard/destruction/ruinous-impact.c");
    addChild("/guilds/wrathguard/destruction/annihilating-mastery.c",
        "/guilds/wrathguard/destruction/unmaking-mastery.c");
    addChild("/guilds/wrathguard/destruction/ruin-endurance.c",
        "/guilds/wrathguard/destruction/ruin-vigor.c");
    addChild("/guilds/wrathguard/destruction/devastate.c",
        "/guilds/wrathguard/destruction/caustic-touch.c");
}
private void EleventhLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/obliterating-mastery.c");
    addResearchElement("/guilds/wrathguard/destruction/ruin-mind.c");
    addResearchElement("/guilds/wrathguard/destruction/acid-wave.c");
    addChild("/guilds/wrathguard/destruction/obliterating-mastery.c",
        "/guilds/wrathguard/destruction/annihilating-mastery.c");
    addChild("/guilds/wrathguard/destruction/ruin-mind.c",
        "/guilds/wrathguard/destruction/ruin-endurance.c");
    addChild("/guilds/wrathguard/destruction/acid-wave.c",
        "/guilds/wrathguard/destruction/devastate.c");
}
private void ThirteenthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-ward.c");
    addResearchElement("/guilds/wrathguard/destruction/ruinous-surge.c");
    addChild("/guilds/wrathguard/destruction/ruin-ward.c",
        "/guilds/wrathguard/destruction/ruin-mind.c");
    addChild("/guilds/wrathguard/destruction/ruinous-surge.c",
        "/guilds/wrathguard/destruction/acid-wave.c");
}
private void FifteenthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-potency.c");
    addResearchElement("/guilds/wrathguard/destruction/ruin-strike.c");
    addResearchElement("/guilds/wrathguard/destruction/corrosive-burst.c");
    addChild("/guilds/wrathguard/destruction/ruin-potency.c",
        "/guilds/wrathguard/destruction/ruin-ward.c");
    addChild("/guilds/wrathguard/destruction/ruin-strike.c",
        "/guilds/wrathguard/destruction/ruinous-surge.c");
    addChild("/guilds/wrathguard/destruction/corrosive-burst.c",
        "/guilds/wrathguard/destruction/ruinous-surge.c");
}
private void SeventeenthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-embrace.c");
    addResearchElement("/guilds/wrathguard/destruction/wall-of-ruin.c");
    addChild("/guilds/wrathguard/destruction/ruin-embrace.c",
        "/guilds/wrathguard/destruction/ruin-potency.c");
    addChild("/guilds/wrathguard/destruction/wall-of-ruin.c",
        "/guilds/wrathguard/destruction/ruin-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-heart.c");
    addResearchElement("/guilds/wrathguard/destruction/ruinous-cascade.c");
    addChild("/guilds/wrathguard/destruction/ruin-heart.c",
        "/guilds/wrathguard/destruction/ruin-embrace.c");
    addChild("/guilds/wrathguard/destruction/ruinous-cascade.c",
        "/guilds/wrathguard/destruction/wall-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/the-queens-ruin.c");
    addResearchElement("/guilds/wrathguard/destruction/absolute-devastation.c");
    addChild("/guilds/wrathguard/destruction/the-queens-ruin.c",
        "/guilds/wrathguard/destruction/ruin-heart.c");
    addChild("/guilds/wrathguard/destruction/absolute-devastation.c",
        "/guilds/wrathguard/destruction/ruinous-cascade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-constitution.c");
    addResearchElement("/guilds/wrathguard/destruction/unmaking-strike.c");
    addChild("/guilds/wrathguard/destruction/ruin-constitution.c",
        "/guilds/wrathguard/destruction/the-queens-ruin.c");
    addChild("/guilds/wrathguard/destruction/unmaking-strike.c",
        "/guilds/wrathguard/destruction/absolute-devastation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-mastery.c");
    addResearchElement("/guilds/wrathguard/destruction/world-unmade.c");
    addChild("/guilds/wrathguard/destruction/ruin-mastery.c",
        "/guilds/wrathguard/destruction/ruin-constitution.c");
    addChild("/guilds/wrathguard/destruction/world-unmade.c",
        "/guilds/wrathguard/destruction/unmaking-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-power.c");
    addResearchElement("/guilds/wrathguard/destruction/obliterating-force.c");
    addChild("/guilds/wrathguard/destruction/ruin-power.c",
        "/guilds/wrathguard/destruction/ruin-mastery.c");
    addChild("/guilds/wrathguard/destruction/obliterating-force.c",
        "/guilds/wrathguard/destruction/world-unmade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/ruin-nova.c");
    addChild("/guilds/wrathguard/destruction/ruin-nova.c",
        "/guilds/wrathguard/destruction/world-unmade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/destruction/seilyndrias-annihilation.c");
    addChild("/guilds/wrathguard/destruction/seilyndrias-annihilation.c",
        "/guilds/wrathguard/destruction/ruin-power.c");
}
protected void Setup()
{
    Name("Ruin and Devastation");
    Description("This research tree embodies Seilyndria's essence of "
        "destruction and ruin - the unmaking of what others have built. "
        "Through devoted study, the Wrathguard learns to shatter, unmake, "
        "and reduce enemies and their works to ash.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/destruction/ruin-root.c");
    TreeRoot("/guilds/wrathguard/destruction/ruin-root.c");
    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    TwentyFirstLevel();
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyFifthLevel();
}