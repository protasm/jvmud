# Documentation coverage and follow-up

The product documentation pass uses Small Mercies for the runnable introduction, replaces the manual's empty sections, preserves
its philosophy/LPC/compiler material, adds task workflows and complete manifest
key coverage, introduces a top-down documentation hub and core package guide,
and expands Java API generation to all six core package families.

## Remaining coverage gaps

| Priority | Gap | Evidence needed to close it |
| --- | --- | --- |
| High | Profile-specific backup and recovery | A tested account/object/database backup-and-restore run, including upgrade and rollback behavior; current guidance does not promise universal snapshots. |
| High | External-service setup contract | Document the JVMud-side JDBC/environment contract and verification boundaries. Game-specific provisioning and schema instructions belong with the content, outside the platform documentation. |
| High | Multi-user and public hosting operations | Implementation decisions and verification for event serialization, output isolation, public transport/security policy, and failure recovery. Align with `DEFERRED_WORK.md`; do not present planned hardening as available. |
| Medium | Advanced authoring tutorial | Small Mercies now supplies runnable Place, Entity, command, communication, and combat examples. Durable persistence and mounted-world transfer need separate generic teaching exercises and verification. |
| Medium | Efun and lifecycle reference completeness | Audit every documented signature, capability, event argument, dispatch order, and error case against source; generate catalogs where practical. This pass improves access but does not claim an exhaustive per-function audit. |
| Medium | Dialect compatibility matrix | Tests separating accepted syntax, compile/load success, and runtime semantics for every optional language feature. Preserve provenance rather than claiming universal LPC parity. |
| Medium | Admin argument types | The shell currently passes arguments as strings. The manual documents this limit and uses a no-argument wrapper; richer typed invocation needs an explicit implementation decision. |
| Medium | Reload contract | Tested state/reference migration examples for object replacement and clones, with limits documented explicitly. |
| Low | Automatic companion-page synchronization | Generate Principles/Glossary HTML from their controlling Markdown to remove manual synchronization. |
| Low | Documentation CI | Run manual/API generation and `src/test/scripts/check-docs.py` automatically; retain render review for PDF and responsive website changes. |

## Recommended next sequence

1. Publish the validated repository documentation through the established site workflow.
2. Verify the Small Mercies quick start on a clean machine and a profile-specific recovery exercise.
3. Extend the authoring tutorial to persistence and transfer, then the generic external-service contract.
4. Audit/generated-reference coverage and add documentation CI.

Keep this file about missing guidance. Implementation debt belongs in
`DEFERRED_WORK.md`; future product direction belongs in `ROADMAP.md` and
`FUTURE_DESIGN_IDEAS.md`.
