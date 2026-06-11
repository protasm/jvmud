//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

protected string WeaponType = "ERROR";
protected string WeaponSkill = "unarmed";
protected string* ValidWeaponTypes = ({ });

/////////////////////////////////////////////////////////////////////////////
protected void SetupResearch()
{
}

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/blood-lash.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-lash.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/root.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/sanguine-surge.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-surge.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/root.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/hemorrhage.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/hemorrhage.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-lash.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/vital-feed.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/vital-feed.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-surge.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/soul-drain.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/soul-drain.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-lash.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/dark-potency.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/vital-rupture.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/dark-potency.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/vital-feed.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/vital-rupture.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/hemorrhage.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/blood-covenant.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-reserve.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/crimson-brand.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-covenant.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-surge.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-reserve.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/root.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/crimson-brand.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/vital-rupture.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/exsanguinate.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-boon.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/exsanguinate.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/soul-drain.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-boon.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/root.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/corrupting-tide.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/blood-shriek.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/corrupting-tide.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/dark-potency.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-shriek.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/crimson-brand.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/hemorrhagic-mastery.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-call.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/zhardegs-lash.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/hemorrhagic-mastery.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-covenant.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-call.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-reserve.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/zhardegs-lash.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-shriek.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/crimson-devastation.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-endurance.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/crimson-devastation.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/exsanguinate.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-endurance.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-boon.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyThirdLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/soul-hunger.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/blood-tide.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/soul-hunger.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/dark-potency.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-tide.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/soul-drain.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-might.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/life-fracture.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-might.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-call.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/life-fracture.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-tide.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void TwentySeventhLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/scarlet-torrent.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/scarlet-torrent.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-tide.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyNinthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-finess.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/crimson-precision.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-finess.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-endurance.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/crimson-precision.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/soul-hunger.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/sanguine-eruption.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-eruption.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/scarlet-torrent.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyThirdLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/final-offering.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/final-offering.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/corrupting-tide.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFifthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/mirosts-ruin.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/mirosts-ruin.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/life-fracture.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtySeventhLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-strength.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/sanguine-resonance.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-strength.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-finess.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-resonance.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/crimson-precision.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-wrath.c", WeaponType));
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/argloths-fury.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-wrath.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/crimson-devastation.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-fury.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-might.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFirstLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/sanguine-mastery.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-mastery.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/final-offering.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FortyThirdLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/blood-weapon-mastery.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-weapon-mastery.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-resonance.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FortyFifthLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/sylthasis-strike.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sylthasis-strike.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/mirosts-ruin.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FortySeventhLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/ruinous-potential.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/ruinous-potential.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sanguine-mastery.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FiftyFirstLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/covenant-of-corruption.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/covenant-of-corruption.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-weapon-mastery.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
private void FiftySeventhLevel()
{
    addResearchElement(sprintf(
        "/guilds/disciple-of-argloth/paths/%s/blood/blood-obliteration.c", WeaponType));

    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-obliteration.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/sylthasis-strike.c", WeaponType));
    addChild(
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/blood-obliteration.c", WeaponType),
        sprintf("/guilds/disciple-of-argloth/paths/%s/blood/argloths-wrath.c", WeaponType));
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Path of Blood");
    Source("Disciple of Argloth");
    Description("This skill provides the Disciple with the knowledge to "
        "channel Argloth's forbidden blood magic through their chosen "
        "weapon, combining martial and sanguine power.");
    SetupResearch();

    if (WeaponType && WeaponType != "ERROR")
    {
        addResearchElement(sprintf(
            "/guilds/disciple-of-argloth/paths/%s/blood/root.c", WeaponType));
        TreeRoot(sprintf(
            "/guilds/disciple-of-argloth/paths/%s/blood/root.c", WeaponType));

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
        FortyFifthLevel();
        FortySeventhLevel();
        FiftyFirstLevel();
        FiftySeventhLevel();
    }
}
