# Lesson 22 — Odometry & pose estimation <small>· Stage 2B</small>

<span class="stage-badge">Stage 2B · Lesson 22</span>

*Pure odometry drifts. After two minutes of swerving and bumping, your robot's idea of where it is and where it actually is have parted ways — and your vision-based scoring breaks.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2B |
    | **Time** | ~55 min |
    | **Prereqs** | [Lesson 21 — Swerve drivetrain (intro)](../21-swerve-intro/) |
    | **Edits** | Replace `SwerveDriveOdometry` with `SwerveDrivePoseEstimator` in `swerve/SwerveSubsystem.java` |
    | **Tests** | `frc.robot.subsystems.swerve.PoseEstimationTest` (`@Tag("lesson-22")`) |
    | **Reference robot** | Kelpie · pose-estimator wiring in [`SwerveSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/SwerveSubsystem.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Track robot pose from module positions + gyro using `SwerveDriveOdometry`.
2. Swap the odometry tracker for a `SwerveDrivePoseEstimator` without changing the public subsystem API.
3. Configure state and vision standard-deviation parameters — without having to read a Kalman-filter textbook.
4. Reset pose at auto start and expose `Drive/Pose` as a `Pose2d` for AdvantageScope's 3D field.

---

## The real-world problem

Lesson 21 gave you a swerve that moves. It does *not* know where it is. The module encoders sum displacement (good); the gyro tracks heading (good); but every wheel slip, every cube hit, every centimeter of carpet stretch shows up as a drift between the integrated estimate and the truth on the field. Over 15 seconds of auto that drift is tolerable. Over a 2:15 match — especially one with defense — it compounds into 30+ centimeters.

`SwerveDriveOdometry` is the dead-reckoning baseline: take where you were, take the module deltas this cycle, plus the gyro delta, fold them in, and you have a new pose. There's no correction mechanism. Once it drifts, it stays drifted.

`SwerveDrivePoseEstimator` is the same dead-reckoning engine wrapped in a Kalman filter that *also* accepts external pose measurements — from AprilTag vision, from a known scoring location, from a manual reset at autonomous start. The vision update arrives, the filter weighs it against the integrated estimate using configured standard deviations, and the pose snaps back toward truth. Lesson 24 will plug in PhotonVision; today we put the seam in place so vision drops in cleanly.

---

## What you'll do

Swap `SwerveDriveOdometry` for `SwerveDrivePoseEstimator`. Add a `resetPose(Pose2d)` factory method so autos can pin the robot at a known starting position. Expose the estimated pose via `@AutoLogOutput` so AdvantageScope's 3D field view shows it live. Crucially: the public subsystem API doesn't change. Lesson 24's vision subsystem will call `addVisionMeasurement(...)`, but you don't write that yet.

This is a refactor lesson, not a new-mechanism lesson — a clean prerequisite for Stage 2C.

---

## Odometry first, then estimator

It's worth standing odometry up briefly so you feel what it does. Inside `periodic()`:

```java
SwerveModulePosition[] positions = new SwerveModulePosition[] {
    modules[0].getPosition(),
    modules[1].getPosition(),
    modules[2].getPosition(),
    modules[3].getPosition(),
};
odometry.update(gyroInputs.yaw, positions);
```

A `SwerveModulePosition` is a `(distance, angle)` pair — the cumulative drive distance and the current steer angle. The gyro and the four positions are everything the kinematics needs to integrate one step forward.

Run it in sim, drive a square, return to the start. The pose probably returns within a few centimeters. Now drive a square *while bumping the joystick into a wall on every side* — and watch the pose wander. That's the pain.

---

## The swap

`SwerveDrivePoseEstimator` is a drop-in for `SwerveDriveOdometry` with two extra constructor arguments:

```java linenums="1"
this.poseEstimator = new SwerveDrivePoseEstimator(
    kinematics,
    gyroInputs.yaw,           // initial gyro angle
    positions,                // initial module positions
    new Pose2d(),             // initial pose
    VecBuilder.fill(0.1, 0.1, 0.05),   // state std-devs (m, m, rad)
    VecBuilder.fill(0.9, 0.9, 0.9));   // vision std-devs (m, m, rad)
```

The two `VecBuilder.fill(...)` calls are the only conceptual new thing. The first describes how much you trust the wheel-and-gyro integration. The second describes how much you trust vision (when it eventually arrives). Smaller numbers = more trust. Kelpie's [`SwerveSubsystem`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/SwerveSubsystem.java) uses values in this ballpark; you can copy and tune later.

!!! note "Don't read the Kalman paper today"

    The intuition is enough. State stddevs say "how noisy is my wheel-based estimate?" Vision stddevs say "how noisy is my camera-based estimate?" The filter math weights them. WPILib provides the math; you provide two reasonable triples and revisit them after you have vision data. [The pose estimator docs](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/state-space/state-space-pose-estimators.html) cover the rest if you want it.

The per-cycle update is almost identical:

```java
poseEstimator.update(gyroInputs.yaw, positions);
```

That's the entire conceptual swap. The public-method shape `getPose()` returns `poseEstimator.getEstimatedPosition()` instead of `odometry.getPoseMeters()`.

---

## Reset pose at auto start

Every auto routine begins with a known robot pose — the one you positioned the robot at on the field. The pose estimator needs to be told what that pose is, or autos start with an arbitrary `(0, 0)` and the first path-following command immediately wants to drive across the field.

Expose a factory:

```java
public Command resetPose(Supplier<Pose2d> pose) {
  return runOnce(() -> poseEstimator.resetPosition(
      gyroInputs.yaw,
      currentModulePositions(),
      pose.get()));
}
```

!!! warning "`Supplier<Pose2d>`, not `Pose2d`"

    A captured `Pose2d` is the same footgun you saw in Lesson 07 with captured joystick values. The pose your auto wants depends on which alliance you're on, which starting position the dashboard selected, and the field's known offset — none of which are known when bindings get wired in `RobotContainer`. Always defer evaluation with a supplier.

---

## Log the pose

`@AutoLogOutput` makes the estimated pose visible in AdvantageScope without writing a line of `Logger.recordOutput`:

```java
@AutoLogOutput(key = "Swerve/Pose")
public Pose2d getPose() {
  return poseEstimator.getEstimatedPosition();
}
```

Now `RealOutputs/Swerve/Pose` shows up as a `Pose2d`-typed value. Drop it onto AdvantageScope's **3D Field** tab and your robot renders at the estimated pose. This is the foundation Lesson 23 (trajectory following) and Lesson 24 (vision) both build on — both produce or consume `Pose2d` against this one source of truth.

---

## Rubric

`PoseEstimationTest` asserts:

1. Driving a 2 m forward + 2 m back path returns the pose within 5 cm of the start.
2. `resetPose(new Pose2d(3, 4, Rotation2d.fromDegrees(45)))` actually sets the pose to that value.
3. `Swerve/Pose` shows up in AdvantageScope as a `Pose2d` (verified by reading the NT entry back as one).
4. The estimator accepts a synthetic `addVisionMeasurement` call without throwing — the seam is wired even though Lesson 22 doesn't exercise it.

Run locally:

```bash
./gradlew test --tests '*PoseEstimationTest' -DincludeTags='lesson-22'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope's **3D Field** tab. Drag `RealOutputs/Swerve/Pose` onto the field. Drive the robot — the rendered pose tracks the simulated robot. Reset pose with the bound button (your `RobotContainer` should have a debug button like `driver.start().onTrue(swerve.resetPose(() -> new Pose2d()))`) and watch the rendered model snap back to origin.

---

## Going further

- Read Kelpie's [`SwerveSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/SwerveSubsystem.java) — note that they sample odometry on a high-frequency thread (250 Hz) rather than at the 50 Hz `periodic()` rate. What problem does that solve? (Hint: aliasing under hard rotation.)
- Add a synthetic "vision" input that publishes the ground-truth pose every 100 ms with a 10 cm noise term. Compare the estimator's output to truth — feel the correction.
- Read [the WPILib pose estimator math overview](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/state-space/state-space-pose-estimators.html). Skim, don't memorize.

---

??? tip "Full reveal — only open if you're really stuck"

    Minimal pose estimator wire-in:

    ```java
    public class SwerveSubsystem extends SubsystemBase {
      private final SwerveDrivePoseEstimator poseEstimator;

      public SwerveSubsystem(/* ... */) {
        this.poseEstimator = new SwerveDrivePoseEstimator(
            kinematics, gyroInputs.yaw, currentModulePositions(), new Pose2d());
      }

      @Override
      public void periodic() {
        // ... process inputs ...
        poseEstimator.update(gyroInputs.yaw, currentModulePositions());
      }

      @AutoLogOutput(key = "Swerve/Pose")
      public Pose2d getPose() { return poseEstimator.getEstimatedPosition(); }
    }
    ```

    The two-arg constructor uses default stddevs; you can pass explicit `VecBuilder.fill(...)` once you're comfortable.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 21**
    Swerve drivetrain (intro)

    [:octicons-arrow-left-24: Back to lesson 21](../21-swerve-intro/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 23**
    Trajectory following

    [:octicons-arrow-right-24: Continue to lesson 23](../23-trajectories/)

</div>
