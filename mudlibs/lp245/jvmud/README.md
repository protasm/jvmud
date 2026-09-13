# Porting LP245 to JVMud

The original Lysator LP 2.4.5 mudlib assumes the language rules, built-in
functions, and lifecycle conventions of its original driver. Adapting it to
JVMud required expressing those assumptions through a compatibility bridge.
This directory contains that bridge: a manifest, source-translation settings,
and LPC adapters.

The following steps explain the adaptations made for LP245 and where they are
configured.

## 1. Preserve the original mudlib and add a separate bridge

The original archive's 582 files were retained unchanged. Compatibility code
lives in `jvmud/`, alongside the original `obj/`, `room/`, and other directories.
This keeps the legacy game content separate from the adaptations needed by its
new host.

The bridge consists of four files:

| File | Purpose |
| --- | --- |
| [lp245.config](lp245.config) | Selects the mudlib, language options, function aliases, and lifecycle mappings |
| [transpilation.json](transpilation.json) | Records checked declaration adaptations |
| [mfuns.c](mfuns.c) | Provides legacy functions through typed LPC adapters |
| [mudlib.c](mudlib.c) | Supplies host lifecycle and diagnostic handlers |

## 2. Describe the mudlib to JVMud

The manifest establishes the source root and include directories, then identifies
which objects JVMud should use:

```properties
mudlib_root = ..
include_paths = obj, room
mudlib_object = jvmud/mudlib
mfun_object = jvmud/mfuns
player_object = obj/player
initial_place = room/church
preload_file = room/init_file
```

Here, `mudlib_root` points from the manifest directory to the original mudlib.
The player, starting room, and preload list remain LP245's own objects and data.
The two bridge objects provide the connection to JVMud.

The manifest also selects the language features used by this profile:
`protected_evaluation`, `typed_function_literals`, `inline_callables`,
`multi_value_mappings`, and `varargs`. Its `engine_capabilities` setting grants
access to mudlib files, session control, and host control.

## 3. Supply explicit types for legacy method declarations

LP245 commonly omits method return types and parameter types. JVMud requires
both, so the bridge enables an in-memory translation:

```properties
transpilation.untyped_methods = true
```

For example, the original declaration:

```c
reset(arg) { /* original body */ }
```

is presented to JVMud as:

```c
mixed reset(mixed arg) { /* original body */ }
```

Existing explicit types and method bodies are preserved. The same translation
applies to declarations from includes, inherited objects, and global helpers.
The original files are never overwritten. This setting defaults to false;
other mudlibs continue to require explicit declarations unless they opt in.

## 4. Preserve implicit calls to methods supplied by descendants

The living base, `obj/living.c`, calls `short()` inside `show_stats()` without
itself declaring or defining `short()`. Player and monster objects inherit the
base and supply that method. The legacy code therefore expresses an implicit
contract between a base object and its descendants.

The bridge enables this convention with a separate setting:

```properties
transpilation.implicit_self_calls = true
```

After ordinary method and function lookup, an unknown bare call is translated
into a required dynamic call on the current LPC object. When the living base
calls `short()`, the concrete object's implementation supplies the description.
Arguments are evaluated once, and the result is treated as `mixed`.

The implementation must exist when the call executes; otherwise the call raises
an error. Known methods, global helpers, native functions, and configured aliases
retain their ordinary argument checks. Explicit qualified calls keep their
existing behavior. This flag defaults to false and is independent of the flag
for untyped declarations.

JVMud applies the manifest settings before loading the bridge object and retains
them for subsequent compilation of the mudlib.

## 5. Correct specific declarations without editing their sources

Some legacy fields are declared with a type that does not match their use.
The bridge records those corrections explicitly:

```properties
transpilation.overrides = transpilation.json
```

For example, the torch declares its fuel amount as a string but uses it in
numeric calculations. Its override is:

```json
{
  "file": "obj/torch.c",
  "field": "amount_of_fuel",
  "expected_type": "string",
  "replacement_type": "int"
}
```

Each entry belongs to the JSON file's `field_type_overrides` array. The configured
corrections are:

| Source | Field | Adapted type | Reason |
| --- | --- | --- | --- |
| `obj/torch.c` | `amount_of_fuel` | `int` | Fuel and value calculations use numbers |
| `room/room.c` | `dest_dir` | `string*` | Exits are destination/direction pairs |
| `room/room.c` | `items` | `string*` | Items are name/description pairs |
| `room/room.c` | `numbers` | `string*` | Number words are stored in an array |
| `room/room.c` | `property` | `mixed` | A room property may be a string or an array |
| `room/adv_guild.c` | `male_title_str`, `fem_title_str`, `neut_title_str` | `string*` | Titles are selected from arrays by level |
| `room/adv_guild.c` | `exp_str` | `int*` | Experience thresholds are selected from an array by level |
| `obj/quicktyper.c` | `list_ab`, `list_cmd`, `list_history` | `string*` | Aliases, their expansions, and command history are arrays of strings |
| `obj/monster.c` | `chat_head`, `a_chat_head` | `string*` | Idle and combat chat select messages from arrays |
| `obj/monster.c` | `talk_func`, `talk_type`, `talk_match` | `string*` | Conversation matching uses parallel arrays of callback names and text |
| `room/vill_road2.c`, `room/yard.c` | `chat_str`, `a_chat_str`, `function`, `type`, `match` | `string*` | Rooms pass chat and conversation arrays to their monsters |
| `room/pub2.c` | `chat_str`, `function`, `type`, `match` | `string*` | The Go player's configuration uses message and conversation arrays |
| `room/orc_vall.c` | `chats` | `string*` | Orcs in the valley and fortress share combat messages |

