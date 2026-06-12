//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/raw-power.c");
    addChild("/guilds/guardian-of-khazurath/strength/raw-power.c",
        "/guilds/guardian-of-khazurath/strength/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/crushing-blow.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/iron-grip.c");

    addChild("/guilds/guardian-of-khazurath/strength/crushing-blow.c",
        "/guilds/guardian-of-khazurath/strength/root.c");
    addChild("/guilds/guardian-of-khazurath/strength/iron-grip.c",
        "/guilds/guardian-of-khazurath/strength/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/dwarven-might.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/stone-fist.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/dwarven-power.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/crushing-grip.c");

    addChild("/guilds/guardian-of-khazurath/strength/dwarven-might.c",
        "/guilds/guardian-of-khazurath/strength/raw-power.c");
    addChild("/guilds/guardian-of-khazurath/strength/stone-fist.c",
        "/guilds/guardian-of-khazurath/strength/iron-grip.c");
    addChild("/guilds/guardian-of-khazurath/strength/dwarven-power.c",
        "/guilds/guardian-of-khazurath/strength/dwarven-might.c");
    addChild("/guilds/guardian-of-khazurath/strength/crushing-grip.c",
        "/guilds/guardian-of-khazurath/strength/stone-fist.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/muscle-mastery.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/bone-crusher.c");

    addChild("/guilds/guardian-of-khazurath/strength/muscle-mastery.c",
        "/guilds/guardian-of-khazurath/strength/dwarven-might.c");
    addChild("/guilds/guardian-of-khazurath/strength/bone-crusher.c",
        "/guilds/guardian-of-khazurath/strength/stone-fist.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/mountain-grip.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/forge-strength.c");

    addChild("/guilds/guardian-of-khazurath/strength/mountain-grip.c",
        "/guilds/guardian-of-khazurath/strength/crushing-blow.c");
    addChild("/guilds/guardian-of-khazurath/strength/forge-strength.c",
        "/guilds/guardian-of-khazurath/strength/muscle-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/titan-strike.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/power-surge.c");

    addChild("/guilds/guardian-of-khazurath/strength/titan-strike.c",
        "/guilds/guardian-of-khazurath/strength/crushing-blow.c");
    addChild("/guilds/guardian-of-khazurath/strength/power-surge.c",
        "/guilds/guardian-of-khazurath/strength/forge-strength.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/mountain-strength.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/shattering-force.c");

    addChild("/guilds/guardian-of-khazurath/strength/mountain-strength.c",
        "/guilds/guardian-of-khazurath/strength/mountain-grip.c");
    addChild("/guilds/guardian-of-khazurath/strength/shattering-force.c",
        "/guilds/guardian-of-khazurath/strength/bone-crusher.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/titan-muscles.c");

    addChild("/guilds/guardian-of-khazurath/strength/titan-muscles.c",
        "/guilds/guardian-of-khazurath/strength/power-surge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/colossus-strength.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/crushing-force.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/titanic-force.c");

    addChild("/guilds/guardian-of-khazurath/strength/colossus-strength.c",
        "/guilds/guardian-of-khazurath/strength/mountain-strength.c");
    addChild("/guilds/guardian-of-khazurath/strength/crushing-force.c",
        "/guilds/guardian-of-khazurath/strength/shattering-force.c");
    addChild("/guilds/guardian-of-khazurath/strength/titanic-force.c",
        "/guilds/guardian-of-khazurath/strength/colossus-strength.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/earthshaker.c");

    addChild("/guilds/guardian-of-khazurath/strength/earthshaker.c",
        "/guilds/guardian-of-khazurath/strength/titan-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/geological-force.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/mountain-muscles.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/mountain-fury.c");

    addChild("/guilds/guardian-of-khazurath/strength/geological-force.c",
        "/guilds/guardian-of-khazurath/strength/titan-muscles.c");
    addChild("/guilds/guardian-of-khazurath/strength/mountain-muscles.c",
        "/guilds/guardian-of-khazurath/strength/colossus-strength.c");
    addChild("/guilds/guardian-of-khazurath/strength/mountain-fury.c",
        "/guilds/guardian-of-khazurath/strength/mountain-muscles.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/force-of-nature.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/deep-power.c");

    addChild("/guilds/guardian-of-khazurath/strength/force-of-nature.c",
        "/guilds/guardian-of-khazurath/strength/crushing-force.c");
    addChild("/guilds/guardian-of-khazurath/strength/deep-power.c",
        "/guilds/guardian-of-khazurath/strength/force-of-nature.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/avalanche-assault.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/tectonic-might.c");

    addChild("/guilds/guardian-of-khazurath/strength/avalanche-assault.c",
        "/guilds/guardian-of-khazurath/strength/earthshaker.c");
    addChild("/guilds/guardian-of-khazurath/strength/tectonic-might.c",
        "/guilds/guardian-of-khazurath/strength/geological-force.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/khazurath-might.c");

    addChild("/guilds/guardian-of-khazurath/strength/khazurath-might.c",
        "/guilds/guardian-of-khazurath/strength/mountain-muscles.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/primal-force.c");

    addChild("/guilds/guardian-of-khazurath/strength/primal-force.c",
        "/guilds/guardian-of-khazurath/strength/force-of-nature.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/mountain-rage.c");

    addChild("/guilds/guardian-of-khazurath/strength/mountain-rage.c",
        "/guilds/guardian-of-khazurath/strength/avalanche-assault.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/living-avalanche.c");

    addChild("/guilds/guardian-of-khazurath/strength/living-avalanche.c",
        "/guilds/guardian-of-khazurath/strength/tectonic-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/mountain-incarnate.c");

    addChild("/guilds/guardian-of-khazurath/strength/mountain-incarnate.c",
        "/guilds/guardian-of-khazurath/strength/khazurath-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/seismic-strength.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/unstoppable-force.c");

    addChild("/guilds/guardian-of-khazurath/strength/seismic-strength.c",
        "/guilds/guardian-of-khazurath/strength/living-avalanche.c");
    addChild("/guilds/guardian-of-khazurath/strength/unstoppable-force.c",
        "/guilds/guardian-of-khazurath/strength/mountain-incarnate.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/geological-might.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/titan-blow.c");

    addChild("/guilds/guardian-of-khazurath/strength/geological-might.c",
        "/guilds/guardian-of-khazurath/strength/seismic-strength.c");
    addChild("/guilds/guardian-of-khazurath/strength/titan-blow.c",
        "/guilds/guardian-of-khazurath/strength/unstoppable-force.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/world-breaker.c");
    addResearchElement("/guilds/guardian-of-khazurath/strength/primordial-strength.c");

    addChild("/guilds/guardian-of-khazurath/strength/world-breaker.c",
        "/guilds/guardian-of-khazurath/strength/geological-might.c");
    addChild("/guilds/guardian-of-khazurath/strength/primordial-strength.c",
        "/guilds/guardian-of-khazurath/strength/titan-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/ancient-mountain-fury.c");

    addChild("/guilds/guardian-of-khazurath/strength/ancient-mountain-fury.c",
        "/guilds/guardian-of-khazurath/strength/primal-force.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/strength/strength-of-khazurath.c");

    addChild("/guilds/guardian-of-khazurath/strength/strength-of-khazurath.c",
        "/guilds/guardian-of-khazurath/strength/world-breaker.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Mountain Strength");
    Description("The strength of the mountain given form. Guardians who walk "
        "this path develop the raw physical power to shatter stone barehanded, "
        "building devastating attack and damage from the bones of the earth.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/strength/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/strength/root.c");

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
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyFifthLevel();
    FortyNinthLevel();
    FiftyThirdLevel();
    FiftySeventhLevel();
    SixtySecondLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
