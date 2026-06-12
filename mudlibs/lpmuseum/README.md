# LPMuseum

LPMuseum is a native JVMud mudlib. It is intentionally free-standing: it can
boot, accept Telnet sessions, bind a Persona, route commands, move through
Places, and present Entities without loading any exhibit mudlib.

## Native Shape

- `jvmud/lpmuseum.config` declares the JVMud boundary in native terms.
- `source/persona/visitor.c` owns the player-facing Persona experience.
- `source/place/` contains world Places and exit policy.
- `source/entity/` contains inspectable Entities and local affordances.
- `source/system/` contains shared museum services such as help text.

LPMuseum may mount exhibit mudlibs through explicit portals, but exhibits are
subordinate destinations. The museum's own login, command grammar, Places,
Entities, and documentation remain useful without them.
