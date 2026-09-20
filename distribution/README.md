# JVMud binary distribution

JVMud is an experimental LPC/LPMud engine. This package includes the compiled
engine, its runtime dependencies, launchers, the Small Mercies example world,
and the LP245 mudlib with its JVMud compatibility bridge.
See `RELEASE-STATUS.md` for this preview's validation and licensing status.

## Requirements

Use a POSIX terminal and the package matching your operating system and processor.
The macOS and Linux packages bundle Eclipse Temurin Java 21; no Java installation
is needed. Linux packages target glibc systems, not Alpine/musl. The runtime-free
`-bin` archive instead requires Java 21 or newer (also usable in Windows WSL).
Maven, a source checkout, and Internet access are not needed to run these packages. A MUD client is needed to play. Native Windows launchers
are not included.

## Install in a stable directory

Keep version numbers on downloads and backups, and use `current` for the running
installation. In the writable folder containing your download, run the following
Apple Silicon example; substitute your package filename for another platform:

```sh
mkdir current &&
tar -xzf jvmud-0.1.0-preview.8-test.20260920-macos-aarch64.tar.gz --strip-components=1 -C current &&
cd current
```

`mkdir` deliberately fails if `current` already exists, preventing extraction
over an existing installation. Use the updater below for subsequent releases.
For an EC2/Linux server, choose a writable parent directory and the
`-linux-x64.tar.gz` package for x86-64 instances, or `-linux-aarch64.tar.gz` for
ARM64.

Keep `scripts/`, `lib/`, `mudlibs/`, `jre`, and `vendor-runtime/` together when
present. If using the ZIP, rename its extracted `jvmud-<version>` folder to
`current` before starting any server; do not replace an existing `current`.
If a ZIP extractor removed executable permissions, run `chmod +x scripts/jvmud-*`.

## Start and play

```sh
scripts/jvmud-start smallmercies
```

Wait for the engine endpoint report and the mudlib's `state=RUNNING` status, then connect a Telnet-capable MUD client to
`127.0.0.1`, port `4000`, with SSL/TLS disabled. Select Small Mercies from the menu,
then choose a guest name (2–16 letters),
`male` or `female`, and `warrior` or `mage`. Try `help`, `look`, `score`, and `north`.
Type `quit` to disconnect. Guests last for one connection. Stop the server with
Ctrl+C in its terminal.

The default listener accepts connections only from this computer. To host for
other computers, use `--bind 0.0.0.0 --port 4000` before the manifest path and
configure the server's firewall/router. Clients use the server's actual address.
Telnet traffic is unencrypted.

## Administration consoles

Start the engine with or without initial mudlibs. Its player/admin ports default
to localhost:4000/4001. Bootstrap using the same OS account:

```sh
scripts/jvmud-console --local
```

`--local` uses `~/.jvmud/engine-4000/engine.sock` and your OS account, without a
token or fingerprint. For custom state, use `--socket <state-directory>/engine.sock`.

Use `help`, `admin-create <name>`, `grant <name> engine`, and
`start <manifest> [player-port admin-port]`. Named administrator tokens and grants
are engine-owned, independent of mudlib player accounts. Remote consoles use TLS
and an explicitly trusted certificate fingerprint:

```sh
scripts/jvmud-console                     # localhost:4001
scripts/jvmud-console server.example      # server.example:4001
scripts/jvmud-console server.example 4401 # server.example:4401
```

The console prompts for the trusted certificate fingerprint, administrator name,
and token (without echo). Obtain the fingerprint from the engine operator.
For scripts, supply `--user`, `--fingerprint`, and `--token-file` explicitly.
The `--host`, `--port`, and local `--socket` forms remain available.

Use a mudlib's admin port for its live object commands. Each mudlib runs in its
own sandboxed worker JVM. Linux requires `/usr/bin/bwrap` and user namespaces;
macOS requires `sandbox-exec`. Unsupported sandbox startup fails closed. Player
quits close the connection; reconnect for the engine menu.

## Run LP245

Stop Small Mercies first, or choose another port:

```sh
scripts/jvmud-start --port 4010 --admin-port 4011 lp245
```

