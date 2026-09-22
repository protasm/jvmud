# Engine, mudlibs and administration

`execution.application.JVMud` owns application lifetime. It starts the public player directory, the
TLS engine-admin listener, and a local recovery socket before booting any mudlib.
An engine with zero running mudlibs is a normal, administratively accessible state.

## Start an engine

```sh
scripts/jvmud-start
```

The default player/admin pair is `localhost:4000` and `localhost:4001`. Use
`--bind 0.0.0.0` to make both TCP endpoints reachable remotely. `--port` and
`--admin-port` select different engine ports. The engine prints its endpoints and
TLS certificate SHA-256 fingerprint. Keep the fingerprint for distribution to
administrators through a trusted channel.

State defaults to `~/.jvmud/engine-<player-port>`; `--state-dir <path>` overrides it.
This private directory contains the administrator registry, TLS private key,
local `engine.sock`, ownership lock, and bounded worker logs. Keep it outside all
mudlib trees. Two engines must use different state directories and port pairs.

Optional positional mudlib manifests or bundled names are booted after engine
services are ready:

```sh
scripts/jvmud-start smallmercies lp245
```

Each receives available player/admin ports, displayed in status. A failed mudlib
startup leaves the engine and other mudlibs available. Initial launch manifests
are supplied again when restarting the engine; stopped/failed instance records
are retained for the current engine lifetime, not persisted as an autostart list.

## Owner and administrator access

The **owner** of an engine instance is the operating-system account that owns
its private state directory, normally the account that starts the engine. Owner
access uses a Unix-domain socket on that machine and grants full engine authority,
including creation and recovery of administrator identities. Run the console on
the host, directly or through SSH, as that account. It does not require Unix root
or `sudo`, and ownership is not a grant in the administrator registry.

An **administrator** is a named JVMud identity with an access token and explicit
grants. Administrators connect over TCP/TLS, either remotely or through localhost.
An `engine` grant includes all mudlibs and administrator management; a
`mudlib:<id>` grant is limited to that mudlib. Administrator identities are separate
from OS accounts and player accounts.

| Console invocation | Transport | Identity and authority |
| --- | --- | --- |
| `jvmud-console --owner` | Default engine's Unix socket | OS owner; full engine authority |
| `jvmud-console --socket <path>` | Specified engine's Unix socket | Same owner authentication |
| `jvmud-console` | TCP/TLS to `localhost:4001` | Named administrator and its grants |
| `jvmud-console <server> [<port>]` | TCP/TLS to the server (default port 4001) | Named administrator and its grants |

TLS connections require a trusted server fingerprint, administrator name, and
access token, even when connecting to localhost. Owner connections require none
of those credentials. `--owner` must stand alone; `--socket` still requires a path.

## Bootstrap administrators as the owner

Run the console as the engine's OS account, locally or from an SSH login:

```sh
scripts/jvmud-console --owner
```

`--owner` connects to `~/.jvmud/engine-4000/engine.sock`, the socket created
by a default engine launch. It does not scan for engines or use TLS credentials.
For a custom state directory or another engine player port, use
`scripts/jvmud-console --socket <state-directory>/engine.sock` instead.
The flag must be used alone.

The OS supplies the connecting process's user identity. The engine requires it
to match the private state directory's owner. The socket is not a TCP endpoint.
This path remains available for bootstrap and recovery even with an empty or
misconfigured administrator registry. Root can run the console as that account;
root is not granted an additional authentication exception.

At `engine>`:

```text
admin-create operator
grant operator engine
admin-create builder
grant builder mudlib:smallmercies
```

`admin-create` displays a new random 256-bit token once. Store it securely for the
named administrator. The registry stores only its SHA-256 hash and explicit
grants; it does not contain player accounts or plaintext tokens. `admin-rotate`
replaces a token. `revoke` removes a grant, and `admin-delete` removes an identity.
Existing sessions are rechecked on every command and at one-second intervals;
revocation disconnects idle sessions too. An operation already executing may
finish. Engine authority includes all mudlib scopes and identity management.

## Connect remotely

The console accepts an optional server and administration port:

```sh
scripts/jvmud-console                     # localhost:4001
scripts/jvmud-console server.example      # server.example:4001
scripts/jvmud-console server.example 4401 # server.example:4401
```

All three forms use TLS and prompt for the trusted server certificate fingerprint,
administrator name, and token. Obtain the fingerprint from the engine operator
through a trusted channel. The token is entered without echo. Connection details
are not saved; supply named options to omit individual prompts. Local TCP connections
require administrator credentials too; use the Unix socket above for bootstrap.

The explicit options remain available:

```sh
scripts/jvmud-console --host server.example --port 4001 --user operator \
  --fingerprint <server-certificate-SHA-256>
```

For noninteractive input, supply `--user`, `--fingerprint`, and
`--token-file <private-file>`; do not put tokens in command-line arguments.
TLS 1.2/1.3 protects remote administration. The console requires an explicitly
supplied certificate fingerprint and checks certificate validity. It never
accepts an unknown certificate automatically. The engine generates and retains
its own certificate; changing or renewing it requires securely distributing the
new fingerprint. An existing private key is never regenerated on normal restart.

## Manage mudlibs

```text
help
status
available
start smallmercies 4100 4101
stop smallmercies
restart smallmercies
quit
```

The engine exposes installed mudlibs by name. `available` lists startable names;
`status` and `mudlibs` show instances already registered with this engine.
A name such as `smallmercies` resolves to
`<mudlib-dir>/smallmercies/jvmud/smallmercies.config`. Names contain 1–64 letters,
digits, underscores or hyphens. Absolute paths, traversal and symlinks escaping
the catalog or a mudlib's own tree are rejected. Restarts recheck this boundary
before stopping the existing worker. Local and TLS consoles use the same rules.

