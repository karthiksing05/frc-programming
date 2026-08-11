# Lesson 22 — Odometry & pose estimation

> **Stage 2B · ~55 minutes · Prerequisite: 21-swerve-intro**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Pure odometry drifts. Wheels slip, carpet compresses, a collision happens. Over a
two-minute match the robot's belief about where it is diverges from where it is, and
anything that depends on that belief — auto paths, vision-assisted aiming — degrades
with it.

## What you'll learn

1. The difference between odometry and pose *estimation*.
2. Swap `SwerveDriveOdometry` for `SwerveDrivePoseEstimator`.
3. What the standard-deviation parameters mean, without the Kalman maths.
4. Reset pose correctly at the start of autonomous.

## What you'll do

The swap is nearly mechanical:

```java
private final SwerveDrivePoseEstimator estimator =
    new SwerveDrivePoseEstimator(
        kinematics,
        gyroAngle,
        modulePositions,
        new Pose2d(),
        VecBuilder.fill(0.05, 0.05, Units.degreesToRadians(5)),   // trust in wheels+gyro
        VecBuilder.fill(0.5, 0.5, Units.degreesToRadians(30)));   // trust in vision
```

The estimator does everything odometry did, and additionally accepts corrections
from other sources via `addVisionMeasurement`. Lesson 24 provides those.

### What the standard deviations actually mean

They are "how much do I trust this?", expressed as an expected error.

Small numbers mean high trust. The first vector says how much you trust wheels and
gyro; the second says how much you trust a vision measurement. The estimator
weights them accordingly.

You do not need Kalman filtering to tune these. Two rules cover most cases:

- **Trust wheels over short intervals, vision over long ones.** Wheels are precise
  moment to moment and drift slowly. Vision is noisy moment to moment and does not
  drift at all.
- **Trust a vision measurement less when it is far away or based on one tag.**
  Scaling the vision standard deviations with distance is a standard, effective
  trick.

### Resetting at the start of auto

```java
drive.resetPose(trajectory.getInitialPose());
```

You did this in lesson 13. The reason it matters more now: everything downstream —
path following, vision fusion, aiming — is relative to this belief. Start wrong and
everything is wrong, consistently, in a way that looks like a tuning problem.

## Done?

Pose updates correctly as the robot drives, `resetPose` works, and `Drive/Pose`
renders on AdvantageScope's field view.

```bash
./tools/frcprog next
```
