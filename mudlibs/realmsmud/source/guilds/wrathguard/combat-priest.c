//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/wrathguard/combat/beguiling-strike-mastery.c");
    addChild("/guilds/wrathguard/combat/beguiling-strike-mastery.c",
        "/guilds/wrathguard/combat/vain-beauty-root.c");

    addResearchElement("/guilds/wrathguard/combat/charming-gaze-mastery.c");
    addChild("/guilds/wrathguard/combat/charming-gaze-mastery.c",
        "/guilds/wrathguard/combat/vain-beauty-root.c");

    addResearchElement("/guilds/wrathguard/combat/fascination-amplifier.c");
    addChild("/guilds/wrathguard/combat/fascination-amplifier.c",
        "/guilds/wrathguard/combat/beguiling-strike-mastery.c");

    addResearchElement("/guilds/wrathguard/combat/seduction-mastery.c");
    addChild("/guilds/wrathguard/combat/seduction-mastery.c",
        "/guilds/wrathguard/combat/charming-gaze-mastery.c");

    addResearchElement("/guilds/wrathguard/combat/domination-amplifier.c");
    addChild("/guilds/wrathguard/combat/domination-amplifier.c",
        "/guilds/wrathguard/combat/fascination-amplifier.c");

    addResearchElement("/guilds/wrathguard/combat/enthralling-mastery.c");
    addChild("/guilds/wrathguard/combat/enthralling-mastery.c",
        "/guilds/wrathguard/combat/seduction-mastery.c");

    addResearchElement("/guilds/wrathguard/combat/beauty-of-ruin-mastery.c");
    addChild("/guilds/wrathguard/combat/beauty-of-ruin-mastery.c",
        "/guilds/wrathguard/combat/domination-amplifier.c");

    addResearchElement("/guilds/wrathguard/combat/void-seduction-mastery.c");
    addChild("/guilds/wrathguard/combat/void-seduction-mastery.c",
        "/guilds/wrathguard/combat/enthralling-mastery.c");

    addResearchElement("/guilds/wrathguard/combat/queens-allure-mastery.c");
    addChild("/guilds/wrathguard/combat/queens-allure-mastery.c",
        "/guilds/wrathguard/combat/beauty-of-ruin-mastery.c");

    addResearchElement("/guilds/wrathguard/combat/perfect-domination.c");
    addChild("/guilds/wrathguard/combat/perfect-domination.c",
        "/guilds/wrathguard/combat/void-seduction-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/wrathguard/combat/beauty-attunement.c");
    addChild("/guilds/wrathguard/combat/beauty-attunement.c",
        "/guilds/wrathguard/combat/vain-beauty-root.c");

    addResearchElement("/guilds/wrathguard/combat/enchanting-presence.c");
    addChild("/guilds/wrathguard/combat/enchanting-presence.c",
        "/guilds/wrathguard/combat/beauty-attunement.c");

    addResearchElement("/guilds/wrathguard/combat/beguiling-words.c");
    addChild("/guilds/wrathguard/combat/beguiling-words.c",
        "/guilds/wrathguard/combat/enchanting-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/wrathguard/combat/alluring-gaze.c");
    addChild("/guilds/wrathguard/combat/alluring-gaze.c",
        "/guilds/wrathguard/combat/vain-beauty-root.c");

    addResearchElement("/guilds/wrathguard/combat/dark-charisma.c");
    addChild("/guilds/wrathguard/combat/dark-charisma.c",
        "/guilds/wrathguard/combat/beauty-attunement.c");

    addResearchElement("/guilds/wrathguard/combat/mesmerizing-touch.c");
    addChild("/guilds/wrathguard/combat/mesmerizing-touch.c",
        "/guilds/wrathguard/combat/alluring-gaze.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/wrathguard/combat/seductive-mind.c");
    addChild("/guilds/wrathguard/combat/seductive-mind.c",
        "/guilds/wrathguard/combat/dark-charisma.c");

    addResearchElement("/guilds/wrathguard/combat/beauty-ward.c");
    addChild("/guilds/wrathguard/combat/beauty-ward.c",
        "/guilds/wrathguard/combat/mesmerizing-touch.c");

    addResearchElement("/guilds/wrathguard/combat/captivating-voice.c");
    addChild("/guilds/wrathguard/combat/captivating-voice.c",
        "/guilds/wrathguard/combat/beguiling-words.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/wrathguard/combat/beauty-resilience.c");
    addChild("/guilds/wrathguard/combat/beauty-resilience.c",
        "/guilds/wrathguard/combat/seductive-mind.c");

    addResearchElement("/guilds/wrathguard/combat/irresistible-form.c");
    addChild("/guilds/wrathguard/combat/irresistible-form.c",
        "/guilds/wrathguard/combat/beauty-ward.c");

    addResearchElement("/guilds/wrathguard/combat/charming-strike.c");
    addChild("/guilds/wrathguard/combat/charming-strike.c",
        "/guilds/wrathguard/combat/captivating-voice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/wrathguard/combat/beauty-vigor.c");
    addChild("/guilds/wrathguard/combat/beauty-vigor.c",
        "/guilds/wrathguard/combat/beauty-resilience.c");

    addResearchElement("/guilds/wrathguard/combat/beauty-endurance.c");
    addChild("/guilds/wrathguard/combat/beauty-endurance.c",
        "/guilds/wrathguard/combat/irresistible-form.c");

    addResearchElement("/guilds/wrathguard/combat/fascinate.c");
    addChild("/guilds/wrathguard/combat/fascinate.c",
        "/guilds/wrathguard/combat/charming-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/wrathguard/combat/seilyndrias-allure.c");
    addChild("/guilds/wrathguard/combat/seilyndrias-allure.c",
        "/guilds/wrathguard/combat/beauty-vigor.c");

    addResearchElement("/guilds/wrathguard/combat/queens-beauty.c");
    addChild("/guilds/wrathguard/combat/queens-beauty.c",
        "/guilds/wrathguard/combat/beauty-endurance.c");

    addResearchElement("/guilds/wrathguard/combat/dominating-gaze.c");
    addChild("/guilds/wrathguard/combat/dominating-gaze.c",
        "/guilds/wrathguard/combat/fascinate.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/wrathguard/combat/beauty-potency.c");
    addChild("/guilds/wrathguard/combat/beauty-potency.c",
        "/guilds/wrathguard/combat/seilyndrias-allure.c");

    addResearchElement("/guilds/wrathguard/combat/enthrall.c");
    addChild("/guilds/wrathguard/combat/enthrall.c",
        "/guilds/wrathguard/combat/dominating-gaze.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/wrathguard/combat/beauty-power.c");
    addChild("/guilds/wrathguard/combat/beauty-power.c",
        "/guilds/wrathguard/combat/beauty-potency.c");

    addResearchElement("/guilds/wrathguard/combat/beauty-constitution.c");
    addChild("/guilds/wrathguard/combat/beauty-constitution.c",
        "/guilds/wrathguard/combat/queens-beauty.c");

    addResearchElement("/guilds/wrathguard/combat/kiss-of-ruin.c");
    addChild("/guilds/wrathguard/combat/kiss-of-ruin.c",
        "/guilds/wrathguard/combat/enthrall.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/wrathguard/combat/beauty-mastery.c");
    addChild("/guilds/wrathguard/combat/beauty-mastery.c",
        "/guilds/wrathguard/combat/beauty-power.c");

    addResearchElement("/guilds/wrathguard/combat/mind-shatter.c");
    addChild("/guilds/wrathguard/combat/mind-shatter.c",
        "/guilds/wrathguard/combat/kiss-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/combat/void-charm.c");
    addChild("/guilds/wrathguard/combat/void-charm.c",
        "/guilds/wrathguard/combat/mind-shatter.c");

    addResearchElement("/guilds/wrathguard/combat/seilyndrias-kiss.c");
    addChild("/guilds/wrathguard/combat/seilyndrias-kiss.c",
        "/guilds/wrathguard/combat/beauty-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/combat/the-queens-gaze.c");
    addChild("/guilds/wrathguard/combat/the-queens-gaze.c",
        "/guilds/wrathguard/combat/void-charm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/combat/seductive-annihilation.c");
    addChild("/guilds/wrathguard/combat/seductive-annihilation.c",
        "/guilds/wrathguard/combat/the-queens-gaze.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Vain Beauty");
    Description("The magic of Seilyndria's divine beauty, bending minds "
        "and breaking wills through charm, seduction, and domination.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/combat/vain-beauty-root.c");
    TreeRoot("/guilds/wrathguard/combat/vain-beauty-root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
}