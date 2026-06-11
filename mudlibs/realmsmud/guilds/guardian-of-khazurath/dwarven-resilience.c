//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/hardy.c");
    addChild("/guilds/guardian-of-khazurath/resilience/hardy.c",
        "/guilds/guardian-of-khazurath/resilience/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/tough-skin.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/dwarven-vitality.c");

    addChild("/guilds/guardian-of-khazurath/resilience/tough-skin.c",
        "/guilds/guardian-of-khazurath/resilience/root.c");
    addChild("/guilds/guardian-of-khazurath/resilience/dwarven-vitality.c",
        "/guilds/guardian-of-khazurath/resilience/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/pain-tolerance.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/stone-blood.c");

    addChild("/guilds/guardian-of-khazurath/resilience/pain-tolerance.c",
        "/guilds/guardian-of-khazurath/resilience/hardy.c");
    addChild("/guilds/guardian-of-khazurath/resilience/stone-blood.c",
        "/guilds/guardian-of-khazurath/resilience/dwarven-vitality.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/thick-hide.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/mountain-blood.c");

    addChild("/guilds/guardian-of-khazurath/resilience/thick-hide.c",
        "/guilds/guardian-of-khazurath/resilience/tough-skin.c");
    addChild("/guilds/guardian-of-khazurath/resilience/mountain-blood.c",
        "/guilds/guardian-of-khazurath/resilience/stone-blood.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/iron-constitution.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/battle-recovery.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/dwarven-fortitude.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/stone-flesh.c");

    addChild("/guilds/guardian-of-khazurath/resilience/iron-constitution.c",
        "/guilds/guardian-of-khazurath/resilience/pain-tolerance.c");
    addChild("/guilds/guardian-of-khazurath/resilience/battle-recovery.c",
        "/guilds/guardian-of-khazurath/resilience/mountain-blood.c");
    addChild("/guilds/guardian-of-khazurath/resilience/dwarven-fortitude.c",
        "/guilds/guardian-of-khazurath/resilience/iron-constitution.c");
    addChild("/guilds/guardian-of-khazurath/resilience/stone-flesh.c",
        "/guilds/guardian-of-khazurath/resilience/battle-recovery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/stone-constitution.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/fast-knit.c");

    addChild("/guilds/guardian-of-khazurath/resilience/stone-constitution.c",
        "/guilds/guardian-of-khazurath/resilience/thick-hide.c");
    addChild("/guilds/guardian-of-khazurath/resilience/fast-knit.c",
        "/guilds/guardian-of-khazurath/resilience/battle-recovery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/dwarven-toughness.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/rapid-mending.c");

    addChild("/guilds/guardian-of-khazurath/resilience/dwarven-toughness.c",
        "/guilds/guardian-of-khazurath/resilience/iron-constitution.c");
    addChild("/guilds/guardian-of-khazurath/resilience/rapid-mending.c",
        "/guilds/guardian-of-khazurath/resilience/fast-knit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/granite-constitution.c");

    addChild("/guilds/guardian-of-khazurath/resilience/granite-constitution.c",
        "/guilds/guardian-of-khazurath/resilience/stone-constitution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/enduring.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/stone-recovery.c");

    addChild("/guilds/guardian-of-khazurath/resilience/enduring.c",
        "/guilds/guardian-of-khazurath/resilience/dwarven-toughness.c");
    addChild("/guilds/guardian-of-khazurath/resilience/stone-recovery.c",
        "/guilds/guardian-of-khazurath/resilience/rapid-mending.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/mountain-constitution.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/khazurath-recovery.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/stone-veins.c");

    addChild("/guilds/guardian-of-khazurath/resilience/mountain-constitution.c",
        "/guilds/guardian-of-khazurath/resilience/granite-constitution.c");
    addChild("/guilds/guardian-of-khazurath/resilience/khazurath-recovery.c",
        "/guilds/guardian-of-khazurath/resilience/stone-recovery.c");
    addChild("/guilds/guardian-of-khazurath/resilience/stone-veins.c",
        "/guilds/guardian-of-khazurath/resilience/mountain-constitution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/titan-constitution.c");

    addChild("/guilds/guardian-of-khazurath/resilience/titan-constitution.c",
        "/guilds/guardian-of-khazurath/resilience/enduring.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/indomitable.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/eternal-recovery.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/mountain-blood-greater.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/endurance-mastery.c");

    addChild("/guilds/guardian-of-khazurath/resilience/indomitable.c",
        "/guilds/guardian-of-khazurath/resilience/mountain-constitution.c");
    addChild("/guilds/guardian-of-khazurath/resilience/eternal-recovery.c",
        "/guilds/guardian-of-khazurath/resilience/khazurath-recovery.c");
    addChild("/guilds/guardian-of-khazurath/resilience/mountain-blood-greater.c",
        "/guilds/guardian-of-khazurath/resilience/indomitable.c");
    addChild("/guilds/guardian-of-khazurath/resilience/endurance-mastery.c",
        "/guilds/guardian-of-khazurath/resilience/eternal-recovery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/mountain-endurance.c");

    addChild("/guilds/guardian-of-khazurath/resilience/mountain-endurance.c",
        "/guilds/guardian-of-khazurath/resilience/titan-constitution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/dwarven-immortality.c");

    addChild("/guilds/guardian-of-khazurath/resilience/dwarven-immortality.c",
        "/guilds/guardian-of-khazurath/resilience/indomitable.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/immortal-fortress.c");

    addChild("/guilds/guardian-of-khazurath/resilience/immortal-fortress.c",
        "/guilds/guardian-of-khazurath/resilience/mountain-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/undying-stone.c");

    addChild("/guilds/guardian-of-khazurath/resilience/undying-stone.c",
        "/guilds/guardian-of-khazurath/resilience/eternal-recovery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/heart-of-stone.c");

    addChild("/guilds/guardian-of-khazurath/resilience/heart-of-stone.c",
        "/guilds/guardian-of-khazurath/resilience/dwarven-immortality.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/ageless-body.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/deep-vitality.c");

    addChild("/guilds/guardian-of-khazurath/resilience/ageless-body.c",
        "/guilds/guardian-of-khazurath/resilience/immortal-fortress.c");
    addChild("/guilds/guardian-of-khazurath/resilience/deep-vitality.c",
        "/guilds/guardian-of-khazurath/resilience/undying-stone.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/mountain-eternal.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/wellspring.c");

    addChild("/guilds/guardian-of-khazurath/resilience/mountain-eternal.c",
        "/guilds/guardian-of-khazurath/resilience/ageless-body.c");
    addChild("/guilds/guardian-of-khazurath/resilience/wellspring.c",
        "/guilds/guardian-of-khazurath/resilience/deep-vitality.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/khazurath-flesh.c");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/eternal-wellspring.c");

    addChild("/guilds/guardian-of-khazurath/resilience/khazurath-flesh.c",
        "/guilds/guardian-of-khazurath/resilience/heart-of-stone.c");
    addChild("/guilds/guardian-of-khazurath/resilience/eternal-wellspring.c",
        "/guilds/guardian-of-khazurath/resilience/wellspring.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/stone-immortal.c");

    addChild("/guilds/guardian-of-khazurath/resilience/stone-immortal.c",
        "/guilds/guardian-of-khazurath/resilience/mountain-eternal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/resilience/flesh-of-the-mountain.c");

    addChild("/guilds/guardian-of-khazurath/resilience/flesh-of-the-mountain.c",
        "/guilds/guardian-of-khazurath/resilience/khazurath-flesh.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Dwarven Resilience");
    Description("The legendary resilience of the dwarven people distilled into "
        "mastery. Guardians who walk this path become nearly impossible to kill, "
        "building vast reserves of health, constitution, and natural recovery.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/resilience/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/resilience/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    TwentiethLevel();
    TwentyThirdLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyFifthLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FiftySecondLevel();
    FiftySeventhLevel();
    SixtySecondLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
