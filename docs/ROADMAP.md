# JVMud roadmap

JVMud aims to provide a readable, inspectable LPC/LPMud stack: compile authored
objects, host them in a native world model, and support interactive, persistent,
player-present text worlds. This roadmap describes direction, not a release
promise or a current test report.

## Design commitments

[Principles](PRINCIPLES.md) control engine design; the
[engine–mudlib contract](ENGINE_MUDLIB_CONTRACT.md) defines the boundary.
The engine owns Places, Links, Entities, identity, time, and presence. Mudlibs
own authored content and compatibility policy. Keep generated-bytecode helpers
separate from the engine, and keep game-specific assumptions out of shared Java and launchers. Small Mercies is
the bundled documentation example; general platform contracts remain independent
of its game rules.

## Existing foundation

The repository provides a staged LPC compiler, object execution APIs, the
`WorldRuntime` containment model, scheduling, manifest-driven hosted instances,
Telnet transport, persistence adapters, and an attached live admin CLI. See the
[core package guide](packages.html) and [User Manual](manual/index.html).

Current verification must come from a fresh test run and the relevant runtime
route. Compilation, informational preload reports, and player interaction are
separate evidence. Do not treat a historical green build as current acceptance.

## Development waypoints

1. **Compiler and diagnostics.** Expand coverage around preprocessing, parsing,
   semantic analysis, typed IR, bytecode generation, and verifier failures.
   Preserve source positions and explicit LPC method/parameter types.
2. **Compatibility evidence.** Classify failures by compilation stage or runtime
   behavior. Keep syntax gates and host capability grants explicit. Use focused
   adapters rather than broad source rewrites or legacy-driver emulation.
3. **Object lifecycle.** Clarify load, clone, reload, destruction, reference, and
   state-preservation contracts. Verify both direct invocation and live callers.
4. **Engine world model.** Align LPC environment/inventory bookkeeping with
   `WorldRuntime`, valid containment, navigable Links, and world-present Entities.
5. **Command and session behavior.** Keep Persona context, command registration,
   captured input, text output, disconnect cleanup, and account resolution coherent.
   Transport controls use an explicit escape prefix; admin work remains separate.
6. **Manifest-driven boot.** Validate source roots, initial Place, Persona paths,
   preloads, lifecycle mappings, language features, and capabilities before play.
   Fail with actionable diagnostics when declared requirements cannot be met.
7. **Hosted-world routing.** Use explicit mounted-world declarations; verify
   transfer, identity, session, and lifecycle boundaries with independent content.
8. **Time and persistence.** Strengthen deterministic scheduling, save/restore,
   storage boundaries, failure recovery, and documented backup/rollback contracts.
9. **Multi-user operation.** Serialize world mutation appropriately, verify output
   isolation and multi-session behavior, then establish public-hosting policy,
   observability, packaging, and long-running reliability.
10. **Documentation and delivery.** Keep the manual task-oriented, generated API
    coverage complete, and source/output builds reproducible. Add documentation CI
    and review rendered outputs before publishing.

## Acceptance standard

Use `mvn test` for the baseline and focused tests for the changed responsibility.
For runtime changes, restart a configured listener and verify an appropriate
entry, command, movement, and disconnect route. State external prerequisites and
incomplete checks. Keep profile-specific test procedures with their content.

Use [deferred work](DEFERRED_WORK.md) for concrete implementation debt and
[documentation gaps](DOCUMENTATION_GAPS.md) for missing guidance. Prefer readable
names and Javadocs that explain non-obvious contracts. A roadmap item becomes a
product claim only after source, verification, and documentation agree.
