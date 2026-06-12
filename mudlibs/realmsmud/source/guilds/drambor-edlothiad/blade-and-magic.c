//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/spellblade-form.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-parry.c");
    addChild("/guilds/drambor-edlothiad/blade/spellblade-form.c", "/guilds/drambor-edlothiad/blade/root.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-parry.c", "/guilds/drambor-edlothiad/blade/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/mana-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/blade-ward.c");
    addChild("/guilds/drambor-edlothiad/blade/mana-strike.c", "/guilds/drambor-edlothiad/blade/spellblade-form.c");
    addChild("/guilds/drambor-edlothiad/blade/blade-ward.c", "/guilds/drambor-edlothiad/blade/arcane-parry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/spell-riposte.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/focused-channeling.c");
    addChild("/guilds/drambor-edlothiad/blade/spell-riposte.c", "/guilds/drambor-edlothiad/blade/arcane-parry.c");
    addChild("/guilds/drambor-edlothiad/blade/focused-channeling.c", "/guilds/drambor-edlothiad/blade/mana-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-slash.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/elven-swordcraft.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-slash.c", "/guilds/drambor-edlothiad/blade/focused-channeling.c");
    addChild("/guilds/drambor-edlothiad/blade/elven-swordcraft.c", "/guilds/drambor-edlothiad/blade/spellblade-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/spell-weave.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-deflection.c");
    addChild("/guilds/drambor-edlothiad/blade/spell-weave.c", "/guilds/drambor-edlothiad/blade/arcane-slash.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-deflection.c", "/guilds/drambor-edlothiad/blade/blade-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/bladesong-stance.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/mana-flow.c");
    addChild("/guilds/drambor-edlothiad/blade/bladesong-stance.c", "/guilds/drambor-edlothiad/blade/spell-riposte.c");
    addChild("/guilds/drambor-edlothiad/blade/mana-flow.c", "/guilds/drambor-edlothiad/blade/focused-channeling.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/runic-blade.c");
    addChild("/guilds/drambor-edlothiad/blade/runic-blade.c", "/guilds/drambor-edlothiad/blade/spell-weave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/battle-meditation.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/swift-casting.c");
    addChild("/guilds/drambor-edlothiad/blade/battle-meditation.c", "/guilds/drambor-edlothiad/blade/elven-swordcraft.c");
    addChild("/guilds/drambor-edlothiad/blade/swift-casting.c", "/guilds/drambor-edlothiad/blade/mana-flow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/mystic-guard.c");
    addChild("/guilds/drambor-edlothiad/blade/mystic-guard.c", "/guilds/drambor-edlothiad/blade/bladesong-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/precision-of-ancients.c");
    addChild("/guilds/drambor-edlothiad/blade/precision-of-ancients.c", "/guilds/drambor-edlothiad/blade/elven-swordcraft.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/spell-weave-mastery.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-precision.c");
    addChild("/guilds/drambor-edlothiad/blade/spell-weave-mastery.c", "/guilds/drambor-edlothiad/blade/spell-weave.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-precision.c", "/guilds/drambor-edlothiad/blade/arcane-deflection.c");
}
/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/spellstorm-strike.c");
    addChild("/guilds/drambor-edlothiad/blade/spellstorm-strike.c", "/guilds/drambor-edlothiad/blade/runic-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/advanced-bladesong.c");
    addChild("/guilds/drambor-edlothiad/blade/advanced-bladesong.c", "/guilds/drambor-edlothiad/blade/bladesong-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/war-caster.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/blade-harmony.c");
    addChild("/guilds/drambor-edlothiad/blade/war-caster.c", "/guilds/drambor-edlothiad/blade/battle-meditation.c");
    addChild("/guilds/drambor-edlothiad/blade/blade-harmony.c", "/guilds/drambor-edlothiad/blade/swift-casting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-riposte.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-riposte.c", "/guilds/drambor-edlothiad/blade/mystic-guard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/battlemage-reflexes.c");
    addChild("/guilds/drambor-edlothiad/blade/battlemage-reflexes.c", "/guilds/drambor-edlothiad/blade/precision-of-ancients.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/spell-weave-transcendence.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/unflinching-focus.c");
    addChild("/guilds/drambor-edlothiad/blade/spell-weave-transcendence.c", "/guilds/drambor-edlothiad/blade/spell-weave-mastery.c");
    addChild("/guilds/drambor-edlothiad/blade/unflinching-focus.c", "/guilds/drambor-edlothiad/blade/arcane-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/battlemage-supremacy.c");
    addChild("/guilds/drambor-edlothiad/blade/battlemage-supremacy.c", "/guilds/drambor-edlothiad/blade/war-caster.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-annihilation.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/combat-arcana.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-annihilation.c", "/guilds/drambor-edlothiad/blade/spellstorm-strike.c");
    addChild("/guilds/drambor-edlothiad/blade/combat-arcana.c", "/guilds/drambor-edlothiad/blade/blade-harmony.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/perfect-bladesong.c");
    addChild("/guilds/drambor-edlothiad/blade/perfect-bladesong.c", "/guilds/drambor-edlothiad/blade/advanced-bladesong.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/elven-war-mastery.c");
    addChild("/guilds/drambor-edlothiad/blade/elven-war-mastery.c", "/guilds/drambor-edlothiad/blade/battlemage-reflexes.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-fortress.c");
    addResearchElement("/guilds/drambor-edlothiad/blade/arcane-mastery.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-fortress.c", "/guilds/drambor-edlothiad/blade/arcane-riposte.c");
    addChild("/guilds/drambor-edlothiad/blade/arcane-mastery.c", "/guilds/drambor-edlothiad/blade/unflinching-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/spellblade-perfection.c");
    addChild("/guilds/drambor-edlothiad/blade/spellblade-perfection.c", "/guilds/drambor-edlothiad/blade/battlemage-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/steel-and-sorcery.c");
    addChild("/guilds/drambor-edlothiad/blade/steel-and-sorcery.c", "/guilds/drambor-edlothiad/blade/combat-arcana.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/bladestorm-mastery.c");
    addChild("/guilds/drambor-edlothiad/blade/bladestorm-mastery.c", "/guilds/drambor-edlothiad/blade/spell-weave-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/elven-perfection.c");
    addChild("/guilds/drambor-edlothiad/blade/elven-perfection.c", "/guilds/drambor-edlothiad/blade/arcane-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/transcendent-swordplay.c");
    addChild("/guilds/drambor-edlothiad/blade/transcendent-swordplay.c", "/guilds/drambor-edlothiad/blade/elven-war-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/immortal-technique.c");
    addChild("/guilds/drambor-edlothiad/blade/immortal-technique.c", "/guilds/drambor-edlothiad/blade/steel-and-sorcery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/eternal-guard.c");
    addChild("/guilds/drambor-edlothiad/blade/eternal-guard.c", "/guilds/drambor-edlothiad/blade/arcane-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/legendary-swordcraft.c");
    addChild("/guilds/drambor-edlothiad/blade/legendary-swordcraft.c", "/guilds/drambor-edlothiad/blade/elven-perfection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/blade-of-eternity.c");
    addChild("/guilds/drambor-edlothiad/blade/blade-of-eternity.c", "/guilds/drambor-edlothiad/blade/arcane-annihilation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/blade/perfect-fusion.c");
    addChild("/guilds/drambor-edlothiad/blade/perfect-fusion.c", "/guilds/drambor-edlothiad/blade/perfect-bladesong.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Blade and Magic");
    Description("This research tree teaches the fundamental art of combining "
        "swordplay with spellcasting, the core technique of the elven battlemage. "
        "From basic mana strikes to the legendary Perfect Fusion, this tree "
        "represents the heart of the Drambor Edlothiad tradition.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/blade/root.c");
    TreeRoot("/guilds/drambor-edlothiad/blade/root.c");

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
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
}
