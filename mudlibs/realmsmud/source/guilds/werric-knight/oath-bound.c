//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/oath/disciplined-mind.c");
    addChild("/guilds/werric-knight/oath/disciplined-mind.c",
        "/guilds/werric-knight/oath/root.c");

    addResearchElement("/guilds/werric-knight/oath/i-am-not-my-fear.c");
    addChild("/guilds/werric-knight/oath/i-am-not-my-fear.c",
        "/guilds/werric-knight/oath/disciplined-mind.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/steadfast-spirit.c");
    addChild("/guilds/werric-knight/oath/steadfast-spirit.c",
        "/guilds/werric-knight/oath/i-am-not-my-fear.c");

    addResearchElement("/guilds/werric-knight/oath/rally-the-faithful.c");
    addChild("/guilds/werric-knight/oath/rally-the-faithful.c",
        "/guilds/werric-knight/oath/steadfast-spirit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/oath/iron-will.c");
    addChild("/guilds/werric-knight/oath/iron-will.c",
        "/guilds/werric-knight/oath/rally-the-faithful.c");

    addResearchElement("/guilds/werric-knight/oath/i-am-not-my-doubt.c");
    addChild("/guilds/werric-knight/oath/i-am-not-my-doubt.c",
        "/guilds/werric-knight/oath/iron-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/aura-of-conviction.c");
    addChild("/guilds/werric-knight/oath/aura-of-conviction.c",
        "/guilds/werric-knight/oath/i-am-not-my-doubt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/oath/selfless-service.c");
    addChild("/guilds/werric-knight/oath/selfless-service.c",
        "/guilds/werric-knight/oath/aura-of-conviction.c");

    addResearchElement("/guilds/werric-knight/oath/i-am-not-my-weakness.c");
    addChild("/guilds/werric-knight/oath/i-am-not-my-weakness.c",
        "/guilds/werric-knight/oath/selfless-service.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/banish-fear.c");
    addChild("/guilds/werric-knight/oath/banish-fear.c",
        "/guilds/werric-knight/oath/i-am-not-my-weakness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/oath-of-protection.c");
    addChild("/guilds/werric-knight/oath/oath-of-protection.c",
        "/guilds/werric-knight/oath/banish-fear.c");

    addResearchElement("/guilds/werric-knight/oath/i-am-not-my-pain.c");
    addChild("/guilds/werric-knight/oath/i-am-not-my-pain.c",
        "/guilds/werric-knight/oath/oath-of-protection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/unshakeable-stance.c");
    addChild("/guilds/werric-knight/oath/unshakeable-stance.c",
        "/guilds/werric-knight/oath/i-am-not-my-pain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/absolute-loyalty.c");
    addChild("/guilds/werric-knight/oath/absolute-loyalty.c",
        "/guilds/werric-knight/oath/unshakeable-stance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/oath/unshakeable-resolve.c");
    addChild("/guilds/werric-knight/oath/unshakeable-resolve.c",
        "/guilds/werric-knight/oath/absolute-loyalty.c");

    addResearchElement("/guilds/werric-knight/oath/i-am-not-my-desire.c");
    addChild("/guilds/werric-knight/oath/i-am-not-my-desire.c",
        "/guilds/werric-knight/oath/unshakeable-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/oath/break-the-chains.c");
    addChild("/guilds/werric-knight/oath/break-the-chains.c",
        "/guilds/werric-knight/oath/i-am-not-my-desire.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/pain-is-instruction.c");
    addChild("/guilds/werric-knight/oath/pain-is-instruction.c",
        "/guilds/werric-knight/oath/break-the-chains.c");

    addResearchElement("/guilds/werric-knight/oath/doubt-is-disobedience.c");
    addChild("/guilds/werric-knight/oath/doubt-is-disobedience.c",
        "/guilds/werric-knight/oath/pain-is-instruction.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/werric-knight/oath/beacon-of-resolve.c");
    addChild("/guilds/werric-knight/oath/beacon-of-resolve.c",
        "/guilds/werric-knight/oath/doubt-is-disobedience.c");

    addResearchElement("/guilds/werric-knight/oath/transcendent-will.c");
    addChild("/guilds/werric-knight/oath/transcendent-will.c",
        "/guilds/werric-knight/oath/beacon-of-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/oath/voice-of-command.c");
    addChild("/guilds/werric-knight/oath/voice-of-command.c",
        "/guilds/werric-knight/oath/transcendent-will.c");

    addResearchElement("/guilds/werric-knight/oath/diamond-mind.c");
    addChild("/guilds/werric-knight/oath/diamond-mind.c",
        "/guilds/werric-knight/oath/voice-of-command.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/oath/i-am-the-oath.c");
    addChild("/guilds/werric-knight/oath/i-am-the-oath.c",
        "/guilds/werric-knight/oath/diamond-mind.c");

    addResearchElement("/guilds/werric-knight/oath/werras-resolve.c");
    addChild("/guilds/werric-knight/oath/werras-resolve.c",
        "/guilds/werric-knight/oath/i-am-the-oath.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Oath-Bound");
    Description("This research tree represents the sacred Oath of Werric: "
        "'I am not my will.' Abilities that draw upon sworn dedication.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/oath/root.c");
    TreeRoot("/guilds/werric-knight/oath/root.c");

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
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
}
