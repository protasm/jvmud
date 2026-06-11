//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/cryomancer/construct/construct-spell.c");

    addChild("/guilds/cryomancer/construct/construct-spell.c",
        "/guilds/cryomancer/construct/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/efficient-casting.c");

    addChild("/guilds/cryomancer/construct/efficient-casting.c",
        "/guilds/cryomancer/construct/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/spell-focus.c");

    addChild("/guilds/cryomancer/construct/spell-focus.c",
        "/guilds/cryomancer/construct/efficient-casting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/improved-efficiency.c");

    addChild("/guilds/cryomancer/construct/improved-efficiency.c",
        "/guilds/cryomancer/construct/efficient-casting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/enhanced-focus.c");

    addChild("/guilds/cryomancer/construct/enhanced-focus.c",
        "/guilds/cryomancer/construct/spell-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/construct/expert-efficiency.c");

    addChild("/guilds/cryomancer/construct/expert-efficiency.c",
        "/guilds/cryomancer/construct/improved-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/spell-amplification.c");

    addChild("/guilds/cryomancer/construct/spell-amplification.c",
        "/guilds/cryomancer/construct/enhanced-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/master-efficiency.c");

    addChild("/guilds/cryomancer/construct/master-efficiency.c",
        "/guilds/cryomancer/construct/expert-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/cryomancer/construct/spell-mastery.c");

    addChild("/guilds/cryomancer/construct/spell-mastery.c",
        "/guilds/cryomancer/construct/spell-amplification.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/cryomancer/construct/grandmaster-efficiency.c");

    addChild("/guilds/cryomancer/construct/grandmaster-efficiency.c",
        "/guilds/cryomancer/construct/master-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/construct/arcane-precision.c");

    addChild("/guilds/cryomancer/construct/arcane-precision.c",
        "/guilds/cryomancer/construct/spell-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/legendary-efficiency.c");

    addChild("/guilds/cryomancer/construct/legendary-efficiency.c",
        "/guilds/cryomancer/construct/grandmaster-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/cryomancer/construct/spell-perfection.c");

    addChild("/guilds/cryomancer/construct/spell-perfection.c",
        "/guilds/cryomancer/construct/arcane-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Spell Construction");
    Description("This research tree provides knowledge of constructing custom "
        "cryomancer spells and enhancing their effectiveness.");
    Source("cryomancer");
    addResearchElement("/guilds/cryomancer/construct/root.c");
    TreeRoot("/guilds/cryomancer/construct/root.c");

    FirstLevel();
    FifthLevel();
    NinthLevel();
    ThirteenthLevel();
    SeventeenthLevel();
    TwentyFirstLevel();
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyFifthLevel();
    FortyNinthLevel();
}
