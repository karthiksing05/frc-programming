# Lesson 26 — maple-sim & game-piece physics

> **Stage 2C · ~55 minutes · Prerequisite: 25-multitag**
> **Extension lesson — needs one online build. See `lessons/EXTENSIONS.md`.**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Everything you have simulated so far is kinematic. The robot passes through walls. Game
pieces do not exist. Two robots cannot collide, because there is only one.

That is fine for control loops and hides a specific class of bug: autos that work
perfectly in simulation and fail on the field because they assumed frictionless wall
contact or a game piece that appears when you drive at it.

[maple-sim](https://shenzhen-robotics-alliance.github.io/maple-sim/) adds real
physics.

## What you'll learn

1. Install maple-sim and swap `ModuleIOSim` for `ModuleIOMapleSim`.
2. Put game pieces on the simulated field.
3. Detect intake by collision rather than by pretending.
4. Recognise which of your simulation results were never real.

## Before you start

Needs the maple-sim vendordep. See `lessons/EXTENSIONS.md`.

## What you'll do

Register a drivetrain with the simulated arena, swap the module IO implementation,
and add game pieces:

```java
SimulatedArena.getInstance().addGamePiece(new CrescendoNoteOnField(new Translation2d(2, 2)));
```

Then drive into one and watch the intake's beam-break trigger because a physical
object physically got there.

### What this catches

- Autos that assume you can push through a wall.
- Intakes that work in sim because the code says they do.
- Paths tuned on a frictionless model that are wrong on carpet.
- Robot-on-robot contact, which is most of a real match.

### What it costs

Physics simulation is more expensive than kinematics. Loop times go up. And it is
another vendordep to maintain across seasons.

Kelpie ships both `ModuleIOSim` and `ModuleIOMapleSim` for exactly this reason: use
the cheap one for control-loop work, the expensive one when you are validating an
auto. That the choice is a one-line swap is the IO layer paying off again — this is
the fourth implementation of the same interface, and nothing above the line changed.

## Done?

The robot collides with walls, driving into a game piece triggers the intake, and
you can be pushed off a target pose.

```bash
./tools/frcprog next
```
