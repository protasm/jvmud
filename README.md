# JVMud

JVMud is an experimental LPC/LPMud text-world engine for the JVM. It compiles LPC
to JVM classes and hosts authored worlds through a native model of Places,
Links, Entities, Players, Sessions, Personas, persistence, and time.

The project is one Maven artifact with six core Java package families. Mudlibs
own fiction, rules, commands, presentation, and compatibility policy. Shared
engine and launcher code remains independent of any bundled game.

## Top priority: the code is the product

**The point of JVMud's code is to BE the product, rather than become the product.**
Reading, learning from, extending, and contributing to the source are primary
ways of using JVMud. Clarity, architecture, naming, documentation, and
consistency are product qualities alongside correct behavior. This principle
governs all project work; see the [guiding principles](docs/PRINCIPLES.md).

## Requirements

This source checkout currently requires a Java 21-capable JDK, Maven, and a
POSIX shell (for example, macOS/Linux, or a suitably configured Windows WSL
installation). Java and Maven must be available in your terminal. The launch
scripts compile the engine and resolve its dependencies automatically; the first
run needs access to Maven repositories. A binary distribution can also be built
with precompiled JARs, runtime dependencies, Small Mercies, and LP245; see
[building a distribution](distribution/BUILDING.md). Bundled macOS and Linux packages include Java 21 and require a POSIX shell;
the runtime-free archive requires an existing Java 21 or newer installation. The experimental [preview download](https://jvmud.org/downloads.html) is available
as a tar.gz package.

Run the commands below from the repository root, the folder containing this
README and `pom.xml`.

## Start the engine with Small Mercies

```sh
scripts/jvmud-start smallmercies
```

This starts the engine and **Small Mercies**, the bundled five-room example
world, in one process. No separate engine daemon, database, or account setup is
needed. Wait for the engine endpoint report and the mudlib's `state=RUNNING` status and the address before connecting.
The default is localhost, TCP port **4000**, accessible from this computer only.
Leave the terminal running; press **Ctrl+C** there to stop the server.

The argument is resolved as a config file first, then as
`mudlibs/<arg>/jvmud/<arg>.config`. If neither file exists, startup stops with an error.

To start another compatible world, provide its manifest path:

```sh
scripts/jvmud-start /absolute/path/to/world/jvmud/world.config
```

The engine requires one or more explicit world manifests. To offer both bundled
mudlibs through one listener:

```sh
scripts/jvmud-start smallmercies lp245
```

Every connection receives a menu, even when only one mudlib is configured. Select
by number or game id to enter that mudlib's login flow. There is no default world
and no travel between worlds; reconnect to choose another mudlib.

`execution.application.JVMud` owns application startup and shutdown and starts `TelnetServer`
as a transport component. Each `MudInstance` owns its own execution queue and
clock. Player input, administration, and scheduled work execute serially on that
instance's thread, using its `temporal_tick_interval`. A slow world does not
hold up another world's commands or ticks. A zero interval disables automatic
ticks for that instance. Tick intervals are delays between completed ticks;
busy worlds do not accumulate catch-up ticks.

## Connect and play

In Mudlet or another Telnet-capable MUD client, create a connection with:

| Setting | Value for local play |
| --- | --- |
| Host / server address | `127.0.0.1` |
| Port | `4000` |
| Connection | Plain Telnet / TCP; SSL/TLS disabled |

If a command-line Telnet client is installed, open a second terminal and run:

```sh
telnet 127.0.0.1 4000
```

Choose a guest name (2–16 letters), `male` or `female`, and `warrior` or `mage`.
Try `help`, `look`, `score`, and `north`. Type `quit` to leave Small Mercies;
`//quit` also disconnects the client. Guest characters last for one connection.
See the [Small Mercies walkthrough](https://jvmud.org/manual/index.html#small-mercies)
for the map and combat.

Mudlet is a client that connects over Telnet. JVMud's current listener supports
Telnet text and GMCP negotiation; game-specific GMCP messages depend on the
selected mudlib. This is not a promise of every MUD protocol or client extension.
There is no native TLS or WebSocket listener.

## Accept connections from other computers

Start Small Mercies on all IPv4 network interfaces:

```sh
scripts/jvmud-start --bind 0.0.0.0 --port 4000 mudlibs/smallmercies/jvmud/smallmercies.config
```

For another computer on your LAN, enter the server computer's LAN IP address
in the MUD client and port `4000`. `0.0.0.0` is the server's listening setting;
clients use its actual address. Allow inbound **TCP 4000** through the server's
firewall.

For Internet players, use the server's public hostname or IP address. If the
server is behind a router, forward external TCP port 4000 to that computer's LAN
address and TCP port 4000. Hosting-provider firewalls must allow the port too.
A private LAN address is not directly reachable from the Internet; networks
behind carrier-grade NAT may require a public endpoint or tunnel.

You can bind a specific local interface instead of `0.0.0.0`, or choose a different
ports with `--port 4010 --admin-port 4011`. Give clients the corresponding address and port. Telnet
traffic is unencrypted; Small Mercies uses disposable guests without passwords.

To see all launcher options:

```sh
scripts/jvmud-start --help
```

## Engine and mudlib administration

The engine runs independently of its mudlibs. `scripts/jvmud-start` with no
manifests opens an unauthenticated player menu on localhost:4000, a TLS engine-admin
listener on localhost:4001, and a same-account Unix recovery socket. Each mudlib
runs in a separate worker JVM with its own player/admin port pair.

Bootstrap through the local console:

```sh
scripts/jvmud-console --local
```

At `engine>`, use `help`, `start <name> [player-port admin-port]`, `status`,
`stop <id>`, and `restart <id>`. `admin-create <name>` issues a random token;
`grant <name> engine` or `grant <name> mudlib:<id>` assigns administrative scope.
The registry stores token hashes, independently of player accounts and in-game permissions.

Remote consoles require TLS certificate pinning and an administrator token:

```sh
scripts/jvmud-console                     # localhost:4001
scripts/jvmud-console server.example      # server.example:4001
scripts/jvmud-console server.example 4401 # server.example:4401
```

The console prompts for the trusted certificate fingerprint, administrator name,
and token. Obtain the fingerprint from the engine operator through a trusted
channel; token input is hidden. For scripts, supply `--user`, `--fingerprint`,
and `--token-file`. Explicit `--host` and `--port` options remain supported.

Use `available` to list mudlibs that can be started, then `start smallmercies`
(default player/admin ports 4100/4101). For another mudlib, choose another pair,
for example `start lp245 4200 4201`. Occupied ports return an error; explicit
`0 0` requests OS-allocated ports. Names map to `<mudlib-dir>/<name>/jvmud/<name>.config`; the host
operator sets `--mudlib-dir` when starting the engine (default: the launch root's
`mudlibs` directory). Administration rejects filesystem paths and symlinks that
escape that directory or the selected mudlib tree.

Use a mudlib's admin port for its live object commands. Both engine-menu and
direct player connections enter the same mudlib. Quitting closes the connection;
players reconnect to return to the menu.

See [Engine and administration](docs/ENGINE-ADMINISTRATION.md) for exact bootstrap,
remote access, lifecycle commands, fault isolation, limitations and migration.
Workers launch directly with Java; no external sandbox tool is required. They
retain the host account's OS permissions. Engine logs are stored under `.jvmud/log`; private state and worker
logs default to `~/.jvmud/engine-<player-port>`.

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

## Repository map

| Path | Responsibility |
| --- | --- |
| `src/main/java/io/github/protasm/jvmud/language/` | LPC pipeline, efuns, language runtime, generated-code helpers, and diagnostics |
| `src/main/java/io/github/protasm/jvmud/execution/` | Application supervision and updates (`application`), game concepts (`model`), and running mudlibs (`instance`) |
| `src/main/java/io/github/protasm/jvmud/communication/` | Connection transports, administrative commands, terminal client, structured messages, and text output |
| `src/main/java/io/github/protasm/jvmud/storage/` | Filesystem, JDBC, and administrator-registry storage adapters |
| `src/test/` | Java tests, smoke checks, and documentation validation; see [testing guide](src/test/README.md) |
| `mudlibs/` | LPC content and profiles, separate from the host |
| `mudlibs/smallmercies/` | Bundled toy mudlib and teaching examples |
| `manual/` | Editable User Manual sources |
| `docs/` | Static site, generated manual and Java API, design records |
| `scripts/` | Shared manifest-driven launch and development tools |

`language.runtime` supports generated LPC bytecode; it is distinct from the
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
