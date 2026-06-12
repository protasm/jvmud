//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/phaedra/justice/lawkeepers-eye.c");
    addResearchElement("/guilds/phaedra/justice/commanding-will.c");

    addChild("/guilds/phaedra/justice/lawkeepers-eye.c",
        "/guilds/phaedra/justice/justice-root.c");
    addChild("/guilds/phaedra/justice/commanding-will.c",
        "/guilds/phaedra/justice/justice-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/phaedra/justice/mark-of-censure.c");
    addResearchElement("/guilds/phaedra/justice/oathbound-authority.c");

    addChild("/guilds/phaedra/justice/mark-of-censure.c",
        "/guilds/phaedra/justice/lawkeepers-eye.c");
    addChild("/guilds/phaedra/justice/oathbound-authority.c",
        "/guilds/phaedra/justice/commanding-will.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/phaedra/justice/censuring-strike.c");
    addResearchElement("/guilds/phaedra/justice/judicial-bearing.c");

    addChild("/guilds/phaedra/justice/censuring-strike.c",
        "/guilds/phaedra/justice/mark-of-censure.c");
    addChild("/guilds/phaedra/justice/judicial-bearing.c",
        "/guilds/phaedra/justice/oathbound-authority.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/phaedra/justice/decree-of-weakness.c");
    addResearchElement("/guilds/phaedra/justice/justices-edge.c");

    addChild("/guilds/phaedra/justice/decree-of-weakness.c",
        "/guilds/phaedra/justice/mark-of-censure.c");
    addChild("/guilds/phaedra/justice/justices-edge.c",
        "/guilds/phaedra/justice/censuring-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/phaedra/justice/word-of-binding.c");
    addResearchElement("/guilds/phaedra/justice/gavel-of-the-crown.c");

    addChild("/guilds/phaedra/justice/word-of-binding.c",
        "/guilds/phaedra/justice/censuring-strike.c");
    addChild("/guilds/phaedra/justice/gavel-of-the-crown.c",
        "/guilds/phaedra/justice/judicial-bearing.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/phaedra/justice/crowns-mandate-active.c");

    addChild("/guilds/phaedra/justice/crowns-mandate-active.c",
        "/guilds/phaedra/justice/gavel-of-the-crown.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/phaedra/justice/judgment-of-phaedra.c");
    addResearchElement("/guilds/phaedra/justice/measured-sentence.c");

    addChild("/guilds/phaedra/justice/judgment-of-phaedra.c",
        "/guilds/phaedra/justice/word-of-binding.c");
    addChild("/guilds/phaedra/justice/measured-sentence.c",
        "/guilds/phaedra/justice/decree-of-weakness.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/phaedra/justice/stern-justice.c");

    addChild("/guilds/phaedra/justice/stern-justice.c",
        "/guilds/phaedra/justice/gavel-of-the-crown.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/phaedra/justice/righteous-verdict.c");
    addResearchElement("/guilds/phaedra/justice/lawkeepers-insight.c");

    addChild("/guilds/phaedra/justice/righteous-verdict.c",
        "/guilds/phaedra/justice/judgment-of-phaedra.c");
    addChild("/guilds/phaedra/justice/lawkeepers-insight.c",
        "/guilds/phaedra/justice/lawkeepers-eye.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/phaedra/justice/voice-of-the-law.c");
    addResearchElement("/guilds/phaedra/justice/orders-authority.c");
    addResearchElement("/guilds/phaedra/justice/iron-verdict.c");

    addChild("/guilds/phaedra/justice/voice-of-the-law.c",
        "/guilds/phaedra/justice/stern-justice.c");
    addChild("/guilds/phaedra/justice/orders-authority.c",
        "/guilds/phaedra/justice/oathbound-authority.c");
    addChild("/guilds/phaedra/justice/iron-verdict.c",
        "/guilds/phaedra/justice/stern-justice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/phaedra/justice/absolute-authority.c");
    addResearchElement("/guilds/phaedra/justice/judicial-strike.c");
    addResearchElement("/guilds/phaedra/justice/binding-word.c");

    addChild("/guilds/phaedra/justice/absolute-authority.c",
        "/guilds/phaedra/justice/righteous-verdict.c");
    addChild("/guilds/phaedra/justice/judicial-strike.c",
        "/guilds/phaedra/justice/justices-edge.c");
    addChild("/guilds/phaedra/justice/binding-word.c",
        "/guilds/phaedra/justice/word-of-binding.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/justice/condemning-light.c");
    addResearchElement("/guilds/phaedra/justice/crowns-bearing.c");
    addResearchElement("/guilds/phaedra/justice/sentence-mastery.c");

    addChild("/guilds/phaedra/justice/condemning-light.c",
        "/guilds/phaedra/justice/righteous-verdict.c");
    addChild("/guilds/phaedra/justice/crowns-bearing.c",
        "/guilds/phaedra/justice/crowns-mandate-active.c");
    addChild("/guilds/phaedra/justice/sentence-mastery.c",
        "/guilds/phaedra/justice/measured-sentence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/justice/radiant-justice.c");
    addResearchElement("/guilds/phaedra/justice/laws-edge.c");
    addResearchElement("/guilds/phaedra/justice/condemning-radiance.c");

    addChild("/guilds/phaedra/justice/radiant-justice.c",
        "/guilds/phaedra/justice/righteous-verdict.c");
    addChild("/guilds/phaedra/justice/laws-edge.c",
        "/guilds/phaedra/justice/voice-of-the-law.c");
    addChild("/guilds/phaedra/justice/condemning-radiance.c",
        "/guilds/phaedra/justice/condemning-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/justice/authority-absolute.c");
    addResearchElement("/guilds/phaedra/justice/laws-champion.c");

    addChild("/guilds/phaedra/justice/authority-absolute.c",
        "/guilds/phaedra/justice/absolute-authority.c");
    addChild("/guilds/phaedra/justice/laws-champion.c",
        "/guilds/phaedra/justice/condemning-radiance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/phaedra/justice/imperishable-law.c");

    addChild("/guilds/phaedra/justice/imperishable-law.c",
        "/guilds/phaedra/justice/laws-champion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/phaedra/justice/final-judgment.c");

    addChild("/guilds/phaedra/justice/final-judgment.c",
        "/guilds/phaedra/justice/condemning-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/phaedra/justice/eternal-justice.c");

    addChild("/guilds/phaedra/justice/eternal-justice.c",
        "/guilds/phaedra/justice/final-judgment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/phaedra/justice/crowns-wrath.c");

    addChild("/guilds/phaedra/justice/crowns-wrath.c",
        "/guilds/phaedra/justice/eternal-justice.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/phaedra/justice/sovereigns-gaze.c");
    addResearchElement("/guilds/phaedra/justice/voice-of-judgment.c");

    addChild("/guilds/phaedra/justice/sovereigns-gaze.c",
        "/guilds/phaedra/justice/imperishable-law.c");
    addChild("/guilds/phaedra/justice/voice-of-judgment.c",
        "/guilds/phaedra/justice/crowns-wrath.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/phaedra/justice/justice-eternal.c");
    addResearchElement("/guilds/phaedra/justice/unbroken-mandate.c");

    addChild("/guilds/phaedra/justice/justice-eternal.c",
        "/guilds/phaedra/justice/sovereigns-gaze.c");
    addChild("/guilds/phaedra/justice/unbroken-mandate.c",
        "/guilds/phaedra/justice/voice-of-judgment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/justice/justice-supreme.c");
    addResearchElement("/guilds/phaedra/justice/mandate-ascendant.c");

    addChild("/guilds/phaedra/justice/justice-supreme.c",
        "/guilds/phaedra/justice/justice-eternal.c");
    addChild("/guilds/phaedra/justice/mandate-ascendant.c",
        "/guilds/phaedra/justice/unbroken-mandate.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/phaedra/justice/iron-mandate.c");

    addChild("/guilds/phaedra/justice/iron-mandate.c",
        "/guilds/phaedra/justice/justice-supreme.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/justice/justice-incarnate.c");

    addChild("/guilds/phaedra/justice/justice-incarnate.c",
        "/guilds/phaedra/justice/iron-mandate.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Justice");
    Description("The fourth pillar of the Order of Phaedra: to uphold "
        "the law of the realm, to mark wrongdoers, to command with "
        "authority, and to ensure that order is preserved.");
    Source("Order of Phaedra");
    addResearchElement("/guilds/phaedra/justice/justice-root.c");
    TreeRoot("/guilds/phaedra/justice/justice-root.c");

    FifthLevel();
    SeventhLevel();
    NinthLevel();
    EleventhLevel();
    ThirteenthLevel();
    FifteenthLevel();
    SeventeenthLevel();
    NineteenthLevel();
    TwentyFirstLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtyNinthLevel();
    FortyThirdLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    SixtyFirstLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
