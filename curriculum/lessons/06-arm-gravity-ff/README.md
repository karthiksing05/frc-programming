# Lesson 06 — Gravity feedforward

**Stage 1B · 45 min · Needs: 05**

An arm fights hardest when it is horizontal and not at all when it points straight up.

## Do this

**1. `subsystems/shoulder/ShoulderSubsystem.java`** — find `TODO (LESSON 06)` and
replace `double gravityVolts = 0.0;` with:

```java
double gravityVolts = Constants.Shoulder.kG * Math.cos(getAngleRadians());
```

**2. `Constants.java`** — set `Shoulder.kG` to `0.12`.

## Check it

```bash
./tools/frcprog check 06-arm-gravity-ff
```

Five checks. The last one is the interesting one: it verifies the holding voltage
comes from feedforward, not from PID fighting an error. A big enough `kP` passes a
position check while missing the point, so the rubric looks at where the voltage
comes from.

## Read that line again

It is `cos(getAngleRadians())`, not `cos(setpointRadians)`.

You are cancelling the torque acting on the arm **now**, not the torque that will
act on it when it arrives. Using the setpoint is a real bug that looks correct: it
behaves fine once settled and misbehaves during every long move.

## Why

Gravity torque on an arm follows `cos(angle)` measured from horizontal. Horizontal
means the full weight on the longest lever. Straight up or down means zero lever.

So the volts needed to cancel it are `kG * cos(angle)`, recomputed every loop from
a sensor you already have.

**Where 0.12 comes from:** measurement, not arithmetic. Command the arm level with
no feedback, raise voltage until it stops falling, write the number down. Calculate
it instead and you will be wrong, because the real mechanism has gearbox friction
and a cable that pulls.

**Why not just raise kP:**

- the arm sags a few degrees below every setpoint, permanently
- someone adds `kI` to hide the sag, which then winds up during long moves
- every gain needs re-tuning whenever the arm is somewhere unusual

## See it

```bash
./tools/frcprog sim
```

Plot arm angle and applied voltage together. Command horizontal: voltage settles
near 0.12. Command 60° up: holding voltage falls to about 0.06, because `cos(60°)`
is 0.5.

## Done

Rubric is green. Stage 1B complete.

```bash
./tools/frcprog next
```

**Where this goes.** Full feedforward is
`kS·sign(v) + kG·cos(θ) + kV·v + kA·a`. You just did `kG`. WPILib has
`ArmFeedforward` and `ElevatorFeedforward` that bundle all four. Writing one term
by hand first is how the class stops being magic.
