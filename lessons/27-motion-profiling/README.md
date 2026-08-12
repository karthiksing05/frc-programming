# Lesson 27 — Motion profiling

> **Stage 2D · ~55 minutes · Prerequisite: 20-superstructure**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Your arm goes from stationary to full speed in one loop. Mechanically that is
violent: it shock-loads the gearbox, spikes current, sags the battery, and induces
oscillation in everything bolted to it.

A motion profile fixes it by asking for a *trajectory* rather than a destination.

## What you'll learn

1. Replace `PIDController` with `ProfiledPIDController`.
2. Choose velocity and acceleration constraints.
3. Add a velocity feedforward term alongside lesson 06's gravity term.
4. Recognise when profiling is worth it and when it is overkill.

## What you'll do

Refactor `ShoulderSubsystem`:

```java
private final ProfiledPIDController pid =
    new ProfiledPIDController(
        Constants.Shoulder.kP,
        Constants.Shoulder.kI,
        Constants.Shoulder.kD,
        new TrapezoidProfile.Constraints(MAX_VELOCITY_RAD_PER_SEC, MAX_ACCEL_RAD_PER_SEC2));
```

Everything else stays. `calculate()` has the same signature, and your gravity
feedforward is unchanged.

### What actually changed

A plain `PIDController` compares you against the *goal*. Ask for 90° and the error is
immediately 90°, so it commands everything it has.

A `ProfiledPIDController` compares you against a *moving setpoint* that walks from
where you are to where you asked, respecting the constraints. Velocity ramps up,
holds, ramps down — the trapezoid the profile is named for. The controller only ever
sees a small error, because the setpoint is never far from the mechanism.

Add the velocity feedforward for a real improvement:

```java
double ffVolts =
    Constants.Shoulder.kG * Math.cos(getAngleRadians())
        + Constants.Shoulder.kV * pid.getSetpoint().velocity;
```

The profile knows how fast the mechanism *should* be moving right now, and `kV`
converts that into the voltage that produces that speed. Feedback is left handling
only the discrepancy.

### When not to bother

- Flywheels. There is no position to profile.
- Rollers. Same.
- Anything that already moves gently.
- Anything where you want maximum speed and do not care about mechanical stress.

Profiling costs two constants to tune and makes every motion slightly slower by
construction. Use it where violence is the problem.

## Done?

The arm reaches its setpoints, velocity never exceeds the constraint, and the
velocity plot is a trapezoid rather than a spike.

```bash
./tools/frcprog next
```
