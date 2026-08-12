# Lesson 24 — PhotonVision single-tag

> **Stage 2C · ~45 minutes · Prerequisite: 23-trajectories**
> **Extension lesson — needs one online build. See `lessons/EXTENSIONS.md`.**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Odometry knows how far the wheels turned. It does not know that you are three
degrees off the goal, or that a collision moved you half a metre. A camera looking at
an AprilTag does.

## What you'll learn

1. What an AprilTag is and why FRC fields are covered in them.
2. Build a `VisionIO` layer, the same shape as lesson 16's.
3. Simulate a camera, so this lesson runs on a laptop.
4. Feed a vision measurement into the pose estimator from lesson 22.

## Before you start

Needs the PhotonVision vendordep. See `lessons/EXTENSIONS.md`.

## What you'll do

An AprilTag is a printed square with a pattern that encodes an ID. The field layout
tells you exactly where each tag is. See one in a camera image and you can compute
where the camera must be — and therefore where the robot is.

```java
var result = camera.getLatestResult();
if (result.hasTargets()) {
  var target = result.getBestTarget();
  Optional<Pose3d> tagPose = fieldLayout.getTagPose(target.getFiducialId());
  if (tagPose.isPresent()) {
    Pose3d robotPose =
        PhotonUtils.estimateFieldToRobotAprilTag(
            target.getBestCameraToTarget(), tagPose.get(), cameraToRobot);
    drive.addVisionMeasurement(robotPose.toPose2d(), result.getTimestampSeconds());
  }
}
```

### Vision *corrects* odometry, it does not replace it

`addVisionMeasurement`, not `resetPose`. Vision is noisy, arrives late, and
occasionally lies — a reflection, a partially-occluded tag, a tag someone left on a
cart in the pit. Slamming the pose to a vision reading every frame produces a robot
whose position jumps around, and anything using that pose jumps with it.

Fusing it in means good measurements pull you gently toward truth and bad ones get
outvoted.

### Reject the obvious nonsense

Before accepting any measurement:

- Is the pose inside the field?
- Is the tag close enough to be trustworthy?
- Is the ambiguity score low? (Single-tag poses have a genuine mathematical
  ambiguity — two poses can produce the same image.)

That ambiguity is the main reason lesson 25 exists.

### Latency

A vision measurement describes where you were when the shutter opened, tens of
milliseconds ago. Passing the timestamp is what lets the estimator rewind, insert the
measurement, and replay forward. Ignore it and you are correcting your current
position with old information — which, at speed, makes things worse rather than
better.

## Done?

The simulated camera sees a tag, the pose estimate improves when it does, and
obviously-wrong measurements are rejected.

```bash
./tools/frcprog next
```
