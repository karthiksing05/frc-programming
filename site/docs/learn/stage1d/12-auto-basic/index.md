# Lesson 12 — Auto routines (basic) <small>· Stage 1D</small>

<span class="stage-badge">Stage 1D · Lesson 12</span>

*Fifteen seconds of autonomous decide how a match starts. You can't drive them — so you compose them.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1D |
    | **Time** | ~40 min |
    | **Prereqs** | [Lesson 11 — Default commands done right](../11-default-commands/) |
    | **Edits** | `src/main/java/frc/robot/autos/SimpleAuto.java`, `RobotContainer.java` |
    | **Tests** | `frc.robot.autos.SimpleAutoTest` (`@Tag("lesson-12")`) |
    | **Reference robot** | Presto · [`auto/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/commands/auto) package |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Wire `RobotContainer.getAutonomousCommand()` to return a composed `Command`.
2. Build a two-step autonomous routine with `Commands.sequence(...)` and `Commands.parallel(...)`.
3. Use `Commands.waitSeconds(t)` correctly — and explain why `Thread.sleep` is forbidden.
4. Bound every auto step with `.withTimeout(t)` for safety.
5. Offer the driver multiple autos through a `SendableChooser`.

---

## The real-world problem

A FRC match opens with a 15-second autonomous period. The driver is standing behind a clear barrier, hands off the joystick by rule. Whatever your robot does in those 15 seconds is exactly what your code says — no human in the loop, no mid-run corrections. This is the period that wins or loses tournament rank points.

The naive instinct is to write a method that calls `drive.setVoltage(...)`, then `Thread.sleep(2000)`, then `flywheels.spinUp()`, then more sleeps. **This will destroy your robot** — or, more precisely, it will freeze the entire `CommandScheduler` for two seconds, during which no telemetry runs, no safety bounds fire, and no command can interrupt the sleeping thread. The robot becomes uninspectable for the duration. Then the sleep ends and everything snaps back to life, often at the wrong time. This is the canonical FRC autonomous bug, and you're going to learn how to never write it.

The right tool is composition. An auto is just a longer `Command` built from smaller commands using the same primitives you already know — `andThen`, `alongWith`, `withTimeout` — plus one new helper for the "wait" case: `Commands.waitSeconds(t)`.

---

## What you'll do

Create `frc/robot/autos/SimpleAuto.java`. Inside, write a factory:

```java
public static Command driveAndScore(Drive drive, Flywheels flywheels, Indexer indexer) {
    return Commands.sequence(
        drive.driveDistanceCommand(2.0).withTimeout(3.0),
        flywheels.spinUpCommand().withTimeout(1.5),
        Commands.waitSeconds(0.5),
        indexer.feedCommand().withTimeout(0.4)
    );
}
```

Then in `RobotContainer`, build a `SendableChooser<Command>`, register two autos (`driveAndScore` and a `doNothing`), and return the chooser's selection from `getAutonomousCommand()`.

---

## `Commands.sequence` vs. chained `andThen`

Both produce the same kind of sequential command, but they read differently:

```java
// Chained — fine for two steps, hard to read past three
drive.driveDistanceCommand(2.0)
     .andThen(flywheels.spinUpCommand())
     .andThen(indexer.feedCommand());

// Sequence — flat, indents the same way as a list
Commands.sequence(
    drive.driveDistanceCommand(2.0),
    flywheels.spinUpCommand(),
    indexer.feedCommand()
);
```

Pick `Commands.sequence` for autos. Autos grow. The flat-list form scales; the `.andThen` chain becomes a staircase by step five.

The sibling helpers worth memorizing for auto code:

| Helper | What it does |
|---|---|
| `Commands.sequence(a, b, c)` | Run in order. Each completes before the next starts. |
| `Commands.parallel(a, b)` | Run together. Composition ends when **all** complete. |
| `Commands.race(a, b)` | Run together. Composition ends when **any** completes; siblings cancel. |
| `Commands.deadline(deadlineCmd, others...)` | Run together. Composition ends when `deadlineCmd` finishes; others cancel. |
| `Commands.waitSeconds(t)` | Park the scheduler timeline at this command for `t` seconds. Yields. |
| `Commands.waitUntil(supplier)` | Block until a boolean condition is true. Yields. |
| `cmd.withTimeout(t)` | End `cmd` after `t` seconds, even if it's not finished. |
| `cmd.until(supplier)` | End `cmd` when the supplier turns true. |

---

## `waitSeconds` is not `Thread.sleep`

This is the one rule worth tattooing.

```java
// ✗ NEVER. Freezes the entire scheduler. No telemetry, no safety, no interrupts.
Thread.sleep(2000);

// ✗ Also forbidden. Same problem, dressed in WPILib clothing.
Timer.delay(2.0);

// ✓ The correct primitive. Yields back to the scheduler every tick.
Commands.waitSeconds(2.0);
```

The difference: `Commands.waitSeconds` schedules a no-op command that returns `false` from `isFinished()` until 2 seconds of robot time have elapsed. Every other command keeps running. The drivetrain still updates odometry; the flywheel PID still ticks; AdvantageKit still logs. `Thread.sleep` stops the world.

!!! danger "If you see `Thread.sleep` in robot code, treat it as a fire alarm."

    No legitimate command-based code uses `Thread.sleep` or `Timer.delay`. If a teammate's PR contains either, the review comment writes itself.

---

## Safety nets: `.withTimeout` on every auto step

A real auto routine has timeouts on every step that could plausibly hang. `drive.driveDistanceCommand(2.0)` is supposed to take two seconds — but what if the encoder is unplugged and the command waits forever? Wrap it:

```java
drive.driveDistanceCommand(2.0).withTimeout(3.0)
```

If the drive finishes naturally inside 3 s, great. If something is wrong, the timeout fires at 3 s and the next step runs anyway, instead of the auto sitting frozen for the rest of the period.

A rule that scales: **every auto step gets a timeout slightly longer than the expected duration.** "Slightly" is judgment, but 1.5× the expected time is a safe default.

---

## SendableChooser

Drivers want options. The chooser puts a dropdown on the dashboard:

```java
private final SendableChooser<Command> autoChooser = new SendableChooser<>();

public RobotContainer() {
    autoChooser.setDefaultOption("Do nothing", Commands.none());
    autoChooser.addOption("Drive + Score",
        SimpleAuto.driveAndScore(drive, flywheels, indexer));
    SmartDashboard.putData("Auto", autoChooser);
}

public Command getAutonomousCommand() {
    return autoChooser.getSelected();
}
```

The chooser is registered in `RobotContainer`'s constructor. The dashboard reads it through NetworkTables and renders a dropdown.

!!! tip "Always have a 'Do nothing' option"

    Drivers occasionally need a safe default — broken sensor, scouting decision, hardware oddity. `Commands.none()` does exactly that: returns instantly and does nothing. Set it as the chooser default so a forgotten dropdown doesn't fire a real routine.

---

## Rubric

`SimpleAutoTest` asserts:

1. In auto mode, the robot drives forward and reaches ≥1.8 m within 3 s.
2. After the drive step, `Flywheels/AppliedVolts` rises above 6 V.
3. The indexer command runs after the flywheels have spun up.
4. Total auto duration ≤ 8 s.
5. `SendableChooser` is published to NetworkTables under key `Auto`.

Run locally:

```bash
./gradlew test --tests '*SimpleAutoTest' -DincludeTags='lesson-12'
```

---

## See it run

```bash
./gradlew simulateJava
```

In SimGUI, switch the operating mode from **Teleoperated** to **Autonomous**. In AdvantageScope, plot `Drive/Inputs/leftPositionMeters` and `Flywheels/Inputs/velocityRPM` on the same timeline. You should see position climb first, then the RPM curve kick up, then the indexer pulse — all without touching the joystick. That's the picture of a working auto.

---

## Going further

- Add a second auto routine. Bind it as a new chooser option. Run both and confirm the dropdown switches between them.
- Use `Commands.parallel(...)` to start spinning up the flywheel **while** the drive moves. Compare total time against the sequential version.
- Browse Presto's `commands/auto/` package — note how every step has a timeout and how the most complex routines are still flat `Commands.sequence` calls underneath.

---

??? tip "Full reveal — only open if you're really stuck"

    A complete `SimpleAuto.driveAndScore`:

    ```java
    public static Command driveAndScore(Drive drive, Flywheels fly, Indexer idx) {
        return Commands.sequence(
            drive.driveDistanceCommand(2.0).withTimeout(3.0),
            Commands.parallel(
                fly.spinUpCommand(),
                Commands.waitSeconds(0.5)
            ).withTimeout(1.5),
            idx.feedCommand().withTimeout(0.4)
        ).withName("DriveAndScore");
    }
    ```

    `.withName("...")` is optional but makes the command identifiable in the scheduler's NT output. Useful when you're staring at AdvantageScope wondering why something didn't run.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 11**
    Default commands done right

    [:octicons-arrow-left-24: Back to lesson 11](../11-default-commands/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 13**
    Path-following intro

    [:octicons-arrow-right-24: Continue to lesson 13](../13-path-following/)

</div>
