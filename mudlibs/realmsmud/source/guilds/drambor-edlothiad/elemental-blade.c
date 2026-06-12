//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/flame-edge.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/shock-edge.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/arcane-edge.c");

    addChild("/guilds/drambor-edlothiad/elemental/flame-edge.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
    addChild("/guilds/drambor-edlothiad/elemental/shock-edge.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
    addChild("/guilds/drambor-edlothiad/elemental/arcane-edge.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/frost-edge.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/energy-edge.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/kindle.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/static-charge.c");

    addChild("/guilds/drambor-edlothiad/elemental/frost-edge.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
    addChild("/guilds/drambor-edlothiad/elemental/energy-edge.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
    addChild("/guilds/drambor-edlothiad/elemental/kindle.c",
        "/guilds/drambor-edlothiad/elemental/flame-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/static-charge.c",
        "/guilds/drambor-edlothiad/elemental/shock-edge.c");
}
/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/acid-edge.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/searing-brand.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/flash-freeze.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/arcane-attunement.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-affinity.c");

    addChild("/guilds/drambor-edlothiad/elemental/acid-edge.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
    addChild("/guilds/drambor-edlothiad/elemental/searing-brand.c",
        "/guilds/drambor-edlothiad/elemental/flame-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/flash-freeze.c",
        "/guilds/drambor-edlothiad/elemental/frost-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/arcane-attunement.c",
        "/guilds/drambor-edlothiad/elemental/arcane-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-affinity.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/glacial-brand.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/voltaic-brand.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/shadow-edge.c");

    addChild("/guilds/drambor-edlothiad/elemental/glacial-brand.c",
        "/guilds/drambor-edlothiad/elemental/frost-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/voltaic-brand.c",
        "/guilds/drambor-edlothiad/elemental/shock-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/shadow-edge.c",
        "/guilds/drambor-edlothiad/elemental/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/force-brand.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/mystic-brand.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/corrosive-knowledge.c");

    addChild("/guilds/drambor-edlothiad/elemental/force-brand.c",
        "/guilds/drambor-edlothiad/elemental/energy-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/mystic-brand.c",
        "/guilds/drambor-edlothiad/elemental/arcane-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/corrosive-knowledge.c",
        "/guilds/drambor-edlothiad/elemental/acid-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/caustic-brand.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/shadow-attunement.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/channeling-efficiency.c");

    addChild("/guilds/drambor-edlothiad/elemental/caustic-brand.c",
        "/guilds/drambor-edlothiad/elemental/acid-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/shadow-attunement.c",
        "/guilds/drambor-edlothiad/elemental/shadow-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/channeling-efficiency.c",
        "/guilds/drambor-edlothiad/elemental/elemental-affinity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/inferno-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/inferno-edge.c",
        "/guilds/drambor-edlothiad/elemental/searing-brand.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/conductivity.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/umbral-brand.c");

    addChild("/guilds/drambor-edlothiad/elemental/conductivity.c",
        "/guilds/drambor-edlothiad/elemental/voltaic-brand.c");
    addChild("/guilds/drambor-edlothiad/elemental/umbral-brand.c",
        "/guilds/drambor-edlothiad/elemental/shadow-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/permafrost.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-harmony.c");

    addChild("/guilds/drambor-edlothiad/elemental/permafrost.c",
        "/guilds/drambor-edlothiad/elemental/glacial-brand.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-harmony.c",
        "/guilds/drambor-edlothiad/elemental/channeling-efficiency.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/arcane-focus.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/arcane-amplification.c");

    addChild("/guilds/drambor-edlothiad/elemental/arcane-focus.c",
        "/guilds/drambor-edlothiad/elemental/mystic-brand.c");
    addChild("/guilds/drambor-edlothiad/elemental/arcane-amplification.c",
        "/guilds/drambor-edlothiad/elemental/force-brand.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/dissolution.c");
    addChild("/guilds/drambor-edlothiad/elemental/dissolution.c",
        "/guilds/drambor-edlothiad/elemental/caustic-brand.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/shadow-mastery.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-convergence.c");

    addChild("/guilds/drambor-edlothiad/elemental/shadow-mastery.c",
        "/guilds/drambor-edlothiad/elemental/umbral-brand.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-convergence.c",
        "/guilds/drambor-edlothiad/elemental/elemental-harmony.c");
}
/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/blazing-vortex.c");
    addChild("/guilds/drambor-edlothiad/elemental/blazing-vortex.c",
        "/guilds/drambor-edlothiad/elemental/inferno-edge.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/thunderstrike.c");
    addChild("/guilds/drambor-edlothiad/elemental/thunderstrike.c",
        "/guilds/drambor-edlothiad/elemental/conductivity.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/avalanche-strike.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-resonance.c");

    addChild("/guilds/drambor-edlothiad/elemental/avalanche-strike.c",
        "/guilds/drambor-edlothiad/elemental/permafrost.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-resonance.c",
        "/guilds/drambor-edlothiad/elemental/elemental-convergence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/resonance-blade.c");
    addChild("/guilds/drambor-edlothiad/elemental/resonance-blade.c",
        "/guilds/drambor-edlothiad/elemental/arcane-amplification.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/vitriol-blade.c");
    addChild("/guilds/drambor-edlothiad/elemental/vitriol-blade.c",
        "/guilds/drambor-edlothiad/elemental/dissolution.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/eldritch-blade.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-mastery.c");

    addChild("/guilds/drambor-edlothiad/elemental/eldritch-blade.c",
        "/guilds/drambor-edlothiad/elemental/arcane-focus.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-mastery.c",
        "/guilds/drambor-edlothiad/elemental/elemental-resonance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/void-blade.c");
    addChild("/guilds/drambor-edlothiad/elemental/void-blade.c",
        "/guilds/drambor-edlothiad/elemental/shadow-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-supremacy.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-supremacy.c",
        "/guilds/drambor-edlothiad/elemental/elemental-mastery.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/pyroclasm-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/pyroclasm-edge.c",
        "/guilds/drambor-edlothiad/elemental/blazing-vortex.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/storm-conduit.c");
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-transcendence.c");

    addChild("/guilds/drambor-edlothiad/elemental/storm-conduit.c",
        "/guilds/drambor-edlothiad/elemental/thunderstrike.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-transcendence.c",
        "/guilds/drambor-edlothiad/elemental/elemental-supremacy.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FortyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/absolute-zero.c");
    addChild("/guilds/drambor-edlothiad/elemental/absolute-zero.c",
        "/guilds/drambor-edlothiad/elemental/avalanche-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/entropy-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/entropy-edge.c",
        "/guilds/drambor-edlothiad/elemental/vitriol-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyThirdLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/annihilation-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/annihilation-edge.c",
        "/guilds/drambor-edlothiad/elemental/resonance-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/oblivion-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/oblivion-edge.c",
        "/guilds/drambor-edlothiad/elemental/void-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/transcendence-edge.c");
    addChild("/guilds/drambor-edlothiad/elemental/transcendence-edge.c",
        "/guilds/drambor-edlothiad/elemental/eldritch-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyNinthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/prismatic-blade.c");
    addChild("/guilds/drambor-edlothiad/elemental/prismatic-blade.c",
        "/guilds/drambor-edlothiad/elemental/elemental-transcendence.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SixtyFifthLevel()
{
    addResearchElement("/guilds/drambor-edlothiad/elemental/elemental-singularity.c");
    addChild("/guilds/drambor-edlothiad/elemental/elemental-singularity.c",
        "/guilds/drambor-edlothiad/elemental/prismatic-blade.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Elemental Blade");
    Description("This research tree teaches the art of infusing wielded blades "
        "with elemental energy. Fire, cold, lightning, acid, shadow, arcane, "
        "and pure energy enchantments can be channeled through a blade to "
        "devastating effect.");
    Source("Drambor Edlothiad");
    addResearchElement("/guilds/drambor-edlothiad/elemental/root.c");
    TreeRoot("/guilds/drambor-edlothiad/elemental/root.c");

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
    FortyFirstLevel();
    FortyFifthLevel();
    FortySeventhLevel();
    FortyNinthLevel();
    FiftyFirstLevel();
    FiftyThirdLevel();
    FiftyFifthLevel();
    FiftySeventhLevel();
    FiftyNinthLevel();
    SixtyFifthLevel();
}
