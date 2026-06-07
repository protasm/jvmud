# JVMud

JVMud is an experimental LPC/LPMud text-world engine for the JVM. This
repository is a monorepo: the core runtime lives under `runtime/`, the LPC
compiler lives under `compiler/`, and vanilla LPMUD 2.4.5 mudlib source lives
under `mudlib/`.

`PRINCIPLES.md` is the controlling design document for the engine, and
`GLOSSARY.md` defines JVMud vocabulary. The engine is being built around
JVMud's core concepts: Game, Text, Multiplayer, Interactive, World (Linked
Places, Entities, Movement), Persistence, Temporality, and Presence. JVMud has
a sole LPC/LPMud target: compatibility choices should deepen that target, not
broaden the engine into a generic MUD framework. The upstream mudlib should
remain unchanged by default; compatibility belongs in dedicated mudlib-side shim
objects plus the engine/compiler support needed to host them.

Here "LPMud" means LPC-authored game worlds compiled into live objects that can
be rewritten, recompiled, and reloaded without rebooting the whole game. It does
not mean JVMud adopts legacy driver concepts such as rooms, heartbeats, applies,
call_outs, or master objects as engine ontology.

## Repository Layout

| Path | Purpose |
| --- | --- |
| `runtime/` | JVMud engine runtime source. It currently contains the first world ontology slice: world, place, link, entity, location, capability, and containment rules. |
| `compiler/` | JVMud compiler Java source. It currently contains the LPC scanner, preprocessor, parser, semantic analysis, IR, bytecode compiler, engine function interfaces, and host-facing runtime loader classes. |
| `server/` | JVMud game-server source. It boots a mudlib, manages shared runtime/world state, and accepts Telnet player sessions. |
| `cli/` | JVMud local admin CLI source. It is a single-user command-line tool for filesystem navigation, object loading, inspection, invocation, and mutation. |
| `mudlib/` | Vanilla LPMUD 2.4.5 mudlib content. Treat upstream files as read-only unless an explicit style or formatting change is requested; add compatibility through dedicated independent shim objects. |
| `docs/` | Static project site published from simple HTML. |

## Runtime Status

The top-level runtime module is now buildable and contains the first
engine-owned world model:

- `World`
- `Place`
- `Link`
- `Entity`
- `Location`
- `Capability`
- `WorldRuntime`

`WorldRuntime` owns single containment: every `Entity` has one immediate
`Location`, movement updates the containment graph, and containment cycles are
rejected. It also owns navigable links between places, so the engine models a
world as connected containment rather than only an assemblage of isolated
places. Links are strictly place-to-place; entities can be contained by places
or other entities, but entities are not link endpoints.

## Compiler Status

The compiler source is present at:

```text
compiler/src/main/java/io/github/protasm/jvmud/compiler/
```

The compiler is now under the JVMud umbrella package,
`io.github.protasm.jvmud.compiler`.

Important compiler packages include:

- `preproc`: include resolution, macro expansion, conditional directives, and source mapping.
- `scanner` and `token`: lexical analysis and token model.
- `parser`: Pratt parser, parselets, AST nodes, and LPC type/operator models.
- `semantic` and `ir`: semantic analysis, type checking, and typed intermediate representation.
- `bytecode`: JVM bytecode generation using ASM.
- `efun`, `runtime`, and `exec`: engine function contracts, generated-code helpers, class loading, and host-facing runtime APIs.
- `pipeline`: orchestration for preprocessing, scanning, parsing, semantic analysis, IR lowering, and bytecode generation.

`io.github.protasm.jvmud.compiler.JvmudCompiler` is the current facade and
command-line entry point. It compiles one LPC source file to a JVM class file
when invoked with:

```text
JvmudCompiler <source-file> [output-dir]
```

## Build Notes

The repository now has Maven build wiring for `runtime`, `compiler`, `server`,
and `cli`. Run the current baseline with:

```text
mvn test
```

The compiler module depends on ASM for bytecode generation and JUnit Jupiter for
tests. The current test suite includes end-to-end compiler/runtime smoke tests
and an informational mudlib compatibility scan. The scan writes:

```text
compiler/target/jvmud-mudlib-compatibility.md
```

That report is deliberately non-failing: it records current parser, semantic,
function, and runtime gaps while keeping the green build useful.

