# Lesson 02 — Variables & types

> **Stage 1A · ~30 minutes · Prerequisite: 01**

Lesson 01's deadband threshold was `0.1`. By the end of the season that number will
appear in the drivetrain, in the turret, in a test, and in an auto routine. One
Saturday you will decide 0.1 is too twitchy and change it to 0.08.

You will find three of the four.

The robot will then behave differently depending on which stick you push, and you
will spend an hour convinced the drivetrain is broken. Everyone does this once.
Today you do the thing that stops it.

## What you'll learn

1. Declare `public static final` constants and explain what each word buys you.
2. Group related constants in nested classes.
3. Choose sensible primitive types — `double` for measurements, `int` for counts.
4. Replace a magic number with a name, at every call site.

## What you'll do

Open `src/main/java/frc/robot/Constants.java`. Most of it is filled in. Four values
are placeholders, marked `TODO (LESSON 02)`:

| Constant | Should be | What it means |
|---|---|---|
| `Drive.DEADBAND` | `0.10` | Lesson 01's threshold, with a name |
| `Drive.MAX_VOLTS` | `12.0` | The most voltage we will ever command |
| `Drive.GEAR_RATIO` | `8.45` | Motor rotations per wheel rotation |
| `Flywheels.MAX_RPM` | `5800.0` | Free speed of the shooter motor |

Set them. Leave the names and the `public static final double` exactly as they are.

### What the three modifiers actually do

```java
public static final double DEADBAND = 0.10;
```

- **`public`** — any class may read it. Constants are meant to be shared; that is
  the entire point.
- **`static`** — it belongs to the class. There is one `DEADBAND` in the whole
  program, and you never write `new Constants.Drive()`.
- **`final`** — it cannot be reassigned. Not by you, not by a teammate, not by you
  at 2am at a competition. The compiler enforces it, which is worth more than a
  comment asking nicely.

The rubric checks all three by reflection — it asks the JVM to describe the fields
— because a constant that is not `final` is just a global variable that nobody has
changed yet.

### Why nested classes

Look at the shape of the file:

```java
public final class Constants {
    public static final class Drive { ... }
    public static final class Elevator { ... }
    public static final class Flywheels { ... }
}
```

Two payoffs. Call sites read as sentences —
`Constants.Elevator.MAX_HEIGHT_METERS` says where it belongs and what it is, with
no comment needed. And adding a subsystem never forces anybody to renumber or
reshuffle anything: you add a nested class and stop.

Presto and Kelpie both go one step further and keep each group next to the
subsystem it configures. Both layouts are common; the one thing nobody does
successfully is scatter loose numbers through the code.

### Types

`double` for anything measured: distances, angles, volts, speeds. `int` for
anything counted: port numbers, encoder ticks per revolution, how many game pieces
you are holding.

The temptation is to use `int` for a distance in centimetres because it looks
tidier. Resist it. Integer division silently truncates — `5 / 2` is `2` in Java,
not `2.5` — and the resulting bug is a robot that is consistently, inexplicably
slightly short.

## Run it

```bash
./tools/frcprog check 02-variables-and-types
```

The rubric checks:

1. `Drive.DEADBAND` is `0.10`.
2. The other three placeholders have their real values.
3. All four are `public static final`, verified by reflection.
4. Nothing subsystem-specific has escaped to the top level of `Constants`.

## See it

```bash
./tools/frcprog sim
```

Nothing visibly changes, and that is the correct outcome: naming a number does not
alter what it is. What changed is that there is now exactly one place to edit it.

## Done?

```bash
./tools/frcprog next
```

Lesson 03 is the one where things get deliberately messy.

## The rule this lesson is really teaching

**A number that means something gets a name.**

Not every number — `2` in `(a + b) / 2` is arithmetic, and naming it `TWO` helps
nobody. The test is whether the number could ever change, and whether changing it
would require finding every copy. `0.1` as a deadband: yes, and yes. `8.45` as a
gear ratio: absolutely — the day the mechanical team swaps a gearbox, you want to
edit one line, not go hunting.
