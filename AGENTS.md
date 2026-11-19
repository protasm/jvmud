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

- `src/main/java/io/github/protasm/jvmud/language/`: `compiler/` owns LPC compilation,
  types, source resolution, and selected compatibility transformations; `execution/`
  owns compiled-class and object invocation contracts; `runtime/` owns generated-code
  context and value operations; `efun/` owns callable contracts and language-only built-ins;
  `formatter/` owns formatting.
  Language code does not import engine, communication, or storage implementations.
- `src/main/java/io/github/protasm/jvmud/engine/`: `application/` owns startup,
  public endpoints, worker supervision, and installation updates; `model/` owns worlds,
  identities, time, and mudlib lifecycle contracts; `configuration/` reads and assembles
  manifest policy; `instance/` assembles and executes one mudlib inside its worker JVM.
  `instance/session/` owns connection lifetime and Persona attachment; `session/login/`
  supplies optional policies that return profile data. `instance/lpc/` connects authored LPC
  to world, session, and storage services;
  `instance/efun/` supplies engine-service efuns and composes the hosted catalog;
  `diagnostics/` checks configured startup content.
- `src/main/java/io/github/protasm/jvmud/communication/`: `transport/` owns sockets,
  Telnet, and administration protocols; `admin/` interprets administrative commands;
  `console/` owns the terminal client; `protocol/` and `output/` handle messages and text.
- `src/main/java/io/github/protasm/jvmud/storage/`: Durable filesystem, JDBC,
  and administrator-registry adapters.
- `mudlibs/lp245/`: LPC mudlib source. `obj/` contains reusable object definitions and
  `room/` contains world/room content, headers, and startup-oriented files.
  Treat upstream vanilla mudlib files as read-only unless the user explicitly
  requests style or formatting changes. Add JVMud compatibility through
  dedicated independent mudlib-side shim objects.
- `docs/`: static project site.

## Application Operation

- `engine.application.JVMud` owns `main()`, application lifetime, public endpoints, and worker supervision.
- The engine starts with zero mudlibs. Each mudlib runs in a separate JVM.
- The engine player endpoint offers an informational directory, never a route into a mudlib. Each ready mudlib has a
  direct player endpoint and a scoped TLS administration endpoint. Quitting a
  mudlib closes the connection; there is no return-to-menu or world-hopping API.
- The engine owns administrator tokens, grants and TLS keys in its private state
  directory. Workers use separate JVMs for fault isolation but retain the host
  account's OS permissions; they are not a security boundary. A same-account Unix socket provides local bootstrap/recovery.
- Each `MudInstance` owns its execution queue and clock within its worker. Queue
  player input, protocol callbacks, administration and ticks there.
- Mudlibs interpret player commands. Transport never interprets LPC return values
  as command success/failure. A world is part of a mudlib, not a synonym for it.
- Runtime/transport unit tests may use `EmbeddedMudlibHost`; production engine
  tests must exercise real worker processes and endpoint authentication.

## Package Layout

The compiler lives under `io.github.protasm.jvmud.language.compiler`. Keep compiler
stages, types, and compilation inputs there. Generated LPC support lives under
`language.runtime`; engine integration belongs under `engine.instance`, not in
compiler helpers. Model classes define world rules and lifecycle contracts;
manifest readers and host-resource selection belong under `engine.configuration`.
Keep the package tree and package Javadocs accurate: they are the reader's first
map of the product.

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
- Keep compiler changes under `src/main/java/io/github/protasm/jvmud/language/compiler/`
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
