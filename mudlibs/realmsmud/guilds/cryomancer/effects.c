//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/cryomancer/effects/damage-hp.c");
    addResearchElement("/guilds/cryomancer/effects/add-hp.c");

    addChild("/guilds/cryomancer/effects/damage-hp.c",
        "/guilds/cryomancer/effects/root.c");
    addChild("/guilds/cryomancer/effects/add-hp.c",
        "/guilds/cryomancer/effects/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/cryomancer/effects/damage-sp.c");
    addResearchElement("/guilds/cryomancer/effects/add-sp.c");

    addChild("/guilds/cryomancer/effects/damage-sp.c",
        "/guilds/cryomancer/effects/damage-hp.c");
    addChild("/guilds/cryomancer/effects/add-sp.c",
        "/guilds/cryomancer/effects/add-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/cryomancer/effects/damage-stamina.c");
    addResearchElement("/guilds/cryomancer/effects/add-stamina.c");

    addChild("/guilds/cryomancer/effects/damage-stamina.c",
        "/guilds/cryomancer/effects/damage-sp.c");
    addChild("/guilds/cryomancer/effects/add-stamina.c",
        "/guilds/cryomancer/effects/add-sp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/cryomancer/effects/siphon-hp.c");
    addResearchElement("/guilds/cryomancer/effects/enhance-attack.c");
    addResearchElement("/guilds/cryomancer/effects/reduce-attack.c");

    addChild("/guilds/cryomancer/effects/siphon-hp.c",
        "/guilds/cryomancer/effects/damage-hp.c");
    addChild("/guilds/cryomancer/effects/enhance-attack.c",
        "/guilds/cryomancer/effects/add-hp.c");
    addChild("/guilds/cryomancer/effects/reduce-attack.c",
        "/guilds/cryomancer/effects/damage-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/cryomancer/effects/siphon-sp.c");
    addResearchElement("/guilds/cryomancer/effects/enhance-damage.c");
    addResearchElement("/guilds/cryomancer/effects/reduce-damage.c");

    addChild("/guilds/cryomancer/effects/siphon-sp.c",
        "/guilds/cryomancer/effects/siphon-hp.c");
    addChild("/guilds/cryomancer/effects/enhance-damage.c",
        "/guilds/cryomancer/effects/enhance-attack.c");
    addChild("/guilds/cryomancer/effects/reduce-damage.c",
        "/guilds/cryomancer/effects/reduce-attack.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/cryomancer/effects/siphon-stamina.c");
    addResearchElement("/guilds/cryomancer/effects/enhance-defense.c");
    addResearchElement("/guilds/cryomancer/effects/reduce-defense.c");

    addChild("/guilds/cryomancer/effects/siphon-stamina.c",
        "/guilds/cryomancer/effects/siphon-sp.c");
    addChild("/guilds/cryomancer/effects/enhance-defense.c",
        "/guilds/cryomancer/effects/enhance-damage.c");
    addChild("/guilds/cryomancer/effects/reduce-defense.c",
        "/guilds/cryomancer/effects/reduce-damage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/cryomancer/effects/enhance-soak.c");
    addResearchElement("/guilds/cryomancer/effects/reduce-soak.c");

    addChild("/guilds/cryomancer/effects/enhance-soak.c",
        "/guilds/cryomancer/effects/enhance-defense.c");
    addChild("/guilds/cryomancer/effects/reduce-soak.c",
        "/guilds/cryomancer/effects/reduce-defense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/cryomancer/effects/enhance-dodge.c");
    addResearchElement("/guilds/cryomancer/effects/reduce-dodge.c");
    addResearchElement("/guilds/cryomancer/effects/apply-slow.c");

    addChild("/guilds/cryomancer/effects/enhance-dodge.c",
        "/guilds/cryomancer/effects/enhance-soak.c");
    addChild("/guilds/cryomancer/effects/reduce-dodge.c",
        "/guilds/cryomancer/effects/reduce-soak.c");
    addChild("/guilds/cryomancer/effects/apply-slow.c",
        "/guilds/cryomancer/effects/reduce-soak.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/cryomancer/effects/enhance-parry.c");
    addResearchElement("/guilds/cryomancer/effects/reduce-parry.c");
    addResearchElement("/guilds/cryomancer/effects/apply-frozen.c");

    addChild("/guilds/cryomancer/effects/enhance-parry.c",
        "/guilds/cryomancer/effects/enhance-dodge.c");
    addChild("/guilds/cryomancer/effects/reduce-parry.c",
        "/guilds/cryomancer/effects/reduce-dodge.c");
    addChild("/guilds/cryomancer/effects/apply-frozen.c",
        "/guilds/cryomancer/effects/apply-slow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/cryomancer/effects/staggering-damage.c");
    addResearchElement("/guilds/cryomancer/effects/debilitating-damage.c");
    addResearchElement("/guilds/cryomancer/effects/disorienting-damage.c");
    addResearchElement("/guilds/cryomancer/effects/invigorating-siphon.c");

    addChild("/guilds/cryomancer/effects/staggering-damage.c",
        "/guilds/cryomancer/effects/reduce-parry.c");
    addChild("/guilds/cryomancer/effects/debilitating-damage.c",
        "/guilds/cryomancer/effects/reduce-parry.c");
    addChild("/guilds/cryomancer/effects/disorienting-damage.c",
        "/guilds/cryomancer/effects/reduce-parry.c");
    addChild("/guilds/cryomancer/effects/invigorating-siphon.c",
        "/guilds/cryomancer/effects/siphon-stamina.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/cryomancer/effects/hampering-damage.c");
    addResearchElement("/guilds/cryomancer/effects/exhausting-damage.c");
    addResearchElement("/guilds/cryomancer/effects/fortifying-siphon.c");

    addChild("/guilds/cryomancer/effects/hampering-damage.c",
        "/guilds/cryomancer/effects/apply-slow.c");
    addChild("/guilds/cryomancer/effects/exhausting-damage.c",
        "/guilds/cryomancer/effects/apply-frozen.c");
    addChild("/guilds/cryomancer/effects/fortifying-siphon.c",
        "/guilds/cryomancer/effects/invigorating-siphon.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Spell Effects");
    Description("This research tree provides knowledge of the various effects "
        "that constructed cryomancer spells can produce.");
    Source("cryomancer");
    addResearchElement("/guilds/cryomancer/effects/root.c");
    TreeRoot("/guilds/cryomancer/effects/root.c");

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
