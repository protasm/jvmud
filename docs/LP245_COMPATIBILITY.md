# Original Lysator LP245: current JVMud assessment

This assessment concerns `mudlibs/lp245`, extracted from the original Lysator
2.4.5 archive. All 582 original files remain byte-identical and read-only.
Experiments used disposable copies under `target/test-artifacts/lp245-assessment/`;
Original source was not changed. The boot-propagation fix described below has
since been implemented and verified.

## Verified current state

| Check | Result |
| --- | --- |
| Compile every original `.c` file, with the manifest registered directly | 255 of 286 compile; 31 fail |
| Compile the actual `room/init_file` list | 4 of 8 compile; 4 fail |
| Normal `MudInstance.boot` after the propagation fix | Boots to the church; 4 preloads skipped; login remains blocked |
| Diagnostic copy with bridge-object declaration merge bypassed | Boots to the church; 4 preloads skipped; login not established |
| Diagnostic copy additionally declaring `mixed short();` on the living base | 256 of 286 compile; 30 fail; further player and monster errors exposed |

The archive's preload file contains only eight objects. Its compile count is not
an estimate of how much of the world is playable. The all-source scan checks
compilation, not reset execution, session attachment, movement, or persistence.

## 1. Repair boot propagation of the transpiler setting

**Implemented.** `MudlibBoot` now registers the manifest's compilation context
before loading the bridge declaration object and preserves
`transpileUntypedMethods` when merging bridge declarations. The absent/false
setting remains strict; bridge declarations do not implicitly enable translation.

`MudlibBootTranspilationTest` verifies hosted startup with both typed and untyped
bridge objects, manifest aliases during bridge compilation, successful untyped
preload and initial-room execution, and rejection when the switch is absent or
false. The actual unmodified LP245 bridge now boots to the church with four
skipped preloads: `obj/quicktyper`, `room/adv_guild`, `obj/living`, and `obj/monster`.
This is startup verification, not a successful player login.

Evidence: `after-boot-propagation.log` under the generated assessment directory.
Earlier logs document the original merge failure and disposable-copy experiments.

## 2. Support the remaining legacy declarations through explicit translation

The untyped-method transformation adds missing method signature types. A
separate checked field-override facility is now implemented:
`transpilation.overrides = transpilation.json`. The bridge JSON currently changes
only `obj/torch.c`'s `amount_of_fuel` from `string` to `int` in memory. The original
torch now compiles and its numeric fuel/value behavior passes an execution test.
All-source compilation improves from 254/286 to 255/286; four startup preloads
still fail. Evidence: `after-field-override.log` under the assessment directory.

This archive also uses scalar-looking declarations for arrays:

- `obj/player.c`: `object list` receives `users()` and is indexed.
- `obj/quicktyper.c`: `object list_ab` and `list_cmd` hold arrays.
- `room/room.c`: `string dest_dir`, `items`, and `numbers` hold array values.
- `room/adv_guild.c`, `room/death/death_room.c`, boards, and tools have similar cases.
- `obj/torch.c` declares `amount_of_fuel` as `string` but performs numeric arithmetic.

Extend the checked bridge-owned field overrides to these declarations only
after verifying their intended types; consider general rules only for proven
legacy conventions. Produce ordinary explicit array, numeric, or mixed declarations for
the existing compiler. Do not reinterpret all declared types under the existing
untyped-method switch or silently disable type checking.

Other syntax/behavior accommodations include:

- `obj/trace.c` and `obj/trace2.c` define `in(str)`, conflicting with JVMud's `in`
  keyword. A method-name translation must also preserve string-based action lookup.
- `obj/master.c` uses cast syntax that the current parser rejects. This driver
  bootstrap object is not selected by our bridge and is not an initial login gate.
- `room/storage.c` passes an argument to a zero-argument inherited `init` method.
  Decide and test an explicit legacy calling convention rather than globally
  relaxing strict arity checks.

## 3. Resolve inheritance and generated-code consistency

`obj/living.c` calls `short()` from `show_stats`, while player and monster classes
supply the implementation. JVMud analyzes the base independently and rejects
that call. A typed virtual declaration or a deliberate dynamic-dispatch adapter
is needed. A disposable-copy experiment adding `mixed short();` resolved this
first error, but did not make player or monster compilation succeed.

That experiment exposed:

- Remaining array-declaration errors in the player object.
- An incompatible `can_put_and_get` override: the base returns a comparison,
  while the child returns integer 0/1. Both signatures were transpiled to `mixed`,
  but inferred signatures differ. Audit stable declared signatures versus inferred
  implementation types; a well-formed mixed-signature regression should decide
  whether this is a compiler defect rather than requiring a mudlib exception.
- More scalar/array mismatches in monster conversation data.

Retained world-loading tests also expose `NoSuchFieldError` for
`room.room`'s `java.util.List dest_dir`. Parent/child generated field descriptors
must agree. An accepted compilation should not fail with a JVM linkage error;
fix consistent field typing/code generation or report a source error before
bytecode emission. This is generic compiler correctness, not permission to accept
untyped source in native mode.

## 4. Finish the bridge's runtime behavior

Several wrappers compile but are placeholders or only approximate the required
behavior. Prioritize login, lookup, movement, and interaction before wizard tools.

