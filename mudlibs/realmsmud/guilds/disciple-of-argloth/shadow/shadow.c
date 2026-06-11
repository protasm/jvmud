//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-step.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-veil.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-step.c",
        "/guilds/disciple-of-argloth/shadow/root.c");
    addChild("/guilds/disciple-of-argloth/shadow/shadow-veil.c",
        "/guilds/disciple-of-argloth/shadow/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/dark-sight.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-strike.c");

    addChild("/guilds/disciple-of-argloth/shadow/dark-sight.c",
        "/guilds/disciple-of-argloth/shadow/shadow-step.c");
    addChild("/guilds/disciple-of-argloth/shadow/shadow-strike.c",
        "/guilds/disciple-of-argloth/shadow/shadow-step.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-meld.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/umbral-shroud.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-meld.c",
        "/guilds/disciple-of-argloth/shadow/shadow-veil.c");
    addChild("/guilds/disciple-of-argloth/shadow/umbral-shroud.c",
        "/guilds/disciple-of-argloth/shadow/shadow-veil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/tendrils-of-shadow.c");

    addChild("/guilds/disciple-of-argloth/shadow/tendrils-of-shadow.c",
        "/guilds/disciple-of-argloth/shadow/shadow-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/night-terror.c");

    addChild("/guilds/disciple-of-argloth/shadow/night-terror.c",
        "/guilds/disciple-of-argloth/shadow/dark-sight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-bind.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-bind.c",
        "/guilds/disciple-of-argloth/shadow/tendrils-of-shadow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/void-step.c");

    addChild("/guilds/disciple-of-argloth/shadow/void-step.c",
        "/guilds/disciple-of-argloth/shadow/shadow-meld.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/dark-embrace.c");

    addChild("/guilds/disciple-of-argloth/shadow/dark-embrace.c",
        "/guilds/disciple-of-argloth/shadow/umbral-shroud.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-rupture.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-rupture.c",
        "/guilds/disciple-of-argloth/shadow/shadow-bind.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/umbral-lance.c");

    addChild("/guilds/disciple-of-argloth/shadow/umbral-lance.c",
        "/guilds/disciple-of-argloth/shadow/night-terror.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/eclipse.c");

    addChild("/guilds/disciple-of-argloth/shadow/eclipse.c",
        "/guilds/disciple-of-argloth/shadow/void-step.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-mastery.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-mastery.c",
        "/guilds/disciple-of-argloth/shadow/dark-embrace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-amplification.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/void-shroud.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-amplification.c",
        "/guilds/disciple-of-argloth/shadow/shadow-rupture.c");
    addChild("/guilds/disciple-of-argloth/shadow/void-shroud.c",
        "/guilds/disciple-of-argloth/shadow/eclipse.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/dark-resilience.c");

    addChild("/guilds/disciple-of-argloth/shadow/dark-resilience.c",
        "/guilds/disciple-of-argloth/shadow/shadow-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/penumbra.c");

    addChild("/guilds/disciple-of-argloth/shadow/penumbra.c",
        "/guilds/disciple-of-argloth/shadow/void-shroud.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-walk.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/umbral-storm.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-walk.c",
        "/guilds/disciple-of-argloth/shadow/shadow-amplification.c");
    addChild("/guilds/disciple-of-argloth/shadow/umbral-storm.c",
        "/guilds/disciple-of-argloth/shadow/umbral-lance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/black-void.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-resilience.c");

    addChild("/guilds/disciple-of-argloth/shadow/black-void.c",
        "/guilds/disciple-of-argloth/shadow/penumbra.c");
    addChild("/guilds/disciple-of-argloth/shadow/shadow-resilience.c",
        "/guilds/disciple-of-argloth/shadow/dark-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-form.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-form.c",
        "/guilds/disciple-of-argloth/shadow/dark-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/eternal-night.c");

    addChild("/guilds/disciple-of-argloth/shadow/eternal-night.c",
        "/guilds/disciple-of-argloth/shadow/shadow-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-attunement.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-potency.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-attunement.c",
        "/guilds/disciple-of-argloth/shadow/umbral-storm.c");
    addChild("/guilds/disciple-of-argloth/shadow/shadow-potency.c",
        "/guilds/disciple-of-argloth/shadow/shadow-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/umbral-theory.c");

    addChild("/guilds/disciple-of-argloth/shadow/umbral-theory.c",
        "/guilds/disciple-of-argloth/shadow/black-void.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/dark-conduit.c");

    addChild("/guilds/disciple-of-argloth/shadow/dark-conduit.c",
        "/guilds/disciple-of-argloth/shadow/shadow-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-covenant.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-covenant.c",
        "/guilds/disciple-of-argloth/shadow/eternal-night.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-theory.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/void-current.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-theory.c",
        "/guilds/disciple-of-argloth/shadow/umbral-theory.c");
    addChild("/guilds/disciple-of-argloth/shadow/void-current.c",
        "/guilds/disciple-of-argloth/shadow/shadow-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/living-shadow.c");

    addChild("/guilds/disciple-of-argloth/shadow/living-shadow.c",
        "/guilds/disciple-of-argloth/shadow/umbral-theory.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/shadow-death.c");

    addChild("/guilds/disciple-of-argloth/shadow/shadow-death.c",
        "/guilds/disciple-of-argloth/shadow/dark-conduit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/oblivion-touch.c");

    addChild("/guilds/disciple-of-argloth/shadow/oblivion-touch.c",
        "/guilds/disciple-of-argloth/shadow/shadow-covenant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/void-walker.c");

    addChild("/guilds/disciple-of-argloth/shadow/void-walker.c",
        "/guilds/disciple-of-argloth/shadow/void-current.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/void-lance.c");

    addChild("/guilds/disciple-of-argloth/shadow/void-lance.c",
        "/guilds/disciple-of-argloth/shadow/void-walker.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/argloth-shadow.c");

    addChild("/guilds/disciple-of-argloth/shadow/argloth-shadow.c",
        "/guilds/disciple-of-argloth/shadow/living-shadow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/shadow/argloth-darkness.c");
    addResearchElement("/guilds/disciple-of-argloth/shadow/argloth-void.c");

    addChild("/guilds/disciple-of-argloth/shadow/argloth-darkness.c",
        "/guilds/disciple-of-argloth/shadow/argloth-void.c");
    addChild("/guilds/disciple-of-argloth/shadow/argloth-void.c",
        "/guilds/disciple-of-argloth/shadow/void-walker.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Shadow Arts");
    Description("The manipulation of shadow and darkness as an extension of death - the power to move unseen, strike from darkness, and bend shadow to the Disciple's will.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/shadow/root.c");
    TreeRoot("/guilds/disciple-of-argloth/shadow/root.c");

    FifthLevel();
    SeventhLevel();
    TenthLevel();
    TwelfthLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SixteenthLevel();
    EighteenthLevel();
    TwentiethLevel();
    TwentySecondLevel();
    TwentyFourthLevel();
    TwentySixthLevel();
    TwentyEighthLevel();
    ThirtiethLevel();
    ThirtySecondLevel();
    ThirtyFourthLevel();
    ThirtyEighthLevel();
    FortiethLevel();
    FortySecondLevel();
    FortyFourthLevel();
    FortySixthLevel();
    FortyEighthLevel();
    FiftiethLevel();
    FiftySecondLevel();
    FiftyFourthLevel();
    FiftyEighthLevel();
    SixtiethLevel();
    SixtyFourthLevel();
    SixtySixthLevel();
    SixtySeventhLevel();
    SeventiethLevel();
}
