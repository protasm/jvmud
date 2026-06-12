//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-strike-mastery.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-strike-mastery.c",
        "/guilds/wrathguard/vengeance/vengeance-root.c");

    addResearchElement("/guilds/wrathguard/vengeance/blood-for-blood-mastery.c");
    addChild("/guilds/wrathguard/vengeance/blood-for-blood-mastery.c",
        "/guilds/wrathguard/vengeance/vengeance-root.c");

    addResearchElement("/guilds/wrathguard/vengeance/vengeance-attunement.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-attunement.c",
        "/guilds/wrathguard/vengeance/vengeance-root.c");

    addResearchElement("/guilds/wrathguard/vengeance/retributive-spirit.c");
    addChild("/guilds/wrathguard/vengeance/retributive-spirit.c",
        "/guilds/wrathguard/vengeance/vengeance-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-strike.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-strike.c",
        "/guilds/wrathguard/vengeance/vengeance-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/retributive-mind.c");
    addChild("/guilds/wrathguard/vengeance/retributive-mind.c",
        "/guilds/wrathguard/vengeance/vengeance-attunement.c");

    addResearchElement("/guilds/wrathguard/vengeance/vengeance-skin.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-skin.c",
        "/guilds/wrathguard/vengeance/retributive-spirit.c");

    addResearchElement("/guilds/wrathguard/vengeance/vengeance-ward.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-ward.c",
        "/guilds/wrathguard/vengeance/retributive-spirit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/retribution.c");
    addChild("/guilds/wrathguard/vengeance/retribution.c",
        "/guilds/wrathguard/vengeance/vengeance-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/retribution-amplifier.c");
    addChild("/guilds/wrathguard/vengeance/retribution-amplifier.c",
        "/guilds/wrathguard/vengeance/vengeance-strike-mastery.c");

    addResearchElement("/guilds/wrathguard/vengeance/eye-for-an-eye-mastery.c");
    addChild("/guilds/wrathguard/vengeance/eye-for-an-eye-mastery.c",
        "/guilds/wrathguard/vengeance/blood-for-blood-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-resilience.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-resilience.c",
        "/guilds/wrathguard/vengeance/retributive-mind.c");

    addResearchElement("/guilds/wrathguard/vengeance/vengeance-vigor.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-vigor.c",
        "/guilds/wrathguard/vengeance/vengeance-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/righteous-strike.c");
    addChild("/guilds/wrathguard/vengeance/righteous-strike.c",
        "/guilds/wrathguard/vengeance/retribution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-endurance.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-endurance.c",
        "/guilds/wrathguard/vengeance/vengeance-vigor.c");

    addResearchElement("/guilds/wrathguard/vengeance/vengeance-strength.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-strength.c",
        "/guilds/wrathguard/vengeance/vengeance-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/divine-punishment.c");
    addChild("/guilds/wrathguard/vengeance/divine-punishment.c",
        "/guilds/wrathguard/vengeance/righteous-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/divine-punishment-mastery.c");
    addChild("/guilds/wrathguard/vengeance/divine-punishment-mastery.c",
        "/guilds/wrathguard/vengeance/retribution-amplifier.c");

    addResearchElement("/guilds/wrathguard/vengeance/righteous-fury-mastery.c");
    addChild("/guilds/wrathguard/vengeance/righteous-fury-mastery.c",
        "/guilds/wrathguard/vengeance/eye-for-an-eye-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/righteous-constitution.c");
    addChild("/guilds/wrathguard/vengeance/righteous-constitution.c",
        "/guilds/wrathguard/vengeance/vengeance-endurance.c");

    addResearchElement("/guilds/wrathguard/vengeance/queens-fury.c");
    addChild("/guilds/wrathguard/vengeance/queens-fury.c",
        "/guilds/wrathguard/vengeance/vengeance-strength.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/eye-for-an-eye.c");
    addChild("/guilds/wrathguard/vengeance/eye-for-an-eye.c",
        "/guilds/wrathguard/vengeance/divine-punishment.c");

    addResearchElement("/guilds/wrathguard/vengeance/vengeance-potency.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-potency.c",
        "/guilds/wrathguard/vengeance/righteous-constitution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-power.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-power.c",
        "/guilds/wrathguard/vengeance/vengeance-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/wrath-unleashed-mastery.c");
    addChild("/guilds/wrathguard/vengeance/wrath-unleashed-mastery.c",
        "/guilds/wrathguard/vengeance/divine-punishment-mastery.c");

    addResearchElement("/guilds/wrathguard/vengeance/queens-retribution-mastery.c");
    addChild("/guilds/wrathguard/vengeance/queens-retribution-mastery.c",
        "/guilds/wrathguard/vengeance/righteous-fury-mastery.c");

    addResearchElement("/guilds/wrathguard/vengeance/blood-for-blood.c");
    addChild("/guilds/wrathguard/vengeance/blood-for-blood.c",
        "/guilds/wrathguard/vengeance/eye-for-an-eye.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/retributive-mastery.c");
    addChild("/guilds/wrathguard/vengeance/retributive-mastery.c",
        "/guilds/wrathguard/vengeance/vengeance-power.c");

    addResearchElement("/guilds/wrathguard/vengeance/righteous-fury.c");
    addChild("/guilds/wrathguard/vengeance/righteous-fury.c",
        "/guilds/wrathguard/vengeance/blood-for-blood.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/seilyndrias-vengeance-mastery.c");
    addChild("/guilds/wrathguard/vengeance/seilyndrias-vengeance-mastery.c",
        "/guilds/wrathguard/vengeance/wrath-unleashed-mastery.c");

    addResearchElement("/guilds/wrathguard/vengeance/absolute-vengeance-mastery.c");
    addChild("/guilds/wrathguard/vengeance/absolute-vengeance-mastery.c",
        "/guilds/wrathguard/vengeance/queens-retribution-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/queens-fury-mastery.c");
    addChild("/guilds/wrathguard/vengeance/queens-fury-mastery.c",
        "/guilds/wrathguard/vengeance/queens-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/queens-retribution.c");
    addChild("/guilds/wrathguard/vengeance/queens-retribution.c",
        "/guilds/wrathguard/vengeance/righteous-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/punishing-blow.c");
    addChild("/guilds/wrathguard/vengeance/punishing-blow.c",
        "/guilds/wrathguard/vengeance/queens-retribution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/wrath-of-the-queen.c");
    addChild("/guilds/wrathguard/vengeance/wrath-of-the-queen.c",
        "/guilds/wrathguard/vengeance/punishing-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-of-seilyndria.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-of-seilyndria.c",
        "/guilds/wrathguard/vengeance/wrath-of-the-queen.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/divine-wrath-strike.c");
    addChild("/guilds/wrathguard/vengeance/divine-wrath-strike.c",
        "/guilds/wrathguard/vengeance/vengeance-of-seilyndria.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/absolute-retribution.c");
    addChild("/guilds/wrathguard/vengeance/absolute-retribution.c",
        "/guilds/wrathguard/vengeance/divine-wrath-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-annihilation.c");
    addChild("/guilds/wrathguard/vengeance/vengeance-annihilation.c",
        "/guilds/wrathguard/vengeance/absolute-retribution.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Vengeance");
    Description("The Wrathguard's sacred duty of retribution - to punish "
        "every slight against the Queen twice over and to ensure that "
        "those who raise their hand against Seilyndria's servants are "
        "utterly destroyed.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/vengeance/vengeance-root.c");
    TreeRoot("/guilds/wrathguard/vengeance/vengeance-root.c");

    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtiethLevel();
    ThirtyFirstLevel();
    ThirtySeventhLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
    SeventhiethLevel();
}
