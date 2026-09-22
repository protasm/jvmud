# JVMud 0.1.0-preview.9

This preview includes Small Mercies and LP245.

## Changes

The engine provides an interactive mudlib directory: numbered entries show direct
Telnet addresses and optional owner-configured descriptions. Players connect to
the mudlib ports for gameplay; the engine no longer forwards player connections.
Use `--public-host` to advertise the public hostname.

Each mudlib runs in its own worker JVM. Worker diagnostic readers use dedicated
threads so idle process pipes cannot exhaust Java 21 virtual-thread carriers on
small servers. Workers retain the host account's OS permissions; they provide
fault isolation, not an OS security sandbox. Java is the only runtime prerequisite.

`jvmud-console --owner` replaces `--local`: the engine state directory's OS owner
has full engine access through a Unix socket. Named administrators use TLS,
tokens and explicit grants. Java packages separate language, execution,
communication and storage.

## Updating

Use the existing installation's `scripts/jvmud-update`. Record the current
mudlib names and port pairs first. The updater restarts the engine with its
recorded launch arguments; mudlibs started through administration must be started
again afterward. See README.md for the exact commands and backup boundaries.

## Validation

The distribution builder runs the Java suite, extracted-archive gameplay and
administration checks, and update/rollback checks before writing checksums.
Platform .validation.json files record checks actually completed. Cross-packaging
verifies archive and vendor-runtime integrity but does not establish execution
on that platform. LP245 compatibility remains experimental. Classes target Java 21.

## Update restart report

Preview.9 records the running mudlibs before orderly engine shutdown and prints
original port pairs and manual restart commands after update or rollback.
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
