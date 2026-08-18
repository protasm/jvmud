//*****************************************************************************
// Copyright (c) 2017-2026 - Allen Cummings, RealmsMUD, All rights reserved. See
//                      the accompanying LICENSE file for details.
//*****************************************************************************

private mapping verses = ([
    "weakness": "I am weak",
    "strength": "I am strong",
    "flame": "Burning",
    "frost": "Freezing",
    "aegis": "Hiding behind inconsequential armor",
    "negation": "I negate my greatest fears",
    "senses": "Unseeing, unhearing",
    "wisdom": "Feeling wise beyond my years",
    "endurance": "Pushed past endurance",
    "resistance": "Resisting no longer",
    "death": "I feel death's loving caress",
    "torment": "Dispel my torment",
    "doom": "My doom awaits me",
    "phantom": "My phantoms assail me",
    "fear": "Held in my tracks by fear",
    "domination": "Dominated by my inadequacies",
    "sanctuary": "I find no sanctuary",
    "envy": "I long for freedom restored"
]);

private mapping colors = ([
    "description": ([
        "none": "",
        "grayscale": "\x1b[0;38;5;250m",
        "3-bit": "\x1b[0;33m",
        "8-bit": "\x1b[0;38;5;42m",
        "24-bit": "\x1b[0;38;2;10;200;100m"
    ]),
    "border":([
        "none": "",
        "grayscale": "\x1b[0;38;5;234m",
        "3-bit": "\x1b[0;35m",
        "8-bit": "\x1b[0;38;5;23m",
        "24-bit": "\x1b[0;38;2;0;85;90m"
    ]), 
    "message": ([
        "none": "",
        "grayscale": "\x1b[0;38;5;245m",
        "3-bit": "\x1b[0;35;1m",
        "8-bit": "\x1b[0;38;5;126m",
        "24-bit": "\x1b[0;38;2;170;20;180m"
    ]), 
    "missing": ([
        "none": "",
        "grayscale": "\x1b[0;38;5;237m",
        "3-bit": "\x1b[0;31m",
        "8-bit": "\x1b[0;38;5;124m",
        "24-bit": "\x1b[0;38;2;180;0;0m"
    ]), 
    "placed": ([
        "none": "",
        "grayscale": "\x1b[0;38;5;248;1m",
        "3-bit": "\x1b[0;35;1m",
        "8-bit": "\x1b[0;38;5;128;1m",
        "24-bit": "\x1b[0;38;2;190;90;210;1m"
    ]),
    "instructions": ([
        "none": "",
        "grayscale": "\x1b[0;38;5;250;1m",
        "3-bit": "\x1b[0;32;1m",
        "8-bit": "\x1b[0;38;5;2;1m",
        "24-bit": "\x1b[0;38;2;160;220;60;1m"
    ]),
]);

private mapping wall = ([]);

private mapping RuneBits = ([
    "weakness": 1,
    "strength": 2,
    "flame": 4,
    "frost": 8,
    "negation": 16,
    "wisdom": 32,
    "endurance": 64,
    "resistance": 128,
    "death": 256,
    "doom": 512,
    "fear": 1024,
    "envy": 2048,
]);

/////////////////////////////////////////////////////////////////////////////
public string Name()
{
    return "test of obedience rune wall";
}

/////////////////////////////////////////////////////////////////////////////
private string *recordedRunes(object player)
{
    string *ret = ({});
    string state = (objectp(player) && function_exists("characterState", player)) ?
        player->characterState(this_object()) : 0;

    if (stringp(state) && (strstr(state, "placed:") == 0))
    {
        string encoded = state[7..];
        if (strstr(encoded, ",") > -1)
        {
            // Read checkpoints written by the initial JVMud compatibility
            // implementation before it switched to a bounded bit set.
            ret = explode(encoded, ",");
        }
        else
        {
            int placed = to_int(encoded);
            foreach(string rune in m_indices(RuneBits))
            {
                if (placed & RuneBits[rune])
                {
                    ret += ({ rune });
                }
            }
        }
    }
    return ret;
}