See `GLOSSARY.md` for JVMud terminology.
See `ROADMAP.md` for the full-stack project waypoints.
See `docs/ENGINE_MUDLIB_CONTRACT.md` for the native JVMud boundary between the
engine and mudlib compatibility layer.

## Engine-First Development

Development should start from `PRINCIPLES.md`, then make compiler, runtime, CLI,
and compatibility choices fit that model. The mudlib is content for JVMud, not a
constraint that forces the engine to recreate every legacy legacy LPC engine behavior.
At the same time, the vanilla mudlib source is upstream material and should not
be rewritten merely to compensate for JVMud gaps.

When a conflict appears, prefer:

- engine semantics that clearly model world, place, entity, location,
  containment, presence, perception, persistence, and time;
- compiler and runtime support for the sole LPC/LPMud target rather than
  alternate mudlib languages or generic MUD abstractions;
- live object reload semantics for LPC-authored game code without turning
  legacy LPMud driver vocabulary into JVMud engine concepts;
- compiler/runtime support that can host legacy LPC content without distorting
  the JVMud ontology;
- dedicated mudlib-side compatibility shims, such as mfun objects, shadow, or
  adapter objects, instead of broad edits to upstream mudlib files;
- JVMud-native engine operation names, with legacy LPC method and engine function names
  translated by compatibility shims;
- small bridge APIs only where they keep current LPC content usable.

## Admin CLI And Game Server

The `cli` module provides a local, single-user admin shell backed by the real
object runtime. After building, run it with:

```text
./jvmud-admin
```

The launcher compiles the CLI and compiler modules, then starts the shell with
the local build output. Optionally pass a mudlib root as the first argument.

The shell is admin-only: every input line is parsed as an admin command. Commands
include `boot`, `call`, `cat`, `cd`, `clone`, `destruct`, `inspect`, `load`,
`look`, `ls`, `move`, `objects`, `pwd`, `reload`, `verbosity`, `where`, and
`quit`. Some commands have single-character shortcuts; run `help` in the shell
to see the current alias list and per-command usage notes.

The CLI includes a mudlib-rooted virtual filesystem. If the mudlib root is
`/Users/jonathan/Projects/jvmud/mudlib`, then CLI path `/` maps to that real
directory. Filesystem commands cannot navigate above the mudlib root.

Use `verbosity quiet`, `verbosity normal`, or `verbosity watch` to control shell
output. `watch` prints compiler stage progress for commands such as `load` and
`clone`, which is useful when inspecting parser, analyzer, lowering, or bytecode
failures.

The `server` module provides the player-facing game server path. Start a mudlib
as a persistent Telnet target with:

```text
./jvmud-start [-mudlib-dir mudlib] [-port 4000] [-host localhost] [-config jvmud/config]
```

All flags are optional. By default it serves `mudlib` on `localhost:4000` using
`jvmud/config`. Starting this process boots one shared runtime and world; each
Telnet connection attempts to attach a configured mudlib player object and falls
back to a host-owned persona if that path fails. Player/world input is routed
through LPC `init`, `add_action`, and `add_verb` registrations on nearby or
carried objects. Telnet slash commands are limited to session controls such as
`/help` and `/quit`; admin inspection and object mutation stay in the admin CLI.
This is still an early development listener: session-to-session messaging,
output isolation between participants, and production networking policy belong
to later server slices.

To run the startup smoke test that launches `jvmud-start`, connects over TCP,
verifies that the configured `obj/player` mudlib player object attaches, drives
the vanilla login prompts through captured input, and checks `look` plus
`north`:

```text
scripts/smoke-jvmud-start.sh
```

## Development Notes

- Keep changes scoped to the relevant top-level area (`compiler/`, `runtime/`,
  `mudlib/`, or `docs/`).
- Use `PRINCIPLES.md` as the source of truth for engine concepts. Do not edit
  vanilla mudlib files unless explicitly asked for style or formatting changes;
  prefer dedicated compatibility shim objects and engine/compiler support.
- Treat `runtime/` and `mudlib/` as intentionally separate from compiler internals:
  compiler helpers used by generated bytecode currently remain under
  `compiler/src/main/java/io/github/protasm/jvmud/compiler/runtime/`.
- Prioritize readability alongside functionality. Prefer clear names, focused
  tests, and short comments explaining non-obvious legacy LPC engine semantics or
  bytecode/runtime lifecycle rules.
