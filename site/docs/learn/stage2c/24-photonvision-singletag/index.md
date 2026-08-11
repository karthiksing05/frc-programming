# Lesson 24 — PhotonVision single-tag <small>· Stage 2C</small>

<span class="stage-badge">Stage 2C · Lesson 24</span>

*Your odometry insists you're square to the reef. The bumper sliding along the fence insists otherwise. Today we let the robot look up and see.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2C |
    | **Time** | ~45 min |
    | **Prereqs** | [Lesson 23 — Trajectory following](../../stage2b/23-trajectories/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/vision/{VisionIO,VisionIOInputsAutoLogged,VisionIOPhotonSim,VisionSubsystem}.java` |
    | **Tests** | `frc.robot.subsystems.vision.VisionTest` (`@Tag("lesson-24")`) |
    | **Reference robot** | Presto · [`apriltagvision/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/apriltagvision) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Install the PhotonVision vendordep and confirm the simulated coprocessor boots.
2. Define a `VisionIO` interface with `@AutoLog`ged target inputs.
3. Stand up `PhotonCameraSim` against the WPILib `AprilTagFieldLayout`.
4. Feed a **single-tag** pose update into the pose estimator you wrote in lesson 22.
5. Articulate why vision *corrects* odometry rather than replacing it.

---

## The real-world problem

Lesson 22 gave you an odometry-fused `Pose2d`. Lesson 23 trusted that pose enough to drive a Choreo path. Both are honest about what they know: gyro yaw plus integrated swerve module positions. Neither has any way to notice that you bumped a defender at second 4, slid two inches sideways, and are now driving the next path along an offset axis.

AprilTags are the fix. They are printed, surveyed, and bolted to known field locations. A camera that sees one resolves the robot's pose with bounded error — no integration, no drift. The deal is: vision *intermittently corrects* the always-on odometry estimate. Trust it too much and a partially-occluded tag yanks the robot two feet sideways. Trust it too little and the drift accumulates anyway.

This lesson is the simplest honest version: one camera, one tag, a measurement that flows through the same IO Layer + AdvantageKit machinery as every other sensor on the robot.

---

## What you'll do

Add the PhotonVision vendordep. Create a `vision/` package with the four-file IO pattern you've used since lesson 16. In sim, wire up a `PhotonCameraSim` against the official 2025 field layout. In `VisionSubsystem.periodic()`, when a tag is visible, push a `Pose2d` update with a timestamp into the pose estimator's `addVisionMeasurement(...)`. Run the lesson 23 path with vision on; watch the estimated pose snap toward ground truth each time the camera sees the tag.

