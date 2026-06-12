//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/arcane-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-weave.c");

    addChild("/guilds/drambor-edlothiad/spellsword/arcane-strike.c",
        "/guilds/drambor-edlothiad/spellsword/root.c");
    addChild("/guilds/drambor-edlothiad/spellsword/spell-weave.c",
        "/guilds/drambor-edlothiad/spellsword/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-strike-training.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/battle-casting.c");

    addChild("/guilds/drambor-edlothiad/spellsword/spell-strike-training.c",
        "/guilds/drambor-edlothiad/spellsword/arcane-strike.c");
    addChild("/guilds/drambor-edlothiad/spellsword/battle-casting.c",
        "/guilds/drambor-edlothiad/spellsword/spell-weave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/shocking-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/frost-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/shocking-strike.c",
        "/guilds/drambor-edlothiad/spellsword/spell-strike-training.c");
    addChild("/guilds/drambor-edlothiad/spellsword/frost-strike.c",
        "/guilds/drambor-edlothiad/spellsword/spell-strike-training.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/flame-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/seamless-weave.c");

    addChild("/guilds/drambor-edlothiad/spellsword/flame-strike.c",
        "/guilds/drambor-edlothiad/spellsword/spell-strike-training.c");
    addChild("/guilds/drambor-edlothiad/spellsword/seamless-weave.c",
        "/guilds/drambor-edlothiad/spellsword/battle-casting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-blade-conditioning.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/shadow-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/spell-blade-conditioning.c",
        "/guilds/drambor-edlothiad/spellsword/flame-strike.c");
    addChild("/guilds/drambor-edlothiad/spellsword/shadow-strike.c",
        "/guilds/drambor-edlothiad/spellsword/spell-blade-conditioning.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/corrosive-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/war-caster.c");

    addChild("/guilds/drambor-edlothiad/spellsword/corrosive-strike.c",
        "/guilds/drambor-edlothiad/spellsword/spell-blade-conditioning.c");
    addChild("/guilds/drambor-edlothiad/spellsword/war-caster.c",
        "/guilds/drambor-edlothiad/spellsword/seamless-weave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/empowered-strikes.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/sonic-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/empowered-strikes.c",
        "/guilds/drambor-edlothiad/spellsword/corrosive-strike.c");
    addChild("/guilds/drambor-edlothiad/spellsword/sonic-strike.c",
        "/guilds/drambor-edlothiad/spellsword/empowered-strikes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/force-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/arcane-flow.c");

    addChild("/guilds/drambor-edlothiad/spellsword/force-strike.c",
        "/guilds/drambor-edlothiad/spellsword/empowered-strikes.c");
    addChild("/guilds/drambor-edlothiad/spellsword/arcane-flow.c",
        "/guilds/drambor-edlothiad/spellsword/war-caster.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/mana-surge.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/holy-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/mana-surge.c",
        "/guilds/drambor-edlothiad/spellsword/force-strike.c");
    addChild("/guilds/drambor-edlothiad/spellsword/holy-strike.c",
        "/guilds/drambor-edlothiad/spellsword/empowered-strikes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/chaos-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/eldritch-combatant.c");

    addChild("/guilds/drambor-edlothiad/spellsword/chaos-strike.c",
        "/guilds/drambor-edlothiad/spellsword/force-strike.c");
    addChild("/guilds/drambor-edlothiad/spellsword/eldritch-combatant.c",
        "/guilds/drambor-edlothiad/spellsword/arcane-flow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-blade-focus.c");

    addChild("/guilds/drambor-edlothiad/spellsword/spell-blade-focus.c",
        "/guilds/drambor-edlothiad/spellsword/mana-surge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/annihilation-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-sword-mastery.c");

    addChild("/guilds/drambor-edlothiad/spellsword/annihilation-strike.c",
        "/guilds/drambor-edlothiad/spellsword/chaos-strike.c");
    addChild("/guilds/drambor-edlothiad/spellsword/spell-sword-mastery.c",
        "/guilds/drambor-edlothiad/spellsword/eldritch-combatant.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/elven-spell-arts.c");

    addChild("/guilds/drambor-edlothiad/spellsword/elven-spell-arts.c",
        "/guilds/drambor-edlothiad/spellsword/spell-blade-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/devastating-spell-strikes.c");

    addChild("/guilds/drambor-edlothiad/spellsword/devastating-spell-strikes.c",
        "/guilds/drambor-edlothiad/spellsword/annihilation-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/grand-spell-sword.c");

    addChild("/guilds/drambor-edlothiad/spellsword/grand-spell-sword.c",
        "/guilds/drambor-edlothiad/spellsword/spell-sword-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/elemental-cascade.c");

    addChild("/guilds/drambor-edlothiad/spellsword/elemental-cascade.c",
        "/guilds/drambor-edlothiad/spellsword/devastating-spell-strikes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/arcane-vigor.c");

    addChild("/guilds/drambor-edlothiad/spellsword/arcane-vigor.c",
        "/guilds/drambor-edlothiad/spellsword/grand-spell-sword.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-strike-supremacy.c");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/living-weapon.c");

    addChild("/guilds/drambor-edlothiad/spellsword/spell-strike-supremacy.c",
        "/guilds/drambor-edlothiad/spellsword/elemental-cascade.c");
    addChild("/guilds/drambor-edlothiad/spellsword/living-weapon.c",
        "/guilds/drambor-edlothiad/spellsword/grand-spell-sword.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/arcane-cataclysm-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/arcane-cataclysm-strike.c",
        "/guilds/drambor-edlothiad/spellsword/spell-strike-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/legendary-spell-sword.c");

    addChild("/guilds/drambor-edlothiad/spellsword/legendary-spell-sword.c",
        "/guilds/drambor-edlothiad/spellsword/living-weapon.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/dual-discipline.c");

    addChild("/guilds/drambor-edlothiad/spellsword/dual-discipline.c",
        "/guilds/drambor-edlothiad/spellsword/legendary-spell-sword.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/oblivion-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/oblivion-strike.c",
        "/guilds/drambor-edlothiad/spellsword/arcane-cataclysm-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/transcendent-spell-sword.c");

    addChild("/guilds/drambor-edlothiad/spellsword/transcendent-spell-sword.c",
        "/guilds/drambor-edlothiad/spellsword/legendary-spell-sword.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/void-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/void-strike.c",
        "/guilds/drambor-edlothiad/spellsword/oblivion-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/apex-predator.c");

    addChild("/guilds/drambor-edlothiad/spellsword/apex-predator.c",
        "/guilds/drambor-edlothiad/spellsword/oblivion-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-sword-legend.c");

    addChild("/guilds/drambor-edlothiad/spellsword/spell-sword-legend.c",
        "/guilds/drambor-edlothiad/spellsword/apex-predator.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/worldbreaker-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/worldbreaker-strike.c",
        "/guilds/drambor-edlothiad/spellsword/apex-predator.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/immortal-spell-sword.c");

    addChild("/guilds/drambor-edlothiad/spellsword/immortal-spell-sword.c",
        "/guilds/drambor-edlothiad/spellsword/transcendent-spell-sword.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/spell-sword-perfection.c");

    addChild("/guilds/drambor-edlothiad/spellsword/spell-sword-perfection.c",
        "/guilds/drambor-edlothiad/spellsword/immortal-spell-sword.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/primordial-strike.c");

    addChild("/guilds/drambor-edlothiad/spellsword/primordial-strike.c",
        "/guilds/drambor-edlothiad/spellsword/worldbreaker-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/spellsword/avatar-of-the-spell-sword.c");

    addChild("/guilds/drambor-edlothiad/spellsword/avatar-of-the-spell-sword.c",
        "/guilds/drambor-edlothiad/spellsword/spell-sword-perfection.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Spell Sword");
    Description("This research tree teaches the art of the spell sword - "
        "seamlessly weaving arcane casting into melee combat, channeling "
        "destructive spells through blade strikes with devastating effect.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/spellsword/root.c");
    TreeRoot("/guilds/drambor-edlothiad/spellsword/root.c");

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
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SeventyLevel();
}
