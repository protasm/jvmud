#!/usr/bin/env python3
"""Build and smoke-test local runtime archives, then generate their checksums."""
import hashlib
from pathlib import Path
import subprocess
import sys
import xml.etree.ElementTree as ET


def main():
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
    for archive in archives:
        subprocess.run([sys.executable, str(root / "src/test/scripts/distribution-smoke.py"),
                        str(archive)], cwd=root, check=True)
    for archive in artifacts:
        digest = hashlib.sha256(archive.read_bytes()).hexdigest()
        archive.with_name(archive.name + ".sha256").write_text(
            f"{digest}  {archive.name}\n", encoding="utf-8")
        print(f"Verified: {archive}")


if __name__ == "__main__":
    main()
