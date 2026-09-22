#!/usr/bin/env python3
"""Test a locally built archive outside the checkout, without Maven on PATH."""
import os
import re
from pathlib import Path, PurePosixPath
import shutil
import socket
import subprocess
import sys
import tarfile
import tempfile
import time
import zipfile


def free_port():
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]


def until(sock, marker):
    text = ""
    deadline = time.monotonic() + 15
    while time.monotonic() < deadline:
        try:
            data = sock.recv(8192)
        except socket.timeout:
            continue
        if not data:
            break
        text += data.decode("utf-8", errors="replace")
        if marker in text:
            return text
    raise AssertionError(f"Missing {marker!r}: {text}")


def command(sock, value, marker):
    sock.sendall((value + "\n").encode())
    text = until(sock, marker)
    # Finish reading this command's response before sending the next command.
    while True:
        try:
            data = sock.recv(8192)
        except socket.timeout:
            return text
        if not data:
            return text
        text += data.decode("utf-8", errors="replace")


def check_world(root, cwd, env, world):
    port, admin_port = free_port(), free_port()
    while admin_port == port:
        admin_port = free_port()
    token = cwd / f"{world}.token"
    state_context = tempfile.TemporaryDirectory(prefix="jvmud-admin-", dir="/tmp")
    state = Path(state_context.name)
    log = cwd / f"{world}.log"
    manifest = root / f"mudlibs/{world}/jvmud/{world}.config"
    # Exercise both explicit manifests from another directory and the short name.
    launch_argument = world if world == "lp245" else str(manifest)
    launch_cwd = root if world == "lp245" else cwd
    with log.open("w") as output:
        server = subprocess.Popen([
            str(root / "scripts/jvmud-start"), "--bind", "127.0.0.1",
            "--port", str(port), "--admin-port", str(admin_port),
            "--state-dir", str(state), launch_argument],
            cwd=launch_cwd, env=env, stdout=output, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 45
            while "state=RUNNING" not in log.read_text():
                if server.poll() is not None or time.monotonic() > deadline:
                    raise AssertionError(log.read_text())
                time.sleep(0.1)
            bootstrap = subprocess.run([
                str(root / "scripts/jvmud-console"), "--socket", str(state / "engine.sock")],
                input=f"admin-create smoke\ngrant smoke mudlib:{world}\nstatus\nquit\n", text=True,
                capture_output=True, timeout=15, cwd=cwd, env=env, check=True)
            secret = re.search(r"shown once\): ([0-9a-f]{64})", bootstrap.stdout)
            assert secret, "Console did not issue the bootstrap token"
            token.write_text(secret.group(1)); token.chmod(0o600)
            mudlib_admin = re.search(r"id=" + world + r",.*?adminPort=(\d+)", bootstrap.stdout)
            assert mudlib_admin, bootstrap.stdout.replace(secret.group(1), "<redacted>")
            mudlib_player = re.search(r"id=" + world + r",.*?playerPort=(\d+)", bootstrap.stdout)
            assert mudlib_player, "Missing mudlib player port"
            fingerprint = (state / "admin-tls.sha256").read_text().strip()
            with socket.create_connection(("127.0.0.1", int(mudlib_player.group(1))), timeout=5) as sock:
                sock.settimeout(0.5)
                if world == "smallmercies":
                    until(sock, "Name (2-16 letters): ")
                    command(sock, "Tester", "Gender (male/female): ")
                    command(sock, "female", "Class (warrior/mage): ")
                    command(sock, "warrior", "> ")
                    command(sock, "look", "Village Square")
                    command(sock, "north", "Municipal Herb Garden")
                    command(sock, "score", "STR 12")
                    command(sock, "quit", "guest book")
                else:
                    until(sock, "What is your name: ")
                    command(sock, "disttester", "Password: ")
                    command(sock, "localtest123", "Password: (again) ")
                    command(sock, "localtest123", "Please enter your email address")
                    command(sock, "none", "Are you, male, female or other")
                    command(sock, "o", "Welcome, Creature!")
                    command(sock, "look", "You are in the local village church.")
                    command(sock, "south", "You are at an open green place")
                    command(sock, "east", "A track going into the village.")
                    command(sock, "east", "A long road going east through the village.")
                    command(sock, "east", "There are stairs going down.")
                    command(sock, "north", "You are in a shop.")
                    command(sock, "west", "You are in a small and dusty storage room.")
                    command(sock, "get quicktyper", "Quicktyper....")
                    command(sock, "alias qlook look", "Ok.")
                    command(sock, "qlook", "You are in a small and dusty storage room.")
                    command(sock, "east", "You are in a shop.")
                    command(sock, "south", "There are stairs going down.")
                    command(sock, "west", "A long road going east through the village.")
                    command(sock, "north", "A small yard surrounded by houses.")
                    command(sock, "east", "You are in the local pub.")
                    command(sock, "say play b1", "You feel that you have gained some experience.")
                    command(sock, "look at board", "7|.......")
            result = subprocess.run([
                str(root / "scripts/jvmud-console"), "--port", mudlib_admin.group(1),
                "--user", "smoke", "--fingerprint", fingerprint, "--token-file", str(token)], input="objects\nquit\n", text=True,
                capture_output=True, timeout=15, cwd=cwd, env=env, check=True)
            expected = "room/square" if world == "smallmercies" else "room/church"
            assert expected in result.stdout, result.stdout + result.stderr
            print(f"PASS: {world} login, look, movement, attached administration")
            for line in log.read_text().splitlines():
                if "preload" in line.lower() and ("failed" in line.lower() or "summary" in line.lower()):
                    print(line)
        except Exception:
            print(log.read_text(), file=sys.stderr)
            raise
        finally:
            server.terminate()
            try:
                server.wait(timeout=10)
            except subprocess.TimeoutExpired:
                server.kill()
                server.wait()
    assert not (state / "engine.sock").exists(), "Normal shutdown left a recovery socket"
    assert (state / "administrators.json").exists(), "Administrator identities were not persisted"
    token.unlink(missing_ok=True)
    state_context.cleanup()


def main():
    archive = Path(sys.argv[1]).resolve()
    with tempfile.TemporaryDirectory(prefix="jvmud distribution ") as temporary:
        work = Path(temporary)
        if archive.suffix == ".zip":
            with zipfile.ZipFile(archive) as source:
                for entry in source.infolist():
                    member = PurePosixPath(entry.filename)
                    assert not member.is_absolute() and ".." not in member.parts
                source.extractall(work)
                for entry in source.infolist():
                    if not entry.is_dir():
                        (work / entry.filename).chmod((entry.external_attr >> 16) & 0o777)
        else:
            with tarfile.open(archive) as source:
                for entry in source.getmembers():
                    member = PurePosixPath(entry.name)
                    assert not member.is_absolute() and ".." not in member.parts
                    assert entry.isfile() or entry.isdir() or entry.issym(), "Unexpected archive entry"
                    if entry.issym():
                        target = (work / entry.name).parent / entry.linkname
                        assert target.resolve().is_relative_to(work.resolve()), "Unsafe symlink"
                source.extractall(work)
        root, = work.glob("jvmud-*")
        assert not list(root.rglob("*.java")), "Runtime archive contains Java source"
        assert not list(root.rglob("*.class")), "Loose generated class files"
        assert not list((root / "mudlibs/lp245/players").glob("*.o")), "Shipped player saves"
        assert not list(root.rglob("*.log")), "Shipped logs"
        for name in ("obj/player.c", "jvmud/transpilation.json", "room/init_file"):
            assert (root / "mudlibs/lp245" / name).is_file(), name
        with zipfile.ZipFile(next((root / "lib").glob("jvmud-*.jar"))) as jar:
            classes = [name for name in jar.namelist() if name.endswith(".class")]
            assert classes
            assert all(int.from_bytes(jar.read(name)[6:8], "big") <= 65 for name in classes)
        # An intentionally minimal PATH proves these launchers do not call Maven.
        path = work / "tools"
        path.mkdir()
        bundled = (root / "jre").is_dir()
        for tool in (("sh", "dirname", "sed") if bundled else ("sh", "dirname", "sed", "java")):
            executable = shutil.which(tool)
            assert executable, f"Missing test prerequisite: {tool}"
            (path / tool).symlink_to(executable)
        env = dict(os.environ, PATH=str(path))
        env.pop("JVMUD_JAVA_HOME", None)
        if bundled:
            env["JAVA_HOME"] = str(work / "ignored-system-java")
            assert not shutil.which("java", path=str(path))
            settings = subprocess.run([str(root / "jre/bin/java"), "-XshowSettings:properties", "-version"],
                                      capture_output=True, text=True, check=True)
            assert "java.specification.version = 21" in settings.stderr
        cwd = work / "caller directory"
        cwd.mkdir()
        for launcher in ("jvmud-start", "jvmud-console", "jvmud-format"):
            subprocess.run([str(root / "scripts" / launcher), "--help"],
                           cwd=cwd, env=env, capture_output=True, check=True, timeout=15)
        invalid = dict(env, JVMUD_JAVA_HOME=str(work / "missing-java"))
        result = subprocess.run([str(root / "scripts/jvmud-start"), "--help"],
                                cwd=cwd, env=invalid, capture_output=True, text=True)
        assert result.returncode != 0 and "Java 21" in result.stderr
        sample = cwd / "sample file.c"
        sample.write_text("int answer(){return 42;}\n")
        subprocess.run([str(root / "scripts/jvmud-format"), sample.name],
                       cwd=cwd, env=env, capture_output=True, check=True, timeout=15)
        assert "42" in sample.read_text()
        for world in ("smallmercies", "lp245"):
            check_world(root, cwd, env, world)
        print(f"PASS: {archive.name}; extracted paths with spaces, runtime-only launchers, Java guard, formatter")


if __name__ == "__main__":
    main()
