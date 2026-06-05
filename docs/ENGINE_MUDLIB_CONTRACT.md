# Engine-Mudlib Boundary Contract

JVMud's engine-mudlib boundary is a native JVMud contract. It is not a promise
to preserve legacy LP driver vocabulary on the engine side.

The engine owns the concepts described in `../PRINCIPLES.md`: game, text,
interactivity, multiplayer world, persistence, temporality, presence, places,
links, entities, locations, containment, and situated perception. Mudlibs provide
world fiction, object behavior, commands, rules, and presentation.

Legacy mudlibs may expect older LPC names such as `reset`, `set_heart_beat`, or
`move_object`. JVMud should support those through dedicated mudlib-side
compatibility shims and focused compiler/runtime adapters, not by making those
names the engine's own conceptual API.

## Boundary Direction

The boundary has two directions:

- engine-to-mudlib calls, where JVMud invokes mudlib hooks at defined lifecycle
  moments;
- mudlib-to-engine requests, where mudlib code asks the engine to perform a
  world operation.

Both directions should use JVMud-native terms in engine design. Compatibility
objects translate legacy mudlib terms into those native operations.

## Engine-To-Mudlib Hooks

JVMud may call these mudlib hooks when present. The exact LPC-facing method names
are adapter details; the engine contract is named by event and intent.

| Engine event | Meaning | Timing |
| --- | --- | --- |
| Object initialized | A newly loaded or cloned mudlib object should initialize its state. | Once after object construction and dependency setup. |
| Object reactivated | An existing mudlib object should refresh recurring or resettable state. | On scheduled world reset or explicit reload policy. |
| Entity enters interaction scope | An object may register commands or update local interaction affordances for an actor. | When an interactive entity enters, carries, or otherwise comes into scope of the object. |
| Scheduled object tick | An object that asked for recurring time should receive a time pulse. | On the engine scheduler's deterministic cadence. |
| Deferred object callback | A previously scheduled one-shot callback should run. | At or after the requested world time. |
| Idle object review | An object may approve, refuse, or prepare for cleanup. | When the engine is considering unloading or compacting idle state. |
| Session connected | The mudlib may create or choose an entity for a new participant. | During session establishment, before the participant enters the world. |
| Policy check | The mudlib may participate in content policy, filesystem, or privilege decisions. | Before sensitive engine operations that delegate policy to the mudlib. |
| Error reported | The mudlib may observe or record a compilation/runtime error. | When JVMud catches an error attributable to mudlib content. |

For LPMUD 2.4.5, the compatibility layer may map these events to names such as
`reset`, `init`, `heart_beat`, `call_out` callbacks, `clean_up`, `connect`,
`valid_read`, `valid_write`, and `log_error`.

## Mudlib-To-Engine Requests

Mudlib code may request engine operations. The engine should expose these
operations in JVMud-native terms internally. Legacy efun names are compatibility
entry points, not the conceptual boundary.

| Engine operation | Meaning | Legacy compatibility examples |
| --- | --- | --- |
| Create or load an object | Materialize mudlib-defined behavior into the running world. | `clone_object`, object load by path |
| Destroy an object | Remove an object and clean up containment, commands, schedules, and references. | `destruct` |
| Move an entity | Change an entity's single immediate location. | `move_object`, `move_player` |
| Query location | Ask where an entity currently is. | `environment` |
| Query contents | Ask what entities are contained by a place or entity. | `all_inventory`, `first_inventory`, `next_inventory` |
| Query current execution context | Ask which object, actor, verb, or command is active. | `this_object`, `this_player`, `query_verb` |
| Register interaction commands | Make an object respond to participant text in its interaction scope. | `enable_commands`, `add_action`, `add_verb` |
| Send text | Deliver text to one participant, nearby participants, or a place. | `write`, `say`, `tell_object`, `tell_room` |
| Schedule time | Request recurring or delayed work. | `set_heart_beat`, `call_out`, `remove_call_out` |
| Access mudlib storage | Read, write, list, or query files within policy. | `read_file`, `write_file`, `file_size`, directory efuns |
| Ask mudlib policy | Delegate permissions or compatibility choices to mudlib policy objects. | master object hooks |
| Resolve compatibility function | Let a mudlib-side shim answer a legacy function call unknown to the compiler/runtime. | simul_efun lookup |

## Compatibility Shim Rules

Compatibility shims are mudlib-side objects dedicated to translation. They are
allowed to know legacy LPC names. They should be independent from upstream
mudlib content so vanilla files remain intact.

Recommended shim roles:

- a boundary/master adapter that declares compatibility objects and policy hooks;
- a simul_efun adapter for legacy function names that are better expressed in
  mudlib code than engine code;
- optional shadow or wrapper adapters for legacy object behavior that cannot be
  expressed as simple function fallback;
- small fixture shims for tests, kept separate from upstream mudlib files.

The engine may load and call compatibility shims, but it should not treat their
legacy method names as engine ontology.

## Boundary Invariants

- `PRINCIPLES.md` controls engine concepts.
- Places are connected by traversable links.
- Entities have containment and location, but entities are not link endpoints.
- Every entity has exactly one immediate location.
- Text is the primary interaction medium.
- Presence is situated; participants perceive from somewhere in the world.
- Time is an engine concern; mudlibs can request timed behavior but do not own
  the scheduler.
- Persistence is an engine concern; mudlibs can provide serialization behavior
  or policy but do not define whether the world endures.
- Vanilla upstream mudlib files are preserved by default.

## First Implementation Slice

The first useful implementation slice is simul_efun registration:

1. boot the mudlib compatibility boundary object;
2. ask it which simul_efun object should be active;
3. load that object as a dedicated mudlib-side shim;
4. when a legacy function call is not a native engine operation, try the active
   simul_efun object before reporting it unsupported;
5. keep the test fixture independent from upstream mudlib files.

This creates the pattern for future compatibility without making legacy LPC
method names the engine's language.
