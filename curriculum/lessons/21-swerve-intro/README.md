# Lesson 21 — Swerve

**Stage 2B · 75 min · Needs: 20**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Tank drive cannot strafe. Picking up something two metres left means rotate, drive,
rotate back.

## Do this

Build `subsystems/swerve/` alongside the existing tank drive. Do not delete
`Drive.java`; every earlier rubric still uses it.

```
swerve/
├── ModuleIO.java          one module's sensors and commands
├── ModuleIOSim.java       two DCMotorSims: drive and steer
├── Module.java            closed-loop control of one module
├── GyroIO.java / GyroIOSim.java
└── SwerveSubsystem.java   kinematics, odometry, drive command
```

**Do one module first.** Get `Module.java` closing the loop on angle and speed with
a `DCMotorSim` behind it. Verify it alone before building four.

The core, once modules work:

```java
ChassisSpeeds speeds =
    ChassisSpeeds.fromFieldRelativeSpeeds(xMps, yMps, omegaRadPerSec, gyroAngle);
SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds);
SwerveDriveKinematics.desaturateWheelSpeeds(states, MAX_SPEED);
for (int i = 0; i < 4; i++) modules[i].setState(states[i]);
```

## What each line does

**`fromFieldRelativeSpeeds`** rotates the driver's intent into the robot's frame
using the gyro. Push the stick away from you and the robot goes away from you,
whichever way it is facing. That is why drivers love swerve, and it is one call.

**`toSwerveModuleStates`** turns "move this way while rotating" into four
(speed, angle) pairs. Pure geometry.

**`desaturateWheelSpeeds`** handles asking for more than the motors can give. It
scales all four together, preserving direction. Skip it and a saturated robot
drives somewhere other than commanded.

**`SwerveModuleState.optimize`** stops a module asked to point at 179° from turning
358° when it is at −179°. It turns 2° and runs the drive motor backwards instead.

## Watch out for

**Wrong module order.** Kinematics returns states in the order you gave positions.
Mixing them up gives a robot that drives diagonally when told to go straight, which
looks like physics and is bookkeeping.

**Skipping optimize**, so modules take the long way and the robot lurches on every
direction change.

## See it

AdvantageScope has a **Swerve** tab that draws four arrows. Publish your
`SwerveModuleState[]` and drop it on. Wrong-direction modules are instantly obvious.

## Done

The robot translates field-relative regardless of heading, rotates in place, and
the arrows point sensibly.

```bash
./tools/frcprog next
```

Kelpie's [`swerve/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/swerve)
is a complete readable implementation to compare against.
