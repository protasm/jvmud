//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-awakening.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-vigor.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-awakening.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-vigor.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-cunning.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-cunning.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-endurance.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-endurance.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-awakening.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-mastery.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-mastery.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-attunement.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-resilience.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-attunement.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-awakening.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-resilience.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-vigor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-fury.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-fury.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-channeling.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-insight.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-channeling.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-insight.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-power.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-power.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-corruption.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-potency.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-corruption.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-endurance.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-potency.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-resilience.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-wrath.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-wrath.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-dominion.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-strike.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-dominion.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-insight.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-strike.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-lore.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-lore.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-rite.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-vigor.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-rite.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-dominion.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-vigor.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-corruption.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-power.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-power.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-clarity.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-clarity.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-vigor.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-sorcery.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-attunement.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-sorcery.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-attunement.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-rite.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-strike.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-strike.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/ascendant-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-might.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-might.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-sacrifice.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-dominion.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/ascendant-sacrifice.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-dominion.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-covenant.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-covenant.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-endurance.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-endurance.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-clarity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-mastery.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-mastery.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-mark.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-mark.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-dominion.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-lore.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-lore.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-fortitude.c");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-gift.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-fortitude.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-gift.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/high-necromancer-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-covenant.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-covenant.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-presence.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-presence.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/argloth-mark.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-chosen-form.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-chosen-form.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-transcendence.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-transcendence.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyNinthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/argloth-will.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/argloth-will.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/the-covenant-fulfilled.c");

    addChild("/guilds/disciple-of-argloth/argloth-ascendant/the-covenant-fulfilled.c",
        "/guilds/disciple-of-argloth/argloth-ascendant/root.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Argloth Ascendant");
    Description("The highest rites of Argloth's covenant, granted only to those who have achieved the rank of High Necromancer - those who carry the fullness of his power.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/argloth-ascendant/root.c");
    TreeRoot("/guilds/disciple-of-argloth/argloth-ascendant/root.c");

    FortiethLevel();
    FortyFirstLevel();
    FortySecondLevel();
    FortyThirdLevel();
    FortyFourthLevel();
    FortyFifthLevel();
    FortySixthLevel();
    FortySeventhLevel();
    FortyEighthLevel();
    FortyNinthLevel();
    FiftiethLevel();
    FiftyFirstLevel();
    FiftySecondLevel();
    FiftyThirdLevel();
    FiftyFourthLevel();
    FiftyFifthLevel();
    FiftySixthLevel();
    FiftySeventhLevel();
    FiftyEighthLevel();
    FiftyNinthLevel();
    SixtiethLevel();
    SixtyFirstLevel();
    SixtySecondLevel();
    SixtyThirdLevel();
    SixtyFourthLevel();
    SixtyFifthLevel();
    SixtySixthLevel();
    SixtySeventhLevel();
    SixtyEighthLevel();
    SixtyNinthLevel();
    SeventiethLevel();
}
