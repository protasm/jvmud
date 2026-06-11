//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/keen-awareness.c");
    addResearchElement("/guilds/phaedra/stewardship/silver-tongue.c");
    addResearchElement("/guilds/phaedra/stewardship/diligent-study.c");

    addChild("/guilds/phaedra/stewardship/keen-awareness.c",
        "/guilds/phaedra/stewardship/stewardship-root.c");
    addChild("/guilds/phaedra/stewardship/silver-tongue.c",
        "/guilds/phaedra/stewardship/stewardship-root.c");
    addChild("/guilds/phaedra/stewardship/diligent-study.c",
        "/guilds/phaedra/stewardship/stewardship-root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/stewards-eye.c");
    addResearchElement("/guilds/phaedra/stewardship/voice-of-authority.c");

    addChild("/guilds/phaedra/stewardship/stewards-eye.c",
        "/guilds/phaedra/stewardship/keen-awareness.c");
    addChild("/guilds/phaedra/stewardship/voice-of-authority.c",
        "/guilds/phaedra/stewardship/silver-tongue.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/voice-of-phaedriel.c");
    addResearchElement("/guilds/phaedra/stewardship/scholars-acumen.c");

    addChild("/guilds/phaedra/stewardship/voice-of-phaedriel.c",
        "/guilds/phaedra/stewardship/voice-of-authority.c");
    addChild("/guilds/phaedra/stewardship/scholars-acumen.c",
        "/guilds/phaedra/stewardship/diligent-study.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/piercing-insight.c");
    addResearchElement("/guilds/phaedra/stewardship/diplomatic-grace.c");
    addResearchElement("/guilds/phaedra/stewardship/word-of-restoration.c");

    addChild("/guilds/phaedra/stewardship/piercing-insight.c",
        "/guilds/phaedra/stewardship/stewards-eye.c");
    addChild("/guilds/phaedra/stewardship/diplomatic-grace.c",
        "/guilds/phaedra/stewardship/voice-of-phaedriel.c");
    addChild("/guilds/phaedra/stewardship/word-of-restoration.c",
        "/guilds/phaedra/stewardship/scholars-acumen.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/calm-judgment.c");
    addResearchElement("/guilds/phaedra/stewardship/commanding-presence.c");
    addResearchElement("/guilds/phaedra/stewardship/restorative-presence.c");

    addChild("/guilds/phaedra/stewardship/calm-judgment.c",
        "/guilds/phaedra/stewardship/piercing-insight.c");
    addChild("/guilds/phaedra/stewardship/commanding-presence.c",
        "/guilds/phaedra/stewardship/diplomatic-grace.c");
    addChild("/guilds/phaedra/stewardship/restorative-presence.c",
        "/guilds/phaedra/stewardship/word-of-restoration.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/rallying-word.c");

    addChild("/guilds/phaedra/stewardship/rallying-word.c",
        "/guilds/phaedra/stewardship/commanding-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/tactical-clarity.c");
    addResearchElement("/guilds/phaedra/stewardship/sustaining-light.c");

    addChild("/guilds/phaedra/stewardship/tactical-clarity.c",
        "/guilds/phaedra/stewardship/calm-judgment.c");
    addChild("/guilds/phaedra/stewardship/sustaining-light.c",
        "/guilds/phaedra/stewardship/restorative-presence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/wardens-lore.c");
    addResearchElement("/guilds/phaedra/stewardship/trusted-counsel.c");

    addChild("/guilds/phaedra/stewardship/wardens-lore.c",
        "/guilds/phaedra/stewardship/tactical-clarity.c");
    addChild("/guilds/phaedra/stewardship/trusted-counsel.c",
        "/guilds/phaedra/stewardship/rallying-word.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/light-of-clarity.c");

    addChild("/guilds/phaedra/stewardship/light-of-clarity.c",
        "/guilds/phaedra/stewardship/piercing-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/good-word.c");
    addResearchElement("/guilds/phaedra/stewardship/orders-wisdom.c");

    addChild("/guilds/phaedra/stewardship/good-word.c",
        "/guilds/phaedra/stewardship/sustaining-light.c");
    addChild("/guilds/phaedra/stewardship/orders-wisdom.c",
        "/guilds/phaedra/stewardship/wardens-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/stewards-resolve.c");
    addResearchElement("/guilds/phaedra/stewardship/keeper-of-traditions.c");
    addResearchElement("/guilds/phaedra/stewardship/shield-of-counsel.c");

    addChild("/guilds/phaedra/stewardship/stewards-resolve.c",
        "/guilds/phaedra/stewardship/trusted-counsel.c");
    addChild("/guilds/phaedra/stewardship/keeper-of-traditions.c",
        "/guilds/phaedra/stewardship/wardens-lore.c");
    addChild("/guilds/phaedra/stewardship/shield-of-counsel.c",
        "/guilds/phaedra/stewardship/trusted-counsel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/light-of-the-steward.c");

    addChild("/guilds/phaedra/stewardship/light-of-the-steward.c",
        "/guilds/phaedra/stewardship/good-word.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/exemplar-of-service.c");

    addChild("/guilds/phaedra/stewardship/exemplar-of-service.c",
        "/guilds/phaedra/stewardship/stewards-resolve.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/master-of-traditions.c");
    addResearchElement("/guilds/phaedra/stewardship/sages-eye.c");
    addResearchElement("/guilds/phaedra/stewardship/exemplars-authority.c");

    addChild("/guilds/phaedra/stewardship/master-of-traditions.c",
        "/guilds/phaedra/stewardship/keeper-of-traditions.c");
    addChild("/guilds/phaedra/stewardship/sages-eye.c",
        "/guilds/phaedra/stewardship/light-of-clarity.c");
    addChild("/guilds/phaedra/stewardship/exemplars-authority.c",
        "/guilds/phaedra/stewardship/exemplar-of-service.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/voice-of-command.c");
    addResearchElement("/guilds/phaedra/stewardship/unassailable-judgment.c");

    addChild("/guilds/phaedra/stewardship/voice-of-command.c",
        "/guilds/phaedra/stewardship/master-of-traditions.c");
    addChild("/guilds/phaedra/stewardship/unassailable-judgment.c",
        "/guilds/phaedra/stewardship/sages-eye.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/living-legacy.c");
    addResearchElement("/guilds/phaedra/stewardship/oracles-sight.c");
    addResearchElement("/guilds/phaedra/stewardship/sovereign-counsel.c");

    addChild("/guilds/phaedra/stewardship/living-legacy.c",
        "/guilds/phaedra/stewardship/light-of-the-steward.c");
    addChild("/guilds/phaedra/stewardship/oracles-sight.c",
        "/guilds/phaedra/stewardship/unassailable-judgment.c");
    addChild("/guilds/phaedra/stewardship/sovereign-counsel.c",
        "/guilds/phaedra/stewardship/voice-of-command.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/radiant-steward.c");

    addChild("/guilds/phaedra/stewardship/radiant-steward.c",
        "/guilds/phaedra/stewardship/living-legacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/grand-steward.c");

    addChild("/guilds/phaedra/stewardship/grand-steward.c",
        "/guilds/phaedra/stewardship/radiant-steward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/enduring-legacy.c");
    addResearchElement("/guilds/phaedra/stewardship/stewardship-supreme.c");

    addChild("/guilds/phaedra/stewardship/enduring-legacy.c",
        "/guilds/phaedra/stewardship/grand-steward.c");
    addChild("/guilds/phaedra/stewardship/stewardship-supreme.c",
        "/guilds/phaedra/stewardship/sovereign-counsel.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/steward-eternal.c");
    addResearchElement("/guilds/phaedra/stewardship/voice-eternal.c");

    addChild("/guilds/phaedra/stewardship/steward-eternal.c",
        "/guilds/phaedra/stewardship/enduring-legacy.c");
    addChild("/guilds/phaedra/stewardship/voice-eternal.c",
        "/guilds/phaedra/stewardship/stewardship-supreme.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/supreme-steward.c");

    addChild("/guilds/phaedra/stewardship/supreme-steward.c",
        "/guilds/phaedra/stewardship/steward-eternal.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/loremaster.c");

    addChild("/guilds/phaedra/stewardship/loremaster.c",
        "/guilds/phaedra/stewardship/supreme-steward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/voice-of-ages.c");

    addChild("/guilds/phaedra/stewardship/voice-of-ages.c",
        "/guilds/phaedra/stewardship/loremaster.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/phaedra/stewardship/stewardship-eternal.c");

    addChild("/guilds/phaedra/stewardship/stewardship-eternal.c",
        "/guilds/phaedra/stewardship/voice-of-ages.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Stewardship");
    Description("The third pillar of the Order of Phaedra: to preserve "
        "what has been entrusted, to see clearly, to speak with "
        "authority, and to ensure the realm endures.");
    Source("Order of Phaedra");
    addResearchElement("/guilds/phaedra/stewardship/stewardship-root.c");
    TreeRoot("/guilds/phaedra/stewardship/stewardship-root.c");

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
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtyNinthLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SixtyNinthLevel();
}
