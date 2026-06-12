//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/arcane-bolt.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/combat-casting.c");
    addChild("/guilds/drambor-edlothiad/battle/arcane-bolt.c", "/guilds/drambor-edlothiad/battle/root.c");
    addChild("/guilds/drambor-edlothiad/battle/combat-casting.c", "/guilds/drambor-edlothiad/battle/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/magic-missile.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/quickened-casting.c");
    addChild("/guilds/drambor-edlothiad/battle/magic-missile.c", "/guilds/drambor-edlothiad/battle/arcane-bolt.c");
    addChild("/guilds/drambor-edlothiad/battle/quickened-casting.c", "/guilds/drambor-edlothiad/battle/combat-casting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/spell-focus.c");
    addChild("/guilds/drambor-edlothiad/battle/spell-focus.c", "/guilds/drambor-edlothiad/battle/combat-casting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/force-blast.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/mana-efficiency.c");
    addChild("/guilds/drambor-edlothiad/battle/force-blast.c", "/guilds/drambor-edlothiad/battle/magic-missile.c");
    addChild("/guilds/drambor-edlothiad/battle/mana-efficiency.c", "/guilds/drambor-edlothiad/battle/spell-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/chain-lightning.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/spell-penetration.c");
    addChild("/guilds/drambor-edlothiad/battle/chain-lightning.c", "/guilds/drambor-edlothiad/battle/magic-missile.c");
    addChild("/guilds/drambor-edlothiad/battle/spell-penetration.c", "/guilds/drambor-edlothiad/battle/spell-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/fireball.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/arcane-might.c");
    addChild("/guilds/drambor-edlothiad/battle/fireball.c", "/guilds/drambor-edlothiad/battle/force-blast.c");
    addChild("/guilds/drambor-edlothiad/battle/arcane-might.c", "/guilds/drambor-edlothiad/battle/mana-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/ice-storm.c");
    addChild("/guilds/drambor-edlothiad/battle/ice-storm.c", "/guilds/drambor-edlothiad/battle/force-blast.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/devastating-focus.c");
    addChild("/guilds/drambor-edlothiad/battle/devastating-focus.c", "/guilds/drambor-edlothiad/battle/arcane-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/war-magic.c");
    addChild("/guilds/drambor-edlothiad/battle/war-magic.c", "/guilds/drambor-edlothiad/battle/chain-lightning.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/spell-penetration-mastery.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/battle-mastery.c");
    addChild("/guilds/drambor-edlothiad/battle/spell-penetration-mastery.c", "/guilds/drambor-edlothiad/battle/spell-penetration.c");
    addChild("/guilds/drambor-edlothiad/battle/battle-mastery.c", "/guilds/drambor-edlothiad/battle/devastating-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/arcane-barrage.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/tempest.c");
    addChild("/guilds/drambor-edlothiad/battle/arcane-barrage.c", "/guilds/drambor-edlothiad/battle/war-magic.c");
    addChild("/guilds/drambor-edlothiad/battle/tempest.c", "/guilds/drambor-edlothiad/battle/chain-lightning.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/inferno.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/elven-battle-focus.c");
    addChild("/guilds/drambor-edlothiad/battle/inferno.c", "/guilds/drambor-edlothiad/battle/fireball.c");
    addChild("/guilds/drambor-edlothiad/battle/elven-battle-focus.c", "/guilds/drambor-edlothiad/battle/battle-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/glacial-cascade.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/war-mage-endurance.c");
    addChild("/guilds/drambor-edlothiad/battle/glacial-cascade.c", "/guilds/drambor-edlothiad/battle/ice-storm.c");
    addChild("/guilds/drambor-edlothiad/battle/war-mage-endurance.c", "/guilds/drambor-edlothiad/battle/battle-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/disintegrate.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/destructive-power.c");
    addChild("/guilds/drambor-edlothiad/battle/disintegrate.c", "/guilds/drambor-edlothiad/battle/arcane-barrage.c");
    addChild("/guilds/drambor-edlothiad/battle/destructive-power.c", "/guilds/drambor-edlothiad/battle/elven-battle-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/elemental-mastery.c");
    addChild("/guilds/drambor-edlothiad/battle/elemental-mastery.c", "/guilds/drambor-edlothiad/battle/destructive-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/spell-penetration-supremacy.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/overwhelming-force.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/thunderbolt.c");
    addChild("/guilds/drambor-edlothiad/battle/spell-penetration-supremacy.c", "/guilds/drambor-edlothiad/battle/spell-penetration-mastery.c");
    addChild("/guilds/drambor-edlothiad/battle/overwhelming-force.c", "/guilds/drambor-edlothiad/battle/destructive-power.c");
    addChild("/guilds/drambor-edlothiad/battle/thunderbolt.c", "/guilds/drambor-edlothiad/battle/tempest.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/meteor-storm.c");
    addChild("/guilds/drambor-edlothiad/battle/meteor-storm.c", "/guilds/drambor-edlothiad/battle/inferno.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/absolute-cold.c");
    addChild("/guilds/drambor-edlothiad/battle/absolute-cold.c", "/guilds/drambor-edlothiad/battle/glacial-cascade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/supreme-battle-mastery.c");
    addChild("/guilds/drambor-edlothiad/battle/supreme-battle-mastery.c", "/guilds/drambor-edlothiad/battle/overwhelming-force.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/annihilating-barrage.c");
    addChild("/guilds/drambor-edlothiad/battle/annihilating-barrage.c", "/guilds/drambor-edlothiad/battle/disintegrate.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/supreme-devastation.c");
    addChild("/guilds/drambor-edlothiad/battle/supreme-devastation.c", "/guilds/drambor-edlothiad/battle/supreme-battle-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/spell-penetration-transcendence.c");
    addResearchElement("/guilds/drambor-edlothiad/battle/transcendent-power.c");
    addChild("/guilds/drambor-edlothiad/battle/spell-penetration-transcendence.c", "/guilds/drambor-edlothiad/battle/spell-penetration-supremacy.c");
    addChild("/guilds/drambor-edlothiad/battle/transcendent-power.c", "/guilds/drambor-edlothiad/battle/supreme-battle-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/storm-of-ages.c");
    addChild("/guilds/drambor-edlothiad/battle/storm-of-ages.c", "/guilds/drambor-edlothiad/battle/thunderbolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/elven-devastation.c");
    addChild("/guilds/drambor-edlothiad/battle/elven-devastation.c", "/guilds/drambor-edlothiad/battle/transcendent-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/reality-tear.c");
    addChild("/guilds/drambor-edlothiad/battle/reality-tear.c", "/guilds/drambor-edlothiad/battle/annihilating-barrage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/legendary-battle-mastery.c");
    addChild("/guilds/drambor-edlothiad/battle/legendary-battle-mastery.c", "/guilds/drambor-edlothiad/battle/elven-devastation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/arcane-cataclysm.c");
    addChild("/guilds/drambor-edlothiad/battle/arcane-cataclysm.c", "/guilds/drambor-edlothiad/battle/reality-tear.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/battle/godlike-power.c");
    addChild("/guilds/drambor-edlothiad/battle/godlike-power.c", "/guilds/drambor-edlothiad/battle/legendary-battle-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Battle Magic");
    Description("This research tree focuses on combat-oriented spells designed "
        "for use in the heat of battle, from basic arcane bolts to "
        "devastating cataclysms of magical destruction.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/battle/root.c");
    TreeRoot("/guilds/drambor-edlothiad/battle/root.c");

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
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    SixtyFirstLevel();
    SixtyFifthLevel();
}
