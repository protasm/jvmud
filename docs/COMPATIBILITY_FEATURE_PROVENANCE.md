# Compatibility Feature Provenance

JVMud supports one language family: LPC for LPMud-style worlds. That does not
mean every LPC dialect feature should become part of JVMud's preferred native
style. Imported mudlibs carry driver history with them, and JVMud should be able
to name that history clearly.

This document defines a lightweight way to mark language and runtime
accommodations by provenance, support reason, and JVMud-native guidance.

## Goals

- Preserve imported mudlibs with as little upstream source churn as practical.
- Keep JVMud's engine vocabulary and preferred LPC style distinct from legacy
  driver habits.
- Make compatibility decisions discoverable in code, tests, and documentation.
- Avoid using dismissive labels in source code. Compatibility work should be
  honest and neutral even when a feature is not the preferred JVMud idiom.

## Non-Goals

- Do not turn every parser rule into a taxonomy exercise.
- Do not block useful compatibility work on exhaustive historical research.
- Do not imply that a discouraged JVMud-native idiom is unsupported.
- Do not import another driver's ontology into JVMud's engine API just because a
  mudlib expects that driver.

## Provenance Dimensions

Each compatibility feature should be described along three independent axes.

### Source

The source identifies where the feature expectation comes from.

Suggested values:

- `JVMUD_NATIVE`
- `ANSI_C`
- `LDMUD`
- `FLUFFOS`
- `DGD`
- `MUDLIB_LOCAL`
- `UNKNOWN_LPC_DIALECT`

Use `MUDLIB_LOCAL` for behavior that appears to be a local convention or shim
rather than a driver-level language feature. Use `UNKNOWN_LPC_DIALECT` when the
feature is clearly compatibility-driven but the exact source has not yet been
verified.

### Support Reason

The support reason explains why JVMud implements the feature.

Suggested values:

- `CORE_LANGUAGE`
- `CROSS_ENGINE_COMPATIBILITY`
- `MUDLIB_IMPORT_COMPATIBILITY`
- `BOUNDARY_SHIM`
- `TEST_HARNESS_COMPATIBILITY`

### JVMud-Native Guidance

Native guidance describes whether new JVMud-authored LPC should use the feature.

Suggested values:

- `PREFERRED`
- `ACCEPTABLE`
- `DISCOURAGED`
- `LEGACY_ONLY`

`DISCOURAGED` means the feature is supported but there is a clearer JVMud-native
idiom for new code. `LEGACY_ONLY` means the feature exists only so imported
content can run and should not be used in new mudlib code.

## Suggested Annotation Shape

If JVMud adds a Java annotation for this later, it could look like this:

```java
@LpcCompatibilityFeature(
    id = "ldmud.array-slice-assignment",
    source = CompatibilitySource.LDMUD,
    reason = CompatibilityReason.MUDLIB_IMPORT_COMPATIBILITY,
    guidance = JvmudNativeGuidance.DISCOURAGED,
    note = "Supports array range replacement syntax used by imported LPC source."
)
```

The annotation should be optional and lightweight. It can appear on parser
parselets, AST nodes, IR nodes, runtime helpers, semantic checks, or tests where
the compatibility behavior enters JVMud.

## Naming Tests

Tests are often the most visible compatibility map. Prefer names that state both
behavior and provenance where it matters:

```java
void ldmudCompatibilitySupportsArraySliceAssignment()
void ldmudCompatibilitySupportsStringSubtraction()
void cCompatibilityConcatenatesAdjacentStringLiterals()
```

When provenance is obvious from a surrounding test class or report, a shorter
behavioral name is fine.

## Examples

### Adjacent String Literals

- Source: `ANSI_C`
- Reason: `CORE_LANGUAGE` or `CROSS_ENGINE_COMPATIBILITY`
- Native guidance: `ACCEPTABLE`

Adjacent string literals are a C-family language feature and are common in LPC
mudlibs for long messages. JVMud may support them without treating them as a
legacy wart.

### Direct Efun Name Translation

- Source: `LDMUD` or a local compatibility profile
- Reason: `MUDLIB_IMPORT_COMPATIBILITY`
- Native guidance: `DISCOURAGED`

Imported mudlibs may call driver-facing names such as `efun::sizeof`. JVMud can
translate those names through a mudlib/profile compatibility registry without
exposing legacy names as preferred engine APIs.

### JVMud Qualified Efun Namespace

- Source: `JVMUD_NATIVE`
- Reason: `CORE_LANGUAGE`
- Native guidance: `PREFERRED`

