//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/forms/touch.c");
    addResearchElement("/guilds/necromancer/forms/bolt.c");

    addChild("/guilds/necromancer/forms/touch.c",
        "/guilds/necromancer/forms/root.c");
    addChild("/guilds/necromancer/forms/bolt.c",
        "/guilds/necromancer/forms/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/forms/arc.c");
    addResearchElement("/guilds/necromancer/forms/tendril.c");

    addChild("/guilds/necromancer/forms/arc.c",
        "/guilds/necromancer/forms/bolt.c");
    addChild("/guilds/necromancer/forms/tendril.c",
        "/guilds/necromancer/forms/touch.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/forms/lance.c");
    addResearchElement("/guilds/necromancer/forms/coil.c");

    addChild("/guilds/necromancer/forms/lance.c",
        "/guilds/necromancer/forms/arc.c");
    addChild("/guilds/necromancer/forms/coil.c",
        "/guilds/necromancer/forms/tendril.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/forms/blast.c");
    addResearchElement("/guilds/necromancer/forms/spray.c");

    addChild("/guilds/necromancer/forms/blast.c",
        "/guilds/necromancer/forms/bolt.c");
    addChild("/guilds/necromancer/forms/spray.c",
        "/guilds/necromancer/forms/arc.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/forms/orb.c");
    addResearchElement("/guilds/necromancer/forms/needle.c");

    addChild("/guilds/necromancer/forms/orb.c",
        "/guilds/necromancer/forms/blast.c");
    addChild("/guilds/necromancer/forms/needle.c",
        "/guilds/necromancer/forms/lance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/forms/wave.c");
    addResearchElement("/guilds/necromancer/forms/pulse.c");

    addChild("/guilds/necromancer/forms/wave.c",
        "/guilds/necromancer/forms/spray.c");
    addChild("/guilds/necromancer/forms/pulse.c",
        "/guilds/necromancer/forms/orb.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/forms/mist.c");
    addResearchElement("/guilds/necromancer/forms/shard.c");

    addChild("/guilds/necromancer/forms/mist.c",
        "/guilds/necromancer/forms/wave.c");
    addChild("/guilds/necromancer/forms/shard.c",
        "/guilds/necromancer/forms/needle.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/forms/ring.c");
    addResearchElement("/guilds/necromancer/forms/strike.c");

    addChild("/guilds/necromancer/forms/ring.c",
        "/guilds/necromancer/forms/pulse.c");
    addChild("/guilds/necromancer/forms/strike.c",
        "/guilds/necromancer/forms/coil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/forms/wall.c");
    addResearchElement("/guilds/necromancer/forms/vortex.c");

    addChild("/guilds/necromancer/forms/wall.c",
        "/guilds/necromancer/forms/ring.c");
    addChild("/guilds/necromancer/forms/vortex.c",
        "/guilds/necromancer/forms/mist.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/forms/cage.c");
    addResearchElement("/guilds/necromancer/forms/storm.c");

    addChild("/guilds/necromancer/forms/cage.c",
        "/guilds/necromancer/forms/wall.c");
    addChild("/guilds/necromancer/forms/storm.c",
        "/guilds/necromancer/forms/vortex.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/forms/shroud.c");
    addResearchElement("/guilds/necromancer/forms/abyss.c");

    addChild("/guilds/necromancer/forms/shroud.c",
        "/guilds/necromancer/forms/mist.c");
    addChild("/guilds/necromancer/forms/abyss.c",
        "/guilds/necromancer/forms/storm.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Necrotic Spell Forms");
    Description("This research tree provides knowledge of the shapes and "
        "forms for constructing necromancer spells.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/forms/root.c");
    TreeRoot("/guilds/necromancer/forms/root.c");

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
