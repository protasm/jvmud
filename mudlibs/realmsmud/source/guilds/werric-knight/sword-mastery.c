//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/researchTree.c";

/////////////////////////////////////////////////////////////////////////////
private void FirstLevel()
{
    addResearchElement("/guilds/werric-knight/sword/sword-training.c");
    addChild("/guilds/werric-knight/sword/sword-training.c",
        "/guilds/werric-knight/sword/root.c");

    addResearchElement("/guilds/werric-knight/sword/basic-form.c");
    addChild("/guilds/werric-knight/sword/basic-form.c",
        "/guilds/werric-knight/sword/root.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirdLevel()
{
    addResearchElement("/guilds/werric-knight/sword/precise-strike.c");
    addChild("/guilds/werric-knight/sword/precise-strike.c",
        "/guilds/werric-knight/sword/sword-training.c");

    addResearchElement("/guilds/werric-knight/sword/discipline-cut.c");
    addChild("/guilds/werric-knight/sword/discipline-cut.c",
        "/guilds/werric-knight/sword/basic-form.c");

    addResearchElement("/guilds/werric-knight/sword/flowing-form.c");
    addChild("/guilds/werric-knight/sword/flowing-form.c",
        "/guilds/werric-knight/sword/precise-strike.c");

    addResearchElement("/guilds/werric-knight/sword/powerful-strike.c");
    addChild("/guilds/werric-knight/sword/powerful-strike.c",
        "/guilds/werric-knight/sword/discipline-cut.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/blade-focus.c");
    addChild("/guilds/werric-knight/sword/blade-focus.c",
        "/guilds/werric-knight/sword/precise-strike.c");

    addResearchElement("/guilds/werric-knight/sword/perfect-strike.c");
    addChild("/guilds/werric-knight/sword/perfect-strike.c",
        "/guilds/werric-knight/sword/discipline-cut.c");

    addResearchElement("/guilds/werric-knight/sword/combat-mastery.c");
    addChild("/guilds/werric-knight/sword/combat-mastery.c",
        "/guilds/werric-knight/sword/basic-form.c");

    addResearchElement("/guilds/werric-knight/sword/rising-cut.c");
    addChild("/guilds/werric-knight/sword/rising-cut.c",
        "/guilds/werric-knight/sword/blade-focus.c");

    addResearchElement("/guilds/werric-knight/sword/falling-blade.c");
    addChild("/guilds/werric-knight/sword/falling-blade.c",
        "/guilds/werric-knight/sword/perfect-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventhLevel()
{
    addResearchElement("/guilds/werric-knight/sword/werras-edge.c");
    addChild("/guilds/werric-knight/sword/werras-edge.c",
        "/guilds/werric-knight/sword/perfect-strike.c");

    addResearchElement("/guilds/werric-knight/sword/form-of-mountain.c");
    addChild("/guilds/werric-knight/sword/form-of-mountain.c",
        "/guilds/werric-knight/sword/combat-mastery.c");

    addResearchElement("/guilds/werric-knight/sword/offensive-stance.c");
    addChild("/guilds/werric-knight/sword/offensive-stance.c",
        "/guilds/werric-knight/sword/blade-focus.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NinthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/whirling-blade.c");
    addChild("/guilds/werric-knight/sword/whirling-blade.c",
        "/guilds/werric-knight/sword/werras-edge.c");

    addResearchElement("/guilds/werric-knight/sword/riposte.c");
    addChild("/guilds/werric-knight/sword/riposte.c",
        "/guilds/werric-knight/sword/form-of-mountain.c");

    addResearchElement("/guilds/werric-knight/sword/improved-precision.c");
    addChild("/guilds/werric-knight/sword/improved-precision.c",
        "/guilds/werric-knight/sword/offensive-stance.c");

    addResearchElement("/guilds/werric-knight/sword/spinning-cut.c");
    addChild("/guilds/werric-knight/sword/spinning-cut.c",
        "/guilds/werric-knight/sword/whirling-blade.c");

    addResearchElement("/guilds/werric-knight/sword/swift-riposte.c");
    addChild("/guilds/werric-knight/sword/swift-riposte.c",
        "/guilds/werric-knight/sword/riposte.c");
}

/////////////////////////////////////////////////////////////////////////////
private void EleventhLevel()
{
    addResearchElement("/guilds/werric-knight/sword/crossed-swords.c");
    addChild("/guilds/werric-knight/sword/crossed-swords.c",
        "/guilds/werric-knight/sword/whirling-blade.c");

    addResearchElement("/guilds/werric-knight/sword/counterattack.c");
    addChild("/guilds/werric-knight/sword/counterattack.c",
        "/guilds/werric-knight/sword/riposte.c");

    addResearchElement("/guilds/werric-knight/sword/blade-weaving.c");
    addChild("/guilds/werric-knight/sword/blade-weaving.c",
        "/guilds/werric-knight/sword/improved-precision.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirteenthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/enhanced-damage.c");
    addChild("/guilds/werric-knight/sword/enhanced-damage.c",
        "/guilds/werric-knight/sword/blade-focus.c");

    addResearchElement("/guilds/werric-knight/sword/critical-strike.c");
    addChild("/guilds/werric-knight/sword/critical-strike.c",
        "/guilds/werric-knight/sword/perfect-strike.c");

    addResearchElement("/guilds/werric-knight/sword/flowing-forms.c");
    addChild("/guilds/werric-knight/sword/flowing-forms.c",
        "/guilds/werric-knight/sword/blade-weaving.c");

    addResearchElement("/guilds/werric-knight/sword/penetrating-thrust.c");
    addChild("/guilds/werric-knight/sword/penetrating-thrust.c",
        "/guilds/werric-knight/sword/critical-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void FifteenthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/master-swordsman.c");
    addChild("/guilds/werric-knight/sword/master-swordsman.c",
        "/guilds/werric-knight/sword/enhanced-damage.c");

    addResearchElement("/guilds/werric-knight/sword/devastating-combo.c");
    addChild("/guilds/werric-knight/sword/devastating-combo.c",
        "/guilds/werric-knight/sword/crossed-swords.c");

    addResearchElement("/guilds/werric-knight/sword/masters-precision.c");
    addChild("/guilds/werric-knight/sword/masters-precision.c",
        "/guilds/werric-knight/sword/flowing-forms.c");
}

/////////////////////////////////////////////////////////////////////////////
private void SeventeenthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/blade-tempest.c");
    addChild("/guilds/werric-knight/sword/blade-tempest.c",
        "/guilds/werric-knight/sword/devastating-combo.c");

    addResearchElement("/guilds/werric-knight/sword/surgical-strike.c");
    addChild("/guilds/werric-knight/sword/surgical-strike.c",
        "/guilds/werric-knight/sword/critical-strike.c");

    addResearchElement("/guilds/werric-knight/sword/parry-mastery.c");
    addChild("/guilds/werric-knight/sword/parry-mastery.c",
        "/guilds/werric-knight/sword/counterattack.c");

    addResearchElement("/guilds/werric-knight/sword/blade-tempest-technique.c");
    addChild("/guilds/werric-knight/sword/blade-tempest-technique.c",
        "/guilds/werric-knight/sword/blade-tempest.c");

    addResearchElement("/guilds/werric-knight/sword/dancing-steel.c");
    addChild("/guilds/werric-knight/sword/dancing-steel.c",
        "/guilds/werric-knight/sword/devastating-combo.c");
}

/////////////////////////////////////////////////////////////////////////////
private void NineteenthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/blade-dance.c");
    addChild("/guilds/werric-knight/sword/blade-dance.c",
        "/guilds/werric-knight/sword/masters-precision.c");

    addResearchElement("/guilds/werric-knight/sword/deadly-precision.c");
    addChild("/guilds/werric-knight/sword/deadly-precision.c",
        "/guilds/werric-knight/sword/surgical-strike.c");

    addResearchElement("/guilds/werric-knight/sword/enhanced-forms.c");
    addChild("/guilds/werric-knight/sword/enhanced-forms.c",
        "/guilds/werric-knight/sword/form-of-mountain.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/sword/righteous-fury.c");
    addChild("/guilds/werric-knight/sword/righteous-fury.c",
        "/guilds/werric-knight/sword/master-swordsman.c");

    addResearchElement("/guilds/werric-knight/sword/hurricane-slash.c");
    addChild("/guilds/werric-knight/sword/hurricane-slash.c",
        "/guilds/werric-knight/sword/blade-tempest.c");

    addResearchElement("/guilds/werric-knight/sword/death-blow.c");
    addChild("/guilds/werric-knight/sword/death-blow.c",
        "/guilds/werric-knight/sword/deadly-precision.c");

    addResearchElement("/guilds/werric-knight/sword/fury-strike.c");
    addChild("/guilds/werric-knight/sword/fury-strike.c",
        "/guilds/werric-knight/sword/righteous-fury.c");
}

/////////////////////////////////////////////////////////////////////////////
private void TwentyFifthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/supreme-mastery.c");
    addChild("/guilds/werric-knight/sword/supreme-mastery.c",
        "/guilds/werric-knight/sword/master-swordsman.c");

    addResearchElement("/guilds/werric-knight/sword/perfect-form.c");
    addChild("/guilds/werric-knight/sword/perfect-form.c",
        "/guilds/werric-knight/sword/enhanced-forms.c");

    addResearchElement("/guilds/werric-knight/sword/flurry-of-blades.c");
    addChild("/guilds/werric-knight/sword/flurry-of-blades.c",
        "/guilds/werric-knight/sword/blade-dance.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyFirstLevel()
{
    addResearchElement("/guilds/werric-knight/sword/legendary-swordsman.c");
    addChild("/guilds/werric-knight/sword/legendary-swordsman.c",
        "/guilds/werric-knight/sword/supreme-mastery.c");

    addResearchElement("/guilds/werric-knight/sword/ultimate-strike.c");
    addChild("/guilds/werric-knight/sword/ultimate-strike.c",
        "/guilds/werric-knight/sword/righteous-fury.c");

    addResearchElement("/guilds/werric-knight/sword/sword-saint.c");
    addChild("/guilds/werric-knight/sword/sword-saint.c",
        "/guilds/werric-knight/sword/perfect-form.c");

    addResearchElement("/guilds/werric-knight/sword/supreme-blade-mastery.c");
    addChild("/guilds/werric-knight/sword/supreme-blade-mastery.c",
        "/guilds/werric-knight/sword/ultimate-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
private void ThirtyNinthLevel()
{
    addResearchElement("/guilds/werric-knight/sword/werras-legacy.c");
    addChild("/guilds/werric-knight/sword/werras-legacy.c",
        "/guilds/werric-knight/sword/legendary-swordsman.c");

    addResearchElement("/guilds/werric-knight/sword/blade-of-kings.c");
    addChild("/guilds/werric-knight/sword/blade-of-kings.c",
        "/guilds/werric-knight/sword/ultimate-strike.c");
}

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    Name("Sword Mastery");
    Description("This research tree provides mastery of the sword, "
        "the primary weapon of the Werric Knights.");
    Source("Werric Knight");
    addResearchElement("/guilds/werric-knight/sword/root.c");
    TreeRoot("/guilds/werric-knight/sword/root.c");

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
    ThirtyFirstLevel();
    ThirtyNinthLevel();
}
