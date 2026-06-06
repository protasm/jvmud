# JVMud Runtime

This directory is reserved for JVMud runtime and server code: object hosting,
world services, persistence, scheduling, networking, and integration with
compiled LPC classes.

The first engine-model slice is now present under
`src/main/java/io/github/protasm/jvmud/runtime/`. It defines the initial JVMud
ontology:

- `World`
- `Place`
- `Link`
- `Entity`
- `Location`
- `Capability`
- `WorldRuntime`

`WorldRuntime` owns the current containment graph. Every `Entity` has exactly
one immediate `Location`, and movement rejects containment cycles. It also owns
links between places, so a world is represented as connected containment rather
than a flat set of isolated rooms. Links are strictly between places. Entities
may contain and be contained, but they are not part of the place-link topology.
This module is the home for engine reality described by `../PRINCIPLES.md`;
compiler/runtime support and dedicated mudlib-side compatibility shims should
adapt to it rather than replacing it with LPC-specific semantics. Upstream
mudlib files should remain intact unless an explicit style or formatting change
is requested. The engine-mudlib boundary is described in
`../docs/ENGINE_MUDLIB_CONTRACT.md`; engine-side concepts should use JVMud
terms, with legacy LP engine names translated by compatibility shims.

The compiler source currently includes a compiler-adjacent runtime helper package
at `compiler/src/main/java/io/github/protasm/jvmud/compiler/runtime/`, plus
host-facing execution classes under `io.github.protasm.jvmud.compiler.exec`.
Those are part of the compiler module and should not be confused with this
top-level runtime module.
