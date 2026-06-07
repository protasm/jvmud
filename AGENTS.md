# JVMud Agent Notes

This repository is the JVMud monorepo. It currently contains the JVMud compiler
source, the JVMud runtime/server module, a vanilla LPMUD 2.4.5 mudlib, and
static docs.

## Top-Level Areas

- `compiler/`: JVMud compiler Java source. Work here for scanner,
  preprocessor, parser, semantic analysis, IR, bytecode generation, efun APIs,
  and the current host-facing runtime/classloading helpers.
- `runtime/`: JVMud runtime/server code. Do not assume this directory is
  the same thing as `compiler/src/main/java/io/github/protasm/jvmud/compiler/runtime/`,
  which contains compiler helper classes used by generated code.
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

Root Maven build wiring is present for the current modules. Use `mvn test` for
the baseline test suite unless the task calls for a narrower command.

## Working Guidance

- Inspect the relevant tree before changing it; this repo is still being shaped
  after migration.
- Keep compiler changes under `compiler/` unless the task is explicitly about
  runtime, mudlibs/lp245, or docs.
- Treat `mudlibs/lp245/` as LPC source/content, not Java module source. Preserve
  upstream mudlib files by default; compatibility belongs in dedicated shim
  objects and focused compiler/runtime support.
- Keep runtime/server concepts separate from compiler execution helpers.
- Prefer small documentation updates that reflect the current repo state over
  speculative roadmaps.
