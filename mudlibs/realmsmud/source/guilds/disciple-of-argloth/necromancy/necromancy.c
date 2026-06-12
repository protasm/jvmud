//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/raise-skeleton.c");

    addChild("/guilds/disciple-of-argloth/necromancy/raise-skeleton.c",
        "/guilds/disciple-of-argloth/necromancy/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/bone-mastery.c");
    addResearchElement("/guilds/disciple-of-argloth/necromancy/deathbolt.c");

    addChild("/guilds/disciple-of-argloth/necromancy/bone-mastery.c",
        "/guilds/disciple-of-argloth/necromancy/raise-skeleton.c");
    addChild("/guilds/disciple-of-argloth/necromancy/deathbolt.c",
        "/guilds/disciple-of-argloth/necromancy/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/animate-dead.c");

    addChild("/guilds/disciple-of-argloth/necromancy/animate-dead.c",
        "/guilds/disciple-of-argloth/necromancy/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/undead-fortitude.c");

    addChild("/guilds/disciple-of-argloth/necromancy/undead-fortitude.c",
        "/guilds/disciple-of-argloth/necromancy/animate-dead.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/skeletal-warrior.c");
    addResearchElement("/guilds/disciple-of-argloth/necromancy/soul-shatter.c");

    addChild("/guilds/disciple-of-argloth/necromancy/skeletal-warrior.c",
        "/guilds/disciple-of-argloth/necromancy/bone-mastery.c");
    addChild("/guilds/disciple-of-argloth/necromancy/soul-shatter.c",
        "/guilds/disciple-of-argloth/necromancy/deathbolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/ghoul-servant.c");
    addResearchElement("/guilds/disciple-of-argloth/necromancy/wither.c");

    addChild("/guilds/disciple-of-argloth/necromancy/ghoul-servant.c",
        "/guilds/disciple-of-argloth/necromancy/undead-fortitude.c");
    addChild("/guilds/disciple-of-argloth/necromancy/wither.c",
        "/guilds/disciple-of-argloth/necromancy/deathbolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/undead-command.c");

    addChild("/guilds/disciple-of-argloth/necromancy/undead-command.c",
        "/guilds/disciple-of-argloth/necromancy/ghoul-servant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/necrotic-pulse.c");

    addChild("/guilds/disciple-of-argloth/necromancy/necrotic-pulse.c",
        "/guilds/disciple-of-argloth/necromancy/soul-shatter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/raise-wight.c");

    addChild("/guilds/disciple-of-argloth/necromancy/raise-wight.c",
        "/guilds/disciple-of-argloth/necromancy/skeletal-warrior.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/death-coil.c");

    addChild("/guilds/disciple-of-argloth/necromancy/death-coil.c",
        "/guilds/disciple-of-argloth/necromancy/wither.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/legion-of-bones.c");

    addChild("/guilds/disciple-of-argloth/necromancy/legion-of-bones.c",
        "/guilds/disciple-of-argloth/necromancy/raise-wight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/corpse-explosion.c");
    addResearchElement("/guilds/disciple-of-argloth/necromancy/dark-servitude.c");

    addChild("/guilds/disciple-of-argloth/necromancy/corpse-explosion.c",
        "/guilds/disciple-of-argloth/necromancy/necrotic-pulse.c");
    addChild("/guilds/disciple-of-argloth/necromancy/dark-servitude.c",
        "/guilds/disciple-of-argloth/necromancy/undead-command.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/necromantic-surge.c");

    addChild("/guilds/disciple-of-argloth/necromancy/necromantic-surge.c",
        "/guilds/disciple-of-argloth/necromancy/death-coil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/undead-resilience.c");

    addChild("/guilds/disciple-of-argloth/necromancy/undead-resilience.c",
        "/guilds/disciple-of-argloth/necromancy/dark-servitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/deathly-aura.c");

    addChild("/guilds/disciple-of-argloth/necromancy/deathly-aura.c",
        "/guilds/disciple-of-argloth/necromancy/legion-of-bones.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/plague-bearer.c");

    addChild("/guilds/disciple-of-argloth/necromancy/plague-bearer.c",
        "/guilds/disciple-of-argloth/necromancy/corpse-explosion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/spectral-servant.c");

    addChild("/guilds/disciple-of-argloth/necromancy/spectral-servant.c",
        "/guilds/disciple-of-argloth/necromancy/necromantic-surge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/raise-revenant.c");

    addChild("/guilds/disciple-of-argloth/necromancy/raise-revenant.c",
        "/guilds/disciple-of-argloth/necromancy/undead-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/death-mastery.c");

    addChild("/guilds/disciple-of-argloth/necromancy/death-mastery.c",
        "/guilds/disciple-of-argloth/necromancy/deathly-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/bone-spear.c");

    addChild("/guilds/disciple-of-argloth/necromancy/bone-spear.c",
        "/guilds/disciple-of-argloth/necromancy/plague-bearer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/necrotic-drain.c");

    addChild("/guilds/disciple-of-argloth/necromancy/necrotic-drain.c",
        "/guilds/disciple-of-argloth/necromancy/spectral-servant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/putrefaction.c");

    addChild("/guilds/disciple-of-argloth/necromancy/putrefaction.c",
        "/guilds/disciple-of-argloth/necromancy/raise-revenant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/mass-animation.c");

    addChild("/guilds/disciple-of-argloth/necromancy/mass-animation.c",
        "/guilds/disciple-of-argloth/necromancy/death-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/grave-chill.c");

    addChild("/guilds/disciple-of-argloth/necromancy/grave-chill.c",
        "/guilds/disciple-of-argloth/necromancy/bone-spear.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/death-ward.c");

    addChild("/guilds/disciple-of-argloth/necromancy/death-ward.c",
        "/guilds/disciple-of-argloth/necromancy/necrotic-drain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/deathsight.c");

    addChild("/guilds/disciple-of-argloth/necromancy/deathsight.c",
        "/guilds/disciple-of-argloth/necromancy/putrefaction.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/undying-legion.c");

    addChild("/guilds/disciple-of-argloth/necromancy/undying-legion.c",
        "/guilds/disciple-of-argloth/necromancy/mass-animation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/necrotic-amplification.c");

    addChild("/guilds/disciple-of-argloth/necromancy/necrotic-amplification.c",
        "/guilds/disciple-of-argloth/necromancy/grave-chill.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/grave-robber.c");

    addChild("/guilds/disciple-of-argloth/necromancy/grave-robber.c",
        "/guilds/disciple-of-argloth/necromancy/death-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/death-shroud.c");

    addChild("/guilds/disciple-of-argloth/necromancy/death-shroud.c",
        "/guilds/disciple-of-argloth/necromancy/deathsight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/skeletal-champion.c");

    addChild("/guilds/disciple-of-argloth/necromancy/skeletal-champion.c",
        "/guilds/disciple-of-argloth/necromancy/undying-legion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/necromantic-mastery.c");

    addChild("/guilds/disciple-of-argloth/necromancy/necromantic-mastery.c",
        "/guilds/disciple-of-argloth/necromancy/necrotic-amplification.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/death-pact.c");

    addChild("/guilds/disciple-of-argloth/necromancy/death-pact.c",
        "/guilds/disciple-of-argloth/necromancy/grave-robber.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/consume-essence.c");

    addChild("/guilds/disciple-of-argloth/necromancy/consume-essence.c",
        "/guilds/disciple-of-argloth/necromancy/death-shroud.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/argloth-lich-form.c");

    addChild("/guilds/disciple-of-argloth/necromancy/argloth-lich-form.c",
        "/guilds/disciple-of-argloth/necromancy/skeletal-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/necromancy/argloth-chosen.c");

    addChild("/guilds/disciple-of-argloth/necromancy/argloth-chosen.c",
        "/guilds/disciple-of-argloth/necromancy/necromantic-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Necromantic Arts");
    Description("The study of death, undeath, and the power that flows between the living and the dead as understood by the disciples of Argloth.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/necromancy/root.c");
    TreeRoot("/guilds/disciple-of-argloth/necromancy/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    EighthLevel();
    TenthLevel();
    TwelfthLevel();
    FourteenthLevel();
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
    ThirtySixthLevel();
    ThirtyEighthLevel();
    FortiethLevel();
    FortyFourthLevel();
    FortySixthLevel();
    FortyEighthLevel();
    FiftiethLevel();
    FiftySecondLevel();
    FiftyFourthLevel();
    FiftySixthLevel();
    FiftyEighthLevel();
    SixtiethLevel();
    SixtySecondLevel();
    SixtyFourthLevel();
    SixtySixthLevel();
    SixtyEighthLevel();
    SeventiethLevel();
}
