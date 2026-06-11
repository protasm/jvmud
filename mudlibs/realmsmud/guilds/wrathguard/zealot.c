//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealous-strike-mastery.c");
    addChild("/guilds/wrathguard/zealotry/zealous-strike-mastery.c",
        "/guilds/wrathguard/zealotry/zealot-root.c");

    addResearchElement("/guilds/wrathguard/zealotry/fervor-mastery.c");
    addChild("/guilds/wrathguard/zealotry/fervor-mastery.c",
        "/guilds/wrathguard/zealotry/zealot-root.c");

    addResearchElement("/guilds/wrathguard/zealotry/zealot-attunement.c");
    addChild("/guilds/wrathguard/zealotry/zealot-attunement.c",
        "/guilds/wrathguard/zealotry/zealot-root.c");

    addResearchElement("/guilds/wrathguard/zealotry/burning-faith.c");
    addChild("/guilds/wrathguard/zealotry/burning-faith.c",
        "/guilds/wrathguard/zealotry/zealot-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealous-strike.c");
    addChild("/guilds/wrathguard/zealotry/zealous-strike.c",
        "/guilds/wrathguard/zealotry/zealot-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-mind.c");
    addChild("/guilds/wrathguard/zealotry/zealot-mind.c",
        "/guilds/wrathguard/zealotry/zealot-attunement.c");

    addResearchElement("/guilds/wrathguard/zealotry/zealot-ward.c");
    addChild("/guilds/wrathguard/zealotry/zealot-ward.c",
        "/guilds/wrathguard/zealotry/burning-faith.c");

    addResearchElement("/guilds/wrathguard/zealotry/fanatic-spirit.c");
    addChild("/guilds/wrathguard/zealotry/fanatic-spirit.c",
        "/guilds/wrathguard/zealotry/burning-faith.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/fervent-strike.c");
    addChild("/guilds/wrathguard/zealotry/fervent-strike.c",
        "/guilds/wrathguard/zealotry/zealous-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/divine-fervor-mastery.c");
    addChild("/guilds/wrathguard/zealotry/divine-fervor-mastery.c",
        "/guilds/wrathguard/zealotry/zealous-strike-mastery.c");

    addResearchElement("/guilds/wrathguard/zealotry/conviction-amplifier.c");
    addChild("/guilds/wrathguard/zealotry/conviction-amplifier.c",
        "/guilds/wrathguard/zealotry/fervor-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-resilience.c");
    addChild("/guilds/wrathguard/zealotry/zealot-resilience.c",
        "/guilds/wrathguard/zealotry/zealot-mind.c");

    addResearchElement("/guilds/wrathguard/zealotry/zealot-skin.c");
    addChild("/guilds/wrathguard/zealotry/zealot-skin.c",
        "/guilds/wrathguard/zealotry/zealot-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/divine-fervor.c");
    addChild("/guilds/wrathguard/zealotry/divine-fervor.c",
        "/guilds/wrathguard/zealotry/fervent-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-vigor.c");
    addChild("/guilds/wrathguard/zealotry/zealot-vigor.c",
        "/guilds/wrathguard/zealotry/zealot-resilience.c");

    addResearchElement("/guilds/wrathguard/zealotry/zealot-endurance.c");
    addChild("/guilds/wrathguard/zealotry/zealot-endurance.c",
        "/guilds/wrathguard/zealotry/zealot-skin.c");

    addResearchElement("/guilds/wrathguard/zealotry/fanatical-devotion.c");
    addChild("/guilds/wrathguard/zealotry/fanatical-devotion.c",
        "/guilds/wrathguard/zealotry/fanatic-spirit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-constitution.c");
    addChild("/guilds/wrathguard/zealotry/zealot-constitution.c",
        "/guilds/wrathguard/zealotry/zealot-vigor.c");

    addResearchElement("/guilds/wrathguard/zealotry/zealot-strength.c");
    addChild("/guilds/wrathguard/zealotry/zealot-strength.c",
        "/guilds/wrathguard/zealotry/zealot-endurance.c");

    addResearchElement("/guilds/wrathguard/zealotry/sacrilegious-blow.c");
    addChild("/guilds/wrathguard/zealotry/sacrilegious-blow.c",
        "/guilds/wrathguard/zealotry/divine-fervor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/martyrs-zeal-mastery.c");
    addChild("/guilds/wrathguard/zealotry/martyrs-zeal-mastery.c",
        "/guilds/wrathguard/zealotry/divine-fervor-mastery.c");

    addResearchElement("/guilds/wrathguard/zealotry/queens-devotion-mastery.c");
    addChild("/guilds/wrathguard/zealotry/queens-devotion-mastery.c",
        "/guilds/wrathguard/zealotry/conviction-amplifier.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-potency.c");
    addChild("/guilds/wrathguard/zealotry/zealot-potency.c",
        "/guilds/wrathguard/zealotry/zealot-constitution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/martyrs-strike.c");
    addChild("/guilds/wrathguard/zealotry/martyrs-strike.c",
        "/guilds/wrathguard/zealotry/sacrilegious-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-power.c");
    addChild("/guilds/wrathguard/zealotry/zealot-power.c",
        "/guilds/wrathguard/zealotry/zealot-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/void-zeal-mastery.c");
    addChild("/guilds/wrathguard/zealotry/void-zeal-mastery.c",
        "/guilds/wrathguard/zealotry/martyrs-zeal-mastery.c");

    addResearchElement("/guilds/wrathguard/zealotry/seilyndrias-chosen-mastery.c");
    addChild("/guilds/wrathguard/zealotry/seilyndrias-chosen-mastery.c",
        "/guilds/wrathguard/zealotry/queens-devotion-mastery.c");

    addResearchElement("/guilds/wrathguard/zealotry/righteous-immolation.c");
    addChild("/guilds/wrathguard/zealotry/righteous-immolation.c",
        "/guilds/wrathguard/zealotry/martyrs-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-mastery.c");
    addChild("/guilds/wrathguard/zealotry/zealot-mastery.c",
        "/guilds/wrathguard/zealotry/zealot-power.c");

    addResearchElement("/guilds/wrathguard/zealotry/holy-terror.c");
    addChild("/guilds/wrathguard/zealotry/holy-terror.c",
        "/guilds/wrathguard/zealotry/righteous-immolation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/holy-terror-mastery.c");
    addChild("/guilds/wrathguard/zealotry/holy-terror-mastery.c",
        "/guilds/wrathguard/zealotry/void-zeal-mastery.c");

    addResearchElement("/guilds/wrathguard/zealotry/queens-zealot-mastery.c");
    addChild("/guilds/wrathguard/zealotry/queens-zealot-mastery.c",
        "/guilds/wrathguard/zealotry/seilyndrias-chosen-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/immolation-of-faith.c");
    addChild("/guilds/wrathguard/zealotry/immolation-of-faith.c",
        "/guilds/wrathguard/zealotry/holy-terror.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/void-zealotry.c");
    addChild("/guilds/wrathguard/zealotry/void-zealotry.c",
        "/guilds/wrathguard/zealotry/immolation-of-faith.c");

    addResearchElement("/guilds/wrathguard/zealotry/devotion-of-ruin.c");
    addChild("/guilds/wrathguard/zealotry/devotion-of-ruin.c",
        "/guilds/wrathguard/zealotry/zealot-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/seilyndrias-chosen.c");
    addChild("/guilds/wrathguard/zealotry/seilyndrias-chosen.c",
        "/guilds/wrathguard/zealotry/void-zealotry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/the-queens-zealot.c");
    addChild("/guilds/wrathguard/zealotry/the-queens-zealot.c",
        "/guilds/wrathguard/zealotry/seilyndrias-chosen.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/zealot-annihilation.c");
    addChild("/guilds/wrathguard/zealotry/zealot-annihilation.c",
        "/guilds/wrathguard/zealotry/devotion-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/wrathguard/zealotry/rapture-of-ruin.c");
    addChild("/guilds/wrathguard/zealotry/rapture-of-ruin.c",
        "/guilds/wrathguard/zealotry/the-queens-zealot.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Zealot");
    Description("The path of absolute devotion to Seilyndria - the "
        "Wrathguard who has surrendered all self in service to the Queen "
        "and channels her divine will as a weapon of fanatical destruction.");
    Source("Wrathguard");
    addResearchElement("/guilds/wrathguard/zealotry/zealot-root.c");
    TreeRoot("/guilds/wrathguard/zealotry/zealot-root.c");

    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentyNinthLevel();
    ThirtiethLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    SixtyFirstLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
    SeventhiethLevel();
}
