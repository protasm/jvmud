#!/usr/bin/env python3
"""Check generated/manual and authored site links using only the Python standard library.

Run after both documentation builds. External URLs and visual layout are outside
this check; local paths and fragments are resolved relative to each HTML page.
"""
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import unquote, urlsplit
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / 'docs'

class Page(HTMLParser):
    def __init__(self, path):
        super().__init__(convert_charrefs=True)
        self.ids = set()
        self.duplicates = set()
        self.links = []
        self.feed(path.read_text(encoding='utf-8'))

    def handle_starttag(self, tag, attrs):
        values = dict(attrs)
        identifier = values.get('id')
        if identifier:
            if identifier in self.ids:
                self.duplicates.add(identifier)
            self.ids.add(identifier)
        if tag == 'a' and values.get('name'):
            self.ids.add(values['name'])
        for key in ('href', 'src'):
            if values.get(key):
                self.links.append(values[key])


def main():
    pages = {p.resolve(): Page(p) for p in DOCS.rglob('*.html')}
    errors = []
    checked = 0
    for path, page in pages.items():
        label = path.relative_to(ROOT)
        # Javadoc owns its generated IDs; validate their targets but do not
        # impose authored-site conventions on the generator's markup.
        if 'apidocs' not in path.parts:
            errors.extend(f'{label}: duplicate id {value}' for value in page.duplicates)
        for link in page.links:
            url = urlsplit(link)
            if url.scheme or url.netloc:
                continue
            target = (DOCS / unquote(url.path).lstrip('/') if url.path.startswith('/')
                      else path.parent / unquote(url.path)) if url.path else path
            if target.is_dir():
                target /= 'index.html'
            target = target.resolve()
            checked += 1
            if not target.exists():
                errors.append(f'{label}: missing target {link}')
            elif url.fragment and target in pages and unquote(url.fragment) not in pages[target].ids:
                errors.append(f'{label}: missing fragment {link}')
    for path in (ROOT / 'manual').rglob('*.adoc'):
        if re.search(r'Content to be written|\bTODO\b|\bTBD\b', path.read_text()):
            errors.append(f'{path.relative_to(ROOT)}: unfinished placeholder')
    if errors:
        print('\n'.join(errors))
        return 1
    print(f'Checked {len(pages)} HTML pages and {checked} local links; no broken targets or manual placeholders.')
    return 0

if __name__ == '__main__':
    sys.exit(main())
