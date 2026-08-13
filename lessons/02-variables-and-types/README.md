# Lesson 02 — Variables and types

**Stage 1A · 30 min · Needs: 01**

That `0.1` will end up in four files. One Saturday you will change three of them.

## Do this

1. Open `src/main/java/frc/robot/Constants.java`
2. Find the two `TODO (LESSON 02)` comments
3. Set these four values:

| Constant | Set to | What it is |
|---|---|---|
| `Drive.DEADBAND` | `0.10` | lesson 01's threshold |
| `Drive.MAX_VOLTS` | `12.0` | most voltage we ever command |
| `Drive.GEAR_RATIO` | `8.45` | motor turns per wheel turn |
| `Flywheels.MAX_RPM` | `5800.0` | motor free speed |

Do not rename anything or change `public static final`.

## Check it

```bash
./tools/frcprog check 02-variables-and-types
```

Checks the four values, that all of them are `public static final` (by reflection),
and that nothing subsystem-specific escaped to the top level.

## Why

**`public`** — anyone may read it. Constants are for sharing.
**`static`** — one per program. You never write `new Constants.Drive()`.
**`final`** — nobody can reassign it. Not you, not a teammate, not you at 2am at a
competition. The compiler enforces it.

A constant that is not `final` is a global variable nobody has changed yet.

**Nested classes** give you `Constants.Elevator.MAX_HEIGHT_METERS`, which reads
like a sentence, and mean adding a subsystem never disturbs anyone else's imports.

**Types:** `double` for anything measured. `int` for anything counted. Integer
division truncates silently, so `5 / 2` is `2`, and your robot ends up
consistently slightly short.

## See it

```bash
./tools/frcprog sim
```

Nothing changes. Naming a number does not alter it. What changed is that there is
now one place to edit it.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**The rule:** a number that means something gets a name. Not every number. The test
is whether it could change, and whether changing it would mean hunting for copies.
