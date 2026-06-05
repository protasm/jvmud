# JVMud Full-Stack Roadmap

JVMud is being built as a readable, inspectable Java LPMud stack. The project
should advance through small vertical slices that leave behind working code,
clear tests, and plain-language notes about what is supported next.

## Current Baseline

The first baseline is a buildable compiler module. `mvn test` now compiles the
compiler with ASM and runs smoke tests for the current LPC-to-bytecode path.

The baseline proves:

- simple LPC source can compile to bytecode;
- generated classes can be loaded, instantiated, and invoked from Java;
- inherited source files can be resolved and compiled;
- registered efuns can be called from generated LPC bytecode;
- early driver efuns provide testable output and shared object invocation;
- a mudlib compatibility scan can report current gaps without failing the build.

The current driver efun slice includes output capture (`write`, `say`,
`tell_object`), current object/player lookup, shared `call_other` invocation,
and minimal signatures for early object lifecycle efuns such as `clone_object`,
`move_object`, inventory traversal, `set_light`, and `set_heart_beat`.

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
`add_verb` efuns, refreshes nearby `init` registrations, and dispatches explicit
`/dispatch <command...>` input through object-defined verb handlers. The same CLI
is now player-facing by default: ordinary input is routed directly through that
command-dispatch path, while slash-prefixed input reaches shell/admin tooling.

## Waypoints

1. **Buildable Compiler Baseline**
   Keep the Maven build green and expand tests around language features already
   supported by the scanner, parser, semantic model, IR, and bytecode compiler.

2. **Compiler Compatibility Harness**
   Expand the compatibility scan from a small curated set toward representative
   imported mudlib files. Track failures by stage so parser, semantic, efun, and
   runtime work can be prioritized from evidence.

3. **Minimal Object Runtime**
   Create the real runtime layer for canonical LPC paths, loaded-object identity,
   clones, destructed objects, environments, and reflective method calls. Keep
   generated-code helper classes separate from server/runtime concepts.

4. **Essential Driver Efuns**
   Implement the first mudlib-blocking efuns: `write`, `say`, `tell_object`,
   `this_object`, `this_player`, `environment`, `move_object`, `clone_object`,
   `destruct`, `present`, inventory traversal, and `call_other`.

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
   and place the local session into that room.

8. **Scheduler And Interactive Input**
   Implement deterministic heartbeats, callouts, and `input_to` so old-school
   interactive mudlib objects can be tested and driven from the CLI.

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
    enough mudlib compatibility for a long-lived playable server.

## Readability Standard

JVMud should be approachable to a human reader. Prefer names and structure that
explain intent. Add comments where LPC driver semantics, bytecode behavior, or
runtime lifecycle rules are non-obvious. Tests should read like executable
documentation for the supported LPC subset.
