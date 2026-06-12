//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/cryomancer/frost/cold-snap.c");

    addChild("/guilds/cryomancer/frost/cold-snap.c",
        "/guilds/cryomancer/frost/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/cryomancer/frost/ice-lance.c");

    addChild("/guilds/cryomancer/frost/ice-lance.c",
        "/guilds/cryomancer/frost/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/winter-grasp.c");

    addChild("/guilds/cryomancer/frost/winter-grasp.c",
        "/guilds/cryomancer/frost/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/cryomancer/frost/rime-burst.c");

    addChild("/guilds/cryomancer/frost/rime-burst.c",
        "/guilds/cryomancer/frost/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/hoarfrost.c");
    addResearchElement("/guilds/cryomancer/frost/frost-blades.c");
    addResearchElement("/guilds/cryomancer/frost/frost-bolt.c");

    addChild("/guilds/cryomancer/frost/hoarfrost.c",
        "/guilds/cryomancer/frost/cold-snap.c");
    addChild("/guilds/cryomancer/frost/frost-blades.c",
        "/guilds/cryomancer/frost/root.c");
    addChild("/guilds/cryomancer/frost/frost-bolt.c",
        "/guilds/cryomancer/frost/cold-snap.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/cryomancer/frost/wintermasters-reserve.c");

    addChild("/guilds/cryomancer/frost/wintermasters-reserve.c",
        "/guilds/cryomancer/frost/frost-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/wintermasters-boon.c");
    addResearchElement("/guilds/cryomancer/frost/ice-whip.c");

    addChild("/guilds/cryomancer/frost/wintermasters-boon.c",
        "/guilds/cryomancer/frost/frost-bolt.c");
    addChild("/guilds/cryomancer/frost/ice-whip.c",
        "/guilds/cryomancer/frost/ice-lance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/cryostasis.c");

    addChild("/guilds/cryomancer/frost/cryostasis.c",
        "/guilds/cryomancer/frost/hoarfrost.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/freezing-gale.c");

    addChild("/guilds/cryomancer/frost/freezing-gale.c",
        "/guilds/cryomancer/frost/rime-burst.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/frost-discharge.c");

    addChild("/guilds/cryomancer/frost/frost-discharge.c",
        "/guilds/cryomancer/frost/frost-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/frost/crystalline-focus.c");
    addResearchElement("/guilds/cryomancer/frost/wintermasters-call.c");

    addChild("/guilds/cryomancer/frost/crystalline-focus.c",
        "/guilds/cryomancer/frost/cryostasis.c");
    addChild("/guilds/cryomancer/frost/wintermasters-call.c",
        "/guilds/cryomancer/frost/wintermasters-reserve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/frost/wintermasters-finesse.c");

    addChild("/guilds/cryomancer/frost/wintermasters-finesse.c",
        "/guilds/cryomancer/frost/wintermasters-boon.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/frost/enhanced-rime.c");

    addChild("/guilds/cryomancer/frost/enhanced-rime.c",
        "/guilds/cryomancer/frost/crystalline-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/blizzard.c");

    addChild("/guilds/cryomancer/frost/blizzard.c",
        "/guilds/cryomancer/frost/frost-discharge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/frost/wintermasters-might.c");

    addChild("/guilds/cryomancer/frost/wintermasters-might.c",
        "/guilds/cryomancer/frost/wintermasters-call.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/frost/glacial-induction.c");
    addResearchElement("/guilds/cryomancer/frost/wintermasters-endurance.c");

    addChild("/guilds/cryomancer/frost/glacial-induction.c",
        "/guilds/cryomancer/frost/enhanced-rime.c");
    addChild("/guilds/cryomancer/frost/wintermasters-endurance.c",
        "/guilds/cryomancer/frost/wintermasters-finesse.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/frost/arctic-tempest.c");

    addChild("/guilds/cryomancer/frost/arctic-tempest.c",
        "/guilds/cryomancer/frost/blizzard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/deep-freeze.c");

    addChild("/guilds/cryomancer/frost/deep-freeze.c",
        "/guilds/cryomancer/frost/glacial-induction.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/frost/wintermasters-fury.c");

    addChild("/guilds/cryomancer/frost/wintermasters-fury.c",
        "/guilds/cryomancer/frost/wintermasters-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/frost/wintermasters-strength.c");

    addChild("/guilds/cryomancer/frost/wintermasters-strength.c",
        "/guilds/cryomancer/frost/wintermasters-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/cryomancer/frost/glacial-condenser.c");

    addChild("/guilds/cryomancer/frost/glacial-condenser.c",
        "/guilds/cryomancer/frost/deep-freeze.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/frost/absolute-zero.c");

    addChild("/guilds/cryomancer/frost/absolute-zero.c",
        "/guilds/cryomancer/frost/glacial-condenser.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/frost/permafrost-eruption.c");

    addChild("/guilds/cryomancer/frost/permafrost-eruption.c",
        "/guilds/cryomancer/frost/absolute-zero.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/frost/polar-vortex.c");

    addChild("/guilds/cryomancer/frost/polar-vortex.c",
        "/guilds/cryomancer/frost/permafrost-eruption.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/frost/frozen-apocalypse.c");

    addChild("/guilds/cryomancer/frost/frozen-apocalypse.c",
        "/guilds/cryomancer/frost/arctic-tempest.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Path of Frost");
    Description("This research tree provides knowledge of frost-based "
        "spells and abilities for the cryomancer.");
    Source("cryomancer");
    addResearchElement("/guilds/cryomancer/frost/root.c");
    TreeRoot("/guilds/cryomancer/frost/root.c");

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
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
}
