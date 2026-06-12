//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/knowledgeResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Crown's Mandate");
    addSpecification("source", "Order of Phaedra");
    addSpecification("description", "The highest teachings of the Order "
        "of Phaedra: to stand as sovereign protectors of the realm, "
        "wielding the full authority of the Crown and all four pillars "
        "of the Order united into a single, unbreakable purpose.");
    addPrerequisite("guild rank",
        (["type": "guild rank", "guild": "Order of Phaedra",
          "value": "steward"]));
    addSpecification("research type", "tree root");
    addSpecification("scope", "self");
    addSpecification("effect", "strategic");
    addSpecification("related research", ({
        "/guilds/phaedra/courage/courage-root.c",
        "/guilds/phaedra/fidelity/fidelity-root.c",
        "/guilds/phaedra/stewardship/stewardship-root.c",
        "/guilds/phaedra/justice/justice-root.c"
    }));
}
