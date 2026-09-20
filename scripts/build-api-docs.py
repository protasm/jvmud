#!/usr/bin/env python3
"""Build Javadocs, then generate the site's package directory and shared navigation.

Descriptions come from Javadoc's rendering of package-info.java and class comments.
Use --decorate-only to refresh navigation around an already generated API without
including unrelated Java work in progress. All output is static and works without JS.
"""
from html import escape
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urlsplit
import argparse
import os
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / 'docs'
API = DOCS / 'api'
JAVADOC = API / 'apidocs'
PREFIX = 'io/github/protasm/jvmud'
MARKER = re.compile(r'<!-- JVMUD NAV START -->.*?<!-- JVMUD NAV END -->\n?', re.S)


class Element:
    """Minimal HTML tree for reading Javadoc summary elements without dependencies."""

    def __init__(self, tag='', attrs=()):
        self.tag = tag
        self.attrs = dict(attrs)
        self.children = []

    def find(self, predicate):
        for child in self.children:
            if isinstance(child, Element):
                if predicate(child):
                    yield child
                yield from child.find(predicate)

    def has_class(self, name):
        return name in self.attrs.get('class', '').split()

    def text(self):
        return ''.join(c.text() if isinstance(c, Element) else c for c in self.children)


class Document(HTMLParser):
    """Parse generated HTML, including HTML void tags, into a small queryable tree."""

    VOID = set('area base br col embed hr img input link meta param source track wbr'.split())

    def __init__(self, text):
        super().__init__(convert_charrefs=True)
        self.root = Element()
        self.stack = [self.root]
        self.feed(text)

    def handle_starttag(self, tag, attrs):
        node = Element(tag, attrs)
        self.stack[-1].children.append(node)
        if tag not in self.VOID:
            self.stack.append(node)

    def handle_endtag(self, tag):
        for i in range(len(self.stack) - 1, 0, -1):
            if self.stack[i].tag == tag:
                del self.stack[i:]
                break

    def handle_data(self, text):
        self.stack[-1].children.append(text)


def relative(target, page):
    return Path(os.path.relpath(target, page.parent)).as_posix()


def link(target, page, label):
    return f'<a href="{escape(relative(target, page), quote=True)}">{escape(label)}</a>'


def clean(text):
    return ' '.join(text.split())


def package_data():
    """Read only generated package summaries; fail rather than publish an empty API."""
    packages = {}
    for path in sorted((JAVADOC / PREFIX).rglob('package-summary.html')):
        name = path.parent.relative_to(JAVADOC / PREFIX).as_posix()
        if name == '.':
            continue
        tree = Document(path.read_text()).root
        description = next(tree.find(lambda n: n.attrs.get('id') == 'package-description'), None)
        block = next(description.find(lambda n: n.has_class('block')), None) if description else None
        summary = ''
        if block:
            # Javadoc puts the lead paragraph before any block-level <p> elements.
            for child in block.children:
                if isinstance(child, Element) and child.tag in ('p', 'ul', 'ol', 'pre'):
                    break
                summary += child.text() if isinstance(child, Element) else child
        classes = []
        table = next(tree.find(lambda n: n.attrs.get('id') == 'class-summary'), None)
        if table:
            pending = None
            for cell in table.find(lambda n: n.has_class('col-first') or n.has_class('col-last')):
                if cell.has_class('col-first'):
                    anchor = next(cell.find(lambda n: n.tag == 'a'), None)
                    pending = (anchor.attrs['href'], clean(anchor.text())) if anchor else None
                elif pending:
                    classes.append((*pending, clean(cell.text())))
                    pending = None
        packages[name] = {'source': path, 'description': clean(summary), 'classes': classes}
    if not packages:
        raise RuntimeError('No JVMud package summaries found; generate Javadocs first.')
    # Let Javadoc choose each lead sentence (including its inline-tag handling).
    overview = Document((JAVADOC / 'index.html').read_text()).root
    pending = None
    for cell in overview.find(lambda n: n.has_class('col-first') or n.has_class('col-last')):
        if cell.has_class('col-first'):
            anchor = next(cell.find(lambda n: n.tag == 'a'), None)
            pending = clean(anchor.text()).removeprefix('io.github.protasm.jvmud.').replace('.', '/') if anchor else None
        elif pending in packages:
            packages[pending]['description'] = clean(cell.text())
            pending = None
    # Namespace-only parents still provide traversal if Java has no types at that level.
    for name in list(packages):
        parts = name.split('/')
        for i in range(1, len(parts)):
            packages.setdefault('/'.join(parts[:i]), {'source': None, 'description': '', 'classes': []})
    return packages


def directory(name=''):
    return API / 'packages' / name / 'index.html' if name else API / 'index.html'


def site_header(page):
    """Reuse the homepage's navigation so the API cannot acquire a separate menu."""
    home = (DOCS / 'index.html').read_text()
    header = re.search(r'<header class="site-header">.*?</header>', home, re.S).group()
    header = re.sub(r' aria-current="[^"]*"', '', header)

    def rebase(match):
        href = match[1]
        if urlsplit(href).scheme or href.startswith('#'):
            return match[0]
        return f'href="{relative(DOCS / href, page)}"'

    return re.sub(r'href="([^"]+)"', rebase, header)


