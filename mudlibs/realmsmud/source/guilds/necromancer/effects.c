//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/effects/damage-hp.c");
    addResearchElement("/guilds/necromancer/effects/siphon-hp.c");

    addChild("/guilds/necromancer/effects/damage-hp.c",
        "/guilds/necromancer/effects/root.c");
    addChild("/guilds/necromancer/effects/siphon-hp.c",
        "/guilds/necromancer/effects/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/effects/damage-sp.c");
    addResearchElement("/guilds/necromancer/effects/siphon-sp.c");

    addChild("/guilds/necromancer/effects/damage-sp.c",
        "/guilds/necromancer/effects/damage-hp.c");
    addChild("/guilds/necromancer/effects/siphon-sp.c",
        "/guilds/necromancer/effects/siphon-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/effects/damage-stamina.c");
    addResearchElement("/guilds/necromancer/effects/siphon-stamina.c");

    addChild("/guilds/necromancer/effects/damage-stamina.c",
        "/guilds/necromancer/effects/damage-sp.c");
    addChild("/guilds/necromancer/effects/siphon-stamina.c",
        "/guilds/necromancer/effects/siphon-sp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/effects/add-hp.c");
    addResearchElement("/guilds/necromancer/effects/reduce-defense.c");

    addChild("/guilds/necromancer/effects/add-hp.c",
        "/guilds/necromancer/effects/siphon-hp.c");
    addChild("/guilds/necromancer/effects/reduce-defense.c",
        "/guilds/necromancer/effects/damage-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/effects/add-sp.c");
    addResearchElement("/guilds/necromancer/effects/reduce-attack.c");
    addResearchElement("/guilds/necromancer/effects/enhance-attack.c");

    addChild("/guilds/necromancer/effects/add-sp.c",
        "/guilds/necromancer/effects/add-hp.c");
    addChild("/guilds/necromancer/effects/reduce-attack.c",
        "/guilds/necromancer/effects/reduce-defense.c");
    addChild("/guilds/necromancer/effects/enhance-attack.c",
        "/guilds/necromancer/effects/siphon-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/effects/add-stamina.c");
    addResearchElement("/guilds/necromancer/effects/reduce-damage.c");
    addResearchElement("/guilds/necromancer/effects/enhance-damage.c");

    addChild("/guilds/necromancer/effects/add-stamina.c",
        "/guilds/necromancer/effects/add-sp.c");
    addChild("/guilds/necromancer/effects/reduce-damage.c",
        "/guilds/necromancer/effects/reduce-attack.c");
    addChild("/guilds/necromancer/effects/enhance-damage.c",
        "/guilds/necromancer/effects/enhance-attack.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/effects/reduce-soak.c");
    addResearchElement("/guilds/necromancer/effects/enhance-defense.c");
    addResearchElement("/guilds/necromancer/effects/apply-disease.c");

    addChild("/guilds/necromancer/effects/reduce-soak.c",
        "/guilds/necromancer/effects/reduce-damage.c");
    addChild("/guilds/necromancer/effects/enhance-defense.c",
        "/guilds/necromancer/effects/enhance-damage.c");
    addChild("/guilds/necromancer/effects/apply-disease.c",
        "/guilds/necromancer/effects/damage-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/effects/reduce-dodge.c");
    addResearchElement("/guilds/necromancer/effects/enhance-soak.c");
    addResearchElement("/guilds/necromancer/effects/apply-poison.c");

    addChild("/guilds/necromancer/effects/reduce-dodge.c",
        "/guilds/necromancer/effects/reduce-soak.c");
    addChild("/guilds/necromancer/effects/enhance-soak.c",
        "/guilds/necromancer/effects/enhance-defense.c");
    addChild("/guilds/necromancer/effects/apply-poison.c",
        "/guilds/necromancer/effects/apply-disease.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/effects/reduce-parry.c");
    addResearchElement("/guilds/necromancer/effects/enhance-dodge.c");
    addResearchElement("/guilds/necromancer/effects/staggering-damage.c");

    addChild("/guilds/necromancer/effects/reduce-parry.c",
        "/guilds/necromancer/effects/reduce-dodge.c");
    addChild("/guilds/necromancer/effects/enhance-dodge.c",
        "/guilds/necromancer/effects/enhance-soak.c");
    addChild("/guilds/necromancer/effects/staggering-damage.c",
        "/guilds/necromancer/effects/damage-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/effects/enhance-parry.c");
    addResearchElement("/guilds/necromancer/effects/debilitating-damage.c");
    addResearchElement("/guilds/necromancer/effects/invigorating-siphon.c");

    addChild("/guilds/necromancer/effects/enhance-parry.c",
        "/guilds/necromancer/effects/enhance-dodge.c");
    addChild("/guilds/necromancer/effects/debilitating-damage.c",
        "/guilds/necromancer/effects/staggering-damage.c");
    addChild("/guilds/necromancer/effects/invigorating-siphon.c",
        "/guilds/necromancer/effects/siphon-hp.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/effects/disorienting-damage.c");
    addResearchElement("/guilds/necromancer/effects/hampering-damage.c");
    addResearchElement("/guilds/necromancer/effects/fortifying-siphon.c");
    addResearchElement("/guilds/necromancer/effects/exhausting-damage.c");

    addChild("/guilds/necromancer/effects/disorienting-damage.c",
        "/guilds/necromancer/effects/debilitating-damage.c");
    addChild("/guilds/necromancer/effects/hampering-damage.c",
        "/guilds/necromancer/effects/debilitating-damage.c");
    addChild("/guilds/necromancer/effects/fortifying-siphon.c",
        "/guilds/necromancer/effects/invigorating-siphon.c");
    addChild("/guilds/necromancer/effects/exhausting-damage.c",
        "/guilds/necromancer/effects/staggering-damage.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Necrotic Spell Effects");
    Description("This research tree provides knowledge of the effects "
        "that can be applied through constructed necromancer spells.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/effects/root.c");
    TreeRoot("/guilds/necromancer/effects/root.c");

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