These fields originally declare `string`, except for the guild's `exp_str`,
which declares `int`, and Quicktyper's lists, which declare `object`. Each rule checks that the named field has the expected
declaration before applying the replacement in memory.
An unexpected type, missing field, or duplicate declaration produces a
translation error. Neighboring fields and local variables retain their types.

The guild's title and experience tables supply character advancement and the
experience values assigned to monsters. Giving those tables their array types
lets both uses retain LP245's original calculations and title selection.

In the monster, indexing the original string declarations was interpreted as
reading individual characters. Declaring those fields as arrays makes each
lookup retrieve a complete message or callback name. The correction belongs to
the array fields; the local strings receiving those entries keep their types.

The player object also declares local variables named `list` as `object` in
`list_peoples()` and `who()`, then assigns the array returned by `users()` to
them. The bridge translates each declaration to `object*` with a rule in the
same file's `local_type_overrides` array:

```json
{
  "file": "obj/player.c",
  "method": "who",
  "local": "list",
  "expected_type": "object",
  "replacement_type": "object*"
}
```

A second rule selects `list_peoples()` with the same local name and types.
Local rules identify a source file, a defined method, and exactly one local
variable within that method. They are applied after parsing, before type
checking, so grouped declarations and initializer order are preserved.
Parameters, fields, and matching names in other methods retain their declarations.
An unexpected type or ambiguous target, including repeated local names in nested
blocks, produces a translation error.

The yard's `extra_reset()` also stores a cloned weapon in a local originally
declared `string`. A local override changes that declaration to `object`, so
room initialization can move the weapon and continue configuring the beggar.

Quicktyper refreshes its actions by briefly moving through `room/storage`.
That room calls `::init(arg)`, although the room base declares `init()` with no
parameters. A checked method rule opts the base method into JVMud's existing
`varargs` convention:

```json
"method_varargs_overrides": [
  { "file": "room/room.c", "method": "init", "expected_parameter_count": 0 }
]
```

The translation requires exactly one defined, non-varargs method with the
expected parameter count, then adds the modifier in memory. The body and
parameters remain unchanged. Extra argument expressions are evaluated once in
order and their values discarded. This also required correcting JVMud's bytecode
emission for surplus arguments and calls to methods with no declared parameters.
Other methods keep their ordinary argument checks.

The JSON file path is relative to the manifest; source paths inside its rules
are relative to `mudlib_root`. Restart the instance after changing these rules.

## 6. Connect legacy functions to JVMud's native operations

LP245 objects call familiar driver functions such as `explode`, `write_file`,
and `sscanf`. Direct aliases in the manifest connect these names to JVMud efuns:

```properties
engine_function.jvmud_split_text = explode
engine_function.jvmud_append_mudlib_text = write_file
engine_function.jvmud_sscanf = sscanf
```

These aliases retain the native signatures, including `sscanf`'s output-parameter
captures. The `write_file` alias supplies the two-argument append operation.
Quicktyper's numbered history uses `sscanf(verb, "%%d%s", ...)`: a literal percent,
an integer capture, and a string capture. JVMud's scanner was corrected to
recognize `%d` after the preceding literal percent. Its supported conversions
remain `%d` and `%s`.

Other adaptations are written in LPC in `mfuns.c`. The manifest's
`mfun_object = jvmud/mfuns` setting makes those functions globally available to
the mudlib. Wrappers connect legacy names to object loading, containment,
actions, input capture, messaging, and scheduling. Helpers such as `implode`,
`all_environment`, and `deep_inventory` express their compatibility behavior
in LPC.

This lets original objects keep using their existing vocabulary while the bridge
provides the corresponding JVMud operations.

## 7. Map the driver's lifecycle to LP245 conventions

LP245 expects the driver to call methods such as `reset`, `init`, and `logon`.
The manifest maps JVMud lifecycle events to these names:

| JVMud event | LP245 method |
| --- | --- |
| Object loaded | `reset` |
| Interaction scope started | `init` |
| Player session connected | `logon` |
| Player session disconnected | `quit` |
| Object destruction requested | `prepare_destruct` |

The heartbeat method and base interval are configured separately:

```properties
temporal_tick_method = heart_beat
temporal_tick_interval = 1
```

The bridge's `mudlib.c` supplies the destruction handler. It moves contained
objects to the enclosing location, or destroys them when no enclosing location
exists. It also supplies compiler, runtime, heartbeat, and shutdown diagnostic
handlers, which write beneath `jvmud/log/`.

Quicktyper's history hook also needs LP245's action conventions:

