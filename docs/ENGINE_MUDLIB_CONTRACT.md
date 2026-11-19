# Engine-Mudlib Boundary Contract

JVMud's engine-mudlib boundary is a native JVMud contract. It is not a promise
to preserve legacy LP engine vocabulary on the engine side.

`GLOSSARY.md` defines the vocabulary used by this contract.

The engine owns the concepts described in `PRINCIPLES.md`: Game, Text,
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

## Live LPC Object Reload

Reload replaces a loaded shared object's compiled behavior while preserving the
object that participates in the world. Three concepts have distinct owners:

- `language.execution.LPCProgramRevision` identifies one source object and its
  generated class. The class's defining loader contains the inherited program
  revisions used for that compilation; analysis and linking use that same graph.
- `engine.instance.lpc.LPCObjectLifecycle` associates a stable live object identity
  with its current behavior receiver and copies compatible instance state between
  receivers. An LPC object reference continues to identify the same live object.
- Engine owners retain the object's bindings: `MudlibWorldProjection` supplies
  containment, `InstanceSessions` supplies session attachment, and
  `MudlibRuntimeContext` adapts command and scheduled-work operations. A new program
  revision does not require destroying or recreating those bindings.

The authenticated mudlib administration command `reload <path>` prepares a fresh
program revision and Java receiver before installing either. It checks that the
replacement has the same instance fields, with the same declaring program, names,
and types, then copies their current values. Installation runs on the mudlib's
execution queue. Compilation, linking, or field-layout failure leaves the previous
revision, live object, and administration handle available.

Reload does **not** run LPC field initialization, `object_loaded`, `create`,
`reset`, or a destruction hook. It preserves current field values, including
object references and collection values, rather than replacing them with source
defaults. An occupied room keeps its location binding and contents; connected
players keep their sessions; commands and timers continue to target the same
object. Ordinary method calls and named function references use its installed
revision. Existing inline closures retain their captured code and local values;
the invocation boundary shares the object's current field state with that code.
While such a closure runs, calls nested within it on the same object also use the
captured revision. Ordinary dispatch returns to the installed revision when the
closure returns.

This gives source edits a deliberate scope:

| Edit or operation | Effect on existing objects |
| --- | --- |
| Change a method body; reload its shared object | Subsequent ordinary calls use the new behavior. |
| Change an inherited method; reload a child shared object | That child uses the newly compiled inheritance graph. Other live objects retain their revisions. |
| Change a field initializer or creation hook | The live object's fields remain unchanged. Newly initialized objects receive the new defaults. |
| Add, remove, or change the type of an instance field | Reload is rejected. Automatic schema migration is not implemented. |
| Create a clone after successful reload | The new clone uses the installed program revision and normal initialization. Existing clones keep their own state and revisions. |

For example, Small Mercies' `room/square.create()` creates its mayor. Rerunning
that hook during a prose edit would create another resident. Reload instead keeps
the existing mayor and players and changes only behavior. Prose stored in a field
by `create()` is live state; changing its source default requires a newly created
object or an explicit mudlib state-editing operation. A literal changed inside a
description method takes effect on the next call after reload.

## Engine-To-Mudlib Lifecycle Hooks

