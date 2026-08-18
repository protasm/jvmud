# JVMud Agent Notes

This repository is the JVMud project. It currently contains the JVMud compiler,
engine, hosted instance, transport, persistence, CLI, bundled mudlibs, and
static docs.

## Top-Level Areas

- `src/main/java/io/github/protasm/jvmud/compiler/`: JVMud compiler Java source. Work here for scanner,
  preprocessor, parser, semantic analysis, IR, bytecode generation, efun APIs,
  and the current host-facing runtime/classloading helpers.
- `src/main/java/io/github/protasm/jvmud/engine/`: JVMud engine code. Do not assume this package is
  the same thing as `src/main/java/io/github/protasm/jvmud/compiler/runtime/`,
  which contains compiler helper classes used by generated code.
- `src/main/java/io/github/protasm/jvmud/instance/`: JVMud hosted-instance code.
  Work here for mudlib boot, shared runtime/world assembly, lifecycle hooks,
  player/persona attachment, and multi-mud transfer.
- `src/main/java/io/github/protasm/jvmud/transport/`: JVMud player transport
  code. Work here for Telnet sockets, sessions, protocol mechanics, line I/O,
  and connection lifecycle.
- `src/main/java/io/github/protasm/jvmud/persistence/`: JVMud durable storage
  adapters for filesystem, JDBC, and future persistence backends.
- `src/main/java/io/github/protasm/jvmud/cli/`: JVMud local admin CLI code.
- `mudlibs/lp245/`: LPC mudlib source. `obj/` contains reusable object definitions and
  `room/` contains world/room content, headers, and startup-oriented files.
  Treat upstream vanilla mudlib files as read-only unless the user explicitly
  requests style or formatting changes. Add JVMud compatibility through
  dedicated independent mudlib-side shim objects.
- `docs/`: static project site.

## Package Layout

The compiler lives under `io.github.protasm.jvmud.compiler`. Keep new compiler
code inside that namespace unless a task explicitly introduces another JVMud
module.

## Current Build State

Root Maven build wiring uses the standard `src/` and `target/` layout. Use
`mvn test` for the baseline test suite unless the task calls for a narrower
command.

## Working Guidance

- Inspect the relevant tree before changing it; this repo is still being shaped
  after migration.
- When starting Realms, redirect the verbose startup output to a file whenever
  possible and inspect only concise summary and error markers. Read or stream
  the full startup output only when it is necessary for the particular agentic
  task, such as diagnosing an initialization or preload failure.
- Keep compiler changes under `src/main/java/io/github/protasm/jvmud/compiler/`
  unless the task is explicitly about engine, instance, transport,
  persistence, mudlibs/lp245, or docs.
- Treat `mudlibs/lp245/` as LPC source/content, not Java module source. Preserve
  upstream mudlib files by default; compatibility belongs in dedicated shim
  objects and focused compiler/runtime support.
- Do not change the compiler to accept untyped LPC method declarations or
  untyped method parameters. When vanilla mudlib files need to compile, add
  explicit LPC return and parameter types to those mudlib sources instead.
- Keep engine, instance, transport, and persistence concepts separate from
  compiler execution helpers.
- Prefer small documentation updates that reflect the current repo state over
  speculative roadmaps.