JVMud supports `jvmud::name(...)` as a direct namespace for JVMud-native efuns.
The namespace requires exact JVMud-native efun names such as
`jvmud::jvmud_size(...)`, and it deliberately bypasses mudlib compatibility
aliases so legacy driver names remain a boundary concern.

### Protected Evaluation Syntax

- Source: `LDMUD`
- Reason: `CROSS_ENGINE_COMPATIBILITY`
- Native guidance: `DISCOURAGED`

JVMud can recognize and honor `catch (...)` syntax for imported LPC while still
describing the concept neutrally as protected evaluation in compiler/runtime
internals.

### Array Slice Assignment

- Source: `LDMUD`
- Reason: `MUDLIB_IMPORT_COMPATIBILITY`
- Native guidance: `DISCOURAGED`

Syntax such as:

```c
path[i - 1 .. i] = ({ });
```

is useful for imported mudlibs that rely on driver-level range replacement, but
new JVMud-authored code should prefer clearer helper functions unless slice
assignment becomes an intentional JVMud style.

## Process

When adding a compatibility feature:

1. Implement the smallest useful behavior that moves a real mudlib forward.
2. Add focused tests using source-shaped examples from the mudlib when possible.
3. Record provenance when it is known.
4. Mark native guidance honestly.
5. If provenance is uncertain, use `UNKNOWN_LPC_DIALECT` or a short comment and
   leave a note to verify later.

This keeps compatibility work practical while preserving JVMud's own design
voice.

## Opt-in untyped method transpilation

- Motivation: original Lysator LP 2.4.5 declarations such as `reset(arg)` and
  `close(str)` in `mudlibs/lp245/obj/chest.c`, preserved without source changes.
- Bridge setting: `transpilation.untyped_methods = true` (default false).
- Implementation: `io.github.protasm.jvmud.language.transpiler` subpackage adds
  explicit `mixed` signature types to expanded tokens before strict compilation.
  Existing type declarations remain authoritative. Other legacy syntax is outside
  this transformation's scope.
- Verification: `UntypedMethodTranspilerTest` covers profile isolation, explicit
  type errors, includes/macros, inheritance, global helpers, source positions,
  prototypes, and preservation of existing tokens. `CompilerSmokeTest` verifies
  that native compilation rejects untyped methods and parameters.

## Checked field-type overrides

- Motivation: original LP245 `obj/torch.c` declares `amount_of_fuel` as `string`
  while initializing it to 2000 and using multiplication/division for burning
  time and value.
- Configuration: `transpilation.overrides` selects a bridge-owned JSON file with
  `field_type_overrides`, independently of the untyped-method flag.
- Implementation: `FieldTypeTranspiler` checks a named field's original type,
  scopes edits by compilation unit, preserves neighboring declarations and source
  positions, and reports mismatches before parsing/code generation.
- Verification: `FieldTypeTranspilerTest` covers negative expectations, grouped
  declarations, arrays, includes/inheritance, global helpers, hosted startup,
  explicit selection, and numeric execution of the unchanged original torch.

## Opt-in implicit self calls

- Motivation: LP245's living base calls `short()` without declaring it, expecting
  a concrete descendant to supply it.
- Bridge setting: `transpilation.implicit_self_calls = true` (default false),
  independent of untyped-method normalization.
- Implementation: `ImplicitSelfCallTranspiler` runs after ordinary name lookup
  during semantic resolution and produces a required dynamic call on the current
  object. The result is mixed, the original call line and arguments are retained,
  and missing implementations fail at runtime. Known names and qualified calls
  retain their existing checks. It does not synthesize abstract declarations.
- Tradeoff: unknown-name typos and missing services may also compile; this flag
  shifts their detection to invocation and does not prove gameplay compatibility.
- Verification: `ImplicitSelfCallTranspilerTest` covers inherited dispatch,
  missing implementations, single evaluation of arguments, strict mode,
  known-function/alias checks and hosted startup. `Lp245BridgeTest` executes
  the unchanged living base's `show_stats()` against a concrete child.

## Checked local-type overrides

- Motivation: LP245's `obj/player.c` stores `users()` in locals declared `object`
  and indexes them as arrays in `list_peoples()` and `who()`.
- Configuration: `local_type_overrides` entries in the selected
  `transpilation.overrides` JSON file identify a file, method, local, expected
  type and replacement type. Either local or field rules may appear alone.