| Surface | Existing starting point | Work |
| --- | --- | --- |
| `find_player` | `jvmud_find_player` exists; wrapper currently returns zero | Replace the placeholder after checking name/visibility semantics |
| `mkdir`, `rm`, `rmdir` | Native create/remove filesystem efuns exist | Map or forward them with the selected write policy |
| `crypt` | Native password hashing and verification exist | Implement an adapter matching LP245's `crypt(input, storedHash)` comparison idiom; the current wrapper returns cleartext |
| `previous_file` | Previous-object and object-ID efuns exist | Compose a bridge helper and verify caller depth/path conventions |
| `people` | Active-user enumeration exists | Supply the expected presentation/command behavior |
| `get_dir` | Native path listing exists | Verify and adapt listing contract if needed by reachable objects |
| `file_size` | Wrapper always returns 1 | Add real byte-size and missing-file/directory results; current native string length is not an equivalent |
| `transfer` | Wrapper moves directly and always reports success | Implement containment, weight, permission, and result-code behavior expected by inventory operations |
| `say`, `shout`, `command` | Native messaging/dispatch exists | Verify current-object versus current-actor semantics, recipients, and NPC behavior; `shout` currently only writes locally |
| bit operations, `filter_objects`, `creator` | Existing wrappers are incomplete | Implement real behavior where used; do not treat compile success as completion |
| `add_worth`, `wizlist`, `create_wizard` | Accounting/provisioning incomplete or absent | Implement bridge policy and reusable services as needed; wizard provisioning can follow basic play |

`obj/master.c` and `obj/simul_efun.c` are upstream driver support, not automatically
required JVMud bootstrap objects. Provide the services the actual world calls;
do not implement their whole old driver API solely for a perfect source-scan count.

## 5. Separate source defects and optional developer tools

Some failures need explicit, reviewable source adaptations or replacements rather
than a general language rule:

- `room/south/sforst19.c` calls the misspelled `this_palyer`.
- `obj/team.c` uses undeclared `vec` and has other incomplete logic.
- `room/def_castle.c` expects template substitutions for `NAME` and `DEST`.
- `players/lars/xx.c` calls driver debugging functionality (`combine_free_list`).
- `room/test.c` calls undefined `bepa`.

A bridge can supply an alias for a misspelling or own a reviewed replacement for
an incomplete object. Any per-source transformation must identify its target and
preserve source provenance. Template objects and obsolete experiments should be
tracked separately from reachable player/world requirements. None were removed
from the archive or silently ignored during the all-source scan.

## 6. Enable and verify persistence deliberately

The originals are still read-only. Needed data locations include player saves,
banishment records, post/mail data, bulletin boards, and logs. Select writable
data destinations or bridge-owned routing while protecting source files.

The current `LpcObjectStateStore` supports scalar state in JVMud JSON and an older
typed-properties format. The archive's original `.o` files use a different LPC
save format. Existing saved characters cannot be assumed to restore correctly;
add an importer/decoder if retaining them is required. Arrays needed for mail,
boards, and player state also require durable serialization support. New-account
save/restart/restore should be a separate acceptance test from compilation.

Wizard creation and room-building tools write source and preload files. They need
an explicit policy for generated content under the bridge rather than opening
write access to the original source tree.

## Recommended implementation order and acceptance gates

1. **Boot integration — complete:** hosted startup preserves the switch;
   untyped fixtures boot with it on and fail with it off.
2. **Player and room foundations:** resolve living-base virtual calls, legacy
   array declarations, and stable inherited field/method types. Compile and load
   the player, monster, room base, church, and adjacent rooms.
3. **First playable session:** complete password and player-lookup adapters;
   demonstrate login, look, movement, speech, inventory, combat, death, and quit.
4. **Persistence:** demonstrate a newly created character surviving server
   restart, then mail/boards and other needed collection state. Preserve original
   checksums throughout.
5. **World completion and wizard tools:** follow reachable failures, implement
   remaining bridge services and explicit source adaptations, and replace stale
   regression expectations with tests against this archive.

No reliable effort estimate follows just from the file count. The boot setting
is a small fix; inherited typing and legacy declaration translation are the main
technical uncertainties. Reassess after the first player-and-room milestone.

## All-source failures

- `obj/explore_xp.c`
- `obj/leo.c`
- `obj/living.c`
- `obj/mag_stone.c`
- `obj/marker.c`
- `obj/master.c`
- `obj/monster.c`
- `obj/monster.talk.c`
- `obj/player.c`
- `obj/quicktyper.c`
- `obj/roommaker.c`
- `obj/shut.c`
- `obj/simul_efun.c`
- `obj/team.c`
- `obj/trace.c`
- `obj/trace2.c`
- `players/lars/board.c`
- `players/lars/rand.c`
- `players/lars/xx.c`
- `players/lars/yy.c`
- `room/adv_guild.c`
- `room/death/death.c`
- `room/death/death_room.c`
- `room/def_castle.c`
- `room/mine/tunnel3.c`
- `room/mine/tunnel9.c`
- `room/port_castle.c`
- `room/shop.c`
- `room/south/sforst19.c`
- `room/storage.c`
- `room/test.c`
