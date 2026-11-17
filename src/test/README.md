# Testing JVMud

Keep repository-owned tests, fixtures, and validation scripts in this tree.
Production mudlibs contain playable content and runtime profiles.

- `java/`: JUnit tests, organized by ownership. `language/` covers compiler and
  language contracts; `engine/instance/lpc/` covers hosted LPC execution;
  `engine/application/` exercises real workers and endpoint authentication.
  `PackageArchitectureTest` checks the four package families and dependency rules.
- `scripts/lp245/smoke-start`: copies LP245 into disposable state, starts an engine
  and worker, discovers the direct player port, checks login and movement, and stops them.
- `scripts/smallmercies/smoke-play.py`: checks two guests against an already running
  Small Mercies mudlib at the direct player port supplied with `--port`.
- `scripts/check-docs.py`: checks generated documentation links and anchors.
- `scripts/distribution-smoke.py`: checks an extracted binary archive without Maven,
  including Small Mercies and LP245 login/movement and attached administration.
  Run through `python3 scripts/build-distribution.py` from the repository root.

The standalone LP245 smoke checks guild entry, advancement costs and the book in one session,
then uses another disposable character for the bridge, wilderness and orc route. Leaving the
adventurers guild currently leaves the player inside; this limitation also reproduces in the
pre-reorganization preview.9 build. The smoke does not claim to verify that exit. Failed runs
retain `transcript.log` beside `listener.log` in their disposable test directory.

Run from the repository root:

```sh
mvn test
src/test/scripts/lp245/smoke-start
# With a dedicated Small Mercies instance running in a separate terminal:
# Replace <player-port> with its playerPort from the startup report.
python3 src/test/scripts/smallmercies/smoke-play.py --port <player-port>
python3 src/test/scripts/check-docs.py
```

The Small Mercies script defaults to host `127.0.0.1`; use `--host` explicitly for
another test host. Port 4000 is normally the engine directory and does not enter
the mudlib. Use disposable guests and a dedicated player endpoint.

External-profile checks may require configured sources or database services.
Inspect missing prerequisites separately from compiler and engine failures.

Generated output belongs in ignored `target/`: JUnit uses `surefire-reports/`,
and live smoke runs use `test-artifacts/`. Successful LP245 smoke runs remove
their disposable world; failed runs retain it with `listener.log` for diagnosis.
Never run smoke checks against existing player saves or put disposable fixtures
in a production mudlib. Keep future reusable fixtures under `src/test/resources/`.

The shared preload scanner in `scripts/scan-mudlib-preload-compile.sh` is an
operator diagnostic backed by the production CLI, so it remains with runtime
tools. Testing documentation remains with the manual that explains it.
