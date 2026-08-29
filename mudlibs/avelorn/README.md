# Avelorn: The Lantern Crown

Avelorn is JVMud's native exemplar game: a persistent, multiplayer,
medieval swords-and-sorcery kingdom authored directly against JVMud's native
engine-mudlib boundary.

The initial campaign begins in Brindleford and follows a newly licensed member
of the Company of the Lantern through ten levels of service to a prosperous,
well-ordered kingdom. The launch story culminates in rekindling the western
Lantern beneath Ashenwatch Keep.

## Native shape

- `jvmud/avelorn.config` declares the JVMud boundary and lifecycle vocabulary.
- `source/persona/` owns character policy and player-facing interaction.
- `source/place/` contains linked Places in the kingdom.
- `source/system/` contains non-present mudlib services such as pronoun grammar.
- `accounts/` contains ignored runtime character snapshots.
- `docs/FOUNDATION.md` fixes the initial identifiers and persistence contract.

## Current playable slice

The world provides sixty-five connected Places spanning Brindleford, the
maintained Lantern Road, Greyhaven's civic center, the northern patrol road,
Merewatch, the Blackstone wardworks, and Ashenwatch Keep.
Characters can train, trade, rest, explore, evaluate hostile combatants, and
fight cooperatively.
Fighters, Rangers, Mages, and Clerics each have a distinct resource-powered
combat technique in addition to ordinary weapon attacks.
Every contributor still present at a victory receives credit. Equipment and
enemy recommendations are warnings and effectiveness adjustments rather than
hard locks.

The first complete assignment, *Miller's Unwelcome Guests*, can be accepted
from Miller Enid in the mill yard. Its three credited cellar clearances use a
timed hostile respawn, persist in the Company journal, and grant their turn-in
reward only once.

The second assignment, *Light for the Road*, sends a level-two Companion to
service three distinct ward lanterns between Brindleford and Greyhaven. Each
objective is credited once, demonstrating non-combat quest objectives and
soft-gated travel progression.

The third assignment, *The Silent Patrol Bell*, begins with Greyhaven's Crown
watch and confronts a level-four bell wraith at an intact but magically
silenced road post. It demonstrates a higher-risk soft gate, class techniques,
recoverable combat, and a timed enemy reset.

The fourth assignment, *Beneath Blackstone*, combines two distinct guardian
victories with restoration of an ancient wardstone. Unique objective tags
prevent repeated reset kills from substituting for the other required work.

The fifth assignment, *Rekindle the Western Lantern*, is a five-part capstone
across Ashenwatch Keep. Three escalating ward combatants and two restoration
objectives deliver the campaign's level-ten XP budget. Completion restores the
Lantern Crown and grants a persistent, softly gated Crown medal.

Character identity, progression, coin, inventory, and equipment persist as
host-filesystem snapshots beneath `accounts/`. Avelorn declares no database
capability and uses no database service.

JVMud host administration can reload a shared Avelorn service or content object
by stable object id while players remain connected. The reload operation is a
host API, not an unauthenticated player command; the acceptance suite verifies
that a connected journal immediately observes a reloaded quest definition.

Run the game from the repository root with:

```text
scripts/jvmud-start mudlibs/avelorn/jvmud/avelorn.config
```