The host operator configures this directory when launching the engine:

```sh
scripts/jvmud-start --mudlib-dir /srv/my-mudlibs
```

The default is `mudlibs` under the launch root. The directory may initially be
empty or absent. Initial manifests explicitly supplied by the host operator on
the startup command line remain supported; an externally launched mudlib outside
the configured directory cannot be restarted through administration. Place it in
the managed directory to make it administratively startable and restartable.
The catalog limits administrative manifest selection; it does not restrict the
worker process's operating-system permissions. Host operators control the directory and its contents.

Supply both requested mudlib ports or neither. The `start` command defaults to
player port 4100 and admin port 4101; occupied ports return an error without
stopping the engine. Use an explicit pair for additional mudlibs, or `0 0` to ask
the OS to allocate both ports. Engine defaults remain 4000/4001. `restart` reuses the prior pair. Each mudlib must have a unique `game_id` and
its own non-overlapping filesystem tree. The engine keeps failed/stopped entries
visible. `shutdown` stops the engine and all workers; `quit` only disconnects the
console. Token issuance and grants are immediately persisted with atomic file
replacement. Local-console recovery permits correcting accidental lockouts.

A mudlib's admin port uses the same TLS certificate and administrator registry,
but requires `mudlib:<id>` or engine authority. Its prompt is `mudlib:<id>>` and
its commands operate on that worker's live objects:

```text
objects
inspect room/square
call room/square short
quit
```

The engine owns public admin listeners and checks credentials/grants before
forwarding commands. Workers receive commands through private inherited pipes;
they receive no administrator tokens, grants, or TLS keys. Administrators have
independent working directories and object-handle state within a shared mudlib.

## Player connections

The engine player port is an interactive directory of running mudlibs. It displays
a numbered list. Enter a number to see that mudlib's `Telnet:` connection details
and administrator-configured description. Enter another number to view another
mudlib, `L` to refresh the list, or `Q` to receive a friendly goodbye and disconnect.
Letters are case-insensitive. Selecting a number never creates a player session
or forwards the connection to a worker. Players connect separately to the
mudlib's own player port for login and gameplay.

Set the advertised hostname with `--public-host play.jvmud.org`; for example,
Small Mercies displays `Telnet: play.jvmud.org:4100`. Without a public hostname,
the directory says `Telnet: same host, port 4100`. This avoids advertising a
private address behind NAT. The option only affects display, not bindings, DNS
or firewall rules; the advertised player ports must be reachable separately.

Administrators configure optional blurbs as UTF-8 text files in the engine's
state directory: `descriptions/<mudlib-id>.txt`. For the default engine, Small
Mercies uses `~/.jvmud/engine-4000/descriptions/smallmercies.txt`. Create the
`descriptions` directory if needed. Blank lines and paragraphs are preserved;
a missing or empty file omits the blurb. Edits take effect on the next selection
without restarting the engine or mudlib. These files are managed on the host.

The direct mudlib endpoint preserves the original client address and Telnet/GMCP
traffic. The mudlib owns player authentication and command interpretation.
Quitting, stopping or losing a mudlib closes its player connections. Player
Telnet remains unencrypted; administration uses a separate TLS protocol.

## Isolation and supervision

Each mudlib runs in a separate JVM with a 256 MiB maximum heap, 128 MiB metaspace,
64 MiB direct-memory limit and a two-processor JVM scheduling view. These are JVM
limits, not a total OS memory quota or a hard CPU quota. Out-of-memory terminates
the offending worker. Startup has a 60-second deadline; a worker admin request
has a 15-second deadline, after which an unresponsive worker is terminated.
Stopping gives hooks a bounded opportunity before forced termination. A crash
removes the mudlib from the directory, closes its public endpoints and leaves its
failure visible. The engine and other workers remain available.

Workers are launched directly with the engine's Java runtime. No bubblewrap,
`sandbox-exec`, or enabled Linux user namespaces are required. The engine clears
the worker's inherited environment and gives it its own temporary directory,
but these are execution defaults, not access controls.

Each worker retains the host account's operating-system permissions, including
file and network access. Engine credentials are not sent through the worker
protocol, but the worker process is not prevented by JVMud from accessing files
owned by that same account. Use trusted mudlibs. The administrative catalog and
mudlib filesystem APIs retain their path checks; they do not provide an OS
security boundary. Host-wide resource exhaustion can still affect every process.

macOS and Linux remain the supported hosting platforms. Linux runtime validation
is performed on Linux, not inferred from a Mac build. Native Windows hosting and
launchers have not been validated by this change.

Worker diagnostics are capped at 1 MiB per start in `logs/<id>.log` and replaced
on restart. `--trace-startup-loads` enables worker-side startup tracing. A forced
exit does not guarantee mudlib-defined persistence hooks finish; durability is
still an explicit mudlib/storage responsibility.

## Code responsibilities

- `execution.application`: application configuration, lifecycle, and worker supervision.
- `execution.application.worker`: Java worker launch and private control protocol.
- `execution.application.update`: installation updates and server tracking.
- `execution.model`: worlds, identities, time, and mudlib contracts.
- `execution.instance`: one worker's mudlib assembly, execution queue, and clock.
- `communication.transport`: player and administration connection protocols.
- `communication.admin`: engine and mudlib command languages.
- `communication.console`: shared interactive administration client.
- `storage.admin`: engine-owned identities and grants.
- `language.diagnostics`: standalone compilation diagnostics.

The former `jvmud-cli`, `--admin-game` and `--admin-token-file` interfaces are
replaced by `jvmud-console`, endpoint-specific administration and named
administrator tokens. Old unencrypted administration clients cannot connect to
the new TLS listeners.
