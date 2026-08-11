# Lesson 28 — System identification (SysId)

> **Stage 2D · ~60 minutes · Prerequisite: 27-motion-profiling**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Every gain you have set so far, you guessed. You guessed well — the recipe works — but
`kG = 0.12` came from a measurement in a probe and `kV_LINEAR = 2.4` came from
arithmetic about free speed.

SysId measures the mechanism and computes the gains from data.

## What you'll learn

1. Wire a `SysIdRoutine` into a subsystem.
2. Run quasistatic and dynamic tests.
3. Read `kS`, `kV`, `kA` out of the results.
4. Know which gains SysId gives you and which it does not.

## What you'll do

```java
private final SysIdRoutine sysId =
    new SysIdRoutine(
        new SysIdRoutine.Config(),
        new SysIdRoutine.Mechanism(
            volts -> motor.setVoltage(volts.in(Volts)), this::logMotorState, this));

public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
  return sysId.quasistatic(direction);
}

public Command sysIdDynamic(SysIdRoutine.Direction direction) {
  return sysId.dynamic(direction);
}
```

Bind the four routines to buttons, run each, and analyse the log.

### The two tests

**Quasistatic** ramps voltage up very slowly, so acceleration is negligible. What is
left is the relationship between voltage and *velocity*, which gives you `kS` (the
voltage needed to overcome static friction) and `kV` (volts per unit velocity).

**Dynamic** applies a voltage step, so acceleration dominates. That gives `kA`.

Run each in both directions to catch asymmetry — a mechanism that behaves differently
up and down usually has gravity or a one-way friction source you have not accounted
for.

### What SysId gives you and what it does not

SysId measures **feedforward**: `kS`, `kV`, `kA`, and `kG` for gravity-loaded
mechanisms.

It does not measure `kP`, `kI`, `kD`. Feedback gains depend on how you want the
mechanism to *behave* — aggressive or gentle, fast or smooth — which is a choice, not
a property of the hardware. The tool computes suggestions; you still choose.

The important consequence: with good feedforward, feedback has very little left to
do, and the gains you need become small and easy. Most of what people call "PID
tuning is hard" is actually "we never measured the feedforward".

## Done?

You have run all four routines, extracted feedforward constants from the data, and
seen your tracking error shrink when you use them.

```bash
./tools/frcprog next
```