/////////////////////////////////////////////////////////////////////////////
public int hasRecordedRune(object player, string rune)
{
    return objectp(player) && stringp(rune) &&
        (member(recordedRunes(player), rune) > -1);
}

/////////////////////////////////////////////////////////////////////////////
private string encodeRunes(string *runes)
{
    int placed = 0;
    foreach(string rune in runes)
    {
        if (member(RuneBits, rune))
        {
            placed |= RuneBits[rune];
        }
    }
    return sprintf("placed:%d", placed);
}

/////////////////////////////////////////////////////////////////////////////
private object *participatingPlayers()
{
    object party = this_player()->getParty();
    return objectp(party) ? party->members(1) : ({ this_player() });
}

/////////////////////////////////////////////////////////////////////////////
private void recordPlacedRune(string rune)
{
    foreach(object player in participatingPlayers())
    {
        if (objectp(player) && function_exists("characterState", player))
        {
            string *placed = recordedRunes(player);
            if (member(placed, rune) < 0)
            {
                placed += ({ rune });
                player->characterState(this_object(), encodeRunes(placed));
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
private void restorePlacedRunes(object player)
{
    string *placed = recordedRunes(player);
    object room = environment(this_object());
    object stateMachine = objectp(room) ? room->stateMachine() : 0;
    string questState = objectp(stateMachine) ?
        stateMachine->getCurrentState(player) : 0;

    // Every state from the first test onward is only reachable after the
    // resistance rune has already been placed. This also repairs older saves
    // made before the wall recorded its own contents.
    if (member(({ "first test", "second test", "third test", "fourth test",
        "fifth test", "sixth test", "seventh test", "poem complete",
        "quest complete" }), questState) > -1)
    {
        placed += ({ "resistance" });
    }

    foreach(string rune in m_indices(mkmapping(placed)))
    {
        if (member(wall, rune) && member(verses, rune))
        {
            wall[rune, 0] = verses[rune];
            wall[rune, 1] = 0;
        }
    }

    // The wall checkpoint is written before the quest transition. If the
    // process stops between those operations, reconcile the lagging quest
    // when the player next enters instead of leaving a completed wall inert.
    if ((questState == "seventh test") && allRunesPlaced())
    {
        stateMachine->receiveEvent(player, "allRunesPlaced");
    }
}

/////////////////////////////////////////////////////////////////////////////
public void resetWall()
{
    wall = ([
        "weakness": "<missing>"; 1,
        "strength": "<missing>"; 1,
        "flame": "<missing>"; 1,
        "frost": "<missing>"; 1,
        "aegis": "Hiding behind inconsequential armor"; 0,
        "negation": "I <missing> fears"; 1,
        "senses": "Unseeing, unhearing"; 0,
        "wisdom": "<missing> my years"; 1,
        "endurance": "Pushed <missing>"; 1,
        "resistance": "<missing> no longer"; 1,
        "death": "I feel <missing> loving caress"; 1,
        "torment": "Dispel my torment"; 0,
        "doom": "<missing> awaits me"; 1,
        "phantom": "My phantoms assail me"; 0,
        "fear": "Held <missing>"; 1,
        "domination": "Dominated by my inadequacies"; 0,
        "sanctuary": "I find no sanctuary"; 0,
        "envy": "<missing> restored"; 1
    ]);
}

/////////////////////////////////////////////////////////////////////////////
public int canTransToEndurance()
{
    return (wall["resistance", 1] == 0);
}

/////////////////////////////////////////////////////////////////////////////
public int allRunesPlaced()
{
    int *items = m_values(wall, 1);
    items -= ({ 0 });

    return (sizeof(items) == 0);
}

/////////////////////////////////////////////////////////////////////////////
public string getPiecesMissing()
{
    string ret;

    int *items = m_values(wall, 1);
    items -= ({ 0 });

    switch (sizeof(items))
    {
        case 1: 
        {
            ret = "is one gap";
            break;
        }
        case 2: 
        {
            ret = "are two gaps";
            break;
        }
        case 3: 
        {
            ret = "are three gaps";
            break;
        }
        case 4: 
        {
            ret = "are four gaps";
            break;
        }
        case 5: 
        {
            ret = "are five gaps";
            break;
        }
        case 6: 
        {
            ret = "are six gaps";
            break;
        }
        case 7: 
        {
            ret = "are seven gaps";
            break;
        }
        case 8: 
        {
            ret = "are eight gaps";
            break;
        }
        case 9: 
        {
            ret = "are nine gaps";
            break;
        }
        case 10: 
        {
            ret = "are ten gaps";
            break;
        }
        case 11: 
        {
            ret = "are eleven gaps";
            break;
        }
        case 12: 
        {
            ret = "are twelve gaps";
            break;
        }
        default: 
        {
            ret = "";
            break;
        }
    }
    return ret;
}

/////////////////////////////////////////////////////////////////////////////
public void create()
{
    resetWall();
}

/////////////////////////////////////////////////////////////////////////////
public void init()
{
    restorePlacedRunes(this_player());
    add_action("placeRune", "place");
}

/////////////////////////////////////////////////////////////////////////////
public int id(string item)
{
    return ((item == "runes") || (item == "wall") || (item == "rune wall"));
}

/////////////////////////////////////////////////////////////////////////////
private void applyExperience()
{
    string colorConfiguration = this_player()->colorConfiguration();
    object configuration = getService("configuration");

    object party = this_player()->getParty();
    if (objectp(party))
    {
        object* members = party->members(1);
        foreach(object member in members)
        {
            if (objectp(member))
            {
                colorConfiguration = member->colorConfiguration();
                member->addExperience(50, "background", 1);
                tell_object(member, configuration->decorate(
                    "You have gained 50 experience.\n", "level up", "score",
                    colorConfiguration));
            }
        }
    }
    else
    {
        this_player()->addExperience(50, "background", 1);
        tell_object(this_player(), configuration->decorate(
            "You have gained 50 experience.\n", "level up", "score",
            colorConfiguration));
    }
}

/////////////////////////////////////////////////////////////////////////////
public int placeRune(string rune)
{
    int ret = 0;

    // For now, we'll assume that this is a rune
    object runeToAdd;
    if (rune)
    {
        runeToAdd = present(rune) || present(rune, this_player());
        if (present(rune))
        {
            environment()->canGet(runeToAdd);
        }
    }

    if (runeToAdd && runeToAdd->isObedienceRune())
    {
        string whichRune = runeToAdd->getRuneType();
        destruct(runeToAdd);
 
        if (member(wall, whichRune) && member(verses, whichRune))
        {
            wall[whichRune, 0] = verses[whichRune];
            wall[whichRune, 1] = 0;
            recordPlacedRune(whichRune);
            string msg = "##InitiatorName::capitalize## ##Infinitive::locate## a gap that "
                "fits and ##Infinitive::place## the rune of %s on the wall.\n";

            object configuration =
                getService("configuration");

            object messageParser = load_object("/lib/core/messageParser.c");
            messageParser->displayMessage(sprintf(msg, whichRune), this_player(), 0,
                "rune wall", "tutorial");

            if (whichRune == "resistance")
            {
                object stateMachineService = getService("stateMachine");

                object party = this_player()->getParty();
                string owner = party ? party->partyName() : this_player()->RealName();

                object stateMachine = stateMachineService->getStateMachine(
                    "/areas/tol-dhurath/state-machine/obedience-quest.c",
                    owner);

                if (stateMachine)
                {
                    stateMachine->receiveEvent(this_player(), "resistanceRunePlaced");
                }
            }

            if (allRunesPlaced())
            {
                messageParser->displayMessage(
                    "As the final rune slides into place, the entire wall "
                    "pulses with blinding light. The poem is whole. A deep "
                    "rumble shakes the chamber and the pedestals flare "
                    "with renewed energy. A hidden path awaits.\n",
                    this_player(), 0, "rune wall", "tutorial");

                object stateMachineService = getService("stateMachine");
                applyExperience();

                object party = this_player()->getParty();
                string owner = party ? party->partyName() : this_player()->RealName();

                object stateMachine = stateMachineService->getStateMachine(
                    "/areas/tol-dhurath/state-machine/obedience-quest.c",
                    owner);

                if (stateMachine)
                {
                    stateMachine->receiveEvent(this_player(), "allRunesPlaced");
                }
            }
            ret = 1;
        }
    }
    return ret;
}

/////////////////////////////////////////////////////////////////////////////
public string short()
{
    return "A wall with many runes etched into it";
}

/////////////////////////////////////////////////////////////////////////////
private string runeEntry(string message, string colorConfiguration)
{
    string ret = message;

    if (sizeof(regexp(({ message }), "<missing>")))
    {
        ret = colors["message"][colorConfiguration] + 
            regreplace(sprintf("%-35s", ret),
            "(<missing>)",
            colors["missing"][colorConfiguration] + "\\1" +
            colors["message"][colorConfiguration], 1);
    }                            
    else
    {
        ret = colors["placed"][colorConfiguration] + sprintf("%-35s", ret);
    }

    return "\t\t" + colors["border"][colorConfiguration] + "|   " + ret +
        colors["border"][colorConfiguration] + "   |" +
        ((colorConfiguration == "none") ? "" : "\x1b[0m") + "\n";
}

/////////////////////////////////////////////////////////////////////////////
public string long()
{
    string colorConfiguration = 
        (this_player() && this_player()->colorConfiguration()) ?
        this_player()->colorConfiguration() : "none";

    string closing = (colorConfiguration == "none") ? "" : "\x1b[0m";

    string missing = getPiecesMissing();
    if (missing != "")
    {
        missing = sprintf(" There %s in the runes%s.",
            missing, (missing == "is one gap") ? "" :
            ", each shaped differently as though pieces to a "
            "puzzle were missing");
    }

    string poem = runeEntry(wall["weakness"], colorConfiguration) +
        runeEntry(wall["strength"], colorConfiguration) +
        runeEntry(wall["flame"], colorConfiguration) +
        runeEntry(wall["frost"], colorConfiguration) +
        runeEntry(wall["aegis"], colorConfiguration) +
        runeEntry(wall["negation"], colorConfiguration) +
        runeEntry("", colorConfiguration) +
        runeEntry(wall["senses"], colorConfiguration) +
        runeEntry(wall["wisdom"], colorConfiguration) +
        runeEntry(wall["endurance"], colorConfiguration) +
        runeEntry(wall["resistance"], colorConfiguration) +
        runeEntry(wall["death"], colorConfiguration) +
        runeEntry(wall["torment"], colorConfiguration) +
        runeEntry("", colorConfiguration) +
        runeEntry(wall["doom"], colorConfiguration) +
        runeEntry(wall["phantom"], colorConfiguration) +
        runeEntry(wall["fear"], colorConfiguration) +
        runeEntry(wall["domination"], colorConfiguration) +
        runeEntry(wall["sanctuary"], colorConfiguration) +
        runeEntry(wall["envy"], colorConfiguration);

    string long = sprintf("%sYou gaze at the wall of runes. As you "
        "decipher them, you note that they form a poem.%s\n\nAs near "
        "as you can tell, this is what the runes spell out:\n"
        "\t\t%s+-----------------------------------------+%s\n%s"
        "\t\t%s+-----------------------------------------+%s\n%s%s%s",
        colors["description"][colorConfiguration],
        missing,
        colors["border"][colorConfiguration], closing,
        poem,
        colors["border"][colorConfiguration], closing,
        colors["instructions"][colorConfiguration],
        (missing == "") ? "" : "If you find some runes, perhaps you can place"
        " them...",
        closing);
    return format(long, 78);
}
