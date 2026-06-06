# JVMud Full-Stack Roadmap

JVMud is being built as a readable, inspectable Java LPMud stack. The project
should advance through small vertical slices that leave behind working code,
clear tests, and plain-language notes about what is supported next.

## Development Posture

`PRINCIPLES.md` is now the controlling design document for the engine. The
engine should be developed to support JVMud's core concepts: game, text,
interactivity, multiplayer world, persistence, temporality, and presence.

The mudlib is no longer the authority that the engine must blindly emulate. LPC
compatibility remains useful, but it is a content and compatibility concern, not
the engine's identity. When old mudlib code conflicts with the engine model, the
preferred move is to add dedicated mudlib-side compatibility shims and focused
engine/compiler support rather than rewriting upstream mudlib files or adding
accidental legacy LPC engine behavior to the engine.

The engine-mudlib boundary is documented in `docs/ENGINE_MUDLIB_CONTRACT.md`.
Engine-facing concepts should use JVMud-native terms; legacy LPC names belong in
compatibility shims and adapters.

In practice:

- engine concepts should be named and designed from `PRINCIPLES.md`, not from
  legacy LPC mudlib quirks;
- upstream mudlib files should be treated as read-only unless an explicit style
  or formatting change is requested;
- compatibility shims should live in dedicated independent mudlib-side objects,
  such as mfun objects, shadow, or adapter objects;
- engine-side APIs should describe JVMud operations rather than preserving
  legacy LP engine method names for their own sake;
- compatibility scans should identify useful evidence, not dictate engine
  architecture;
- runtime/server work should preserve the distinction between JVMud world
  reality and generated-code helper APIs.

## Current Baseline

The top-level `runtime` module now contains the first engine-owned JVMud world
model. It defines `World`, `Place`, `Link`, `Entity`, `Location`, `Capability`,
and `WorldRuntime`. `WorldRuntime` owns single containment, requires every
created entity to have an immediate location, rejects containment cycles, and
models navigable links between places. Links are place-to-place topology only;
entities participate through containment, not as link endpoints.

The first baseline is a buildable compiler module. `mvn test` now compiles the
compiler with ASM and runs smoke tests for the current LPC-to-bytecode path.

The baseline proves:

- simple LPC source can compile to bytecode;
- generated classes can be loaded, instantiated, and invoked from Java;
- inherited source files can be resolved and compiled;
- registered engine functions can be called from generated LPC bytecode;
- early engine functions provide testable output and shared object invocation;
- a mudlib compatibility scan can report current gaps without failing the build.

The current mudlib function slice includes output delivery, current
execution context lookup, shared object invocation, object creation/destruction,
movement, inventory traversal, local perception, and early time scheduling.
Several of those are still exposed through legacy LPC names in the current
compiler helper layer; the next boundary work should translate them toward
JVMud-native engine operations.

The first object runtime slice adds real clone/load bridging, environment
tracking, inventories, `present`, inventory traversal, and destruct cleanup. It
is intentionally identity-based and small until the top-level runtime module
takes over broader server lifecycle responsibilities.

The first admin CLI slice adds a separate `cli` Maven module with a local shell
for one admin user. It can boot a runtime, load and clone LPC objects, call
methods, move objects, inspect environments, list handles, reload compiled
objects, destruct objects, and quit. It also has a mudlib-rooted virtual
filesystem with `pwd`, `cd`, `ls`, and `cat`, so an admin can navigate content
using LPC-style root-relative paths. This shell is intentionally local-only until
command/session behavior is ready for networking. The shell also supports
`verbosity quiet`, `verbosity normal`, and `verbosity watch`; watch mode displays
coarse compiler stage progress for compilation-backed commands.

The first command-system slice now exists behind the CLI. The runtime tracks a
current command actor, supports minimal `enable_commands`, `add_action`, and
`add_verb` engine functions, refreshes nearby `init` registrations, and dispatches explicit
`/dispatch <command...>` input through object-defined verb handlers. The same CLI
is now player-facing by default: ordinary input is routed directly through that
command-dispatch path, while slash-prefixed input reaches shell/admin tooling.

The first mudlib boot slice interprets `room/init_file` as a startup preload
list, tolerates currently unsupported preload entries, loads a default starting
room when present, and creates a host-owned local session actor situated in that
room through `WorldRuntime`. This actor is not an LPC player object; it is a
small JVMud session entity that lets LPC-defined room exits move the local
participant through the existing command path while the compiler runtime acts as
an adapter.

## Waypoints

1. **Buildable Compiler Baseline**
   Keep the Maven build green and expand tests around language features already
   supported by the scanner, parser, semantic model, IR, and bytecode compiler.

2. **Compiler Compatibility Harness**
   Keep the compatibility scan as evidence for useful language and content
   support, not as a mandate to emulate every legacy LPC engine behavior. Track
   failures by stage so parser, semantic, engine function, runtime, or shim-object work can
   be chosen deliberately.

3. **Minimal Object Runtime**
   Build on the top-level runtime module as the engine-owned world model.
   Compiler-side loaded-object identity, clones, destructed objects,
   environments, and reflective method calls should increasingly act as adapters
   into `WorldRuntime`. Keep generated-code helper classes separate from
   server/runtime concepts.

4. **Essential Engine Functions**
   Implement the first engine-compatible operations needed by current content:
   text delivery, current actor/object lookup, location queries, movement,
   object materialization, destruction, presence lookup, inventory traversal,
   and object calls. Keep legacy engine function names as compatibility entry points rather
   than as the engine's ontology.

5. **Single-Admin CLI**
   Add a local command-line shell backed by the real runtime. Initial commands
   should load, clone, call, move, inspect, reload, destruct, and list objects.

6. **Command System And Player-Like Session**
   Add `enable_commands`, `add_action`, `add_verb`, command dispatch, current
   actor context, and enough built-in command behavior for a local admin/player
   to look, move, get, and drop through LPC-defined objects. The first explicit
   CLI dispatch path and player-facing input loop are in place; the next work is
   to make common player commands feel natural with mudlib boot, starting rooms,
   and movement support.

7. **Mudlib Boot Slice**
   Interpret `mudlib/room/init_file`, load startup objects, boot a starting room,
   and place the local session into that room. The first slice is in place; the
   next work is richer boot configuration and broader real-mudlib compatibility
   through dedicated shim objects and focused runtime/compiler support.

8. **Scheduler And Interactive Input**
   Implement deterministic recurring ticks, delayed callbacks, and interactive
   input capture so old-school interactive mudlib objects can be tested and
   driven from the CLI through compatibility shims.

9. **Persistence And Filesystem Policy**
   Add save/restore, logs, path normalization, mudlib root isolation, writable
   data areas, and permission checks.

10. **Telnet Server**
    Reuse the CLI-proven command/session engine behind Telnet sessions, login,
    line input, output buffering, and disconnect handling.

11. **Multi-User World Runtime**
    Support multiple sessions, shared rooms, messaging, command isolation, user
    lookup, and admin operations for sessions and loaded objects.

12. **Production Hardening**
    Add config, structured diagnostics, packaging, CI, backups, deployment, and
    enough engine-aligned compatibility support for a long-lived playable server.

## Readability Standard

JVMud should be approachable to a human reader. Prefer names and structure that
explain intent. Add comments where legacy LPC engine semantics, bytecode behavior, or
runtime lifecycle rules are non-obvious. Tests should read like executable
documentation for the supported LPC subset.
