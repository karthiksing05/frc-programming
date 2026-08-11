# Lesson 21 — Swerve drivetrain (intro) <small>· Stage 2B</small>

<span class="stage-badge">Stage 2B · Lesson 21</span>

*Tank drive can't strafe. To pick up a coral two meters to the side you rotate, drive, rotate back — and you've lost the match. Today we put real swerve under the robot.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2B |
    | **Time** | ~75 min |
    | **Prereqs** | [Lesson 20 — Subsystem composition at scale](../../stage2a/20-superstructure/) |
    | **Edits** | Refactor `src/main/java/frc/robot/subsystems/drive/` into `swerve/` with 4 modules + a gyro IO |
    | **Tests** | `frc.robot.subsystems.swerve.SwerveTest` (`@Tag("lesson-21")`) |
    | **Reference robot** | Kelpie · [`subsystems/swerve/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/swerve) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Articulate the four-module kinematic model — drive motor + turning motor per corner.
2. Convert chassis-frame `ChassisSpeeds` into per-module `SwerveModuleState[]` using `SwerveDriveKinematics`.
3. Write a `ModuleIO` interface with `ModuleIOSim` and `ModuleIOMapleSim` implementations.
4. Drive field-relative: joystick X/Y translate the robot in field coordinates regardless of heading.

---

## The real-world problem

Lesson 07's tank drive served you well for fifteen lessons. But every Stage 1 anti-pattern you've defanged so far has been a code smell — this one is a *physics* smell. A tank chassis cannot move sideways. Period. Reefscape (and Crescendo, and Charged Up) all reward robots that translate freely while their heading does something else entirely — point at a goal, line up a feeder station, stay parallel to a wall while you slide along it.

Swerve adds a second motor at each corner. Now every wheel can both *spin* (for translation) and *steer* (point any direction). The cost is conceptual: instead of "left side at +0.4, right side at +0.4," you reason about a chassis-frame velocity vector and let kinematics math figure out what each module does. Kelpie's `swerve/` package is the destination. We're going to build a smaller version of it, end-to-end.

---

## What you'll do

Refactor `drive/` into `swerve/`. Build a `ModuleIO` interface with sim and (eventually) real implementations. Wire `SwerveDriveKinematics` and `ChassisSpeeds.fromFieldRelativeSpeeds(...)` so the joystick produces a holonomic motion in field frame. The default command consumes three `DoubleSupplier`s (x, y, omega) and produces four `SwerveModuleState`s every cycle.

This is the heavy lift of Stage 2B. Expect 75 minutes of focused work, not 30.

---

## Anatomy of a swerve module

A swerve *module* is one corner of the robot. It owns two motors (one for drive, one for steer) and reports four things up to the subsystem: drive position, drive velocity, steer angle, and absolute encoder angle. Kelpie's `ModuleIO` is the canonical shape; ours will be a trimmed version of it.

```java linenums="1"
public interface ModuleIO {
  @AutoLog
  class ModuleIOInputs {
    public double drivePositionMeters = 0.0;
    public double driveVelocityMetersPerSec = 0.0;
    public Rotation2d steerAngle = new Rotation2d();
    public Rotation2d steerAbsoluteAngle = new Rotation2d();
    public double driveAppliedVolts = 0.0;
    public double steerAppliedVolts = 0.0;
  }

  default void updateInputs(ModuleIOInputs inputs) {}
  default void setDriveVoltage(double volts) {}
  default void setSteerVoltage(double volts) {}
}
```

Mirror Kelpie's [`ModuleIO.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIO.java) for fidelity — they include current and temperature on the inputs struct, which you'll want once you wire real Krakens in Stage 2D.

!!! note "Why two angle fields?"

    `steerAngle` is the integrated motor encoder — fast, smooth, but drifts. `steerAbsoluteAngle` is a CANcoder (or Pigeon-mounted sensor) reading — slow, noisy, but absolute. At boot you seed the relative from the absolute, then trust the relative every cycle. Kelpie does this in `ModuleIOReal`; the sim doesn't care, but the inputs struct keeps the shape.

The two sim implementations live in [`ModuleIOSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIOSim.java) (vanilla WPILib `DCMotorSim`) and [`ModuleIOMapleSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIOMapleSim.java) (physics-accurate). Start with `ModuleIOSim` — it's enough to validate the kinematics math without the additional complexity.

---

## Kinematics: chassis speeds in, module states out

The whole abstraction the WPILib swerve API gives you is one line:

```java
SwerveModuleState[] states = kinematics.toSwerveModuleStates(chassisSpeeds);
```

You hand it a chassis-frame velocity (vx, vy, omega) and it returns four `SwerveModuleState`s, one per module. Each state has a speed (m/s) and an angle. Your job in the module is to (a) drive the motor at that speed, and (b) steer the wheel to that angle.

Construct `kinematics` once, in the subsystem constructor:

