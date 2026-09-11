# Deferred Work

This document tracks known JVMud work that is worth doing, but that we are
intentionally skipping in the current implementation slice.

Entries here should be concrete enough to return to later without rereading a
long chat thread. This is not the roadmap and not a promise of sequencing. It is
the parking lot for design debt, compatibility debt, and deliberately postponed
hardening.

When adding an entry, prefer this shape:

- what we are postponing;
- why it matters;
- why we are not doing it now;
- what evidence should trigger revisiting it.

## Entity Location Versus Daemon Objects

JVMud currently has runtime-side location bookkeeping that can associate one
loaded LPC object with another loaded LPC object as its containing location.
That is enough for legacy mudlib compatibility, but it is permissive: a mudlib
could technically place a world-present thing inside a service object such as a
daemon.

That does not match JVMud's intended ontology. Only world-present entities
should be locatable, and only valid locations or containers should be able to
contain other entities. Daemons and service objects should remain executable LPC
objects without physical presence.

We are postponing stricter enforcement because legacy
mudlibs blur the line between "object" and "thing in the world", and the current
compatibility goal is to keep moving through real boot blockers. Revisit this
when JVMud has a clearer runtime distinction between generic loaded LPC objects,
world-present entities, and container-capable entities.

Possible future shape:

- mark or realize selected LPC objects as world-present entities;
- reject `jvmud_set_entity_location` when the moved object is not an entity;
- reject locations that are daemons or otherwise not container-capable;
- keep legacy names such as `environment` and `set_environment` in mudlib
  compatibility config or shims.

## Runtime Location Bookkeeping And WorldRuntime

The compiler/runtime path currently owns object environment and inventory
bookkeeping for generated LPC objects. The engine package also has
`WorldRuntime`, where JVMud's longer-term world model lives.

This split is useful during compatibility work but should not become permanent
confusion. Eventually, object movement, location, containment, and presence
should line up with the engine's world model rather than existing only as
compiler-runtime helper maps.

We are postponing this because LPC compatibility is still shaking out the
minimum required LPC surface. Revisit once the startup path is stable enough to
separate "make legacy code run" from "make the engine model authoritative."

## Single Runtime Context And World Event Lane

Each hosted mud instance currently has one main `LPCRuntime`, and that runtime
has one authoritative `RuntimeContext`. That shape is acceptable: loaded LPC
objects, object ids, locations, inventories, sessions, command registrations,
efuns, and scheduler hooks should belong to the running instance, not to each
individual player thread.

The active execution stacks inside `RuntimeContext`, such as current object,
previous object, current command actor, and current verb, are thread-local. That
keeps simultaneous Java threads from sharing the same immediate call context.
However, most of the underlying instance state is ordinary mutable collection
state and should not be treated as broadly thread-safe.

The long-term design should keep one authoritative runtime context per mud
instance, but protect it with a single world event lane. Player session threads,
world-clock ticks, admin commands, object loading/reloading, and future async
callbacks should enqueue work into the instance rather than mutating LPC runtime
state directly.

Possible future shape:

- session threads enqueue player commands;
- the world clock enqueues scheduled ticks;
- admin tools enqueue load, reload, inspect, or mutation requests;
- one mud instance loop executes LPC/runtime mutations in deterministic order.

We are postponing this because the current `MudInstance` synchronized boundary
is serviceable for early development. Revisit before treating JVMud as a real
multi-user hosted server, before adding async database/network callbacks, or
before allowing compilation/reload paths to run concurrently with player/world
events.

## Complete Protected Evaluation Semantics

JVMud recognizes LDMud-style protected evaluation syntax such as `catch (...)`
well enough for current compatibility work, but the full runtime semantics
should be audited and documented.

The important future question is how JVMud-native protected evaluation should
behave when generated LPC code throws runtime errors, Java helper code throws,
or compatibility efuns signal failure. The surface should stay neutral JVMud
LPC, even if it accepts legacy syntax.

We are postponing the deeper audit while the configured boot path is still
surfacing more basic missing efuns and runtime helpers.

## First-Class Callable Completeness

JVMud has started supporting callable values for closure-shaped LPC
compatibility, including inline callable forms needed by common efuns. The full
LPC callable family is broader: typed function literals, function symbols,
stored closures, bound and unbound lambda forms, string method references in
legacy efun contracts, and callable invocation in more contexts.

We are postponing the complete model because each new callable source should be
driven by real mudlib evidence. Revisit when compatibility scans or startup
failures show a callable form that cannot be handled by the currently supported
subset.

## Variadic Or Rest Parameters

Mudlib compatibility shims sometimes need several overloads of the same helper
because JVMud LPC does not yet have rest parameters. Rest parameters would let a
method receive "the rest of the arguments" as an array-like value and could make
shim code smaller and clearer.

We are postponing this because the current overload approach is serviceable,
and rest parameters touch parser, semantic analysis, IR, bytecode generation,
and invocation matching. Revisit if compatibility wrappers become noisy enough
that the language feature is cheaper than the overload sprawl.

## LDMud Header And Sys Compatibility Strategy

Some LPC profiles expect LDMud-style `/sys` headers and driver-defined constants in a
few places. JVMud should avoid vendoring LDMud-controlled code into the engine.
When possible, compatibility should be expressed as JVMud-native language
support, small compatibility headers under the mudlib boundary, or manifest
predefines and aliases.

We are postponing a full policy because the practical need is still being
discovered file by file. Revisit if `/sys` dependencies expand beyond small
constants and declarations.

## Database Setup Contract

Database-backed profiles need clear requirements for connection URLs, credentials,
service availability, and failure handling. Profile-specific provisioning scripts
and schemas belong with the content rather than the JVMud platform manual.

JVMud should eventually have a repeatable host-side database connection guide that
documents what JVMud configures and what the selected content must supply. We are postponing deeper automation while the boot path is still
revealing runtime compatibility blockers.

Revisit when integration work reaches a point where database contents, not compiler or
runtime compatibility, are the main obstacle to meaningful gameplay smoke tests.