- Implementation: `LocalTypeTranspiler` validates parsed local declarations before
  semantic analysis, excluding parameters and fields and rejecting ambiguous
  names. Replacing the declaration preserves local identity and initializer order.
- Verification: `LocalTypeTranspilerTest` covers scope, inherited execution,
  included declarations, malformed rules, ambiguity and hosted boot propagation.
  `Lp245BridgeTest` exercises the unchanged player's user lists and login prompt.

## Contextual foreach delimiter and remaining LP245 declarations

- Motivation: original `obj/trace.c` and `obj/trace2.c` define a method named
  `in`. Globally reserving that word prevented the legacy declaration translator
  and strict parser from recognizing the method.
- Implementation: the scanner classifies `in` as an identifier; the parser
  recognizes it as a delimiter only after the foreach variable declaration.
  Explicit method and parameter types remain required by the compiler.
- Verification: `CompilerSmokeTest.inIsContextualInForeachAndCanNameMethodsAndLocals`
  executes methods and locals named `in`, alongside both foreach delimiters.
- Profile adaptations: checked field/local rules cover tracer storage, marker
  captures, roommaker lists, Go grids and color captures, random-distribution
  counters, death-room player/tick pairs, and room flags. The shop's two-argument
  `add_worth` is handled in LPC, preserving the existing accounting no-op.
- Archive verification: `Lp245BridgeTest` compiles 283 original objects and loads
  281 in disposable storage, with five explicit historical exceptions documented
  in `mudlibs/lp245/jvmud/README.md`. Behavior tests exercise tracer value storage
  and inventory selection, death-room player removal, shop payouts and inventory
  transfer, and Go-board initialization, patching, scoring and filling. The
  upstream hash test continues to guard original sources.


## LP245 armour weight and value

- Motivation: `room/forest1.c` configures its leather jacket with weight 2 and
  value 50, but `obj/armour.c` declares both fields as strings. Pickup fails
  when the player reads the resulting string weight into an integer local;
  sale has the same mismatch for value.
- Adaptation: checked `field_type_overrides` change only those two declarations
  from `string` to `int`, preserving the original armour source.
- Verification: `Lp245BridgeTest.originalForestJacketCanBePickedUpWornDroppedAndSold`
  reproduces the String-to-Number failure without the overrides, then verifies
  actual command dispatch for pickup, wear, drop, repeat pickup, and a 50-coin
  sale into shop inventory with the overrides enabled.


## Location departure lifecycle delivery

- Motivation: `room/death/death_room.c` recursively drains its completed-player
  queue on heartbeat 70, expecting `move_object` to invoke `exit(player)`.
  Without departure delivery, the queue never shrank and raised StackOverflowError.
- Implementation: deliver the existing `ENTITY_DEPARTED_FROM_PLACE` event on the
  old location after moving a connected or command-enabled actor, before arrival
  callbacks. Bind the departing actor; accept one-argument or zero-argument
  methods. Same-location moves, initial placement, ordinary items, and absent
  mappings or methods do not invoke cleanup. Skip stale arrival if cleanup
  redirects or destroys the actor.
- Profile mapping: LP245 selects `exit`; its original source remains unchanged.
- Verification: `Lp245BridgeTest.originalDeathSequenceReturnsGhostsToChurchAndClearsItsQueue`
  reproduced the scheduled-tick overflow before the fix and now completes the
  full 70-tick sequence for two ghosts. `CompilerSmokeTest` checks callback
  ordering, actor identity, excluded moves, missing methods, zero-argument hooks,
  and movement redirected during cleanup.

## Optional dynamic value typing

- Configuration: `compiler.dynamic_types = true`, default false, selected per
  mudlib and preserved through hosted bridge declaration merging.
- Motivation: LP245's documentary declarations repeatedly disagree with actual
  values. Its 51 field and 24 local overrides are replaced by this policy.
- Implementation: declaration symbols retain written type names but use stable
  mixed storage throughout parsing, analysis, inheritance, and code generation.
  This is a compiler policy, not a token substitution. Typed literal declarations
  follow the same policy. No new casts or syntax are introduced.
- Preserved contracts: explicit conversions, visibility/storage modifiers,
  argument structure, native function signatures, and runtime operation errors.
  Duplicate methods of the same arity remain errors; absent declarations still
  need the separate untyped-method opt-in. Mixed defaults/empty returns use zero.
- Verification: `DynamicTypesTest`, `MudlibBootTranspilationTest`, and the original
  LP245 archive/gameplay tests. The generic checked override facilities remain
  available to strict mudlibs; LP245 retains only its method-varargs rule.
