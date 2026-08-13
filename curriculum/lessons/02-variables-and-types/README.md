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

## How it works

### What the three words buy you

```java
public static final double DEADBAND = 0.10;
```

**`public`** lets any class read it. Constants exist to be shared, so anything
narrower defeats the purpose.

**`static`** attaches it to the class rather than to an object. There is one
`DEADBAND` in the whole program and you never write `new Constants.Drive()`. If it
were not static, every class wanting the value would need an instance, which is
pointless bookkeeping for a number that never changes.

**`final`** stops reassignment. The compiler refuses. This is the one that earns its
keep: it is 11pm at a competition, somebody is editing fast, autocomplete offers
`Constants.Drive.DEADBAND =` and Java says no.

A constant that is not `final` is a global variable nobody has changed yet.

??? question "Predict: what does the rubric check by reflection, and why?"

    It asks the JVM to describe the field and confirms all three modifiers.

    Values alone would not catch dropping `final`. The number would still be 0.10
    and every behavioural test would pass, right up until somebody assigns to it.

    This is a general and useful idea: when the property you care about is about the
    *shape* of the code rather than its output, reflection is how a test reaches it.

### Why nested classes

```java
public final class Constants {
    public static final class Drive { ... }
    public static final class Elevator { ... }
}
```

Two payoffs, and the second is the one you feel later.

Call sites read as sentences. `Constants.Elevator.MAX_HEIGHT_METERS` tells you what
it is and where it belongs with no comment.

And adding a subsystem never disturbs anyone. You add a nested class and stop. With
one flat namespace, `MAX_HEIGHT` from the elevator and `MAX_HEIGHT` from the climber
collide, and somebody renames one to `MAX_HEIGHT_2`.

Presto and Kelpie both go one step further and keep each group in the subsystem's
own folder. Either works. What nobody does successfully is scatter loose numbers.

### Types, and one trap

`double` for anything measured: distances, angles, volts, speeds.
`int` for anything counted: port numbers, ticks per revolution, game pieces held.

The trap is integer division. Java truncates:

```java
int a = 5, b = 2;
System.out.println(a / b);        // 2, not 2.5
```

Store a distance in centimetres as an `int` to look tidy, divide it somewhere, and
your robot ends up consistently slightly short. Nothing warns you.

??? info "When is a number NOT worth naming?"

    `(a + b) / 2` is an average. Naming the `2` helps nobody.

    Two questions decide it:

    1. Could this value ever change?
    2. If it changed, would I have to hunt for copies?

    `0.1` as a deadband: yes and yes. `8.45` as a gear ratio: absolutely, the day
    mechanical swaps a gearbox. The `2` in an average: no and no.

## See it

```bash
./tools/frcprog sim
```

Nothing changes. Naming a number does not alter it.

Open **NetworkTables** in the simulator and look around, though. Everything the
robot publishes is there, live, and it is the fastest way to confirm a value is what
you think it is.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

Lesson 03 is where things get deliberately messy.
