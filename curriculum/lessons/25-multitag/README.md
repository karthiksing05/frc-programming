# Lesson 25 — Multi-tag pose estimation

> **Stage 2C · ~60 minutes · Prerequisite: 24-photonvision-singletag**
> **Extension lesson — needs one online build. See `lessons/EXTENSIONS.md`.**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

A single tag gives an ambiguous answer. Geometrically, two different camera poses can
produce the same image of one square — and at distance, or at a shallow angle, the
solver genuinely cannot tell which is right. You see it as a pose that occasionally
flips.

Two tags in one frame removes the ambiguity entirely.

## What you'll learn

1. Configure multiple simulated cameras.
2. Use `PhotonPoseEstimator` with a multi-tag strategy.
3. Scale trust with distance and tag count.
4. Filter measurements properly.

## Before you start

Needs PhotonVision. See `lessons/EXTENSIONS.md`.

## What you'll do

```java
photonEstimator.setPrimaryStrategy(PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR);
photonEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);
```

Then scale the standard deviations you pass to `addVisionMeasurement`:

```java
Matrix<N3, N1> stdDevs = singleTagStdDevs;
if (tagCount > 1) stdDevs = multiTagStdDevs;
stdDevs = stdDevs.times(1 + avgDistance * avgDistance / 30.0);
```

Read that last line as a statement about physics: error grows roughly with the square
of distance, because a tag twice as far away covers a quarter as many pixels.

### Two cameras

More coverage, more chances to see two tags, and a sanity check — two cameras
disagreeing is information. The cost is two transforms to get right and two things
to keep clean.

### Filters worth having

- Pose outside the field → reject.
- Pose implying the robot is airborne or tilted → reject.
- Pose more than a metre from the current estimate, with only one tag → suspicious.
- Average tag distance beyond about four metres → accept, but trust it much less.

Each filter costs a line and prevents a category of embarrassing behaviour.

## Done?

Two cameras publish, multi-tag estimates are visibly tighter than single-tag ones,
and out-of-field poses never make it into the estimator.

```bash
./tools/frcprog next
```
