# JVMud 0.1.0-preview.2

This is an experimental preview distribution with Small Mercies and LP245.
Java 21 or newer and a POSIX shell are required. Java and Maven are not bundled;
Maven is not needed to run this package.

## Fix in this preview

Lifecycle dispatch now selects the most-derived hook before adapting arguments.
This fixes LP245 storage-room quicktyper creation when `init(arg)` inherits a
zero-argument `init()`. Upstream LPC source is unchanged. The package smoke test
now obtains a quicktyper through normal play and uses an alias.

## Validation

Both mudlibs passed extracted-package login, movement, and attached administration
checks on macOS with OpenJDK 25. All 541 Maven tests passed. JVMud classes target
Java 21. Java 21 itself, Linux, and Windows WSL have not yet been validated for
this preview. LP245 compatibility remains experimental.

## Licensing status

No project license has yet been declared for JVMud or Small Mercies. This preview
does not introduce a new license grant. LP245 retains its upstream source and
notices. Third-party dependency JARs are shipped unchanged, with any embedded
licenses and notices retained. The build dependency versions are recorded in
`metadata/pom.xml`.
