//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/tests/framework/testFixture.c";

object Player;
object Selector;

/////////////////////////////////////////////////////////////////////////////
void Init()
{
    ignoreList += ({ "AdvanceToLevel" });
}

/////////////////////////////////////////////////////////////////////////////
int AdvanceToLevel(int level)
{
    int runningLevel = Player.guildLevel("runeskald");
    while ((Player.guildLevel("runeskald") < level) &&
           Player.memberOfGuild("runeskald"))
    {
        Player.addExperience(1000 * runningLevel + 1000);
        Player.advanceLevel("runeskald");
        destruct(present_clone("/lib/modules/guilds/advanceLevelSelector.c",
            Player));
        runningLevel = Player.guildLevel("runeskald");
    }
    return runningLevel;
}

/////////////////////////////////////////////////////////////////////////////
void Setup()
{
    load_object("/guilds/runeskald/runeskald.c");
    Selector = clone_object("/guilds/runeskald/selectors/craftRuneSelector.c");

    Player = clone_object("/lib/tests/support/services/mockPlayer.c");
    Player.Name("bob");
    Player.addCommands();
    Player.colorConfiguration("none");
    Player.charsetConfiguration("ascii");

    Player.addSkillPoints(500);
    Player.advanceSkill("gem crafting", 5);
    Player.advanceSkill("local history", 5);
    Player.advanceSkill("spellcraft", 5);

    Player.addResearchPoints(20);

    Player.joinGuild("runeskald");
    destruct(present_clone("/lib/modules/guilds/advanceLevelSelector.c",
        Player));
    AdvanceToLevel(7);
    ExpectEq(({ "runeskald" }), Player.memberOfGuilds());

    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/basic-power-rune.c"));
    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/basic-ward-rune.c"));
    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/basic-blade-rune.c"));
    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/basic-storm-rune.c"));
    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/basic-frost-rune.c"));
    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/basic-flame-rune.c"));

    move_object(Selector, Player);
}

/////////////////////////////////////////////////////////////////////////////
void CleanUp()
{
    destruct(Player);
    destruct(Selector);
}

/////////////////////////////////////////////////////////////////////////////
void FullMenuDisplaysAllBasicRunesCorrectly()
{
    Selector.initiateSelector(Player);
    string msg = Player.caughtMessage();

    ExpectSubStringMatch("Craft Rune", msg);
    // All six basic rune types enabled: %-20s pad + "    " default spacing.
    ExpectSubStringMatch("Basic Power rune        ", msg);
    ExpectSubStringMatch("Basic Ward rune         ", msg);
    ExpectSubStringMatch("Basic Blade rune        ", msg);
    ExpectSubStringMatch("Basic Storm rune        ", msg);
    ExpectSubStringMatch("Basic Frost rune        ", msg);
    ExpectSubStringMatch("Basic Flame rune        ", msg);
    ExpectSubStringMatch("Exit Craft Rune Menu", msg);
}

/////////////////////////////////////////////////////////////////////////////
void TopLevelMenuShowsBasicRunesWhenOnlyBasicResearched()
{
    Selector.initiateSelector(Player);
    string msg = Player.caughtMessage();
    ExpectSubStringMatch("Basic Power rune", msg);
    ExpectSubStringMatch("Basic Ward rune", msg);
    ExpectSubStringMatch("Basic Blade rune", msg);
    ExpectSubStringMatch("Basic Storm rune", msg);
    ExpectSubStringMatch("Basic Frost rune", msg);
    ExpectSubStringMatch("Basic Flame rune", msg);
}

/////////////////////////////////////////////////////////////////////////////
void TopLevelMenuDoesNotShowElderRunesWithoutElderResearch()
{
    Selector.initiateSelector(Player);
    string msg = Player.caughtMessage();
    ExpectFalse(sizeof(regexp(({ msg }), "elder.*rune")),
        "Elder runes should not appear without elder research");
}

/////////////////////////////////////////////////////////////////////////////
void ElderRunesAppearAfterElderResearchGranted()
{
    Player.advanceSkill("gem crafting", 5);
    Player.advanceSkill("spellcraft", 3);
    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/elder-rune-crafting.c"));
    ExpectTrue(Player.initiateResearch(
        "/guilds/runeskald/rune-crafting/elder-power-rune.c"));

    Selector.initiateSelector(Player);
    string msg = Player.caughtMessage();
    ExpectSubStringMatch("Elder Power rune", msg);
}

/////////////////////////////////////////////////////////////////////////////
void UnresearchedTypeAppearsDisabledInMenu()
{
    // Research the tier but not a specific type (e.g. power only).
    // Other basic types should appear greyed out.
    object secondSelector =
        clone_object("/guilds/runeskald/selectors/craftRuneSelector.c");

    object secondPlayer = clone_object("/lib/tests/support/services/mockPlayer.c");
    secondPlayer.Name("bob2");
    secondPlayer.addCommands();
    secondPlayer.colorConfiguration("none");
    secondPlayer.charsetConfiguration("ascii");
    secondPlayer.addSkillPoints(500);
    secondPlayer.advanceSkill("gem crafting", 1);
    secondPlayer.advanceSkill("local history", 1);
    secondPlayer.addResearchPoints(10);
    secondPlayer.joinGuild("runeskald");
    destruct(present_clone("/lib/modules/guilds/advanceLevelSelector.c",
        secondPlayer));
    ExpectTrue(secondPlayer.initiateResearch(
        "/guilds/runeskald/rune-crafting/basic-power-rune.c"));

    move_object(secondSelector, secondPlayer);
    secondSelector.initiateSelector(secondPlayer);
    string secondMessage = secondPlayer.caughtMessage();

    // Power rune researched: %-20s pad + "    " proves enabled.
    ExpectSubStringMatch("Basic Power rune        ", secondMessage);
    // Ward rune not researched: %-20s pad + " (X)" proves disabled.
    ExpectSubStringMatch("Basic Ward rune      \\(X\\)", secondMessage);

    destruct(secondPlayer);
    destruct(secondSelector);
}

/////////////////////////////////////////////////////////////////////////////
void ExitOptionAlwaysPresent()
{
    Selector.initiateSelector(Player);
    ExpectSubStringMatch("Exit Craft Rune Menu", Player.caughtMessage());
}

/////////////////////////////////////////////////////////////////////////////
void ChoosingExitClosesSelector()
{
    Selector.initiateSelector(Player);
    // Setup researches only basic tier (6 types) + Exit = option 7.
    Selector.applySelection("7");
    ExpectSubStringMatch("You have selected 'Exit Craft Rune Menu'",
        Player.caughtMessage());
}
