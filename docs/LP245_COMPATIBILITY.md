# Original Lysator LP245: current JVMud assessment

This assessment concerns `mudlibs/lp245`, extracted from the original Lysator
2.4.5 archive. All 582 original files remain byte-identical and read-only.
Experiments used disposable copies under `target/test-artifacts/lp245-assessment/`;
Original source was not changed. The boot-propagation fix described below has
since been implemented and verified.

## Verified current state

| Check | Result |
| --- | --- |
| Compile every original `.c` file, with the manifest registered directly | 266 of 286 compile; 20 fail |
| Compile the actual `room/init_file` list | 5 of 8 compile; 3 fail |
| Normal `MudInstance.boot` with the current bridge | Boots to the church; 3 preloads skipped; login remains blocked |
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

- `obj/player.c`: the two local `object list` declarations now have checked
  `object*` overrides in `list_peoples()` and `who()`.
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

### Player locals: checked local overrides implemented

The JSON configuration now accepts `local_type_overrides`, independently of
field overrides. Each rule specifies `file`, `method`, `local`, `expected_type`,
and `replacement_type`. The two rules for `obj/player.c` select `list` in
`list_peoples()` and `who()`, translating `object` to `object*`.

Local overrides run on parsed declarations before semantic analysis. Parameters
and fields are excluded; ambiguous method/local names, missing declarations,
and unexpected original types fail at `TRANSPILE`. All selected rules are
validated before any local is changed. Grouped declarations, initializer order,
nested scopes, and included declarations retain their parsed identities.

The original player now compiles and loads. An execution regression invokes
`reset(0)`, binds a session, exercises both adapted methods against the nonempty
user list, and invokes `logon()`. The name prompt appears and its input handler
is registered. This establishes initial login setup, not completed account login,
authentication, or persistence.

The current scan is **266/286**, with the same three skipped preloads. Evidence:
`after-local-overrides.log` in the generated assessment directory. The local
transpiler and bridge suites pass all 12 tests. The full suite runs 519 tests
with 11 errors in retained LP245 compatibility/Telnet cases, including a login
flow timeout and stale source paths. All 582 original archive files are unchanged.

### Room foundation: checked field overrides implemented

The bridge now translates `room/room.c`'s `dest_dir`, `items`, and `numbers`
from `string` to `string*`. Their upstream comments and uses define arrays of
exit pairs, item/description pairs, and number words. `property` becomes `mixed`
because the original contract explicitly accepts either a string or an array.
All substitutions remain checked, source-specific rules in `transpilation.json`.

`Lp245BridgeTest.originalRoomBaseSupportsInheritedExitsItemsAndProperties`
loads the original parent before the original village green, executes its reset,
and verifies inherited exits, its short description, and shared number lookup.
A typed test child also verifies item lookup and switching property values between
an array and a string through a mixed setter. This removes the observed
`dest_dir` linkage failure on that real-room path without changing the compiler.
It does not establish generic inherited-field inference correctness: a child
assigning an array literal and then a string literal directly to a mixed field
still encounters inferred-array narrowing and requires separate compiler work.

Before enabling implicit self calls, the room-only assessment was **255/286**,
with hosted boot reaching the
church and the same four skipped preloads (`after-room-overrides.log` in the
generated assessment directory). The value of this step is executable
inherited-room behavior; login is still blocked. All 582 archive checksums match.
At that stage, 27 focused bridge/transpiler tests passed. The full suite ran 505 tests
with one failure and 14 errors in retained LP245 compatibility/Telnet tests,
including stale `source/` paths and unresolved living/array/syntax behavior;
it is not a passing gameplay acceptance suite.

## 3. Resolve inheritance and generated-code consistency

**Implicit self calls implemented.** `transpilation.implicit_self_calls = true`
is an independent, default-off manifest option. After normal name resolution,
`ImplicitSelfCallTranspiler` translates unknown bare calls to required dynamic
invocations on the current LPC object. Known method/function names retain their
checks even when called with invalid arity; qualified calls are unchanged.
This is late binding, not an abstract-class declaration or a relaxed type checker.
Untyped declarations still require their own flag.

The original `obj/living.c` now compiles, and an execution regression verifies
that `show_stats()` dispatches `short()` to a concrete test child. Tests also
cover multilevel inheritance, argument evaluation once, missing implementation
errors, global helpers, aliases, strict/default mode, and hosted boot propagation.

After implicit self calls, the scan reached **263/286**; boot reaches the church with three skipped preloads.
Evidence: `after-implicit-self-calls.log` in the generated assessment directory.
Some additional compile successes merely defer missing services or misspellings
(e.g. `this_palyer`) to runtime; those objects are not verified playable.
The source archive remains unchanged.

**Declared mixed return contracts fixed.** The type checker now preserves an
explicit method return declaration instead of replacing `mixed` with a narrower
type inferred from its body. This keeps parent and child JVM method descriptors
consistent. The living base's comparison result and the monster's integer
results satisfy the same declared `mixed` contract.

`MixedReturnContractTest` reproduces the failure with explicitly typed source
and verifies generated return descriptors, inherited virtual calls across three
levels, and continued rejection of incompatible narrow signatures and untyped
methods. `Lp245BridgeTest` also loads the unchanged `obj/monster.talk.c` and
executes its `can_put_and_get` implementation. Neither a new transpiler flag nor
an LP245-specific compiler exception is involved.

After the return-contract fix, the scan reached **265/286**, adding `obj/monster.talk.c` and `players/lars/yy.c`.
Boot still reaches the church with three skipped preloads. Evidence:
`after-mixed-return-contracts.log` in the generated assessment directory.
The 341 focused compiler/bridge tests pass; the full suite runs 513 tests with
one failure and 13 errors in retained LP245 compatibility/Telnet tests.

At that stage, player and monster analysis errors concerned legacy array
declarations and local string/integer assignments; the `can_put_and_get` signature error is
resolved.

Before the room overrides, retained world-loading tests exposed `NoSuchFieldError` for
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

**Player save-directory permissions corrected.** A real Telnet character creation
and movement session succeeded, but the next connection treated the same name as
new. The live `players/` directory had mode 0555 and no corresponding character
save existed. The legacy player prints its saving message without checking the
save result. Adding owner-write permission to the directory allows new character
files at the existing `players/<name>.o` paths; no storage redirection is used.
Every original file retains its read-only permissions and archive checksum.

`Lp245BridgeTest.originalPlayerSaveRestoresAcrossRuntimeRestart` exercises the
original player's save routine in an isolated archive copy, restores its name
and gold in an independent runtime, and confirms subsequent saves update the
same file. The bridge suite passes eight tests. Live reconnect/authentication
with the user's character remains to be confirmed after an actual save. The
permission setup is documented in the bridge README because directory modes
are not carried by Git.

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
2. **Player and room foundations:** living-base late binding and the selected
   room fields are implemented; resolve remaining legacy array declarations
   and stable inherited field/method types. Compile and load
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

- `obj/marker.c`
- `obj/master.c`
- `obj/monster.c`
- `obj/quicktyper.c`
- `obj/roommaker.c`
- `obj/shut.c`
- `obj/team.c`
- `obj/trace.c`
- `obj/trace2.c`
- `players/lars/board.c`
- `players/lars/rand.c`
- `room/adv_guild.c`
- `room/death/death.c`
- `room/death/death_room.c`
- `room/def_castle.c`
- `room/mine/tunnel3.c`
- `room/mine/tunnel9.c`
- `room/shop.c`
- `room/storage.c`
- `room/test.c`
