# Lesson 25 — Multi-tag pose estimation <small>· Stage 2C</small>

<span class="stage-badge">Stage 2C · Lesson 25</span>

*One tag tells you where you are, modulo a flip. Two tags, simultaneously, tell you where you are.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2C |
    | **Time** | ~60 min |
    | **Prereqs** | [Lesson 24 — PhotonVision single-tag](../24-photonvision-singletag/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/vision/{VisionIO,VisionIOPhotonSim,VisionSubsystem}.java` (extend) |
    | **Tests** | `frc.robot.subsystems.vision.MultiTagTest` (`@Tag("lesson-25")`) |
    | **Reference robot** | Presto · [`apriltagvision/AprilTagVision.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/apriltagvision) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Run more than one simulated camera against a shared `VisionSystemSim`.
2. Use `PhotonPoseEstimator.MULTI_TAG_PNP_ON_COPROCESSOR` and explain what "on coprocessor" implies.
3. Compute per-measurement standard deviations from tag count + average distance.
4. Reject measurements that land outside the field, behind a wall, or below the floor.
5. Read a multi-camera AdvantageScope view well enough to spot a misaligned extrinsic.

---

## The real-world problem

Lesson 24's single-tag pipeline is honest but fragile. A single AprilTag has a mathematical pose ambiguity (the IPPE problem) — the tag plane has two valid orientations that produce the same image, and PhotonVision will silently pick one. Worse, single-tag distance estimates degrade quickly: at 3 m, a 1° pixel error in the corner detector turns into ~5 cm of pose error. At 6 m it's worse than that.

Multi-tag PnP fixes both. When the camera sees two or more tags in the same frame, the solver has enough constraints to resolve the ambiguity and tighten the variance. Add a second physical camera looking the other direction and the robot almost always has *some* tag in view from *some* angle, which means continuous correction instead of episodic snaps.

Production teams (Presto, Kelpie, every championship-eligible swerve) treat multi-tag as the default. Single-tag becomes the fallback for "we glimpsed exactly one and we're not sure."

---

## What you'll do

Extend the lesson 24 `VisionIO` to handle an *array* of camera observations, not one. Stand up a second `PhotonCameraSim` at a rear-facing extrinsic. Inside `VisionSubsystem`, route each frame through a `PhotonPoseEstimator` configured with `MULTI_TAG_PNP_ON_COPROCESSOR`. Compute per-frame standard deviations from tag count and average tag distance; pass them into `addVisionMeasurement(pose, timestamp, stdDevs)`. Add three rejection heuristics (out-of-field, high z-error, max ambiguity) and verify the pose stays inside 5 cm of ground truth on a long path.

!!! note "Why the strategy is named `MULTI_TAG_PNP_ON_COPROCESSOR`"

    The name signals where the solve happens: the coprocessor (Orange Pi, Raspberry Pi) runs the multi-tag PnP solver and ships PhotonVision a single fused pose. The RIO-side library reads that fused pose rather than re-solving. In sim there *is* no coprocessor, but PhotonVision's sim layer mimics the same data flow, so your subsystem code is identical between sim and real.

---

## Multi-camera IO

The inputs struct grows from "one observation" to "an array of observations." Match Presto's shape:

```java linenums="1"
@AutoLog
public class VisionIOInputs {
  public boolean[] connected = new boolean[0];
  public PoseObservation[] observations = new PoseObservation[0];
  public int[] tagIdsSeen = new int[0];
}

public record PoseObservation(
    double timestampSeconds,
    Pose3d estimatedPose,
    double ambiguity,
    int tagCount,
    double averageTagDistanceMeters,
    PoseStrategy strategy) {}
```

A `record` plays nicely with AdvantageKit's structured logging if you register a struct serializer for it. Presto's `apriltagvision/` package has the exact pattern — copy it; don't reinvent it.

---

## Per-measurement standard deviations

