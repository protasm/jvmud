# JVMud Agent Notes

## Top Priority: The Code Is the Product

**JVMud's code is the product. The point of the code is to BE the product,
rather than become the product.**

JVMud is a public-facing software project. Reading, learning from, extending,
and contributing to its source are primary ways of using the product.
Clarity, architecture, naming, documentation, and consistency are therefore
product qualities and acceptance criteria for every change.

Treat this as a top priority in all project work: design, implementation,
maintenance, testing, documentation, and review. Correct behavior is necessary;
a change must also leave an understandable, deliberate, maintainable
implementation. Do not defer source quality as optional polish after a feature
works. Prefer straightforward designs whose intent and boundaries are clear,
and document non-obvious decisions where future readers need them.

This repository is the JVMud project. It currently contains the JVMud compiler,
engine, hosted instance, transport, persistence, administration consoles, bundled mudlibs, and
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
  player/persona attachment, per-mudlib execution, and the worker entry point.
- `src/main/java/io/github/protasm/jvmud/transport/`: JVMud player transport
  code. Work here for Telnet sockets, sessions, protocol mechanics, line I/O,
  and connection lifecycle.
- `src/main/java/io/github/protasm/jvmud/persistence/`: JVMud durable storage
  adapters for filesystem, JDBC, and future persistence backends.
- `src/main/java/io/github/protasm/jvmud/admin/`: Administrative command interpretation
  and per-administrator session state.
- `src/main/java/io/github/protasm/jvmud/console/`: Shared interactive administration console.
- `src/main/java/io/github/protasm/jvmud/maintenance/`: Standalone updates and compilation diagnostics.
- `mudlibs/lp245/`: LPC mudlib source. `obj/` contains reusable object definitions and
  `room/` contains world/room content, headers, and startup-oriented files.
  Treat upstream vanilla mudlib files as read-only unless the user explicitly
  requests style or formatting changes. Add JVMud compatibility through
  dedicated independent mudlib-side shim objects.
- `docs/`: static project site.

## Application Operation

- `engine.JVMud` owns `main()`, application lifetime, public endpoints, and worker supervision.
- The engine starts with zero mudlibs. Each mudlib runs in a separate sandboxed JVM.
- The engine player endpoint offers a public menu. Each ready mudlib also has a
  direct player endpoint and a scoped TLS administration endpoint. Quitting a
  mudlib closes the connection; there is no return-to-menu or world-hopping API.
- The engine owns administrator tokens, grants and TLS keys outside worker-visible
  storage. A same-account Unix socket provides local bootstrap/recovery.
- Each `MudInstance` owns its execution queue and clock within its worker. Queue
  player input, protocol callbacks, administration and ticks there.
- Mudlibs interpret player commands. Transport never interprets LPC return values
  as command success/failure. A world is part of a mudlib, not a synonym for it.
- Runtime/transport unit tests may use `EmbeddedMudlibHost`; production engine
  tests must exercise real worker processes and endpoint authentication.

## Package Layout

The compiler lives under `io.github.protasm.jvmud.compiler`. Keep new compiler
code inside that namespace unless a task explicitly introduces another JVMud
module.

## Naming

In class and file names, always spell acronyms in uppercase, regardless of their
position. For example, use `LPCObject`, `ASTNode`, `GMCPCodec`, `PlayerID`, and
`LPCFormatterCLI`; never `LpcObject`, `GmcpCodec`, `PlayerId`, or `LPCFormatterCli`.

## Current Build State

Root Maven build wiring uses the standard `src/` and `target/` layout. Use
`mvn test` for the baseline test suite unless the task calls for a narrower
command.

## Working Guidance

- Inspect the relevant tree before changing it; this repo is still being shaped
  after migration.
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
