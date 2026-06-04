# JVMud Agent Notes

This repository is the JVMud monorepo. It currently contains the JVMud compiler
source plus placeholders for the runtime/server and mudlib.

## Top-Level Areas

- `compiler/`: JVMud compiler Java source. Work here for scanner,
  preprocessor, parser, semantic analysis, IR, bytecode generation, efun APIs,
  and the current host-facing runtime/classloading helpers.
- `runtime/`: future JVMud runtime/server code. Do not assume this directory is
  the same thing as `compiler/src/main/java/io/github/protasm/jvmud/compiler/runtime/`,
  which contains compiler helper classes used by generated code.
- `mudlib/`: future LPC mudlib source, examples, and fixtures.
- `docs/`: static project site.

## Package Layout

The compiler lives under `io.github.protasm.jvmud.compiler`. Keep new compiler
code inside that namespace unless a task explicitly introduces another JVMud
module.

## Current Build State

No root Maven or Gradle build files are present yet. Avoid documenting or
assuming commands such as `mvn test` or `gradle build` until build wiring exists.
The compiler source uses ASM APIs, so future compiler build metadata should add
ASM as a dependency.

## Working Guidance

- Inspect the relevant tree before changing it; this repo is still being shaped
  after migration.
- Keep compiler changes under `compiler/` unless the task is explicitly about
  runtime, mudlib, or docs.
- Keep runtime/server concepts separate from compiler execution helpers.
- Prefer small documentation updates that reflect the current repo state over
  speculative roadmaps.