!!! info "Vendordep install"

    From the WPILib palette: **WPILib: Manage Vendor Libraries** → **Install new libraries (online)** → paste the PhotonVision `photonlib.json` URL from [PhotonVision's install docs](https://docs.photonvision.org/en/latest/docs/programming/photonlib/adding-vendordep.html). Commit the generated `vendordeps/photonlib.json`.

---

## The IO interface

Presto's `AprilTagVisionIO.java` is your template. The inputs struct carries everything the subsystem layer needs without exposing PhotonVision types beyond this file:

```java linenums="1"
public interface VisionIO {
  @AutoLog
  class VisionIOInputs {
    public boolean connected = false;
    public double latestTimestampSeconds = 0.0;
    public int[] tagIds = new int[0];
    public Pose3d[] tagPosesOnField = new Pose3d[0];
    public Pose2d estimatedRobotPose = new Pose2d();
    public double ambiguity = 1.0; // 0 = perfect, 1 = unusable
  }

  default void updateInputs(VisionIOInputs inputs) {}
}
```

!!! tip "Why `ambiguity` is an input"

    Single-tag PnP has a mathematical two-solution ambiguity — the tag plane can flip. PhotonVision reports an ambiguity score per result. Logging it as an input means replay (lesson 19) can re-derive your rejection logic without re-running vision.

---

## Standing up `PhotonCameraSim`

`VisionIOPhotonSim` is the only place PhotonVision's sim types appear. It owns a `PhotonCamera`, a `PhotonCameraSim`, and a `VisionSystemSim` keyed against the WPILib field layout. Each loop it pulls the latest result, fills the inputs struct, and updates the simulated camera with the current ground-truth pose.

```java linenums="1"
private final VisionSystemSim visionSim = new VisionSystemSim("main");
private final PhotonCamera camera = new PhotonCamera("front");
private final PhotonCameraSim cameraSim;

public VisionIOPhotonSim(Supplier<Pose2d> groundTruthPoseSupplier) {
  var props = new SimCameraProperties(); // defaults are fine for lesson 24
  cameraSim = new PhotonCameraSim(camera, props);
  visionSim.addCamera(cameraSim, ROBOT_TO_CAMERA);
  visionSim.addAprilTags(AprilTagFieldLayout.loadField(AprilTagFields.k2025Reefscape));
  this.groundTruthPose = groundTruthPoseSupplier;
}
```

!!! warning "Don't feed sim ground-truth into your subsystem"

    The supplier above is *only* for the sim layer — it tells `visionSim` where to render the camera frustum from. Your real subsystem code must never read ground truth. If you reach for it, you've broken the abstraction and replay will lie to you.

---

## Subsystem wiring

`VisionSubsystem.periodic()` reads inputs, logs them, and — if a tag is visible with low enough ambiguity — calls into the swerve subsystem's pose estimator:

```java linenums="1"
io.updateInputs(inputs);
Logger.processInputs("Vision", inputs);
if (inputs.tagIds.length == 1 && inputs.ambiguity < 0.2) {
  swerve.addVisionMeasurement(
      inputs.estimatedRobotPose,
      inputs.latestTimestampSeconds);
}
```

The `addVisionMeasurement` overload that takes only a pose and a timestamp uses the pose estimator's default standard deviations. That's fine for lesson 24 — lesson 25 is where per-measurement stddev tuning becomes interesting.

---

## Rubric

The test class `VisionTest` asserts:

1. The sim publishes at least one frame with `tagIds.length == 1` when the robot is positioned 1.5 m in front of tag 18.
2. Within 0.5 s of first sight, the pose estimator's reported pose is within **5 cm** of ground truth.
3. With the camera disconnected (`inputs.connected == false`), `addVisionMeasurement` is **not** called.
4. High-ambiguity frames (`ambiguity > 0.2`) are rejected.

Run locally:

```bash
./gradlew test --tests '*VisionTest' -DincludeTags='lesson-24'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope's 3D field view, drop in:

- `Drive/Pose` (your fused estimate, lesson 22).
- `Vision/Inputs/estimatedRobotPose` (the raw vision pose).
- `Vision/Inputs/tagPosesOnField` (the tags being seen).

Drive a Choreo path that passes a known tag. The fused pose should snap toward the vision estimate at each sighting and then drift gently on odometry between sightings. That snap-then-drift signature is what working vision fusion looks like.

---

## Going further

- Compare your `VisionIO` to Presto's [`AprilTagVisionIO.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/apriltagvision/AprilTagVisionIO.java). What information does theirs carry that yours doesn't?
- Move the camera 20 cm forward in `ROBOT_TO_CAMERA`. Does the estimated pose track correctly, or does the extrinsic offset show up as a constant error?
- Try with `ambiguity < 0.5`. What happens to the pose trace when a glancing-angle frame slips through?

---

??? tip "Full reveal — only open if you're really stuck"

    The minimal `periodic()` looks like this:

    ```java
    @Override
    public void periodic() {
      io.updateInputs(inputs);
      Logger.processInputs("Vision", inputs);

      if (!inputs.connected) return;
      if (inputs.tagIds.length == 0) return;
      if (inputs.ambiguity > 0.2) return;

      swerve.addVisionMeasurement(
          inputs.estimatedRobotPose,
          inputs.latestTimestampSeconds);
    }
    ```

    If the test still fails, check that `latestTimestampSeconds` is in the same time base the pose estimator expects (`Timer.getFPGATimestamp()`-relative, not wall-clock).

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 23**
    Trajectory following

    [:octicons-arrow-left-24: Back to lesson 23](../../stage2b/23-trajectories/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 25**
    Multi-tag pose estimation

    [:octicons-arrow-right-24: Continue to lesson 25](../25-multitag/)

</div>
