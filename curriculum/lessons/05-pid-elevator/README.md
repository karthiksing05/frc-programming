# Lesson 05 — PID

**Stage 1B · 50 min · Needs: 04**

Tell a motor to run at 40% power until the elevator reaches 90 centimetres and it will
sail straight past, because a moving carriage does not stop the instant you stop pushing
it. Drop the power to 10% instead and it never gets there at all, because that is not
enough to lift its own weight.

There is no single power level that works, and that is the whole problem. What you need
is a rule that *changes* the power based on how far away you still are — a lot when the
gap is big, gently as it closes, nothing when you arrive. That rule is a PID controller,
and it is the single most reused idea in robot programming: every arm, elevator, turret,
shooter and drivetrain in FRC has one somewhere.

## Do this

**1. `subsystems/elevator/ElevatorSubsystem.java`** — find `TODO (LESSON 05)` in
`periodic()`. Three lines:

```java
double feedbackVolts = pid.calculate(getHeightMeters(), setpointMeters);
double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);
appliedVolts = volts;
motor.setVoltage(volts);
```

**2. `Constants.java`** — tune `Elevator.kP`, `kI`, `kD`. They start at zero, so the
carriage sits on the floor.

## Check it

```bash
./tools/frcprog check 05-pid-elevator
```

Five checks, grading a response over time:

- reaches low, mid and high within 2 cm
- gets there inside 1.5 s
- never overshoots by more than 5 cm, including on the way down
- holds position for two seconds afterwards

## How it works

### The problem with the obvious approach

Full power until you are close, then off. It is called bang-bang control.

The carriage arrives at 2 m/s and you cut power. It does not stop; it carries on,
because it has mass. It overshoots, you drive back, it overshoots the other way.
Eventually friction wins. On real hardware you can hear it.

The fix is to stop commanding full power and start commanding an amount that depends
on how far you have left to go.

### Error is the whole idea

```
error = setpoint - measurement
```

Far away, error is large. Close, error is small. At the target, zero.

Every term of a PID controller is a different function of that one number.

### kP, proportional

```java
output = kP * error
```

Push in proportion to how wrong you are. Half a metre out with `kP = 40` gives 20
volts, clamped to 12. Two centimetres out gives 0.8 V, a gentle nudge.

This does most of the work. It also cannot stop cleanly on its own, because at the
moment error hits zero the output hits zero, and the carriage is still moving.

### kD, derivative

```java
output = kD * (rate of change of error)
```

If error is shrinking fast, you are approaching fast, and `kD` pushes back. It is a
brake that engages based on speed rather than position.

This is what kills the oscillation `kP` creates.

Its weakness: "rate of change" is exactly what sensor noise looks like. A jittery
encoder makes `kD` produce jittery voltage. Too much and the motor buzzes.

### kI, integral

```java
output = kI * (sum of all past error)
```

Error that refuses to shrink accumulates, and the output grows until something
gives. It is the cure for parking two centimetres short forever.

Its weakness is wind-up. During a long travel the sum grows large, and when you
finally arrive that stored value is still pushing. The mechanism lurches past.

That makes it the term to reach for last and to use sparingly — usually only when a
mechanism settles just short of its target and refuses to close the final gap.

??? question "Predict: with kP = 40, kD = 0, what will the plot look like?"

    Fast rise, then overshoot, then a bounce back, then a smaller overshoot, decaying
    over a second or two.

    `kP` alone has nothing that responds to velocity, so nothing anticipates the
    stop. The only thing that eventually settles it is friction and the fact that
    each overshoot is smaller than the last.

    Try it before you read on. Set `kD = 0` and run the sim. Then set `kD = 6` and
    run it again. The difference is the clearest demonstration of what `kD` is for
    that you will get.

### The tuning recipe

1. All three at zero.
2. Raise `kP` until it arrives fast but bounces.
3. Raise `kD` until the bouncing stops.
4. Only if it parks short forever, add a little `kI`.

Move in **big steps**. Try 10, then 40, then back off. Nudging by 0.5 takes all
afternoon and teaches you nothing.

Heights here are in metres, so an error of "half a metre" is `0.5`. To get a useful
number of volts out of that, `kP` has to be large. If your units were centimetres
the same mechanism would want `kP` a hundred times smaller. **Gains are not
portable between unit systems**, and this is where most confused tuning comes from.

### The gravity term, already written for you

```java
double gravityVolts = Constants.Elevator.kG_HOLD;
```

An elevator weighs the same at every height, so one constant number of volts holds
it anywhere.

Without it, `kP` would have to fight gravity by *letting error exist*. The carriage
would sag until `kP × error` happened to equal the holding force, and it would sit
there, permanently a bit low.

This is your first feedforward: a term computed from what you know about the
physics, rather than from the error. Lesson 06 is an arm, where that number changes
with angle and you compute it yourself.

??? info "Why setVoltage and not set()"

    `motor.set(0.7)` means 70% of whatever the battery currently is.

    A fresh battery is 12.6 V, so that is 8.8 V. Late in a match under load it sags
    to 10.5 V, so the same line now delivers 7.35 V. Your carefully tuned `kP` is
    quietly wrong in the last thirty seconds, exactly when it matters.

    `setVoltage(8.4)` asks the controller for 8.4 volts and it compensates
    internally using its own measurement of the bus.

    Tune in volts, send volts. Every reference robot in this curriculum does.

## See it

This is the lesson where the plot stops being optional. Full walkthrough:
**[Running the simulator](../../../setup/simulator.md)**.

**Terminal 1:**

```bash
./tools/frcprog sim
```

**Terminal 2:**

```bash
./tools/frcprog scope
```

Then:

1. In AdvantageScope: **File → Connect to Simulator**
2. In the sidebar expand `NT` → `Elevator`
3. Drag the height signal onto the graph
4. Drag the setpoint onto the **same** graph
5. In the sim, click **Teleoperated** and press your elevator key

You are looking at a step response. Read it like this:

| What you see | What to change |
|---|---|
| Rises and stops cleanly | nothing, you are done |
| Overshoots and comes back, repeatedly | more `kD`, or less `kP` |
| Creeps up and stalls short | more `kP`, or a little `kI` |
| Fuzzy, constantly jittering | less `kD` |
| Flat at zero | nothing is reaching the motor. Check **PWM Outputs** channel 6. |

That last row is the one to check first. Before touching a gain, confirm voltage is
actually arriving.

??? example "Experiment: make each gain misbehave on purpose"

    Twenty minutes, and worth more than reading about it.

    | Set | Run | What you should see |
    |---|---|---|
    | `kP=5, kD=0` | check 05 | never arrives, stalls low |
    | `kP=200, kD=0` | sim | violent oscillation, may never settle |
    | `kP=40, kD=0` | sim | arrives fast, rings, settles slowly |
    | `kP=40, kD=6` | check 05 | arrives fast, stops clean |
    | `kP=40, kD=60` | sim | sluggish and fuzzy, over-damped |

    Watch the plot each time. After this you will recognise all four shapes on sight,
    which is the actual skill. The numbers are specific to this mechanism; the shapes
    are not.

## Done

The rubric passes, and the carriage now holds a height instead of falling to the
floor whenever you stop asking it to move.

```bash
./tools/frcprog next
```

**Honest limit.** These gains are for this mass, this gearing, this drum radius. Copy
them onto a different elevator and they will be wrong. What transfers is the recipe
and the ability to read the plot.

You also tuned by hand. Lesson 28 introduces SysId, which measures the mechanism and
computes the feedforward from data instead.
