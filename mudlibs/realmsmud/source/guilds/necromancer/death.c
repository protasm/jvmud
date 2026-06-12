//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/death/chill-touch.c");

    addChild("/guilds/necromancer/death/chill-touch.c",
        "/guilds/necromancer/death/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/death/shadow-bolt.c");

    addChild("/guilds/necromancer/death/shadow-bolt.c",
        "/guilds/necromancer/death/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/death/necrotic-lance.c");

    addChild("/guilds/necromancer/death/necrotic-lance.c",
        "/guilds/necromancer/death/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/death/drain-life.c");

    addChild("/guilds/necromancer/death/drain-life.c",
        "/guilds/necromancer/death/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/death/death-coil.c");
    addResearchElement("/guilds/necromancer/death/shadow-fangs.c");
    addResearchElement("/guilds/necromancer/death/necrotic-bolt.c");

    addChild("/guilds/necromancer/death/death-coil.c",
        "/guilds/necromancer/death/chill-touch.c");
    addChild("/guilds/necromancer/death/shadow-fangs.c",
        "/guilds/necromancer/death/root.c");
    addChild("/guilds/necromancer/death/necrotic-bolt.c",
        "/guilds/necromancer/death/chill-touch.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/death/deathweavers-reserve.c");

    addChild("/guilds/necromancer/death/deathweavers-reserve.c",
        "/guilds/necromancer/death/necrotic-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/death/deathweavers-boon.c");
    addResearchElement("/guilds/necromancer/death/deathweavers-finesse.c");
    addResearchElement("/guilds/necromancer/death/soul-rend.c");

    addChild("/guilds/necromancer/death/deathweavers-boon.c",
        "/guilds/necromancer/death/necrotic-bolt.c");
    addChild("/guilds/necromancer/death/deathweavers-finesse.c",
        "/guilds/necromancer/death/deathweavers-boon.c");
    addChild("/guilds/necromancer/death/soul-rend.c",
        "/guilds/necromancer/death/shadow-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/death/death-trance.c");

    addChild("/guilds/necromancer/death/death-trance.c",
        "/guilds/necromancer/death/death-coil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/death/shadow-storm.c");

    addChild("/guilds/necromancer/death/shadow-storm.c",
        "/guilds/necromancer/death/drain-life.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/death/necrotic-discharge.c");

    addChild("/guilds/necromancer/death/necrotic-discharge.c",
        "/guilds/necromancer/death/necrotic-bolt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/death/dark-channel.c");
    addResearchElement("/guilds/necromancer/death/deathweavers-call.c");

    addChild("/guilds/necromancer/death/dark-channel.c",
        "/guilds/necromancer/death/death-trance.c");
    addChild("/guilds/necromancer/death/deathweavers-call.c",
        "/guilds/necromancer/death/deathweavers-reserve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/necromancer/death/void-strike.c");

    addChild("/guilds/necromancer/death/void-strike.c",
        "/guilds/necromancer/death/shadow-storm.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/necromancer/death/entropy-wave.c");

    addChild("/guilds/necromancer/death/entropy-wave.c",
        "/guilds/necromancer/death/void-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/necromancer/death/soul-shatter.c");

    addChild("/guilds/necromancer/death/soul-shatter.c",
        "/guilds/necromancer/death/necrotic-discharge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/necromancer/death/death-pulse.c");
    addResearchElement("/guilds/necromancer/death/deathweavers-might.c");

    addChild("/guilds/necromancer/death/death-pulse.c",
        "/guilds/necromancer/death/dark-channel.c");
    addChild("/guilds/necromancer/death/deathweavers-might.c",
        "/guilds/necromancer/death/deathweavers-call.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/necromancer/death/necrotic-induction.c");
    addResearchElement("/guilds/necromancer/death/deathweavers-endurance.c");

    addChild("/guilds/necromancer/death/necrotic-induction.c",
        "/guilds/necromancer/death/soul-shatter.c");
    addChild("/guilds/necromancer/death/deathweavers-endurance.c",
        "/guilds/necromancer/death/deathweavers-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/necromancer/death/shadow-tempest.c");

    addChild("/guilds/necromancer/death/shadow-tempest.c",
        "/guilds/necromancer/death/entropy-wave.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/necromancer/death/deep-void.c");

    addChild("/guilds/necromancer/death/deep-void.c",
        "/guilds/necromancer/death/necrotic-induction.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/necromancer/death/deathweavers-fury.c");

    addChild("/guilds/necromancer/death/deathweavers-fury.c",
        "/guilds/necromancer/death/deathweavers-might.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/necromancer/death/deathweavers-strength.c");

    addChild("/guilds/necromancer/death/deathweavers-strength.c",
        "/guilds/necromancer/death/deathweavers-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/necromancer/death/void-condenser.c");

    addChild("/guilds/necromancer/death/void-condenser.c",
        "/guilds/necromancer/death/deep-void.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/necromancer/death/death-absolute.c");

    addChild("/guilds/necromancer/death/death-absolute.c",
        "/guilds/necromancer/death/void-condenser.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/necromancer/death/entropic-eruption.c");

    addChild("/guilds/necromancer/death/entropic-eruption.c",
        "/guilds/necromancer/death/death-absolute.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/necromancer/death/void-vortex.c");

    addChild("/guilds/necromancer/death/void-vortex.c",
        "/guilds/necromancer/death/entropic-eruption.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/necromancer/death/death-incarnate.c");

    addChild("/guilds/necromancer/death/death-incarnate.c",
        "/guilds/necromancer/death/shadow-tempest.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Path of Death");
    Description("This research tree provides knowledge of death-based "
        "spells and abilities for the necromancer.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/death/root.c");
    TreeRoot("/guilds/necromancer/death/root.c");

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
    TwentyThirdLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FiftyFirstLevel();
    FiftySeventhLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
}
