# JVMud 0.1.0-preview.5

This experimental preview includes Small Mercies and LP245. The macOS and Linux
packages include Eclipse Temurin Java 21.0.12.1+1-LTS. A POSIX shell is required;
Maven is not needed. The separate runtime-free archive requires Java 21 or newer.

## Changes

Adds explicitly opt-in `compiler.dynamic_types = true`: declared value types use
stable mixed storage while strict typing remains the default. LP245 selects this
policy, replacing all 51 field and 24 local type overrides. Its original source
remains unchanged. One method-varargs rule and separate legacy syntax settings
remain. Shop valuation now correctly prices the frog's crown at 30 gold coins.

The full Java suite passes 560 tests, including strict/dynamic policy isolation,
hosted startup, original LP245 archive checks, Telnet gameplay, and saved-player
restore. Configurations combining dynamic typing with field/local overrides are
rejected; update the LP245 configuration and override file together.

### Retained from preview.4

Adds `jvmud-update`: verified downloads, clean server shutdown and restart,
full installation backups beside the distribution in `backup/`, and rollback
on installation/restart failure. Mudlib content outside `jvmud/` stays untouched;
local adapter conflicts require a manual merge. The bundled Java symlink is now
`jre`. Server logs live under each mudlib's `jvmud/log/` directory.

World perception now reaches entities and locations, with structured native events
and an LP245 adapter for original `catch_tell` handlers. The Go puzzle and Leo's
quest hand-in are covered by regression tests; the spoken Go solution is also
tested through Telnet. Private interface output remains separate.

Launchers now accept a mudlib name: `scripts/jvmud-start lp245` or
`scripts/jvmud-start smallmercies` from the extracted package directory.

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
`vendor-runtime/` (also accessible through `jre/`). Runtime version, original
archive URL, checksum, and upstream release link are in `metadata/runtime.json`.
The upstream release provides the corresponding OpenJDK source archives.
