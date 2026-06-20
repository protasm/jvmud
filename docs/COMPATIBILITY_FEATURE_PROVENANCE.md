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
- `LP_245`
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
    note = "Supports array range replacement syntax used by RealmsMUD."
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

- Source: `LDMUD`, `LP_245`, or mudlib-specific profile
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
The namespace accepts shortened calls such as `jvmud::size(...)` for
`jvmud_size(...)`, but it deliberately bypasses mudlib compatibility aliases so
legacy driver names remain a boundary concern.

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
