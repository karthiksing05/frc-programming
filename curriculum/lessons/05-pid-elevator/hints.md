# Hints — Lesson 05

## Hint 1 — Where to start

Two separate jobs, and doing them in the wrong order wastes time.

**First** get the code in `periodic()` working. Until voltage reaches the motor, no
amount of tuning does anything, and you will conclude your gains are wrong when
your wiring is.

**Then** tune. A quick way to tell them apart: after writing the code, set `kP` to
something large like 50 and run the check. If the carriage moves *at all* — even
badly — the code is right and the rest is tuning.

## Hint 2 — The shape of the answer

`periodic()` is three statements. `pid` and `gravityVolts` already exist; you only
need to use them.

```java
double feedbackVolts = pid.calculate( /* where am I? */, /* where do I want to be? */ );
double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);
appliedVolts = volts;
motor.setVoltage(volts);
```

`getHeightMeters()` answers the first question. `setpointMeters` is the second.

Do not skip `appliedVolts = volts;` — the physics simulation reads that field, so
without it the carriage never moves no matter what you command.

## Hint 3 — Almost there

Tuning, with numbers to actually try.

The elevator carries 5 kg through 6:1 gearing on a 2 cm drum, and heights are in
metres — so an error of "half a metre" is `0.5`, not `50`. That means `kP` needs to
be big: multiplying 0.5 by 10 gives 5 volts, which is in the right region.

Try, in order:

| `kP` | `kD` | What you should see |
|---:|---:|---|
| 10 | 0 | Rises, slowly, probably stalls short |
| 40 | 0 | Gets there fast, overshoots and rings |
| 40 | 6 | Gets there fast, stops cleanly ✓ |

If it overshoots, raise `kD`. If it is sluggish, raise `kP`. You very likely do not
need `kI` at all here — the gravity feedforward is already doing the job `kI` would
otherwise be dragged into.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

**ElevatorSubsystem.periodic**

```java
@Override
public void periodic() {
    double gravityVolts = Constants.Elevator.kG_HOLD;

    // Feedback: how hard to push, given how far off we are right now.
    double feedbackVolts = pid.calculate(getHeightMeters(), setpointMeters);

    // Clamping is not optional. A large kP multiplied by a large startup error
    // asks for a voltage that does not exist, and on real hardware the motor
    // controller's own limiting will produce behaviour you did not model.
    double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);

    appliedVolts = volts;
    motor.setVoltage(volts);
}
```

**Constants.Elevator**

```java
public static final double kP = 40.0;
public static final double kI = 0.0;
public static final double kD = 6.0;
```

These are not the only gains that pass. Anything that arrives within 2 cm in 1.5 s
without overshooting 5 cm is a correct answer, and finding your own is better
practice than copying these.

**Two ways to go wrong:**

`pid.calculate(setpointMeters, getHeightMeters())` — arguments swapped. The sign of
every correction inverts, so the carriage sprints away from the target and pins
itself at whichever end it reaches first. Signature is
`calculate(measurement, setpoint)`.

`motor.set(volts)` instead of `motor.setVoltage(volts)` — `set` expects −1 to 1, so
a 6-volt request is interpreted as 600% throttle and clipped to full power. You get
bang-bang control, with extra steps.

</details>
