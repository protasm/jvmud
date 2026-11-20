#!/usr/bin/env python3
"""Exercise bundled-runtime upgrade and rollback using two actual release archives."""

import argparse
import hashlib
import http.server
import importlib.util
import io
import json
import os
from pathlib import Path
import re
import shutil
import socket
import subprocess
import sys
import tarfile
import tempfile
import threading
import time
import zipfile

sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location("update_smoke", Path(__file__).with_name("update-smoke.py"))
update = importlib.util.module_from_spec(spec)
spec.loader.exec_module(update)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def extract(archive, destination):
    with tarfile.open(archive) as source:
        source.extractall(destination, filter="data")
    root, = destination.iterdir()
    assert (root / "jre/bin/java").is_file(), "Both inputs must include their actual bundled JRE"
    return root


def identity(root, environment):
    """Inspect the selected bundle and application independently of the caller's system Java."""
    settings = subprocess.run([str(root / "jre/bin/java"), "-XshowSettings:properties", "-version"],
                              env=environment, capture_output=True, text=True, check=True)
    major = int(re.search(r"java.specification.version = (\d+)", settings.stderr)[1])
    launcher = (root / "scripts/jvmud-start").read_text()
    main_class = re.search(r"io\.github\.protasm\.jvmud\.[\w.]+", launcher)[0]
    with zipfile.ZipFile(next((root / "lib").glob("jvmud-*.jar"))) as jar:
        application_major = int.from_bytes(jar.read(main_class.replace(".", "/") + ".class")[6:8], "big")
    return {
        "version": json.loads((root / "metadata/update-index.json").read_text())["version"],
        "target": json.loads((root / "metadata/runtime.json").read_text())["target"],
        "java_major": major,
        "application_major": application_major,
        "main_class": main_class,
        "runtime_release": digest(root / "jre/release"),
        "runtime_link": str((root / "jre").readlink()),
    }


def wait_world(root, state, environment):
    deadline = time.monotonic() + 60
    while time.monotonic() < deadline:
        status = subprocess.run([str(root / "scripts/jvmud-console"), "--socket", str(state / "engine.sock")],
                                input="status\nquit\n", env=environment, capture_output=True, text=True, timeout=15)
        ready = next((line for line in status.stdout.splitlines()
                      if "id=lp245," in line and "state=RUNNING" in line), None)
        if ready:
            return int(re.search(r"playerPort=(\d+)", ready)[1])
        time.sleep(.2)
    raise AssertionError(f"LP245 did not become ready: {status.stdout} {status.stderr}")


def assert_running(root, expected, state, environment):
    records = update.live_records(root)
    assert len(records) == 1, records
    pid = records[0]["pid"]
    command = subprocess.run(["ps", "-p", str(pid), "-o", "command="],
                             capture_output=True, text=True, check=True).stdout
    assert str(root / "jre/bin/java") in command, command
    assert expected["main_class"] in command, command
    assert identity(root, environment) == expected
    return wait_world(root, state, environment)


def broken_restart_archive(source, destination):
    """Keep the real new release and JRE, replacing only its engine launcher."""
    with tarfile.open(source) as original, tarfile.open(destination, "w:gz") as broken:
        for member in original:
            content = original.extractfile(member) if member.isfile() else None
            if member.name.endswith("/scripts/jvmud-start"):
                script = b"#!/bin/sh\nexit 7\n"
                member.size = len(script)
                content = io.BytesIO(script)
            broken.addfile(member, content)


