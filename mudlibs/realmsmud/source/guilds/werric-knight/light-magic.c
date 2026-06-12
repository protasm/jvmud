//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/light/divine-connection.c");
    addChild("/guilds/werric-knight/light/divine-connection.c",
        "/guilds/werric-knight/light/root.c");

    addResearchElement("/guilds/werric-knight/light/holy-light.c");
    addChild("/guilds/werric-knight/light/holy-light.c",
        "/guilds/werric-knight/light/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/light/minor-heal.c");
    addChild("/guilds/werric-knight/light/minor-heal.c",
        "/guilds/werric-knight/light/divine-connection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/light/smite-evil.c");
    addChild("/guilds/werric-knight/light/smite-evil.c",
        "/guilds/werric-knight/light/minor-heal.c");

    addResearchElement("/guilds/werric-knight/light/radiant-burst.c");
    addChild("/guilds/werric-knight/light/radiant-burst.c",
        "/guilds/werric-knight/light/holy-light.c");

    addResearchElement("/guilds/werric-knight/light/smite.c");
    addChild("/guilds/werric-knight/light/smite.c",
        "/guilds/werric-knight/light/radiant-burst.c");

    addResearchElement("/guilds/werric-knight/light/smiting-technique.c");
    addChild("/guilds/werric-knight/light/smiting-technique.c",
        "/guilds/werric-knight/light/smite-evil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/light/divine-protection.c");
    addChild("/guilds/werric-knight/light/divine-protection.c",
        "/guilds/werric-knight/light/smite-evil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/light/blessing-of-light.c");
    addChild("/guilds/werric-knight/light/blessing-of-light.c",
        "/guilds/werric-knight/light/divine-protection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/light/healing-touch.c");
    addChild("/guilds/werric-knight/light/healing-touch.c",
        "/guilds/werric-knight/light/blessing-of-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/light/holy-strike.c");
    addChild("/guilds/werric-knight/light/holy-strike.c",
        "/guilds/werric-knight/light/healing-touch.c");

    addResearchElement("/guilds/werric-knight/light/righteous-strike.c");
    addChild("/guilds/werric-knight/light/righteous-strike.c",
        "/guilds/werric-knight/light/holy-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/light/aura-of-courage.c");
    addChild("/guilds/werric-knight/light/aura-of-courage.c",
        "/guilds/werric-knight/light/holy-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/light/channel-divinity.c");
    addChild("/guilds/werric-knight/light/channel-divinity.c",
        "/guilds/werric-knight/light/aura-of-courage.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/werric-knight/light/divine-wrath.c");
    addChild("/guilds/werric-knight/light/divine-wrath.c",
        "/guilds/werric-knight/light/channel-divinity.c");

    addResearchElement("/guilds/werric-knight/light/holy-wrath.c");
    addChild("/guilds/werric-knight/light/holy-wrath.c",
        "/guilds/werric-knight/light/divine-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/light/greater-heal.c");
    addChild("/guilds/werric-knight/light/greater-heal.c",
        "/guilds/werric-knight/light/divine-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/light/aura-of-protection.c");
    addChild("/guilds/werric-knight/light/aura-of-protection.c",
        "/guilds/werric-knight/light/greater-heal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/werric-knight/light/radiant-power.c");
    addChild("/guilds/werric-knight/light/radiant-power.c",
        "/guilds/werric-knight/light/aura-of-protection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/werric-knight/light/smite-wicked.c");
    addChild("/guilds/werric-knight/light/smite-wicked.c",
        "/guilds/werric-knight/light/radiant-power.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/light/mass-healing.c");
    addChild("/guilds/werric-knight/light/mass-healing.c",
        "/guilds/werric-knight/light/smite-wicked.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/light/holy-radiance.c");
    addChild("/guilds/werric-knight/light/holy-radiance.c",
        "/guilds/werric-knight/light/mass-healing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/werric-knight/light/divine-mastery.c");
    addChild("/guilds/werric-knight/light/divine-mastery.c",
        "/guilds/werric-knight/light/holy-radiance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/light/pillar-of-light.c");
    addChild("/guilds/werric-knight/light/pillar-of-light.c",
        "/guilds/werric-knight/light/divine-mastery.c");

    addResearchElement("/guilds/werric-knight/light/ultimate-judgment.c");
    addChild("/guilds/werric-knight/light/ultimate-judgment.c",
        "/guilds/werric-knight/light/pillar-of-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/werric-knight/light/divine-resurrection.c");
    addChild("/guilds/werric-knight/light/divine-resurrection.c",
        "/guilds/werric-knight/light/pillar-of-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/light/sanctified-champion.c");
    addChild("/guilds/werric-knight/light/sanctified-champion.c",
        "/guilds/werric-knight/light/divine-resurrection.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/werric-knight/light/divine-judgment.c");
    addChild("/guilds/werric-knight/light/divine-judgment.c",
        "/guilds/werric-knight/light/sanctified-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/werric-knight/light/smite-damned.c");
    addChild("/guilds/werric-knight/light/smite-damned.c",
        "/guilds/werric-knight/light/divine-judgment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/werric-knight/light/transcendent-light.c");
    addChild("/guilds/werric-knight/light/transcendent-light.c",
        "/guilds/werric-knight/light/smite-damned.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySixthLevel()
{
    addResearchElement("/guilds/werric-knight/light/avatar-of-light.c");
    addChild("/guilds/werric-knight/light/avatar-of-light.c",
        "/guilds/werric-knight/light/transcendent-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/werric-knight/light/eternal-light.c");
    addChild("/guilds/werric-knight/light/eternal-light.c",
        "/guilds/werric-knight/light/avatar-of-light.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Light Magic");
    Description("This research tree represents the holy powers granted to those "
        "who serve the Oath with absolute dedication. Light Magic emphasizes "
        "protection, healing, and smiting evil - the divine blessing upon those "
        "who defend the innocent.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/light/root.c");
    TreeRoot("/guilds/werric-knight/light/root.c");

    FirstLevel();
    ThirdLevel();
    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    TwentiethLevel();
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentyEighthLevel();
    ThirtiethLevel();
    ThirtyThirdLevel();
    ThirtySeventhLevel();
    FortiethLevel();
    FortyFifthLevel();
    FiftiethLevel();
    FiftyThirdLevel();
    FiftySeventhLevel();
    SixtiethLevel();
    SixtyThirdLevel();
    SixtySixthLevel();
    SeventiethLevel();
}
