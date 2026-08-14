# Lesson 22 — Pose estimation

**Stage 2B · 55 min · Needs: 21**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Wheels slip, carpet compresses, robots get hit. Over two minutes your belief about
where you are drifts from where you are.

## Do this

**1. Swap the class**, changing nothing else:

```java
private final SwerveDrivePoseEstimator estimator =
    new SwerveDrivePoseEstimator(
        kinematics, gyroAngle, modulePositions, new Pose2d(),
        VecBuilder.fill(0.05, 0.05, Units.degreesToRadians(5)),   // trust wheels+gyro
        VecBuilder.fill(0.5, 0.5, Units.degreesToRadians(30)));   // trust vision
```

It does everything odometry did, and also accepts corrections via
`addVisionMeasurement`. Lesson 24 supplies those.

**2. Publish the pose as a struct** (lesson 18) and put it on a 2D Field tab.
Watching the robot move on a field drawing is the fastest way to catch a sign
error.

**3. Add a `resetPose`** and call it at the start of auto.

## What the standard deviations mean

"How much do I trust this", as an expected error. Small means high trust.

Two rules cover most cases:

- **Trust wheels over short intervals, vision over long ones.** Wheels are precise
  moment to moment and drift slowly. Vision is noisy moment to moment and does not
  drift.
- **Trust a vision measurement less when it is far away or based on one tag.**
  Scaling vision standard deviations with distance is standard and effective.

You do not need Kalman theory to tune these.

## Watch out for

**Module positions in a different order** than the kinematics was constructed with.
This one produces no error and no warning; the pose simply drifts, and the drift
gets worse the more the robot turns.

**Resetting the gyro instead of the pose.** `resetPose` says where you are. Zeroing
the gyro changes what "forward" means, which is almost never what you wanted.

**Calling `update` more than once per loop**, which double-counts wheel travel.


## See it

Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

Add a **2D Field** tab and drag `Drive/Pose` onto it. Drive around and watch the
robot track.

Sign errors are obvious here and invisible in a number: drive forward and the robot
should move up the field, not sideways or backwards.

Then plot the pose X and Y on a Line Graph and drive a square. You should see the
two traces trade off cleanly, and end near where you started.

## Done

Pose updates correctly, `resetPose` works, and `Drive/Pose` renders on the field
view.

```bash
./tools/frcprog next
```

Everything downstream (path following, vision fusion, aiming) is relative to this
belief. Start wrong and everything is wrong, in a way that looks like a tuning
problem.
