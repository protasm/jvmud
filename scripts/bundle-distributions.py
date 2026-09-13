#!/usr/bin/env python3
"""Package pinned JREs with a previously built runtime-free distribution."""
import argparse
import hashlib
import json
import platform
from pathlib import Path
import shutil
import subprocess
import sys
import tarfile
import tempfile
import xml.etree.ElementTree as ET


def digest(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def unpack(archive, destination):
    with tarfile.open(archive) as source:
        source.extractall(destination, filter='data')


def main():
    if sys.version_info < (3, 12):
        sys.exit("Bundled distributions require Python 3.12 or newer.")
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--targets', nargs='+', help='Default: all targets in distribution/runtimes.json')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    pinned = json.loads((root / 'distribution/runtimes.json').read_text())
    targets = args.targets or list(pinned)
    if set(targets) - pinned.keys():
        parser.error('Unknown target')
    version = ET.parse(root / 'pom.xml').getroot().findtext('{http://maven.apache.org/POM/4.0.0}version')
    base = root / 'target' / f'jvmud-{version}-bin.tar.gz'
    # Require the base builder to have completed its tests.
    assert base.with_name(base.name + '.sha256').read_text().split()[0] == digest(base)
    host_os = {'Darwin': 'macos', 'Linux': 'linux'}.get(platform.system(), platform.system())
    host_arch = {'arm64': 'aarch64', 'aarch64': 'aarch64', 'x86_64': 'x64', 'AMD64': 'x64'}.get(platform.machine(), platform.machine())
    host = f'{host_os}-{host_arch}'
    cache = root / 'target/runtime-downloads'
    cache.mkdir(exist_ok=True)
    for target in targets:
        item = pinned[target]
        package = item['package']
        output = root / 'target' / f'jvmud-{version}-{target}.tar.gz'
        checksum = output.with_name(output.name + '.sha256')
        checksum.unlink(missing_ok=True)
        report_path = output.with_name(output.name + '.validation.json')
        report_path.unlink(missing_ok=True)
        download = cache / package['name']
        if not download.exists():
            partial = download.with_name(download.name + '.part')
            subprocess.run(['curl', '--fail', '--location', '--retry', '3', '--output', str(partial), package['link']], check=True)
            assert digest(partial) == package['checksum'], 'JRE checksum mismatch'
            partial.replace(download)
        assert digest(download) == package['checksum'], 'JRE checksum mismatch'
        with tempfile.TemporaryDirectory(prefix='jvmud-bundle-') as temporary:
            staging = Path(temporary)
            unpack(base, staging / 'app')
            app, = (staging / 'app').iterdir()
            unpack(download, staging / 'vendor')
            vendor_root, = (staging / 'vendor').iterdir()
            # Preserve the complete vendor bundle, including macOS signature/resources.
            shutil.copytree(vendor_root, app / 'vendor-runtime', symlinks=True)
            runtime_target = 'vendor-runtime/Contents/Home' if item['os'] == 'mac' else 'vendor-runtime'
            (app / 'runtime').symlink_to(runtime_target, target_is_directory=True)
            assert (app / 'runtime/bin/java').is_file()
            assert (app / 'runtime/bin/java').stat().st_mode & 0o111
            assert (app / 'runtime/legal').is_dir()
            release = (app / 'runtime/release').read_text()
            assert 'JAVA_VERSION="21.' in release
            assert f'OS_ARCH="{item["architecture"] if item["architecture"] != "x64" else "x86_64"}"' in release
            metadata = dict(item, target=target, runtime_path=runtime_target)
            (app / 'metadata/runtime.json').write_text(json.dumps(metadata, indent=2) + '\n')
            with tarfile.open(output, 'w:gz', dereference=False) as archive:
                archive.add(app, arcname=app.name)
            # Re-extract and compare every runtime file and symlink to vendor input.
            unpack(output, staging / 'verify')
            extracted = staging / 'verify' / app.name / 'vendor-runtime'
            for source in vendor_root.rglob('*'):
                copy = extracted / source.relative_to(vendor_root)
                if source.is_symlink():
                    assert copy.is_symlink() and source.readlink() == copy.readlink()
                elif source.is_file():
                    assert digest(source) == digest(copy)
                    assert source.stat().st_mode & 0o777 == copy.stat().st_mode & 0o777
            report = {'target': target, 'runtime': item['version'], 'vendor_sha256_verified': True,
                      'archive_contents_verified': True, 'runtime_smoke_test': 'not run: requires matching host'}
            if target == host:
                subprocess.run([sys.executable, str(root / 'src/test/scripts/distribution-smoke.py'), str(output)], check=True)
                report['runtime_smoke_test'] = 'passed: launchers, formatter, both worlds, login, movement, administration'
            report_path.write_text(json.dumps(report, indent=2) + '\n')
            checksum.write_text(f'{digest(output)}  {output.name}\n')
            print(f'Built {output}: {report["runtime_smoke_test"]}', flush=True)


if __name__ == '__main__':
    main()