Connect to `127.0.0.1:4010`, select LP245, and follow its character creation prompts.
Its editable LPC source and compatibility settings are under `mudlibs/lp245/`;
see `mudlibs/lp245/jvmud/README.md` for the porting details. Compatibility remains
experimental. Character saves belong to this extracted copy; keep them when
upgrading. Character saves and old logs are excluded; authored wizard objects
under `players/` and upstream name reservations in `banish/` are retained.

## Use your own world

Pass its manifest to `scripts/jvmud-start`. The argument is resolved as a config
file first, then as `mudlibs/<arg>/jvmud/<arg>.config`; if neither file exists,
startup stops with an error. Launchers preserve your working
directory: relative arguments resolve from the terminal's current directory.
Use absolute paths when launching from elsewhere. Bundled packages use their own
runtime, ignoring `JAVA_HOME`. Set `JVMUD_JAVA_HOME` to explicitly override it.
Runtime-free packages use `JVMUD_JAVA_HOME`, then `JAVA_HOME`, then `java` on `PATH`. Use Java's
`JAVA_TOOL_OPTIONS` for JVM options such as `-Xmx1g`.

```sh
scripts/jvmud-start --help
scripts/jvmud-console --help
scripts/jvmud-format --help
```

The formatter edits the LPC files supplied to it. Back up your world before
formatting. Use the updater below to upgrade an installed distribution.

Read the [User Manual](https://jvmud.org/manual/index.html) and
[Small Mercies walkthrough](https://jvmud.org/manual/index.html#small-mercies).
Manual commands prefixed with `scripts/` work here; Maven build commands apply
to source checkouts only. Source and issues: https://github.com/protasm/jvmud.

## Update an installed distribution

From your existing `current` directory:

```sh
scripts/jvmud-update --check
scripts/jvmud-update
```

The updater downloads the latest matching package over HTTPS and checks its
SHA-256 before stopping anything. It gracefully stops servers started by this
installation's launcher, waits for their player-save hooks to finish, and makes
a verified full backup in `../backup/<installation-name>-<timestamp>/`.
It replaces JVMud engine files, launchers, bundled Java, and shipped files under
each mudlib's `jvmud/` directory. All other mudlib content and saved files stay in
place. Local changes to an adapter/configuration file are retained if the release
has not changed it. Locally edited Markdown documentation is archived in the full backup and replaced
with the release copy without blocking updates. Configuration comparisons ignore comments, blank lines, and
spacing around setting separators, while preserving setting values and order.
New packages include verified configuration baselines; for older hash-only
packages the updater tries the previous version's `-bin.tar.gz` in the same
download directory and checks recovered files against the installed hashes.
If that baseline is unavailable, ambiguous configuration changes still require
manual review. All conflicting adapter/configuration paths are reported together
before servers stop. Replaced files, including local configuration comments,
remain available in the full backup.

Servers restart with their recorded arguments, working directories, and Java
settings. Run the updater as the same OS user, with any game-specific environment
variables still available. Players reconnect after the restart. The installation
directory keeps its existing name, so `current` remains stable across releases.
`jvmud-update --check` reports the installed version; the folder name does not.
Existing version-named installations still work. Do not rename one while servers
are running: stop every server, confirm clean shutdown and saves, move the folder
without overwriting another installation, and relaunch from the new location with
the same options. Update any service definitions or external absolute paths too. The bundled JRE is accessed through `jre`, a symlink into `vendor-runtime`.

Server output is displayed in the terminal and appended to
`.jvmud/log/server-<port>.log`. Updater restart logs go in that same engine log
directory. Worker logs are kept in the private engine state directory. Backups include the installation's files, including saves,
configuration and logs; external files, mounted worlds and external databases need
separate backups. External mudlib symlinks require a manual update.

A failed installation or restart restores the previous JVMud files and attempts
to restart the prior servers. An interrupted update leaves recovery details in
`.jvmud/update-in-progress.json`; use the listed backup and paths to restore only
the affected engine/adapter files before removing that marker. Do not overwrite
newer player saves during recovery. Never run two updaters at once or update
under a service supervisor that automatically respawns stopped processes.

Older previews without `jvmud-update` need one manual migration: stop their
servers, extract the new package, and transfer your saved data and local changes.
Future updates can then use the command above. Backups are retained until you
choose to remove them.
