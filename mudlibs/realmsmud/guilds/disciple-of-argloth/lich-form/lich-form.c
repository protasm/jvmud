//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void SixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/bone-resonance.c");

    addChild("/guilds/disciple-of-argloth/lich-form/bone-resonance.c",
        "/guilds/disciple-of-argloth/lich-form/lich-form-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/undead-toughness.c");

    addChild("/guilds/disciple-of-argloth/lich-form/undead-toughness.c",
        "/guilds/disciple-of-argloth/lich-form/lich-form-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/chill-touch.c");

    addChild("/guilds/disciple-of-argloth/lich-form/chill-touch.c",
        "/guilds/disciple-of-argloth/lich-form/bone-resonance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/bone-lance.c");

    addChild("/guilds/disciple-of-argloth/lich-form/bone-lance.c",
        "/guilds/disciple-of-argloth/lich-form/bone-resonance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/lich-attunement.c");

    addChild("/guilds/disciple-of-argloth/lich-form/lich-attunement.c",
        "/guilds/disciple-of-argloth/lich-form/bone-resonance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/deaths-economy.c");

    addChild("/guilds/disciple-of-argloth/lich-form/deaths-economy.c",
        "/guilds/disciple-of-argloth/lich-form/lich-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/undying-resilience.c");

    addChild("/guilds/disciple-of-argloth/lich-form/undying-resilience.c",
        "/guilds/disciple-of-argloth/lich-form/undead-toughness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/death-grasp.c");

    addChild("/guilds/disciple-of-argloth/lich-form/death-grasp.c",
        "/guilds/disciple-of-argloth/lich-form/lich-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/undying-focus.c");

    addChild("/guilds/disciple-of-argloth/lich-form/undying-focus.c",
        "/guilds/disciple-of-argloth/lich-form/lich-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/deathly-insight.c");

    addChild("/guilds/disciple-of-argloth/lich-form/deathly-insight.c",
        "/guilds/disciple-of-argloth/lich-form/lich-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/deathless-cadence.c");

    addChild("/guilds/disciple-of-argloth/lich-form/deathless-cadence.c",
        "/guilds/disciple-of-argloth/lich-form/deaths-economy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/wail-of-the-dead.c");

    addChild("/guilds/disciple-of-argloth/lich-form/wail-of-the-dead.c",
        "/guilds/disciple-of-argloth/lich-form/deathly-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/lich-fortitude.c");

    addChild("/guilds/disciple-of-argloth/lich-form/lich-fortitude.c",
        "/guilds/disciple-of-argloth/lich-form/undying-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/sepulchral-mastery.c");

    addChild("/guilds/disciple-of-argloth/lich-form/sepulchral-mastery.c",
        "/guilds/disciple-of-argloth/lich-form/deathly-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/spectral-bolt.c");

    addChild("/guilds/disciple-of-argloth/lich-form/spectral-bolt.c",
        "/guilds/disciple-of-argloth/lich-form/sepulchral-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/death-immunity.c");

    addChild("/guilds/disciple-of-argloth/lich-form/death-immunity.c",
        "/guilds/disciple-of-argloth/lich-form/lich-fortitude.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/lich-conduit-knowledge.c");

    addChild("/guilds/disciple-of-argloth/lich-form/lich-conduit-knowledge.c",
        "/guilds/disciple-of-argloth/lich-form/sepulchral-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/necrotic-burst.c");

    addChild("/guilds/disciple-of-argloth/lich-form/necrotic-burst.c",
        "/guilds/disciple-of-argloth/lich-form/lich-conduit-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/phylactery-theory.c");

    addChild("/guilds/disciple-of-argloth/lich-form/phylactery-theory.c",
        "/guilds/disciple-of-argloth/lich-form/lich-conduit-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/arcane-phylactery.c");

    addChild("/guilds/disciple-of-argloth/lich-form/arcane-phylactery.c",
        "/guilds/disciple-of-argloth/lich-form/death-immunity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/soul-rend-form.c");

    addChild("/guilds/disciple-of-argloth/lich-form/soul-rend-form.c",
        "/guilds/disciple-of-argloth/lich-form/phylactery-theory.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/beyond-mortality.c");

    addChild("/guilds/disciple-of-argloth/lich-form/beyond-mortality.c",
        "/guilds/disciple-of-argloth/lich-form/arcane-phylactery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/lich-nova.c");

    addChild("/guilds/disciple-of-argloth/lich-form/lich-nova.c",
        "/guilds/disciple-of-argloth/lich-form/soul-rend-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/lich-form/argloth-lich-ascendant.c");

    addChild("/guilds/disciple-of-argloth/lich-form/argloth-lich-ascendant.c",
        "/guilds/disciple-of-argloth/lich-form/beyond-mortality.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Lich Form");
    Description("This research tree provides the knowledge to assume the Lich "
        "Form of Argloth - shedding the vulnerability of living flesh to become "
        "a conduit of death energy. Through progressive mastery, disciples gain "
        "undead resilience, dominion over bone and spirit, and eventually "
        "command the most destructive forces of death itself.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/lich-form/lich-form-root.c");
    TreeRoot("/guilds/disciple-of-argloth/lich-form/lich-form-root.c");

    SixthLevel();
    SeventhLevel();
    EighthLevel();
    TenthLevel();
    EleventhLevel();
    TwelfthLevel();
    ThirteenthLevel();
    FourteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentiethLevel();
    TwentyFirstLevel();
    TwentyThirdLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    SixtyFirstLevel();
}
