# JVMud Glossary

This glossary defines JVMud engine vocabulary. It is meant to keep engine
concepts distinct from legacy LPC/LPMud compatibility names.

## Core Concepts

### Game

The playable system JVMud hosts. A Game has rules, mechanics, goals,
challenges, progression, or other game-like structures.

### Text

The primary medium through which the World is perceived and manipulated.

### Interactive

The property that Players can affect the World and the World can affect
Players.

### Multiplayer

The property that multiple Players inhabit the same World at the same time.

### World

The complete shared virtual domain of the Game. A World is composed of Places,
Links, Entities, Locations, containment, and Movement.

### Persistence

The property that the World and its state endure independently of any individual
Session.

Persistence includes two related modes:

World Continuity is the core MUD pillar: the World remains live and coherent
independently of any particular Player's connection.

Save/Restore State is durable state for selected Player, account, Entity, or
mudlib-defined data that is outside active World temporality until restored.

Legacy LPC `save_object` and `restore_object` are compatibility vocabulary for
saving and restoring LPC object state. `Object` is LPC/LPMud vocabulary, not a
JVMud engine concept.

### Temporality

The property that the World can change through time.

### Presence

The core MUD requirement that a player experiences the game from within the
World.

Presence is created when a player engages the World through a located Persona.
It is experiential, not merely mechanical. Location says where an Entity is;
Presence says that a player is experiencing the World through that located
Entity.

Many Entities have Location without creating Presence: tools, doors, dropped
objects, environmental features, and many NPCs are in the World, but they are
not necessarily a player's point of perception and action.

## World Model

### Place

A discrete location in the game space where Entities may exist, be perceived, or
interact. Legacy mudlibs may call these rooms, but `Place` is the JVMud engine
concept.

### Link

A traversable relationship between Places. Links make the World navigable.

### Entity

A thing in the World.

Examples include players, coins, swords, bags, vehicles, NPCs, effects, and
environmental features.

### Location

The mechanical relationship that says where an Entity is.

Every Entity has exactly one immediate Location. A Location is either a Place or
another Entity.

### Containment

The location relationship in which one Entity is inside, carried by, mounted on,
or otherwise immediately located in another Entity.

### Single Containment

The rule that every Entity has exactly one immediate Location. This lets JVMud
model inventories, containers, vehicles, nested containment, and location
resolution without ambiguous ownership.

### Movement

The transition of an Entity from one Location to another. Place-to-place
Movement usually happens through Links, but Movement can also place an Entity
inside another Entity.

## Player And Connection Model

### Player

The human person playing the Game from outside the fictional World.

A Player interacts with JVMud through a Session and experiences the World
through a Persona. The Player is not an Entity inside the World; the Persona is
the in-world Entity that creates Presence for that Player.

### Session

An active connection/control context, such as a telnet connection.

A Session is not itself an Entity. It is the live connection between JVMud and a
Player, and it controls or observes the World through a Persona.

### Persona

The Entity currently associated with a Session as that Session's in-world
perspective.

A Persona is the player's current point of perception and action. It must have a
Location for Presence to exist. A Persona may be located in a Place or in
another Entity if the game supports containment, vehicles, possession, carried
objects, or similar structures.

A Persona is a specific role for an Entity: it is linked to a Player through a
Session. A Persona is usually also an Actor because Player commands can cause it
to act, but Actor is a broader capability that also includes NPCs, traps,
schedulers, scripted effects, and other Entities that initiate change.

### Player Object

The LPC-authored mudlib object that gives a Persona its behavior.

This is mudlib and compatibility vocabulary, especially for legacy LP mudlibs.
It should not be confused with a JVMud Player, who is the human person outside
the World, or with the Persona, which is the Entity through which Presence
occurs. A player object is an LPC implementation attached to a Persona.

### Player Connection Lifecycle

The engine sequence that attaches a Session to a Persona, ensures that the
Persona has a live player object implementation, gives the Persona a Location,
refreshes interaction scope, routes input, and later disconnects or saves the
Session association.

### Interaction Scope

The local command and perception context around a Persona. It includes the
Persona, the Persona's Location, carried Entities, and nearby Entities that may
register local interactions.

### Command Dispatch

The engine process that takes a line of Player input, identifies the verb,
sets the active command context, and invokes mudlib behavior registered for that
Interaction Scope.

## Capability Model

### Perceptive

The capability to receive information about the World.

### Actor

The capability to initiate change within the World.

Actor describes what an Entity can do, not whose perspective it represents. A
Persona may have the Actor capability when the linked Player can cause it to act
through command input, but not every Actor is a Persona.

## LPC/LPMud Target

### LPC

The language used by JVMud's sole mudlib target.

### LPMud

For JVMud, an LPMud is a MUD whose game world is authored in LPC, compiled into
live game objects, and can be rewritten, recompiled, and reloaded without
rebooting the whole Game.

This does not mean JVMud adopts legacy driver concepts such as rooms,
heartbeats, applies, call_outs, or master objects as engine concepts.

### Mudlib

The LPC source/content layer that defines world fiction, object behavior,
commands, rules, and presentation.

### Compatibility Shim

A mudlib-side object dedicated to translating legacy LPC/LPMud vocabulary into
JVMud-native engine requests.

### Mfun

A mudlib-global function provided by a configured mudlib object. Mfuns are the
JVMud equivalent of the traditional simul_efun role, but implemented as an
explicit boundary object rather than a legacy driver singleton.

### Efun

An engine function callable from compiled LPC code. JVMud efuns should expose
engine operations without making legacy LPC names into engine ontology.

### Lifecycle Hook

A configured engine-to-mudlib call for a JVMud lifecycle event. Hook method
names are mudlib boundary configuration, not engine concepts.

### Live Reload

The LPMud-target requirement that LPC-authored objects can be rewritten,
recompiled, and reloaded while the Game continues running.

## Legacy Compatibility Terms

### Room

A legacy mudlib term for a location-like object. JVMud's engine concept is
Place.

### Heartbeat

A legacy driver concept for recurring object callbacks. JVMud's engine concept
is Temporality and scheduled time.

### Call Out

A legacy driver concept for delayed callbacks. JVMud treats delayed work as a
mudlib request to the engine's Temporality model.

### Apply

A legacy driver term for driver-invoked mudlib methods. JVMud uses explicit
Lifecycle Hooks.

### Master Object

A legacy driver policy object. JVMud uses explicit boundary configuration and
compatibility shims.
