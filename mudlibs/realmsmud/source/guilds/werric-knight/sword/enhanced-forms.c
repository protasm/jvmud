//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/modules/research/passiveResearchItem.c";

/////////////////////////////////////////////////////////////////////////////
protected void Setup()
{
    addSpecification("name", "Enhanced Forms");
    addSpecification("source", "Werric Knight");
    addSpecification("description", "Through relentless practice and unwavering "
        "dedication, you have refined your combat stances to their ultimate expression. "
        "Your forms now embody the perfect balance between offense and defense, "
        "between aggression and caution. Each stance becomes more potent, more "
        "effective - a testament to the power of disciplined perfection.");

    addPrerequisite("guilds/werric-knight/sword/offensive-stance.c",
        (["type": "research"]));
    addPrerequisite("guilds/werric-knight/sword/deadly-precision.c",
        (["type": "research"]));
    addPrerequisite("/guilds/werric-knight/werric-knight.c",
        (["type": "guild", "level":19]));
    addSpecification("scope", "self");
    addSpecification("research type", "points");
    addSpecification("research cost", 1);

    addSpecification("bonus defense", 2);
    addSpecification("bonus attack", 2);

    addSpecification("affected research", ([
        "Form of the Mountain": 25,
        "Offensive Stance": 25
    ]));
    addSpecification("affected research type", "percentage");
}
