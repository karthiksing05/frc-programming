# Hints — Lesson 06

## Hint 1 — Where to start

You are replacing exactly one line. Find:

```java
double gravityVolts = 0.0;
```

Two questions to answer before you type:

- Which angle should go inside `cos()` — where the arm *is*, or where you *asked*
  for it to be?
- The gain is in `Constants.Shoulder`. What is it currently set to, and what does
  that mean the whole term currently evaluates to?

## Hint 2 — The shape of the answer

```java
double gravityVolts = Constants.Shoulder.kG * Math.cos( /* which angle? */ );
```

The method that tells you where the arm is right now is `getAngleRadians()`.

`Math.cos` takes radians, and `getAngleRadians()` returns radians, so no conversion
is needed. That is not luck — it is why the getter is named the way it is.

## Hint 3 — Almost there

If the arm still sags after fixing the line, look at `Constants.Shoulder.kG`. Zero
times anything is zero, so a correct formula with a zero gain behaves exactly like
no feedforward at all.

The value for this arm is `0.12`.

If check 5 fails while checks 1–3 pass, you are holding position using PID error
rather than feedforward — usually a `kG` that is far too small, or a `cos` of the
setpoint rather than of the current angle.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

**ShoulderSubsystem.periodic**

```java
double feedbackVolts = pid.calculate(getAngleRadians(), setpointRadians);

// cos() of the CURRENT angle, not of the setpoint: we are cancelling the
// torque acting on the arm at this instant. At 0 rad (horizontal) cos is 1
// and gravity is at its worst; at ±90° cos is 0 and gravity does nothing.
double gravityVolts = Constants.Shoulder.kG * Math.cos(getAngleRadians());

double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);
appliedVolts = volts;
motor.setVoltage(volts);
```

**Constants.Shoulder**

```java
public static final double kG = 0.12;
```

**The bug worth seeing:**

```java
double gravityVolts = Constants.Shoulder.kG * Math.cos(setpointRadians);  // ✗
```

Once the arm has arrived, current angle and setpoint are the same, so this is
indistinguishable from correct. It only misbehaves *during* a move — the arm
travelling from −45° up to +60° is compensating for a force it will not experience
until it gets there, so it lags on the way up and rushes on the way down.

You will find this class of bug repeatedly in control code: something that is
correct at steady state and wrong in transit. Steady-state testing will never
catch it. Watching the plot during a move will.

</details>
