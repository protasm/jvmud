//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/defender/crowns-shield.c");
    addChild("/guilds/werric-knight/defender/crowns-shield.c",
        "/guilds/werric-knight/defender/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/defender/living-wall.c");
    addChild("/guilds/werric-knight/defender/living-wall.c",
        "/guilds/werric-knight/defender/crowns-shield.c");

    addResearchElement("/guilds/werric-knight/defender/intercept.c");
    addChild("/guilds/werric-knight/defender/intercept.c",
        "/guilds/werric-knight/defender/crowns-shield.c");

    addResearchElement("/guilds/werric-knight/defender/guardians-vigil.c");
    addChild("/guilds/werric-knight/defender/guardians-vigil.c",
        "/guilds/werric-knight/defender/intercept.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/werric-knight/defender/selfless-protector.c");
    addChild("/guilds/werric-knight/defender/selfless-protector.c",
        "/guilds/werric-knight/defender/guardians-vigil.c");

    addResearchElement("/guilds/werric-knight/defender/sacrifice.c");
    addChild("/guilds/werric-knight/defender/sacrifice.c",
        "/guilds/werric-knight/defender/selfless-protector.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/defender/shield-innocent.c");
    addChild("/guilds/werric-knight/defender/shield-innocent.c",
        "/guilds/werric-knight/defender/selfless-protector.c");

    addResearchElement("/guilds/werric-knight/defender/immovable-guardian.c");
    addChild("/guilds/werric-knight/defender/immovable-guardian.c",
        "/guilds/werric-knight/defender/sacrifice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/defender/indomitable-defense.c");
    addChild("/guilds/werric-knight/defender/indomitable-defense.c",
        "/guilds/werric-knight/defender/immovable-guardian.c");

    addResearchElement("/guilds/werric-knight/defender/aegis-of-realm.c");
    addChild("/guilds/werric-knight/defender/aegis-of-realm.c",
        "/guilds/werric-knight/defender/immovable-guardian.c");

    addResearchElement("/guilds/werric-knight/defender/defiant-stand.c");
    addChild("/guilds/werric-knight/defender/defiant-stand.c",
        "/guilds/werric-knight/defender/aegis-of-realm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/defender/unyielding-bastion.c");
    addChild("/guilds/werric-knight/defender/unyielding-bastion.c",
        "/guilds/werric-knight/defender/defiant-stand.c");

    addResearchElement("/guilds/werric-knight/defender/sanctified-defense.c");
    addChild("/guilds/werric-knight/defender/sanctified-defense.c",
        "/guilds/werric-knight/defender/unyielding-bastion.c");

    addResearchElement("/guilds/werric-knight/defender/kings-champion.c");
    addChild("/guilds/werric-knight/defender/kings-champion.c",
        "/guilds/werric-knight/defender/unyielding-bastion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/defender/redemptions-shield.c");
    addChild("/guilds/werric-knight/defender/redemptions-shield.c",
        "/guilds/werric-knight/defender/kings-champion.c");

    addResearchElement("/guilds/werric-knight/defender/protectors-heart.c");
    addChild("/guilds/werric-knight/defender/protectors-heart.c",
        "/guilds/werric-knight/defender/kings-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/werric-knight/defender/ultimate-protector.c");
    addChild("/guilds/werric-knight/defender/ultimate-protector.c",
        "/guilds/werric-knight/defender/protectors-heart.c");

    addResearchElement("/guilds/werric-knight/defender/last-stand.c");
    addChild("/guilds/werric-knight/defender/last-stand.c",
        "/guilds/werric-knight/defender/protectors-heart.c");

    addResearchElement("/guilds/werric-knight/defender/eternal-guardian.c");
    addChild("/guilds/werric-knight/defender/eternal-guardian.c",
        "/guilds/werric-knight/defender/last-stand.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Defender of the Crown");
    Description("This research tree is available only to Knight Commanders who "
        "have proven themselves worthy of the sacred duty of defending the Crown. "
        "It represents the ultimate defensive techniques and the legendary King's "
        "Champion form.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/defender/root.c");
    TreeRoot("/guilds/werric-knight/defender/root.c");

    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
}
