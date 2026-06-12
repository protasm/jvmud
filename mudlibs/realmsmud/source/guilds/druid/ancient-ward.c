//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/barkskin-ward.c");
    addResearchElement("/guilds/druid/ancient-ward/ward-lore.c");
    addResearchElement("/guilds/druid/ancient-ward/warding-instinct.c");

    addChild("/guilds/druid/ancient-ward/barkskin-ward.c",
        "/guilds/druid/ancient-ward/root.c");
    addChild("/guilds/druid/ancient-ward/ward-lore.c",
        "/guilds/druid/ancient-ward/root.c");
    addChild("/guilds/druid/ancient-ward/warding-instinct.c",
        "/guilds/druid/ancient-ward/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(
        "/guilds/druid/ancient-ward/magical-reservoir.c");

    addChild("/guilds/druid/ancient-ward/magical-reservoir.c",
        "/guilds/druid/ancient-ward/ward-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/thorny-shield.c");
    addResearchElement("/guilds/druid/ancient-ward/stone-ward.c");
    addResearchElement(
        "/guilds/druid/ancient-ward/natural-fortress.c");
    addResearchElement("/guilds/druid/ancient-ward/protected-body.c");
    addResearchElement("/guilds/druid/ancient-ward/rooted-stance.c");

    addChild("/guilds/druid/ancient-ward/thorny-shield.c",
        "/guilds/druid/ancient-ward/barkskin-ward.c");
    addChild("/guilds/druid/ancient-ward/stone-ward.c",
        "/guilds/druid/ancient-ward/barkskin-ward.c");
    addChild("/guilds/druid/ancient-ward/natural-fortress.c",
        "/guilds/druid/ancient-ward/ward-lore.c");
    addChild("/guilds/druid/ancient-ward/protected-body.c",
        "/guilds/druid/ancient-ward/warding-instinct.c");
    addChild("/guilds/druid/ancient-ward/rooted-stance.c",
        "/guilds/druid/ancient-ward/warding-instinct.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/druid/ancient-ward/ancient-knowledge.c");
    addResearchElement(
        "/guilds/druid/ancient-ward/channeling-efficiency.c");
    addResearchElement(
        "/guilds/druid/ancient-ward/natural-resilience.c");
    addResearchElement("/guilds/druid/ancient-ward/ward-focus.c");

    addChild("/guilds/druid/ancient-ward/ancient-knowledge.c",
        "/guilds/druid/ancient-ward/natural-fortress.c");
    addChild(
        "/guilds/druid/ancient-ward/channeling-efficiency.c",
        "/guilds/druid/ancient-ward/magical-reservoir.c");
    addChild("/guilds/druid/ancient-ward/natural-resilience.c",
        "/guilds/druid/ancient-ward/protected-body.c");
    addChild("/guilds/druid/ancient-ward/ward-focus.c",
        "/guilds/druid/ancient-ward/rooted-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/wild-aura.c");

    addChild("/guilds/druid/ancient-ward/wild-aura.c",
        "/guilds/druid/ancient-ward/thorny-shield.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/ward-weaving.c");
    addResearchElement("/guilds/druid/ancient-ward/arcane-fortress.c");
    addResearchElement("/guilds/druid/ancient-ward/grove-shield.c");
    addResearchElement("/guilds/druid/ancient-ward/ward-echo.c");

    addChild("/guilds/druid/ancient-ward/ward-weaving.c",
        "/guilds/druid/ancient-ward/ancient-knowledge.c");
    addChild("/guilds/druid/ancient-ward/arcane-fortress.c",
        "/guilds/druid/ancient-ward/channeling-efficiency.c");
    addChild("/guilds/druid/ancient-ward/grove-shield.c",
        "/guilds/druid/ancient-ward/ward-focus.c");
    addChild("/guilds/druid/ancient-ward/ward-echo.c",
        "/guilds/druid/ancient-ward/natural-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/natures-embrace.c");

    addChild("/guilds/druid/ancient-ward/natures-embrace.c",
        "/guilds/druid/ancient-ward/stone-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/thorned-aegis.c");

    addChild("/guilds/druid/ancient-ward/thorned-aegis.c",
        "/guilds/druid/ancient-ward/grove-shield.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/stone-skin.c");

    addChild("/guilds/druid/ancient-ward/stone-skin.c",
        "/guilds/druid/ancient-ward/thorned-aegis.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/ancestral-ward.c");
    addResearchElement("/guilds/druid/ancient-ward/living-ward.c");
    addResearchElement("/guilds/druid/ancient-ward/ancient-skin.c");
    addResearchElement("/guilds/druid/ancient-ward/nature-well.c");

    addChild("/guilds/druid/ancient-ward/ancestral-ward.c",
        "/guilds/druid/ancient-ward/wild-aura.c");
    addChild("/guilds/druid/ancient-ward/living-ward.c",
        "/guilds/druid/ancient-ward/ward-weaving.c");
    addChild("/guilds/druid/ancient-ward/ancient-skin.c",
        "/guilds/druid/ancient-ward/stone-skin.c");
    addChild("/guilds/druid/ancient-ward/nature-well.c",
        "/guilds/druid/ancient-ward/arcane-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/enduring-will.c");

    addChild("/guilds/druid/ancient-ward/enduring-will.c",
        "/guilds/druid/ancient-ward/ancient-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/iron-grove.c");
    addResearchElement(
        "/guilds/druid/ancient-ward/immovable-nature.c");

    addChild("/guilds/druid/ancient-ward/iron-grove.c",
        "/guilds/druid/ancient-ward/natures-embrace.c");
    addChild("/guilds/druid/ancient-ward/immovable-nature.c",
        "/guilds/druid/ancient-ward/enduring-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/ward-permanence.c");

    addChild("/guilds/druid/ancient-ward/ward-permanence.c",
        "/guilds/druid/ancient-ward/enduring-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/deep-reserves.c");

    addChild("/guilds/druid/ancient-ward/deep-reserves.c",
        "/guilds/druid/ancient-ward/nature-well.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/druid/ancient-ward/primal-sanctuary.c");

    addChild("/guilds/druid/ancient-ward/primal-sanctuary.c",
        "/guilds/druid/ancient-ward/ancestral-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/natures-aegis.c");

    addChild("/guilds/druid/ancient-ward/natures-aegis.c",
        "/guilds/druid/ancient-ward/ward-permanence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/druid/ancient-ward/ancient-fortitude.c");
    addResearchElement(
        "/guilds/druid/ancient-ward/arcane-permanence.c");

    addChild("/guilds/druid/ancient-ward/ancient-fortitude.c",
        "/guilds/druid/ancient-ward/immovable-nature.c");
    addChild("/guilds/druid/ancient-ward/arcane-permanence.c",
        "/guilds/druid/ancient-ward/deep-reserves.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/primal-ward.c");
    addResearchElement("/guilds/druid/ancient-ward/ward-mastery-ki.c");

    addChild("/guilds/druid/ancient-ward/primal-ward.c",
        "/guilds/druid/ancient-ward/natures-aegis.c");
    addChild("/guilds/druid/ancient-ward/ward-mastery-ki.c",
        "/guilds/druid/ancient-ward/ancient-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/eternal-ward.c");
    addResearchElement("/guilds/druid/ancient-ward/grove-guardian.c");

    addChild("/guilds/druid/ancient-ward/eternal-ward.c",
        "/guilds/druid/ancient-ward/iron-grove.c");
    addChild("/guilds/druid/ancient-ward/grove-guardian.c",
        "/guilds/druid/ancient-ward/ancient-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement(
        "/guilds/druid/ancient-ward/eternal-guardian.c");

    addChild("/guilds/druid/ancient-ward/eternal-guardian.c",
        "/guilds/druid/ancient-ward/primal-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/druid/ancient-ward/fortress-of-nature.c");
    addResearchElement(
        "/guilds/druid/ancient-ward/ancient-ward-lord.c");

    addChild("/guilds/druid/ancient-ward/fortress-of-nature.c",
        "/guilds/druid/ancient-ward/arcane-permanence.c");
    addChild("/guilds/druid/ancient-ward/ancient-ward-lord.c",
        "/guilds/druid/ancient-ward/eternal-guardian.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/living-stone.c");
    addResearchElement("/guilds/druid/ancient-ward/ward-supremacy.c");

    addChild("/guilds/druid/ancient-ward/living-stone.c",
        "/guilds/druid/ancient-ward/fortress-of-nature.c");
    addChild("/guilds/druid/ancient-ward/ward-supremacy.c",
        "/guilds/druid/ancient-ward/ancient-ward-lord.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/druid/ancient-ward/immovable-force.c");
    addResearchElement(
        "/guilds/druid/ancient-ward/eternal-ward-master.c");

    addChild("/guilds/druid/ancient-ward/immovable-force.c",
        "/guilds/druid/ancient-ward/living-stone.c");
    addChild("/guilds/druid/ancient-ward/eternal-ward-master.c",
        "/guilds/druid/ancient-ward/ward-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/druid/ancient-ward/avatar-of-stone.c");

    addChild("/guilds/druid/ancient-ward/avatar-of-stone.c",
        "/guilds/druid/ancient-ward/immovable-force.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Ancient Ward");
    Description("This research tree grants the druid mastery over "
        "ancient protective magic - wards and auras drawn from the "
        "enduring power of stone, root, and grove that shield the "
        "druid against harm.");
    Source("druid");
    addResearchElement("/guilds/druid/ancient-ward/root.c");
    TreeRoot("/guilds/druid/ancient-ward/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    NinthLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtyNinthLevel();
}
