# Lesson 05 — PID

**Stage 1B · 50 min · Needs: 04**

Make a motor stop at a number instead of near it.

## Do this

**1. `subsystems/elevator/ElevatorSubsystem.java`** — find `TODO (LESSON 05)` in
`periodic()`. Three lines:

```java
double feedbackVolts = pid.calculate(getHeightMeters(), setpointMeters);
double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);
appliedVolts = volts;
motor.setVoltage(volts);
```

**2. `Constants.java`** — tune `Elevator.kP`, `kI`, `kD`. They start at zero, so
the carriage sits on the floor.

## Check it

```bash
./tools/frcprog check 05-pid-elevator
```

Five checks, and they grade a response over time:

- reaches low, mid and high within 2 cm
- gets there inside 1.5 s
- never overshoots by more than 5 cm, including on the way down
- holds position for two seconds afterwards

## Tuning recipe

1. All three at zero.
2. Raise `kP` until it arrives fast but bounces.
3. Raise `kD` until the bouncing stops.
4. Only if it parks short forever, add a little `kI`.

Move in big steps. Try 10, then 40, then back off. Nudging by 0.5 takes all
afternoon. Heights are in metres, so an error of "half a metre" is `0.5`, which
means `kP` needs to be large.

## Why

**`kP`** — push in proportion to how wrong you are. Most of your tuning.
**`kD`** — how fast the error is shrinking. Brakes early. Cures the bouncing.
**`kI`** — how long you have been slightly wrong. Fixes parking short. Easy to overdo.

**The gravity term is already written.** An elevator weighs the same at every
height, so one constant holds it. Without it, `kP` would have to fight gravity by
letting error exist. Lesson 06 is an arm, where that number changes with angle.

**Volts, not throttle.** `motor.set(0.7)` means 70% of whatever the battery is
right now. A fresh battery is 12.6 V, a sagging one is 10.5 V, so the same line
delivers different physics late in a match. `setVoltage` compensates. Tune in
volts, send volts.

## See it

```bash
./tools/frcprog sim
```

Then `./tools/frcprog scope` and File → Connect to Simulator. Plot height against
setpoint. The shape tells you which knob to turn:

| Shape | Fix |
|---|---|
| Rises and stops cleanly | done |
| Overshoots and comes back, repeatedly | more `kD`, or less `kP` |
| Creeps up and stalls short | more `kP`, or a little `kI` |
| Jitters constantly | less `kD` |

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**Honest limit:** these gains are for this mass and this gearing. Copying them to a
different elevator is how tuning becomes folklore. What transfers is the recipe and
being able to read the plot.
