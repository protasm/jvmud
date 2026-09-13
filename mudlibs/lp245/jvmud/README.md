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
| [transpilation.json](transpilation.json) | Records the remaining method-varargs adaptation |
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

## 5. Use dynamic values for documentary declarations

```properties
compiler.dynamic_types = true
transpilation.overrides = transpilation.json
```

The compiler treats declared fields, locals, parameters, and return values as
`mixed`, including array declarations and typed function literals. Written type
names remain available in the parsed source, but they do not coerce values or
restrict storage. Assignments cannot narrow the inferred storage type. This
policy also applies to includes, inherited objects, and global helpers.

Strict typing remains the default for other mudlibs. This flag does not accept
missing declarations; `transpilation.untyped_methods` handles those separately.
Combining dynamic typing with field/local type overrides is rejected; remove
those redundant rules when enabling the policy.
It preserves modifiers, argument structure, native function contracts, and
runtime operation checks. Same-arity duplicate methods remain errors. Explicit
conversion functions retain their behavior; this feature does not add support
for the old driver's cast syntax. Mixed defaults and empty returns use LPC zero.

This replaces all 51 field and 24 local type overrides, including the shop's
crown valuation correction. The JSON file now contains only one method-varargs
rule; it changes a calling convention, not a value type.

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

Dynamic typing covers documentary declarations throughout the archive, including
tools, armour, boards, and the death-room queue. The compiler also treats `in`
as a contextual foreach delimiter, allowing the tracer's method named `in`.
The bridge accepts the shop's two-argument `add_worth(value, object)` call;
driver wealth accounting remains a no-op.

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

### Shop valuation

The original shop's `value` method declares `name_of_item` as a string but uses
it to hold an object returned by inventory lookup. Dynamic typing preserves the
object reference, so `value crown` reports 30 gold coins, matching its sale price.
The regression test checks valuation in inventory and on the shop floor, then
verifies payment and stock transfer.

### World perception

`lifecycle.perception_delivery = deliver_perception` connects native JVMud world
observations to the original objects' optional `catch_tell(text)` methods. The
adapter passes exact legacy text; it does not parse commands or implement quests.
Room broadcasts now reach objects as well as connected players. `tell_object`
uses directed world delivery, while JVMud's private output primitive remains
private. Room-originated `say` calls outside a player command broadcast at the
room itself. This supports the Go player's deferred puzzle response and Leo's
quest hand-in without modifying either upstream object.
