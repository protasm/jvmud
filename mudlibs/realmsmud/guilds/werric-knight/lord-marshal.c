//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/strategic-command.c");
    addChild("/guilds/werric-knight/marshal/strategic-command.c",
        "/guilds/werric-knight/marshal/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/marshals-presence.c");
    addChild("/guilds/werric-knight/marshal/marshals-presence.c",
        "/guilds/werric-knight/marshal/strategic-command.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/grand-strategy.c");
    addChild("/guilds/werric-knight/marshal/grand-strategy.c",
        "/guilds/werric-knight/marshal/marshals-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/legendary-commander.c");
    addChild("/guilds/werric-knight/marshal/legendary-commander.c",
        "/guilds/werric-knight/marshal/grand-strategy.c");

    addResearchElement("/guilds/werric-knight/marshal/rally-armies.c");
    addChild("/guilds/werric-knight/marshal/rally-armies.c",
        "/guilds/werric-knight/marshal/legendary-commander.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/supreme-leadership.c");
    addChild("/guilds/werric-knight/marshal/supreme-leadership.c",
        "/guilds/werric-knight/marshal/rally-armies.c");

    addResearchElement("/guilds/werric-knight/marshal/master-of-war.c");
    addChild("/guilds/werric-knight/marshal/master-of-war.c",
        "/guilds/werric-knight/marshal/supreme-leadership.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/invincible-army.c");
    addChild("/guilds/werric-knight/marshal/invincible-army.c",
        "/guilds/werric-knight/marshal/master-of-war.c");

    addResearchElement("/guilds/werric-knight/marshal/werras-legacy-marshal.c");
    addChild("/guilds/werric-knight/marshal/werras-legacy-marshal.c",
        "/guilds/werric-knight/marshal/invincible-army.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/commanding-aura.c");
    addChild("/guilds/werric-knight/marshal/commanding-aura.c",
        "/guilds/werric-knight/marshal/werras-legacy-marshal.c");

    addResearchElement("/guilds/werric-knight/marshal/victorys-call.c");
    addChild("/guilds/werric-knight/marshal/victorys-call.c",
        "/guilds/werric-knight/marshal/commanding-aura.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/werric-knight/marshal/eternal-marshal.c");
    addChild("/guilds/werric-knight/marshal/eternal-marshal.c",
        "/guilds/werric-knight/marshal/victorys-call.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Lord Marshal's Command");
    Description("This research tree is available only to Lord Marshals - those who "
        "have achieved the highest rank within the Werric Knights. It represents "
        "the pinnacle of military leadership and strategic command.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/marshal/root.c");
    TreeRoot("/guilds/werric-knight/marshal/root.c");

    ThirtyFifthLevel();
    FortiethLevel();
    FortyFifthLevel();
    FiftiethLevel();
    FiftyFifthLevel();
    SixtiethLevel();
    SixtyFifthLevel();
    SeventiethLevel();
}
