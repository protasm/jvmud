//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/druid/companions/bear/might-i.c");
    addResearchElement("/guilds/druid/companions/bear/endurance-i.c");
    addResearchElement("/guilds/druid/companions/bear/iron-hide-i.c");

    addChild("/guilds/druid/companions/bear/might-i.c",
        "/guilds/druid/companions/bear/root.c");
    addChild("/guilds/druid/companions/bear/endurance-i.c",
        "/guilds/druid/companions/bear/root.c");
    addChild("/guilds/druid/companions/bear/iron-hide-i.c",
        "/guilds/druid/companions/bear/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/druid/companions/bear/heavy-blow.c");
    addResearchElement("/guilds/druid/companions/bear/thick-skull.c");
    addResearchElement("/guilds/druid/companions/bear/mountain-stance.c");

    addChild("/guilds/druid/companions/bear/heavy-blow.c",
        "/guilds/druid/companions/bear/root.c");
    addChild("/guilds/druid/companions/bear/thick-skull.c",
        "/guilds/druid/companions/bear/root.c");
    addChild("/guilds/druid/companions/bear/mountain-stance.c",
        "/guilds/druid/companions/bear/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/druid/companions/bear/might-ii.c");
    addResearchElement("/guilds/druid/companions/bear/endurance-ii.c");
    addResearchElement("/guilds/druid/companions/bear/iron-hide-ii.c");

    addChild("/guilds/druid/companions/bear/might-ii.c",
        "/guilds/druid/companions/bear/might-i.c");
    addChild("/guilds/druid/companions/bear/endurance-ii.c",
        "/guilds/druid/companions/bear/endurance-i.c");
    addChild("/guilds/druid/companions/bear/iron-hide-ii.c",
        "/guilds/druid/companions/bear/iron-hide-i.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/druid/companions/bear/heavy-blow-ii.c");
    addResearchElement("/guilds/druid/companions/bear/thick-skull-ii.c");
    addResearchElement("/guilds/druid/companions/bear/crushing-charge.c");

    addChild("/guilds/druid/companions/bear/heavy-blow-ii.c",
        "/guilds/druid/companions/bear/heavy-blow.c");
    addChild("/guilds/druid/companions/bear/thick-skull-ii.c",
        "/guilds/druid/companions/bear/thick-skull.c");
    addChild("/guilds/druid/companions/bear/crushing-charge.c",
        "/guilds/druid/companions/bear/mountain-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/druid/companions/bear/might-iii.c");
    addResearchElement("/guilds/druid/companions/bear/endurance-iii.c");
    addResearchElement("/guilds/druid/companions/bear/iron-hide-iii.c");

    addChild("/guilds/druid/companions/bear/might-iii.c",
        "/guilds/druid/companions/bear/might-ii.c");
    addChild("/guilds/druid/companions/bear/endurance-iii.c",
        "/guilds/druid/companions/bear/endurance-ii.c");
    addChild("/guilds/druid/companions/bear/iron-hide-iii.c",
        "/guilds/druid/companions/bear/iron-hide-ii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/bear/heavy-blow-iii.c");
    addResearchElement("/guilds/druid/companions/bear/thick-skull-iii.c");
    addResearchElement("/guilds/druid/companions/bear/primal-roar.c");

    addChild("/guilds/druid/companions/bear/heavy-blow-iii.c",
        "/guilds/druid/companions/bear/heavy-blow-ii.c");
    addChild("/guilds/druid/companions/bear/thick-skull-iii.c",
        "/guilds/druid/companions/bear/thick-skull-ii.c");
    addChild("/guilds/druid/companions/bear/primal-roar.c",
        "/guilds/druid/companions/bear/crushing-charge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/bear/might-iv.c");
    addResearchElement("/guilds/druid/companions/bear/ursine-fortitude.c");
    addResearchElement("/guilds/druid/companions/bear/iron-hide-iv.c");

    addChild("/guilds/druid/companions/bear/might-iv.c",
        "/guilds/druid/companions/bear/might-iii.c");
    addChild("/guilds/druid/companions/bear/ursine-fortitude.c",
        "/guilds/druid/companions/bear/endurance-iii.c");
    addChild("/guilds/druid/companions/bear/iron-hide-iv.c",
        "/guilds/druid/companions/bear/iron-hide-iii.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/bear/bear-hug.c");
    addResearchElement("/guilds/druid/companions/bear/unstoppable.c");
    addResearchElement("/guilds/druid/companions/bear/earthen-resilience.c");

    addChild("/guilds/druid/companions/bear/bear-hug.c",
        "/guilds/druid/companions/bear/might-iv.c");
    addChild("/guilds/druid/companions/bear/unstoppable.c",
        "/guilds/druid/companions/bear/ursine-fortitude.c");
    addChild("/guilds/druid/companions/bear/earthen-resilience.c",
        "/guilds/druid/companions/bear/iron-hide-iv.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/bear/might-v.c");
    addResearchElement("/guilds/druid/companions/bear/endurance-iv.c");
    addResearchElement("/guilds/druid/companions/bear/iron-hide-v.c");

    addChild("/guilds/druid/companions/bear/might-v.c",
        "/guilds/druid/companions/bear/bear-hug.c");
    addChild("/guilds/druid/companions/bear/endurance-iv.c",
        "/guilds/druid/companions/bear/unstoppable.c");
    addChild("/guilds/druid/companions/bear/iron-hide-v.c",
        "/guilds/druid/companions/bear/earthen-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/bear/mountain-heart.c");
    addResearchElement("/guilds/druid/companions/bear/primal-dominance.c");

    addChild("/guilds/druid/companions/bear/mountain-heart.c",
        "/guilds/druid/companions/bear/endurance-iv.c");
    addChild("/guilds/druid/companions/bear/primal-dominance.c",
        "/guilds/druid/companions/bear/might-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/bear/might-vi.c");
    addResearchElement("/guilds/druid/companions/bear/unyielding-hide.c");
    addResearchElement("/guilds/druid/companions/bear/earthshaking-blow.c");

    addChild("/guilds/druid/companions/bear/might-vi.c",
        "/guilds/druid/companions/bear/primal-dominance.c");
    addChild("/guilds/druid/companions/bear/unyielding-hide.c",
        "/guilds/druid/companions/bear/iron-hide-v.c");
    addChild("/guilds/druid/companions/bear/earthshaking-blow.c",
        "/guilds/druid/companions/bear/mountain-heart.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/druid/companions/bear/endurance-v.c");
    addResearchElement("/guilds/druid/companions/bear/iron-will.c");

    addChild("/guilds/druid/companions/bear/endurance-v.c",
        "/guilds/druid/companions/bear/mountain-heart.c");
    addChild("/guilds/druid/companions/bear/iron-will.c",
        "/guilds/druid/companions/bear/unyielding-hide.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/druid/companions/bear/titanic-strength.c");
    addResearchElement("/guilds/druid/companions/bear/living-fortress.c");
    addResearchElement("/guilds/druid/companions/bear/primal-regeneration.c");

    addChild("/guilds/druid/companions/bear/titanic-strength.c",
        "/guilds/druid/companions/bear/might-vi.c");
    addChild("/guilds/druid/companions/bear/living-fortress.c",
        "/guilds/druid/companions/bear/iron-will.c");
    addChild("/guilds/druid/companions/bear/primal-regeneration.c",
        "/guilds/druid/companions/bear/endurance-v.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/druid/companions/bear/apex-predator.c");
    addResearchElement("/guilds/druid/companions/bear/eternal-endurance.c");
    addResearchElement("/guilds/druid/companions/bear/stone-skin.c");

    addChild("/guilds/druid/companions/bear/apex-predator.c",
        "/guilds/druid/companions/bear/titanic-strength.c");
    addChild("/guilds/druid/companions/bear/eternal-endurance.c",
        "/guilds/druid/companions/bear/primal-regeneration.c");
    addChild("/guilds/druid/companions/bear/stone-skin.c",
        "/guilds/druid/companions/bear/living-fortress.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/druid/companions/bear/ursine-rampage.c");
    addResearchElement("/guilds/druid/companions/bear/ancient-resilience.c");
    addResearchElement("/guilds/druid/companions/bear/fortress-stance.c");

    addChild("/guilds/druid/companions/bear/ursine-rampage.c",
        "/guilds/druid/companions/bear/apex-predator.c");
    addChild("/guilds/druid/companions/bear/ancient-resilience.c",
        "/guilds/druid/companions/bear/eternal-endurance.c");
    addChild("/guilds/druid/companions/bear/fortress-stance.c",
        "/guilds/druid/companions/bear/stone-skin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/druid/companions/bear/wrath-of-the-wilds.c");
    addResearchElement("/guilds/druid/companions/bear/eternal-guardian.c");

    addChild("/guilds/druid/companions/bear/wrath-of-the-wilds.c",
        "/guilds/druid/companions/bear/ursine-rampage.c");
    addChild("/guilds/druid/companions/bear/eternal-guardian.c",
        "/guilds/druid/companions/bear/ancient-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/druid/companions/bear/spirit-of-the-ancient.c");

    addChild("/guilds/druid/companions/bear/spirit-of-the-ancient.c",
        "/guilds/druid/companions/bear/wrath-of-the-wilds.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Kin of the Bear");
    Description("This research tree deepens the druid's bond with their "
        "bear companion, transforming it from a formidable creature into "
        "an unstoppable bastion of primal strength. As the druid "
        "progresses, the bear grows into a living fortress of the wild, "
        "combining tremendous offensive power with nearly impenetrable "
        "natural defenses.");
    Source("druid");
    addResearchElement("/guilds/druid/companions/bear/root.c");
    TreeRoot("/guilds/druid/companions/bear/root.c");

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
