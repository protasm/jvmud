//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/functions/death.c");
    addResearchElement("/guilds/necromancer/functions/shadow.c");

    addChild("/guilds/necromancer/functions/death.c",
        "/guilds/necromancer/functions/root.c");
    addChild("/guilds/necromancer/functions/shadow.c",
        "/guilds/necromancer/functions/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/functions/decay.c");
    addResearchElement("/guilds/necromancer/functions/blight.c");

    addChild("/guilds/necromancer/functions/decay.c",
        "/guilds/necromancer/functions/death.c");
    addChild("/guilds/necromancer/functions/blight.c",
        "/guilds/necromancer/functions/shadow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/functions/necrotic.c");
    addResearchElement("/guilds/necromancer/functions/void.c");

    addChild("/guilds/necromancer/functions/necrotic.c",
        "/guilds/necromancer/functions/decay.c");
    addChild("/guilds/necromancer/functions/void.c",
        "/guilds/necromancer/functions/blight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/functions/entropy.c");
    addResearchElement("/guilds/necromancer/functions/grave.c");

    addChild("/guilds/necromancer/functions/entropy.c",
        "/guilds/necromancer/functions/necrotic.c");
    addChild("/guilds/necromancer/functions/grave.c",
        "/guilds/necromancer/functions/void.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/functions/undead.c");
    addResearchElement("/guilds/necromancer/functions/soul.c");

    addChild("/guilds/necromancer/functions/undead.c",
        "/guilds/necromancer/functions/entropy.c");
    addChild("/guilds/necromancer/functions/soul.c",
        "/guilds/necromancer/functions/grave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/functions/void-death.c");

    addChild("/guilds/necromancer/functions/void-death.c",
        "/guilds/necromancer/functions/soul.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Necrotic Spell Functions");
    Description("This research tree provides knowledge of the elemental "
        "functions for constructing necromancer spells.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/functions/root.c");
    TreeRoot("/guilds/necromancer/functions/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
}
