//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/smash.c");
    addChild("/guilds/guardian-of-khazurath/hammer/smash.c",
        "/guilds/guardian-of-khazurath/hammer/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/force-discipline.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammer-warding.c");

    addChild("/guilds/guardian-of-khazurath/hammer/force-discipline.c",
        "/guilds/guardian-of-khazurath/hammer/root.c");
    addChild("/guilds/guardian-of-khazurath/hammer/hammer-warding.c",
        "/guilds/guardian-of-khazurath/hammer/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/crushing-blow.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammer-focus.c");

    addChild("/guilds/guardian-of-khazurath/hammer/crushing-blow.c",
        "/guilds/guardian-of-khazurath/hammer/smash.c");
    addChild("/guilds/guardian-of-khazurath/hammer/hammer-focus.c",
        "/guilds/guardian-of-khazurath/hammer/force-discipline.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/crushing-tempo.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/blow-guard.c");

    addChild("/guilds/guardian-of-khazurath/hammer/crushing-tempo.c",
        "/guilds/guardian-of-khazurath/hammer/smash.c");
    addChild("/guilds/guardian-of-khazurath/hammer/blow-guard.c",
        "/guilds/guardian-of-khazurath/hammer/hammer-warding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/concussive-knowledge.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/pain-for-pain.c");

    addChild("/guilds/guardian-of-khazurath/hammer/concussive-knowledge.c",
        "/guilds/guardian-of-khazurath/hammer/smash.c");
    addChild("/guilds/guardian-of-khazurath/hammer/pain-for-pain.c",
        "/guilds/guardian-of-khazurath/hammer/smash.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/thunderstrike.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammer-affinity.c");

    addChild("/guilds/guardian-of-khazurath/hammer/thunderstrike.c",
        "/guilds/guardian-of-khazurath/hammer/crushing-blow.c");
    addChild("/guilds/guardian-of-khazurath/hammer/hammer-affinity.c",
        "/guilds/guardian-of-khazurath/hammer/hammer-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/bone-deep.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammers-cadence.c");

    addChild("/guilds/guardian-of-khazurath/hammer/bone-deep.c",
        "/guilds/guardian-of-khazurath/hammer/concussive-knowledge.c");
    addChild("/guilds/guardian-of-khazurath/hammer/hammers-cadence.c",
        "/guilds/guardian-of-khazurath/hammer/crushing-tempo.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/punishment-returned.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/iron-will.c");

    addChild("/guilds/guardian-of-khazurath/hammer/punishment-returned.c",
        "/guilds/guardian-of-khazurath/hammer/thunderstrike.c");
    addChild("/guilds/guardian-of-khazurath/hammer/iron-will.c",
        "/guilds/guardian-of-khazurath/hammer/pain-for-pain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/brutal-force.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/retribution-efficiency.c");

    addChild("/guilds/guardian-of-khazurath/hammer/brutal-force.c",
        "/guilds/guardian-of-khazurath/hammer/hammer-affinity.c");
    addChild("/guilds/guardian-of-khazurath/hammer/retribution-efficiency.c",
        "/guilds/guardian-of-khazurath/hammer/punishment-returned.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/unyielding-guard.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/dwarven-cadence.c");

    addChild("/guilds/guardian-of-khazurath/hammer/unyielding-guard.c",
        "/guilds/guardian-of-khazurath/hammer/blow-guard.c");
    addChild("/guilds/guardian-of-khazurath/hammer/dwarven-cadence.c",
        "/guilds/guardian-of-khazurath/hammer/hammers-cadence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/shattering-arc.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/shatter-deep.c");

    addChild("/guilds/guardian-of-khazurath/hammer/shattering-arc.c",
        "/guilds/guardian-of-khazurath/hammer/thunderstrike.c");
    addChild("/guilds/guardian-of-khazurath/hammer/shatter-deep.c",
        "/guilds/guardian-of-khazurath/hammer/bone-deep.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/dwarven-hammer-lore.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/retribution-power.c");

    addChild("/guilds/guardian-of-khazurath/hammer/dwarven-hammer-lore.c",
        "/guilds/guardian-of-khazurath/hammer/hammer-affinity.c");
    addChild("/guilds/guardian-of-khazurath/hammer/retribution-power.c",
        "/guilds/guardian-of-khazurath/hammer/retribution-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/battle-worn.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammer-haste.c");

    addChild("/guilds/guardian-of-khazurath/hammer/battle-worn.c",
        "/guilds/guardian-of-khazurath/hammer/iron-will.c");
    addChild("/guilds/guardian-of-khazurath/hammer/hammer-haste.c",
        "/guilds/guardian-of-khazurath/hammer/dwarven-cadence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/ironwall-stance.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/wrath-strike.c");

    addChild("/guilds/guardian-of-khazurath/hammer/ironwall-stance.c",
        "/guilds/guardian-of-khazurath/hammer/unyielding-guard.c");
    addChild("/guilds/guardian-of-khazurath/hammer/wrath-strike.c",
        "/guilds/guardian-of-khazurath/hammer/retribution-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammers-mastery.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/khazurath-impact.c");

    addChild("/guilds/guardian-of-khazurath/hammer/hammers-mastery.c",
        "/guilds/guardian-of-khazurath/hammer/brutal-force.c");
    addChild("/guilds/guardian-of-khazurath/hammer/khazurath-impact.c",
        "/guilds/guardian-of-khazurath/hammer/shatter-deep.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammer-flow.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/stone-wall-passive.c");

    addChild("/guilds/guardian-of-khazurath/hammer/hammer-flow.c",
        "/guilds/guardian-of-khazurath/hammer/hammer-haste.c");
    addChild("/guilds/guardian-of-khazurath/hammer/stone-wall-passive.c",
        "/guilds/guardian-of-khazurath/hammer/ironwall-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/mountain-hammer.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammer-economy.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/endurance-mastery.c");

    addChild("/guilds/guardian-of-khazurath/hammer/mountain-hammer.c",
        "/guilds/guardian-of-khazurath/hammer/shattering-arc.c");
    addChild("/guilds/guardian-of-khazurath/hammer/hammer-economy.c",
        "/guilds/guardian-of-khazurath/hammer/battle-worn.c");
    addChild("/guilds/guardian-of-khazurath/hammer/endurance-mastery.c",
        "/guilds/guardian-of-khazurath/hammer/battle-worn.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/hammer/endless-wrath.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/hammer-mastery-bonus.c");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/retribution-fury.c");

    addChild("/guilds/guardian-of-khazurath/hammer/endless-wrath.c",
        "/guilds/guardian-of-khazurath/hammer/khazurath-impact.c");
    addChild("/guilds/guardian-of-khazurath/hammer/hammer-mastery-bonus.c",
        "/guilds/guardian-of-khazurath/hammer/hammers-mastery.c");
    addChild("/guilds/guardian-of-khazurath/hammer/retribution-fury.c",
        "/guilds/guardian-of-khazurath/hammer/wrath-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Hammer Mastery");
    Description("The hammer-craft of Khazurath, forged in the deep mines of "
        "Mirost. This tree teaches the art of crushing blows, punishment "
        "returned, and the principle that pain itself becomes a weapon.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/hammer/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/hammer/root.c");

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
    TwentySixthLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortiethLevel();
}
