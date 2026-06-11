//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/cryomancer/forms/arc.c");
    addResearchElement("/guilds/cryomancer/forms/droplet.c");
    addResearchElement("/guilds/cryomancer/forms/touch.c");

    addChild("/guilds/cryomancer/forms/arc.c",
        "/guilds/cryomancer/forms/root.c");
    addChild("/guilds/cryomancer/forms/droplet.c",
        "/guilds/cryomancer/forms/root.c");
    addChild("/guilds/cryomancer/forms/touch.c",
        "/guilds/cryomancer/forms/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/cryomancer/forms/bolt.c");
    addResearchElement("/guilds/cryomancer/forms/shard.c");
    addResearchElement("/guilds/cryomancer/forms/spear.c");

    addChild("/guilds/cryomancer/forms/bolt.c",
        "/guilds/cryomancer/forms/arc.c");
    addChild("/guilds/cryomancer/forms/shard.c",
        "/guilds/cryomancer/forms/droplet.c");
    addChild("/guilds/cryomancer/forms/spear.c",
        "/guilds/cryomancer/forms/touch.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/cryomancer/forms/spray.c");
    addResearchElement("/guilds/cryomancer/forms/needle.c");

    addChild("/guilds/cryomancer/forms/spray.c",
        "/guilds/cryomancer/forms/shard.c");
    addChild("/guilds/cryomancer/forms/needle.c",
        "/guilds/cryomancer/forms/spear.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/cryomancer/forms/blast.c");
    addResearchElement("/guilds/cryomancer/forms/mist.c");
    addResearchElement("/guilds/cryomancer/forms/razor.c");

    addChild("/guilds/cryomancer/forms/blast.c",
        "/guilds/cryomancer/forms/bolt.c");
    addChild("/guilds/cryomancer/forms/mist.c",
        "/guilds/cryomancer/forms/spray.c");
    addChild("/guilds/cryomancer/forms/razor.c",
        "/guilds/cryomancer/forms/needle.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/cryomancer/forms/wave.c");
    addResearchElement("/guilds/cryomancer/forms/tendril.c");

    addChild("/guilds/cryomancer/forms/wave.c",
        "/guilds/cryomancer/forms/mist.c");
    addChild("/guilds/cryomancer/forms/tendril.c",
        "/guilds/cryomancer/forms/razor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/cryomancer/forms/orb.c");
    addResearchElement("/guilds/cryomancer/forms/ring.c");

    addChild("/guilds/cryomancer/forms/orb.c",
        "/guilds/cryomancer/forms/blast.c");
    addChild("/guilds/cryomancer/forms/ring.c",
        "/guilds/cryomancer/forms/wave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/cryomancer/forms/wall.c");
    addResearchElement("/guilds/cryomancer/forms/cage.c");
    addResearchElement("/guilds/cryomancer/forms/lance.c");

    addChild("/guilds/cryomancer/forms/wall.c",
        "/guilds/cryomancer/forms/wave.c");
    addChild("/guilds/cryomancer/forms/cage.c",
        "/guilds/cryomancer/forms/ring.c");
    addChild("/guilds/cryomancer/forms/lance.c",
        "/guilds/cryomancer/forms/spear.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/cryomancer/forms/glacier.c");
    addResearchElement("/guilds/cryomancer/forms/strike.c");

    addChild("/guilds/cryomancer/forms/glacier.c",
        "/guilds/cryomancer/forms/orb.c");
    addChild("/guilds/cryomancer/forms/strike.c",
        "/guilds/cryomancer/forms/lance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/cryomancer/forms/coil.c");
    addResearchElement("/guilds/cryomancer/forms/vortex.c");

    addChild("/guilds/cryomancer/forms/coil.c",
        "/guilds/cryomancer/forms/cage.c");
    addChild("/guilds/cryomancer/forms/vortex.c",
        "/guilds/cryomancer/forms/glacier.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/cryomancer/forms/pulse.c");
    addResearchElement("/guilds/cryomancer/forms/avalanche.c");

    addChild("/guilds/cryomancer/forms/pulse.c",
        "/guilds/cryomancer/forms/coil.c");
    addChild("/guilds/cryomancer/forms/avalanche.c",
        "/guilds/cryomancer/forms/vortex.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/forms/blizzard.c");

    addChild("/guilds/cryomancer/forms/blizzard.c",
        "/guilds/cryomancer/forms/strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/forms/icestorm.c");

    addChild("/guilds/cryomancer/forms/icestorm.c",
        "/guilds/cryomancer/forms/avalanche.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Spell Forms");
    Description("This research tree provides knowledge of the various forms "
        "that constructed cryomancer spells can take.");
    Source("cryomancer");
    addResearchElement("/guilds/cryomancer/forms/root.c");
    TreeRoot("/guilds/cryomancer/forms/root.c");

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
}
