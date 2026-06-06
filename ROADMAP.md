# JVMud Full-Stack Roadmap

JVMud is being built as a readable, inspectable Java LPC/LPMud stack. The project
should advance through small vertical slices that leave behind working code,
clear tests, and plain-language notes about what is supported next.

## Development Posture

`PRINCIPLES.md` is now the controlling design document for the engine. The
engine should be developed to support JVMud's core concepts: Game, Text,
Multiplayer, Interactive, World (Linked Places, Entities, Movement),
Persistence, Temporality, and Presence.

JVMud has one mudlib and language target: LPC for LPMud-style worlds. The
mudlib is no longer the authority that the engine must blindly emulate, but LPC
compatibility is the chosen target rather than one option among many. When old
mudlib code conflicts with the engine model, the preferred move is to add
dedicated mudlib-side compatibility shims and focused engine/compiler support
rather than rewriting upstream mudlib files or adding accidental legacy LPC
engine behavior to the engine.

"LPMud" means that the game world is LPC code compiled into live game objects
that can be rewritten, recompiled, and reloaded without rebooting the whole
game. It does not mean importing legacy driver concepts such as room,
heartbeat, apply, call_out, or master object into JVMud's engine model.

The engine-mudlib boundary is documented in `docs/ENGINE_MUDLIB_CONTRACT.md`.
Engine-facing concepts should use JVMud-native terms; legacy LPC names belong in
compatibility shims and adapters.

In practice:

- engine concepts should be named and designed from `PRINCIPLES.md`, not from
  legacy LPC mudlib quirks;
- compiler and runtime work should assume the sole LPC/LPMud target and avoid
  generic multi-language mudlib architecture;
- live reload of LPC-authored game objects is part of the target, while legacy
  driver vocabulary remains compatibility vocabulary rather than ontology;
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
using LPC-style root-relative paths. The shell is admin-only: plain input is an
admin command, and player/world input belongs to interactive sessions. It also supports
`verbosity quiet`, `verbosity normal`, and `verbosity watch`; watch mode displays
coarse compiler stage progress for compilation-backed commands.

The first command-system slice now exists behind Telnet. The runtime tracks a
current command actor, supports minimal `enable_commands`, `add_action`, and
`add_verb` engine functions, refreshes nearby `init` registrations, and
dispatches line input through object-defined verb handlers. The admin CLI no
longer simulates a selected player or command actor.

The first mudlib boot slice interprets `room/init_file` as a startup preload
list, tolerates currently unsupported preload entries, loads a default starting
room when present, and creates a host-owned local session actor situated in that
room through `WorldRuntime`. This actor is not an LPC player object; it is a
small JVMud session entity that lets LPC-defined room exits move the local
participant through the existing command path while the compiler runtime acts as
an adapter.

The first Telnet slice now owns the interactive command/session path over a
line-oriented socket listener. The listener boots one shared development runtime
and world for the process; each connection attaches a fresh host-owned persona
in the configured starting room. It accepts player commands plus a small set of
slash-prefixed session controls and performs basic Telnet option refusal. It
deliberately stops short of mudlib-defined player login, session-to-session
messaging, output routing between participants, and production server policy.

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
   actor context, and enough built-in command behavior for an interactive Telnet
   participant to look, move, get, and drop through LPC-defined objects. The
   first Telnet player-facing input loop is in place; the next work is to make
   common player commands feel natural with mudlib boot, starting rooms, and
   movement support.

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
    Reuse the CLI-proven command/session engine behind Telnet sessions. The
    first persistent listener is in place; the next work is mudlib-defined
    player login, output buffering, session isolation, and disconnect handling.

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
