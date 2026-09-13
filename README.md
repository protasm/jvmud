# JVMud

JVMud is an experimental LPC/LPMud text-world engine for the JVM. It compiles LPC
to JVM classes and hosts authored worlds through a native model of Places,
Links, Entities, Players, Sessions, Personas, persistence, and time.

The project is one Maven artifact with six core Java package families. Mudlibs
own fiction, rules, commands, presentation, and compatibility policy. Shared
engine and launcher code remains independent of any bundled game.

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
needed. Wait for `JVMud mudlib listening on` and the address before connecting.
The default is localhost, TCP port **4000**, accessible from this computer only.
Leave the terminal running; press **Ctrl+C** there to stop the server.

The argument is resolved as a config file first, then as
`mudlibs/<arg>/jvmud/<arg>.config`. If neither file exists, startup stops with an error.

To start another compatible world, provide its manifest path:

```sh
scripts/jvmud-start /absolute/path/to/world/jvmud/world.config
```

The engine always requires a world manifest. The repository includes Small
Mercies and LP245; other mudlibs are maintained outside this checkout.

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
port with `--port 4001`. Give clients the corresponding address and port. Telnet
traffic is unencrypted; Small Mercies uses disposable guests without passwords.

To see all launcher options:

```sh
scripts/jvmud-start --help
```

## Connect the CLI to a live server

Enable a separate local admin port when starting the server:

```sh
scripts/jvmud-start --port 4000 --admin-port 4100 mudlibs/smallmercies/jvmud/smallmercies.config
```

In another terminal, connect to that **admin port**:

```sh
scripts/jvmud-cli --port 4100
```

The CLI displays the server's admin port and world path. At `jvmud>`, try:

```text
help
objects
inspect room/square
call room/square short
quit
```

These commands operate on the running server's actual objects, including player
objects. `quit` or Ctrl+D disconnects the CLI; players and the engine keep running.
Stop the server with Ctrl+C in its own terminal. The CLI does not boot a world,
and the `boot` command is unavailable when connected.

Use `help` for loading, inspecting, calling, and reloading LPC objects. Changes
affect the live world. `reload` replaces an object; it does not migrate all
existing state or references, so use it with care for occupied rooms and players.
Administrative commands run in coordination with player commands and world ticks;
a long command can delay gameplay in that server.

Administration is disabled unless `--admin-port` is specified. It listens only
on `127.0.0.1`, independently of the player `--bind` setting. The server creates a
private credential at `~/.jvmud/admin/4100.token`, which the CLI reads automatically
when run by the same OS user. Normal server shutdown removes the credential.
After an abnormal exit, confirm the old server is stopped before removing its
stale token file and restarting. Credentials are never overwritten at startup.

To choose another credential location, pass `--admin-token-file /path/to/key` to
`jvmud-start` and `--token-file /path/to/key` to `jvmud-cli`. The parent directory
must be writable by the server; the key file must not already exist. Keep this
file private. The current credential creation requires POSIX file permissions,
consistent with the launch scripts' macOS/Linux/WSL environment.

## Run several servers

Give each process a distinct player port and admin port. For example, in separate
terminals (replace the second manifest with your own world's path):

```sh
scripts/jvmud-start --bind 0.0.0.0 --port 4000 --admin-port 4100 mudlibs/smallmercies/jvmud/smallmercies.config
scripts/jvmud-start --bind 0.0.0.0 --port 4001 --admin-port 4101 /absolute/path/to/other-world/jvmud/world.config
```

Select the world you administer by its admin port:

```sh
scripts/jvmud-cli --port 4100
scripts/jvmud-cli --port 4101
```

Players use ports 4000 and 4001; administration uses 4100 and 4101. Each process
has its own runtime and shutdown lifecycle. Use separate writable world data for
independent copies of a persistent mudlib.

A manifest can already mount worlds for player transfers, but this is distinct
from hosting independently configured player listeners. Administration currently
targets the primary world of the selected server; it does not select mounted
worlds. Multiple independent listeners in one process are not provided by these
launch commands.

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
| `src/main/java/io/github/protasm/jvmud/engine/` | World, identity, time, mudlib boundary, output, and support |
| `src/main/java/io/github/protasm/jvmud/compiler/` | LPC pipeline, efuns, generated-code helpers, and execution APIs |
| `src/main/java/io/github/protasm/jvmud/instance/` | Boot, hosted worlds, Personas, lifecycle dispatch, and routing |
| `src/main/java/io/github/protasm/jvmud/transport/` | Telnet sessions and protocol mechanics |
| `src/main/java/io/github/protasm/jvmud/persistence/` | Filesystem and JDBC storage adapters |
| `src/main/java/io/github/protasm/jvmud/cli/` | Local admin shell |
| `src/test/` | Java tests, smoke checks, and documentation validation; see [testing guide](src/test/README.md) |
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
