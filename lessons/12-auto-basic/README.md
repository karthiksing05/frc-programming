# Lesson 12 — Autonomous

**Stage 1D · 40 min · Needs: 11**

Fifteen seconds where nobody may touch a controller.

## Do this

**1. `autos/SimpleAuto.java`** — fill in `driveAndScore`:

```java
return Commands.sequence(
        drive.driveDistanceCommand(2.0, 6.0),
        scoreOnce(flywheels, roller))
    .withTimeout(8.0)
    .withName("Drive and Score");
```

**2. `RobotContainer.configureAutos()`** — offer it:

```java
autoChooser.addOption("Drive and Score", SimpleAuto.driveAndScore(drive, flywheels, roller));
```

A routine nothing selects never runs.

## Check it

```bash
./tools/frcprog check 12-auto-basic
```

Five checks: it drives 1.8 m in three seconds, scores after driving rather than
during, finishes inside eight seconds, and leaves everything stopped.

## sequence, parallel or deadline

| Composition | Starts | Finishes |
|---|---|---|
| `Commands.sequence(a, b)` | a, then b | when b is done |
| `Commands.parallel(a, b)` | both | when **both** are done |
| `Commands.deadline(a, b)` | both | when **a** is done |

`sequence` here. `parallel` would spin the shooter while still driving, which is a
fine optimisation later and is not what this lesson asks for.

`deadline` is the one people forget. "Run the intake while driving this path" is
`deadline(followPath, runIntake)`.

## Bound everything

`.withTimeout(8.0)` on the whole routine.

Auto is fifteen seconds. A routine that stalls waiting for a sensor is **still
running** when teleop starts, still holding the drivetrain. Your driver pushes the
stick and nothing happens, because a command from twenty seconds ago has not let
go. This is a common way to lose a match and it is entirely preventable.

## Why it reuses everything

An auto routine is just a command, scheduled by `autonomousInit()` instead of a
button. Build teleop from small composable commands and auto is an afternoon.
Build it from `if` statements in `teleopPeriodic` and you start over.

The requirement system also saves you here: `driveDistanceCommand` and the teleop
default command both require the drivetrain, so the scheduler will never run both.

## See it

```bash
./tools/frcprog sim
```

Click **Autonomous** instead of Teleoperated. Do not touch anything. Plot drive
distance and `Flywheels/TargetRPM`: distance climbs, flattens, then RPM jumps.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**Real-world note:** a reliable two-piece beats a four-piece that works one match in
three. A failed auto often costs more than it could have gained.
