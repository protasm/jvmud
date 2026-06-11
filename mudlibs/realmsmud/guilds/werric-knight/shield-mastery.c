//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/shield/shield-training.c");
    addChild("/guilds/werric-knight/shield/shield-training.c",
        "/guilds/werric-knight/shield/root.c");

    addResearchElement("/guilds/werric-knight/shield/defensive-stance.c");
    addChild("/guilds/werric-knight/shield/defensive-stance.c",
        "/guilds/werric-knight/shield/shield-training.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/shield/shield-bash.c");
    addChild("/guilds/werric-knight/shield/shield-bash.c",
        "/guilds/werric-knight/shield/defensive-stance.c");

    addResearchElement("/guilds/werric-knight/shield/bulwark.c");
    addChild("/guilds/werric-knight/shield/bulwark.c",
        "/guilds/werric-knight/shield/defensive-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/counter-block.c");
    addChild("/guilds/werric-knight/shield/counter-block.c",
        "/guilds/werric-knight/shield/bulwark.c");

    addResearchElement("/guilds/werric-knight/shield/protect-ally.c");
    addChild("/guilds/werric-knight/shield/protect-ally.c",
        "/guilds/werric-knight/shield/counter-block.c");

    addResearchElement("/guilds/werric-knight/shield/shield-impact.c");
    addChild("/guilds/werric-knight/shield/shield-impact.c",
        "/guilds/werric-knight/shield/shield-bash.c");

    addResearchElement("/guilds/werric-knight/shield/defensive-mastery.c");
    addChild("/guilds/werric-knight/shield/defensive-mastery.c",
        "/guilds/werric-knight/shield/bulwark.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/shield/shield-wall.c");
    addChild("/guilds/werric-knight/shield/shield-wall.c",
        "/guilds/werric-knight/shield/protect-ally.c");

    addResearchElement("/guilds/werric-knight/shield/fortified-defense.c");
    addChild("/guilds/werric-knight/shield/fortified-defense.c",
        "/guilds/werric-knight/shield/protect-ally.c");

    addResearchElement("/guilds/werric-knight/shield/defensive-strike.c");
    addChild("/guilds/werric-knight/shield/defensive-strike.c",
        "/guilds/werric-knight/shield/counter-block.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/crushing-blow.c");
    addChild("/guilds/werric-knight/shield/crushing-blow.c",
        "/guilds/werric-knight/shield/fortified-defense.c");

    addResearchElement("/guilds/werric-knight/shield/stalwart-defender.c");
    addChild("/guilds/werric-knight/shield/stalwart-defender.c",
        "/guilds/werric-knight/shield/crushing-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/shield/aegis-formation.c");
    addChild("/guilds/werric-knight/shield/aegis-formation.c",
        "/guilds/werric-knight/shield/stalwart-defender.c");

    addResearchElement("/guilds/werric-knight/shield/shield-charge.c");
    addChild("/guilds/werric-knight/shield/shield-charge.c",
        "/guilds/werric-knight/shield/aegis-formation.c");

    addResearchElement("/guilds/werric-knight/shield/crushing-force.c");
    addChild("/guilds/werric-knight/shield/crushing-force.c",
        "/guilds/werric-knight/shield/crushing-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/unyielding-defense.c");
    addChild("/guilds/werric-knight/shield/unyielding-defense.c",
        "/guilds/werric-knight/shield/shield-charge.c");

    addResearchElement("/guilds/werric-knight/shield/devastating-counter.c");
    addChild("/guilds/werric-knight/shield/devastating-counter.c",
        "/guilds/werric-knight/shield/unyielding-defense.c");

    addResearchElement("/guilds/werric-knight/shield/counter-mastery.c");
    addChild("/guilds/werric-knight/shield/counter-mastery.c",
        "/guilds/werric-knight/shield/devastating-counter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/bastion.c");
    addChild("/guilds/werric-knight/shield/bastion.c",
        "/guilds/werric-knight/shield/devastating-counter.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/shield-slam.c");
    addChild("/guilds/werric-knight/shield/shield-slam.c",
        "/guilds/werric-knight/shield/bastion.c");

    addResearchElement("/guilds/werric-knight/shield/bulwark-of-realm.c");
    addChild("/guilds/werric-knight/shield/bulwark-of-realm.c",
        "/guilds/werric-knight/shield/shield-slam.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/fortress-stance.c");
    addChild("/guilds/werric-knight/shield/fortress-stance.c",
        "/guilds/werric-knight/shield/bulwark-of-realm.c");

    addResearchElement("/guilds/werric-knight/shield/retribution-strike.c");
    addChild("/guilds/werric-knight/shield/retribution-strike.c",
        "/guilds/werric-knight/shield/fortress-stance.c");

    addResearchElement("/guilds/werric-knight/shield/thunderous-blow.c");
    addChild("/guilds/werric-knight/shield/thunderous-blow.c",
        "/guilds/werric-knight/shield/retribution-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/shield/guardians-resilience.c");
    addChild("/guilds/werric-knight/shield/guardians-resilience.c",
        "/guilds/werric-knight/shield/retribution-strike.c");

    addResearchElement("/guilds/werric-knight/shield/impenetrable-defense.c");
    addChild("/guilds/werric-knight/shield/impenetrable-defense.c",
        "/guilds/werric-knight/shield/guardians-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/master-guardian.c");
    addChild("/guilds/werric-knight/shield/master-guardian.c",
        "/guilds/werric-knight/shield/impenetrable-defense.c");

    addResearchElement("/guilds/werric-knight/shield/shield-of-werra.c");
    addChild("/guilds/werric-knight/shield/shield-of-werra.c",
        "/guilds/werric-knight/shield/master-guardian.c");

    addResearchElement("/guilds/werric-knight/shield/ultimate-defense.c");
    addChild("/guilds/werric-knight/shield/ultimate-defense.c",
        "/guilds/werric-knight/shield/shield-of-werra.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/werric-knight/shield/unbreakable-will.c");
    addChild("/guilds/werric-knight/shield/unbreakable-will.c",
        "/guilds/werric-knight/shield/shield-of-werra.c");

    addResearchElement("/guilds/werric-knight/shield/thunderous-impact.c");
    addChild("/guilds/werric-knight/shield/thunderous-impact.c",
        "/guilds/werric-knight/shield/unbreakable-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/shield/sentinels-vigil.c");
    addChild("/guilds/werric-knight/shield/sentinels-vigil.c",
        "/guilds/werric-knight/shield/thunderous-impact.c");

    addResearchElement("/guilds/werric-knight/shield/legendary-guardian.c");
    addChild("/guilds/werric-knight/shield/legendary-guardian.c",
        "/guilds/werric-knight/shield/sentinels-vigil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/shield/ultimate-sacrifice.c");
    addChild("/guilds/werric-knight/shield/ultimate-sacrifice.c",
        "/guilds/werric-knight/shield/legendary-guardian.c");

    addResearchElement("/guilds/werric-knight/shield/eternal-bulwark.c");
    addChild("/guilds/werric-knight/shield/eternal-bulwark.c",
        "/guilds/werric-knight/shield/ultimate-sacrifice.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Shield of Hillgarath");
    Description("The Shield of Hillgarath tree represents the defensive arts "
        "mastered by Werric Knights. These are the techniques that make you "
        "the bulwark of the realm, the protector of the innocent, the shield "
        "that never breaks.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/shield/root.c");
    TreeRoot("/guilds/werric-knight/shield/root.c");

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
}
