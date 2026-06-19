# JVMud Engine Module

This Maven module currently houses JVMud's engine-owned model. The directory is
still named `runtime/` for module stability, but the Java package is
`io.github.protasm.jvmud.engine` to reflect the compiler-engine-mudlib project
model.

The engine model is present under
`src/main/java/io/github/protasm/jvmud/engine/`. It defines JVMud ontology and
boundary concepts through focused subpackages:

- `world`: `World`, `WorldRuntime`, `Place`, `Entity`, `Link`, `Location`, and `Capability`
- `identity`: `PlayerRecord`, `SessionRecord`, `PersonaRecord`, and their ids
- `time`: `WorldScheduler`, `ScheduledTask`, and `WorldClock`
- `mudlib`: `MudlibBoundary`, lifecycle events, config reading, and mudlib projections
- `output`: text formatting for output leaving the runtime
- `support`: small shared helpers that do not define MUD concepts

The Eight Pillars of MUD are used as an educational checklist for this layout:
interactive, text-only, multiplayer, world-based, persistent, temporal,
player-present games. The package names stay concrete because several pillars
are cross-cutting rather than one-class-per-pillar homes.

`WorldRuntime` owns the current containment graph. Every `Entity` has exactly
one immediate `Location`, and movement rejects containment cycles. It also owns
links between places, so a world is represented as connected containment rather
than a flat set of isolated rooms. Links are strictly between places. Entities
may contain and be contained, but they are not part of the place-link topology.
This module is the home for engine reality described by `../PRINCIPLES.md`.
Compiler support and dedicated mudlib-side compatibility shims should adapt to
it rather than replacing it with LPC-specific semantics. Upstream mudlib files
should remain intact unless an explicit style or formatting change is requested.
The engine-mudlib boundary is described in
`../docs/ENGINE_MUDLIB_CONTRACT.md`; engine-side concepts should use JVMud
terms, with legacy LP engine names translated by compatibility shims.

The compiler source still includes a compiled-LPC support runtime package at
`compiler/src/main/java/io/github/protasm/jvmud/compiler/runtime/`, plus
host-facing execution classes under `io.github.protasm.jvmud.compiler.exec`.
Those are part of the compiler module and should not be confused with this
engine module.
