# Lesson 24 — AprilTags

**Stage 2C · 45 min · Needs: 23**

!!! warning "Needs one online build"

    This lesson uses a vendor library. See `lessons/EXTENSIONS.md` for the
    four-step install. Everything else in the curriculum runs offline.

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Odometry cannot tell you that you are three degrees off the goal. A camera can.

## Do this

1. Install the PhotonVision vendordep
2. **Get the simulated camera producing targets first.** `VisionSystemSim` plus the
   field layout will show you what it sees. If it sees nothing, no amount of
   estimator work helps.
3. Build a `VisionIO` layer, same shape as lesson 16's
4. Feed measurements in one at a time and watch the pose on the field view

The core:

```java
var result = camera.getLatestResult();
if (result.hasTargets()) {
  var target = result.getBestTarget();
  Optional<Pose3d> tagPose = fieldLayout.getTagPose(target.getFiducialId());
  if (tagPose.isPresent()) {
    Pose3d robotPose = PhotonUtils.estimateFieldToRobotAprilTag(
        target.getBestCameraToTarget(), tagPose.get(), cameraToRobot);
    drive.addVisionMeasurement(robotPose.toPose2d(), result.getTimestampSeconds());
  }
}
```

## Three things that matter

**`addVisionMeasurement`, not `resetPose`.** Vision is noisy, late, and occasionally
lies: a reflection, a half-occluded tag, a tag someone left on a cart. Slamming the
pose every frame gives you a robot that jumps, and everything using that pose jumps
too. Fusing means good measurements pull gently and bad ones get outvoted.

**Reject obvious nonsense** before accepting anything:

- pose outside the field
- tag too far to trust
- high ambiguity score

Single-tag poses have a real mathematical ambiguity: two poses can produce the same
image. That is why lesson 25 exists.

**Pass the timestamp.** A measurement describes where you were when the shutter
opened, tens of milliseconds ago. The timestamp lets the estimator rewind, insert
it, and replay forward. Ignore it and you correct your current position with old
information, which at speed makes things worse.

## Watch out for

**A wrong camera-to-robot transform.** Every pose is then offset by exactly that
error, consistently, which reads as "vision is broken".

**Using `Timer.getFPGATimestamp()`** instead of the result's own timestamp.

## Done

The simulated camera sees a tag, the estimate improves when it does, and bad
measurements are rejected.

```bash
./tools/frcprog next
```
