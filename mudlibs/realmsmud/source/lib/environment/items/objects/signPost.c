//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************
inherit "/lib/environment/items/baseItem.c";

/////////////////////////////////////////////////////////////////////////////
public void Setup()
{
    Name("signpost");

    addDescriptionTemplate("a signpost");

    addItemTemplate(
        "the signpost is a weathered wooden post set beside the road. Several "
        "directional arms point toward nearby settlements, their carved "
        "lettering darkened by age and exposure to the weather"
    );
}
