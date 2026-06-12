//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-stone.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-stone.c",
        "/guilds/guardian-of-khazurath/oaths/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/dwarven-oath.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/word-of-khazurath.c");

    addChild("/guilds/guardian-of-khazurath/oaths/dwarven-oath.c",
        "/guilds/guardian-of-khazurath/oaths/root.c");
    addChild("/guilds/guardian-of-khazurath/oaths/word-of-khazurath.c",
        "/guilds/guardian-of-khazurath/oaths/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/sworn-protector.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-endurance.c");

    addChild("/guilds/guardian-of-khazurath/oaths/sworn-protector.c",
        "/guilds/guardian-of-khazurath/oaths/dwarven-oath.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-endurance.c",
        "/guilds/guardian-of-khazurath/oaths/word-of-khazurath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-battle.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/vow-of-stone.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-valor.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-endurance-lesser.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-battle.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-stone.c");
    addChild("/guilds/guardian-of-khazurath/oaths/vow-of-stone.c",
        "/guilds/guardian-of-khazurath/oaths/sworn-protector.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-valor.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-battle.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-endurance-lesser.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-stone.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-might.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-resilience.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-might.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-battle.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-resilience.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oathsworn.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-the-deep.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oathsworn.c",
        "/guilds/guardian-of-khazurath/oaths/vow-of-stone.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-the-deep.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-iron.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/mountain-oath.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/blood-oath.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-iron.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-stone.c");
    addChild("/guilds/guardian-of-khazurath/oaths/mountain-oath.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-might.c");
    addChild("/guilds/guardian-of-khazurath/oaths/blood-oath.c",
        "/guilds/guardian-of-khazurath/oaths/mountain-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/sworn-guardian.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-endurance-greater.c");

    addChild("/guilds/guardian-of-khazurath/oaths/sworn-guardian.c",
        "/guilds/guardian-of-khazurath/oaths/oathsworn.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-endurance-greater.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-the-deep.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/iron-oath.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/unbreakable-oath.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/fortress-oath.c");

    addChild("/guilds/guardian-of-khazurath/oaths/iron-oath.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-iron.c");
    addChild("/guilds/guardian-of-khazurath/oaths/unbreakable-oath.c",
        "/guilds/guardian-of-khazurath/oaths/mountain-oath.c");
    addChild("/guilds/guardian-of-khazurath/oaths/fortress-oath.c",
        "/guilds/guardian-of-khazurath/oaths/iron-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-keeper.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/deep-oath.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-purpose.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oath-keeper.c",
        "/guilds/guardian-of-khazurath/oaths/sworn-guardian.c");
    addChild("/guilds/guardian-of-khazurath/oaths/deep-oath.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-endurance-greater.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-purpose.c",
        "/guilds/guardian-of-khazurath/oaths/oath-keeper.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-the-mountain.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/ancient-vow.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-the-mountain.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-iron.c");
    addChild("/guilds/guardian-of-khazurath/oaths/ancient-vow.c",
        "/guilds/guardian-of-khazurath/oaths/iron-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/mountain-keeper.c");

    addChild("/guilds/guardian-of-khazurath/oaths/mountain-keeper.c",
        "/guilds/guardian-of-khazurath/oaths/unbreakable-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/eternal-oath.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-khazurath.c");

    addChild("/guilds/guardian-of-khazurath/oaths/eternal-oath.c",
        "/guilds/guardian-of-khazurath/oaths/oath-keeper.c");
    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-khazurath.c",
        "/guilds/guardian-of-khazurath/oaths/deep-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/eternal-vigil.c");

    addChild("/guilds/guardian-of-khazurath/oaths/eternal-vigil.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-the-mountain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/bound-by-stone.c");

    addChild("/guilds/guardian-of-khazurath/oaths/bound-by-stone.c",
        "/guilds/guardian-of-khazurath/oaths/mountain-keeper.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/ancient-guardian.c");

    addChild("/guilds/guardian-of-khazurath/oaths/ancient-guardian.c",
        "/guilds/guardian-of-khazurath/oaths/eternal-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/khazuraths-legacy.c");

    addChild("/guilds/guardian-of-khazurath/oaths/khazuraths-legacy.c",
        "/guilds/guardian-of-khazurath/oaths/eternal-vigil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oaths-fulfilled.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oaths-fulfilled.c",
        "/guilds/guardian-of-khazurath/oaths/oath-of-khazurath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/eternal-bond.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/mountain-pact.c");

    addChild("/guilds/guardian-of-khazurath/oaths/eternal-bond.c",
        "/guilds/guardian-of-khazurath/oaths/ancient-guardian.c");
    addChild("/guilds/guardian-of-khazurath/oaths/mountain-pact.c",
        "/guilds/guardian-of-khazurath/oaths/bound-by-stone.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/oath-of-eternity.c");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/living-oath.c");

    addChild("/guilds/guardian-of-khazurath/oaths/oath-of-eternity.c",
        "/guilds/guardian-of-khazurath/oaths/eternal-bond.c");
    addChild("/guilds/guardian-of-khazurath/oaths/living-oath.c",
        "/guilds/guardian-of-khazurath/oaths/oaths-fulfilled.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/oaths/guardian-eternal.c");

    addChild("/guilds/guardian-of-khazurath/oaths/guardian-eternal.c",
        "/guilds/guardian-of-khazurath/oaths/khazuraths-legacy.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Ancient Oaths");
    Description("The ancient oaths sworn by the first Guardians of Khazurath, "
        "binding them to the mountain forever. Each oath taken deepens the "
        "bond and grants power, building toward the ultimate expression of "
        "what it means to be a Guardian.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/oaths/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/oaths/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    TwentyFirstLevel();
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyFifthLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    SixtiethLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
