# Documentation maintenance

JVMud uses its existing static HTML site and Maven documentation generators.
No JavaScript application or external hosting service is needed to edit it.

## Reading order

Documentation home → design philosophy → architecture → core packages → User
Manual tasks → LPC/efun/lifecycle references → Java API. Existing general-purpose page URLs and manual chapter anchors are retained.
The former named-profile notes are replaced by generic profile validation.

## Source ownership

| Surface | Edit | Generated or published output |
| --- | --- | --- |
| Site and navigation | `docs/*.html`, `docs/styles.css` | Same files, served statically |
| User Manual | `manual/**/*.adoc`, `manual/docinfo*.html`, `manual/pdf-theme.yml`, `docs/manual.css` | `docs/manual/index.html`, `docs/manual/jvmud-user-manual.pdf` |
| Java API | Java Javadocs and `pom.xml` profile `site-docs` | `docs/api/apidocs/` |
| API landing page | `docs/api/index.html` | Authored package guide, not generated |
| Design principles | `docs/PRINCIPLES.md` | Maintain `docs/principles.html` as its reader-facing companion |
| Vocabulary | `docs/GLOSSARY.md` | Maintain `docs/glossary.html` as its reader-facing companion |
| Boundary design | `docs/ENGINE_MUDLIB_CONTRACT.md` | Manual links to this detailed contract |
| Work tracking | Roadmap, deferred work, provenance, and documentation gaps | Markdown records; proposals are not product guarantees |

Procedures belong in the manual; README and landing pages should point to them.
Keep short quick starts consistent with the manual. Keep focused catalogs in the
efun/lifecycle pages instead of copying full lists into multiple guides.
Principles and glossary HTML remain manually maintained companions; when changing
the controlling Markdown, check both versions for semantic agreement.

## Build and check

From the repository root:

```sh
mvn -Pmanual-docs -DskipTests process-resources
mvn -Psite-docs -DskipTests javadoc:javadoc
python3 src/test/scripts/check-docs.py
python3 -m http.server 8000 --directory docs
```

Open `http://localhost:8000/`; stop the preview with Ctrl+C. HTML and PDF are
built from the same AsciiDoc. Do not hand-edit generated pages. The API build
includes compiler, engine, instance, transport, persistence, and CLI.

The checker validates local links and anchors, duplicate IDs in authored pages,
and unfinished manual placeholders. External URLs, visual layout, actual runtime
commands, and complete API semantics require separate review. Inspect the manual
PDF for clipped code, crowded tables, poor page breaks, and missing characters.

## Editorial rules

Use Small Mercies as the bundled teaching example. Keep the platform architecture
and reference contracts independent of game content, and do not reference other
particular mudlibs in the website or documentation. Generic placeholders remain
appropriate when explaining how to configure independently authored content.

Use JVMud-native terms for the engine and actual legacy names when describing a
profile's source. Explain Player, Session, and Persona before using them in a
workflow. Name the context for every command: terminal, attached admin CLI, or player
client. State prerequisites, expected results, recovery steps, and material
limits. Distinguish implemented behavior, observed verification, and proposals.

The site is published from `docs/` using the repository's established publishing
arrangement and `CNAME`. A local rebuild does not update the public website.
