# Avelorn world architecture

## Scale and topology

Avelorn contains exactly 100,000 connected, traversable Places:

- 65 individually authored Places in the original western chapter;
- 99,935 deterministic Places in the greater realm;
- 253 columns by 395 rows in the greater-realm lattice; and
- 220 geographical regions, normally 23 by 20 rooms each.

The Western Crown Lantern connects the authored chapter to greater-realm room
`place/world/r00000`. Greater-realm identifiers run through
`place/world/r99934`. They are stable across restarts and suitable for saved
character locations and automapper identities. Rooms load only when visited.

Royal-road rows and columns provide long-distance bearings. Local routes fill
the land between them, so the realm is fully traversable rather than a set of
disconnected scenic backdrops.

## Major geography

- **Crownspire** occupies twenty-four large districts. Its playable fabric
  includes residential and commercial streets, quays, canals, six undercity
  and sewer districts, palace gardens, two royal-castle levels, a citadel,
  barracks, archives, temples, markets, and service passages.
- **Irongate** is a northern foundry and fortress city with wards organized
  around gates, guilds, temples, markets, and its citadel.
- **Saltmere** is a southern marsh and port city whose docks and civic wards
  open into a broad wetland frontier.
- **Elderwild** and the **Dawn Coast** are multi-region wilderness anchors.
  The remaining named marches, vales, reaches, forests, fens, ridges, coasts,
  and borders form the connective world map.
- Wilderness points of interest include shrines, abandoned camps, crofts,
  wells, forts, mines, caves, barrows, hermit cells, towers, ferries, bridges,
  hunter blinds, standing stones, and signal cairns.

## Gloamhold

Gloamhold is an abandoned castle complex occupying approximately 5,500 rooms.
Its twelve areas include the ruined demesne, outer works, lower castle, hollow
courts, upper keep, broken towers, servants' labyrinth, undercroft, prison
levels, buried palace, flooded vaults, and moonless grotto.

The story unfolds through examinable architecture. During the Long Vigil,
Regent Maelin's astronomer-priests mistook resonance in the grotto for
prophecy. By feeding it vows, secrets, and fear, generations of inhabitants
made an echo sentient: the Listener in the Water. The castle barred itself,
officers who rejected a midnight oath were imprisoned, and mechanisms that
isolated the grotto failed. Drowned archives, inward-facing signal mirrors,
servants' records, reversed lantern emblems, and the final pool reveal that no
invading army destroyed Gloamhold.

Vertical links supplement the castle's halls, stairs, galleries, vaults, and
grotto passages. `look` and `examine` expose the lantern, reflection, water,
Listener, walls, terrain, routes, and each room's architectural feature.

## The Deep Concord

The Deep Concord is a living subterranean civilization occupying approximately
8,000 rooms across three districts and six principal physical depths. Its
Z-negative hierarchy assigns greater ceremonial status to greater physical
depth:

1. Z-1 envoy galleries face the surface and carry the lowest formal rank.
2. Z-2 contains makers, mills, kilns, guilds, and lifts.
3. Z-3 contains civic halls, archives, schools, markets, and clinics.
4. Z-4 contains noble and temple deeps.
5. Z-5 contains the sovereign council and underground river city.
6. Z-6 reaches crystal farms, rootward mines, and the founding chasm.

The hierarchy began as an emergency evacuation scheme during an ancient
surface catastrophe and later hardened into hereditary theology. Boat crews,
flood engineers, archivists, farmers, and dissident "Risers" complicate the
official order. The Risers build level meeting halls and mark forbidden upward
routes with an arrow inside an open circle. Examinable depth seals, rank stairs,
ledgers, lifts, locks, gardens, assembly vaults, and founding tablets carry the
story through ordinary civic space.

## Content rules

Generated room prose describes only permanent scenery. Every named scenery
feature can be inspected with `look`, `examine`, or `exa`; terrain, routes, and
weather have their own examination text. Occupants and portable objects are
real world entities, appear in room inventories, and use the normal interaction
system. Deterministic population intervals keep a lazily loaded world alive
without pre-creating a hundred thousand inventories at startup.

Room names combine region, local aspect, and feature so all rooms within a
region have stable unique mapper labels. The in-game `atlas` command provides
the high-level orientation without revealing every local discovery.
