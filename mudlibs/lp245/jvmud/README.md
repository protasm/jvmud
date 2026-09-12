# JVMud bridge for original Lysator LP245

This writable directory is the home for JVMud porting work. All 582 files
outside it remain byte-identical to the original Lysator archive and read-only.
The baseline and archive provenance are in
`src/test/resources/lp245-lysator/` at the repository root.

## Starting files

- `lp245.config`: manifest for this mudlib, its include paths, player object,
  church starting room, preload list, lifecycle hooks, and native efun aliases.
- `mfuns.c`: typed compatibility helpers copied from our LDMud LP245 bridge.
  Includes the `implode`, `all_environment`, and `deep_inventory` helpers.
- `mudlib.c`: lifecycle hooks copied from that bridge; diagnostics are directed
  to `jvmud/log/`, keeping bridge diagnostics away from original files.
- `log/`: writable, ignored diagnostic output directory.

The manifest maps `explode`, `write_file`, and `sscanf` to native JVMud efuns.
This is a starting scaffold, not a working or production-ready port. The old
bridge's verification results do not establish compatibility with this source.
Inherited placeholders include `crypt`, `file_size`, `ed`, and `add_worth`;
these require review before gameplay or account persistence can be trusted.

## Porting constraints and next work

Keep original source unchanged and keep JVMud's explicit return and parameter
type requirements. This archive contains untyped declarations, so supplying
bridge functions alone will not make it compile. The manifest explicitly enables:

```properties
transpilation.untyped_methods = true
```

The sibling Java package `io.github.protasm.jvmud.transpiler` supplies missing
method return and parameter types as explicit `mixed` after macro/include
expansion, before strict parsing and semantic analysis. It also applies to
inherited objects and global helpers, in memory, without changing source files.
Existing explicit types and method bodies are preserved. This switch defaults
to false in other profiles. It does not fix other legacy syntax or runtime gaps.

Continue inventorying compilation blockers against this original archive.
Audit helpers and lifecycle behavior against this archive before enabling play.
Original persistence destinations remain read-only; the `write_file` alias does
not redirect game data into this bridge. Persistence policy is still pending.

Use `jvmud/lp245.config` as this mudlib's manifest. Keep test code and fixtures
under `src/test/` and generated test reports under
`target/test-artifacts/lp245-lysator/`. Git does not preserve the original tree's
read-only permissions. Only this bridge is intended to be writable.

## Initial transpilation verification

With the switch enabled, the original-source compile scan reports 254 of 286
LPC files compiling. The other 32 still have separate syntax, type, or missing
function problems; this is not a boot or gameplay success claim.
`UntypedMethodTranspilerTest` also compiles the original chest object and verifies
all 582 archive checksums. Generated scan details are in
`target/test-artifacts/lp245-lysator/all-source-compile.md`.

## Checked field overrides

`lp245.config` explicitly selects `transpilation.json` through
`transpilation.overrides = transpilation.json`. This currently changes only
`obj/torch.c`'s `amount_of_fuel` from `string` to `int` in compiler input.
The source file remains unchanged. The rule must find exactly one field with
the expected original type or compilation fails with a `TRANSPILE` diagnostic.

The override file is relative to the manifest; each rule's `file` is relative
to the mudlib root and selects that compilation unit, including expanded headers.
This setting is independent of untyped-method transpilation. Only field-type
changes are supported so far; no automatic inference or other LP245 repairs are
enabled. Restart to load changed rules.
