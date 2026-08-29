# Avelorn foundation contract

This document fixes the first native implementation decisions that later
content may rely on.

## Kingdom and campaign

The game id is `avelorn` and the launch campaign is *The Lantern Crown*.
Avelorn begins as a cohesive and competently governed kingdom. The Crown,
chartered settlements, temples, guilds, and royal services cooperate against a
failing ancient ward; civil collapse is not the launch premise.

The player's institutional home is the Crown-chartered Company of the Lantern.
The first explorable settlement is Brindleford. Greyhaven is the regional town,
and Ashenwatch Keep is the level 8-10 campaign destination.

## Stable identifiers

Stable ids are lowercase slash-separated paths or lowercase hyphenated keys.
They are never display names.

- Classes: `fighter`, `ranger`, `mage`, `cleric`.
- Genders: `male`, `female`, `non-binary`.
- Quests: `millers-unwelcome-guests`, `light-for-the-road`,
  `silent-patrol-bell`, `beneath-blackstone`, `rekindle-western-lantern`.
- Places begin under `place/` and item blueprints will begin under `item/`.

Identifiers may gain aliases during migrations, but an identifier already
written to a character snapshot must not be silently reassigned.

## Character snapshot version 1

The first save shape keeps account authentication and one character in the
same LPC object snapshot while preserving separate fields for their meanings.
The durable fields are:

- account id and password hash;
- character name, gender, and class;
- save format version;
- level and experience;
- six base attributes;
- current and maximum class resources;
- currency;
- stable item blueprint ids and equipment assignments;
- quest stages and counters;
- discovered Place ids and last safe Place id.

The initial vertical slice writes the identity, gender, class, level, and base
attributes. Later milestones add the remaining version-1 fields without saving
live LPC object references. A future incompatible shape increments the version
and migrates explicitly on restore.

Plaintext passwords must never reach durable fields. Runtime-only pending input
is cleared before every save.

## Gender and language

Gender never changes attributes, classes, equipment, quests, or progression.
All Personas and NPCs use one of the three stable gender keys. The shared
pronoun service supplies subject, object, possessive adjective, possessive
pronoun, reflexive, and verb-agreement forms at presentation time. Stored prose
must not bake in a character's pronouns.

## Native boundary

JVMud owns sessions, Players, Personas, presence, Places, Entities, containment,
movement, time, and persistence mechanics. Avelorn owns authentication policy,
character rules, combat, items, quests, NPC behavior, economy, and fiction.

Avelorn uses native lifecycle names such as `initialize`,
`offer_interactions`, `begin_session`, and `end_session`. New Java behavior is
appropriate only when it expresses a generally reusable JVMud capability
rather than an Avelorn rule.

## Filesystem-only persistence

Avelorn does not use a database. Accounts, characters, inventory blueprints,
equipment state, quest progress, and future durable world checkpoints are
stored beneath the selected Avelorn mudlib root through JVMud's host-filesystem
persistence capability. The Avelorn manifest must not request database access,
and Avelorn source must not call database engine functions.

Character snapshots use atomic host-managed file writes once JVMud exposes
that general capability. Until then, Avelorn uses the existing isolated LPC
object-state files and treats an unsuccessful save as a persistence error to be
reported rather than silently ignored.
