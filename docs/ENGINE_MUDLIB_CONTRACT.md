# Engine-Mudlib Boundary Contract

JVMud's engine-mudlib boundary is a native JVMud contract. It is not a promise
to preserve legacy LP engine vocabulary on the engine side.

`../GLOSSARY.md` defines the vocabulary used by this contract.

The engine owns the concepts described in `../PRINCIPLES.md`: Game, Text,
Multiplayer, Interactive, World (Linked Places, Entities, Movement),
Persistence, Temporality, Presence, places, links, locations, containment, and
situated perception. Mudlibs provide world fiction, object behavior, commands,
rules, and presentation.

JVMud has a sole mudlib and language target: LPC for LPMud-style worlds. This
contract defines how that LPC/LPMud target crosses into the JVMud engine; it is
not a generic plugin boundary for alternate mudlib languages.

In this contract, "LPMud-style" means LPC source compiled into live game objects
with support for rewriting, recompiling, and reloading those objects without a
whole-game reboot. It does not mean adopting legacy driver concepts such as
rooms, heartbeats, applies, call_outs, or master objects as engine concepts.

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

## Engine-To-Mudlib Lifecycle Hooks

JVMud lifecycle events are engine concepts. LPC method names are mudlib boundary
configuration. A mudlib may map an event to whatever method name it wants; for a
legacy LPMUD 2.4.5 compatibility layer, that mapping may happen to use names
such as `reset`, `init`, `heart_beat`, `call_out` callbacks, `valid_read`,
`valid_write`, or `log_error`.

The rule is:

- if a lifecycle event has no mudlib mapping, JVMud does not call into the
  mudlib for that event;
- if a lifecycle event has a mudlib mapping and the event occurs, JVMud must call
  the mapped method on the specified target object;
- if the target object does not define the mapped method, the call is skipped
  unless that event is later marked required by the boundary contract;
- a hook method's return value is advisory unless the event definition below
  says otherwise.

Lifecycle mappings are declared in the mudlib boundary configuration, for
example:

```text
lifecycle.object_loaded = reset
lifecycle.interaction_scope_started = init
```

The currently defined JVMud lifecycle events are:

| Event key | Current status | Target | Arguments | Meaning |
| --- | --- | --- | --- | --- |
| `object_loaded` | Implemented, optional mapping | Loaded or cloned object | `mixed first_load` | A mudlib object has been materialized and may initialize object state. Current compatibility passes `0` for `first_load`. |
| `object_activated` | Reserved | Activated object | none yet | An existing object has been reactivated by an explicit reload, reset, or world maintenance policy. |
| `object_destroyed` | Reserved | Destroyed object or boundary object | none yet | An object is being removed and may release mudlib-owned state before references are discarded. |
| `entity_arrived_at_place` | Reserved | Arriving entity or destination place | source place, destination place, movement action | An entity has completed movement into a place. |
| `entity_departed_from_place` | Reserved | Departing entity or source place | source place, destination place, movement action | An entity is leaving a place. |
| `entity_added_to_entity` | Reserved | Added entity or containing entity | container entity | An entity has entered another entity's containment. |
| `entity_removed_from_entity` | Reserved | Removed entity or containing entity | previous container entity | An entity has left another entity's containment. |
| `player_session_connected` | Implemented, optional mapping | Player object | none currently | A Player transport has connected and JVMud has bound the Session to the player object; compatibility mudlibs may use this to start login input. |
| `player_persona_resolved` | Reserved | Boundary object or player object | persona id | JVMud has resolved the entity that this session will use as its in-world perspective. |
| `player_object_bound` | Reserved | Player object | persona id, session id | JVMud has associated a live player object entity with a connected session. |
| `player_entered_world` | Reserved | Player object | starting place | JVMud has placed the player object into the world and interaction can begin. |
| `player_session_disconnected` | Reserved | Player object or boundary object | persona id, session id | The transport has disconnected and JVMud is about to save, unbind, or clean up session control. |
| `interaction_scope_started` | Implemented, optional mapping | Actor, actor location, carried objects, and nearby objects | none currently | An interactive actor's local command/perception scope is being refreshed; mudlib objects may register text commands or interaction affordances. |
| `command_dispatch_started` | Reserved | Command actor or boundary object | command text, verb | JVMud is about to dispatch Player text to mudlib behavior. |
| `command_dispatch_finished` | Reserved | Command actor or boundary object | command text, verb, handled status | JVMud has finished dispatching Player text. |
| `scheduled_tick` | Implemented, optional temporal mapping | Scheduled object | none currently | The engine scheduler is delivering deterministic recurring time to an object. The mudlib config chooses the method name and default interval. |
| `deferred_callback` | Reserved | Scheduled object | callback payload | A previously requested one-shot deferred callback is due. |

No lifecycle hook is globally required today. The minimum runnable mudlib
boundary can choose to define none of them. A playable LPC/LPMud mudlib will
usually map at least `object_loaded` and `interaction_scope_started`, because
those are the current hooks that let objects initialize state and expose local
commands.

## Player Connection Lifecycle

A connected player is represented by four related but distinct JVMud concepts:

- an entity, which is a thing in the world;
- presence, which is the experiential relationship created when a player
  engages the world through a located persona;
- a session, which is the active connection/control context;
- a persona, which is the entity currently associated with a session as that
  session's in-world perspective.