def breadcrumbs(page, name=''):
    items = [link(directory(), page, 'Java API')]
    parts = name.split('/') if name else []
    for i, part in enumerate(parts):
        items.append(link(directory('/'.join(parts[:i + 1])), page, part))
    return '<nav class="api-breadcrumbs" aria-label="Package breadcrumbs"><ol>' + ''.join(
        '<li>' + item + '</li>' for item in items) + '</ol></nav>'


def render_directory(name, packages):
    page = directory(name)
    title = name.replace('/', '.') if name else 'Java API'
    content = breadcrumbs(page, name) if name else ''
    content += f'<header class="page-heading"><p class="kicker">Java API</p><h1>{escape(title)}</h1>'
    if name and packages[name]['description']:
        content += '<p class="lead">' + escape(packages[name]['description']) + '</p>'
    elif not name:
        content += '<p class="lead">Packages and their responsibilities.</p>'
    content += '</header>'
    children = sorted(k for k in packages if k.rpartition('/')[0] == name)
    if children:
        content += '<section><h2>' + ('Subpackages' if name else 'Packages') + '</h2><div class="resource-grid">'
        for child in children:
            content += '<article class="resource"><h3>' + link(directory(child), page, child.split('/')[-1]) + '</h3>'
            description = packages[child]['description']
            if description:
                content += '<p>' + escape(description) + '</p>'
            content += '</article>'
        content += '</div></section>'
    if name and packages[name]['classes']:
        content += '<section><h2>Classes and interfaces</h2><dl class="api-types">'
        for href, label, description in packages[name]['classes']:
            content += '<dt>' + link(packages[name]['source'].parent / href, page, label) + '</dt>'
            content += '<dd>' + escape(description) + '</dd>'
        content += '</dl></section>'
    content += '<section class="index-note"><h2>Reference</h2><p>'
    if name and packages[name]['source']:
        content += link(packages[name]['source'], page, 'Full package documentation') + ' · '
    content += link(JAVADOC / 'search.html', page, 'Search API') + ' · '
    content += link(JAVADOC / 'index-all.html', page, 'Complete index') + '</p></section>'
    page.parent.mkdir(parents=True, exist_ok=True)
    page.write_text(f'''<!doctype html>
<!-- Generated by scripts/build-api-docs.py. Edit Java documentation, not this file. -->
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{escape(title)} — JVMud</title>
<link rel="stylesheet" href="{relative(DOCS / 'styles.css', page)}?v=api-1">
<link rel="stylesheet" href="{relative(DOCS / 'api.css', page)}?v=api-1">
</head><body class="site"><a class="skip-link" href="#main">Skip to content</a>
{site_header(page)}<main id="main">{content}</main>
<footer class="site-footer">JVMud Java API · Generated from Java source</footer>
</body></html>
''')


def decorate(packages):
    """Add static chrome to every reference page, preserving Javadoc content and URLs."""
    for page in sorted(JAVADOC.rglob('*.html')):
        source = MARKER.sub('', page.read_text())
        source = re.sub(r'(<(?:header role="banner"|body\b[^>]*)>)\n+', r'\1\n', source)
        styles = ''.join(f'<link rel="stylesheet" href="{relative(DOCS / name, page)}?v=api-1">\n'
                         for name in ('styles.css', 'api.css'))
        source = source.replace('</head>', '<!-- JVMUD NAV START -->\n' + styles + '<!-- JVMUD NAV END -->\n</head>', 1)
        parent = page.parent
        while parent != JAVADOC and not (parent / 'package-summary.html').exists():
            parent = parent.parent
        name = parent.relative_to(JAVADOC / PREFIX).as_posix() if parent.is_relative_to(JAVADOC / PREFIX) else ''
        if name not in packages:
            name = ''
        # Inside Javadoc's existing sticky header: its JS measures the whole header,
        # keeping anchor scrolling and sidebar offsets correct at every viewport size.
        chrome = '<!-- JVMUD NAV START -->\n<div class="api-site-chrome">' + site_header(page).replace('<header ', '<div ').replace('</header>', '</div>') + breadcrumbs(page, name) + '</div>\n<!-- JVMUD NAV END -->\n'
        if '<header role="banner">' in source:
            source = source.replace('<header role="banner">', '<header role="banner">' + chrome, 1)
        else:
            source = re.sub(r'(<body\b[^>]*>)', lambda m: m[0] + chrome, source, count=1)
        page.write_text(source)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--decorate-only', action='store_true', help='Reuse existing Javadocs; regenerate only navigation and directory pages.')
    args = parser.parse_args()
    if not args.decorate_only:
        subprocess.run(['mvn', '-Psite-docs', '-DskipTests', 'javadoc:javadoc'], cwd=ROOT, check=True)
    packages = package_data()
    # Remove only generator-owned directory pages for packages that disappeared.
    for old in (API / 'packages').rglob('index.html'):
        if old.parent.relative_to(API / 'packages').as_posix() not in packages:
            old.unlink()
    for name in ['', *sorted(packages)]:
        render_directory(name, packages)
    decorate(packages)
    print(f'Generated package directory for {len(packages)} packages and added site navigation to Javadocs.')


if __name__ == '__main__':
    main()