Vision quality is not constant. A two-tag frame at 1.5 m is dramatically better than a one-tag frame at 4 m, and your pose estimator deserves to know. The standard heuristic (it's the one Presto and Kelpie both use, give or take constants) is:

```java linenums="1"
double xyStd = BASE_XY_STD
    * Math.pow(obs.averageTagDistanceMeters(), 2)
    / Math.max(1, obs.tagCount());
double thetaStd = (obs.tagCount() >= 2)
    ? BASE_THETA_STD * Math.pow(obs.averageTagDistanceMeters(), 2)
    : Double.POSITIVE_INFINITY; // single-tag: don't trust rotation
swerve.addVisionMeasurement(
    obs.estimatedPose().toPose2d(),
    obs.timestampSeconds(),
    VecBuilder.fill(xyStd, xyStd, thetaStd));
```

Two ideas in one snippet: variance scales with distance squared (the geometry of pixel-to-meter projection), and single-tag rotation gets infinite stddev so the estimator ignores it. Multi-tag is the only thing trusted to correct heading.

!!! warning "Don't tune `BASE_XY_STD` by feel alone"

    Log `Vision/Observations/*/xyStd` alongside the pose error against ground truth in sim. If your stddev is way smaller than your actual error, the estimator over-trusts vision and the pose dances. Way larger and the estimator may as well not be using vision at all. Tune in sim until the trace looks "snappy without jittering."

---

## Rejecting bad measurements

Three filters cover most failure modes:

1. **Out-of-field.** If `estimatedPose.getX()` is outside `[0, FIELD_LENGTH]` or `getY()` outside `[0, FIELD_WIDTH]`, drop it. The PnP solver occasionally picks the flipped solution and reports a pose 8 meters off the field. No measurement is better than that one.
2. **Floor / ceiling.** If `|estimatedPose.getZ()| > 0.5 m`, drop it. The robot's base is flat on the carpet; large z means the solver picked the ambiguous flipped plane.
3. **Ambiguity ceiling for single-tag.** If `tagCount == 1 && ambiguity > 0.2`, drop it.

These three reject roughly the same set of garbage frames Presto and Kelpie reject. They're cheap. Apply them before computing stddevs.

---

## Rubric

The test class `MultiTagTest` asserts:

1. Both simulated cameras publish observations.
2. Across a 6-second simulated drive past three tags, the fused pose error stays under **3 cm** average — tighter than lesson 24's 5 cm.
3. A pose with `getX() < 0` (manually injected into the inputs) is rejected.
4. A single-tag frame at 5 m with `ambiguity = 0.4` is rejected.
5. The stddev vector for a 1-tag observation has `theta == Double.POSITIVE_INFINITY`.

Run locally:

```bash
./gradlew test --tests '*MultiTagTest' -DincludeTags='lesson-25'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope's 3D field view. You should be able to drop in:

- `Drive/Pose` — the fused estimate.
- `Vision/Inputs/observations/*/estimatedPose` — raw per-camera observations.
- Two camera frustums, one for each simulated camera.

Drive a long Choreo path along the field perimeter. The fused pose should stay tight throughout — no episodic snap-correct-drift like lesson 24, just a continuous low-amplitude correction signal.

!!! example "What "good" looks like"

    Plot `Drive/Pose` and the ground-truth `Sim/RobotPose` on the same chart for X, Y, and theta. The two traces should be indistinguishable at zoom level "1 second visible." If you can see a daylight gap, your stddevs are wrong or your extrinsic is misaligned.

---

## Going further

- Read Presto's [`AprilTagVision.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/apriltagvision/AprilTagVision.java) end-to-end. Their rejection logic has a few more checks than yours — which ones surprise you?
- Disable one camera mid-run. Does the estimator degrade gracefully?
- Try `MULTI_TAG_PNP_ON_RIO` instead. What changes? (Latency goes up; the IO interface stays identical — that's the point of the abstraction.)

---

??? tip "Full reveal — only open if you're really stuck"

    The `periodic()` loop is essentially:

    ```java
    io.updateInputs(inputs);
    Logger.processInputs("Vision", inputs);
    for (var obs : inputs.observations) {
      if (rejectOutOfField(obs)) continue;
      if (rejectHighZ(obs)) continue;
      if (obs.tagCount() == 1 && obs.ambiguity() > 0.2) continue;
      var stdDevs = computeStdDevs(obs);
      swerve.addVisionMeasurement(
          obs.estimatedPose().toPose2d(),
          obs.timestampSeconds(),
          stdDevs);
    }
    ```

    If `MultiTagTest` fails on the 3 cm bound, your stddevs are the most likely culprit. Halve `BASE_XY_STD` and re-run.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 24**
    PhotonVision single-tag

    [:octicons-arrow-left-24: Back to lesson 24](../24-photonvision-singletag/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 26**
    maple-sim & game-piece physics

    [:octicons-arrow-right-24: Continue to lesson 26](../26-maplesim/)

</div>
