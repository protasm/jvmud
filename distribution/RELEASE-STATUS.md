# JVMud 0.1.0-preview.10

This preview includes Small Mercies and LP245.

## Changes

Java 25 is the minimum build and runtime version. Platform packages bundle
Eclipse Temurin Java 25; runtime-free packages require an existing Java 25 or
newer installation. Published preview.9 archives retain their Java 21 runtime
and requirements.

Hosted LPC responsibilities now have explicit owners for invocation scopes and
method calls, sessions and Persona attachment, commands, scheduled callbacks, and
world interactions. `WorldRuntime` remains the sole containment graph, reached
through `MudlibWorldProjection`; the language layer remains independent of host
world, session, and storage implementations.

Live object reload preserves object identity and compatible field state,
occupants, sessions, command registrations, and timers. A failed replacement
leaves the working revision available. Authored creation hooks do not rerun.
Language-only efuns and engine services have separate catalogs while retaining
LPC names and compatibility aliases. Upstream LP245 source remains preserved.

## Runtime boundaries

The engine provides an interactive mudlib directory: numbered entries show direct
Telnet addresses and optional owner-configured descriptions. Players connect to
the mudlib ports for gameplay; the engine does not forward player connections.
Use `--public-host` to advertise the public hostname.

Each mudlib runs in its own worker JVM. Worker diagnostic readers use dedicated
platform threads. Workers retain the host account's OS permissions; they provide
fault isolation, not an OS security sandbox.

The engine state directory's OS owner has full engine access through
`jvmud-console --owner` and a Unix socket. Named administrators use TLS, tokens
and explicit grants. Java packages separate language, engine, communication and
storage.

## Updating

Use the existing installation's `scripts/jvmud-update`. Record the current
mudlib names and port pairs first. The updater restarts the engine with its
recorded launch arguments; mudlibs started through administration must be started
again afterward. See README.md for the exact commands and backup boundaries.
An external or explicitly overridden Java runtime must meet the Java 25 minimum
before updating to this preview. Bundled packages supply their own Java 25 runtime.

## Validation

The Java 25 suite passes 679 tests, including connected-player movement,
successful occupied-room reloads, and rejected replacements. Preview.10 packages
are built locally; no preview.10 packages have been published. The public download
manifest continues to identify preview.9.

The distribution builder runs the Java suite, extracted-archive gameplay and
administration checks, and update/rollback checks before writing checksums.
Platform .validation.json files record checks actually completed. Cross-packaging
verifies archive and vendor-runtime integrity but does not establish execution
on that platform. LP245 compatibility remains experimental.

## Update restart report

Since preview.9, the updater records running mudlibs before orderly engine
shutdown and prints original port pairs and manual restart commands after update or rollback.
Initial mudlibs confirmed running after restart are identified separately.
Older installed updaters do not gain this behavior until they have been upgraded.

## Licensing status

No project license has yet been declared for JVMud or Small Mercies. This preview
does not introduce a new license grant. LP245 retains its upstream source and
notices. Third-party dependency JARs are shipped unchanged, with any embedded
licenses and notices retained. Build dependencies are in `metadata/pom.xml`.
Bundled Eclipse Temurin retains the vendor's license and legal notices in
`vendor-runtime/` (also accessible through `jre/`). Runtime version, original
archive URL, checksum, and upstream release link are in `metadata/runtime.json`.
The upstream release provides the corresponding OpenJDK source archives.
