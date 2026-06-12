//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/defense/armor-proficiency.c");
    addChild("/guilds/werric-knight/defense/armor-proficiency.c",
        "/guilds/werric-knight/defense/root.c");

    addResearchElement("/guilds/werric-knight/defense/positional-awareness.c");
    addChild("/guilds/werric-knight/defense/positional-awareness.c",
        "/guilds/werric-knight/defense/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/defense/defensive-reading.c");
    addChild("/guilds/werric-knight/defense/defensive-reading.c",
        "/guilds/werric-knight/defense/positional-awareness.c");

    addResearchElement("/guilds/werric-knight/defense/defensive-anticipation.c");
    addChild("/guilds/werric-knight/defense/defensive-anticipation.c",
        "/guilds/werric-knight/defense/armor-proficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/defense/tactical-defense.c");
    addChild("/guilds/werric-knight/defense/tactical-defense.c",
        "/guilds/werric-knight/defense/defensive-reading.c");

    addResearchElement("/guilds/werric-knight/defense/measured-response.c");
    addChild("/guilds/werric-knight/defense/measured-response.c",
        "/guilds/werric-knight/defense/defensive-anticipation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/defense/deflecting-blow.c");
    addChild("/guilds/werric-knight/defense/deflecting-blow.c",
        "/guilds/werric-knight/defense/armor-proficiency.c");

    addResearchElement("/guilds/werric-knight/defense/terrain-advantage.c");
    addChild("/guilds/werric-knight/defense/terrain-advantage.c",
        "/guilds/werric-knight/defense/tactical-defense.c");

    addResearchElement("/guilds/werric-knight/defense/defensive-discipline.c");
    addChild("/guilds/werric-knight/defense/defensive-discipline.c",
        "/guilds/werric-knight/defense/measured-response.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/defense/perfect-positioning.c");
    addChild("/guilds/werric-knight/defense/perfect-positioning.c",
        "/guilds/werric-knight/defense/terrain-advantage.c");

    addResearchElement("/guilds/werric-knight/defense/tactical-shift.c");
    addChild("/guilds/werric-knight/defense/tactical-shift.c",
        "/guilds/werric-knight/defense/defensive-discipline.c");

    addResearchElement("/guilds/werric-knight/defense/defensive-precision.c");
    addChild("/guilds/werric-knight/defense/defensive-precision.c",
        "/guilds/werric-knight/defense/deflecting-blow.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/defense/counter-tactics.c");
    addChild("/guilds/werric-knight/defense/counter-tactics.c",
        "/guilds/werric-knight/defense/defensive-precision.c");

    addResearchElement("/guilds/werric-knight/defense/defensive-coordination.c");
    addChild("/guilds/werric-knight/defense/defensive-coordination.c",
        "/guilds/werric-knight/defense/perfect-positioning.c");

    addResearchElement("/guilds/werric-knight/defense/master-of-terrain.c");
    addChild("/guilds/werric-knight/defense/master-of-terrain.c",
        "/guilds/werric-knight/defense/perfect-positioning.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/defense/fortified-defense.c");
    addChild("/guilds/werric-knight/defense/fortified-defense.c",
        "/guilds/werric-knight/defense/deflecting-blow.c");

    addResearchElement("/guilds/werric-knight/defense/defensive-pivot.c");
    addChild("/guilds/werric-knight/defense/defensive-pivot.c",
        "/guilds/werric-knight/defense/tactical-shift.c");

    addResearchElement("/guilds/werric-knight/defense/zone-control.c");
    addChild("/guilds/werric-knight/defense/zone-control.c",
        "/guilds/werric-knight/defense/master-of-terrain.c");

    addResearchElement("/guilds/werric-knight/defense/defensive-mastery.c");
    addChild("/guilds/werric-knight/defense/defensive-mastery.c",
        "/guilds/werric-knight/defense/counter-tactics.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/defense/defensive-reposition.c");
    addChild("/guilds/werric-knight/defense/defensive-reposition.c",
        "/guilds/werric-knight/defense/defensive-pivot.c");

    addResearchElement("/guilds/werric-knight/defense/strategic-withdrawal.c");
    addChild("/guilds/werric-knight/defense/strategic-withdrawal.c",
        "/guilds/werric-knight/defense/defensive-coordination.c");

    addResearchElement("/guilds/werric-knight/defense/adaptive-defense.c");
    addChild("/guilds/werric-knight/defense/adaptive-defense.c",
        "/guilds/werric-knight/defense/defensive-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/defense/fortify-position.c");
    addChild("/guilds/werric-knight/defense/fortify-position.c",
        "/guilds/werric-knight/defense/zone-control.c");

    addResearchElement("/guilds/werric-knight/defense/eternal-vigilance.c");
    addChild("/guilds/werric-knight/defense/eternal-vigilance.c",
        "/guilds/werric-knight/defense/strategic-withdrawal.c");

    addResearchElement("/guilds/werric-knight/defense/defensive-circle.c");
    addChild("/guilds/werric-knight/defense/defensive-circle.c",
        "/guilds/werric-knight/defense/adaptive-defense.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/defense/impenetrable-guard.c");
    addChild("/guilds/werric-knight/defense/impenetrable-guard.c",
        "/guilds/werric-knight/defense/fortified-defense.c");

    addResearchElement("/guilds/werric-knight/defense/impenetrable-defense.c");
    addChild("/guilds/werric-knight/defense/impenetrable-defense.c",
        "/guilds/werric-knight/defense/fortify-position.c");

    addResearchElement("/guilds/werric-knight/defense/unassailable-position.c");
    addChild("/guilds/werric-knight/defense/unassailable-position.c",
        "/guilds/werric-knight/defense/eternal-vigilance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/defense/absolute-defense.c");
    addChild("/guilds/werric-knight/defense/absolute-defense.c",
        "/guilds/werric-knight/defense/impenetrable-defense.c");

    addResearchElement("/guilds/werric-knight/defense/tactical-fortress.c");
    addChild("/guilds/werric-knight/defense/tactical-fortress.c",
        "/guilds/werric-knight/defense/unassailable-position.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/defense/defensive-supremacy.c");
    addChild("/guilds/werric-knight/defense/defensive-supremacy.c",
        "/guilds/werric-knight/defense/absolute-defense.c");

    addResearchElement("/guilds/werric-knight/defense/supreme-tactical-defense.c");
    addChild("/guilds/werric-knight/defense/supreme-tactical-defense.c",
        "/guilds/werric-knight/defense/tactical-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/defense/guardian-stance.c");
    addChild("/guilds/werric-knight/defense/guardian-stance.c",
        "/guilds/werric-knight/defense/impenetrable-guard.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/defense/legendary-defender.c");
    addChild("/guilds/werric-knight/defense/legendary-defender.c",
        "/guilds/werric-knight/defense/defensive-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/werric-knight/defense/timeless-defender.c");
    addChild("/guilds/werric-knight/defense/timeless-defender.c",
        "/guilds/werric-knight/defense/legendary-defender.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Defensive Tactics");
    Description("This research tree provides advanced defensive combat techniques, "
        "allowing knights to protect themselves and their allies through "
        "armor mastery, tactical positioning, and impenetrable defenses.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/defense/root.c");
    TreeRoot("/guilds/werric-knight/defense/root.c");

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
    TwentySeventhLevel();
    ThirtyFirstLevel();
    FortiethLevel();
}