```java
this.kinematics = new SwerveDriveKinematics(
    new Translation2d( TRACK_X / 2,  TRACK_Y / 2),  // FL
    new Translation2d( TRACK_X / 2, -TRACK_Y / 2),  // FR
    new Translation2d(-TRACK_X / 2,  TRACK_Y / 2),  // BL
    new Translation2d(-TRACK_X / 2, -TRACK_Y / 2)); // BR
```

The order you list modules here is the order you must keep them in throughout — the array index *is* the module identity. Pick a convention (FL, FR, BL, BR is the WPILib doc default) and never deviate.

---

## Field-relative driving

`ChassisSpeeds.fromFieldRelativeSpeeds(vx, vy, omega, gyro.getRotation2d())` rotates your joystick input from field frame into chassis frame before kinematics runs. Without this, pushing forward on the stick after the robot has rotated 90° would send it sideways — driver-disorienting, never used in practice. Stage 1's tank drive didn't need this; swerve always does.

```java
public Command joystickDrive(DoubleSupplier x, DoubleSupplier y, DoubleSupplier omega) {
  return run(() -> {
    ChassisSpeeds field = new ChassisSpeeds(
        x.getAsDouble() * MAX_LINEAR_MPS,
        y.getAsDouble() * MAX_LINEAR_MPS,
        omega.getAsDouble() * MAX_ANGULAR_RAD_PER_SEC);
    ChassisSpeeds chassis = ChassisSpeeds.fromFieldRelativeSpeeds(field, gyroInputs.yaw);
    SwerveModuleState[] states = kinematics.toSwerveModuleStates(chassis);
    SwerveDriveKinematics.desaturateWheelSpeeds(states, MAX_LINEAR_MPS);
    for (int i = 0; i < 4; i++) modules[i].runSetpoint(states[i]);
  });
}
```

!!! warning "Always desaturate"

    `desaturateWheelSpeeds` scales the whole array down so no module is asked to spin faster than physically possible. Skip it and a hard-over rotation command at full translate will silently saturate the back-left motor and your robot will crab sideways. Kelpie calls it; Presto calls it; you call it.

---

## Gyro IO

The gyro is a fifth IO peer (`GyroIO`, `GyroIOPigeon2`, `GyroIOSim`). It publishes a single field — `yaw` as a `Rotation2d` — and the subsystem reads it the same way the modules' inputs get read. Keep this simple: one field, one `updateInputs` method, two implementations. Kelpie's [`GyroIO.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/GyroIO.java) is twelve lines for a reason.

---

## Rubric

`SwerveTest` asserts:

1. Joystick X/Y translates the robot in field-relative direction regardless of heading.
2. Joystick rotation spins the robot in place (translation stays at zero).
3. Module states publish to `Swerve/ModuleStates` as a `SwerveModuleState[]` for AdvantageScope.
4. Gyro yaw publishes correctly to `Swerve/GyroYaw`.

Run locally:

```bash
./gradlew test --tests '*SwerveTest' -DincludeTags='lesson-21'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope, connect to NT4 at `localhost`, and add the **Swerve States** tab. Point it at `RealOutputs/Swerve/ModuleStates` — you'll see four arrows representing each wheel's commanded velocity and direction. Push the stick forward; all four arrows should point forward. Push diagonally; arrows angle. Rotate the stick around the chassis (joystick rotation only); arrows fan out tangentially.

The 3D field view will show the robot translating holonomically — strafing without rotating, exactly what tank could never do.

---

## Going further

- Wire `ModuleIOMapleSim` instead of `ModuleIOSim`. Push another simulated robot in AdvantageScope and watch the physics. Kelpie's [`ModuleIOMapleSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIOMapleSim.java) shows the integration.
- Add closed-loop drive velocity control inside the module (Lesson 21 used open-loop voltage). Kelpie does this in `ModuleIOReal`.
- Read Kelpie's [`OdometryThreadIO.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/OdometryThreadIO.java) — what does the high-frequency odometry thread buy you over reading module positions in `periodic()`?

---

??? tip "Full reveal — only open if you're really stuck"

    The minimum SwerveSubsystem skeleton:

    ```java
    public class SwerveSubsystem extends SubsystemBase {
      private final ModuleIO[] modules;
      private final ModuleIOInputsAutoLogged[] moduleInputs;
      private final GyroIO gyro;
      private final GyroIOInputsAutoLogged gyroInputs = new GyroIOInputsAutoLogged();
      private final SwerveDriveKinematics kinematics;

      @Override
      public void periodic() {
        gyro.updateInputs(gyroInputs);
        Logger.processInputs("Swerve/Gyro", gyroInputs);
        for (int i = 0; i < 4; i++) {
          modules[i].updateInputs(moduleInputs[i]);
          Logger.processInputs("Swerve/Module" + i, moduleInputs[i]);
        }
      }
    }
    ```

    Try to derive the joystick command yourself before peeking at the field-relative block above.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 20**
    Subsystem composition at scale

    [:octicons-arrow-left-24: Back to lesson 20](../../stage2a/20-superstructure/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 22**
    Odometry & pose estimation

    [:octicons-arrow-right-24: Continue to lesson 22](../22-odometry/)

</div>