```properties
command_actions.newest_first = true
command_actions.arguments_only = true
```

The first setting runs matching actions in reverse registration order across
both exact verbs and prefix matches. This lets the newly registered history
hook observe a command before its ordinary handler consumes it. The second
passes only the command arguments to empty-verb hooks; `query_verb()` supplies
the verb separately. Both settings default to false, preserving JVMud's native
exact-action precedence and whole-line input for empty-verb handlers.

## 8. Allow player saves alongside the preserved sources

LP245 saves characters as `players/<name>.o` beneath the mudlib root. The
`save_object` and `restore_object` adapters retain those paths and use JVMud's
object-state storage format.

The source freeze must leave the `players/` directory writable by the account
running JVMud so the server can create and update character files. From the
repository root, set the directory permission with:

```sh
chmod u+w mudlibs/lp245/players
```

This changes the directory permission only; the original files and source
subdirectories retain their permissions. Newly created character files remain
writable for subsequent saves. Generated player saves are excluded from Git.

Use `jvmud/lp245.config` as the manifest when selecting this mudlib in JVMud.

## 9. Use the carried Quicktyper

Once a character carries `obj/quicktyper`, aliases can supply direction shortcuts:

```text
alias s south
alias w west
s
w
```

`alias` lists the current aliases; `alias s` removes one. `history` lists recent
commands, `%%` repeats the last command, and `%2` repeats history entry 2.
`do smile,look,laugh` runs a sequence on heartbeats; `do` pauses it and `resume`
continues it. `refresh` re-registers the carried tool's actions. Quicktyper's
original autoload format preserves aliases when the player saves and reloads.

## 10. Check the whole archive, beyond preloads

The bridge also adapts declarations in objects outside `room/init_file`:

| Objects | Adaptation |
| --- | --- |
| `obj/trace.c`, `obj/trace2.c` | Variable names and query names become string arrays; stored values and callback results use mixed types; user lists become object arrays |
| `obj/armour.c` | Weight and sale value become integers so configured armour can be picked up and sold |
| `obj/marker.c` | Locals shared by string and integer `sscanf` captures become mixed |
| `obj/roommaker.c` | Room lighting becomes an integer; exit and generated-text lists become string arrays |
| `players/lars/board.c` | Board and mark grids become arrays of rows; the opponent lookup, string/integer color capture, and saved grid retain their actual value types |
| `players/lars/rand.c` | The distribution counter becomes an integer array |
| `room/death/death_room.c` | The player/tick pairs and temporary copies become mixed arrays |
| `room/mine/tunnel3.c`, `room/mine/tunnel9.c`, `room/test.c` | Boolean flags become integers; the computer room's summoned player becomes an object |

These are checked field/local rules in `transpilation.json`. JVMud now treats
`in` as a contextual foreach delimiter, allowing the tracer's method named
`in` without renaming it. Both `foreach (int item in values)` and the colon
form retain their behavior. The bridge also accepts the shop's two-argument
`add_worth(value, object)` call; driver wealth accounting remains a no-op,
as it was for the one-argument adapter.

`Lp245BridgeTest` checks all 286 original `.c` files against the archive list:
283 compile, and 281 initialize in a disposable copy. The following historical
exceptions are explicit; they are not silently counted as supported:

| Source | Remaining limitation |
| --- | --- |
| `obj/master.c` | Old LPmud 3.0 driver master uses unsupported cast syntax. JVMud selects `jvmud/mudlib.c` for its host callbacks. |
| `obj/team.c` | Unfinished source uses undeclared `vec` and calls its one-parameter `add` with two arguments. |
| `room/def_castle.c` | A source template requiring `NAME` and `DEST` definitions before compilation. |
| `obj/explore_xp.c` | Compiles, but initialization calls the undefined `previous_file()` helper in its save-path check. |
| `players/lars/test.c` | Compiles, but deliberately executes `1/0` during reset (and exit). |

Run the archive and behavior checks with:

```sh
mvn -Dtest=Lp245BridgeTest,MudlibCompatibilityScanTest test
scripts/scan-mudlib-preload-compile.sh mudlibs/lp245/jvmud/lp245.config
```

Initialization coverage does not mean every command or legacy driver facility
is implemented. The adapters still contain placeholders for facilities such as
the editor, snooping, filesystem metadata, and driver accounting. The original
sources remain unchanged, and the archive hash check guards their preservation.


## 11. Run room departure cleanup

The manifest maps `lifecycle.entity_departed_from_place = exit`. JVMud calls the
previous room's optional `exit` after relocating an interactive or
command-enabled actor, before running arrival callbacks. The departing object is
both the callback argument and `this_player()`; zero-argument hooks such as the
post office's `exit()` also work. Ordinary item moves do not invoke this hook.

The original death room relies on `exit(player)` to remove a finished ghost
from its queue. Without it, the 70th heartbeat moved the ghost to the church,
then recursively retried the same queue entry until the JVM stack overflowed.
The complete two-ghost sequence is covered by `Lp245BridgeTest`, including
return to the church and an empty queue afterward. Cleanup that redirects or
destroys an actor prevents arrival callbacks at the original destination.
