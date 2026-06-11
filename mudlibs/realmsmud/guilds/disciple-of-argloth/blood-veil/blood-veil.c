//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-foundation.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/blood-focus.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/veil-foundation.c",
        "/guilds/disciple-of-argloth/blood-veil/root.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/blood-focus.c",
        "/guilds/disciple-of-argloth/blood-veil/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-attunement.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/pain-to-power.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/veil-attunement.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-foundation.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/pain-to-power.c",
        "/guilds/disciple-of-argloth/blood-veil/blood-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-hardening.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/sanguine-resonance.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/veil-hardening.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-attunement.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/sanguine-resonance.c",
        "/guilds/disciple-of-argloth/blood-veil/pain-to-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/tainted-vigil.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-thorns.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/dark-ward.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/tainted-vigil.c",
        "/guilds/disciple-of-argloth/blood-veil/root.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-thorns.c",
        "/guilds/disciple-of-argloth/blood-veil/tainted-vigil.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/dark-ward.c",
        "/guilds/disciple-of-argloth/blood-veil/tainted-vigil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/scarlet-fortitude.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-absorption.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/blood-skin.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/scarlet-fortitude.c",
        "/guilds/disciple-of-argloth/blood-veil/tainted-vigil.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-absorption.c",
        "/guilds/disciple-of-argloth/blood-veil/scarlet-fortitude.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/blood-skin.c",
        "/guilds/disciple-of-argloth/blood-veil/scarlet-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/argloths-ward.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-mastery.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/argloths-ward.c",
        "/guilds/disciple-of-argloth/blood-veil/root.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-mastery.c",
        "/guilds/disciple-of-argloth/blood-veil/argloths-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/drain-veil.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-diffusion.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/drain-veil.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-thorns.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-diffusion.c",
        "/guilds/disciple-of-argloth/blood-veil/dark-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-of-mirost.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/mirost-echo.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-shielding.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/veil-of-mirost.c",
        "/guilds/disciple-of-argloth/blood-veil/argloths-ward.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/mirost-echo.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-of-mirost.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-shielding.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-of-mirost.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/sanguine-caul.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-resilience.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/sanguine-caul.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-absorption.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-resilience.c",
        "/guilds/disciple-of-argloth/blood-veil/blood-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/sanguine-shell.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/shell-hardening.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-fortress.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/sanguine-shell.c",
        "/guilds/disciple-of-argloth/blood-veil/scarlet-fortitude.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/shell-hardening.c",
        "/guilds/disciple-of-argloth/blood-veil/sanguine-shell.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-fortress.c",
        "/guilds/disciple-of-argloth/blood-veil/sanguine-shell.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/blood-armor.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-concentration.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/blood-armor.c",
        "/guilds/disciple-of-argloth/blood-veil/shell-hardening.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/veil-concentration.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/crimson-carapace.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/carapace-spikes.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/crimson-carapace.c",
        "/guilds/disciple-of-argloth/blood-veil/sanguine-shell.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/carapace-spikes.c",
        "/guilds/disciple-of-argloth/blood-veil/crimson-carapace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-of-ruin.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/dark-barrier.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/veil-of-ruin.c",
        "/guilds/disciple-of-argloth/blood-veil/blood-armor.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/dark-barrier.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-concentration.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/zhardegs-shroud.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/shroud-amplification.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/void-ward.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/zhardegs-shroud.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-of-mirost.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/shroud-amplification.c",
        "/guilds/disciple-of-argloth/blood-veil/zhardegs-shroud.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/void-ward.c",
        "/guilds/disciple-of-argloth/blood-veil/zhardegs-shroud.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-immortality.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/blood-bulwark.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/veil-immortality.c",
        "/guilds/disciple-of-argloth/blood-veil/shroud-amplification.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/blood-bulwark.c",
        "/guilds/disciple-of-argloth/blood-veil/void-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/blood-hardened-will.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/iron-will-of-argloth.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/blood-hardened-will.c",
        "/guilds/disciple-of-argloth/blood-veil/crimson-carapace.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/iron-will-of-argloth.c",
        "/guilds/disciple-of-argloth/blood-veil/blood-hardened-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/veil-transcendence.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/covenant-shield.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/veil-transcendence.c",
        "/guilds/disciple-of-argloth/blood-veil/veil-immortality.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/covenant-shield.c",
        "/guilds/disciple-of-argloth/blood-veil/blood-bulwark.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/dark-covenant-veil.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/dark-covenant-veil.c",
        "/guilds/disciple-of-argloth/blood-veil/iron-will-of-argloth.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/the-last-covenant.c");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/beyond-the-covenant.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/the-last-covenant.c",
        "/guilds/disciple-of-argloth/blood-veil/zhardegs-shroud.c");
    addChild("/guilds/disciple-of-argloth/blood-veil/beyond-the-covenant.c",
        "/guilds/disciple-of-argloth/blood-veil/the-last-covenant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void Fortieth()
{
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/argloth-shroud.c");

    addChild("/guilds/disciple-of-argloth/blood-veil/argloth-shroud.c",
        "/guilds/disciple-of-argloth/blood-veil/dark-covenant-veil.c");
}
/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Blood Veil");
    Description("This skill provides the Disciple with the knowledge to weave their own lifeblood into a corrupted magical shield.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/blood-veil/root.c");
    TreeRoot("/guilds/disciple-of-argloth/blood-veil/root.c");

    FirstLevel();
    SecondLevel();
    ThirdLevel();
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
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    Fortieth();
}
