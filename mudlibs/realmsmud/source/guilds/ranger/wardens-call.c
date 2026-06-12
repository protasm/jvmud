//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-focus.c");

    addChild("/guilds/ranger/wardens-call/wardens-focus.c",
        "/guilds/ranger/wardens-call/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-precision.c");
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-endurance.c");

    addChild("/guilds/ranger/wardens-call/wardens-precision.c",
        "/guilds/ranger/wardens-call/wardens-focus.c");
    addChild("/guilds/ranger/wardens-call/wardens-endurance.c",
        "/guilds/ranger/wardens-call/wardens-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-insight.c");

    addChild("/guilds/ranger/wardens-call/wardens-insight.c",
        "/guilds/ranger/wardens-call/wardens-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/enhanced-archery.c");
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-reflexes.c");

    addChild("/guilds/ranger/wardens-call/enhanced-archery.c",
        "/guilds/ranger/wardens-call/wardens-precision.c");
    addChild("/guilds/ranger/wardens-call/wardens-reflexes.c",
        "/guilds/ranger/wardens-call/wardens-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/enhanced-elements.c");

    addChild("/guilds/ranger/wardens-call/enhanced-elements.c",
        "/guilds/ranger/wardens-call/wardens-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-arrow-lore.c");

    addChild("/guilds/ranger/wardens-call/wardens-arrow-lore.c",
        "/guilds/ranger/wardens-call/enhanced-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-might.c");

    addChild("/guilds/ranger/wardens-call/wardens-might.c",
        "/guilds/ranger/wardens-call/enhanced-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-wisdom.c");
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-resilience.c");

    addChild("/guilds/ranger/wardens-call/wardens-wisdom.c",
        "/guilds/ranger/wardens-call/enhanced-elements.c");
    addChild("/guilds/ranger/wardens-call/wardens-resilience.c",
        "/guilds/ranger/wardens-call/wardens-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-element-lore.c");

    addChild(
        "/guilds/ranger/wardens-call/wardens-element-lore.c",
        "/guilds/ranger/wardens-call/enhanced-elements.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/coordinated-assault.c");

    addChild(
        "/guilds/ranger/wardens-call/coordinated-assault.c",
        "/guilds/ranger/wardens-call/wardens-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/elemental-harmony.c");

    addChild(
        "/guilds/ranger/wardens-call/elemental-harmony.c",
        "/guilds/ranger/wardens-call/wardens-wisdom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/storm-and-earth.c");
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-fortitude.c");

    addChild("/guilds/ranger/wardens-call/storm-and-earth.c",
        "/guilds/ranger/wardens-call/elemental-harmony.c");
    addChild("/guilds/ranger/wardens-call/wardens-fortitude.c",
        "/guilds/ranger/wardens-call/coordinated-assault.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-blade-lore.c");

    addChild(
        "/guilds/ranger/wardens-call/wardens-blade-lore.c",
        "/guilds/ranger/wardens-call/coordinated-assault.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-authority.c");

    addChild(
        "/guilds/ranger/wardens-call/wardens-authority.c",
        "/guilds/ranger/wardens-call/coordinated-assault.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/natures-blessing.c");

    addChild("/guilds/ranger/wardens-call/natures-blessing.c",
        "/guilds/ranger/wardens-call/storm-and-earth.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/greater-arrow-lore.c");

    addChild(
        "/guilds/ranger/wardens-call/greater-arrow-lore.c",
        "/guilds/ranger/wardens-call/wardens-authority.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/superior-archery.c");
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-vigor.c");

    addChild("/guilds/ranger/wardens-call/superior-archery.c",
        "/guilds/ranger/wardens-call/wardens-authority.c");
    addChild("/guilds/ranger/wardens-call/wardens-vigor.c",
        "/guilds/ranger/wardens-call/wardens-authority.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/superior-elements.c");

    addChild(
        "/guilds/ranger/wardens-call/superior-elements.c",
        "/guilds/ranger/wardens-call/natures-blessing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/greater-element-lore.c");

    addChild(
        "/guilds/ranger/wardens-call/greater-element-lore.c",
        "/guilds/ranger/wardens-call/natures-blessing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-legacy.c");

    addChild("/guilds/ranger/wardens-call/wardens-legacy.c",
        "/guilds/ranger/wardens-call/superior-archery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/primal-blessing.c");

    addChild("/guilds/ranger/wardens-call/primal-blessing.c",
        "/guilds/ranger/wardens-call/superior-elements.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/supreme-arrow-lore.c");
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-protection.c");

    addChild(
        "/guilds/ranger/wardens-call/supreme-arrow-lore.c",
        "/guilds/ranger/wardens-call/wardens-legacy.c");
    addChild(
        "/guilds/ranger/wardens-call/wardens-protection.c",
        "/guilds/ranger/wardens-call/wardens-legacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-command.c");

    addChild("/guilds/ranger/wardens-call/wardens-command.c",
        "/guilds/ranger/wardens-call/wardens-legacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/greater-harmony.c");

    addChild("/guilds/ranger/wardens-call/greater-harmony.c",
        "/guilds/ranger/wardens-call/primal-blessing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/supreme-element-lore.c");

    addChild(
        "/guilds/ranger/wardens-call/supreme-element-lore.c",
        "/guilds/ranger/wardens-call/greater-harmony.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-mastery.c");

    addChild("/guilds/ranger/wardens-call/wardens-mastery.c",
        "/guilds/ranger/wardens-call/wardens-command.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/supreme-elements.c");

    addChild("/guilds/ranger/wardens-call/supreme-elements.c",
        "/guilds/ranger/wardens-call/greater-harmony.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/supreme-blade-lore.c");
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-immortality.c");

    addChild(
        "/guilds/ranger/wardens-call/supreme-blade-lore.c",
        "/guilds/ranger/wardens-call/wardens-mastery.c");
    addChild(
        "/guilds/ranger/wardens-call/wardens-immortality.c",
        "/guilds/ranger/wardens-call/wardens-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-supremacy.c");

    addChild(
        "/guilds/ranger/wardens-call/wardens-supremacy.c",
        "/guilds/ranger/wardens-call/wardens-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/natures-perfection.c");

    addChild(
        "/guilds/ranger/wardens-call/natures-perfection.c",
        "/guilds/ranger/wardens-call/supreme-elements.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/transcendent-lore.c");

    addChild(
        "/guilds/ranger/wardens-call/transcendent-lore.c",
        "/guilds/ranger/wardens-call/wardens-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/wardens-transcendence.c");

    addChild(
        "/guilds/ranger/wardens-call/wardens-transcendence.c",
        "/guilds/ranger/wardens-call/wardens-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement(
        "/guilds/ranger/wardens-call/eternal-warden-call.c");

    addChild(
        "/guilds/ranger/wardens-call/eternal-warden-call.c",
        "/guilds/ranger/wardens-call/natures-perfection.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Warden's Call");
    Description("This research tree provides the ranger with "
        "advanced warden techniques that enhance all combat "
        "abilities - archery, elemental arrows, and blade work.");
    Source("ranger");
    addResearchElement("/guilds/ranger/wardens-call/root.c");
    TreeRoot("/guilds/ranger/wardens-call/root.c");

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
    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
