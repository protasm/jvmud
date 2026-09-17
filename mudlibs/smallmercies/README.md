# Small Mercies

A tiny, cheerful mudlib bundled with JVMud. The whole world fits on a postcard;
the goose would still like to see your permit.

## Play

From the JVMud repository root, with Java 21 or later and Maven installed:

```sh
scripts/jvmud-start mudlibs/smallmercies/jvmud/smallmercies.config
```

Connect a MUD client or Telnet client to `localhost:4000`. Enter a name of
2–16 letters, choose `male` or `female`, then `warrior` or `mage`.
Gender changes the character description, not the rules or attributes.
Names must be unique among connected visitors.

Characters are guests: there are no passwords or saved characters. Name,
class, health, and victories last for this connection only. NPC state is shared
and continues while the server runs, independently of visitors. Restarting the
server resets the world. This deliberately small example has no inventory,
levels, shops, quests, equipment, or player-versus-player combat.

## The whole world

```text
                    Herb Garden
                     (goose)
                        |
The Resting Hero -- Village Square -- Very Short Lane
  (innkeeper)          (mayor)              |
                                          down
                                           |
                                  Cellar of Mild Peril
                                          (rat)
```

The inn is west of the square, the garden north, and the lane east.
The cellar is down from the lane. Every exit has a return route.

| Command | What it does |
| --- | --- |
| `help` | Show the command list and combat rules. |
| `look` / `l`, `look <name>` | Describe the room and its occupants, or someone here. |
| `north`, `south`, `east`, `west`, `up`, `down` | Move; initials and `go <direction>` also work. |
| `say <message>` | Speak to everyone in your current room, including yourself. |
| `who` | List adventurers who have finished character creation. |
| `talk <npc>` | Hear the mayor, innkeeper, goose, or rat's very limited wisdom. |
| `smile`, `wave`, `bow`, `laugh` | Perform a room-local emote. |
| `score` | Show name, gender, class, STR/INT/DEX, HP, victories, and opponent. |
| `roll` | Roll two six-sided dice for everyone in the room to see. |
| `attack <npc>` / `kill <npc>` | Begin repeated sparring with the goose or rat. |
| `stop` | End your bout immediately. Movement also ends combat. |
| `rest` | Recover all HP at the inn. |
| `quit` | Leave and discard this guest character. |

## Rules small enough to remember

| Class | STR | INT | DEX | Maximum HP |
| --- | ---: | ---: | ---: | ---: |
| Warrior | 12 | 6 | 9 | 32 |
| Mage | 6 | 12 | 9 | 26 |

Every two seconds, warriors swing for `2 + STR / 3` damage; mages cast a
spark for `2 + INT / 3`. Division is integer division. If the opponent remains
standing, it retaliates. Dodge checks roll twice from 0–29 and keep the lower
result; a result below DEX dodges. At DEX 9 this gives a 51% dodge chance. Gender has no
mechanical effect. There are no additional attributes or mana points.

The goose has 16 HP and hits for 4; the rat has 20 HP and hits for 5.
Only one visitor can spar with an NPC at a time. Walking away, stopping, or
disconnecting releases that opponent; its remaining HP persists. Defeated NPCs
take a twenty-second breather and then recover fully. Each victory adds one to
your score. If you run out of HP, the innkeeper wheels you to the inn at 1 HP.
Type `rest` for tea, toast, and full recovery. Nobody dies permanently.

## Read and change it

All gameplay is typed LPC using native JVMud functions; no compatibility mudlib,
account service, database, or other world is required.

- `jvmud/smallmercies.config`: boot profile, preload manifest path, lifecycle mappings,
  session input capability, and a one-second world tick.
- `source/preload_objects`: seven startup object paths, one per line in load order.
- `source/lib/room.c`: room descriptions, exit mappings, and NPC placement.
- `source/lib/mercies.c`: shared mudlib functions selected by `mfun_object`.
- `source/room/*.c`: the five rooms, their reciprocal routes, and residents.
- `source/player/adventurer.c`: character creation, commands, and a two-second
  recurring combat callback. Output from timed callbacks targets the player's
  session explicitly.
- `source/npc/villager.c`: dialogue, one-challenger combat ownership, shared HP,
  and a delayed recovery callback.

To add a room, inherit `lib/room`, set the title, description, routes and exit
text in `create`, add a return exit in its neighbor, and add it to
`source/preload_objects`. Change the two class stat assignments in `choose_class` to
experiment with combat balance. The engine code needs no changes.

## Mudlib functions

`mfun_object = lib/mercies` makes the typed functions in `source/lib/mercies.c`
available to other objects without inheritance or explicit object calls. The
shared object is stateless; pass the source object explicitly when an operation
needs its location.

- `roll_dice(count, sides)` adds independent rolls. It accepts 1–100 dice with
  1–1000 sides; invalid requests return zero. The `roll` command uses `roll_dice(2, 6)`.
- `health_description(health, maximum)` supplies the same gently heroic condition
  descriptions for `score`, looking at adventurers, and looking at villagers.
- `announce_near(source, message)` adds a newline and delivers to everyone at the
  source's location. Chat, emotes, dice rolls, and timed NPC recovery use it.
  An absent source or location produces no announcement.
- `mercy_random(limit)` rolls twice and keeps the lower
  result, giving adventurers a small mercy on dodge checks. Nonpositive limits
  return zero. Both this helper and `roll_dice` call the engine's
  `jvmud_random(limit)` primitive. Dice stay fair; dodge checks explicitly choose
  the merciful game rule. The `jvmud_` prefix is reserved for engine symbols and
  cannot be used in mudlib declarations.

These are mudlib rules written in LPC. NPC recovery demonstrates that announcements also work
from a timed callback, without relying on a current player.

## Verification

From the repository root:

```sh
mvn -Dtest=SmallMerciesTest test
mvn test
```

The focused integration tests boot the real profile and exercise session
input validation, both classes, room-local chat and emotes, every route, NPC
contention, scheduled victory/recovery, stop/movement/disconnect cleanup,
defeat-to-inn, and healing.

For a live transport smoke, start a fresh toy server with the launch command
above, then run in another terminal (Python 3 required):

```sh
python3 src/test/scripts/smallmercies/smoke-play.py
```

This connects two disposable guests, walks the tutorial route, checks chat and
emotes, waits for a real timed combat victory, checks score and healing, and
quits. It prints `PASS` only after all checks succeed. Start with a fresh world
so the goose is available for the bout.

Verification completed: all three toy integration tests, the restarted live
Telnet smoke, and the 478-test credential-free baseline passed. The full
`mvn test` was also attempted; 27 existing external-database tests could not
run because their required password environment variable was unavailable.
To run the same credential-free baseline:

```sh
mvn test
```
