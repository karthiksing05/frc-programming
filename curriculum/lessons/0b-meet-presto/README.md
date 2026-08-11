# Lesson 0B — Meet Presto

> **Stage 0 · ~15 minutes · Prerequisite: 0A**

You are about to spend thirty lessons writing robot code. It helps enormously to
have seen where it ends up.

Presto is Team 6328 Mechanical Advantage's 2024 Crescendo robot. Their code is
public, it is MIT licensed, and it is written by the same people who wrote
AdvantageKit and AdvantageScope — the two tools this curriculum uses. When we say
"this is how real teams do it", Presto is a substantial part of what we mean.

## What you'll learn

1. What each of Presto's mechanisms does, in plain language.
2. Where to find a subsystem in a real competition codebase.
3. That production robot code is smaller and more readable than you expect.

## What you'll do

### The robot

Crescendo asked robots to pick foam rings off the floor and fire them into a goal
seven feet up. Presto's answer:

| Mechanism | What it does |
|---|---|
| **Swerve drive** | Four wheels that each steer independently, so the robot can move in any direction without turning first. |
| **Flywheels** | Two wheels spinning at a few thousand RPM. A ring fed between them leaves at speed. Two *independent* wheels, so spinning them at different speeds puts curve on the shot. |
| **Rollers** | Move a ring from the intake, through the robot, into the flywheels. |
| **Arm** | Pivots the whole shooter up and down to aim. |
| **Climber** | Pulls the robot off the ground at the end of the match. |
| **Backpack** | A second, smaller scoring mechanism for the "trap". |

Watch a match if you can find one. The
[6328 build thread](https://www.chiefdelphi.com/c/general/build-blogs/189) has
video and, more valuably, the reasoning behind each decision.

### The code

Open <https://github.com/Mechanical-Advantage/RobotCode2024Public> and navigate to
`src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/`.

You will find four files:

```
FlywheelsIO.java              what the flywheels can sense and be told
FlywheelsIOSim.java           a physics model, for running on a laptop
FlywheelsIOKrakenFOC.java     real motors, one vendor
FlywheelsIOSparkFlex.java     real motors, a different vendor
Flywheels.java                the actual logic — the only file that decides anything
```

Open `FlywheelsIO.java`. It is about forty lines and most of them are a list of
things a flywheel can tell you: position, velocity, applied volts, current
temperature.

That split — one file for "what the hardware is", one file per "which hardware",
one file for "what we do about it" — is the pattern this curriculum builds toward.
You will construct it yourself in lesson 16, by which point you will have felt why
it is worth the extra files. For now, just notice it exists.

**Do not try to understand all of Presto.** It is a full season of work by a very
good team. The point of this lesson is to make the destination visible, not to
arrive at it today.

### One thing worth doing

Open `Flywheels.java` — the logic file — and read the method names without reading
the bodies. `runVelocity`, `stop`, `atGoal`. That is roughly the vocabulary you
will be writing in by lesson 10.

## Done?

You can name Presto's mechanisms and you have found `FlywheelsIO.java` with your
own hands.

Nothing to run, nothing to grade. Move on:

```bash
./tools/frcprog next
```

## Why this is here

New programmers routinely assume real teams' code is beyond them — some other
category of thing, written by people who already knew everything. It is not. It is
ordinary Java, in small files, with clear names, and in six months you could
contribute to it.

Seeing that on day one, before anything is hard, is worth fifteen minutes.