The player object is the LPC-authored mudlib object that gives the persona
entity its behavior. It is still an entity and therefore has a mechanical
location. It creates player presence only while it is associated with an active
session as that session's in-world perspective.

The minimal JVMud-native attach sequence is:

1. A telnet session connects. JVMud creates a session record and records the
   transport address and idle timestamp.
2. JVMud resolves, creates, loads, or restores the entity that will serve as
   the session's persona.
3. JVMud ensures that entity has a live LPC player object implementation.
4. JVMud associates the session with that entity as its control context.
5. JVMud gives the persona entity a location, using the configured initial place
   when no stronger restored location exists.
6. JVMud refreshes the interaction scope for the persona entity so surrounding
   mudlib objects can register commands.
7. Each input line is dispatched as that persona entity. During dispatch,
   `this_player()` is the session's persona and `query_verb()` is the parsed
   verb.
8. When the session disconnects, JVMud removes the session association and
   session-only routing. The entity may remain present in the world or be saved,
   moved, or removed according to persistence policy.

The player lifecycle hooks above correspond to this sequence. They are optional
boundary hooks, not legacy driver applies. A mudlib may map them to LPC methods
on a boundary object, a player shim, or the player object itself. If a mapping
is absent, JVMud proceeds with the engine-owned step.

The first playable implementation should prefer the smallest coherent contract:

- bind one telnet session to one persona entity with a player object
  implementation;
- route `write` and `tell_object` to the bound session when their target is the
  active persona;
- route `say` and `shout` through presence-aware text delivery, even if the
  first slice uses simple broadcast behavior;
- make `users()` return connected persona entities, not all loaded objects;
- make `query_idle(player)` and `query_ip_number(player)` read from the bound
  session record;
- dispatch input through the command registry populated by
  `interaction_scope_started`;
- move the persona entity through world containment/location APIs rather than
  treating legacy rooms as engine concepts.

`save_object` and `restore_object` are compatibility spellings for persistence
requests. JVMud owns persistence timing and storage policy. Mudlib code may
provide serialized fields or policy decisions, but player connection lifecycle
should not depend on a legacy driver save file model.

## Mudlib-To-Engine Requests

Mudlib code may request engine operations. The engine should expose these
operations in JVMud-native terms internally. Legacy engine function names are compatibility
entry points, not the conceptual boundary.

| Engine operation | Meaning | Legacy compatibility examples |
| --- | --- | --- |
| Create or load an object | Materialize mudlib-defined behavior into the running world. | `clone_object`, object load by path |
| Destroy an object | Remove an object and clean up containment, commands, schedules, and references. | `destruct` |
| Move an entity | Change an entity's single immediate location. | `move_object`, `move_player` |
| Query location | Ask where an entity currently is. | `environment` |
| Query contents | Ask what entities are contained by a place or entity. | `all_inventory`, `first_inventory`, `next_inventory` |
| Query current execution context | Ask which object, actor, verb, or command is active. | `this_object`, `this_player`, `query_verb` |
| Register interaction commands | Make an object respond to Player text in its interaction scope. | `enable_commands`, `add_action`, `add_verb` |
| Send text | Deliver text to one Player, nearby Players, or a place. | `write`, `say`, `tell_object`, `tell_room` |
| Schedule time | Request recurring or delayed work. | `set_heart_beat`, `call_out`, `remove_call_out` |
| Access mudlib storage | Read, write, list, or query files within policy. | `read_file`, `write_file`, `file_size`, directory engine functions |
| Ask mudlib policy | Delegate permissions or compatibility choices to mudlib policy objects. | master object hooks |
| Resolve mudlib function | Let a mudlib-side object answer a globally callable mudlib function. | mfun lookup |

## Compatibility Shim Rules

Compatibility shims are mudlib-side objects dedicated to translation. They are
allowed to know legacy LPC names. They should be independent from upstream
mudlib content so vanilla files remain intact.

New compatibility work should be added in two steps. First identify and name
the JVMud-native engine operation in terms of this contract, such as session
input capture, text delivery, persistence, scheduled time, movement, location,
or interaction scope. Then expose the legacy LPC spelling in a compatibility
shim that delegates to that native operation. The fact that a legacy mudlib
uses a driver function is evidence for a needed capability; it is not by itself
the engine API design.

Recommended shim roles:

- a boundary/master adapter that declares compatibility objects and policy hooks;
- an mfun object for mudlib-global functions, including legacy function names
  that are better expressed in mudlib code than engine code;
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
- Presence is situated; Players perceive from somewhere in the world.
- Time is an engine concern; mudlibs can request timed behavior but do not own
  the scheduler.
- Persistence is an engine concern; mudlibs can provide serialization behavior
  or policy but do not define whether the world endures.
- Live reload of LPC-authored objects is an LPMud target requirement, but legacy
  reload hooks and driver names are adapter details.
- Vanilla upstream mudlib files are preserved by default.

## First Implementation Slice

The first useful implementation slice is mfun registration:

1. boot the mudlib compatibility boundary object;
2. ask it which mfun object should be active;
3. load that object as a dedicated mudlib-side shim;
4. when a function call is not a local method, try the active mfun object before
   engine functions so mudlib functions can shadow efuns;
5. keep the test fixture independent from upstream mudlib files.

This creates the pattern for future compatibility without making legacy LPC
method names the engine's language.
