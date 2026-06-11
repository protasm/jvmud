//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/starlight.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/elven-insight.c");
    addChild("/guilds/drambor-edlothiad/sorcery/starlight.c", "/guilds/drambor-edlothiad/sorcery/root.c");
    addChild("/guilds/drambor-edlothiad/sorcery/elven-insight.c", "/guilds/drambor-edlothiad/sorcery/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/elven-ward.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/ancient-knowledge.c");
    addChild("/guilds/drambor-edlothiad/sorcery/elven-ward.c", "/guilds/drambor-edlothiad/sorcery/root.c");
    addChild("/guilds/drambor-edlothiad/sorcery/ancient-knowledge.c", "/guilds/drambor-edlothiad/sorcery/elven-insight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/moonbeam.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/spell-reservoir.c");
    addChild("/guilds/drambor-edlothiad/sorcery/moonbeam.c", "/guilds/drambor-edlothiad/sorcery/starlight.c");
    addChild("/guilds/drambor-edlothiad/sorcery/spell-reservoir.c", "/guilds/drambor-edlothiad/sorcery/ancient-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/mana-sight.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/moonlight-veil.c");
    addChild("/guilds/drambor-edlothiad/sorcery/mana-sight.c", "/guilds/drambor-edlothiad/sorcery/ancient-knowledge.c");
    addChild("/guilds/drambor-edlothiad/sorcery/moonlight-veil.c", "/guilds/drambor-edlothiad/sorcery/elven-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/moonfire.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/elven-meditation.c");
    addChild("/guilds/drambor-edlothiad/sorcery/moonfire.c", "/guilds/drambor-edlothiad/sorcery/moonbeam.c");
    addChild("/guilds/drambor-edlothiad/sorcery/elven-meditation.c", "/guilds/drambor-edlothiad/sorcery/spell-reservoir.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/greater-ward.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/sorcery-amplification.c");
    addChild("/guilds/drambor-edlothiad/sorcery/greater-ward.c", "/guilds/drambor-edlothiad/sorcery/elven-ward.c");
    addChild("/guilds/drambor-edlothiad/sorcery/sorcery-amplification.c", "/guilds/drambor-edlothiad/sorcery/mana-sight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/arcane-lore.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/star-shield.c");
    addChild("/guilds/drambor-edlothiad/sorcery/arcane-lore.c", "/guilds/drambor-edlothiad/sorcery/mana-sight.c");
    addChild("/guilds/drambor-edlothiad/sorcery/star-shield.c", "/guilds/drambor-edlothiad/sorcery/moonlight-veil.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/eldritch-star.c");
    addChild("/guilds/drambor-edlothiad/sorcery/eldritch-star.c", "/guilds/drambor-edlothiad/sorcery/moonfire.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/timeless-ward.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/deep-knowledge.c");
    addChild("/guilds/drambor-edlothiad/sorcery/timeless-ward.c", "/guilds/drambor-edlothiad/sorcery/greater-ward.c");
    addChild("/guilds/drambor-edlothiad/sorcery/deep-knowledge.c", "/guilds/drambor-edlothiad/sorcery/arcane-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/ancient-power.c");
    addChild("/guilds/drambor-edlothiad/sorcery/ancient-power.c", "/guilds/drambor-edlothiad/sorcery/eldritch-star.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/sorcery-mastery.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/ethereal-sight.c");
    addChild("/guilds/drambor-edlothiad/sorcery/sorcery-mastery.c", "/guilds/drambor-edlothiad/sorcery/sorcery-amplification.c");
    addChild("/guilds/drambor-edlothiad/sorcery/ethereal-sight.c", "/guilds/drambor-edlothiad/sorcery/deep-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/profound-understanding.c");
    addChild("/guilds/drambor-edlothiad/sorcery/profound-understanding.c", "/guilds/drambor-edlothiad/sorcery/deep-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/celestial-lance.c");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/eternal-ward.c");
    addChild("/guilds/drambor-edlothiad/sorcery/celestial-lance.c", "/guilds/drambor-edlothiad/sorcery/ancient-power.c");
    addChild("/guilds/drambor-edlothiad/sorcery/eternal-ward.c", "/guilds/drambor-edlothiad/sorcery/timeless-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/timeless-magic.c");
    addChild("/guilds/drambor-edlothiad/sorcery/timeless-magic.c", "/guilds/drambor-edlothiad/sorcery/ethereal-sight.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/elven-resilience.c");
    addChild("/guilds/drambor-edlothiad/sorcery/elven-resilience.c", "/guilds/drambor-edlothiad/sorcery/timeless-magic.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/sorcery-supremacy.c");
    addChild("/guilds/drambor-edlothiad/sorcery/sorcery-supremacy.c", "/guilds/drambor-edlothiad/sorcery/sorcery-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/elder-knowledge.c");
    addChild("/guilds/drambor-edlothiad/sorcery/elder-knowledge.c", "/guilds/drambor-edlothiad/sorcery/timeless-magic.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/elder-star.c");
    addChild("/guilds/drambor-edlothiad/sorcery/elder-star.c", "/guilds/drambor-edlothiad/sorcery/celestial-lance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/supreme-ward.c");
    addChild("/guilds/drambor-edlothiad/sorcery/supreme-ward.c", "/guilds/drambor-edlothiad/sorcery/eternal-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/elder-magic.c");
    addChild("/guilds/drambor-edlothiad/sorcery/elder-magic.c", "/guilds/drambor-edlothiad/sorcery/elder-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/astral-mastery.c");
    addChild("/guilds/drambor-edlothiad/sorcery/astral-mastery.c", "/guilds/drambor-edlothiad/sorcery/elder-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/primordial-light.c");
    addChild("/guilds/drambor-edlothiad/sorcery/primordial-light.c", "/guilds/drambor-edlothiad/sorcery/elder-star.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/transcendent-knowledge.c");
    addChild("/guilds/drambor-edlothiad/sorcery/transcendent-knowledge.c", "/guilds/drambor-edlothiad/sorcery/elder-magic.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/ward-of-ages.c");
    addChild("/guilds/drambor-edlothiad/sorcery/ward-of-ages.c", "/guilds/drambor-edlothiad/sorcery/supreme-ward.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/sorcery-transcendence.c");
    addChild("/guilds/drambor-edlothiad/sorcery/sorcery-transcendence.c", "/guilds/drambor-edlothiad/sorcery/sorcery-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/cosmic-awareness.c");
    addChild("/guilds/drambor-edlothiad/sorcery/cosmic-awareness.c", "/guilds/drambor-edlothiad/sorcery/astral-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/immortal-knowledge.c");
    addChild("/guilds/drambor-edlothiad/sorcery/immortal-knowledge.c", "/guilds/drambor-edlothiad/sorcery/transcendent-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/star-of-creation.c");
    addChild("/guilds/drambor-edlothiad/sorcery/star-of-creation.c", "/guilds/drambor-edlothiad/sorcery/primordial-light.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/sorcery/eternal-wisdom.c");
    addChild("/guilds/drambor-edlothiad/sorcery/eternal-wisdom.c", "/guilds/drambor-edlothiad/sorcery/immortal-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Elven Sorcery");
    Description("This research tree provides ancient elven magical techniques, "
        "refined over millennia. From starlight and moonbeams to the "
        "primordial Star of Creation, this tree embodies the deepest "
        "magical traditions of elvenkind.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/sorcery/root.c");
    TreeRoot("/guilds/drambor-edlothiad/sorcery/root.c");

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
    TwentyThirdLevel();
    TwentyFifthLevel();
    TwentySeventhLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    ThirtyNinthLevel();
    FortyFirstLevel();
    FortyThirdLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftyNinthLevel();
    SixtyThirdLevel();
}
