# Lesson 21 — Swerve drivetrain (intro)

> **Stage 2B · ~75 minutes · Prerequisite: 20-superstructure**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Tank drive cannot strafe. To pick up a game piece two metres to your left, you
must rotate, drive, rotate back — three moves where a holonomic drivetrain makes one.
Every competitive FRC team runs swerve, and this is why.

## What you'll learn

1. The four-module model: each corner has a drive motor and a steering motor.
2. `SwerveDriveKinematics` — chassis speeds in, four module states out.
3. Field-relative control: "forward" means forward *on the field*, not on the robot.
4. Building a `ModuleIO` layer, four times over.

## What you'll do

Build `subsystems/swerve/` alongside your existing tank drive — do not delete
`Drive.java`, because every earlier rubric still depends on it.

```
swerve/
├── ModuleIO.java          one module's sensors and commands
├── ModuleIOSim.java       two DCMotorSims: one drive, one steer
├── Module.java            closed-loop control of one module
├── GyroIO.java / GyroIOSim.java
└── SwerveSubsystem.java   kinematics, odometry, the drive command
```

The core of it:

```java
ChassisSpeeds speeds =
    ChassisSpeeds.fromFieldRelativeSpeeds(xMetersPerSec, yMetersPerSec, omegaRadPerSec, gyroAngle);
SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
SwerveDriveKinematics.desaturateWheelSpeeds(states, MAX_SPEED);
for (int i = 0; i < 4; i++) modules[i].setState(states[i]);
```

Four lines, and each is worth understanding.

**`fromFieldRelativeSpeeds`** rotates the driver's intent into the robot's frame
using the gyro. Push the stick away from you and the robot moves away from you,
regardless of which way it happens to be facing. That is the whole reason drivers
love swerve, and it is one function call.

**`toSwerveModuleStates`** turns "move this way while rotating this fast" into four
(speed, angle) pairs. Pure geometry, given the module positions.

**`desaturateWheelSpeeds`** handles the case where the maths asks for more than the
motors can deliver — it scales all four down together, preserving the *direction* of
travel. Skip it and a saturated robot drives somewhere other than where it was told.

**Module optimisation** is the other classic: a module asked to point at 179° when it
is currently at −179° should turn 2°, not 358°. `SwerveModuleState.optimize` handles
it by allowing the drive motor to run backwards instead.

## See it

AdvantageScope has a **Swerve** tab that draws four arrows, one per module. Publish
your `SwerveModuleState[]` and drop it on. Wrong-direction modules are instantly
obvious as arrows pointing somewhere they should not.

## Done?

The robot translates in field-relative directions regardless of heading, rotates in
place, and the module arrows point sensibly.

```bash
./tools/frcprog next
```
