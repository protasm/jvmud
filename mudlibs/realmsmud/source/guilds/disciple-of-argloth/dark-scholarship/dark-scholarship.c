//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/blood-theory.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/death-theory.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/forbidden-lore.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/blood-theory.c",
        "/guilds/disciple-of-argloth/dark-scholarship/forbidden-lore.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/forbidden-lore.c",
        "/guilds/disciple-of-argloth/dark-scholarship/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/argloths-writings.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/undead-lore.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/argloths-writings.c",
        "/guilds/disciple-of-argloth/dark-scholarship/root.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/undead-lore.c",
        "/guilds/disciple-of-argloth/dark-scholarship/root.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/death-theory.c",
        "/guilds/disciple-of-argloth/dark-scholarship/argloths-writings.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/mirost-codex.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/mirost-codex.c",
        "/guilds/disciple-of-argloth/dark-scholarship/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/zhardeg-treatise.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/zhardeg-treatise.c",
        "/guilds/disciple-of-argloth/dark-scholarship/forbidden-lore.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/ruin-age-history.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/sylthasis-mysteries.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/ruin-age-history.c",
        "/guilds/disciple-of-argloth/dark-scholarship/root.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/sylthasis-mysteries.c",
        "/guilds/disciple-of-argloth/dark-scholarship/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/necrotic-theory.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/sanguine-theory.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/necrotic-theory.c",
        "/guilds/disciple-of-argloth/dark-scholarship/death-theory.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/sanguine-theory.c",
        "/guilds/disciple-of-argloth/dark-scholarship/blood-theory.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwelfthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/soul-mechanics.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/soul-mechanics.c",
        "/guilds/disciple-of-argloth/dark-scholarship/zhardeg-treatise.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FourteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/corruption-theory.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/covenant-secrets.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/corruption-theory.c",
        "/guilds/disciple-of-argloth/dark-scholarship/sanguine-theory.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/covenant-secrets.c",
        "/guilds/disciple-of-argloth/dark-scholarship/necrotic-theory.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/undead-anatomy.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/undead-anatomy.c",
        "/guilds/disciple-of-argloth/dark-scholarship/soul-mechanics.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EighteenthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/blood-scripture.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/death-scripture.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/blood-scripture.c",
        "/guilds/disciple-of-argloth/dark-scholarship/corruption-theory.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/death-scripture.c",
        "/guilds/disciple-of-argloth/dark-scholarship/covenant-secrets.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/argloth-chronicle.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/argloth-chronicle.c",
        "/guilds/disciple-of-argloth/dark-scholarship/undead-anatomy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/arcane-corruption.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/dark-philosophy.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/arcane-corruption.c",
        "/guilds/disciple-of-argloth/dark-scholarship/death-scripture.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/dark-philosophy.c",
        "/guilds/disciple-of-argloth/dark-scholarship/blood-scripture.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/cooldown-mastery.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/spell-efficiency.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/cooldown-mastery.c",
        "/guilds/disciple-of-argloth/dark-scholarship/arcane-corruption.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/spell-efficiency.c",
        "/guilds/disciple-of-argloth/dark-scholarship/dark-philosophy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/forbidden-anatomy.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/forbidden-anatomy.c",
        "/guilds/disciple-of-argloth/dark-scholarship/argloth-chronicle.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/mana-efficiency.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/mana-efficiency.c",
        "/guilds/disciple-of-argloth/dark-scholarship/spell-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/ancient-rites.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/ancient-rites.c",
        "/guilds/disciple-of-argloth/dark-scholarship/cooldown-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/blood-philosophy.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/blood-philosophy.c",
        "/guilds/disciple-of-argloth/dark-scholarship/forbidden-anatomy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/void-philosophy.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/void-philosophy.c",
        "/guilds/disciple-of-argloth/dark-scholarship/mana-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/scholar-attunement.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/scholar-attunement.c",
        "/guilds/disciple-of-argloth/dark-scholarship/ancient-rites.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/argloth-scholar.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/argloth-scholar.c",
        "/guilds/disciple-of-argloth/dark-scholarship/blood-philosophy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/master-theorist.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/master-theorist.c",
        "/guilds/disciple-of-argloth/dark-scholarship/void-philosophy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/dark-erudition.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/dark-erudition.c",
        "/guilds/disciple-of-argloth/dark-scholarship/scholar-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFourthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/lore-of-ruin.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/lore-of-ruin.c",
        "/guilds/disciple-of-argloth/dark-scholarship/master-theorist.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySixthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/argloth-gospel.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/argloth-gospel.c",
        "/guilds/disciple-of-argloth/dark-scholarship/argloth-scholar.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/argloth-codex.c");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/theoretical-mastery.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/argloth-codex.c",
        "/guilds/disciple-of-argloth/dark-scholarship/argloth-gospel.c");
    addChild("/guilds/disciple-of-argloth/dark-scholarship/theoretical-mastery.c",
        "/guilds/disciple-of-argloth/dark-scholarship/master-theorist.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/testament-of-argloth.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/testament-of-argloth.c",
        "/guilds/disciple-of-argloth/dark-scholarship/dark-erudition.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySecondLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/the-annals-of-ruin.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/the-annals-of-ruin.c",
        "/guilds/disciple-of-argloth/dark-scholarship/lore-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/the-book-of-endings.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/the-book-of-endings.c",
        "/guilds/disciple-of-argloth/dark-scholarship/lore-of-ruin.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyEighthLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/dark-scholarship-mastery.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/dark-scholarship-mastery.c",
        "/guilds/disciple-of-argloth/dark-scholarship/the-book-of-endings.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventiethLevel()
{
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/the-final-scripture.c");

    addChild("/guilds/disciple-of-argloth/dark-scholarship/the-final-scripture.c",
        "/guilds/disciple-of-argloth/dark-scholarship/dark-scholarship-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Dark Scholarship");
    Description("The lore and theory of blood and death magic as recorded by Argloth's scholars - a path of knowledge that amplifies every other branch of the covenant.");
    Source("Disciple of Argloth");
    addResearchElement("/guilds/disciple-of-argloth/dark-scholarship/root.c");
    TreeRoot("/guilds/disciple-of-argloth/dark-scholarship/root.c");

    FirstLevel();
    ThirdLevel();
    FourthLevel();
    FifthLevel();
    SeventhLevel();
    TenthLevel();
    TwelfthLevel();
    FourteenthLevel();
    SixteenthLevel();
    EighteenthLevel();
    TwentiethLevel();
    TwentySecondLevel();
    TwentySixthLevel();
    TwentyEighthLevel();
    ThirtiethLevel();
    ThirtySecondLevel();
    ThirtyFourthLevel();
    ThirtySixthLevel();
    FortiethLevel();
    FortySecondLevel();
    FortyFourthLevel();
    FiftiethLevel();
    FiftyFourthLevel();
    FiftySixthLevel();
    FiftyEighthLevel();
    SixtiethLevel();
    SixtySecondLevel();
    SixtyFifthLevel();
    SixtyEighthLevel();
    SeventiethLevel();
}
