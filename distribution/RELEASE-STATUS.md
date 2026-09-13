# JVMud 0.1.0-preview.1

This is an experimental preview distribution with Small Mercies and LP245.
Java 21 or newer and a POSIX shell are required. Java and Maven are not bundled;
Maven is not needed to run this package.

## Validation

Both mudlibs passed extracted-package login, movement, and attached administration
checks on macOS with OpenJDK 25. All 540 Maven tests passed. JVMud classes target
Java 21. Java 21 itself, Linux, and Windows WSL have not yet been validated for
this preview. LP245 compatibility remains experimental.

## Licensing status

No project license has yet been declared for JVMud or Small Mercies. This preview
does not introduce a new license grant. LP245 retains its upstream source and
notices. Third-party dependency JARs are shipped unchanged, with any embedded
licenses and notices retained. The build dependency versions are recorded in
`metadata/pom.xml`.
