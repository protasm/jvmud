//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-grace.c");
    addChild("/guilds/drambor-edlothiad/heritage/elven-grace.c",
        "/guilds/drambor-edlothiad/heritage/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-reflexes.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-vitality.c");

    addChild("/guilds/drambor-edlothiad/heritage/elven-reflexes.c",
        "/guilds/drambor-edlothiad/heritage/elven-grace.c");
    addChild("/guilds/drambor-edlothiad/heritage/elven-vitality.c",
        "/guilds/drambor-edlothiad/heritage/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/keen-senses.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/ancient-memory.c");

    addChild("/guilds/drambor-edlothiad/heritage/keen-senses.c",
        "/guilds/drambor-edlothiad/heritage/elven-reflexes.c");
    addChild("/guilds/drambor-edlothiad/heritage/ancient-memory.c",
        "/guilds/drambor-edlothiad/heritage/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/trance-meditation.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-poise.c");

    addChild("/guilds/drambor-edlothiad/heritage/trance-meditation.c",
        "/guilds/drambor-edlothiad/heritage/elven-vitality.c");
    addChild("/guilds/drambor-edlothiad/heritage/elven-poise.c",
        "/guilds/drambor-edlothiad/heritage/elven-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-agility.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-intuition.c");

    addChild("/guilds/drambor-edlothiad/heritage/elven-agility.c",
        "/guilds/drambor-edlothiad/heritage/keen-senses.c");
    addChild("/guilds/drambor-edlothiad/heritage/elven-intuition.c",
        "/guilds/drambor-edlothiad/heritage/ancient-memory.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/timeless-wisdom.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-resilience.c");

    addChild("/guilds/drambor-edlothiad/heritage/timeless-wisdom.c",
        "/guilds/drambor-edlothiad/heritage/elven-grace.c");
    addChild("/guilds/drambor-edlothiad/heritage/elven-resilience.c",
        "/guilds/drambor-edlothiad/heritage/trance-meditation.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/swift-as-wind.c");

    addChild("/guilds/drambor-edlothiad/heritage/swift-as-wind.c",
        "/guilds/drambor-edlothiad/heritage/elven-agility.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/memory-of-ages.c");

    addChild("/guilds/drambor-edlothiad/heritage/memory-of-ages.c",
        "/guilds/drambor-edlothiad/heritage/timeless-wisdom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/preternatural-grace.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/fey-resistance.c");

    addChild("/guilds/drambor-edlothiad/heritage/preternatural-grace.c",
        "/guilds/drambor-edlothiad/heritage/swift-as-wind.c");
    addChild("/guilds/drambor-edlothiad/heritage/fey-resistance.c",
        "/guilds/drambor-edlothiad/heritage/elven-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/lorekeepers-mind.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/starlit-blood.c");

    addChild("/guilds/drambor-edlothiad/heritage/lorekeepers-mind.c",
        "/guilds/drambor-edlothiad/heritage/memory-of-ages.c");
    addChild("/guilds/drambor-edlothiad/heritage/starlit-blood.c",
        "/guilds/drambor-edlothiad/heritage/fey-resistance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/fey-ancestry.c");

    addChild("/guilds/drambor-edlothiad/heritage/fey-ancestry.c",
        "/guilds/drambor-edlothiad/heritage/timeless-wisdom.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/dance-of-shadows.c");

    addChild("/guilds/drambor-edlothiad/heritage/dance-of-shadows.c",
        "/guilds/drambor-edlothiad/heritage/preternatural-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/mind-of-the-ancients.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/spirit-of-the-wood.c");

    addChild("/guilds/drambor-edlothiad/heritage/mind-of-the-ancients.c",
        "/guilds/drambor-edlothiad/heritage/lorekeepers-mind.c");
    addChild("/guilds/drambor-edlothiad/heritage/spirit-of-the-wood.c",
        "/guilds/drambor-edlothiad/heritage/fey-ancestry.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/ancient-fortitude.c");

    addChild("/guilds/drambor-edlothiad/heritage/ancient-fortitude.c",
        "/guilds/drambor-edlothiad/heritage/spirit-of-the-wood.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/ethereal-movement.c");

    addChild("/guilds/drambor-edlothiad/heritage/ethereal-movement.c",
        "/guilds/drambor-edlothiad/heritage/dance-of-shadows.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/ageless-endurance.c");

    addChild("/guilds/drambor-edlothiad/heritage/ageless-endurance.c",
        "/guilds/drambor-edlothiad/heritage/spirit-of-the-wood.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/immortal-heritage.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/elder-knowledge.c");

    addChild("/guilds/drambor-edlothiad/heritage/immortal-heritage.c",
        "/guilds/drambor-edlothiad/heritage/fey-ancestry.c");
    addChild("/guilds/drambor-edlothiad/heritage/elder-knowledge.c",
        "/guilds/drambor-edlothiad/heritage/mind-of-the-ancients.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/elven-longevity.c");

    addChild("/guilds/drambor-edlothiad/heritage/elven-longevity.c",
        "/guilds/drambor-edlothiad/heritage/ageless-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/perfection-of-form.c");

    addChild("/guilds/drambor-edlothiad/heritage/perfection-of-form.c",
        "/guilds/drambor-edlothiad/heritage/ethereal-movement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/immortal-constitution.c");

    addChild("/guilds/drambor-edlothiad/heritage/immortal-constitution.c",
        "/guilds/drambor-edlothiad/heritage/ageless-endurance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/timeless-intellect.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/wisdom-of-trees.c");

    addChild("/guilds/drambor-edlothiad/heritage/timeless-intellect.c",
        "/guilds/drambor-edlothiad/heritage/elder-knowledge.c");
    addChild("/guilds/drambor-edlothiad/heritage/wisdom-of-trees.c",
        "/guilds/drambor-edlothiad/heritage/elder-knowledge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/undying-spirit.c");

    addChild("/guilds/drambor-edlothiad/heritage/undying-spirit.c",
        "/guilds/drambor-edlothiad/heritage/immortal-constitution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/elder-blood.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/transcendent-grace.c");

    addChild("/guilds/drambor-edlothiad/heritage/elder-blood.c",
        "/guilds/drambor-edlothiad/heritage/immortal-heritage.c");
    addChild("/guilds/drambor-edlothiad/heritage/transcendent-grace.c",
        "/guilds/drambor-edlothiad/heritage/perfection-of-form.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/blood-of-the-firstborn.c");
    addResearchElement("/guilds/drambor-edlothiad/heritage/mind-beyond-time.c");

    addChild("/guilds/drambor-edlothiad/heritage/blood-of-the-firstborn.c",
        "/guilds/drambor-edlothiad/heritage/mind-beyond-time.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addChild("/guilds/drambor-edlothiad/heritage/mind-beyond-time.c",
        "/guilds/drambor-edlothiad/heritage/timeless-intellect.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/eternal-resilience.c");

    addChild("/guilds/drambor-edlothiad/heritage/eternal-resilience.c",
        "/guilds/drambor-edlothiad/heritage/undying-spirit.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/avatar-of-grace.c");

    addChild("/guilds/drambor-edlothiad/heritage/avatar-of-grace.c",
        "/guilds/drambor-edlothiad/heritage/transcendent-grace.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/firstborn-legacy.c");

    addChild("/guilds/drambor-edlothiad/heritage/firstborn-legacy.c",
        "/guilds/drambor-edlothiad/heritage/elder-blood.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/eternal-lineage.c");

    addChild("/guilds/drambor-edlothiad/heritage/eternal-lineage.c",
        "/guilds/drambor-edlothiad/heritage/firstborn-legacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/heritage/avatar-of-the-eldar.c");

    addChild("/guilds/drambor-edlothiad/heritage/avatar-of-the-eldar.c",
        "/guilds/drambor-edlothiad/heritage/eternal-lineage.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Elven Heritage");
    Description("This research tree draws upon the ancient heritage of the elves, "
        "granting passive bonuses and abilities tied to elven nature and longevity.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/heritage/root.c");
    TreeRoot("/guilds/drambor-edlothiad/heritage/root.c");

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
    FortyThirdLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftyNinthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtySeventhLevel();
    SeventyLevel();
}
