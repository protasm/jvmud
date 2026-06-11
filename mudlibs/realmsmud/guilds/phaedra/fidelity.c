//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/companions-bond.c");
    addResearchElement("/guilds/phaedra/fidelity/oath-of-protection.c");
    addResearchElement("/guilds/phaedra/fidelity/mending-touch.c");

    addChild("/guilds/phaedra/fidelity/companions-bond.c",
        "/guilds/phaedra/fidelity/fidelity-root.c");
    addChild("/guilds/phaedra/fidelity/oath-of-protection.c",
        "/guilds/phaedra/fidelity/fidelity-root.c");
    addChild("/guilds/phaedra/fidelity/mending-touch.c",
        "/guilds/phaedra/fidelity/oath-of-protection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/vigilant-guard.c");
    addResearchElement("/guilds/phaedra/fidelity/restorative-knowledge.c");

    addChild("/guilds/phaedra/fidelity/vigilant-guard.c",
        "/guilds/phaedra/fidelity/companions-bond.c");
    addChild("/guilds/phaedra/fidelity/restorative-knowledge.c",
        "/guilds/phaedra/fidelity/mending-touch.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/guardians-ward.c");
    addResearchElement("/guilds/phaedra/fidelity/healing-surge.c");

    addChild("/guilds/phaedra/fidelity/guardians-ward.c",
        "/guilds/phaedra/fidelity/vigilant-guard.c");
    addChild("/guilds/phaedra/fidelity/healing-surge.c",
        "/guilds/phaedra/fidelity/restorative-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/devoted-shield.c");
    addResearchElement("/guilds/phaedra/fidelity/restorative-mastery.c");

    addChild("/guilds/phaedra/fidelity/devoted-shield.c",
        "/guilds/phaedra/fidelity/guardians-ward.c");
    addChild("/guilds/phaedra/fidelity/restorative-mastery.c",
        "/guilds/phaedra/fidelity/healing-surge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/stalwart-shield.c");
    addResearchElement("/guilds/phaedra/fidelity/faithful-endurance.c");

    addChild("/guilds/phaedra/fidelity/stalwart-shield.c",
        "/guilds/phaedra/fidelity/devoted-shield.c");
    addChild("/guilds/phaedra/fidelity/faithful-endurance.c",
        "/guilds/phaedra/fidelity/vigilant-guard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/companions-burden.c");
    addResearchElement("/guilds/phaedra/fidelity/loyal-sentinel.c");

    addChild("/guilds/phaedra/fidelity/companions-burden.c",
        "/guilds/phaedra/fidelity/restorative-mastery.c");
    addChild("/guilds/phaedra/fidelity/loyal-sentinel.c",
        "/guilds/phaedra/fidelity/stalwart-shield.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/renewed-spirit.c");
    addResearchElement("/guilds/phaedra/fidelity/bulwark-of-faith.c");

    addChild("/guilds/phaedra/fidelity/renewed-spirit.c",
        "/guilds/phaedra/fidelity/companions-burden.c");
    addChild("/guilds/phaedra/fidelity/bulwark-of-faith.c",
        "/guilds/phaedra/fidelity/loyal-sentinel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/healing-word.c");
    addResearchElement("/guilds/phaedra/fidelity/protective-instinct.c");

    addChild("/guilds/phaedra/fidelity/healing-word.c",
        "/guilds/phaedra/fidelity/renewed-spirit.c");
    addChild("/guilds/phaedra/fidelity/protective-instinct.c",
        "/guilds/phaedra/fidelity/bulwark-of-faith.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/font-of-healing.c");
    addResearchElement("/guilds/phaedra/fidelity/shield-of-the-faithful.c");

    addChild("/guilds/phaedra/fidelity/font-of-healing.c",
        "/guilds/phaedra/fidelity/healing-word.c");
    addChild("/guilds/phaedra/fidelity/shield-of-the-faithful.c",
        "/guilds/phaedra/fidelity/protective-instinct.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/devoted-healer.c");

    addChild("/guilds/phaedra/fidelity/devoted-healer.c",
        "/guilds/phaedra/fidelity/font-of-healing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/radiant-mending.c");
    addResearchElement("/guilds/phaedra/fidelity/unshakeable-loyalty.c");

    addChild("/guilds/phaedra/fidelity/radiant-mending.c",
        "/guilds/phaedra/fidelity/devoted-healer.c");
    addChild("/guilds/phaedra/fidelity/unshakeable-loyalty.c",
        "/guilds/phaedra/fidelity/shield-of-the-faithful.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/healers-wisdom.c");
    addResearchElement("/guilds/phaedra/fidelity/iron-ward.c");

    addChild("/guilds/phaedra/fidelity/healers-wisdom.c",
        "/guilds/phaedra/fidelity/radiant-mending.c");
    addChild("/guilds/phaedra/fidelity/iron-ward.c",
        "/guilds/phaedra/fidelity/unshakeable-loyalty.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/great-healing.c");

    addChild("/guilds/phaedra/fidelity/great-healing.c",
        "/guilds/phaedra/fidelity/healers-wisdom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/enduring-protection.c");
    addResearchElement("/guilds/phaedra/fidelity/unshakeable-bond.c");

    addChild("/guilds/phaedra/fidelity/enduring-protection.c",
        "/guilds/phaedra/fidelity/iron-ward.c");
    addChild("/guilds/phaedra/fidelity/unshakeable-bond.c",
        "/guilds/phaedra/fidelity/enduring-protection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/master-healer.c");

    addChild("/guilds/phaedra/fidelity/master-healer.c",
        "/guilds/phaedra/fidelity/great-healing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/unyielding-guardian.c");

    addChild("/guilds/phaedra/fidelity/unyielding-guardian.c",
        "/guilds/phaedra/fidelity/enduring-protection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/devoted-mastery.c");

    addChild("/guilds/phaedra/fidelity/devoted-mastery.c",
        "/guilds/phaedra/fidelity/master-healer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/unbreakable-ward.c");
    addResearchElement("/guilds/phaedra/fidelity/font-of-restoration.c");

    addChild("/guilds/phaedra/fidelity/unbreakable-ward.c",
        "/guilds/phaedra/fidelity/unyielding-guardian.c");
    addChild("/guilds/phaedra/fidelity/font-of-restoration.c",
        "/guilds/phaedra/fidelity/devoted-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/eternal-guardian.c");

    addChild("/guilds/phaedra/fidelity/eternal-guardian.c",
        "/guilds/phaedra/fidelity/unbreakable-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/restorative-aura.c");

    addChild("/guilds/phaedra/fidelity/restorative-aura.c",
        "/guilds/phaedra/fidelity/font-of-restoration.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/aegis-of-phaedra.c");

    addChild("/guilds/phaedra/fidelity/aegis-of-phaedra.c",
        "/guilds/phaedra/fidelity/eternal-guardian.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/sovereign-healer.c");

    addChild("/guilds/phaedra/fidelity/sovereign-healer.c",
        "/guilds/phaedra/fidelity/restorative-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/bastion.c");
    addResearchElement("/guilds/phaedra/fidelity/radiant-mending-surge.c");

    addChild("/guilds/phaedra/fidelity/bastion.c",
        "/guilds/phaedra/fidelity/aegis-of-phaedra.c");
    addChild("/guilds/phaedra/fidelity/radiant-mending-surge.c",
        "/guilds/phaedra/fidelity/sovereign-healer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/fidelity-eternal.c");
    addResearchElement("/guilds/phaedra/fidelity/supreme-dedication.c");

    addChild("/guilds/phaedra/fidelity/fidelity-eternal.c",
        "/guilds/phaedra/fidelity/bastion.c");
    addChild("/guilds/phaedra/fidelity/supreme-dedication.c",
        "/guilds/phaedra/fidelity/radiant-mending-surge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/absolute-guardian.c");

    addChild("/guilds/phaedra/fidelity/absolute-guardian.c",
        "/guilds/phaedra/fidelity/fidelity-eternal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/word-of-restoration.c");

    addChild("/guilds/phaedra/fidelity/word-of-restoration.c",
        "/guilds/phaedra/fidelity/absolute-guardian.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/fidelity/unending-fidelity.c");

    addChild("/guilds/phaedra/fidelity/unending-fidelity.c",
        "/guilds/phaedra/fidelity/word-of-restoration.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Fidelity");
    Description("The second pillar of the Order of Phaedra: the fidelity "
        "to stand beside those who depend upon you, to shield the "
        "vulnerable, and to bear the burden willingly.");
    Source("Order of Phaedra");
    addResearchElement("/guilds/phaedra/fidelity/fidelity-root.c");
    TreeRoot("/guilds/phaedra/fidelity/fidelity-root.c");

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
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtyNinthLevel();
    FortyFifthLevel();
    FortyFirstLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
