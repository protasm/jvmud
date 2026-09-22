# Documentation maintenance

The static site is a directory to the project and its documentation. About and
FAQ provide brief orientation; Downloads lists release artifacts and requirements.
The glossary is also available as a standalone website reference generated from the manual source.
Other procedures and technical reference material belong in the User Manual or Javadocs,
not in parallel website guides.

## Source ownership

| Surface | Authored source | Published output |
| --- | --- | --- |
| Site navigation, About, FAQ, Downloads | `docs/*.html`, `docs/styles.css` | Same static files |
| User Manual, including efun catalog, lifecycle reference, and glossary | `manual/**/*.adoc`, `manual/docinfo*.html`, `manual/pdf-theme.yml`, `docs/manual.css` | `docs/manual/index.html`, `docs/manual/jvmud-user-manual.pdf` |
| Standalone glossary | `manual/glossary.adoc`, `manual/appendixes/glossary.adoc`, `manual/glossary-docinfo*.html` | `docs/glossary.html` |
| Java API | Java Javadocs, `pom.xml` profile `site-docs`, and `scripts/build-api-docs.py` | `docs/api/index.html`, `docs/api/packages/`, `docs/api/apidocs/` |
| Internal design and work tracking | Markdown records in `docs/` | Repository documents; proposals are not product guarantees |

The former architecture, principles, package, LPC, efun, and lifecycle
HTML URLs are retained as pointers to the manual. Do not add technical prose to
these pointer pages. The glossary is generated, not hand-edited. `docs/api/index.html` starts a generated package directory. Branch pages show immediate
subpackages and classes. Links into the standard Javadoc reference open a new tab,
leaving the styled directory available in the original tab.
Descriptions come from the generated Java documentation, not separate website prose.

Original website graphics are preserved in `design/archive/website-graphics/`,
outside the published tree, with SHA-256 hashes. The site uses text, typography,
and CSS layout without images, icon fonts, or text art.

## Build and check

From the repository root:

```sh
mvn -Pmanual-docs -DskipTests process-resources
python3 scripts/build-api-docs.py
python3 src/test/scripts/check-docs.py
python3 -m http.server 8000 --directory docs
```

HTML and PDF are generated from the same AsciiDoc. Do not edit generated outputs
by hand. The API build includes compiler, engine, instance, transport, persistence,
and CLI. Rebuild the manual after changing its content or navigation fragments;
rebuild Javadocs when their source changes.

The checker validates local links, fragments, duplicate IDs in authored pages,
and unfinished manual placeholders. Check desktop and narrow-screen layouts,
keyboard navigation, and rendered manual pages separately.

The site is published from `docs/` using the repository's existing Pages setup
and `CNAME`. A local rebuild does not update the public website.

The API build script runs Maven, generates the package directory, and removes any
previously added site chrome from the standard Javadoc pages. Use this
script instead of the bare Maven goal when preparing API output for publication.
`--directory-only` regenerates the directory from existing Javadocs without
rebuilding Java documentation. Both operations are repeatable.
Styling lives in `docs/api.css`; the main navigation is read from `docs/index.html`.
