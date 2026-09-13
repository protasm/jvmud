# Building a JVMud distribution

From a source checkout, with a Java 21-capable JDK, Maven, and Python 3 (3.12 or newer for bundled packages) and curl:

```sh
python3 scripts/build-distribution.py --with-jre
```

This runs the Maven tests, builds the runtime archives, exercises each extracted
archive, and writes SHA-256 checksums only after validation succeeds. Outputs
are `target/jvmud-<version>-bin.tar.gz`, `target/jvmud-<version>-bin.zip`, and a
matching `.sha256` file for each. A separate `target/jvmud-<version>-sources.jar`
contains the Java source and has its own checksum. For packaging alone, use
`mvn -Pdistribution verify`; this does not run the extracted-archive checks.

The archive contains precompiled JVMud and runtime dependency JARs in `lib/`,
POSIX launchers in `scripts/`, an end-user README, build metadata, and editable
Small Mercies and LP245 mudlibs. Java source, Maven, a Java runtime, test fixtures,
local player saves, and logs are not included in the runtime-free archive. LP245's original name reservation
data is included. Compatibility files remain separate from upstream sources.

The distribution launchers preserve the caller's working directory. Source
checkout launchers still build using Maven and run from the repository root.

## Bundled packages

`--with-jre` additionally builds `target/jvmud-<version>-<platform>.tar.gz` for
`macos-aarch64`, `macos-x64`, `linux-aarch64`, and `linux-x64`. Each includes a
complete vendor JRE, a SHA-256 sidecar, and a separate `.validation.json` report.
Checksums identify archive contents; the report distinguishes structural checks
from execution on a matching host. Run the smoke test on each matching platform
before claiming it is runtime-validated. Linux runtimes require glibc.

Runtime downloads are pinned by URL and SHA-256 in `distribution/runtimes.json`
and cached in `target/runtime-downloads`. Update the pins for Java security
releases and rebuild under a new JVMud version. Vendor legal files are retained.
After a successful base build, packages can be rebuilt with Python 3.12+:

```sh
python3 scripts/bundle-distributions.py
# Or a single target:
python3 scripts/bundle-distributions.py --targets linux-x64
```

Omit `--with-jre` to build only the runtime-free archives and source JAR.
No native installers, signing, notarization, or website publication are performed.

## Website publication

The website offers the four bundled platform tar.gz packages and the optional
runtime-free tar.gz, each with its SHA-256 checksum.
ZIP and Java source JAR outputs remain local build artifacts; do not copy them
to the website or attach them to a public release.

For a new preview, select a unique version in `pom.xml`, update
`RELEASE-STATUS.md` with the actual validation and licensing status, then run the
build script. Copy only the tar.gz and its checksum into `docs/downloads/` and
update `docs/downloads.html`. Commit and push the prepared website to `main`;
GitHub Pages publishes `docs/`. Verify the public archive against its checksum.
Never overwrite an existing versioned archive with different contents.

The build script creates local artifacts only. It does not publish a release or
change the live website. Do not label an untested platform as verified.
