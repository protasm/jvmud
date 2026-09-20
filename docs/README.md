# Documentation maintenance

The static site is a directory to the project and its documentation. About and
FAQ provide brief orientation; Downloads lists release artifacts and requirements.
Procedures and technical reference material belong in the User Manual or Javadocs,
not in parallel website guides.

## Source ownership

| Surface | Authored source | Published output |
| --- | --- | --- |
| Site navigation, About, FAQ, Downloads | `docs/*.html`, `docs/styles.css` | Same static files |
| User Manual, including efun catalog, lifecycle reference, and glossary | `manual/**/*.adoc`, `manual/docinfo*.html`, `manual/pdf-theme.yml`, `docs/manual.css` | `docs/manual/index.html`, `docs/manual/jvmud-user-manual.pdf` |
| Java API | Java Javadocs and `pom.xml` profile `site-docs` | `docs/api/apidocs/` |
| Internal design and work tracking | Markdown records in `docs/` | Repository documents; proposals are not product guarantees |

The former architecture, principles, package, LPC, efun, lifecycle, and glossary
HTML URLs are retained as pointers to the manual. Do not add technical prose to
these pages. `docs/api/index.html` points directly to generated Javadocs.

Original website graphics are preserved in `design/archive/website-graphics/`,
outside the published tree, with SHA-256 hashes. The site uses text, typography,
and CSS layout without images, icon fonts, or text art.

## Build and check

From the repository root:

```sh
mvn -Pmanual-docs -DskipTests process-resources
mvn -Psite-docs -DskipTests javadoc:javadoc
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
