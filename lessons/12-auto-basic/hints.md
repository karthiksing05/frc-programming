# Hints — Lesson 12

## Hint 1 — Where to start

Two files, and the second one is easy to forget: `SimpleAuto.driveAndScore`, and
then `RobotContainer.configureAutos()` to actually offer it. A routine nothing
selects never runs.

## Hint 2 — The shape of the answer

`scoreOnce(flywheels, roller)` is already written for you at the bottom of
`SimpleAuto.java`. You are composing two existing commands, not writing new ones.

```java
return Commands.sequence(
        /* drive 2.0 m at 6 volts */,
        /* score once */)
    .withTimeout(8.0);
```

`drive.driveDistanceCommand(meters, volts)` is the first.

## Hint 3 — Almost there

If check 2 fails (not far enough), check the argument order:
`driveDistanceCommand(2.0, 6.0)` is two metres at six volts, not the other way
round. Two volts barely moves a robot.

If check 3 fails (shooter spinning during the drive), you used `Commands.parallel`
rather than `Commands.sequence`.

If check 4 fails (never finishes), the `.withTimeout(8.0)` is missing.

If check 1 passes in `SimpleAutoTest` but the robot does nothing in the simulator,
you wrote the routine and never added it to `autoChooser`.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

**SimpleAuto.driveAndScore**

```java
public static Command driveAndScore(Drive drive, Flywheels flywheels, RollerSubsystem roller) {
    return Commands.sequence(
            drive.driveDistanceCommand(2.0, 6.0), scoreOnce(flywheels, roller))
        .withTimeout(8.0)
        .withName("Drive and Score");
}
```

**RobotContainer.configureAutos**

```java
autoChooser.addOption("Drive and Score", SimpleAuto.driveAndScore(drive, flywheels, roller));
```

**Worth reading:** `driveDistanceCommand` in `Drive.java`, which you have been using
without looking at.

```java
final double[] startMeters = new double[1];
return runOnce(() -> startMeters[0] = getAverageDistanceMeters())
    .andThen(run(() -> setVoltage(volts, volts)))
    .until(() -> Math.abs(getAverageDistanceMeters() - startMeters[0]) >= Math.abs(meters))
    .finallyDo(() -> setVoltage(0.0, 0.0));
```

Three things worth stealing:

- **The one-element array.** A lambda may only capture effectively-final locals, but
  it may mutate the *contents* of a captured object. This is the standard Java
  workaround for "a variable the lambda writes to".
- **Measuring from a captured start**, rather than resetting the encoder. Resetting
  hardware to make your arithmetic easier is a habit that eventually collides with
  something else that was relying on that reading.
- **`finallyDo`.** Runs on every exit path, including cancellation. Without it, a
  timeout would leave the drivetrain at six volts.

</details>
