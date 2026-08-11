# Hints — Lesson 22

## Hint 1 — Where to start

Swap the class first and change nothing else. `SwerveDrivePoseEstimator` is a drop-in for `SwerveDriveOdometry` if you pass the same arguments plus the two standard-deviation vectors.

## Hint 2 — The shape of the answer

Then publish the pose as a struct (lesson 18) and put it on a 2D Field tab. Watching the robot move on a field drawing is the fastest way to catch a sign error.

## Hint 3 — What usually goes wrong

Calling `update` with module positions in a different order than the kinematics was constructed with. Silent, and produces a pose that is subtly wrong in a way that gets worse as you turn.

Resetting the gyro instead of the pose. `resetPose` tells the estimator where you are; zeroing the gyro changes what 'forward' means, which is almost never what you wanted.

Calling `update` more than once per loop, which double-counts wheel travel.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

[WPILib's pose estimator documentation](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/state-space/state-space-pose-estimators.html) is the reference. Kelpie's `SwerveSubsystem` shows the wiring, including how vision measurements are gated before being accepted.

</details>
