# Testing JVMud

Keep repository-owned tests, fixtures, and validation scripts in this tree.
Production mudlibs contain playable content and runtime profiles.

- `java/`: JUnit tests, organized by Java package.
- `scripts/lp245/smoke-start`: starts a disposable LP245 copy, checks login and movement, and stops it.
- `scripts/smallmercies/smoke-play.py`: checks two guests against an already running Small Mercies server on port 4000.
- `scripts/check-docs.py`: checks generated documentation links and anchors.
- `scripts/distribution-smoke.py`: checks an extracted binary archive without Maven,
  including Small Mercies and LP245 login/movement and attached administration.
  Run through `python3 scripts/build-distribution.py` from the repository root.

Run from the repository root:

```sh
mvn test
src/test/scripts/lp245/smoke-start
# With Small Mercies running in a separate terminal:
python3 src/test/scripts/smallmercies/smoke-play.py
python3 src/test/scripts/check-docs.py
```

Some integration tests still reference the formerly bundled external mudlibs and
require their configuration and services. Their relocation needs a separate
integration-path update; do not interpret missing sources as engine regressions.

Generated output belongs in ignored `target/`: JUnit uses `surefire-reports/`,
and live smoke runs use `test-artifacts/`. Successful LP245 smoke runs remove
their disposable world; failed runs retain it with `listener.log` for diagnosis.
Never run smoke checks against existing player saves or put disposable fixtures
in a production mudlib. Keep future reusable fixtures under `src/test/resources/`.

The shared preload scanner in `scripts/scan-mudlib-preload-compile.sh` is an
operator diagnostic backed by the production CLI, so it remains with runtime
tools. Testing documentation remains with the manual that explains it.
