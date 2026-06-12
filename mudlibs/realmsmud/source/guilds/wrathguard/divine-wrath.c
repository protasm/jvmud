//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/wrathguard/herald/herald-form.c");
    addChild("/guilds/wrathguard/herald/herald-form.c",
        "/guilds/wrathguard/herald/herald-of-darkness-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/herald/shadow-presence.c");
    addChild("/guilds/wrathguard/herald/shadow-presence.c",
        "/guilds/wrathguard/herald/herald-form.c");

    addResearchElement("/guilds/wrathguard/herald/void-aura.c");
    addChild("/guilds/wrathguard/herald/void-aura.c",
        "/guilds/wrathguard/herald/herald-form.c");

    addResearchElement("/guilds/wrathguard/herald/herald-ward.c");
    addChild("/guilds/wrathguard/herald/herald-ward.c",
        "/guilds/wrathguard/herald/herald-form.c");

    addResearchElement("/guilds/wrathguard/herald/dark-strength.c");
    addChild("/guilds/wrathguard/herald/dark-strength.c",
        "/guilds/wrathguard/herald/herald-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/wrathguard/herald/shadow-bolt.c");
    addChild("/guilds/wrathguard/herald/shadow-bolt.c",
        "/guilds/wrathguard/herald/herald-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFourthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/herald-mind.c");
    addChild("/guilds/wrathguard/herald/herald-mind.c",
        "/guilds/wrathguard/herald/shadow-presence.c");

    addResearchElement("/guilds/wrathguard/herald/void-skin.c");
    addChild("/guilds/wrathguard/herald/void-skin.c",
        "/guilds/wrathguard/herald/void-aura.c");

    addResearchElement("/guilds/wrathguard/herald/shadow-vigor.c");
    addChild("/guilds/wrathguard/herald/shadow-vigor.c",
        "/guilds/wrathguard/herald/herald-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/void-touch.c");
    addChild("/guilds/wrathguard/herald/void-touch.c",
        "/guilds/wrathguard/herald/shadow-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/herald/shadow-strike-mastery.c");
    addChild("/guilds/wrathguard/herald/shadow-strike-mastery.c",
        "/guilds/wrathguard/herald/shadow-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/herald-endurance.c");
    addChild("/guilds/wrathguard/herald/herald-endurance.c",
        "/guilds/wrathguard/herald/herald-mind.c");

    addResearchElement("/guilds/wrathguard/herald/dark-wisdom.c");
    addChild("/guilds/wrathguard/herald/dark-wisdom.c",
        "/guilds/wrathguard/herald/void-skin.c");

    addResearchElement("/guilds/wrathguard/herald/shadow-power.c");
    addChild("/guilds/wrathguard/herald/shadow-power.c",
        "/guilds/wrathguard/herald/shadow-vigor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/wrathguard/herald/darkening-strike.c");
    addChild("/guilds/wrathguard/herald/darkening-strike.c",
        "/guilds/wrathguard/herald/void-touch.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySecondLevel()
{
    addResearchElement("/guilds/wrathguard/herald/herald-resilience.c");
    addChild("/guilds/wrathguard/herald/herald-resilience.c",
        "/guilds/wrathguard/herald/herald-endurance.c");

    addResearchElement("/guilds/wrathguard/herald/void-constitution.c");
    addChild("/guilds/wrathguard/herald/void-constitution.c",
        "/guilds/wrathguard/herald/dark-wisdom.c");

    addResearchElement("/guilds/wrathguard/herald/dark-charisma.c");
    addChild("/guilds/wrathguard/herald/dark-charisma.c",
        "/guilds/wrathguard/herald/shadow-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/heralds-wrath.c");
    addChild("/guilds/wrathguard/herald/heralds-wrath.c",
        "/guilds/wrathguard/herald/darkening-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/herald/void-power-mastery.c");
    addChild("/guilds/wrathguard/herald/void-power-mastery.c",
        "/guilds/wrathguard/herald/shadow-strike-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyEighthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/shadow-potency.c");
    addChild("/guilds/wrathguard/herald/shadow-potency.c",
        "/guilds/wrathguard/herald/herald-resilience.c");

    addResearchElement("/guilds/wrathguard/herald/herald-attunement.c");
    addChild("/guilds/wrathguard/herald/herald-attunement.c",
        "/guilds/wrathguard/herald/void-constitution.c");

    addResearchElement("/guilds/wrathguard/herald/void-resilience.c");
    addChild("/guilds/wrathguard/herald/void-resilience.c",
        "/guilds/wrathguard/herald/dark-charisma.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/wrathguard/herald/shadow-embrace.c");
    addChild("/guilds/wrathguard/herald/shadow-embrace.c",
        "/guilds/wrathguard/herald/heralds-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/herald/dark-authority.c");
    addChild("/guilds/wrathguard/herald/dark-authority.c",
        "/guilds/wrathguard/herald/shadow-potency.c");

    addResearchElement("/guilds/wrathguard/herald/shadow-mastery-buff.c");
    addChild("/guilds/wrathguard/herald/shadow-mastery-buff.c",
        "/guilds/wrathguard/herald/herald-attunement.c");

    addResearchElement("/guilds/wrathguard/herald/herald-vigor.c");
    addChild("/guilds/wrathguard/herald/herald-vigor.c",
        "/guilds/wrathguard/herald/void-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/heralds-wrath-mastery.c");
    addChild("/guilds/wrathguard/herald/heralds-wrath-mastery.c",
        "/guilds/wrathguard/herald/void-power-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyEighthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/void-endurance.c");
    addChild("/guilds/wrathguard/herald/void-endurance.c",
        "/guilds/wrathguard/herald/dark-authority.c");

    addResearchElement("/guilds/wrathguard/herald/darkness-power.c");
    addChild("/guilds/wrathguard/herald/darkness-power.c",
        "/guilds/wrathguard/herald/shadow-mastery-buff.c");

    addResearchElement("/guilds/wrathguard/herald/heralds-grace.c");
    addChild("/guilds/wrathguard/herald/heralds-grace.c",
        "/guilds/wrathguard/herald/herald-vigor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/wrathguard/herald/void-annihilation.c");
    addChild("/guilds/wrathguard/herald/void-annihilation.c",
        "/guilds/wrathguard/herald/shadow-embrace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/heralds-pinnacle-mastery.c");
    addChild("/guilds/wrathguard/herald/heralds-pinnacle-mastery.c",
        "/guilds/wrathguard/herald/heralds-wrath-mastery.c");

    addResearchElement("/guilds/wrathguard/herald/shadow-constitution.c");
    addChild("/guilds/wrathguard/herald/shadow-constitution.c",
        "/guilds/wrathguard/herald/void-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyEighthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/dark-potency.c");
    addChild("/guilds/wrathguard/herald/dark-potency.c",
        "/guilds/wrathguard/herald/heralds-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/wrathguard/herald/darkness-absolute.c");
    addChild("/guilds/wrathguard/herald/darkness-absolute.c",
        "/guilds/wrathguard/herald/void-annihilation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/wrathguard/herald/darkness-amplifier.c");
    addChild("/guilds/wrathguard/herald/darkness-amplifier.c",
        "/guilds/wrathguard/herald/heralds-pinnacle-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/herald/void-sovereign.c");
    addChild("/guilds/wrathguard/herald/void-sovereign.c",
        "/guilds/wrathguard/herald/dark-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/wrathguard/herald/heralds-judgment.c");
    addChild("/guilds/wrathguard/herald/heralds-judgment.c",
        "/guilds/wrathguard/herald/darkness-absolute.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Herald of Darkness");
    Description("The transformation into Seilyndria's chosen herald - a "
        "sustained divine form that reshapes the Wrathguard into a vessel "
        "of absolute darkness. While the Herald walks, shadow bends to "
        "their will and enemies are struck down by void-touched power.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/herald/herald-of-darkness-root.c");
    TreeRoot("/guilds/wrathguard/herald/herald-of-darkness-root.c");

    TwentiethLevel();
    TwentyFirstLevel();
    TwentySecondLevel();
    TwentyFourthLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyEighthLevel();
    ThirtiethLevel();
    ThirtySecondLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyEighthLevel();
    FortiethLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortyEighthLevel();
    FiftiethLevel();
    FiftyFifthLevel();
    FiftyEighthLevel();
    SixtiethLevel();
    SixtySecondLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
