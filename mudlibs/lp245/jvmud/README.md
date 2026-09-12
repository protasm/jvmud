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
| [transpilation.json](transpilation.json) | Records explicit corrections to legacy field types |
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

## 5. Correct specific field declarations without editing their sources

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

All five fields originally declare `string`. Each rule checks that the named
field has the expected declaration before applying the replacement in memory.
An unexpected type, missing field, or duplicate declaration produces a
translation error. Neighboring fields and local variables retain their types.

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

Use `jvmud/lp245.config` as the manifest when selecting this mudlib in JVMud.
