//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/necromancer/wraith/ethereal-touch.c");
    addResearchElement("/guilds/necromancer/wraith/shadow-form.c");

    addChild("/guilds/necromancer/wraith/ethereal-touch.c",
        "/guilds/necromancer/wraith/root.c");
    addChild("/guilds/necromancer/wraith/shadow-form.c",
        "/guilds/necromancer/wraith/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/necromancer/wraith/life-drain.c");
    addResearchElement("/guilds/necromancer/wraith/incorporeal.c");
    addResearchElement("/guilds/necromancer/wraith/terror.c");

    addChild("/guilds/necromancer/wraith/life-drain.c",
        "/guilds/necromancer/wraith/ethereal-touch.c");
    addChild("/guilds/necromancer/wraith/incorporeal.c",
        "/guilds/necromancer/wraith/shadow-form.c");
    addChild("/guilds/necromancer/wraith/terror.c",
        "/guilds/necromancer/wraith/shadow-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/necromancer/wraith/soul-siphon.c");
    addResearchElement("/guilds/necromancer/wraith/phase-shift.c");
    addResearchElement("/guilds/necromancer/wraith/dread-presence.c");
    addResearchElement("/guilds/necromancer/wraith/shadow-haunt.c");

    addChild("/guilds/necromancer/wraith/soul-siphon.c",
        "/guilds/necromancer/wraith/life-drain.c");
    addChild("/guilds/necromancer/wraith/phase-shift.c",
        "/guilds/necromancer/wraith/incorporeal.c");
    addChild("/guilds/necromancer/wraith/dread-presence.c",
        "/guilds/necromancer/wraith/terror.c");
    addChild("/guilds/necromancer/wraith/shadow-haunt.c",
        "/guilds/necromancer/wraith/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/necromancer/wraith/essence-drain.c");
    addResearchElement("/guilds/necromancer/wraith/void-step.c");
    addResearchElement("/guilds/necromancer/wraith/paralyzing-terror.c");
    addResearchElement("/guilds/necromancer/wraith/extra-wraith.c");

    addChild("/guilds/necromancer/wraith/essence-drain.c",
        "/guilds/necromancer/wraith/soul-siphon.c");
    addChild("/guilds/necromancer/wraith/void-step.c",
        "/guilds/necromancer/wraith/phase-shift.c");
    addChild("/guilds/necromancer/wraith/paralyzing-terror.c",
        "/guilds/necromancer/wraith/dread-presence.c");
    addChild("/guilds/necromancer/wraith/extra-wraith.c",
        "/guilds/necromancer/wraith/shadow-haunt.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/necromancer/wraith/spirit-crush.c");
    addResearchElement("/guilds/necromancer/wraith/shadow-meld.c");
    addResearchElement("/guilds/necromancer/wraith/wail-of-despair.c");
    addResearchElement("/guilds/necromancer/wraith/haunting-aura.c");
    addResearchElement("/guilds/necromancer/wraith/shadow-step.c");

    addChild("/guilds/necromancer/wraith/spirit-crush.c",
        "/guilds/necromancer/wraith/essence-drain.c");
    addChild("/guilds/necromancer/wraith/shadow-meld.c",
        "/guilds/necromancer/wraith/void-step.c");
    addChild("/guilds/necromancer/wraith/wail-of-despair.c",
        "/guilds/necromancer/wraith/paralyzing-terror.c");
    addChild("/guilds/necromancer/wraith/haunting-aura.c",
        "/guilds/necromancer/wraith/extra-wraith.c");
    addChild("/guilds/necromancer/wraith/shadow-step.c",
        "/guilds/necromancer/wraith/phase-shift.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/necromancer/wraith/soul-rend.c");
    addResearchElement("/guilds/necromancer/wraith/void-cloak.c");
    addResearchElement("/guilds/necromancer/wraith/mind-shatter.c");
    addResearchElement("/guilds/necromancer/wraith/shadow-legion.c");
    addResearchElement("/guilds/necromancer/wraith/ethereal-armor.c");

    addChild("/guilds/necromancer/wraith/soul-rend.c",
        "/guilds/necromancer/wraith/spirit-crush.c");
    addChild("/guilds/necromancer/wraith/void-cloak.c",
        "/guilds/necromancer/wraith/shadow-meld.c");
    addChild("/guilds/necromancer/wraith/mind-shatter.c",
        "/guilds/necromancer/wraith/wail-of-despair.c");
    addChild("/guilds/necromancer/wraith/shadow-legion.c",
        "/guilds/necromancer/wraith/haunting-aura.c");
    addChild("/guilds/necromancer/wraith/ethereal-armor.c",
        "/guilds/necromancer/wraith/shadow-meld.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/necromancer/wraith/soul-destruction.c");
    addResearchElement("/guilds/necromancer/wraith/phantom-strike.c");
    addResearchElement("/guilds/necromancer/wraith/nightmare-presence.c");
    addResearchElement("/guilds/necromancer/wraith/wraith-host.c");

    addChild("/guilds/necromancer/wraith/soul-destruction.c",
        "/guilds/necromancer/wraith/soul-rend.c");
    addChild("/guilds/necromancer/wraith/phantom-strike.c",
        "/guilds/necromancer/wraith/void-cloak.c");
    addChild("/guilds/necromancer/wraith/nightmare-presence.c",
        "/guilds/necromancer/wraith/mind-shatter.c");
    addChild("/guilds/necromancer/wraith/wraith-host.c",
        "/guilds/necromancer/wraith/shadow-legion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/necromancer/wraith/annihilation-touch.c");
    addResearchElement("/guilds/necromancer/wraith/ghost-walk.c");
    addResearchElement("/guilds/necromancer/wraith/eternal-terror.c");

    addChild("/guilds/necromancer/wraith/annihilation-touch.c",
        "/guilds/necromancer/wraith/soul-destruction.c");
    addChild("/guilds/necromancer/wraith/ghost-walk.c",
        "/guilds/necromancer/wraith/phantom-strike.c");
    addChild("/guilds/necromancer/wraith/eternal-terror.c",
        "/guilds/necromancer/wraith/nightmare-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/necromancer/wraith/oblivion-touch.c");
    addResearchElement("/guilds/necromancer/wraith/shadow-sovereign.c");
    addResearchElement("/guilds/necromancer/wraith/doom-presence.c");

    addChild("/guilds/necromancer/wraith/oblivion-touch.c",
        "/guilds/necromancer/wraith/annihilation-touch.c");
    addChild("/guilds/necromancer/wraith/shadow-sovereign.c",
        "/guilds/necromancer/wraith/ghost-walk.c");
    addChild("/guilds/necromancer/wraith/doom-presence.c",
        "/guilds/necromancer/wraith/eternal-terror.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/necromancer/wraith/void-herald.c");
    addResearchElement("/guilds/necromancer/wraith/haunt-supreme.c");

    addChild("/guilds/necromancer/wraith/void-herald.c",
        "/guilds/necromancer/wraith/oblivion-touch.c");
    addChild("/guilds/necromancer/wraith/haunt-supreme.c",
        "/guilds/necromancer/wraith/shadow-sovereign.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/necromancer/wraith/lord-of-the-haunt.c");

    addChild("/guilds/necromancer/wraith/lord-of-the-haunt.c",
        "/guilds/necromancer/wraith/void-herald.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Wraith Haunt");
    Description("This research tree provides the knowledge necessary for "
        "summoning and enhancing wraith minions.");
    Source("necromancer");
    addResearchElement("/guilds/necromancer/wraith/root.c");
    TreeRoot("/guilds/necromancer/wraith/root.c");

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
}
