//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/rending-claws.c");
    addResearchElement("/guilds/necromancer/ghoul/necrotic-stench.c");

    addChild("/guilds/necromancer/ghoul/rending-claws.c",
        "/guilds/necromancer/ghoul/root.c");
    addChild("/guilds/necromancer/ghoul/necrotic-stench.c",
        "/guilds/necromancer/ghoul/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/paralyzing-bite.c");
    addResearchElement("/guilds/necromancer/ghoul/carrion-hide.c");
    addResearchElement("/guilds/necromancer/ghoul/grave-hunger.c");

    addChild("/guilds/necromancer/ghoul/paralyzing-bite.c",
        "/guilds/necromancer/ghoul/rending-claws.c");
    addChild("/guilds/necromancer/ghoul/carrion-hide.c",
        "/guilds/necromancer/ghoul/necrotic-stench.c");
    addChild("/guilds/necromancer/ghoul/grave-hunger.c",
        "/guilds/necromancer/ghoul/necrotic-stench.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/venom-claws.c");
    addResearchElement("/guilds/necromancer/ghoul/corpse-hide.c");
    addResearchElement("/guilds/necromancer/ghoul/feeding-frenzy.c");
    addResearchElement("/guilds/necromancer/ghoul/pack-hunter.c");

    addChild("/guilds/necromancer/ghoul/venom-claws.c",
        "/guilds/necromancer/ghoul/paralyzing-bite.c");
    addChild("/guilds/necromancer/ghoul/corpse-hide.c",
        "/guilds/necromancer/ghoul/carrion-hide.c");
    addChild("/guilds/necromancer/ghoul/feeding-frenzy.c",
        "/guilds/necromancer/ghoul/grave-hunger.c");
    addChild("/guilds/necromancer/ghoul/pack-hunter.c",
        "/guilds/necromancer/ghoul/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/toxic-bite.c");
    addResearchElement("/guilds/necromancer/ghoul/iron-hide.c");
    addResearchElement("/guilds/necromancer/ghoul/blood-frenzy.c");
    addResearchElement("/guilds/necromancer/ghoul/extra-ghoul.c");

    addChild("/guilds/necromancer/ghoul/toxic-bite.c",
        "/guilds/necromancer/ghoul/venom-claws.c");
    addChild("/guilds/necromancer/ghoul/iron-hide.c",
        "/guilds/necromancer/ghoul/corpse-hide.c");
    addChild("/guilds/necromancer/ghoul/blood-frenzy.c",
        "/guilds/necromancer/ghoul/feeding-frenzy.c");
    addChild("/guilds/necromancer/ghoul/extra-ghoul.c",
        "/guilds/necromancer/ghoul/pack-hunter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/plague-bite.c");
    addResearchElement("/guilds/necromancer/ghoul/necrotic-armor.c");
    addResearchElement("/guilds/necromancer/ghoul/savage-frenzy.c");
    addResearchElement("/guilds/necromancer/ghoul/pack-coordination.c");
    addResearchElement("/guilds/necromancer/ghoul/death-leap.c");

    addChild("/guilds/necromancer/ghoul/plague-bite.c",
        "/guilds/necromancer/ghoul/toxic-bite.c");
    addChild("/guilds/necromancer/ghoul/necrotic-armor.c",
        "/guilds/necromancer/ghoul/iron-hide.c");
    addChild("/guilds/necromancer/ghoul/savage-frenzy.c",
        "/guilds/necromancer/ghoul/blood-frenzy.c");
    addChild("/guilds/necromancer/ghoul/pack-coordination.c",
        "/guilds/necromancer/ghoul/extra-ghoul.c");
    addChild("/guilds/necromancer/ghoul/death-leap.c",
        "/guilds/necromancer/ghoul/rending-claws.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/virulent-plague-bite.c");
    addResearchElement("/guilds/necromancer/ghoul/bone-plating.c");
    addResearchElement("/guilds/necromancer/ghoul/berserker-frenzy.c");
    addResearchElement("/guilds/necromancer/ghoul/pack-ambush.c");
    addResearchElement("/guilds/necromancer/ghoul/rending-leap.c");

    addChild("/guilds/necromancer/ghoul/virulent-plague-bite.c",
        "/guilds/necromancer/ghoul/plague-bite.c");
    addChild("/guilds/necromancer/ghoul/bone-plating.c",
        "/guilds/necromancer/ghoul/necrotic-armor.c");
    addChild("/guilds/necromancer/ghoul/berserker-frenzy.c",
        "/guilds/necromancer/ghoul/savage-frenzy.c");
    addChild("/guilds/necromancer/ghoul/pack-ambush.c",
        "/guilds/necromancer/ghoul/pack-coordination.c");
    addChild("/guilds/necromancer/ghoul/rending-leap.c",
        "/guilds/necromancer/ghoul/death-leap.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/death-plague.c");
    addResearchElement("/guilds/necromancer/ghoul/titanium-hide.c");
    addResearchElement("/guilds/necromancer/ghoul/slaughter-frenzy.c");
    addResearchElement("/guilds/necromancer/ghoul/pack-devastation.c");

    addChild("/guilds/necromancer/ghoul/death-plague.c",
        "/guilds/necromancer/ghoul/virulent-plague-bite.c");
    addChild("/guilds/necromancer/ghoul/titanium-hide.c",
        "/guilds/necromancer/ghoul/bone-plating.c");
    addChild("/guilds/necromancer/ghoul/slaughter-frenzy.c",
        "/guilds/necromancer/ghoul/berserker-frenzy.c");
    addChild("/guilds/necromancer/ghoul/pack-devastation.c",
        "/guilds/necromancer/ghoul/pack-ambush.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/apocalyptic-plague.c");
    addResearchElement("/guilds/necromancer/ghoul/ghoul-lord.c");
    addResearchElement("/guilds/necromancer/ghoul/massacre-frenzy.c");

    addChild("/guilds/necromancer/ghoul/apocalyptic-plague.c",
        "/guilds/necromancer/ghoul/death-plague.c");
    addChild("/guilds/necromancer/ghoul/ghoul-lord.c",
        "/guilds/necromancer/ghoul/titanium-hide.c");
    addChild("/guilds/necromancer/ghoul/massacre-frenzy.c",
        "/guilds/necromancer/ghoul/slaughter-frenzy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/plague-lord.c");
    addResearchElement("/guilds/necromancer/ghoul/undead-champion.c");
    addResearchElement("/guilds/necromancer/ghoul/death-frenzy.c");

    addChild("/guilds/necromancer/ghoul/plague-lord.c",
        "/guilds/necromancer/ghoul/apocalyptic-plague.c");
    addChild("/guilds/necromancer/ghoul/undead-champion.c",
        "/guilds/necromancer/ghoul/ghoul-lord.c");
    addChild("/guilds/necromancer/ghoul/death-frenzy.c",
        "/guilds/necromancer/ghoul/massacre-frenzy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/plague-sovereign.c");
    addResearchElement("/guilds/necromancer/ghoul/pack-supreme.c");

    addChild("/guilds/necromancer/ghoul/plague-sovereign.c",
        "/guilds/necromancer/ghoul/plague-lord.c");
    addChild("/guilds/necromancer/ghoul/pack-supreme.c",
        "/guilds/necromancer/ghoul/undead-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/ghoul/lord-of-the-pack.c");

    addChild("/guilds/necromancer/ghoul/lord-of-the-pack.c",
        "/guilds/necromancer/ghoul/plague-sovereign.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Ghoul Pack");
    Description("This research tree provides the knowledge necessary for "
        "raising and enhancing ghoul minions.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/ghoul/root.c");
    TreeRoot("/guilds/necromancer/ghoul/root.c");

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
