//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/craft-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/basic-power-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/basic-ward-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/basic-blade-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/stone-lore.c");
    addResearchElement("/guilds/runeskald/rune-crafting/rune-inscription.c");

    addChild("/guilds/runeskald/rune-crafting/craft-rune.c",
        "/guilds/runeskald/rune-crafting/root.c");
    addChild("/guilds/runeskald/rune-crafting/basic-power-rune.c",
        "/guilds/runeskald/rune-crafting/root.c");
    addChild("/guilds/runeskald/rune-crafting/basic-ward-rune.c",
        "/guilds/runeskald/rune-crafting/root.c");
    addChild("/guilds/runeskald/rune-crafting/basic-blade-rune.c",
        "/guilds/runeskald/rune-crafting/root.c");
    addChild("/guilds/runeskald/rune-crafting/stone-lore.c",
        "/guilds/runeskald/rune-crafting/craft-rune.c");
    addChild("/guilds/runeskald/rune-crafting/rune-inscription.c",
        "/guilds/runeskald/rune-crafting/stone-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/basic-storm-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/basic-frost-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/basic-flame-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/rune-binding.c");
    addResearchElement("/guilds/runeskald/rune-crafting/rune-material-lore.c");

    addChild("/guilds/runeskald/rune-crafting/basic-storm-rune.c",
        "/guilds/runeskald/rune-crafting/root.c");
    addChild("/guilds/runeskald/rune-crafting/basic-frost-rune.c",
        "/guilds/runeskald/rune-crafting/root.c");
    addChild("/guilds/runeskald/rune-crafting/basic-flame-rune.c",
        "/guilds/runeskald/rune-crafting/root.c");
    addChild("/guilds/runeskald/rune-crafting/rune-binding.c",
        "/guilds/runeskald/rune-crafting/rune-inscription.c");
    addChild("/guilds/runeskald/rune-crafting/rune-material-lore.c",
        "/guilds/runeskald/rune-crafting/rune-binding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
    addResearchElement("/guilds/runeskald/rune-crafting/elder-power-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/elder-ward-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/elder-blade-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/basic-rune-mastery.c");
    addResearchElement("/guilds/runeskald/rune-crafting/elder-rune-lore.c");

    addChild("/guilds/runeskald/rune-crafting/elder-rune-crafting.c",
        "/guilds/runeskald/rune-crafting/basic-power-rune.c");
    addChild("/guilds/runeskald/rune-crafting/elder-power-rune.c",
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/elder-ward-rune.c",
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/elder-blade-rune.c",
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/basic-rune-mastery.c",
        "/guilds/runeskald/rune-crafting/basic-blade-rune.c");
    addChild("/guilds/runeskald/rune-crafting/elder-rune-lore.c",
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/elder-storm-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/elder-frost-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/elder-flame-rune.c");

    addChild("/guilds/runeskald/rune-crafting/elder-storm-rune.c",
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/elder-frost-rune.c",
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/elder-flame-rune.c",
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-power-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-ward-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-blade-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/elder-rune-mastery.c");
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-rune-lore.c");

    addChild("/guilds/runeskald/rune-crafting/ancient-rune-crafting.c",
        "/guilds/runeskald/rune-crafting/elder-power-rune.c");
    addChild("/guilds/runeskald/rune-crafting/ancient-power-rune.c",
        "/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/ancient-ward-rune.c",
        "/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/ancient-blade-rune.c",
        "/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/elder-rune-mastery.c",
        "/guilds/runeskald/rune-crafting/elder-blade-rune.c");
    addChild("/guilds/runeskald/rune-crafting/ancient-rune-lore.c",
        "/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-storm-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-frost-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-flame-rune.c");

    addChild("/guilds/runeskald/rune-crafting/ancient-storm-rune.c",
        "/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/ancient-frost-rune.c",
        "/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/ancient-flame-rune.c",
        "/guilds/runeskald/rune-crafting/ancient-rune-crafting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
    addResearchElement("/guilds/runeskald/rune-crafting/primal-power-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/primal-ward-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/primal-blade-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/ancient-rune-mastery.c");
    addResearchElement("/guilds/runeskald/rune-crafting/primal-rune-lore.c");

    addChild("/guilds/runeskald/rune-crafting/primal-rune-crafting.c",
        "/guilds/runeskald/rune-crafting/ancient-power-rune.c");
    addChild("/guilds/runeskald/rune-crafting/primal-power-rune.c",
        "/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/primal-ward-rune.c",
        "/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/primal-blade-rune.c",
        "/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/ancient-rune-mastery.c",
        "/guilds/runeskald/rune-crafting/ancient-blade-rune.c");
    addChild("/guilds/runeskald/rune-crafting/primal-rune-lore.c",
        "/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/primal-storm-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/primal-frost-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/primal-flame-rune.c");
    addResearchElement("/guilds/runeskald/rune-crafting/primal-rune-mastery.c");

    addChild("/guilds/runeskald/rune-crafting/primal-storm-rune.c",
        "/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/primal-frost-rune.c",
        "/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/primal-flame-rune.c",
        "/guilds/runeskald/rune-crafting/primal-rune-crafting.c");
    addChild("/guilds/runeskald/rune-crafting/primal-rune-mastery.c",
        "/guilds/runeskald/rune-crafting/primal-blade-rune.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/master-carver.c");

    addChild("/guilds/runeskald/rune-crafting/master-carver.c",
        "/guilds/runeskald/rune-crafting/primal-rune-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/rune-potency.c");

    addChild("/guilds/runeskald/rune-crafting/rune-potency.c",
        "/guilds/runeskald/rune-crafting/primal-rune-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/crafting-efficiency.c");

    addChild("/guilds/runeskald/rune-crafting/crafting-efficiency.c",
        "/guilds/runeskald/rune-crafting/primal-rune-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/runic-calligraphy.c");

    addChild("/guilds/runeskald/rune-crafting/runic-calligraphy.c",
        "/guilds/runeskald/rune-crafting/master-carver.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/rune-potency-ii.c");

    addChild("/guilds/runeskald/rune-crafting/rune-potency-ii.c",
        "/guilds/runeskald/rune-crafting/rune-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/crafting-efficiency-ii.c");

    addChild("/guilds/runeskald/rune-crafting/crafting-efficiency-ii.c",
        "/guilds/runeskald/rune-crafting/crafting-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/deep-inscription.c");

    addChild("/guilds/runeskald/rune-crafting/deep-inscription.c",
        "/guilds/runeskald/rune-crafting/runic-calligraphy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/rune-potency-iii.c");

    addChild("/guilds/runeskald/rune-crafting/rune-potency-iii.c",
        "/guilds/runeskald/rune-crafting/rune-potency-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/crafting-efficiency-iii.c");

    addChild("/guilds/runeskald/rune-crafting/crafting-efficiency-iii.c",
        "/guilds/runeskald/rune-crafting/crafting-efficiency-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/eternal-carving.c");

    addChild("/guilds/runeskald/rune-crafting/eternal-carving.c",
        "/guilds/runeskald/rune-crafting/deep-inscription.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/rune-potency-iv.c");

    addChild("/guilds/runeskald/rune-crafting/rune-potency-iv.c",
        "/guilds/runeskald/rune-crafting/rune-potency-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/crafting-efficiency-iv.c");

    addChild("/guilds/runeskald/rune-crafting/crafting-efficiency-iv.c",
        "/guilds/runeskald/rune-crafting/crafting-efficiency-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/world-rune-script.c");

    addChild("/guilds/runeskald/rune-crafting/world-rune-script.c",
        "/guilds/runeskald/rune-crafting/eternal-carving.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/rune-potency-v.c");

    addChild("/guilds/runeskald/rune-crafting/rune-potency-v.c",
        "/guilds/runeskald/rune-crafting/rune-potency-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/crafting-efficiency-v.c");

    addChild("/guilds/runeskald/rune-crafting/crafting-efficiency-v.c",
        "/guilds/runeskald/rune-crafting/crafting-efficiency-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/primordial-inscription.c");

    addChild("/guilds/runeskald/rune-crafting/primordial-inscription.c",
        "/guilds/runeskald/rune-crafting/world-rune-script.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/rune-potency-vi.c");

    addChild("/guilds/runeskald/rune-crafting/rune-potency-vi.c",
        "/guilds/runeskald/rune-crafting/rune-potency-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/crafting-efficiency-vi.c");

    addChild("/guilds/runeskald/rune-crafting/crafting-efficiency-vi.c",
        "/guilds/runeskald/rune-crafting/crafting-efficiency-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/transcendent-rune-craft.c");

    addChild("/guilds/runeskald/rune-crafting/transcendent-rune-craft.c",
        "/guilds/runeskald/rune-crafting/primordial-inscription.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/runeskald/rune-crafting/rune-potency-vii.c");

    addChild("/guilds/runeskald/rune-crafting/rune-potency-vii.c",
        "/guilds/runeskald/rune-crafting/rune-potency-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Rune Crafting");
    Source("runeskald");
    Description("This tree covers the art of carving, charging, and binding "
        "rune-stones across all four tiers of runeskaldic tradition.");

    addResearchElement("/guilds/runeskald/rune-crafting/root.c");
    TreeRoot("/guilds/runeskald/rune-crafting/root.c");

    FirstLevel();
    ThirdLevel();
    SeventhLevel();
    NinthLevel();
    SeventeenthLevel();
    NineteenthLevel();
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
