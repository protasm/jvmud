//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/talon-i.c");
    addResearchElement("/guilds/druid/companions/eagle/swift-wing-i.c");
    addResearchElement("/guilds/druid/companions/eagle/keen-eye-i.c");

    addChild("/guilds/druid/companions/eagle/talon-i.c",
        "/guilds/druid/companions/eagle/root.c");
    addChild("/guilds/druid/companions/eagle/swift-wing-i.c",
        "/guilds/druid/companions/eagle/root.c");
    addChild("/guilds/druid/companions/eagle/keen-eye-i.c",
        "/guilds/druid/companions/eagle/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/aerial-grace.c");
    addResearchElement("/guilds/druid/companions/eagle/piercing-gaze.c");
    addResearchElement("/guilds/druid/companions/eagle/feather-light.c");

    addChild("/guilds/druid/companions/eagle/aerial-grace.c",
        "/guilds/druid/companions/eagle/root.c");
    addChild("/guilds/druid/companions/eagle/piercing-gaze.c",
        "/guilds/druid/companions/eagle/root.c");
    addChild("/guilds/druid/companions/eagle/feather-light.c",
        "/guilds/druid/companions/eagle/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/talon-ii.c");
    addResearchElement("/guilds/druid/companions/eagle/swift-wing-ii.c");
    addResearchElement("/guilds/druid/companions/eagle/keen-eye-ii.c");

    addChild("/guilds/druid/companions/eagle/talon-ii.c",
        "/guilds/druid/companions/eagle/talon-i.c");
    addChild("/guilds/druid/companions/eagle/swift-wing-ii.c",
        "/guilds/druid/companions/eagle/swift-wing-i.c");
    addChild("/guilds/druid/companions/eagle/keen-eye-ii.c",
        "/guilds/druid/companions/eagle/keen-eye-i.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/aerial-strike.c");
    addResearchElement("/guilds/druid/companions/eagle/iron-feathers-i.c");
    addResearchElement("/guilds/druid/companions/eagle/hunters-focus-i.c");

    addChild("/guilds/druid/companions/eagle/aerial-strike.c",
        "/guilds/druid/companions/eagle/aerial-grace.c");
    addChild("/guilds/druid/companions/eagle/iron-feathers-i.c",
        "/guilds/druid/companions/eagle/feather-light.c");
    addChild("/guilds/druid/companions/eagle/hunters-focus-i.c",
        "/guilds/druid/companions/eagle/piercing-gaze.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/talon-iii.c");
    addResearchElement("/guilds/druid/companions/eagle/swift-wing-iii.c");
    addResearchElement("/guilds/druid/companions/eagle/keen-eye-iii.c");

    addChild("/guilds/druid/companions/eagle/talon-iii.c",
        "/guilds/druid/companions/eagle/talon-ii.c");
    addChild("/guilds/druid/companions/eagle/swift-wing-iii.c",
        "/guilds/druid/companions/eagle/swift-wing-ii.c");
    addChild("/guilds/druid/companions/eagle/keen-eye-iii.c",
        "/guilds/druid/companions/eagle/keen-eye-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/iron-feathers-ii.c");
    addResearchElement("/guilds/druid/companions/eagle/hunters-focus-ii.c");
    addResearchElement("/guilds/druid/companions/eagle/dive-bomb.c");

    addChild("/guilds/druid/companions/eagle/iron-feathers-ii.c",
        "/guilds/druid/companions/eagle/iron-feathers-i.c");
    addChild("/guilds/druid/companions/eagle/hunters-focus-ii.c",
        "/guilds/druid/companions/eagle/hunters-focus-i.c");
    addChild("/guilds/druid/companions/eagle/dive-bomb.c",
        "/guilds/druid/companions/eagle/aerial-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/talon-iv.c");
    addResearchElement("/guilds/druid/companions/eagle/sky-dancer.c");
    addResearchElement("/guilds/druid/companions/eagle/keen-eye-iv.c");

    addChild("/guilds/druid/companions/eagle/talon-iv.c",
        "/guilds/druid/companions/eagle/talon-iii.c");
    addChild("/guilds/druid/companions/eagle/sky-dancer.c",
        "/guilds/druid/companions/eagle/swift-wing-iii.c");
    addChild("/guilds/druid/companions/eagle/keen-eye-iv.c",
        "/guilds/druid/companions/eagle/keen-eye-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/screaming-dive.c");
    addResearchElement("/guilds/druid/companions/eagle/iron-feathers-iii.c");
    addResearchElement("/guilds/druid/companions/eagle/hunters-focus-iii.c");

    addChild("/guilds/druid/companions/eagle/screaming-dive.c",
        "/guilds/druid/companions/eagle/talon-iv.c");
    addChild("/guilds/druid/companions/eagle/iron-feathers-iii.c",
        "/guilds/druid/companions/eagle/iron-feathers-ii.c");
    addChild("/guilds/druid/companions/eagle/hunters-focus-iii.c",
        "/guilds/druid/companions/eagle/hunters-focus-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/talon-v.c");
    addResearchElement("/guilds/druid/companions/eagle/swift-wing-iv.c");
    addResearchElement("/guilds/druid/companions/eagle/keen-eye-v.c");

    addChild("/guilds/druid/companions/eagle/talon-v.c",
        "/guilds/druid/companions/eagle/screaming-dive.c");
    addChild("/guilds/druid/companions/eagle/swift-wing-iv.c",
        "/guilds/druid/companions/eagle/sky-dancer.c");
    addChild("/guilds/druid/companions/eagle/keen-eye-v.c",
        "/guilds/druid/companions/eagle/hunters-focus-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/lords-mastery.c");
    addResearchElement("/guilds/druid/companions/eagle/wind-dancer.c");

    addChild("/guilds/druid/companions/eagle/lords-mastery.c",
        "/guilds/druid/companions/eagle/talon-v.c");
    addChild("/guilds/druid/companions/eagle/wind-dancer.c",
        "/guilds/druid/companions/eagle/swift-wing-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/talon-vi.c");
    addResearchElement("/guilds/druid/companions/eagle/cyclone-evasion.c");
    addResearchElement("/guilds/druid/companions/eagle/death-from-above.c");

    addChild("/guilds/druid/companions/eagle/talon-vi.c",
        "/guilds/druid/companions/eagle/lords-mastery.c");
    addChild("/guilds/druid/companions/eagle/cyclone-evasion.c",
        "/guilds/druid/companions/eagle/wind-dancer.c");
    addChild("/guilds/druid/companions/eagle/death-from-above.c",
        "/guilds/druid/companions/eagle/keen-eye-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/swift-wing-v.c");
    addResearchElement("/guilds/druid/companions/eagle/iron-feathers-iv.c");

    addChild("/guilds/druid/companions/eagle/swift-wing-v.c",
        "/guilds/druid/companions/eagle/cyclone-evasion.c");
    addChild("/guilds/druid/companions/eagle/iron-feathers-iv.c",
        "/guilds/druid/companions/eagle/iron-feathers-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/killing-strike.c");
    addResearchElement("/guilds/druid/companions/eagle/phantom-flight.c");
    addResearchElement("/guilds/druid/companions/eagle/primal-vitality.c");

    addChild("/guilds/druid/companions/eagle/killing-strike.c",
        "/guilds/druid/companions/eagle/talon-vi.c");
    addChild("/guilds/druid/companions/eagle/phantom-flight.c",
        "/guilds/druid/companions/eagle/swift-wing-v.c");
    addChild("/guilds/druid/companions/eagle/primal-vitality.c",
        "/guilds/druid/companions/eagle/iron-feathers-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/apex-hunter.c");
    addResearchElement("/guilds/druid/companions/eagle/eternal-wind.c");
    addResearchElement("/guilds/druid/companions/eagle/undying-bond.c");

    addChild("/guilds/druid/companions/eagle/apex-hunter.c",
        "/guilds/druid/companions/eagle/killing-strike.c");
    addChild("/guilds/druid/companions/eagle/eternal-wind.c",
        "/guilds/druid/companions/eagle/phantom-flight.c");
    addChild("/guilds/druid/companions/eagle/undying-bond.c",
        "/guilds/druid/companions/eagle/primal-vitality.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/storm-diver.c");
    addResearchElement("/guilds/druid/companions/eagle/sky-sovereign.c");
    addResearchElement("/guilds/druid/companions/eagle/primal-endurance.c");

    addChild("/guilds/druid/companions/eagle/storm-diver.c",
        "/guilds/druid/companions/eagle/apex-hunter.c");
    addChild("/guilds/druid/companions/eagle/sky-sovereign.c",
        "/guilds/druid/companions/eagle/eternal-wind.c");
    addChild("/guilds/druid/companions/eagle/primal-endurance.c",
        "/guilds/druid/companions/eagle/undying-bond.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/wrath-of-the-sky.c");
    addResearchElement("/guilds/druid/companions/eagle/eternal-guardian.c");

    addChild("/guilds/druid/companions/eagle/wrath-of-the-sky.c",
        "/guilds/druid/companions/eagle/storm-diver.c");
    addChild("/guilds/druid/companions/eagle/eternal-guardian.c",
        "/guilds/druid/companions/eagle/sky-sovereign.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/druid/companions/eagle/spirit-of-the-storm.c");

    addChild("/guilds/druid/companions/eagle/spirit-of-the-storm.c",
        "/guilds/druid/companions/eagle/wrath-of-the-sky.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Wings of the Eagle");
    Description("This research tree deepens the druid's bond with their "
        "eagle companion, transforming it from a keen predator into an "
        "avatar of sky and storm. As the druid progresses, the eagle "
        "grows from a swift and deadly hunter into something that moves "
        "between the mortal world and the realm of wind and tempest, "
        "striking with the force of a diving thunderbolt.");
    Source("druid");
    addResearchElement("/guilds/druid/companions/eagle/root.c");
    TreeRoot("/guilds/druid/companions/eagle/root.c");

    FifthLevel();
    SeventhLevel();
    EleventhLevel();
    ThirteenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtySeventhLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    FiftyNinthLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
    SeventiethLevel();
}
