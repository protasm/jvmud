//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/zombie/rotting-flesh.c");
    addResearchElement("/guilds/necromancer/zombie/decomposing-body.c");

    addChild("/guilds/necromancer/zombie/rotting-flesh.c",
        "/guilds/necromancer/zombie/root.c");
    addChild("/guilds/necromancer/zombie/decomposing-body.c",
        "/guilds/necromancer/zombie/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/zombie/crushing-blow.c");
    addResearchElement("/guilds/necromancer/zombie/festering-wounds.c");
    addResearchElement("/guilds/necromancer/zombie/undead-resilience.c");

    addChild("/guilds/necromancer/zombie/crushing-blow.c",
        "/guilds/necromancer/zombie/rotting-flesh.c");
    addChild("/guilds/necromancer/zombie/festering-wounds.c",
        "/guilds/necromancer/zombie/decomposing-body.c");
    addChild("/guilds/necromancer/zombie/undead-resilience.c",
        "/guilds/necromancer/zombie/decomposing-body.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/zombie/plague-carrier.c");
    addResearchElement("/guilds/necromancer/zombie/bone-crushing-strike.c");
    addResearchElement("/guilds/necromancer/zombie/necrotic-hide.c");
    addResearchElement("/guilds/necromancer/zombie/shambling-horde.c");

    addChild("/guilds/necromancer/zombie/plague-carrier.c",
        "/guilds/necromancer/zombie/festering-wounds.c");
    addChild("/guilds/necromancer/zombie/bone-crushing-strike.c",
        "/guilds/necromancer/zombie/crushing-blow.c");
    addChild("/guilds/necromancer/zombie/necrotic-hide.c",
        "/guilds/necromancer/zombie/undead-resilience.c");
    addChild("/guilds/necromancer/zombie/shambling-horde.c",
        "/guilds/necromancer/zombie/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/zombie/virulent-plague.c");
    addResearchElement("/guilds/necromancer/zombie/infectious-strike.c");
    addResearchElement("/guilds/necromancer/zombie/armored-corpse.c");
    addResearchElement("/guilds/necromancer/zombie/extra-zombie.c");

    addChild("/guilds/necromancer/zombie/virulent-plague.c",
        "/guilds/necromancer/zombie/plague-carrier.c");
    addChild("/guilds/necromancer/zombie/infectious-strike.c",
        "/guilds/necromancer/zombie/bone-crushing-strike.c");
    addChild("/guilds/necromancer/zombie/armored-corpse.c",
        "/guilds/necromancer/zombie/necrotic-hide.c");
    addChild("/guilds/necromancer/zombie/extra-zombie.c",
        "/guilds/necromancer/zombie/shambling-horde.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/zombie/rampaging-corpse.c");
    addResearchElement("/guilds/necromancer/zombie/death-grip.c");
    addResearchElement("/guilds/necromancer/zombie/rot-aura.c");
    addResearchElement("/guilds/necromancer/zombie/bloated-corpse.c");
    addResearchElement("/guilds/necromancer/zombie/mass-grave.c");

    addChild("/guilds/necromancer/zombie/rampaging-corpse.c",
        "/guilds/necromancer/zombie/infectious-strike.c");
    addChild("/guilds/necromancer/zombie/death-grip.c",
        "/guilds/necromancer/zombie/bone-crushing-strike.c");
    addChild("/guilds/necromancer/zombie/rot-aura.c",
        "/guilds/necromancer/zombie/virulent-plague.c");
    addChild("/guilds/necromancer/zombie/bloated-corpse.c",
        "/guilds/necromancer/zombie/armored-corpse.c");
    addChild("/guilds/necromancer/zombie/mass-grave.c",
        "/guilds/necromancer/zombie/extra-zombie.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/zombie/relentless-assault.c");
    addResearchElement("/guilds/necromancer/zombie/overwhelming-numbers.c");
    addResearchElement("/guilds/necromancer/zombie/necrotic-explosion.c");
    addResearchElement("/guilds/necromancer/zombie/undead-fortitude.c");
    addResearchElement("/guilds/necromancer/zombie/flesh-golem.c");

    addChild("/guilds/necromancer/zombie/relentless-assault.c",
        "/guilds/necromancer/zombie/rampaging-corpse.c");
    addChild("/guilds/necromancer/zombie/overwhelming-numbers.c",
        "/guilds/necromancer/zombie/mass-grave.c");
    addChild("/guilds/necromancer/zombie/necrotic-explosion.c",
        "/guilds/necromancer/zombie/bloated-corpse.c");
    addChild("/guilds/necromancer/zombie/undead-fortitude.c",
        "/guilds/necromancer/zombie/bloated-corpse.c");
    addChild("/guilds/necromancer/zombie/flesh-golem.c",
        "/guilds/necromancer/zombie/armored-corpse.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/zombie/unstoppable-shamble.c");
    addResearchElement("/guilds/necromancer/zombie/zombie-tide.c");
    addResearchElement("/guilds/necromancer/zombie/corpse-explosion.c");
    addResearchElement("/guilds/necromancer/zombie/iron-corpse.c");

    addChild("/guilds/necromancer/zombie/unstoppable-shamble.c",
        "/guilds/necromancer/zombie/relentless-assault.c");
    addChild("/guilds/necromancer/zombie/zombie-tide.c",
        "/guilds/necromancer/zombie/overwhelming-numbers.c");
    addChild("/guilds/necromancer/zombie/corpse-explosion.c",
        "/guilds/necromancer/zombie/necrotic-explosion.c");
    addChild("/guilds/necromancer/zombie/iron-corpse.c",
        "/guilds/necromancer/zombie/flesh-golem.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/zombie/death-march.c");
    addResearchElement("/guilds/necromancer/zombie/risen-champion.c");
    addResearchElement("/guilds/necromancer/zombie/plague-bearer.c");

    addChild("/guilds/necromancer/zombie/death-march.c",
        "/guilds/necromancer/zombie/zombie-tide.c");
    addChild("/guilds/necromancer/zombie/risen-champion.c",
        "/guilds/necromancer/zombie/iron-corpse.c");
    addChild("/guilds/necromancer/zombie/plague-bearer.c",
        "/guilds/necromancer/zombie/corpse-explosion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/zombie/lords-of-undeath.c");
    addResearchElement("/guilds/necromancer/zombie/eternal-horde.c");
    addResearchElement("/guilds/necromancer/zombie/undying-champion.c");

    addChild("/guilds/necromancer/zombie/lords-of-undeath.c",
        "/guilds/necromancer/zombie/death-march.c");
    addChild("/guilds/necromancer/zombie/eternal-horde.c",
        "/guilds/necromancer/zombie/plague-bearer.c");
    addChild("/guilds/necromancer/zombie/undying-champion.c",
        "/guilds/necromancer/zombie/risen-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/zombie/army-of-the-dead.c");
    addResearchElement("/guilds/necromancer/zombie/zombie-apocalypse.c");

    addChild("/guilds/necromancer/zombie/army-of-the-dead.c",
        "/guilds/necromancer/zombie/eternal-horde.c");
    addChild("/guilds/necromancer/zombie/zombie-apocalypse.c",
        "/guilds/necromancer/zombie/lords-of-undeath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/zombie/lord-of-the-horde.c");

    addChild("/guilds/necromancer/zombie/lord-of-the-horde.c",
        "/guilds/necromancer/zombie/army-of-the-dead.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Zombie Horde");
    Description("This research tree provides the knowledge necessary for "
        "raising and enhancing zombie minions.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/zombie/root.c");
    TreeRoot("/guilds/necromancer/zombie/root.c");

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
}
