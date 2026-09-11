# JVMud

JVMud is an experimental LPC/LPMud text-world engine for the JVM. It compiles LPC
to JVM classes and hosts authored worlds through a native model of Places,
Links, Entities, Players, Sessions, Personas, persistence, and time.

The project is one Maven artifact with six core Java package families. Mudlibs
own fiction, rules, commands, presentation, and compatibility policy. Shared
engine and launcher code remains independent of any bundled game.

## Documentation

Read from the system design down to the details:

1. [Design principles](docs/PRINCIPLES.md) — the controlling design document.
2. [Architecture](https://jvmud.org/architecture.html) — responsibility and execution boundaries.
3. [Core packages](https://jvmud.org/packages.html) — package ownership and API entry points.
4. [User Manual](https://jvmud.org/manual/index.html) — installation, play, operation,
   LPC authoring, troubleshooting, and command/configuration reference.
5. [Java API](https://jvmud.org/api/index.html) — generated contracts for all core packages.

The [documentation home](https://jvmud.org/documentation.html) groups reader paths
and references. The editable manual is [manual/index.adoc](manual/index.adoc);
[documentation maintenance](docs/README.md) explains how to build and preview the
local site. Website changes appear online only after publication.

## Run locally

Use a Java 21-capable JDK, Maven, and a POSIX shell. From the checkout root:

```sh
mvn -DskipTests compile
scripts/jvmud-start mudlibs/smallmercies/jvmud/smallmercies.config
```

This starts **Small Mercies**, the bundled toy mudlib: five rooms, friendly NPCs,
a goose to spar with, and no database or account setup. The launcher still
selects content explicitly through its manifest.
Wait for the listening message, then connect a Telnet-capable client to
`127.0.0.1:4000`. Choose a guest name, male/female, and warrior/mage, then try `help` and `score`.
Guest characters last for one connection. See the
[Small Mercies walkthrough](https://jvmud.org/manual/index.html#small-mercies)
for the map, communication, and combat. Ctrl+C in the
server terminal stops the listener. The launcher requires a manifest and does
not accept a port argument.

For a local admin sandbox:

```sh
scripts/jvmud-admin mudlibs/smallmercies/jvmud/smallmercies.config
```

Run `help` inside the shell. This is a separate runtime, not a connection to the
Telnet server. See the User Manual for object loading, inspection, and reload.

## Repository map

| Path | Responsibility |
| --- | --- |
| `src/main/java/io/github/protasm/jvmud/engine/` | World, identity, time, mudlib boundary, output, and support |
| `src/main/java/io/github/protasm/jvmud/compiler/` | LPC pipeline, efuns, generated-code helpers, and execution APIs |
| `src/main/java/io/github/protasm/jvmud/instance/` | Boot, hosted worlds, Personas, lifecycle dispatch, and routing |
| `src/main/java/io/github/protasm/jvmud/transport/` | Telnet sessions and protocol mechanics |
| `src/main/java/io/github/protasm/jvmud/persistence/` | Filesystem and JDBC storage adapters |
| `src/main/java/io/github/protasm/jvmud/cli/` | Local admin shell |
| `src/test/java/` | Java tests mirroring package ownership |
| `mudlibs/` | LPC content and profiles, separate from the host |
| `mudlibs/smallmercies/` | Bundled toy mudlib and teaching examples |
| `manual/` | Editable User Manual sources |
| `docs/` | Static site, generated manual and Java API, design records |
| `scripts/` | Shared manifest-driven launch and development tools |

`compiler.runtime` supports generated LPC bytecode; it is distinct from the
engine's world model. These packages are not separately released Maven modules.

## Verification and status

```sh
mvn test
```

Some integration checks require external services and configured environment
variables. Inspect missing prerequisites separately from Java failures. A live
smoke test should verify the selected profile's entry, commands, movement,
disconnect, and expected persistence with disposable state.

A successful compile, informational compatibility report, and live gameplay test
prove different things. JVMud remains experimental. See [deferred work](docs/DEFERRED_WORK.md),
[the roadmap](docs/ROADMAP.md), and [documentation gaps](docs/DOCUMENTATION_GAPS.md).

## Contributing

Read [AGENTS.md](AGENTS.md), [design principles](docs/PRINCIPLES.md),
[the glossary](docs/GLOSSARY.md), and [the engine–mudlib contract](docs/ENGINE_MUDLIB_CONTRACT.md).
Preserve upstream mudlib material; prefer dedicated profile shims and native
engine behavior. Keep untyped LPC methods and parameters as compiler errors.
Keep profile-specific launchers under the owning mudlib, and document non-obvious
Java behavior with Javadocs. Test at the changed layer and replay a live route
when runtime behavior changes.
