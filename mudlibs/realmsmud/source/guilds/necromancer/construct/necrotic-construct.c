//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/activeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Construct Necromancer Spell");
    addSpecification("source", "necromancer");
    addSpecification("description", "This skill provides the user with the knowledge of how to construct a custom necromancer spell combining form, function, and effect.");

    addSpecification("scope", "self");
    addSpecification("research type", "granted");
    addSpecification("command template", "construct necromancer spell");
}

/////////////////////////////////////////////////////////////////////////////
protected nomask int executeOnSelf(string unparsedCommand, object owner,
    string researchName)
{
    object selectorObj = clone_object(
        "/lib/modules/guilds/selectors/constructedResearchSelector.c");

    // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
    // Original bad LPC: selectorObj.setType("Necromancer Spell");
    selectorObj->setType("Necromancer Spell");
    // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
    // Original bad LPC: selectorObj.setConstructedGrouping("/guilds/necromancer/construct/root.c");
    selectorObj->setConstructedGrouping("/guilds/necromancer/construct/root.c");

    move_object(selectorObj, owner);
    // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
    // Original bad LPC: selectorObj.registerEvent(this_object());
    selectorObj->registerEvent(this_object());
    // JVMud cleanup: Realms had non-LPC dot-call syntax here; use LPC arrow notation.
    // Original bad LPC: selectorObj.initiateSelector(owner);
    selectorObj->initiateSelector(owner);

    return 1;
}

/////////////////////////////////////////////////////////////////////////////
public nomask void onSelectorCompleted(object caller)
{
    caller->cleanUp();
}

/////////////////////////////////////////////////////////////////////////////
public nomask void onSelectorAborted(object caller)
{
    caller->cleanUp();
}
