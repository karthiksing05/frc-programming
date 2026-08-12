# Hints — Lesson 25

## Hint 1 — Where to start

Add the second camera before touching the estimation strategy. Two cameras with the simple strategy is a working system; one camera with a complicated strategy is not.

## Hint 2 — The shape of the answer

Then switch the strategy and compare. Log both estimates and the ground-truth pose the simulator gives you — the improvement should be obvious on a plot, and if it is not, something is wrong.

## Hint 3 — What usually goes wrong

Adding the same measurement twice, once per camera, when both saw the same tags. The estimator becomes overconfident.

Standard deviations that do not scale with distance, so a four-metre single-tag reading gets the same trust as a one-metre multi-tag one.

Camera transforms measured from the wrong reference point. Measure to the robot's centre of rotation, consistently, and write down which corner of the camera you measured to.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

[PhotonVision pose estimation strategies](https://docs.photonvision.org/en/latest/docs/programming/photonlib/robot-pose-estimator.html). Presto's multi-camera fusion is the reference implementation.

</details>
