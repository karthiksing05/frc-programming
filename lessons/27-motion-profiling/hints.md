# Hints — Lesson 27

## Hint 1 — Where to start

Swap the controller class and set generous constraints — something the mechanism can clearly achieve. Verify it still reaches its setpoints before tightening anything.

## Hint 2 — The shape of the answer

Then plot velocity. The trapezoid should be obvious. If it is not, your constraints are looser than what the mechanism does naturally, and the profile is doing nothing.

## Hint 3 — What usually goes wrong

Constraints tighter than the mechanism needs, making everything sluggish for no benefit.

Using `pid.getSetpoint().velocity` for feedforward but forgetting `pid.reset(currentPosition)` when re-enabling — the profile then starts from a stale state and the arm jumps.

Expecting a profile to fix a badly tuned PID. It hides the symptoms by never presenting a large error, which is not the same as fixing it.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

[WPILib ProfiledPIDController docs](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/profiled-pidcontroller.html).

```java
@Override
public void periodic() {
  double feedback = pid.calculate(getAngleRadians(), setpointRadians);
  double gravity = Constants.Shoulder.kG * Math.cos(getAngleRadians());
  double velocityFf = Constants.Shoulder.kV * pid.getSetpoint().velocity;

  double volts = MathUtil.clamp(feedback + gravity + velocityFf, -12.0, 12.0);
  appliedVolts = volts;
  motor.setVoltage(volts);
}
```

</details>
