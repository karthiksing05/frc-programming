# Lesson 18 — Logging discipline

**Stage 2A · 35 min · Needs: 17**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Ten signals is fine. Two hundred with inconsistent names is alphabetical soup.

## Do this

Audit every subsystem. Four changes.

**1. One naming scheme:** `SubsystemName/FieldName`, PascalCase.

`Drive/LeftVolts`. Not `drive_left_volts`, not `leftDriveVoltage`. AdvantageScope
builds its tree from the slashes.

**2. Separate inputs from outputs.**

- Inputs are measurements: encoder positions, sensor states, battery voltage
- Outputs are decisions: setpoints, commanded volts, state enums

Use `Drive/Inputs/LeftPositionMeters` and `Drive/Outputs/LeftVolts`. It matters
because inputs are what you would replay and outputs are what you compare against.

**3. Log structured types.**

```java
// three signals you have to recombine in your head
xPub.set(pose.getX());
yPub.set(pose.getY());
thetaPub.set(pose.getRotation().getDegrees());

// one signal you can drop straight onto a field view
StructPublisher<Pose2d> posePub =
    table.getStructTopic("Pose", Pose2d.struct).publish();
posePub.set(getPose());
```

WPILib has struct serializers for `Pose2d`, `Pose3d`, `SwerveModuleState`,
`ChassisSpeeds` and more.

**4. Log every setpoint next to its measurement.** "What I asked for" and "what
happened" is the most useful pair in FRC. Neither is much use alone.

## What good looks like

```
NT
├── Drive
│   ├── Inputs/  LeftPositionMeters, RightPositionMeters, GyroYawRadians
│   ├── Outputs/ LeftVolts, RightVolts
│   └── Pose
├── Elevator
│   ├── Inputs/  HeightMeters
│   └── Outputs/ GoalMeters, AppliedVolts, AtGoal
└── Flywheels
    ├── Inputs/  ActualRPM
    └── Outputs/ TargetRPM, ErrorRPM
```

## What not to log

Logging costs bandwidth, disk and a little loop time.

**Worth it:** anything you would want during a post-match argument.
**Not worth it:** values derivable from other logged values, constants, and
anything you added "just in case" and have never looked at. The second category is
what turns a useful log into an unusable one.

## Done

Every subsystem uses one scheme, and every setpoint sits beside its measurement.

The test: could a teammate find the elevator's height in five seconds without
asking you?

```bash
./tools/frcprog next
```
