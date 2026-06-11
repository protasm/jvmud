//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/stand-firm.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/stand-firm.c",
        "/guilds/guardian-of-khazurath/unbreakable/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/brace.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/resolve.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/brace.c",
        "/guilds/guardian-of-khazurath/unbreakable/root.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/resolve.c",
        "/guilds/guardian-of-khazurath/unbreakable/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/iron-resolve.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/stone-will.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/iron-resolve.c",
        "/guilds/guardian-of-khazurath/unbreakable/resolve.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/stone-will.c",
        "/guilds/guardian-of-khazurath/unbreakable/brace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/stubborn.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/brace-for-impact.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/stubborn.c",
        "/guilds/guardian-of-khazurath/unbreakable/stand-firm.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/brace-for-impact.c",
        "/guilds/guardian-of-khazurath/unbreakable/stone-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/grit.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/mountain-will.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/iron-bones.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/stubborn-endurance.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/grit.c",
        "/guilds/guardian-of-khazurath/unbreakable/iron-resolve.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/mountain-will.c",
        "/guilds/guardian-of-khazurath/unbreakable/brace-for-impact.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/iron-bones.c",
        "/guilds/guardian-of-khazurath/unbreakable/grit.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/stubborn-endurance.c",
        "/guilds/guardian-of-khazurath/unbreakable/mountain-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/unyielding.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/bedrock.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/unyielding.c",
        "/guilds/guardian-of-khazurath/unbreakable/stubborn.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/bedrock.c",
        "/guilds/guardian-of-khazurath/unbreakable/grit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/stone-resolve.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/tectonic-will.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/stone-resolve.c",
        "/guilds/guardian-of-khazurath/unbreakable/mountain-will.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/tectonic-will.c",
        "/guilds/guardian-of-khazurath/unbreakable/bedrock.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/dwarven-stubbornness.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/dwarven-stubbornness.c",
        "/guilds/guardian-of-khazurath/unbreakable/unyielding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/eternal-grit.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/mountain-resolve.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/granite-resolve.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/mountain-spirit.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/eternal-grit.c",
        "/guilds/guardian-of-khazurath/unbreakable/stone-resolve.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/mountain-resolve.c",
        "/guilds/guardian-of-khazurath/unbreakable/tectonic-will.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/granite-resolve.c",
        "/guilds/guardian-of-khazurath/unbreakable/eternal-grit.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/mountain-spirit.c",
        "/guilds/guardian-of-khazurath/unbreakable/mountain-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/immovable-object.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/unshakeable.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/immovable-object.c",
        "/guilds/guardian-of-khazurath/unbreakable/dwarven-stubbornness.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/unshakeable.c",
        "/guilds/guardian-of-khazurath/unbreakable/eternal-grit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/bastion.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/bastion.c",
        "/guilds/guardian-of-khazurath/unbreakable/mountain-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/unstoppable.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/unyielding-core.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/unstoppable.c",
        "/guilds/guardian-of-khazurath/unbreakable/unshakeable.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/unyielding-core.c",
        "/guilds/guardian-of-khazurath/unbreakable/unstoppable.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/last-stand.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/stone-heart.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/last-stand.c",
        "/guilds/guardian-of-khazurath/unbreakable/immovable-object.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/stone-heart.c",
        "/guilds/guardian-of-khazurath/unbreakable/bastion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/khazurath-endurance.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/khazurath-endurance.c",
        "/guilds/guardian-of-khazurath/unbreakable/unstoppable.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/unbreakable-will.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/unbreakable-will.c",
        "/guilds/guardian-of-khazurath/unbreakable/stone-heart.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/indestructible.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/indestructible.c",
        "/guilds/guardian-of-khazurath/unbreakable/last-stand.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/mountain-eternal.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/mountain-eternal.c",
        "/guilds/guardian-of-khazurath/unbreakable/khazurath-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/living-stone.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/absolute-will.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/living-stone.c",
        "/guilds/guardian-of-khazurath/unbreakable/indestructible.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/absolute-will.c",
        "/guilds/guardian-of-khazurath/unbreakable/unbreakable-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/eternal-stone.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/iron-spirit.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/eternal-stone.c",
        "/guilds/guardian-of-khazurath/unbreakable/living-stone.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/iron-spirit.c",
        "/guilds/guardian-of-khazurath/unbreakable/absolute-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/undying-will.c");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/stone-incarnate.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/undying-will.c",
        "/guilds/guardian-of-khazurath/unbreakable/iron-spirit.c");
    addChild("/guilds/guardian-of-khazurath/unbreakable/stone-incarnate.c",
        "/guilds/guardian-of-khazurath/unbreakable/eternal-stone.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/will-of-the-mountain.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/will-of-the-mountain.c",
        "/guilds/guardian-of-khazurath/unbreakable/mountain-eternal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/truly-unbreakable.c");

    addChild("/guilds/guardian-of-khazurath/unbreakable/truly-unbreakable.c",
        "/guilds/guardian-of-khazurath/unbreakable/undying-will.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Unbreakable");
    Description("The ultimate expression of dwarven endurance. Guardians who "
        "walk this path become virtually impossible to destroy, building will, "
        "constitution, and hit points to legendary heights.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/unbreakable/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/unbreakable/root.c");

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
    FortyNinthLevel();
    FiftyThirdLevel();
    FiftySeventhLevel();
    SixtySecondLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
