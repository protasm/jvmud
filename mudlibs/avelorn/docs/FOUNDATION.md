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

The current vertical slice writes identity, gender, class, level, experience,
base attributes, resources, currency, inventory blueprint ids, and equipment
assignments. It reconstructs live item Entities after login rather than saving
object references. Quest state uses the same scalar-safe serialization pattern;
later milestones add discovery state. A future
incompatible shape increments the version and migrates explicitly on restore.

Quest progress is serialized as stable quest ids, integer stages, and counters
inside the character snapshot. The runtime mapping is reconstructed at login.
Stage `1` is active, `2` is ready to report, and `3` is complete; rewards are
issued only during the transition from `2` to `3`.

Repeated objectives use counters. Distinct exploration or interaction
objectives additionally store stable completion tags, preventing one Place or
interaction from satisfying a multi-location assignment more than once.
Combat objectives use the same protection unless a quest definition explicitly
marks its defeat tag repeatable, as the introductory granary clearance does.

## Combat contract

Combatants publish level and health so players can evaluate risk before
engaging. Level and equipment recommendations are soft gates: they alter
warnings and effectiveness but never prohibit an attempt. Hostile combatants
remember distinct contributors, and every contributor still present when the
opponent falls receives victory credit. Defeat is recoverable: Crown wardens
return the character to the Brindleford shrine, restore resources, and assess a
small coin loss.

Each class has an initial resource-powered technique: a Fighter's mighty blow,
a Ranger's aimed shot, a Mage's arcane bolt, or a Cleric's Lantern smite.
Techniques use the class's primary attribute and the same soft equipment
effectiveness rules as ordinary attacks. Class resources recover at shrines.

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
