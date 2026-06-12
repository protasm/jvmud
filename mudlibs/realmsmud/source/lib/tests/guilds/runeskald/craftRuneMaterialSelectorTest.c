//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/tests/framework/testFixture.c";

object Player;
object Selector;

/////////////////////////////////////////////////////////////////////////////
private void addStoneMaterial(int qty)
{
    object mat = clone_object("/lib/instances/items/materials/stone/granite.c");
    mat.set("quantity", qty);
    move_object(mat, Player);
}

/////////////////////////////////////////////////////////////////////////////
private void addCrystalMaterial(int qty)
{
    object mat = clone_object("/lib/instances/items/materials/crystal/crystal.c");
    mat.set("quantity", qty);
    move_object(mat, Player);
}

/////////////////////////////////////////////////////////////////////////////
private void addSkeletalMaterial(int qty)
{
    object mat = clone_object("/lib/instances/items/materials/skeletal/bone.c");
    mat.set("quantity", qty);
    move_object(mat, Player);
}

/////////////////////////////////////////////////////////////////////////////
void Setup()
{
    Selector = clone_object("/guilds/runeskald/selectors/craftRuneMaterialSelector.c");
    Selector.setTier("basic");
    Selector.setRuneType("power");
    Selector.setMaxBonuses(1);

    Player = clone_object("/lib/tests/support/services/mockPlayer.c");
    Player.Name("bob");
    Player.addCommands();

    move_object(Selector, Player);
}

/////////////////////////////////////////////////////////////////////////////
void CleanUp()
{
    destruct(Player);
    destruct(Selector);
}

/////////////////////////////////////////////////////////////////////////////
void EmptyInventoryShowsNoMaterialsMessage()
{
    Selector.initiateSelector(Player);
    ExpectSubStringMatch("No suitable materials", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void StoneMaterialsAppearInMenu()
{
    addStoneMaterial(3);
    Selector.initiateSelector(Player);
    ExpectSubStringMatch("Granite", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void CrystalMaterialsAppearInMenu()
{
    addCrystalMaterial(2);
    Selector.initiateSelector(Player);
    ExpectSubStringMatch("Crystal", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void SkeletalMaterialsAppearInMenu()
{
    addSkeletalMaterial(4);
    Selector.initiateSelector(Player);
    ExpectSubStringMatch("Bone", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void MetalMaterialsDoNotAppearInMenu()
{
    object mat = clone_object("/lib/instances/items/materials/metal/iron.c");
    mat.set("quantity", 5);
    move_object(mat, Player);

    Selector.initiateSelector(Player);
    ExpectFalse(sizeof(regexp(({ Player.caughtMessage() }), "Iron")),
        "Metal materials should not be shown");
}

/////////////////////////////////////////////////////////////////////////////
void MultipleMaterialTypesAllAppear()
{
    addStoneMaterial(2);
    addCrystalMaterial(1);
    addSkeletalMaterial(3);
    Selector.initiateSelector(Player);
    string msg = Player.caughtMessage();
    ExpectSubStringMatch("Granite", msg);
    ExpectSubStringMatch("Crystal", msg);
    ExpectSubStringMatch("Bone", msg);
}

/////////////////////////////////////////////////////////////////////////////
void QuantitiesAreAggregatedAcrossMultipleStacks()
{
    addStoneMaterial(3);
    addStoneMaterial(4);
    Selector.initiateSelector(Player);
    // Description should show total of 7
    ExpectSubStringMatch("7", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void ExitOptionAlwaysPresent()
{
    Selector.initiateSelector(Player);
    ExpectSubStringMatch("Exit Material Selection", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void SelectingMaterialLaunchesBonusSelector()
{
    addStoneMaterial(2);
    Selector.initiateSelector(Player);
    Player.resetCatchList();

    // Option 1 should be Granite - selecting it pushes to bonus selector.
    command("1", Player);
    ExpectSubStringMatch("Enchant", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void SelectingExitDoesNotLaunchBonusSelector()
{
    addStoneMaterial(2);
    Selector.initiateSelector(Player);
    string msg = Player.caughtMessage();

    string *lines = explode(msg, "\n");
    int lastOption = 0;
    foreach (string line in lines)
    {
        int n;
        if (sscanf(line, "    [%d]", n) == 1 && n > lastOption)
        {
            lastOption = n;
        }
    }
    command(to_string(lastOption), Player);
    // No "Enchant" message should have been sent.
    ExpectFalse(sizeof(regexp(({ Player.caughtMessage() }), "Enchant")));
}
