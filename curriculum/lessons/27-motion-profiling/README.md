# Lesson 27 — Motion profiling

**Stage 2D · 55 min · Needs: 20**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Your arm goes from stopped to full speed in one loop. That shock-loads the gearbox,
spikes current and sags the battery.

## Do this

**1. Swap the controller** in `ShoulderSubsystem`:

```java
private final ProfiledPIDController pid =
    new ProfiledPIDController(
        Constants.Shoulder.kP, Constants.Shoulder.kI, Constants.Shoulder.kD,
        new TrapezoidProfile.Constraints(MAX_VELOCITY_RAD_PER_SEC, MAX_ACCEL_RAD_PER_SEC2));
```

Everything else stays. `calculate()` has the same signature and your gravity term is
unchanged.

Start with **generous** constraints the mechanism can clearly achieve. Verify it
still reaches setpoints before tightening anything.

**2. Add the velocity feedforward:**

```java
double ffVolts =
    Constants.Shoulder.kG * Math.cos(getAngleRadians())
        + Constants.Shoulder.kV * pid.getSetpoint().velocity;
```

**3. Plot velocity.** The trapezoid should be obvious. If it is not, your
constraints are looser than what the mechanism does anyway, and the profile is
doing nothing.

## What changed

A plain `PIDController` compares you against the **goal**. Ask for 90° and the error
is instantly 90°, so it commands everything it has.

A `ProfiledPIDController` compares you against a **moving setpoint** that walks from
where you are to where you asked, respecting the constraints. Velocity ramps up,
holds, ramps down. The controller only ever sees a small error.

The profile also knows how fast the mechanism *should* be moving right now, and
`kV` turns that into the voltage that produces that speed. Feedback is left handling
only the discrepancy.

## When not to bother

- flywheels and rollers: no position to profile
- anything that already moves gently
- anything where you want maximum speed and do not care about stress

Profiling costs two constants to tune and makes every motion slightly slower by
construction. Use it where violence is the problem.

## Watch out for

**Forgetting `pid.reset(currentPosition)`** when re-enabling. The profile starts
from a stale state and the arm jumps.

**Expecting a profile to fix a badly tuned PID.** It hides the symptoms by never
presenting a large error. Not the same as fixing it.

## Done

The arm reaches setpoints, velocity never exceeds the constraint, and the velocity
plot is a trapezoid rather than a spike.

```bash
./tools/frcprog next
```
