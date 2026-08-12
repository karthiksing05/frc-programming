# Lesson 18 — Logging discipline

> **Stage 2A · ~35 minutes · Prerequisite: 17**


!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.


Lesson 10 told you to publish values. You have been doing it ad hoc ever since, and
by now the key names probably do not agree with each other.

That is fine at ten signals. At two hundred — which is a normal competition robot —
an inconsistent naming scheme turns AdvantageScope's sidebar into an alphabetical
soup at exactly the moment you are trying to find something with four minutes left in
the pit.

## What you'll learn

1. Apply one key-naming convention everywhere.
2. Separate *inputs* (what the robot sensed) from *outputs* (what it decided).
3. Log structured types — `Pose2d`, not three doubles.
4. Decide what is worth logging, and what is noise.

## What you'll do

Audit every subsystem. For each one:

**Name keys `SubsystemName/FieldName`, PascalCase.** `Drive/LeftVolts`, not
`drive_left_volts` or `leftDriveVoltage`. AdvantageScope builds its tree from the
slashes, so the convention is what gives you a browsable list grouped by mechanism.

**Distinguish inputs from outputs.** Inputs are measurements: encoder positions,
sensor states, battery voltage. Outputs are decisions: setpoints, commanded volts,
state enums. Keeping them apart matters because inputs are what you would replay and
outputs are what you would compare against.

Convention: `Drive/Inputs/LeftPositionMeters` and `Drive/Outputs/LeftVolts`.

**Log structured types where they exist.**

```java
// three separate signals you have to mentally recombine
xPub.set(pose.getX());
yPub.set(pose.getY());
thetaPub.set(pose.getRotation().getDegrees());

// one signal AdvantageScope can drop straight onto a field view
StructPublisher<Pose2d> posePub =
    table.getStructTopic("Pose", Pose2d.struct).publish();
posePub.set(getPose());
```

WPILib has struct serializers for `Pose2d`, `Pose3d`, `SwerveModuleState`,
`ChassisSpeeds` and more. Using them is the difference between a plot of three
numbers and a robot drawn on a field.

**Log every setpoint next to its measurement.** The single most useful pair of
signals in FRC is "what I asked for" and "what happened". Neither is much use alone.

## Run it

No rubric. The check is practical: open AdvantageScope, look at the sidebar, and ask
whether a teammate could find the elevator's height in under five seconds without
asking you.

## See it

```bash
./tools/frcprog sim
./tools/frcprog scope
```

A well-named tree looks like this:

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

## Done?

Every subsystem publishes to a consistent scheme, and every setpoint sits next to
its measurement.

```bash
./tools/frcprog next
```

## What not to log

Logging is not free — bandwidth, disk, and a little loop time each.

Worth logging: anything you would want during a post-match argument. Setpoints,
measurements, commanded outputs, state enums, whether a mechanism thinks it has
arrived.

Not worth logging: values you can derive from other logged values, constants that
never change, and anything you added "just in case" and have never once looked at.
The second category is what turns a useful log into an unusable one.
