# Lesson 06 — Gravity feedforward

**Stage 1B · 45 min · Needs: 05**

An arm fights hardest when horizontal and not at all when it points straight up.

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

Five checks. The last one verifies the holding voltage comes from feedforward rather
than from PID fighting an error, because a big enough `kP` passes a position check
while missing the point entirely.

## How it works

### Why cosine

Torque from gravity is force times lever arm.

```
        ↑ up (cos 90° = 0, no torque)
        |
        |
  ------+------→ horizontal (cos 0° = 1, maximum torque)
        |
        |
        ↓ down (cos -90° = 0, no torque)
```

The weight always pulls straight down. What changes with angle is the *horizontal
distance* from the pivot to the centre of mass, and that distance is
`length × cos(angle)`.

Straight out: full lever, maximum torque. Straight up or straight down: the mass is
directly above or below the pivot, zero lever, no torque at all.

So the voltage needed to cancel gravity is `kG × cos(angle)`. One constant scaled by
a number you recompute every loop from a sensor you already have.

### Feedforward versus feedback

This is the distinction the lesson is really about.

**Feedback** reacts to error. Something went wrong, so push back. It is general: it
works for disturbances you never predicted.

**Feedforward** acts on knowledge. You know gravity is pulling with this much torque
right now, so supply exactly that much voltage before any error appears.

Gravity is completely predictable. Making a feedback loop discover it fresh, 50
times a second, forever, is wasteful and it works badly.

The pattern is `output = feedforward + feedback`. Feedforward carries the known
load. Feedback cleans up what is left: friction, a bent bracket, a game piece you
picked up. You will see this shape in every mechanism from here on.

??? question "Predict: why cos(current angle) and not cos(setpoint)?"

    You are cancelling the torque acting on the arm **right now**. Not the torque
    that will act on it once it arrives.

    Using the setpoint looks correct, and once the arm has settled it *is* correct,
    because current angle and setpoint are then the same.

    It misbehaves during the move. Travelling from −45° up to +60°, the arm
    compensates the whole way for a force it will not feel until it gets there. It
    lags going up and rushes coming down.

    This is a whole category of bug: correct at steady state, wrong in transit.
    Steady-state testing will never find it. Watching the plot during a move will.

??? question "Predict: kP is already 12. Why not just raise it to 100?"

    It sort of works, and plenty of shipped robots do it. Three things happen.

    **The arm sags below every setpoint, permanently.** PID only produces holding
    voltage when there is error to multiply, so the error has to exist. Bigger `kP`
    makes the sag smaller, never zero.

    **Somebody adds `kI` to remove the sag.** Now the integral winds up during long
    travels and the arm lurches on arrival.

    **Every gain needs re-tuning** whenever the arm is somewhere unusual, because
    the disturbance it is fighting changes with angle and `kP` does not know that.

    Feedforward supplies the known force directly, so the feedback loop only handles
    what is genuinely unpredictable. That is the job it is good at.

### Where 0.12 comes from

Measurement, not arithmetic.

Command the arm to sit level with no feedback at all. Raise the voltage until it
stops falling. Write that number down. Done.

Calculate it instead, from mass and length and gear ratio and motor constants, and
you will be wrong. The real mechanism has gearbox friction, a cable carrier that
pulls, and a bearing somebody over-tightened. None of that is in your model.

Measure it on the real robot too, once you have one.

## See it

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

Connect, then plot the arm angle and its applied voltage on the **same** graph
(setup guide: **[Running the simulator](../../../setup/simulator.md)**).

Command the arm to horizontal. Voltage settles near 0.12.
Command it to 60° up. Holding voltage falls to about 0.06, because `cos(60°)` is 0.5.

That curve, voltage tracking the cosine of angle, is the whole lesson in one picture.

??? example "Experiment: watch feedforward fail"

    1. Set `kG = 0.0` and run `frcprog check 06-arm-gravity-ff`. It fails.
    2. Run the sim and plot angle against setpoint.
    3. Command level. Watch the arm settle a few degrees **below** and stay there.
    4. That gap is the gravity torque you are not cancelling. The PID has to let
       error build until `kP × error` happens to equal the holding voltage.
    5. Now set `kG = 0.12` and repeat. The gap closes.
    6. Set `kG = 0.5`, far too high, and watch it settle a few degrees **above**.

    Under-compensate and it sags. Over-compensate and it floats. Correct and it sits
    where you asked.

## Done

Rubric is green. Stage 1B complete.

```bash
./tools/frcprog next
```

**Where this goes.** Full feedforward for a mechanism is usually written:

```
volts = kS·sign(velocity) + kG·cos(angle) + kV·velocity + kA·acceleration
```

| Term | Cancels |
|---|---|
| `kS` | static friction, the voltage to break it loose |
| `kG` | gravity. You just did this one. |
| `kV` | the voltage that sustains a speed |
| `kA` | the extra voltage to change speed |

WPILib has `ArmFeedforward` and `ElevatorFeedforward` that bundle all four. Writing
one term by hand first is how that class stops being magic. Lesson 10 uses `kV` on
the flywheels, and lesson 28 measures all of them properly.
