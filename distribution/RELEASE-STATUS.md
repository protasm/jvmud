# JVMud 0.1.0-preview.3

This experimental preview includes Small Mercies and LP245. The macOS and Linux
packages include Eclipse Temurin Java 21.0.12.1+1-LTS. A POSIX shell is required;
Maven is not needed. The separate runtime-free archive requires Java 21 or newer.

## Changes

Platform packages include the complete vendor JRE and use it by default.
`JVMUD_JAVA_HOME` provides an explicit override. Packages are available for
macOS Apple Silicon and Intel, and Linux ARM64 and x86-64 (glibc).

## Validation

See the accompanying `.validation.json` for each platform package's actual
validation. The packager verifies vendor SHA-256 checksums, runtime architecture,
archive extraction, and preservation of runtime files, permissions, and symlinks.
It runs launcher, formatter, Small Mercies and LP245 login, movement, and attached
administration checks only on the matching build host. Other targets are not
runtime-validated by cross-packaging. JVMud classes target Java 21.
LP245 compatibility remains experimental.

## Licensing status

No project license has yet been declared for JVMud or Small Mercies. This preview
does not introduce a new license grant. LP245 retains its upstream source and
notices. Third-party dependency JARs are shipped unchanged, with any embedded
licenses and notices retained. Build dependencies are in `metadata/pom.xml`.
Bundled Eclipse Temurin retains the vendor's license and legal notices in
`vendor-runtime/` (also accessible through `runtime/`). Runtime version, original
archive URL, checksum, and upstream release link are in `metadata/runtime.json`.
The upstream release provides the corresponding OpenJDK source archives.
