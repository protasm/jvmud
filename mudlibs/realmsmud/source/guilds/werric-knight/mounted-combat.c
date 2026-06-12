//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/basic-horsemanship.c");
    addChild("/guilds/werric-knight/mounted/basic-horsemanship.c",
        "/guilds/werric-knight/mounted/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/mounted-defense.c");
    addChild("/guilds/werric-knight/mounted/mounted-defense.c",
        "/guilds/werric-knight/mounted/basic-horsemanship.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/charge-attack.c");
    addChild("/guilds/werric-knight/mounted/charge-attack.c",
        "/guilds/werric-knight/mounted/mounted-defense.c");

    addResearchElement("/guilds/werric-knight/mounted/charge-technique.c");
    addChild("/guilds/werric-knight/mounted/charge-technique.c",
        "/guilds/werric-knight/mounted/charge-attack.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/mounted-weapon-training.c");
    addChild("/guilds/werric-knight/mounted/mounted-weapon-training.c",
        "/guilds/werric-knight/mounted/charge-attack.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/cavalry-formation.c");
    addChild("/guilds/werric-knight/mounted/cavalry-formation.c",
        "/guilds/werric-knight/mounted/mounted-weapon-training.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/lance-mastery.c");
    addChild("/guilds/werric-knight/mounted/lance-mastery.c",
        "/guilds/werric-knight/mounted/cavalry-formation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/mounted-agility.c");
    addChild("/guilds/werric-knight/mounted/mounted-agility.c",
        "/guilds/werric-knight/mounted/lance-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/trampling-charge.c");
    addChild("/guilds/werric-knight/mounted/trampling-charge.c",
        "/guilds/werric-knight/mounted/mounted-agility.c");

    addResearchElement("/guilds/werric-knight/mounted/momentum-strike.c");
    addChild("/guilds/werric-knight/mounted/momentum-strike.c",
        "/guilds/werric-knight/mounted/trampling-charge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/expert-rider.c");
    addChild("/guilds/werric-knight/mounted/expert-rider.c",
        "/guilds/werric-knight/mounted/trampling-charge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/mounted-strike.c");
    addChild("/guilds/werric-knight/mounted/mounted-strike.c",
        "/guilds/werric-knight/mounted/expert-rider.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/cavalry-tactics.c");
    addChild("/guilds/werric-knight/mounted/cavalry-tactics.c",
        "/guilds/werric-knight/mounted/mounted-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/thundering-charge.c");
    addChild("/guilds/werric-knight/mounted/thundering-charge.c",
        "/guilds/werric-knight/mounted/cavalry-tactics.c");

    addResearchElement("/guilds/werric-knight/mounted/thundering-impact.c");
    addChild("/guilds/werric-knight/mounted/thundering-impact.c",
        "/guilds/werric-knight/mounted/thundering-charge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/mounted-combat-mastery.c");
    addChild("/guilds/werric-knight/mounted/mounted-combat-mastery.c",
        "/guilds/werric-knight/mounted/thundering-charge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/cavalry-wedge.c");
    addChild("/guilds/werric-knight/mounted/cavalry-wedge.c",
        "/guilds/werric-knight/mounted/mounted-combat-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/unstoppable-momentum.c");
    addChild("/guilds/werric-knight/mounted/unstoppable-momentum.c",
        "/guilds/werric-knight/mounted/cavalry-wedge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/mounted-superiority.c");
    addChild("/guilds/werric-knight/mounted/mounted-superiority.c",
        "/guilds/werric-knight/mounted/unstoppable-momentum.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/devastating-charge.c");
    addChild("/guilds/werric-knight/mounted/devastating-charge.c",
        "/guilds/werric-knight/mounted/mounted-superiority.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/legendary-rider.c");
    addChild("/guilds/werric-knight/mounted/legendary-rider.c",
        "/guilds/werric-knight/mounted/devastating-charge.c");

    addResearchElement("/guilds/werric-knight/mounted/ultimate-cavalry.c");
    addChild("/guilds/werric-knight/mounted/ultimate-cavalry.c",
        "/guilds/werric-knight/mounted/legendary-rider.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/perfect-cavalry.c");
    addChild("/guilds/werric-knight/mounted/perfect-cavalry.c",
        "/guilds/werric-knight/mounted/legendary-rider.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/master-of-horse.c");
    addChild("/guilds/werric-knight/mounted/master-of-horse.c",
        "/guilds/werric-knight/mounted/perfect-cavalry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/apocalyptic-charge.c");
    addChild("/guilds/werric-knight/mounted/apocalyptic-charge.c",
        "/guilds/werric-knight/mounted/master-of-horse.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/transcendent-rider.c");
    addChild("/guilds/werric-knight/mounted/transcendent-rider.c",
        "/guilds/werric-knight/mounted/apocalyptic-charge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySixthLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/supreme-cavalry.c");
    addChild("/guilds/werric-knight/mounted/supreme-cavalry.c",
        "/guilds/werric-knight/mounted/transcendent-rider.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/werric-knight/mounted/eternal-cavalier.c");
    addChild("/guilds/werric-knight/mounted/eternal-cavalier.c",
        "/guilds/werric-knight/mounted/supreme-cavalry.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Mounted Combat");
    Description("This research tree represents the art of fighting from horseback. "
        "Hillgarath's knights have always been renowned cavalry, and these "
        "techniques represent the mounted warfare that has won countless battles.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/mounted/root.c");
    TreeRoot("/guilds/werric-knight/mounted/root.c");

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
    TwentyEighthLevel();
    ThirtiethLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortiethLevel();
    FortyFifthLevel();
    FiftiethLevel();
    FiftyFifthLevel();
    SixtiethLevel();
    SixtyThirdLevel();
    SixtySixthLevel();
    SeventiethLevel();
}

