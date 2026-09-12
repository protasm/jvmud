#!/usr/bin/env python3
"""Exercise a running Small Mercies server on loopback; create disposable guests."""
import re
import socket
import time
import uuid


class Guest:
    """A minimal Telnet client with bounded reads and explicit output assertions."""
    def __init__(self, name, gender, profession):
        self.socket = socket.create_connection(("127.0.0.1", 4000), timeout=5)
        self.socket.settimeout(0.2)
        self.until("Name (2-16 letters): ")
        self.command(name, "Gender (male/female): ")
        self.command(gender, "Class (warrior/mage): ")
        self.command(profession, "> ")

    def until(self, marker, timeout=8):
        deadline = time.monotonic() + timeout
        text = ""
        while time.monotonic() < deadline:
            try:
                data = self.socket.recv(8192)
            except socket.timeout:
                if marker in text:
                    return text
                continue
            if not data and marker in text:
                return text
            if not data:
                raise AssertionError("Connection closed: " + text)
            # Negotiation commands are separate from the gameplay being checked.
            data = re.sub(rb"\xff[\xfb-\xfe].", b"", data)
            text += data.decode("utf-8", errors="replace").replace("\r", "")
        raise AssertionError(f"Missing {marker!r}: {text}")

    def command(self, line, marker="> "):
        self.socket.sendall((line + "\n").encode())
        return self.until(marker)

    def close(self):
        self.socket.close()


def contains(text, expected):
    assert expected in text, f"Missing {expected!r}: {text}"


def main():
    # Hex digits mapped to letters keep names valid and avoid existing visitors.
    suffix = uuid.uuid4().hex[:8].translate(str.maketrans("0123456789", "ghijklmnop"))
    alice = Guest("Ada" + suffix, "female", "warrior")
    bob = None
    try:
        bob = Guest("Ben" + suffix, "male", "mage")
        contains(alice.command("score"), "STR 12  INT 6  DEX 9")
        contains(bob.command("score"), "STR 6  INT 12  DEX 9")
        contains(alice.command("say Tea first"), "says: Tea first")
        contains(bob.until("Tea first"), "says: Tea first")
        contains(alice.command("wave"), "waves with unnecessary ceremony")
        route = [("look", "Village Square"), ("talk mayor", "manageable expectations"),
                 ("west", "The Resting Hero"), ("talk innkeeper", "full health"),
                 ("rest", "Tea, toast"), ("east", "Village Square"),
                 ("north", "Municipal Herb Garden"), ("look goose", "tiny helmet"),
                 ("south", "Village Square"), ("east", "Very Short Lane"),
                 ("down", "Cellar of Mild Peril"), ("look rat", "Duke of Cheddar"),
                 ("up", "Very Short Lane"), ("west", "Village Square")]
        for command, expected in route:
            contains(alice.command(command), expected)
        contains(alice.command("north"), "Municipal Herb Garden")
        contains(alice.command("attack goose"), "begin sparring")
        contains(alice.until("Victory!", timeout=10), "Victory!")
        contains(alice.command("score"), "Victories 1")
        contains(alice.command("stop"), "Hostilities adjourned")
        contains(alice.command("south"), "Village Square")
        contains(alice.command("west"), "The Resting Hero")
        contains(alice.command("rest"), "full health")
        contains(alice.command("quit", "guest book"), "guest book")
        contains(bob.command("quit", "guest book"), "guest book")
        print("PASS: live guest login, both classes, chat, emote, tutorial route, timed combat, score, rest, quit")
    finally:
        alice.close()
        if bob:
            bob.close()


if __name__ == "__main__":
    main()