JVMud lifecycle events are engine concepts. LPC method names are mudlib boundary
configuration. A mudlib may map an event to whatever method name it wants; for a
legacy LPC compatibility layer, that mapping may happen to use names
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
mudlib_object = jvmud/mudlib
mfun_object = secure/simul_efun
compatibility_object = jvmud/compat
language_features = protected_evaluation, inline_callables, varargs
engine_capabilities = mudlib_files, session_control
lifecycle.object_loaded = reset
lifecycle.interaction_scope_started = init
```

`mudlib_object` identifies the mudlib-side boundary adapter object.
`mfun_object` identifies an optional mudlib-owned global helper object. Neither
path is discovered from a bundled mudlib's layout or historical driver convention.

`compatibility_object` identifies an optional mudlib-side JVMud compatibility
object. JVMud no longer discovers this object by convention; the manifest names
it explicitly when the mudlib needs compatibility logic rather than simple
engine-function name mapping.

`language_features` enables optional syntax families for this profile; JVMud
does not infer a historical dialect from the mudlib's identity or directory.
`engine_capabilities` grants access to host resources. Database, mudlib-file,
session-control, and host-control operations are absent unless explicitly
granted. Include roots, mounts, initial place, transport controls, and startup
hooks are likewise manifest data rather than driver conventions.

The currently defined JVMud lifecycle events are:

| Event key | Current status | Target | Arguments | Meaning |
| --- | --- | --- | --- | --- |
| `object_loaded` | Implemented, optional mapping | Newly loaded or cloned object | `mixed first_load` | A mudlib object has been materialized and may initialize object state. Current compatibility passes `0` for `first_load`. Safe reload preserves existing state and does not emit this event. |
| `object_activated` | Reserved | Activated object | none yet | An existing object has been reactivated by an explicit reload, reset, or world maintenance policy. |
| `object_source_missing` | Implemented, optional mapping | Boundary object | requested object path | A shared object path has no source; mudlib policy may supply a substitute object. |
| `object_destruction_requested` | Implemented, optional mapping | Boundary object | target object | An object is about to be removed and mudlib policy may perform pre-destruction cleanup. |
| `object_destroyed` | Reserved | Destroyed object or boundary object | none yet | Post-destruction notification for future policies that require it. |
| `entity_arrived_at_place` | Reserved | Arriving entity or destination place | source place, destination place, movement action | An entity has completed movement into a place. |
| `entity_departed_from_place` | Implemented, optional mapping | Previous location | departing actor (zero-argument methods also accepted) | After an interactive or command-enabled entity changes location, before arrival callbacks. Initial placement, ordinary items, and same-location moves are excluded. |
| `entity_added_to_entity` | Reserved | Added entity or containing entity | container entity | An entity has entered another entity's containment. |
| `entity_removed_from_entity` | Reserved | Removed entity or containing entity | previous container entity | An entity has left another entity's containment. |
| `player_session_connected` | Implemented, optional mapping | Player object | none currently | A Session has connected and JVMud has created a Player endpoint for that Session; compatibility mudlibs may use this to start login input. |
| `player_session_post_rebind` | Implemented, optional mapping | Newly bound mudlib projection | none currently | A live Session has been rebound to a different mudlib-facing interactive object. |
| `player_persona_resolved` | Implemented for host-managed session policies | Player/Persona object | external user id, display name, gender text, profile-attribute mapping | Mudlib policy or JVMud fallback has resolved the entity that this Player will use as its in-world perspective. |
| `player_object_bound` | Reserved | Player object | persona id, session id | JVMud has associated a live compatibility player object with the Player, Session, and Persona relationship. |
| `player_entered_world` | Implemented for host-managed session policies | Player/Persona object | starting Place object | JVMud has placed the Persona into the world and interaction can begin. |
| `player_session_disconnected` | Implemented, optional mapping | Player object | none currently | The transport has disconnected and mudlib policy may save or clean up related state before session routing is removed. |
| `interaction_scope_started` | Implemented, optional mapping | Persona, Persona location, carried objects, and nearby objects | none currently | An interactive Persona's local command/perception scope is being refreshed; mudlib objects may register text commands or interaction affordances. |
| `command_dispatch_started` | Reserved | Persona or boundary object | command text, verb | JVMud is about to dispatch Player text to mudlib behavior. |
| `command_dispatch_finished` | Reserved | Persona or boundary object | command text, verb, handled status | JVMud has finished dispatching Player text. |
| `server_started` | Implemented, optional mapping | Boundary object | none | Configured boot and preloading are complete; a mudlib may now perform profile-specific startup. |
| `scheduled_tick` | Implemented, optional temporal mapping | Scheduled object | none currently | The engine scheduler is delivering deterministic recurring time to an object. The mudlib config chooses the method name and default interval. |
| `deferred_callback` | Reserved | Scheduled object | callback payload | A previously requested one-shot deferred callback is due. |

No lifecycle hook is globally required today. The minimum runnable mudlib
boundary can choose to define none of them. A playable LPC/LPMud mudlib will
usually map at least `object_loaded` and `interaction_scope_started`, because
those are the current hooks that let objects initialize state and expose local
commands.

## Player Connection Lifecycle

A connected player is represented by five related but distinct JVMud concepts:

- a Session, which is the active connection and transport pipe;
- a Player, which is the human-controller endpoint associated with that
  Session;
- a Persona, which is the Player's current in-World manifestation and
  perspective;
- an Entity, which is a thing in the World and may be used as a Persona;
- Presence, which is the experiential relationship created when a Player
  engages the World through a located Persona.

Player, Session, and Persona are JVMud-native engine concepts. A Session is the
transport layer: socket or channel, remote address, idle time, terminal state,
raw input/output, and disconnect lifecycle. A Player is deliberately thinner:
the human-facing controller role for that Session. Player is not an account,
login, durable identity, or authentication principal, and JVMud does not know
whether two Players are the same real-world person. A Persona is the in-World
Entity controlled by a Player.

Mudlibs may optionally provide projections and policies for those concepts:
profile/account data, login prompts, passwords, character names, duplicate
control rules, and Persona behavior. Those are mudlib-owned meanings layered on
top of JVMud's anonymous Player/Session/Persona mechanics.

### Connection And Attachment Ownership

The hosted implementation expresses the connection lifecycle in
`engine.instance.session`:

- `InstanceSessions` owns the instance's active connections and the indexes that
  associate Sessions, Players, Personas, and authored behavior. Attach, rebind, and
  detach operations update those relationships together.
- `InstanceSession` is the stable handle for one connection's entire lifetime,
  including login before a Persona has been selected. Its `SessionID` identifies
  state owned by `InstanceSessions`. A login flow is explicit connection state;
  it does not stand in for a Persona.
- `PersonaAttachment` explicitly relates the selected Persona to its native
  Entity and stable authored LPC object identity. World location belongs to the
  Entity; replacing compiled behavior does not replace that identity.

`MudInstance` coordinates these transitions on its execution queue.
`MudlibRuntimeContext` translates LPC-facing session operations to the same
`InstanceSessions` owner rather than keeping another set of connection bindings.
The records under `engine.model.identity` remain the engine's identity vocabulary.
Managed login policies and prompt flows live under `engine.instance.session.login`.
A policy consumes input and returns a `ManagedLoginResult`, optionally containing
a neutral `ManagedPersonaProfile`; `MudInstance` performs the resulting attachment
or disconnect transition. Account names and authentication policy remain mudlib
choices.

Connection liveness and Persona attachment are separate questions.
`InstanceSession.isOpen()` remains true during login; `attachment()` is empty until
a Persona is selected. Transport callers use `InstanceHost.isOpen(session)` to
check the connection and inspect the optional attachment when they need in-world
behavior.

A compatibility handoff changes the authored behavior attachment while retaining
its `SessionID`, `PlayerID`, connection timestamp, text and protocol output sinks,
and negotiated protocol state. Captured input belongs to the attached Persona's
scope: rebind clears the previous attachment's callback, and the newly attached
object may request its own callback. Disconnect removes the live attachment,
connection-only routing, callbacks, and the former attachment's world presence.
The mudlib's disconnect hook may save or clean up authored state before routing
is removed; persistent Persona retention is a separate policy.

The optional `filesystem_accounts` host policy provides reusable local
account-file, password-hashing, and pre-Persona prompt mechanics when a manifest
selects it explicitly. It does not name or invoke mudlib methods directly.
Instead, successful authentication produces a neutral managed Persona profile,
then the mapped `player_persona_resolved` and `player_entered_world` lifecycle
methods let the mudlib interpret that profile and perform its own entry behavior.

The phrase player object is compatibility vocabulary for a mudlib-authored
object, not an engine concept. A compatibility mudlib may use one object for
account/profile fields, Persona behavior, login flow, duplicate-login policy,
and legacy session glue. JVMud supports that combined shape through adapters,
but it does not make the combined
object the engine ontology.

The minimal JVMud-native attach sequence is:

1. A telnet connection arrives. `InstanceSessions` creates an `InstanceSession`
   and Session record, recording connection details such as remote address, idle
   timestamp, and output sink.
2. JVMud creates the Player endpoint associated with that Session. Login,
   character-selection, server notice, and other human-facing text can be
   messaged directly to the Player before any Persona exists.
3. Mudlib policy, or a JVMud fallback, resolves, creates, loads, or restores the
   Persona that will serve as the Player's in-World perspective.
4. JVMud ensures that the Persona has any required mudlib-side behavior
   projection, such as a compatibility player object.
5. `InstanceSessions` installs a `PersonaAttachment` that associates the Session,
   Player, Persona, native Entity, and authored behavior as the active control
   relationship, without deciding mudlib account ownership or duplicate-control
   policy.
6. JVMud gives the Persona's entity a location, using the configured initial
   place when no stronger restored location exists.
7. JVMud refreshes the interaction scope for the Persona so surrounding mudlib
   objects can register commands.
8. Each gameplay input line is dispatched as that Persona. During dispatch,
   `this_player()` is the compatibility view of the active Persona and
   `query_verb()` is the parsed verb.
9. When the Session disconnects, `InstanceSessions` removes the active connection
   and attachment, and the hosting instance removes the former attachment's world
   presence. Mudlib policy may save or clean up authored Persona or profile state
   before that cleanup.

The player lifecycle hooks above correspond to this sequence. They are optional
boundary hooks, not legacy driver applies. A mudlib may map them to LPC methods
on a boundary object, a profile/account shim, a Persona behavior shim, or a
legacy combined player object. If a mapping is absent, JVMud proceeds with the
engine-owned step.

JVMud-native text output uses write terminology. Engine APIs should prefer names
such as `writeToPlayer`, `writeToSession`, and `writeToPersona` rather than legacy
LPMud tell vocabulary or transport-oriented send vocabulary. Direct Player output is for login prompts,
character-selection prompts, server notices, and other control-plane text aimed
at the human endpoint. Normal gameplay output is Persona-routed because it is
perspectival and may depend on location, perception, command actor, inventory,
or world state. Legacy LPC names such as `write`, `say`, `tell_object`, and
`tell_room` remain compatibility entry points layered onto those engine
operations.

The first playable implementation should prefer the smallest coherent contract:

- bind one telnet Session to one Player and, after mudlib policy resolves a
  character or fallback, to one Persona with any required mudlib behavior
  projection;
- route login prompts, character-selection prompts, and system/control-plane
  text through Player messaging;
- route `write` and `tell_object` to the bound session when their target is the
  active Persona;
- route `say` and `shout` through presence-aware text output, even if the
  first slice uses simple broadcast behavior;
- make `users()` return connected Persona entities or their compatibility
  mudlib projections, not all loaded objects;
- make `query_idle(player)` and `query_ip_number(player)` read from the bound
  session record;
- dispatch input through the command registry populated by
  `interaction_scope_started`;
- move the Persona entity through World containment/location APIs rather than
  treating legacy rooms as engine concepts.

Persistence has two modes at this boundary. World Continuity is the MUD pillar:
the World endures independently of individual sessions. Save/Restore State is
durable state for selected Personas, Entities, or mudlib-defined account/profile
data that is outside active World temporality until restored.

`save_object` and `restore_object` are compatibility spellings for saving and
restoring LPC object state. JVMud owns storage policy and engine Persistence.
Mudlib code may provide fields or policy decisions, but player connection
lifecycle should not make legacy driver save files into engine ontology.

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
| Query current execution context | Ask which object, Persona, verb, or command is active. | `this_object`, `this_player`, `query_verb` |
| Register interaction commands | Make an object respond to Player text in its interaction scope. | `enable_commands`, `add_action`, `add_verb` |
| Message output | Deliver text to a Player, Session, Persona, nearby perceivers, or a place. | `write`, `say`, `tell_object`, `tell_room` |
| Schedule time | Request recurring or delayed work. | `set_heart_beat`, `call_out`, `remove_call_out` |
| Access mudlib storage | Read, write, list, or query files within policy. | `read_file`, `write_file`, `file_size`, directory engine functions |
| Ask mudlib policy | Delegate permissions or compatibility choices to mudlib policy objects. | master object hooks |
| Resolve mudlib function | Let a mudlib-side object answer a globally callable mudlib function. | mfun lookup |

## Compatibility Shim Rules

Compatibility shims are mudlib-side objects dedicated to translation. They are
allowed to know legacy LPC names. They should be independent from upstream
mudlib content so upstream files remain intact.

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
- Persistence is an engine concern. Mudlibs can provide Save/Restore State
  behavior or policy, but they do not define whether the World endures.
- Live reload of LPC-authored objects is an LPMud target requirement, but legacy
  reload hooks and driver names are adapter details.
- Upstream mudlib files are preserved by default.

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
