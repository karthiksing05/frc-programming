# Vision (PhotonVision, AprilTags)

!!! warning "Coming in Phase 2"

    This page is a stub. The full handbook entry is authored in Phase 2 — see [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Until then, Stage 2C lessons 24–25 are the canonical reference.

## What this page will cover

The vision pipeline most competitive FRC teams now run:

- **AprilTags** — the standardized fiducials FIRST adopted in 2023; tag families, sizes, field layouts
- **PhotonVision** — open-source coprocessor stack; running it on an Orange Pi 5 or a Limelight
- **Camera calibration** — intrinsics, why your pose estimate is bad if you skip this
- **Single-tag pose** vs **multi-tag pose** vs **MegaTag** (Limelight) — accuracy tradeoffs
- **Fusing vision into odometry** — `SwerveDrivePoseEstimator.addVisionMeasurement` with proper stddev tuning
- **Latency compensation** — the timestamp your camera reports is *not* now

## One useful nugget right now

**Vision pose is a measurement, not a truth.** The right way to use it is to feed it to `SwerveDrivePoseEstimator` with stddevs that reflect your real confidence — high stddev when only one tag is visible far away, low stddev with multi-tag at close range. Treating vision as authoritative leads to robots that teleport when a tag flickers in and out of view.

```java
poseEstimator.addVisionMeasurement(
    visionPose,
    captureTimestampSeconds,
    VecBuilder.fill(stddevX, stddevY, stddevTheta)
);
```

## Lessons that cover this material today

- [Lesson 24 — PhotonVision single-tag](../learn/stage2c/24-photonvision-singletag/index.md)
- [Lesson 25 — Multi-tag pose estimation](../learn/stage2c/25-multitag/index.md)

## See also

- [PhotonVision docs](https://docs.photonvision.org/)
- [WPILib AprilTags](https://docs.wpilib.org/en/stable/docs/software/vision-processing/apriltag/index.html)
- Presto's [`apriltagvision/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/apriltagvision) — the canonical multi-camera fusion example
