# Lesson 12 — Auto routines

> **Stage 1D · ~40 minutes · Prerequisite: 11**

Fifteen seconds at the start of every match during which no human may touch a
controller. Whatever the robot does, it decided by itself.

That sounds like it needs new machinery. It does not. An auto routine is a command —
usually a composition of the same factories teleop already uses — that happens to be
scheduled by `autonomousInit()` instead of by a button.

That reuse is the payoff for lessons 07 through 11. Build teleop from small,
composable, requirement-aware commands and auto is an afternoon. Build it from `if`
statements in `teleopPeriodic` and you start over.

## What you'll learn

1. Compose an auto with `Commands.sequence`.
2. Tell `sequence`, `parallel` and `deadline` apart, and pick deliberately.
3. Put a timeout on everything, and know what it is protecting you from.
4. Publish a `SendableChooser` so the routine can be picked at the field.

## What you'll do

### 1. `SimpleAuto.driveAndScore`

Open `src/main/java/frc/robot/autos/SimpleAuto.java`:

```java
return Commands.sequence(
        drive.driveDistanceCommand(2.0, 6.0),
        scoreOnce(flywheels, roller))
    .withTimeout(8.0)
    .withName("Drive and Score");
```

### 2. Offer it on the dashboard

In `RobotContainer.configureAutos()`:

```java
autoChooser.addOption("Drive and Score", SimpleAuto.driveAndScore(drive, flywheels, roller));
```

`getAutonomousCommand()` already returns the chooser's selection, and `Robot`
already schedules it in `autonomousInit()`. The "Do Nothing" default is already
there — a robot that does nothing scores zero, but a robot running a half-finished
routine can foul, or drive into a partner.

### sequence vs parallel vs deadline

| Composition | Starts | Finishes |
|---|---|---|
| `Commands.sequence(a, b)` | a, then b when a is done | when b is done |
| `Commands.parallel(a, b)` | both at once | when **both** are done |
| `Commands.deadline(a, b)` | both at once | when **a** is done |

This routine wants `sequence`: drive, *then* score. `parallel` would spin the shooter
while the robot is still moving, which is a legitimate optimisation on a real robot
and is not what this lesson is asking for. Rubric check 3 watches the whole run and
fails if the shooter spins while the drive step is still commanding the motors.

`deadline` is the one people forget. "Run the intake while driving this path" is
`deadline(followPath, runIntake)` — the path decides when you are done, the intake
just goes along.

### Bound everything

`.withTimeout(8.0)` on the whole routine.

Autonomous is fifteen seconds. A routine with no bound that stalls — a beam-break
that never triggers, a drive step that hits a wall — is *still running* when teleop
starts. It still holds the drivetrain. Your driver pushes the stick and nothing
happens, because a command from twenty seconds ago has not let go.

This is a genuinely common way to lose a match, and it is entirely preventable by a
habit: **every auto step gets a timeout, and so does the routine.**

### Where the requirement system saves you

`driveDistanceCommand` requires the drivetrain. So does the teleop default command.
The scheduler will never run both — when auto's command is scheduled, the default is
interrupted; when auto finishes, the default resumes automatically.

You get that for free, and only because every command declares what it needs. This
is what all the `run(...)` and `runOnce(...)` calls on `SubsystemBase` have been
buying you.

## Run it

```bash
./tools/frcprog check 12-auto-basic
```

Five checks:

1. The routine is built.
2. The robot drives at least 1.8 m forward within three seconds.
3. The scoring step runs after the drive, not alongside it.
4. The routine finishes on its own inside eight seconds.
5. Everything is left stopped when it ends.

Check 5 is about `finallyDo`, which runs however a command ends — including when a
timeout cancels it. Cleanup that only happens on the happy path is not cleanup.

## See it

```bash
./tools/frcprog sim
```

In the simulator, click **Autonomous** instead of Teleoperated. Do not touch
anything.

In AdvantageScope, plot the drive distance and `Flywheels/TargetRPM` on one chart.
Distance climbs, flattens; *then* RPM jumps. Two traces, and the handoff between
them is your routine's structure drawn out in time.

## Done?

```bash
./tools/frcprog next
```

## Real-world note

Autos in serious FRC are shorter than people expect. A reliable two-piece that works
every match beats a four-piece that works one match in three — a failed auto often
costs more than it could have gained, because a robot stuck against a wall is not
where your driver needed it.

Build the simple one. Make it work every time. Then extend it.
