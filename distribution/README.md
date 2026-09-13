# JVMud binary distribution

JVMud is an experimental LPC/LPMud engine. This package includes the compiled
engine, its runtime dependencies, launchers, the Small Mercies example world,
and the LP245 mudlib with its JVMud compatibility bridge.
See `RELEASE-STATUS.md` for this preview's validation and licensing status.

## Requirements

Install Java 21 or newer and use a POSIX terminal on macOS, Linux, or Windows WSL.
Check `java -version`. Maven, a source checkout, and Internet access are not needed
to run this package. A MUD client is needed to play. Native Windows launchers
are not included.

Extract the entire archive into a writable directory. Keep `scripts/`, `lib/`,
and `mudlibs/` together. Open a terminal in the extracted `jvmud-<version>` folder.
If a ZIP extractor removed executable permissions, run `chmod +x scripts/jvmud-*`.

## Start and play

```sh
scripts/jvmud-start mudlibs/smallmercies/jvmud/smallmercies.config
```

Wait for `JVMud mudlib listening on`, then connect a Telnet-capable MUD client to
`127.0.0.1`, port `4000`, with SSL/TLS disabled. Choose a guest name (2–16 letters),
`male` or `female`, and `warrior` or `mage`. Try `help`, `look`, `score`, and `north`.
Type `quit` to disconnect. Guests last for one connection. Stop the server with
Ctrl+C in its terminal.

The default listener accepts connections only from this computer. To host for
other computers, use `--bind 0.0.0.0 --port 4000` before the manifest path and
configure the server's firewall/router. Clients use the server's actual address.
Telnet traffic is unencrypted.

## Administer a running world

```sh
scripts/jvmud-start --admin-port 4100 mudlibs/smallmercies/jvmud/smallmercies.config
# In a second terminal, as the same OS user:
scripts/jvmud-cli --port 4100
```

Try `help`, `objects`, `inspect room/square`, and `quit`. The admin listener is
local-only. Its private credential is created at `~/.jvmud/admin/4100.token` and
removed on normal shutdown. Use `--admin-token-file` on the server and
`--token-file` on the CLI for another location. After an abnormal exit, confirm
the server is stopped before removing a stale token file.

## Run LP245

Stop Small Mercies first, or choose another port:

```sh
scripts/jvmud-start --port 4001 mudlibs/lp245/jvmud/lp245.config
```

Connect to `127.0.0.1:4001` and follow LP245's character creation prompts.
Its editable LPC source and compatibility settings are under `mudlibs/lp245/`;
see `mudlibs/lp245/jvmud/README.md` for the porting details. Compatibility remains
experimental. Character saves belong to this extracted copy; keep them when
upgrading. Character saves and old logs are excluded; authored wizard objects
under `players/` and upstream name reservations in `banish/` are retained.

## Use your own world

Pass its manifest to `scripts/jvmud-start`. Launchers preserve your working
directory: relative arguments resolve from the terminal's current directory.
Use absolute paths when launching from elsewhere. `JAVA_HOME` selects a particular
Java installation; otherwise the launchers use `java` on `PATH`. Use Java's
`JAVA_TOOL_OPTIONS` for JVM options such as `-Xmx1g`.

```sh
scripts/jvmud-start --help
scripts/jvmud-cli --help
scripts/jvmud-format --help
```

The formatter edits the LPC files supplied to it. Back up your world before
upgrading; extract new distributions into separate folders and copy only the
worlds and data you intend to retain.

Read the [User Manual](https://jvmud.org/manual/index.html) and
[Small Mercies walkthrough](https://jvmud.org/manual/index.html#small-mercies).
Manual commands prefixed with `scripts/` work here; Maven build commands apply
to source checkouts only. Source and issues: https://github.com/protasm/jvmud.
