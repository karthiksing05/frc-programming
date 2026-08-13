# Lesson 25 — Multi-tag

**Stage 2C · 60 min · Needs: 24**

!!! warning "Needs one online build"

    This lesson uses a vendor library. See `lessons/EXTENSIONS.md` for the
    four-step install. Everything else in the curriculum runs offline.

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

One tag gives an ambiguous answer. Two in one frame removes the ambiguity entirely.

## Do this

**1. Add a second camera first.** Two cameras with the simple strategy is a working
system. One camera with a complicated strategy is not.

**2. Then switch strategy:**

```java
photonEstimator.setPrimaryStrategy(PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR);
photonEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);
```

**3. Scale trust with distance and tag count:**

```java
Matrix<N3, N1> stdDevs = singleTagStdDevs;
if (tagCount > 1) stdDevs = multiTagStdDevs;
stdDevs = stdDevs.times(1 + avgDistance * avgDistance / 30.0);
```

Read that last line as physics: error grows roughly with the square of distance,
because a tag twice as far covers a quarter as many pixels.

**4. Compare.** Log both estimates against the ground-truth pose the simulator
gives you. The improvement should be obvious on a plot. If it is not, something is
wrong.

## Why the ambiguity exists

Geometrically, two different camera poses can produce the same image of one square.
At distance or a shallow angle the solver genuinely cannot tell which is right. You
see it as a pose that occasionally flips.

## Filters worth having

Each costs a line and prevents a category of embarrassment.

- pose outside the field → reject
- pose implying the robot is airborne or tilted → reject
- pose more than a metre from the current estimate with only one tag → suspicious
- average tag distance beyond about four metres → accept, trust much less

## Watch out for

**Adding the same measurement twice**, once per camera, when both saw the same
tags. The estimator becomes overconfident.

**Camera transforms measured from the wrong reference point.** Measure to the
robot's centre of rotation, consistently, and write down which corner of the camera
you measured to.

## Done

Two cameras publish, multi-tag estimates are visibly tighter, and out-of-field
poses never reach the estimator.

```bash
./tools/frcprog next
```
