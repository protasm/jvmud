//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/fire-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/frost-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/lightning-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/fire-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/root.c");
    addChild("/guilds/drambor-edlothiad/enchantment/frost-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/root.c");
    addChild("/guilds/drambor-edlothiad/enchantment/lightning-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/acid-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/energy-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/acid-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/root.c");
    addChild("/guilds/drambor-edlothiad/enchantment/energy-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/magical-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/magical-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/searing-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/glacial-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/voltaic-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/searing-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/fire-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/glacial-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/frost-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/voltaic-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/lightning-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/caustic-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/force-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/shadow-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/enchantment-efficiency.c");

    addChild("/guilds/drambor-edlothiad/enchantment/caustic-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/acid-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/force-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/energy-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/shadow-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/magical-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/enchantment-efficiency.c",
        "/guilds/drambor-edlothiad/enchantment/fire-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/sonic-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/enchantment-duration.c");

    addChild("/guilds/drambor-edlothiad/enchantment/sonic-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/root.c");
    addChild("/guilds/drambor-edlothiad/enchantment/enchantment-duration.c",
        "/guilds/drambor-edlothiad/enchantment/enchantment-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/holy-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/dual-attunement.c");

    addChild("/guilds/drambor-edlothiad/enchantment/holy-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/magical-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/dual-attunement.c",
        "/guilds/drambor-edlothiad/enchantment/enchantment-duration.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/inferno-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/permafrost-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/inferno-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/searing-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/permafrost-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/glacial-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/thunderstrike-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/dissolution-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/thunderstrike-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/voltaic-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/dissolution-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/caustic-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/chaos-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/void-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/resonance-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/chaos-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/force-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/void-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/shadow-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/resonance-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/sonic-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/undead-bane-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/enchantment-potency.c");

    addChild("/guilds/drambor-edlothiad/enchantment/undead-bane-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/holy-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/enchantment-potency.c",
        "/guilds/drambor-edlothiad/enchantment/dual-attunement.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/rapid-enchanting.c");

    addChild("/guilds/drambor-edlothiad/enchantment/rapid-enchanting.c",
        "/guilds/drambor-edlothiad/enchantment/enchantment-potency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/hellfire-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/absolute-zero-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/elven-enchantcraft.c");

    addChild("/guilds/drambor-edlothiad/enchantment/hellfire-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/inferno-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/absolute-zero-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/permafrost-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/elven-enchantcraft.c",
        "/guilds/drambor-edlothiad/enchantment/rapid-enchanting.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/stormwrath-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/oblivion-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/stormwrath-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/thunderstrike-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/oblivion-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/dissolution-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/maelstrom-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/abyssal-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/maelstrom-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/chaos-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/abyssal-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/void-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/wail-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/sanctification-enchantment.c");

    addChild("/guilds/drambor-edlothiad/enchantment/wail-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/resonance-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/sanctification-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/undead-bane-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/enchantment-mastery.c");

    addChild("/guilds/drambor-edlothiad/enchantment/enchantment-mastery.c",
        "/guilds/drambor-edlothiad/enchantment/elven-enchantcraft.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/persistent-enchantments.c");

    addChild("/guilds/drambor-edlothiad/enchantment/persistent-enchantments.c",
        "/guilds/drambor-edlothiad/enchantment/enchantment-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/blade-resonance.c");

    addChild("/guilds/drambor-edlothiad/enchantment/blade-resonance.c",
        "/guilds/drambor-edlothiad/enchantment/persistent-enchantments.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/apocalypse-flame.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/entropic-frost.c");

    addChild("/guilds/drambor-edlothiad/enchantment/apocalypse-flame.c",
        "/guilds/drambor-edlothiad/enchantment/hellfire-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/entropic-frost.c",
        "/guilds/drambor-edlothiad/enchantment/absolute-zero-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/primordial-lightning.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/annihilation-acid.c");

    addChild("/guilds/drambor-edlothiad/enchantment/primordial-lightning.c",
        "/guilds/drambor-edlothiad/enchantment/stormwrath-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/annihilation-acid.c",
        "/guilds/drambor-edlothiad/enchantment/oblivion-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/pandemonium-edge.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/nihility-enchantment.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/enchantment-supremacy.c");

    addChild("/guilds/drambor-edlothiad/enchantment/pandemonium-edge.c",
        "/guilds/drambor-edlothiad/enchantment/maelstrom-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/nihility-enchantment.c",
        "/guilds/drambor-edlothiad/enchantment/abyssal-enchantment.c");
    addChild("/guilds/drambor-edlothiad/enchantment/enchantment-supremacy.c",
        "/guilds/drambor-edlothiad/enchantment/blade-resonance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/arcane-imbuer.c");

    addChild("/guilds/drambor-edlothiad/enchantment/arcane-imbuer.c",
        "/guilds/drambor-edlothiad/enchantment/enchantment-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/eldritch-flame.c");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/cosmic-frost.c");

    addChild("/guilds/drambor-edlothiad/enchantment/eldritch-flame.c",
        "/guilds/drambor-edlothiad/enchantment/apocalypse-flame.c");
    addChild("/guilds/drambor-edlothiad/enchantment/cosmic-frost.c",
        "/guilds/drambor-edlothiad/enchantment/entropic-frost.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/divine-storm.c");

    addChild("/guilds/drambor-edlothiad/enchantment/divine-storm.c",
        "/guilds/drambor-edlothiad/enchantment/primordial-lightning.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/transcendent-edge.c");

    addChild("/guilds/drambor-edlothiad/enchantment/transcendent-edge.c",
        "/guilds/drambor-edlothiad/enchantment/nihility-enchantment.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventyLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/enchantment/enchantment-transcendence.c");

    addChild("/guilds/drambor-edlothiad/enchantment/enchantment-transcendence.c",
        "/guilds/drambor-edlothiad/enchantment/arcane-imbuer.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Blade Enchantments");
    Description("This research tree provides the knowledge to imbue "
        "wielded blades with elemental and arcane enchantments, a "
        "hallmark of the Drambor Edlothiad tradition of weaving magic "
        "with swordplay.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/enchantment/root.c");
    TreeRoot("/guilds/drambor-edlothiad/enchantment/root.c");

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
    TwentyFifthLevel();
    TwentyNinthLevel();
    ThirtyFirstLevel();
    ThirtyThirdLevel();
    ThirtyFifthLevel();
    ThirtySeventhLevel();
    FortyFirstLevel();
    FortyFifthLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyFifthLevel();
    SixtyFirstLevel();
    SixtyThirdLevel();
    SixtyFifthLevel();
    SixtySeventhLevel();
    SeventyLevel();
}
