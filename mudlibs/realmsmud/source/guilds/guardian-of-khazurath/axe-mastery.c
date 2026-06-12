//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/cleave.c");
    addChild("/guilds/guardian-of-khazurath/axe/cleave.c",
        "/guilds/guardian-of-khazurath/axe/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/edge-discipline.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/axe-warding.c");

    addChild("/guilds/guardian-of-khazurath/axe/edge-discipline.c",
        "/guilds/guardian-of-khazurath/axe/root.c");
    addChild("/guilds/guardian-of-khazurath/axe/axe-warding.c",
        "/guilds/guardian-of-khazurath/axe/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/rending-blow.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/axe-focus.c");

    addChild("/guilds/guardian-of-khazurath/axe/rending-blow.c",
        "/guilds/guardian-of-khazurath/axe/cleave.c");
    addChild("/guilds/guardian-of-khazurath/axe/axe-focus.c",
        "/guilds/guardian-of-khazurath/axe/edge-discipline.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/swiftstrike-mastery.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/weapon-guard.c");

    addChild("/guilds/guardian-of-khazurath/axe/swiftstrike-mastery.c",
        "/guilds/guardian-of-khazurath/axe/cleave.c");
    addChild("/guilds/guardian-of-khazurath/axe/weapon-guard.c",
        "/guilds/guardian-of-khazurath/axe/axe-warding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/cutting-insight.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/iron-endurance.c");

    addChild("/guilds/guardian-of-khazurath/axe/cutting-insight.c",
        "/guilds/guardian-of-khazurath/axe/cleave.c");
    addChild("/guilds/guardian-of-khazurath/axe/iron-endurance.c",
        "/guilds/guardian-of-khazurath/axe/cleave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/brutal-cleave.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/axe-affinity.c");

    addChild("/guilds/guardian-of-khazurath/axe/brutal-cleave.c",
        "/guilds/guardian-of-khazurath/axe/rending-blow.c");
    addChild("/guilds/guardian-of-khazurath/axe/axe-affinity.c",
        "/guilds/guardian-of-khazurath/axe/axe-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/blood-hunger.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/battle-tempo.c");

    addChild("/guilds/guardian-of-khazurath/axe/blood-hunger.c",
        "/guilds/guardian-of-khazurath/axe/cutting-insight.c");
    addChild("/guilds/guardian-of-khazurath/axe/battle-tempo.c",
        "/guilds/guardian-of-khazurath/axe/swiftstrike-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/relentless-assault.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/dwarven-reserve.c");

    addChild("/guilds/guardian-of-khazurath/axe/relentless-assault.c",
        "/guilds/guardian-of-khazurath/axe/brutal-cleave.c");
    addChild("/guilds/guardian-of-khazurath/axe/dwarven-reserve.c",
        "/guilds/guardian-of-khazurath/axe/iron-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/brutal-edge.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/assault-efficiency.c");

    addChild("/guilds/guardian-of-khazurath/axe/brutal-edge.c",
        "/guilds/guardian-of-khazurath/axe/axe-affinity.c");
    addChild("/guilds/guardian-of-khazurath/axe/assault-efficiency.c",
        "/guilds/guardian-of-khazurath/axe/relentless-assault.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/ironside-stance.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/relentless-tempo.c");

    addChild("/guilds/guardian-of-khazurath/axe/ironside-stance.c",
        "/guilds/guardian-of-khazurath/axe/weapon-guard.c");
    addChild("/guilds/guardian-of-khazurath/axe/relentless-tempo.c",
        "/guilds/guardian-of-khazurath/axe/battle-tempo.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/headsmans-arc.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/deep-rend.c");

    addChild("/guilds/guardian-of-khazurath/axe/headsmans-arc.c",
        "/guilds/guardian-of-khazurath/axe/brutal-cleave.c");
    addChild("/guilds/guardian-of-khazurath/axe/deep-rend.c",
        "/guilds/guardian-of-khazurath/axe/blood-hunger.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/dwarven-axe-lore.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/assault-power.c");

    addChild("/guilds/guardian-of-khazurath/axe/dwarven-axe-lore.c",
        "/guilds/guardian-of-khazurath/axe/axe-affinity.c");
    addChild("/guilds/guardian-of-khazurath/axe/assault-power.c",
        "/guilds/guardian-of-khazurath/axe/assault-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/battle-hardened.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/dwarven-haste.c");

    addChild("/guilds/guardian-of-khazurath/axe/battle-hardened.c",
        "/guilds/guardian-of-khazurath/axe/dwarven-reserve.c");
    addChild("/guilds/guardian-of-khazurath/axe/dwarven-haste.c",
        "/guilds/guardian-of-khazurath/axe/relentless-tempo.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/battle-poise.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/unyielding-strike.c");

    addChild("/guilds/guardian-of-khazurath/axe/battle-poise.c",
        "/guilds/guardian-of-khazurath/axe/ironside-stance.c");
    addChild("/guilds/guardian-of-khazurath/axe/unyielding-strike.c",
        "/guilds/guardian-of-khazurath/axe/assault-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/masters-edge.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/khazurath-precision.c");

    addChild("/guilds/guardian-of-khazurath/axe/masters-edge.c",
        "/guilds/guardian-of-khazurath/axe/brutal-edge.c");
    addChild("/guilds/guardian-of-khazurath/axe/khazurath-precision.c",
        "/guilds/guardian-of-khazurath/axe/deep-rend.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/axe-flow.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/stone-hide-passive.c");

    addChild("/guilds/guardian-of-khazurath/axe/axe-flow.c",
        "/guilds/guardian-of-khazurath/axe/dwarven-haste.c");
    addChild("/guilds/guardian-of-khazurath/axe/stone-hide-passive.c",
        "/guilds/guardian-of-khazurath/axe/battle-poise.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/mountain-strike.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/axe-economy.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/stamina-mastery.c");

    addChild("/guilds/guardian-of-khazurath/axe/mountain-strike.c",
        "/guilds/guardian-of-khazurath/axe/headsmans-arc.c");
    addChild("/guilds/guardian-of-khazurath/axe/axe-economy.c",
        "/guilds/guardian-of-khazurath/axe/battle-hardened.c");
    addChild("/guilds/guardian-of-khazurath/axe/stamina-mastery.c",
        "/guilds/guardian-of-khazurath/axe/battle-hardened.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/axe/endless-assault.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/axe-mastery-bonus.c");
    addResearchElement("/guilds/guardian-of-khazurath/axe/battle-fury-passive.c");

    addChild("/guilds/guardian-of-khazurath/axe/endless-assault.c",
        "/guilds/guardian-of-khazurath/axe/khazurath-precision.c");
    addChild("/guilds/guardian-of-khazurath/axe/axe-mastery-bonus.c",
        "/guilds/guardian-of-khazurath/axe/masters-edge.c");
    addChild("/guilds/guardian-of-khazurath/axe/battle-fury-passive.c",
        "/guilds/guardian-of-khazurath/axe/unyielding-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Axe Mastery");
    Description("The axe-craft of Khazurath, passed down from the weaponmasters "
        "of Mirost. This tree teaches the art of cleaving, wounding, and the "
        "sustained offensive pressure that makes a Guardian's axe feared.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/axe/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/axe/root.c");

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
    TwentySixthLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortiethLevel();
}
