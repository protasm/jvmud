#!/usr/bin/env python3
"""Regression checks for generated package traversal and new-tab reference boundaries."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[3]
spec = importlib.util.spec_from_file_location('api_docs', ROOT / 'scripts/build-api-docs.py')
api = importlib.util.module_from_spec(spec)
spec.loader.exec_module(api)


class ApiDirectoryTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        api.DOCS = Path(self.temp.name)
        api.API = api.DOCS / 'api'
        api.JAVADOC = api.API / 'apidocs'
        api.JAVADOC.mkdir(parents=True)
        (api.DOCS / 'index.html').write_text('<header class="site-header"><nav><a href="index.html" aria-current="page">JVMud</a></nav></header>')
        (api.JAVADOC / 'index.html').write_text('<html><head></head><body><header role="banner"></header></body></html>')
        for name in ('compiler', 'compiler/parser', 'compiler/parser/ast', 'engine'):
            directory = api.JAVADOC / api.PREFIX / name
            directory.mkdir(parents=True)
            (directory / 'package-summary.html').write_text('''<html><head></head><body><header role="banner"></header>
<section id="package-description"><div class="block">A &amp; B.<p>More detail.</p></div></section>
<div id="class-summary"><div class="col-first"><a href="Example.html">Example</a></div><div class="col-last">A class.</div></div></body></html>''')
            (directory / 'Example.html').write_text('<html><head></head><body><header role="banner"></header><main id="detail">Class reference</main></body></html>')
        self.packages = api.package_data()

    def test_directory_shows_only_immediate_children(self):
        api.render_directory('', self.packages)
        home = api.directory().read_text()
        self.assertIn('packages/compiler/index.html', home)
        self.assertNotIn('packages/compiler/parser/index.html', home)
        api.render_directory('compiler', self.packages)
        branch = api.directory('compiler').read_text()
        self.assertIn('href="parser/index.html"', branch)
        self.assertNotIn('parser/ast/index.html', branch)
        self.assertIn('A &amp; B.', branch)
        self.assertNotIn('More detail.', branch)
        self.assertIn('compiler/Example.html', branch)

    def test_only_reference_links_open_new_tabs(self):
        api.render_directory('compiler', self.packages)
        tree = api.Document(api.directory('compiler').read_text()).root
        for anchor in tree.find(lambda n: n.tag == 'a'):
            href = anchor.attrs['href']
            if 'apidocs/' in href:
                self.assertEqual(anchor.attrs['target'], '_blank')
                self.assertEqual(anchor.attrs['rel'], 'noopener noreferrer')
            else:
                self.assertNotIn('target', anchor.attrs)

    def test_cleanup_preserves_reference_and_is_repeatable(self):
        path = api.JAVADOC / api.PREFIX / 'compiler/Example.html'
        original = path.read_text()
        path.write_text(original.replace('</head>',
            '<!-- JVMUD NAV START --><link href="custom.css"><!-- JVMUD NAV END -->\n</head>'))
        api.remove_site_chrome()
        self.assertEqual(original, path.read_text())
        api.remove_site_chrome()
        self.assertEqual(original, path.read_text())

    def test_missing_namespace_gets_a_traversable_parent(self):
        parent = api.JAVADOC / api.PREFIX / 'compiler/parser/package-summary.html'
        parent.unlink()
        packages = api.package_data()
        self.assertIn('compiler/parser', packages)
        self.assertIsNone(packages['compiler/parser']['source'])


if __name__ == '__main__':
    unittest.main()
