# JVMud Mudlib

This directory contains the basic LPC mudlib content for JVMud.

## Layout

| Path | Purpose |
| --- | --- |
| `obj/` | Reusable LPC object definitions such as players, monsters, inventory, equipment, doors, and shared headers. |
| `room/` | Room and world content, including startup-oriented files, room headers, and nested area directories. |

The mudlib is content that the compiler/runtime will eventually compile and
load. Keep Java implementation work in `compiler/` or `runtime/`; keep LPC
world definitions here.
