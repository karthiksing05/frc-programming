# Lesson 06 — Arm with gravity feedforward

> **Stage 1B · ~45 minutes · Prerequisite: 05**

The elevator fought a force that never changed. An arm does not have that luxury.

Hold an arm straight out horizontally and gravity acts on the longest possible
lever — it fights you as hard as it ever will. Rotate it straight up or straight
down and the lever is zero; gravity does nothing at all. Everywhere in between is
somewhere in between.

The torque follows `cos(angle)`, measuring from horizontal. So the voltage needed to
cancel it is `kG × cos(angle)` — a number that changes every loop, computed from a
sensor you already have.

## What you'll learn

1. Compute a feedforward term that varies with the mechanism's state.
2. Add feedforward to PID output before commanding the motor.
3. Tell "constant gravity" (elevator) apart from "angle-dependent gravity" (arm).
4. Recognise what a missing feedforward looks like on a plot.

## What you'll do

Open `src/main/java/frc/robot/subsystems/shoulder/ShoulderSubsystem.java`. The PID
half is already written — this lesson is only about the term next to it.

Replace:

```java
double gravityVolts = 0.0;
```

with:

```java
double gravityVolts = Constants.Shoulder.kG * Math.cos(getAngleRadians());
```

Then set `Constants.Shoulder.kG` to `0.12` volts.

### Read that line again

It is `cos(getAngleRadians())`, not `cos(setpointRadians)`.

You are cancelling the torque acting on the arm **right now**, not the torque that
will act on it once it arrives. Using the setpoint is a real bug that looks
completely correct: it behaves fine once the arm is settled, and misbehaves during
every long travel, exactly when you are least likely to be watching closely.

### Where 0.12 comes from

It is the number of volts that holds this arm exactly level. On a real robot you
find it by measurement, not arithmetic: command the arm to sit horizontal with no
feedback at all, raise the voltage until it stops falling, write that number down.

You could compute it — arm mass, length, gearing, motor torque constant — and the
answer would be wrong, because the real mechanism has a gearbox with friction, a
cable carrier that pulls, and a bearing that is slightly tight. Measure it.

### Why not just raise kP

You can. It even sort of works, and plenty of shipped robots do it. What you get:

- The arm sags a few degrees below every setpoint, permanently, because the PID only
  produces holding voltage when there is error to multiply.
- Someone adds `kI` to remove the sag, which works, and now the integral winds up
  during long travels and the arm lurches on arrival.
- Every gain has to be re-tuned when the mechanism is anywhere unusual.

Feedforward supplies the known force directly. The feedback loop is then left
handling only what is genuinely unpredictable, which is the job it is good at.

## Run it

```bash
./tools/frcprog check 06-arm-gravity-ff
```

Five checks:

1. Holds horizontal within 2° — and *keeps* holding it for another two seconds.
2. Reaches the down setpoint.
3. Reaches the up setpoint.
4. `kG` has a real value.
5. **The holding voltage comes from feedforward, not from PID fighting an error.**

Check 5 is the interesting one, and it exists because check 1 alone is cheatable: a
big enough `kP` shrinks the sag below 2° while completely missing the point. So the
rubric looks at *where the voltage is coming from*. With correct feedforward, a
settled arm sits at its setpoint with almost no error, so the PID contributes almost
nothing and the applied voltage is essentially all `kG × cos(angle)`. Without it,
the arm must sit off-target to generate the voltage it needs, and the two numbers
diverge.

That trick — grading the decomposition rather than the result — is worth
remembering.

## See it

```bash
./tools/frcprog sim
```

Connect AdvantageScope and plot the arm angle and its applied voltage together.

Command the arm to horizontal and watch the voltage settle at about 0.12. Then
command it to 60° up and watch the *holding* voltage fall to about 0.06 — because
`cos(60°)` is 0.5, and gravity now has half the lever it had.

That curve, voltage tracking the cosine of angle, is the lesson in one picture.

## Done?

```bash
./tools/frcprog next
```

Stage 1B is complete. You have a state machine, a position loop, and a mechanism
that compensates for physics. Next: commands, and your first drivable robot.

## Where this goes

`kG` is one term of a family. Full feedforward for a mechanism is usually written:

```
volts = kS·sign(velocity) + kG·cos(angle) + kV·velocity + kA·acceleration
```

- `kS` — static friction: the voltage needed just to break the mechanism loose
- `kG` — gravity, which you just did
- `kV` — volts per unit of velocity, for holding a *speed* (lesson 10 uses this)
- `kA` — volts per unit of acceleration, for changing speed

WPILib has `ArmFeedforward` and `ElevatorFeedforward` classes that bundle these.
Writing the `kG` term by hand once, first, is how the class stops being magic.
