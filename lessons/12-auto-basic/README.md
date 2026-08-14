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

Registering the routine with the chooser is what makes it selectable from the
dashboard, and a routine that nothing can select will never run no matter how
correct it is.

## Check it

```bash
./tools/frcprog check 12-auto-basic
```

Five checks: drives 1.8 m in three seconds, scores after driving rather than during,
finishes inside eight seconds, and leaves everything stopped.

## How it works

### An auto is just a command

There is no autonomous framework. `autonomousInit()` schedules a command;
`autonomousPeriodic()` does nothing at all. The scheduler runs it exactly as it runs
a button-bound command.

That is the payoff for lessons 07 to 11. Build teleop out of small, composable,
requirement-aware commands and your auto is an afternoon of gluing them together.
Build it out of `if` statements in `teleopPeriodic` and you start from nothing.

### sequence, parallel, deadline

| Composition | Starts | Finishes |
|---|---|---|
| `sequence(a, b)` | a, then b | b done |
| `parallel(a, b)` | both | **both** done |
| `deadline(a, b)` | both | **a** done |

`sequence` here, because the lesson is about knowing which one you asked for.

`deadline` is the one people forget and the one competitive autos live on. "Run the
intake while driving this path" is `deadline(followPath, runIntake)`: the path
decides when you are done, the intake just goes along and gets cancelled when the
path ends.

??? question "Predict: what would parallel do here, and why is it not wrong exactly?"

    The shooter would spin up **while** the robot is still driving.

    On a real robot that is often what you want, because spin-up takes half a second
    and you may as well use the driving time for it. Serious autos overlap
    aggressively.

    It is not wrong. It is just not what this lesson asked for, and check 3 grades
    what you asked for. The point is that `sequence` and `parallel` are a deliberate
    choice with a real trade-off, not interchangeable words.

### The requirement system saves you again

`driveDistanceCommand` requires the drivetrain. So does the teleop default command
from lesson 07.

The scheduler will never run both. When auto's command is scheduled, the default is
interrupted. When auto finishes, the default resumes automatically.

You get that for free, and only because every command declares what it needs.

### Bound everything

`.withTimeout(8.0)` on the whole routine.

Autonomous is fifteen seconds. A routine that stalls, waiting for a beam-break that
never triggers, is **still running** when teleop starts and **still holding the
drivetrain**. Your driver pushes the stick and nothing happens, because a command
from twenty seconds ago has not let go.

This is a common way to lose a match and it is entirely preventable by habit.

??? info "Read driveDistanceCommand, it has three things worth stealing"

    ```java
    final double[] startMeters = new double[1];
    return runOnce(() -> startMeters[0] = getAverageDistanceMeters())
        .andThen(run(() -> setVoltage(volts, volts)))
        .until(() -> Math.abs(getAverageDistanceMeters() - startMeters[0]) >= Math.abs(meters))
        .finallyDo(() -> setVoltage(0.0, 0.0));
    ```

    **The one-element array.** A lambda may only capture effectively-final locals,
    but it may mutate the contents of a captured object. This is the standard Java
    workaround for "a variable the lambda writes to".

    **Measuring from a captured start** rather than resetting the encoder. Resetting
    hardware to make your arithmetic easier eventually collides with something else
    that was relying on that reading.

    **`finallyDo`.** Runs on every exit path including cancellation. Without it, a
    timeout would leave the drivetrain at six volts.

## See it

Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

1. In the sim, find the **Auto Routine** chooser. It appears in the
   **NetworkTables** panel under `SmartDashboard`, or on a dashboard if you have one
   open. Select **Drive and Score**.
2. Click **Autonomous** in Robot State. Do not touch anything.
3. In AdvantageScope plot the drive distance and `Flywheels/TargetRPM` on one graph.

You should see distance climb, flatten out around 2 m, and only **then** RPM jump.
Two traces, and the handoff between them is your `sequence` drawn in time.

If RPM rises while distance is still climbing, you used `parallel`.

??? example "Experiment: watch a routine overrun"

    1. Remove the `.withTimeout(8.0)`
    2. In `Constants.Roller`, temporarily set `BEAM_BREAK_DIO` handling so the piece
       never appears, or simply leave DIO 4 high the whole run
    3. Run Autonomous, wait, then click **Teleoperated**
    4. Try to drive. You cannot: the auto command still holds the drivetrain.

    Put the timeout back and repeat. Teleop takes over cleanly.

    That is the failure the timeout exists for, and it is much better to meet it
    here.

## Done

The rubric passes, and the robot does something useful in the fifteen seconds
before the drivers are allowed to touch it.

```bash
./tools/frcprog next
```

**Real-world note.** A reliable two-piece beats a four-piece that works one match in
three. A failed auto often costs more than it could have gained, because a robot
stuck against a wall is not where your driver needed it to start.
