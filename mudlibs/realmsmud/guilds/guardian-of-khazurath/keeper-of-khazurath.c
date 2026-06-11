//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/guardian-aura.c");
    addChild("/guilds/guardian-of-khazurath/keeper/guardian-aura.c",
        "/guilds/guardian-of-khazurath/keeper/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/steadfast-presence.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/wardens-eye.c");

    addChild("/guilds/guardian-of-khazurath/keeper/steadfast-presence.c",
        "/guilds/guardian-of-khazurath/keeper/root.c");
    addChild("/guilds/guardian-of-khazurath/keeper/wardens-eye.c",
        "/guilds/guardian-of-khazurath/keeper/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/inspiring-guard.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/sentinel.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/wardens-strength.c");

    addChild("/guilds/guardian-of-khazurath/keeper/inspiring-guard.c",
        "/guilds/guardian-of-khazurath/keeper/steadfast-presence.c");
    addChild("/guilds/guardian-of-khazurath/keeper/sentinel.c",
        "/guilds/guardian-of-khazurath/keeper/wardens-eye.c");
    addChild("/guilds/guardian-of-khazurath/keeper/wardens-strength.c",
        "/guilds/guardian-of-khazurath/keeper/sentinel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/dwarven-rally.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/iron-presence.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/iron-vow.c");

    addChild("/guilds/guardian-of-khazurath/keeper/dwarven-rally.c",
        "/guilds/guardian-of-khazurath/keeper/guardian-aura.c");
    addChild("/guilds/guardian-of-khazurath/keeper/iron-presence.c",
        "/guilds/guardian-of-khazurath/keeper/inspiring-guard.c");
    addChild("/guilds/guardian-of-khazurath/keeper/iron-vow.c",
        "/guilds/guardian-of-khazurath/keeper/dwarven-rally.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/protective-stance.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/wardens-call.c");

    addChild("/guilds/guardian-of-khazurath/keeper/protective-stance.c",
        "/guilds/guardian-of-khazurath/keeper/guardian-aura.c");
    addChild("/guilds/guardian-of-khazurath/keeper/wardens-call.c",
        "/guilds/guardian-of-khazurath/keeper/sentinel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/stone-commander.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/mountain-presence.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/stone-authority.c");

    addChild("/guilds/guardian-of-khazurath/keeper/stone-commander.c",
        "/guilds/guardian-of-khazurath/keeper/dwarven-rally.c");
    addChild("/guilds/guardian-of-khazurath/keeper/mountain-presence.c",
        "/guilds/guardian-of-khazurath/keeper/iron-presence.c");
    addChild("/guilds/guardian-of-khazurath/keeper/stone-authority.c",
        "/guilds/guardian-of-khazurath/keeper/stone-commander.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/shield-of-khazurath.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/wardens-resolve.c");

    addChild("/guilds/guardian-of-khazurath/keeper/shield-of-khazurath.c",
        "/guilds/guardian-of-khazurath/keeper/protective-stance.c");
    addChild("/guilds/guardian-of-khazurath/keeper/wardens-resolve.c",
        "/guilds/guardian-of-khazurath/keeper/wardens-call.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/battle-cry.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/stalwart-commander.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/battle-presence.c");

    addChild("/guilds/guardian-of-khazurath/keeper/battle-cry.c",
        "/guilds/guardian-of-khazurath/keeper/stone-commander.c");
    addChild("/guilds/guardian-of-khazurath/keeper/stalwart-commander.c",
        "/guilds/guardian-of-khazurath/keeper/mountain-presence.c");
    addChild("/guilds/guardian-of-khazurath/keeper/battle-presence.c",
        "/guilds/guardian-of-khazurath/keeper/battle-cry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/defend-the-halls.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/eternal-sentinel.c");

    addChild("/guilds/guardian-of-khazurath/keeper/defend-the-halls.c",
        "/guilds/guardian-of-khazurath/keeper/shield-of-khazurath.c");
    addChild("/guilds/guardian-of-khazurath/keeper/eternal-sentinel.c",
        "/guilds/guardian-of-khazurath/keeper/wardens-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/rallying-presence.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/granite-command.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/mountain-authority.c");

    addChild("/guilds/guardian-of-khazurath/keeper/rallying-presence.c",
        "/guilds/guardian-of-khazurath/keeper/battle-cry.c");
    addChild("/guilds/guardian-of-khazurath/keeper/granite-command.c",
        "/guilds/guardian-of-khazurath/keeper/stalwart-commander.c");
    addChild("/guilds/guardian-of-khazurath/keeper/mountain-authority.c",
        "/guilds/guardian-of-khazurath/keeper/rallying-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/mountain-bastion.c");

    addChild("/guilds/guardian-of-khazurath/keeper/mountain-bastion.c",
        "/guilds/guardian-of-khazurath/keeper/defend-the-halls.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/fortress-commander.c");

    addChild("/guilds/guardian-of-khazurath/keeper/fortress-commander.c",
        "/guilds/guardian-of-khazurath/keeper/defend-the-halls.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/khazurath-presence.c");

    addChild("/guilds/guardian-of-khazurath/keeper/khazurath-presence.c",
        "/guilds/guardian-of-khazurath/keeper/rallying-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/warden-of-the-deep.c");

    addChild("/guilds/guardian-of-khazurath/keeper/warden-of-the-deep.c",
        "/guilds/guardian-of-khazurath/keeper/eternal-sentinel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/eternal-guardian.c");

    addChild("/guilds/guardian-of-khazurath/keeper/eternal-guardian.c",
        "/guilds/guardian-of-khazurath/keeper/mountain-bastion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/immortal-warden.c");

    addChild("/guilds/guardian-of-khazurath/keeper/immortal-warden.c",
        "/guilds/guardian-of-khazurath/keeper/khazurath-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/voice-of-khazurath.c");

    addChild("/guilds/guardian-of-khazurath/keeper/voice-of-khazurath.c",
        "/guilds/guardian-of-khazurath/keeper/warden-of-the-deep.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/unshakeable-command.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/deep-warden.c");

    addChild("/guilds/guardian-of-khazurath/keeper/unshakeable-command.c",
        "/guilds/guardian-of-khazurath/keeper/immortal-warden.c");
    addChild("/guilds/guardian-of-khazurath/keeper/deep-warden.c",
        "/guilds/guardian-of-khazurath/keeper/voice-of-khazurath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/mountain-commander.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/eternal-watch.c");

    addChild("/guilds/guardian-of-khazurath/keeper/mountain-commander.c",
        "/guilds/guardian-of-khazurath/keeper/unshakeable-command.c");
    addChild("/guilds/guardian-of-khazurath/keeper/eternal-watch.c",
        "/guilds/guardian-of-khazurath/keeper/deep-warden.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/ancient-presence.c");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/undying-sentinel.c");

    addChild("/guilds/guardian-of-khazurath/keeper/ancient-presence.c",
        "/guilds/guardian-of-khazurath/keeper/mountain-commander.c");
    addChild("/guilds/guardian-of-khazurath/keeper/undying-sentinel.c",
        "/guilds/guardian-of-khazurath/keeper/eternal-watch.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/paragon-guardian.c");

    addChild("/guilds/guardian-of-khazurath/keeper/paragon-guardian.c",
        "/guilds/guardian-of-khazurath/keeper/eternal-guardian.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhiethLevel()
{
    addResearchElement("/guilds/guardian-of-khazurath/keeper/chosen-of-khazurath.c");

    addChild("/guilds/guardian-of-khazurath/keeper/chosen-of-khazurath.c",
        "/guilds/guardian-of-khazurath/keeper/ancient-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Keeper of Khazurath");
    Description("The sacred duty of the Keeper - to guard allies, hold ground, "
        "and inspire those who stand beside them. This tree focuses on auras, "
        "presence, and abilities that make the Guardian a cornerstone of "
        "any fighting force.");
    Source("Guardian of Khazurath");
    addResearchElement("/guilds/guardian-of-khazurath/keeper/root.c");
    TreeRoot("/guilds/guardian-of-khazurath/keeper/root.c");

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
    TwentySeventhLevel();
    ThirtyFirstLevel();
    ThirtyFifthLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FiftySecondLevel();
    FiftySeventhLevel();
    SixtySecondLevel();
    SixtyFifthLevel();
    SeventhiethLevel();
}
