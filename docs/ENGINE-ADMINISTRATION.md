# Engine, mudlibs and administration

`engine.JVMud` owns application lifetime. It starts the public player menu, the
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

## Bootstrap administrators locally

Run the console as the engine's OS account, locally or from an SSH login:

```sh
scripts/jvmud-console --socket ~/.jvmud/engine-4000/engine.sock
```

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

```sh
scripts/jvmud-console --host server.example --port 4001 --user operator \
  --fingerprint <server-certificate-SHA-256>
```

The console prompts for the token without echoing it. For noninteractive input,
use `--token-file <private-file>`; do not put tokens in command-line arguments.
TLS 1.2/1.3 protects remote administration. The console requires an explicitly
supplied certificate fingerprint and checks certificate validity. It never
accepts an unknown certificate automatically. The engine generates and retains
its own certificate; changing or renewing it requires securely distributing the
new fingerprint. An existing private key is never regenerated on normal restart.

## Manage mudlibs

```text
help
status
start /absolute/path/to/smallmercies/jvmud/smallmercies.config 4100 4101
stop smallmercies
restart smallmercies
quit
```

Supply both requested mudlib ports or neither; omitted ports are allocated by the
OS. `restart` reuses the prior pair. Each mudlib must have a unique `game_id` and
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

The engine player port lists ready mudlibs without credentials. Selecting one
enters its normal login flow. The mudlib's own player port goes directly to that
same running instance. Neither entry point creates another mudlib instance.
The engine relays the original client address and preserves Telnet/GMCP traffic.
The mudlib owns player authentication and command interpretation.

Quitting, stopping or losing the selected mudlib closes the connection. There is
no transfer back to the engine menu. Reconnect to choose another mudlib. Player
Telnet remains unencrypted; administration uses a separate TLS protocol.

## Isolation and supervision

Each mudlib runs in a separate JVM with a 256 MiB maximum heap, 128 MiB metaspace,
64 MiB direct-memory limit and a two-processor JVM scheduling view. These are JVM
limits, not a total OS memory quota or a hard CPU quota. Out-of-memory terminates
the offending worker. Startup has a 60-second deadline; a worker admin request
has a 15-second deadline, after which an unresponsive worker is terminated.
Stopping gives hooks a bounded opportunity before forced termination. A crash
removes the mudlib from the menu, closes its public endpoints and leaves its
failure visible. The engine and other workers remain available.

On macOS workers run under `sandbox-exec`: file contents are restricted to system runtime inputs, the declared classpath,
mudlib and scratch paths; engine state is
explicitly denied; writes are limited to the mudlib and scratch; process spawning
and signaling other processes are denied; outbound connections are denied and
inbound networking is loopback-only. System runtime reads remain permitted for
macOS dynamic linking. This platform sandbox requires `sandbox-exec`.

On Linux workers require `/usr/bin/bwrap` (bubblewrap) and enabled unprivileged
user namespaces. Workers receive private process/IPC namespaces and a filesystem
view containing read-only system/JRE/classpath inputs, their writable mudlib and
scratch, and private `/tmp` and `/proc`. Host network access remains available
for the private loopback relay; engine credentials and the Unix recovery socket
are absent from the filesystem view. No capabilities are retained. The engine
fails mudlib startup if the sandbox cannot start; there is no unsandboxed fallback.

The macOS implementation is integration-tested on the development host. The
Linux launcher needs runtime validation on a Linux host with bubblewrap. Native
Windows is not currently supported by this sandbox implementation. Database or
other external-service access requires additional platform-specific capability
design; macOS workers cannot currently make outbound database connections.

Worker diagnostics are capped at 1 MiB per start in `logs/<id>.log` and replaced
on restart. `--trace-startup-loads` enables worker-side startup tracing. A forced
exit does not guarantee mudlib-defined persistence hooks finish; durability is
still an explicit mudlib/storage responsibility.

## Code responsibilities

- `engine`: application configuration, lifecycle and worker supervision.
- `engine.worker`: OS sandbox launch, bounded private control protocol and process ownership.
- `instance`: one worker's mudlib assembly, runtime, execution queue and clock.
- `transport`: public menu/direct relays, Telnet handling, TLS/Unix administration transport.
- `admin`: separate engine and mudlib command languages.
- `console`: shared interactive administration client.
- `persistence.admin`: engine-owned identities and grants.
- `maintenance`: installation updates and standalone compilation diagnostics.

The former `jvmud-cli`, `--admin-game` and `--admin-token-file` interfaces are
replaced by `jvmud-console`, endpoint-specific administration and named
administrator tokens. Old unencrypted administration clients cannot connect to
the new TLS listeners.
