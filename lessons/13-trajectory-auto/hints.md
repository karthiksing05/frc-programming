# Hints — Lesson 13

## Hint 1 — Where to start

Everything is inside the `drive.run(() -> { ... })` lambda in `follow`. The pieces —
`trajectory`, `controller`, `timer`, `KINEMATICS` — already exist; you are wiring
them together.

Work in the order the data flows: time → desired pose → chassis speeds → wheel
speeds → volts.

## Hint 2 — The shape of the answer

```java
Trajectory.State goal = trajectory.sample( /* how far into the path are we? */ );
ChassisSpeeds speeds = controller.calculate( /* where we are */, /* where we should be */);
DifferentialDriveWheelSpeeds wheels = KINEMATICS.toWheelSpeeds(speeds);
drive.setVoltage( /* left m/s -> volts */, /* right m/s -> volts */);
```

- Elapsed time is `timer.get()`. The timer is restarted for you in the `runOnce`
  step just above.
- Where we are is `drive.getPose()`.
- Metres per second becomes volts by multiplying by `Constants.Drive.kV_LINEAR`.

The `ChassisSpeeds` and `DifferentialDriveWheelSpeeds` imports are already at the top
of the file.

## Hint 3 — Almost there

**Robot drives but ends up wrong:** you are probably sampling the trajectory at a
fixed time rather than at `timer.get()`, so it drives toward one fixed pose the
whole way. Check 4 is designed to catch exactly this.

**Robot barely moves:** the wheel speeds are probably being sent as speeds rather
than as volts. `setVoltage` expects volts; 2 m/s is 2 volts' worth of *request*, not
2 volts.

**Robot spins:** left and right swapped. `wheels.leftMetersPerSecond` goes to the
first argument of `setVoltage`.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
drive.run(
    () -> {
        // Where the path says we should be, right now.
        Trajectory.State goal = trajectory.sample(timer.get());

        // How to get from where we are to there, as chassis speeds.
        ChassisSpeeds speeds = controller.calculate(drive.getPose(), goal);

        // Chassis speeds -> per-side wheel speeds -> volts.
        DifferentialDriveWheelSpeeds wheels = KINEMATICS.toWheelSpeeds(speeds);
        drive.setVoltage(
            wheels.leftMetersPerSecond * Constants.Drive.kV_LINEAR,
            wheels.rightMetersPerSecond * Constants.Drive.kV_LINEAR);
    })
```

and in `RobotContainer.configureAutos()`:

```java
autoChooser.addOption("S-Curve", TrajectoryAuto.sCurveAuto(drive));
```

**Worth reading:** the `runOnce` step just above your lambda.

```java
drive.runOnce(
    () -> {
        drive.resetPose(trajectory.getInitialPose());
        timer.restart();
    })
```

Telling odometry "we are, by definition, at the start of the path" is not optional.
Skip it and the controller spends the first second driving to wherever it believes
the path begins relative to the last reset — which, on a real field, is usually
somewhere embarrassing.

This is also why real robots reset their pose from vision at the start of auto: the
alternative is trusting that the robot was placed on the field exactly where the
path assumed.

</details>
