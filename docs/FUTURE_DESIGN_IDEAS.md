# Future Design Ideas

This document stores design ideas that are worth remembering but not yet ready
for implementation.

Entries here are intentionally lighter than roadmap items. They should capture
the problem, the likely engine/mudlib split, and the reason to revisit the idea
later without implying that the current development slice should pursue it.

## Mudlib-Declared Efun Access Policy

JVMud may eventually need engine-side support for mudlib policies around which
mudlib objects can call engine-facing efuns directly.

The goal would not be to recreate legacy driver policy objects or make mudlib
authors master a large permission system before building a world. The goal would
be to give the engine a small enforcement mechanism while allowing the mudlib to
declare policy.

Possible shape:

- the engine enforces access checks at efun call time;
- the mudlib boundary declares which efuns are public, restricted, or
  boundary-only;
- high-impact operations such as object destruction, object spawning, session
  input capture, persistence, movement, scheduling, or future admin-facing
  operations can be guarded;
- ordinary world objects can still use safe world services without unnecessary
  ceremony.

Design pressure:

- unrestricted efun access may give every object too much leverage over engine
  state;
- overbuilt policy could become a new bridge-object burden;
- the preferred first slice would be a small allow/deny or category-based policy,
  not a full security language.

## Customizable Player Input Throttling

JVMud may eventually need engine-side support for player input throttling, with
room for mudlib-specific policy.

Transport should own basic session protection because raw input arrives there
before it becomes instance or world behavior. A client should not be able to
flood command dispatch, fill queues, starve other sessions, or hammer mudlib
code without limits.

Possible shape:

- the engine provides conservative default rate limits;
- the engine enforces queue size limits, flood protection, and disconnect or
  backpressure behavior;
- the mudlib can declare or influence gameplay-specific pacing, custom messages,
  role-based exemptions, login behavior, chat rules, combat pacing, or wizard
  privileges;
- admin and test paths remain able to inspect or override throttling deliberately.

Design pressure:

- no throttling is operationally risky for a networked multiplayer server;
- purely mudlib-side throttling happens too late to protect the transport and
  runtime;
- purely engine-hardcoded throttling may stifle mudlib-specific gameplay and
  moderation choices.

## Adding Future Entries

When adding a future design idea, prefer this shape:

- name the idea;
- explain why it might matter;
- describe the likely engine responsibility;
- describe the likely mudlib responsibility;
- note why it is not being implemented yet.
