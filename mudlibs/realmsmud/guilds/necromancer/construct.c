//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/construct/necrotic-construct.c");

    addChild("/guilds/necromancer/construct/necrotic-construct.c",
        "/guilds/necromancer/construct/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/construct/spell-focus.c");
    addResearchElement("/guilds/necromancer/construct/necromancers-reach.c");

    addChild("/guilds/necromancer/construct/spell-focus.c",
        "/guilds/necromancer/construct/necrotic-construct.c");
    addChild("/guilds/necromancer/construct/necromancers-reach.c",
        "/guilds/necromancer/construct/necrotic-construct.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/construct/efficient-channeling.c");
    addResearchElement("/guilds/necromancer/construct/extended-reach.c");

    addChild("/guilds/necromancer/construct/efficient-channeling.c",
        "/guilds/necromancer/construct/spell-focus.c");
    addChild("/guilds/necromancer/construct/extended-reach.c",
        "/guilds/necromancer/construct/necromancers-reach.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/construct/greater-spell-focus.c");
    addResearchElement("/guilds/necromancer/construct/greater-reach.c");
    addResearchElement("/guilds/necromancer/construct/rapid-construction.c");

    addChild("/guilds/necromancer/construct/greater-spell-focus.c",
        "/guilds/necromancer/construct/efficient-channeling.c");
    addChild("/guilds/necromancer/construct/greater-reach.c",
        "/guilds/necromancer/construct/extended-reach.c");
    addChild("/guilds/necromancer/construct/rapid-construction.c",
        "/guilds/necromancer/construct/efficient-channeling.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/construct/supreme-spell-focus.c");
    addResearchElement("/guilds/necromancer/construct/construct-mastery.c");

    addChild("/guilds/necromancer/construct/supreme-spell-focus.c",
        "/guilds/necromancer/construct/greater-spell-focus.c");
    addChild("/guilds/necromancer/construct/construct-mastery.c",
        "/guilds/necromancer/construct/rapid-construction.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/construct/supreme-construct-mastery.c");
    addResearchElement("/guilds/necromancer/construct/arcane-construct.c");

    addChild("/guilds/necromancer/construct/supreme-construct-mastery.c",
        "/guilds/necromancer/construct/construct-mastery.c");
    addChild("/guilds/necromancer/construct/arcane-construct.c",
        "/guilds/necromancer/construct/supreme-spell-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Necrotic Spell Construction");
    Description("This research tree provides the knowledge necessary for "
        "constructing custom necromancer spells by combining forms, "
        "functions, and effects.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/construct/root.c");
    TreeRoot("/guilds/necromancer/construct/root.c");

    FirstLevel();
    FifthLevel();
    NinthLevel();
    ThirteenthLevel();
    SeventeenthLevel();
    TwentyFirstLevel();
}
