# JVMud 0.1.0-preview.8-test.20260920

Local test candidate for the September 20 engine and package restructuring.
This candidate is not a published release. It includes Small Mercies and LP245.

## Changes under test

The engine supports an empty public menu, separate sandboxed mudlib worker JVMs,
direct mudlib player endpoints, TLS administration with named tokens and scoped
grants, and a same-account Unix recovery socket. Player quits disconnect.
Java packages now separate language, execution, communication, and storage.

Use a fresh installation and disposable player data. Keep engine private state
outside the mudlib trees. Linux requires bubblewrap and unprivileged user
namespaces; macOS requires sandbox-exec. See the packaged README for commands.

## Validation

The distribution builder runs the Java suite, extracted-archive gameplay and
administration checks, and update/rollback checks before writing checksums.
The accompanying platform .validation.json records checks actually completed.
A package for another platform is not runtime-validated by cross-packaging.
Linux runtime validation and the manual walkthrough remain pending.
LP245 compatibility remains experimental. JVMud classes target Java 21.

## Licensing status

No project license has yet been declared for JVMud or Small Mercies. This preview
does not introduce a new license grant. LP245 retains its upstream source and
notices. Third-party dependency JARs are shipped unchanged, with any embedded
licenses and notices retained. Build dependencies are in `metadata/pom.xml`.
Bundled Eclipse Temurin retains the vendor's license and legal notices in
`vendor-runtime/` (also accessible through `jre/`). Runtime version, original
archive URL, checksum, and upstream release link are in `metadata/runtime.json`.
The upstream release provides the corresponding OpenJDK source archives.
