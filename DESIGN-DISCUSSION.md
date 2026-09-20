# JVMud design walkthrough: topics to revisit

These original walkthrough notes predate the worker-process and administration
redesign. The implemented ownership and lifecycle decisions are documented in
[Engine and administration](docs/ENGINE-ADMINISTRATION.md).

Original discussion list:

- Initial startup object: TelnetServer?
- Mudlib Router should be capable of offering a menu of mudlibs.
- Remove support for world-hopping.
- If `mud` is a mud router, call it `router`.
- Keep interpretation of player input and command-result messages in the mudlib. `TelnetSession` should not interpret a dispatch result of integer `0` as a rejected command or print "You can't do that." Any return value to the transport should describe input delivery or session status, not the input's content or meaning.
- Give each MudInstance its own clock, configurable tick interval, and independent execution instead of feeding ticks sequentially from the router. Queue player input and process commands and scheduled work serially within each world, so a slow world does not block other worlds. The router handles world selection and routes input to the chosen instance; worlds do not require coordinated timing or a shared tick rate.
