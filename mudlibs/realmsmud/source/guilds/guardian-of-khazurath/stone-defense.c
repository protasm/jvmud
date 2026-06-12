//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/stone-skin.c");
    addChild("/guilds/guardian-of-khazurath/defense/stone-skin.c",
        "/guilds/guardian-of-khazurath/defense/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/dwarven-guard.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/stone-soak.c");

    addChild("/guilds/guardian-of-khazurath/defense/dwarven-guard.c",
        "/guilds/guardian-of-khazurath/defense/root.c");
    addChild("/guilds/guardian-of-khazurath/defense/stone-soak.c",
        "/guilds/guardian-of-khazurath/defense/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/parry-mastery.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/granite-hide.c");

    addChild("/guilds/guardian-of-khazurath/defense/parry-mastery.c",
        "/guilds/guardian-of-khazurath/defense/dwarven-guard.c");
    addChild("/guilds/guardian-of-khazurath/defense/granite-hide.c",
        "/guilds/guardian-of-khazurath/defense/stone-soak.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/granite-defense.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/stone-stance.c");

    addChild("/guilds/guardian-of-khazurath/defense/granite-defense.c",
        "/guilds/guardian-of-khazurath/defense/stone-skin.c");
    addChild("/guilds/guardian-of-khazurath/defense/stone-stance.c",
        "/guilds/guardian-of-khazurath/defense/dwarven-guard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/deflection.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/iron-skin.c");

    addChild("/guilds/guardian-of-khazurath/defense/deflection.c",
        "/guilds/guardian-of-khazurath/defense/parry-mastery.c");
    addChild("/guilds/guardian-of-khazurath/defense/iron-skin.c",
        "/guilds/guardian-of-khazurath/defense/granite-hide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/shield-mastery.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/stone-endurance.c");

    addChild("/guilds/guardian-of-khazurath/defense/shield-mastery.c",
        "/guilds/guardian-of-khazurath/defense/stone-stance.c");
    addChild("/guilds/guardian-of-khazurath/defense/stone-endurance.c",
        "/guilds/guardian-of-khazurath/defense/iron-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/ward-of-stone.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/rock-skin.c");

    addChild("/guilds/guardian-of-khazurath/defense/ward-of-stone.c",
        "/guilds/guardian-of-khazurath/defense/deflection.c");
    addChild("/guilds/guardian-of-khazurath/defense/rock-skin.c",
        "/guilds/guardian-of-khazurath/defense/stone-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/mountain-shield.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/battle-hardening.c");

    addChild("/guilds/guardian-of-khazurath/defense/mountain-shield.c",
        "/guilds/guardian-of-khazurath/defense/granite-defense.c");
    addChild("/guilds/guardian-of-khazurath/defense/battle-hardening.c",
        "/guilds/guardian-of-khazurath/defense/shield-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/stone-bulwark.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/dwarf-iron-hide.c");

    addChild("/guilds/guardian-of-khazurath/defense/stone-bulwark.c",
        "/guilds/guardian-of-khazurath/defense/ward-of-stone.c");
    addChild("/guilds/guardian-of-khazurath/defense/dwarf-iron-hide.c",
        "/guilds/guardian-of-khazurath/defense/rock-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/living-fortress.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/mountain-guard.c");

    addChild("/guilds/guardian-of-khazurath/defense/living-fortress.c",
        "/guilds/guardian-of-khazurath/defense/battle-hardening.c");
    addChild("/guilds/guardian-of-khazurath/defense/mountain-guard.c",
        "/guilds/guardian-of-khazurath/defense/mountain-shield.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/immovable.c");

    addChild("/guilds/guardian-of-khazurath/defense/immovable.c",
        "/guilds/guardian-of-khazurath/defense/stone-bulwark.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/basalt-skin.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/stalwart.c");

    addChild("/guilds/guardian-of-khazurath/defense/basalt-skin.c",
        "/guilds/guardian-of-khazurath/defense/dwarf-iron-hide.c");
    addChild("/guilds/guardian-of-khazurath/defense/stalwart.c",
        "/guilds/guardian-of-khazurath/defense/living-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/impenetrable-stone.c");

    addChild("/guilds/guardian-of-khazurath/defense/impenetrable-stone.c",
        "/guilds/guardian-of-khazurath/defense/mountain-guard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/stone-dominance.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/granite-wall.c");

    addChild("/guilds/guardian-of-khazurath/defense/stone-dominance.c",
        "/guilds/guardian-of-khazurath/defense/immovable.c");
    addChild("/guilds/guardian-of-khazurath/defense/granite-wall.c",
        "/guilds/guardian-of-khazurath/defense/basalt-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/unassailable.c");

    addChild("/guilds/guardian-of-khazurath/defense/unassailable.c",
        "/guilds/guardian-of-khazurath/defense/stalwart.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/mountain-skin.c");

    addChild("/guilds/guardian-of-khazurath/defense/mountain-skin.c",
        "/guilds/guardian-of-khazurath/defense/granite-wall.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/fortress.c");

    addChild("/guilds/guardian-of-khazurath/defense/fortress.c",
        "/guilds/guardian-of-khazurath/defense/impenetrable-stone.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/titan-guard.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/deep-stone-hide.c");

    addChild("/guilds/guardian-of-khazurath/defense/titan-guard.c",
        "/guilds/guardian-of-khazurath/defense/stone-dominance.c");
    addChild("/guilds/guardian-of-khazurath/defense/deep-stone-hide.c",
        "/guilds/guardian-of-khazurath/defense/mountain-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/khazurath-defense.c");

    addChild("/guilds/guardian-of-khazurath/defense/khazurath-defense.c",
        "/guilds/guardian-of-khazurath/defense/unassailable.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/living-mountain.c");

    addChild("/guilds/guardian-of-khazurath/defense/living-mountain.c",
        "/guilds/guardian-of-khazurath/defense/fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/absolute-guard.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/petrified-hide.c");

    addChild("/guilds/guardian-of-khazurath/defense/absolute-guard.c",
        "/guilds/guardian-of-khazurath/defense/titan-guard.c");
    addChild("/guilds/guardian-of-khazurath/defense/petrified-hide.c",
        "/guilds/guardian-of-khazurath/defense/deep-stone-hide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/paragon-defense.c");
    addResearchElement("/guilds/guardian-of-khazurath/defense/mountain-iron-hide.c");

    addChild("/guilds/guardian-of-khazurath/defense/paragon-defense.c",
        "/guilds/guardian-of-khazurath/defense/absolute-guard.c");
    addChild("/guilds/guardian-of-khazurath/defense/mountain-iron-hide.c",
        "/guilds/guardian-of-khazurath/defense/petrified-hide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/eternal-bulwark.c");

    addChild("/guilds/guardian-of-khazurath/defense/eternal-bulwark.c",
        "/guilds/guardian-of-khazurath/defense/khazurath-defense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/stone-eternal.c");

    addChild("/guilds/guardian-of-khazurath/defense/stone-eternal.c",
        "/guilds/guardian-of-khazurath/defense/paragon-defense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/defense/wall-of-khazurath.c");

    addChild("/guilds/guardian-of-khazurath/defense/wall-of-khazurath.c",
        "/guilds/guardian-of-khazurath/defense/living-mountain.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Stone Defense");
    Description("The defensive mastery of Khazurath - the art of becoming "
        "immovable, impenetrable, and unyielding. Guardians who walk this "
        "path transform their body into living stone, adding layer upon layer "
        "of passive defense, soak, and parry until no blow can break them.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/defense/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/defense/root.c");

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
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FiftySecondLevel();
    FiftySeventhLevel();
    SixtySecondLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
