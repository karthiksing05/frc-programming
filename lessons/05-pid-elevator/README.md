# Lesson 05 — PID introduction (Elevator)

> **Stage 1B · ~50 minutes · Prerequisite: 04**

The roller had three states and each was a fixed motor speed. That works because a
roller does not care where it is.

An elevator cares enormously. "Go to L4" means arrive at a specific height and
*stay* there — not five centimetres short, not five past, not oscillating around it
until the battery dies.

The obvious approach is full power until you are close, then off. It is called
bang-bang control and it does exactly what it sounds like: the carriage slams into
the target, overshoots, slams back, and settles only when friction happens to win.
On a real robot you can hear it.

PID is the standard answer. It has a reputation for being mathematical and hard.
For the mechanisms you will meet in the next two seasons, it is a recipe, and today
you will follow the recipe.

## What you'll learn

1. Wire a `PIDController` into a subsystem.
2. Tune `kP`, `kI`, `kD` by reading a step response instead of guessing.
3. Read a sensor each cycle and act on it.
4. Send **volts**, not throttle — and explain why that distinction is real.

## What you'll do

Open `src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java`.

`periodic()` has a TODO. Three steps:

```java
double feedbackVolts = pid.calculate(getHeightMeters(), setpointMeters);
double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);
appliedVolts = volts;
motor.setVoltage(volts);
```

`pid.calculate(measurement, setpoint)` is the whole controller: you tell it where
you are and where you want to be, it tells you how hard to push.

Then open `Constants.Elevator` and tune `kP`, `kI`, `kD`. They start at zero, which
means the controller does nothing and the carriage sits on the floor.

### The three knobs

Everything a PID controller does is a reaction to *error* — how far the mechanism is
from where you asked for it to be.

- **`kP` — proportional.** *"Push in proportion to how wrong I am."* Far away, push
  hard; close, push gently. This is the workhorse and most of your tuning happens
  here. Too little and you never arrive. Too much and you arrive too fast and
  overshoot.

- **`kD` — derivative.** *"How fast is the error shrinking?"* If the carriage is
  racing toward the setpoint, `kD` brakes early. It is the cure for the oscillation
  a big `kP` causes. Too much and sensor noise becomes motor jitter, because noise
  looks like very fast change.

- **`kI` — integral.** *"How long have I been a little bit wrong?"* Error that
  refuses to shrink accumulates, and the controller pushes progressively harder.
  Fixes a carriage that parks two centimetres short forever. Easy to overdo: too
  much `kI` winds up while the mechanism is travelling and then overshoots hard when
  it finally arrives.

### The recipe

1. All three at zero.
2. Raise `kP` until the carriage arrives quickly but rings — bounces past and back.
3. Raise `kD` until the ringing damps out.
4. Only if it still parks short and stays there, add a sliver of `kI`.

Big steps first — try 10, then 40, then 20 if 40 was too much. Nudging by 0.5 from
zero will take all afternoon.

### The gravity term, and why it is free today

`gravityVolts` is already supplied. It is the constant voltage needed to hold this
carriage against gravity, and it is constant because an elevator weighs the same at
every height.

Without it, `kP` would have to fight gravity by *letting error exist* — the carriage
sags until `kP × error` happens to equal the holding force. That is a controller
permanently surprised by a force that never changes.

Lesson 06 is an arm, where the gravity term depends on angle, and you will compute
it yourself.

### Volts, not throttle

WPILib lets you write either `motor.set(0.7)` or `motor.setVoltage(8.4)`. They look
interchangeable. They are not.

`set(0.7)` means "70% of whatever the battery currently is". A fresh battery is
12.6 V, so that is 8.8 V. Halfway through a hard match it sags to 10.5 V, so the
same line now delivers 7.35 V. Your carefully tuned `kP` is quietly wrong, in the
last thirty seconds, when it matters.

`setVoltage(8.4)` asks the motor controller for 8.4 volts and it compensates
internally. **Tune in volts, send volts.** Every reference robot in this curriculum
does.

## Run it

```bash
./tools/frcprog check 05-pid-elevator
```

Five checks, and unlike earlier lessons they grade a *response over time*:

1–3. Reaches the low, mid, and high setpoints within 2 cm, inside 1.5 s.
4. Survives a full four-setpoint sweep, including coming back down, without
   overshooting more than 5 cm.
5. Holds position for two seconds afterwards without drifting.

Check 4 is the hard one. Going up is easy — gravity helps you stop. Coming down,
gravity is adding to the thing you are trying to arrest.

## See it

```bash
./tools/frcprog sim
```

In another terminal, `./tools/frcprog scope`, then **File → Connect to Simulator**.

Plot the elevator height against its setpoint on one chart. What you are looking at
is a *step response*, and its shape tells you which knob to turn:

- rises and stops cleanly → you are done
- rises past and comes back, repeatedly → too much `kP`, or not enough `kD`
- creeps up and stalls short → not enough `kP`, or you need a little `kI`
- jitters constantly → too much `kD`

Learning to read that picture is worth more than any amount of PID theory at this
stage.

## Done?

```bash
./tools/frcprog next
```

## Honest limits of what you just learned

The gains you found are for **this** mechanism, at **this** mass, with **this**
gearing. Copying them onto a different elevator is how tuning turns into folklore.
What transfers is the recipe and the ability to read the plot.

You also tuned by hand. Lesson 28 introduces SysId, which measures the mechanism and
computes gains from data — the difference between guessing well and knowing.
