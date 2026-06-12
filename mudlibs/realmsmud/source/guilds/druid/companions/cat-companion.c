//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/companions/cat/predator-i.c");
    addResearchElement("/guilds/druid/companions/cat/shadow-i.c");
    addResearchElement("/guilds/druid/companions/cat/sinew-i.c");

    addChild("/guilds/druid/companions/cat/predator-i.c",
        "/guilds/druid/companions/cat/root.c");
    addChild("/guilds/druid/companions/cat/shadow-i.c",
        "/guilds/druid/companions/cat/root.c");
    addChild("/guilds/druid/companions/cat/sinew-i.c",
        "/guilds/druid/companions/cat/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/druid/companions/cat/razor-claws.c");
    addResearchElement("/guilds/druid/companions/cat/silent-step.c");
    addResearchElement("/guilds/druid/companions/cat/lithe-frame.c");

    addChild("/guilds/druid/companions/cat/razor-claws.c",
        "/guilds/druid/companions/cat/root.c");
    addChild("/guilds/druid/companions/cat/silent-step.c",
        "/guilds/druid/companions/cat/root.c");
    addChild("/guilds/druid/companions/cat/lithe-frame.c",
        "/guilds/druid/companions/cat/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/druid/companions/cat/predator-ii.c");
    addResearchElement("/guilds/druid/companions/cat/shadow-ii.c");
    addResearchElement("/guilds/druid/companions/cat/sinew-ii.c");

    addChild("/guilds/druid/companions/cat/predator-ii.c",
        "/guilds/druid/companions/cat/predator-i.c");
    addChild("/guilds/druid/companions/cat/shadow-ii.c",
        "/guilds/druid/companions/cat/shadow-i.c");
    addChild("/guilds/druid/companions/cat/sinew-ii.c",
        "/guilds/druid/companions/cat/sinew-i.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/companions/cat/ambush-instinct.c");
    addResearchElement("/guilds/druid/companions/cat/ghost-step.c");
    addResearchElement("/guilds/druid/companions/cat/iron-sinew.c");

    addChild("/guilds/druid/companions/cat/ambush-instinct.c",
        "/guilds/druid/companions/cat/razor-claws.c");
    addChild("/guilds/druid/companions/cat/ghost-step.c",
        "/guilds/druid/companions/cat/silent-step.c");
    addChild("/guilds/druid/companions/cat/iron-sinew.c",
        "/guilds/druid/companions/cat/lithe-frame.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/companions/cat/predator-iii.c");
    addResearchElement("/guilds/druid/companions/cat/shadow-iii.c");
    addResearchElement("/guilds/druid/companions/cat/sinew-iii.c");

    addChild("/guilds/druid/companions/cat/predator-iii.c",
        "/guilds/druid/companions/cat/predator-ii.c");
    addChild("/guilds/druid/companions/cat/shadow-iii.c",
        "/guilds/druid/companions/cat/shadow-ii.c");
    addChild("/guilds/druid/companions/cat/sinew-iii.c",
        "/guilds/druid/companions/cat/sinew-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/cat/death-mark.c");
    addResearchElement("/guilds/druid/companions/cat/void-step.c");
    addResearchElement("/guilds/druid/companions/cat/feral-endurance.c");

    addChild("/guilds/druid/companions/cat/death-mark.c",
        "/guilds/druid/companions/cat/ambush-instinct.c");
    addChild("/guilds/druid/companions/cat/void-step.c",
        "/guilds/druid/companions/cat/ghost-step.c");
    addChild("/guilds/druid/companions/cat/feral-endurance.c",
        "/guilds/druid/companions/cat/iron-sinew.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/cat/predator-iv.c");
    addResearchElement("/guilds/druid/companions/cat/phantom-agility.c");
    addResearchElement("/guilds/druid/companions/cat/sinew-iv.c");

    addChild("/guilds/druid/companions/cat/predator-iv.c",
        "/guilds/druid/companions/cat/predator-iii.c");
    addChild("/guilds/druid/companions/cat/phantom-agility.c",
        "/guilds/druid/companions/cat/shadow-iii.c");
    addChild("/guilds/druid/companions/cat/sinew-iv.c",
        "/guilds/druid/companions/cat/sinew-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/cat/eviscerate.c");
    addResearchElement("/guilds/druid/companions/cat/shadow-iv.c");
    addResearchElement("/guilds/druid/companions/cat/primal-sinew.c");

    addChild("/guilds/druid/companions/cat/eviscerate.c",
        "/guilds/druid/companions/cat/predator-iv.c");
    addChild("/guilds/druid/companions/cat/shadow-iv.c",
        "/guilds/druid/companions/cat/phantom-agility.c");
    addChild("/guilds/druid/companions/cat/primal-sinew.c",
        "/guilds/druid/companions/cat/feral-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/cat/predator-v.c");
    addResearchElement("/guilds/druid/companions/cat/shadow-v.c");
    addResearchElement("/guilds/druid/companions/cat/sinew-v.c");

    addChild("/guilds/druid/companions/cat/predator-v.c",
        "/guilds/druid/companions/cat/eviscerate.c");
    addChild("/guilds/druid/companions/cat/shadow-v.c",
        "/guilds/druid/companions/cat/shadow-iv.c");
    addChild("/guilds/druid/companions/cat/sinew-v.c",
        "/guilds/druid/companions/cat/primal-sinew.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/cat/lethal-grace.c");
    addResearchElement("/guilds/druid/companions/cat/shadow-walk.c");

    addChild("/guilds/druid/companions/cat/lethal-grace.c",
        "/guilds/druid/companions/cat/predator-v.c");
    addChild("/guilds/druid/companions/cat/shadow-walk.c",
        "/guilds/druid/companions/cat/shadow-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/cat/predator-vi.c");
    addResearchElement("/guilds/druid/companions/cat/evasive-mastery.c");
    addResearchElement("/guilds/druid/companions/cat/apex-strike.c");

    addChild("/guilds/druid/companions/cat/predator-vi.c",
        "/guilds/druid/companions/cat/lethal-grace.c");
    addChild("/guilds/druid/companions/cat/evasive-mastery.c",
        "/guilds/druid/companions/cat/shadow-walk.c");
    addChild("/guilds/druid/companions/cat/apex-strike.c",
        "/guilds/druid/companions/cat/sinew-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/cat/ghost-form.c");
    addResearchElement("/guilds/druid/companions/cat/sinew-vi.c");

    addChild("/guilds/druid/companions/cat/ghost-form.c",
        "/guilds/druid/companions/cat/evasive-mastery.c");
    addChild("/guilds/druid/companions/cat/sinew-vi.c",
        "/guilds/druid/companions/cat/lethal-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/druid/companions/cat/killing-edge.c");
    addResearchElement("/guilds/druid/companions/cat/spectral-evasion.c");
    addResearchElement("/guilds/druid/companions/cat/primal-vitality.c");

    addChild("/guilds/druid/companions/cat/killing-edge.c",
        "/guilds/druid/companions/cat/predator-vi.c");
    addChild("/guilds/druid/companions/cat/spectral-evasion.c",
        "/guilds/druid/companions/cat/ghost-form.c");
    addChild("/guilds/druid/companions/cat/primal-vitality.c",
        "/guilds/druid/companions/cat/sinew-vi.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/druid/companions/cat/apex-predator.c");
    addResearchElement("/guilds/druid/companions/cat/phantom-form.c");
    addResearchElement("/guilds/druid/companions/cat/undying-grace.c");

    addChild("/guilds/druid/companions/cat/apex-predator.c",
        "/guilds/druid/companions/cat/killing-edge.c");
    addChild("/guilds/druid/companions/cat/phantom-form.c",
        "/guilds/druid/companions/cat/spectral-evasion.c");
    addChild("/guilds/druid/companions/cat/undying-grace.c",
        "/guilds/druid/companions/cat/primal-vitality.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/cat/perfect-killer.c");
    addResearchElement("/guilds/druid/companions/cat/shadow-of-the-wild.c");
    addResearchElement("/guilds/druid/companions/cat/primal-grace.c");

    addChild("/guilds/druid/companions/cat/perfect-killer.c",
        "/guilds/druid/companions/cat/apex-predator.c");
    addChild("/guilds/druid/companions/cat/shadow-of-the-wild.c",
        "/guilds/druid/companions/cat/phantom-form.c");
    addChild("/guilds/druid/companions/cat/primal-grace.c",
        "/guilds/druid/companions/cat/undying-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/cat/death-incarnate.c");
    addResearchElement("/guilds/druid/companions/cat/eternal-shadow.c");

    addChild("/guilds/druid/companions/cat/death-incarnate.c",
        "/guilds/druid/companions/cat/perfect-killer.c");
    addChild("/guilds/druid/companions/cat/eternal-shadow.c",
        "/guilds/druid/companions/cat/shadow-of-the-wild.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/druid/companions/cat/spirit-of-the-hunt.c");

    addChild("/guilds/druid/companions/cat/spirit-of-the-hunt.c",
        "/guilds/druid/companions/cat/death-incarnate.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Shadow of the Panther");
    Description("This research tree deepens the druid's bond with their "
        "panther companion, transforming it from a deadly hunter into an "
        "avatar of the primal hunt. As the druid progresses, the panther "
        "grows from a swift and lethal predator into something that moves "
        "like shadow given form, striking with supernatural precision and "
        "vanishing before any defense can be raised.");
    Source("druid");
    addResearchElement("/guilds/druid/companions/cat/root.c");
    TreeRoot("/guilds/druid/companions/cat/root.c");

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
