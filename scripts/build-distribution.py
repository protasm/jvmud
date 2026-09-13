#!/usr/bin/env python3
"""Build and smoke-test local runtime archives, then generate their checksums."""
import argparse
import hashlib
import io
import json
import tarfile
import zipfile
from pathlib import Path
import subprocess
import sys
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--with-jre", action="store_true", help="Also build all pinned macOS and Linux JRE packages")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    version = ET.parse(root / "pom.xml").getroot().findtext(
        "{http://maven.apache.org/POM/4.0.0}version")
    archives = [root / "target" / f"jvmud-{version}-bin.{ext}"
                for ext in ("tar.gz", "zip")]
    artifacts = archives + [root / "target" / f"jvmud-{version}-sources.jar"]
    # A failed rebuild must not leave apparently current validation checksums.
    for archive in artifacts:
        archive.with_name(archive.name + ".sha256").unlink(missing_ok=True)
    subprocess.run(["mvn", "-B", "-Pdistribution", "verify"], cwd=root, check=True)
    # Ship a baseline for conflict detection in the explicitly updatable adapters.
    for archive in archives:
        index = {"version": version, "adapters": {}}
        member_name = f"jvmud-{version}/metadata/update-index.json"
        rebuilt = archive.with_name(archive.name + ".indexed")
        if archive.name.endswith(".tar.gz"):
            with tarfile.open(archive) as source, tarfile.open(rebuilt, "w:gz") as output:
                for member in source.getmembers():
                    relative = member.name.removeprefix(f"jvmud-{version}/")
                    stream = source.extractfile(member) if member.isfile() else None
                    if member.isfile() and relative.startswith("mudlibs/") and "/jvmud/" in relative:
                        data = stream.read()
                        index["adapters"][relative] = hashlib.sha256(data).hexdigest()
                        stream = io.BytesIO(data)
                    output.addfile(member, stream)
                data = json.dumps(index, indent=2).encode()
                member = tarfile.TarInfo(member_name); member.size = len(data); member.mode = 0o644
                output.addfile(member, io.BytesIO(data))
        else:
            with zipfile.ZipFile(archive) as source, zipfile.ZipFile(rebuilt, "w", zipfile.ZIP_DEFLATED) as output:
                for member in source.infolist():
                    data = source.read(member)
                    relative = member.filename.removeprefix(f"jvmud-{version}/")
                    if not member.is_dir() and relative.startswith("mudlibs/") and "/jvmud/" in relative:
                        index["adapters"][relative] = hashlib.sha256(data).hexdigest()
                    output.writestr(member, data)
                output.writestr(member_name, json.dumps(index, indent=2))
        rebuilt.replace(archive)
    for archive in archives:
        subprocess.run([sys.executable, str(root / "src/test/scripts/distribution-smoke.py"),
                        str(archive)], cwd=root, check=True)
    subprocess.run([sys.executable, str(root / "src/test/scripts/update-smoke.py"),
                    str(archives[0])], cwd=root, check=True)
    for archive in artifacts:
        digest = hashlib.sha256(archive.read_bytes()).hexdigest()
        archive.with_name(archive.name + ".sha256").write_text(
            f"{digest}  {archive.name}\n", encoding="utf-8")
        print(f"Verified: {archive}")

    if args.with_jre:
        subprocess.run([sys.executable, str(root / "scripts/bundle-distributions.py")], cwd=root, check=True)


if __name__ == "__main__":
    main()
