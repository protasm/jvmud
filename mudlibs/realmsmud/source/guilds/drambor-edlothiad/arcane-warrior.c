//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-speed.c");
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-haste.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-speed.c", "/guilds/drambor-edlothiad/arcane/root.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-haste.c", "/guilds/drambor-edlothiad/arcane/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/battle-sense.c");
    addChild("/guilds/drambor-edlothiad/arcane/battle-sense.c", "/guilds/drambor-edlothiad/arcane/arcane-speed.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-conditioning.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-conditioning.c", "/guilds/drambor-edlothiad/arcane/battle-sense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/elven-agility.c");
    addResearchElement("/guilds/drambor-edlothiad/arcane/combat-focus.c");
    addChild("/guilds/drambor-edlothiad/arcane/elven-agility.c", "/guilds/drambor-edlothiad/arcane/arcane-conditioning.c");
    addChild("/guilds/drambor-edlothiad/arcane/combat-focus.c", "/guilds/drambor-edlothiad/arcane/arcane-haste.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/enhanced-reflexes.c");
    addChild("/guilds/drambor-edlothiad/arcane/enhanced-reflexes.c", "/guilds/drambor-edlothiad/arcane/elven-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-warrior-focus.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-warrior-focus.c", "/guilds/drambor-edlothiad/arcane/enhanced-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/war-dancer.c");
    addChild("/guilds/drambor-edlothiad/arcane/war-dancer.c", "/guilds/drambor-edlothiad/arcane/enhanced-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-fortitude.c");
    addResearchElement("/guilds/drambor-edlothiad/arcane/whirlwind-stance.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-fortitude.c", "/guilds/drambor-edlothiad/arcane/war-dancer.c");
    addChild("/guilds/drambor-edlothiad/arcane/whirlwind-stance.c", "/guilds/drambor-edlothiad/arcane/combat-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/spell-dancer.c");
    addChild("/guilds/drambor-edlothiad/arcane/spell-dancer.c", "/guilds/drambor-edlothiad/arcane/arcane-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/elven-precision.c");
    addChild("/guilds/drambor-edlothiad/arcane/elven-precision.c", "/guilds/drambor-edlothiad/arcane/spell-dancer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-resilience.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-resilience.c", "/guilds/drambor-edlothiad/arcane/elven-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/lightning-reflexes.c");
    addChild("/guilds/drambor-edlothiad/arcane/lightning-reflexes.c", "/guilds/drambor-edlothiad/arcane/whirlwind-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/battle-dancer.c");
    addChild("/guilds/drambor-edlothiad/arcane/battle-dancer.c", "/guilds/drambor-edlothiad/arcane/arcane-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-warrior-mastery.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-warrior-mastery.c", "/guilds/drambor-edlothiad/arcane/enhanced-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/war-magic-infusion.c");
    addChild("/guilds/drambor-edlothiad/arcane/war-magic-infusion.c", "/guilds/drambor-edlothiad/arcane/battle-dancer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/supreme-agility.c");
    addChild("/guilds/drambor-edlothiad/arcane/supreme-agility.c", "/guilds/drambor-edlothiad/arcane/war-magic-infusion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-fury.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-fury.c", "/guilds/drambor-edlothiad/arcane/lightning-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-mastery.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-mastery.c", "/guilds/drambor-edlothiad/arcane/supreme-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/transcendent-warrior.c");
    addChild("/guilds/drambor-edlothiad/arcane/transcendent-warrior.c", "/guilds/drambor-edlothiad/arcane/arcane-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/elven-swiftness.c");
    addChild("/guilds/drambor-edlothiad/arcane/elven-swiftness.c", "/guilds/drambor-edlothiad/arcane/transcendent-warrior.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/storm-dancer.c");
    addChild("/guilds/drambor-edlothiad/arcane/storm-dancer.c", "/guilds/drambor-edlothiad/arcane/arcane-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-warrior-supremacy.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-warrior-supremacy.c", "/guilds/drambor-edlothiad/arcane/enhanced-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/legendary-warrior.c");
    addChild("/guilds/drambor-edlothiad/arcane/legendary-warrior.c", "/guilds/drambor-edlothiad/arcane/elven-swiftness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/immortal-warrior.c");
    addChild("/guilds/drambor-edlothiad/arcane/immortal-warrior.c", "/guilds/drambor-edlothiad/arcane/legendary-warrior.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/avatar-of-war.c");
    addChild("/guilds/drambor-edlothiad/arcane/avatar-of-war.c", "/guilds/drambor-edlothiad/arcane/storm-dancer.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevelB()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/combat-meditation.c");
    addChild("/guilds/drambor-edlothiad/arcane/combat-meditation.c", "/guilds/drambor-edlothiad/arcane/combat-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevelB()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/martial-precision.c");
    addChild("/guilds/drambor-edlothiad/arcane/martial-precision.c", "/guilds/drambor-edlothiad/arcane/enhanced-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-instinct.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-instinct.c", "/guilds/drambor-edlothiad/arcane/arcane-warrior-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevelB()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-endurance.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-endurance.c", "/guilds/drambor-edlothiad/arcane/arcane-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/elven-war-magic.c");
    addChild("/guilds/drambor-edlothiad/arcane/elven-war-magic.c", "/guilds/drambor-edlothiad/arcane/war-magic-infusion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevelB()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/fluid-motion.c");
    addChild("/guilds/drambor-edlothiad/arcane/fluid-motion.c", "/guilds/drambor-edlothiad/arcane/supreme-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/primal-agility.c");
    addChild("/guilds/drambor-edlothiad/arcane/primal-agility.c", "/guilds/drambor-edlothiad/arcane/arcane-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/spell-infused-strikes.c");
    addChild("/guilds/drambor-edlothiad/arcane/spell-infused-strikes.c", "/guilds/drambor-edlothiad/arcane/primal-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/warrior-transcendence.c");
    addChild("/guilds/drambor-edlothiad/arcane/warrior-transcendence.c", "/guilds/drambor-edlothiad/arcane/legendary-warrior.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/arcane-supremacy.c");
    addChild("/guilds/drambor-edlothiad/arcane/arcane-supremacy.c", "/guilds/drambor-edlothiad/arcane/warrior-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/eternal-warrior.c");
    addChild("/guilds/drambor-edlothiad/arcane/eternal-warrior.c", "/guilds/drambor-edlothiad/arcane/immortal-warrior.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/timeless-combatant.c");
    addChild("/guilds/drambor-edlothiad/arcane/timeless-combatant.c", "/guilds/drambor-edlothiad/arcane/eternal-warrior.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/arcane/avatar-of-arcane-war.c");
    addChild("/guilds/drambor-edlothiad/arcane/avatar-of-arcane-war.c", "/guilds/drambor-edlothiad/arcane/timeless-combatant.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Arcane Warrior");
    Description("This research tree enhances melee combat prowess with "
        "arcane techniques and magical enhancements, turning the battlemage "
        "into an unstoppable force on the battlefield.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/arcane/root.c");
    TreeRoot("/guilds/drambor-edlothiad/arcane/root.c");

    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    EleventhLevelB();
    ThirteenthLevel();
    FifteenthLevel();
    FifteenthLevelB();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentyFifthLevelB();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyFirstLevelB();
    FortyThirdLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SeventyLevel();
}