def update_with_live_player(root, port, name, manifest, environment):
    """Leave a real player's changed inventory unsaved until the updater shuts the worker down."""
    with socket.create_connection(("127.0.0.1", port)) as player:
        player.settimeout(.5)
        update.dist.until(player, "What is your name: ")
        for text, marker in [
            (name, "Password: "), ("secret123", "Password: (again) "),
            ("secret123", "Please enter your email address"), ("none", "Are you, male, female or other"),
            ("o", "Welcome, Creature!"), ("south", "You are at an open green place"),
            ("west", "An old humpbacked bridge."), ("get money", "Ok."),
        ]:
            update.dist.command(player, text, marker)
        save = root / f"mudlibs/lp245/players/{name}.o"
        before = save.read_bytes() if save.exists() else None
        result = subprocess.run([str(root / "scripts/jvmud-update"), "--manifest", manifest],
                                cwd=root, env=environment, capture_output=True, text=True, timeout=180)
    assert save.exists() and save.read_bytes() != before, "Shutdown did not save the live player's changes"
    return result, save


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("old_archive", type=Path)
    parser.add_argument("new_archive", type=Path)
    args = parser.parse_args()
    environment = os.environ.copy()
    # An explicit old JVMUD_JAVA_HOME overrides the new bundle and correctly causes restart
    # failure/rollback. This scenario proves the default bundled-runtime transition instead.
    for name in ("JVMUD_JAVA_HOME", "JAVA_HOME", "JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS"):
        environment.pop(name, None)
    with tempfile.TemporaryDirectory(prefix="jvmud baseline update ") as temporary, \
            tempfile.TemporaryDirectory(prefix="jvmud-baseline-", dir="/tmp") as private_state:
        work = Path(temporary)
        root = extract(args.old_archive.resolve(), work / "installed")
        fresh = extract(args.new_archive.resolve(), work / "release")
        old = identity(root, environment)
        new = identity(fresh, environment)
        assert old["version"] != new["version"], "Use two actual release versions"
        assert old["target"] == new["target"], "Archive platforms must match"
        assert new["java_major"] > old["java_major"], "This smoke must cross a Java baseline"
        assert new["application_major"] > old["application_major"], "Application bytecode must cross the baseline too"
        state = Path(private_state)
        custom = root / "mudlibs/lp245/room/baseline-local.c"
        custom.write_text("Local authored content survives the Java baseline transition.\n")
        custom_bytes = custom.read_bytes()
        archive = work / "release.tar.gz"
        broken_restart_archive(args.new_archive.resolve(), archive)
        manifest = work / "latest.json"

        def publish():
            manifest.write_text(json.dumps({"version": new["version"], "artifacts": {
                old["target"]: {"url": archive.name, "sha256": digest(archive)}}}))

        class Handler(http.server.SimpleHTTPRequestHandler):
            def __init__(self, *args, **kwargs):
                super().__init__(*args, directory=str(work), **kwargs)

            def log_message(self, *args):
                pass

        web = http.server.ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        threading.Thread(target=web.serve_forever, daemon=True).start()
        manifest_url = f"http://127.0.0.1:{web.server_port}/latest.json"
        process = None
        try:
            with (work / "original-engine.log").open("w") as log:
                ports = set()
                while len(ports) < 2:
                    ports.add(update.dist.free_port())
                player_port, admin_port = ports
                process = subprocess.Popen([str(root / "scripts/jvmud-start"), "--port", str(player_port),
                                            "--admin-port", str(admin_port), "--state-dir", str(state), "lp245"],
                                           cwd=root, env=environment, stdout=log, stderr=subprocess.STDOUT)
                threading.Thread(target=process.wait, daemon=True).start()
                update.wait_ready(root, process.pid)
                direct_port = assert_running(root, old, state, environment)
                publish()
                rolled_back, first_save = update_with_live_player(root, direct_port, "baselinex", manifest_url, environment)
                assert rolled_back.returncode != 0 and "Previous JVMud files restored" in rolled_back.stderr, \
                    rolled_back.stdout + rolled_back.stderr
                direct_port = assert_running(root, old, state, environment)
                assert custom.read_bytes() == custom_bytes
                backups = list((root.parent / "backup").iterdir())
                assert len(backups) == 1
                assert identity(backups[0], environment) == old, "Backup must retain the complete original JRE and application"
                assert (backups[0] / first_save.relative_to(root)).read_bytes() == first_save.read_bytes()
                assert not (root / ".jvmud/update-in-progress.json").exists()
                print(f"PASS: failed {old['version']} -> {new['version']} restart restores Java {old['java_major']}, "
                      "the old application, live-player save and authored content", flush=True)

                shutil.copy2(args.new_archive.resolve(), archive)
                publish()
                upgraded, second_save = update_with_live_player(root, direct_port, "baseliney", manifest_url, environment)
                assert upgraded.returncode == 0, upgraded.stdout + upgraded.stderr
                assert_running(root, new, state, environment)
                assert custom.read_bytes() == custom_bytes
                second_backup, = set((root.parent / "backup").iterdir()) - set(backups)
                assert identity(second_backup, environment) == old
                assert (second_backup / second_save.relative_to(root)).read_bytes() == second_save.read_bytes()
                assert not (root / ".jvmud/update-in-progress.json").exists()
                print(f"PASS: actual {old['version']} Java {old['java_major']} -> {new['version']} "
                      f"Java {new['java_major']} upgrade runs the new application and LP245 worker, "
                      "preserving live-player saves, authored content and the old-runtime backup", flush=True)
        finally:
            for record in update.live_records(root):
                try:
                    os.kill(record["pid"], 15)
                except ProcessLookupError:
                    pass
            if process is not None:
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()
            time.sleep(1)
            web.shutdown()


if __name__ == "__main__":
    main()
