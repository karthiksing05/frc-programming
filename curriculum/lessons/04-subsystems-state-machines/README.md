# Lesson 04 — Subsystems as state machines

> **Stage 1B · ~45 minutes · Prerequisite: 03**

Look at what you wrote yesterday. It works. It is also a method that knows about a
sensor's wiring polarity, a motor's speed constants, a controller's button layout,
and a policy decision about when to stop intaking — all at once, in one place, with
nothing separating them.

Today you take it apart, and the pieces end up somewhere better.

## What you'll learn

1. Write a class that `extends SubsystemBase`.
2. Keep hardware `private final`, so nothing outside the class can touch it.
3. Model a mechanism's modes with an `enum` instead of a pile of booleans.
4. Split *deciding* (`setMode`) from *acting* (`periodic`), and see why that helps.
5. Shrink `Robot.teleopPeriodic()` back to something you can read at a glance.

## What you'll do

Three files change, and the shape of the change is the lesson.

### 1. Fill in `RollerSubsystem`

Open `src/main/java/frc/robot/subsystems/roller/RollerSubsystem.java`. Two TODOs.

**`setMode(State desired)`** stores the requested state. One line. Note what it does
*not* do: it does not touch the motor. Deciding is instantaneous and can happen from
anywhere; acting happens fifty times a second in one known place. Keeping those
apart is most of what a subsystem is for.

**`periodic()`** turns the current state into a motor output:

| State | Output |
|---|---|
| `OFF` | `0.0` |
| `INTAKING` | `INTAKE_SPEED`, unless `hasGamePiece()` — then `0.0` |
| `EJECTING` | `EJECT_SPEED`, always |

`periodic()` is called for you by the `CommandScheduler`, every loop, because the
class extends `SubsystemBase`. You never call it yourself.

### 2. Empty out `Robot.java`

Delete the motor and the beam-break fields. They belong to the roller now. Replace
the body of `teleopPeriodic()` with three or four lines that say what the operator
wants and nothing about how it happens.

Notice what disappears in the process: **the beam-break check is gone from
`Robot.java` entirely.** Not because it stopped mattering, but because "don't keep
intaking once you have one" was never the driver's decision. It is a fact about how
a roller should behave, and it now lives with the roller.

### 3. Construct it in `RobotContainer`

`RobotContainer` currently has:

```java
private RollerSubsystem roller = null;
```

Make it a real object:

```java
private final RollerSubsystem roller = new RollerSubsystem();
```

**This is why it was null.** WPILib allocates a PWM channel to exactly one object.
While `Robot` owned a motor on PWM 5, constructing a `RollerSubsystem` here would
have crashed at startup with "PWM 5 already allocated". That error was not in your
way — it was the framework refusing to let two pieces of code own one motor. Now
that `Robot` has let go, the roller can take it.

### Why an enum

```java
public enum State { OFF, INTAKING, EJECTING }
```

Compare with the obvious alternative, a couple of booleans. `boolean isIntaking`
works until you add ejecting, at which point you have `isIntaking` and `isEjecting`
and four combinations, one of which — both true — describes a mechanism that
physically cannot exist. Enums make illegal states unrepresentable, and a `switch`
over one is exhaustive: add a fourth state later and the compiler shows you every
place that needs to handle it.

## Run it

```bash
./tools/frcprog check 04-subsystems-state-machines
```

Six checks. The first four are the *same four scenarios lesson 03 tested* — because
the robot's behaviour must not change during a refactor. If the behaviour changed,
you did not refactor, you rewrote.

Checks 5 and 6 are the new part:

5. The motor and sensor fields are `private final` (checked by reflection).
6. `Robot.java` no longer mentions `PWMSparkMax` or `DigitalInput`, and does say
   `setMode`.

Check 6 exists because you could pass the first five with everything still in
`teleopPeriodic`. The point of the lesson is *where the code lives*.

## See it

```bash
./tools/frcprog sim
```

Identical behaviour to lesson 03 — same PWM output, same buttons. That is the
success condition. What changed is the file you would open to fix a roller bug.

## Done?

```bash
./tools/frcprog next
```

## The two promises a subsystem makes

**It owns its hardware.** `private final` means nothing outside this file can call
`motor.set(...)`. When the roller misbehaves, the bug is in this file. That
guarantee is worth an enormous amount on a team where six people are editing the
same repository during build season.

**It exposes intent, not mechanism.** Callers say `setMode(State.INTAKING)`. They do
not say "0.6 unless the beam is broken". What 0.6 means, and when it does not apply,
is the roller's business.

Kelpie's [`roller/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller)
is this exact class on a competition robot, one refactor further along — the
refactor you will do in lesson 16.
