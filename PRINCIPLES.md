JVMud
Master Statement of Guiding Principles
======================================

Purpose
-------

JVMud is a JVM-based engine built around eight core concepts:

    Game
    Text
    Interactive
    Multiplayer
    World
    Persistence
    Temporality
    Presence

Together these concepts define the kind of system JVMud exists to build.

JVMud is not intended to be a general-purpose simulation framework,
chat server, collaboration platform, or generic game engine.

JVMud is specifically an engine for creating and operating persistent,
multiplayer, text-based game worlds in the tradition of LP MUDs.

JVMud has one mudlib and language target: LPC for LPMud-style worlds. It is not
a multi-language MUD framework. Compatibility, tooling, compiler work, runtime
APIs, documentation, and tests should all assume this sole LPC/LPMud target.

In JVMud, "LPMud engine" means that the game world is authored in LPC, compiled
into game objects, and can be rewritten, recompiled, and reloaded while the game
continues running. It does not mean adopting legacy LPMud driver concepts such
as rooms, heartbeats, applies, call_outs, or master objects as JVMud engine
ontology. Those names may exist as LPC compatibility vocabulary, but JVMud's
engine concepts remain the concepts named here.

The Eight Core Concepts
-----------------------

1. Game

   The world exists to be played.

   JVMud assumes the existence of rules, mechanics, goals,
   challenges, progression, or other game-like structures.

2. Text

   Text is the primary medium through which the world is perceived
   and manipulated.

   Text is not an afterthought layered onto another medium.
   It is a first-class concern.

3. Interactive

   Participants can affect the world and the world can affect
   participants.

   Actions have consequences.

4. Multiplayer

   Multiple participants inhabit the same world simultaneously.

   The world is shared.

5. World

   The game takes place within a concrete virtual domain.

   Participants and entities occupy places and exist somewhere
   within the world.

   The World concept includes Linked Places, Entities, and Movement.

   Linked Places give the world traversable structure.

   Entities are the things that exist in that structure.

   Movement changes where an Entity is present.

6. Persistence

   The world and its state endure independently of any individual
   participant's session.

   A participant may disconnect and later return to a world whose
   state has continued to exist.

   Persistence does not require eternal continuity. Periodic
   reboots and resets are compatible with persistence provided
   that the world endures independently of individual sessions.

7. Temporality

   The world can change through time.

   Events occur.
   Actions have consequences.
   State evolves.

   Temporality is independent of Persistence.

8. Presence

   Participants perceive the world from within it.

   Presence is situated perception.

   Participants are somewhere, not everywhere.

   Presence creates locality of information and enables:

       Exploration
       Discovery
       Concealment
       Surprise
       Navigation
       Mystery

   Presence is central to the style of world simulation that
   JVMud is designed to support.

Core Philosophy
---------------

JVMud is intentionally opinionated.

The objective is not to support every conceivable kind of MUD.

The objective is to provide exceptional support for persistent,
multiplayer, text-based worlds in the LP tradition.

Concepts that appear repeatedly across the overwhelming majority
of such worlds belong in the engine.

Concepts that vary substantially between worlds belong in the
mudlib.

The engine provides the grammar of virtual reality.

The mudlib provides the fiction.

World
-----

A World is the spatial structure of a Game.

A World is composed of Places and the Links between them.

A Place represents a discrete location in the game space: a room, field, hallway,
shop, cave mouth, ship deck, or any other area where Entities may exist,
be perceived, or interact.

A Link represents a navigable relationship between Places.

Links are what make a collection of Places into a World. Without Links, Places
are merely isolated locations. With Links, they form a traversable spatial model:
a map, dungeon, city, wilderness, building, ship, dreamscape, or other simulated
environment.

A Link may be simple, such as:

    north from Kitchen to Hallway

or more complex, such as:

    climb ladder from Cellar to Attic
    enter portal from Shrine to Shadow Realm
    unlock iron gate from Courtyard to Garden
    swim downstream from River Bend to Waterfall Pool

Links may be directional or bidirectional. They may be visible or hidden,
open or closed, locked or unlocked, conditional or unconditional. A Link may
also describe the action required to traverse it.

The important concept is that spatiality in JVMud is not just containment.
It is connected containment.

Places hold Entities.
Links connect Places.
Together, they define the explorable World.

### Movement

Movement is the transition of an Entity from one Place to another through the
links that connect Places.

Because a World is composed of Linked Places, Entities do not merely exist in
isolated locations. They may move between Places by traversing the connections
the World defines. Movement changes an Entity's Presence: the Entity leaves one
Place and enters another.

In the simplest case, Movement is immediate: an Entity moves through an
available link and its Presence changes from the source Place to the destination
Place. More complex forms of movement, such as blocked movement, forced
movement, delayed travel, vehicles, portals, or scripted transitions, should be
understood as refinements of this same basic concept.

Movement should remain conceptually separate from decision-making. An Actor may
initiate movement, but not all movement is voluntary. A player may choose to
walk north; a trapdoor may drop an Entity into a cellar; a spell may move an
Entity to another Place. In all cases, Movement is the World-level change in
where an Entity is present.

Entity

    Something that exists within the world.

    Examples:

        Player
        Coin
        Sword
        Bag
        Carriage
        Dragon
        Spell Effect
        Aura
        Weather System

Location
--------

Every Entity has exactly one Location.

A Location is either:

    A Place
    Another Entity

Examples:

    Carriage.location = Muddy Lane

    Player.location = Carriage

    CoinBag.location = Player

    Coin.location = CoinBag

This rule establishes Single Containment.

Single Containment
------------------

Single Containment is a core JVMud rule.

Every Entity has exactly one immediate Location.

This enables:

    Inventories
    Containers
    Vehicles
    Nested containment
    Location resolution
    Movement propagation

The engine is responsible for maintaining containment integrity
and preventing containment cycles.

Example:

    Muddy Lane
        contains Carriage

    Carriage
        contains Player

    Player
        contains Coin Bag

    Coin Bag
        contains Coins

Movement of a parent propagates naturally to descendants.
Capabilities
------------

JVMud distinguishes between existence and capability.

Capabilities are not necessarily types.

An Entity may possess either, both, or neither.

Perceptive

    The capability to receive information about the world.

    A Perceptive entity can detect events, state, or conditions
    within its sphere of observation.

Actor

    The capability to initiate change within the world.

    An Actor can perform actions that affect entities, places,
    state, or events.

Perceptive and Actor are independent capabilities.

Capability Matrix
-----------------

Perceptive + Actor

    Receives information and initiates action.

    Examples:

        Player
        Merchant
        Dragon
        Thermostat
        Guard NPC
        Pressure Plate Trap

Perceptive Only

    Receives information but does not initiate action.

    Examples:

        Surveillance Crystal
        Security Camera
        Logging System
        Audit Trail
        Analytics Collector
        Passive Monitoring Sensor

Actor Only

    Initiates action but does not receive information.

    Examples:

        Clock
        Scheduled Event
        Timed Curse
        Scripted World Event
        Periodic Weather Effect

Neither

    Neither receives information nor initiates action.

    Examples:

        Coin
        Rock
        Tree
        Sword
        Wall

Design Philosophy
-----------------

JVMud intentionally avoids assumptions regarding:

    Consciousness
    Awareness
    Intelligence
    Subjective Experience
    Sentience

The engine concerns itself with observable simulation behavior.

Perceptive answers:

    "Can this entity receive information about the world?"

Actor answers:

    "Can this entity initiate change within the world?"

More sophisticated concepts may be introduced by individual
mudlibs, but they are not required by the core engine.

Perception
----------

Perception is location-relative.

What an entity can perceive depends upon where it is.

Presence and Perception are closely related.

A participant located in a carriage perceives a different
subset of the world than a participant located inside a bag
within that carriage.

The engine provides the structural model.

Mudlibs define specific rules governing:

    Visibility
    Lighting
    Sound
    Smell
    Concealment
    Detection
    Other perception systems

Core Engine Commitments
-----------------------

JVMud intentionally embraces a small number of strong
architectural commitments.

One Engine

    Hosts one Game.

One Game

    Contains one World.

One World

    Represents the complete shared virtual domain.

One Persistence Model

    Provides authoritative storage and recovery
    of world state.

One Temporality Model

    Provides authoritative progression of time
    and events.

One Player Population

    Represents the participants inhabiting the world.

One Canonical Text Model

    Provides the primary medium for perception
    and interaction.

These commitments favor clarity, understandability,
maintainability, and simplicity over premature
generalization.

Engine vs Mudlib
----------------

Engine Responsibilities

    World
    Place
    Entity
    Location
    Containment
    Presence
    Perceptive
    Actor
    Movement
    Perception
    Persistence
    Temporality

Mudlib Responsibilities

    Rooms
    Doors
    Coins
    Bags
    Weapons
    Quests
    Guilds
    Skills
    Combat
    Economies
    Visibility Rules
    Lighting Rules
    Social Systems
    Setting-Specific Mechanics

The engine should provide foundational reality.

The mudlib should define what exists within that reality.

Architectural Principles
------------------------

Clarity over cleverness.

Simplicity over abstraction.

Readability over novelty.

Maintainability over optimization.

Descriptive names over concise names.

Explicit behavior over implicit behavior.

Composition over unnecessary hierarchy.

Capabilities over deep inheritance trees.

Do not introduce complexity without a demonstrated need.

Optimize for understanding first.

A design that is easy to understand, debug, test, and maintain
is preferred to a more sophisticated design with marginal
benefits. Maintainability and understandability are key quality
attributes of successful software systems.

Guiding Principle
-----------------

JVMud models persistent, temporal, multiplayer text worlds
inhabited by participants who perceive those worlds from
within them.

The engine is responsible for providing the fundamental
reality of those worlds.

The mudlib is responsible for defining what exists within
them.
