# Lesson 28 — System identification

**Stage 2D · 60 min · Needs: 27**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Every gain so far, you guessed. SysId measures the mechanism and computes them.

## Do this

**1. Add a routine** to one subsystem. The elevator is a good first choice.

```java
private final SysIdRoutine sysId =
    new SysIdRoutine(
        new SysIdRoutine.Config(),
        new SysIdRoutine.Mechanism(
            volts -> motor.setVoltage(volts.in(Volts)), this::logMotorState, this));
```

**2. Bind four commands** to four buttons: quasistatic forward and reverse, dynamic
forward and reverse.

**3. Run each in simulation**, save the log, open it in WPILib's SysId tool. It
wants position, velocity and applied voltage, so make sure you log all three.

**4. Plug the results** into your feedforward constants and watch tracking error
shrink.

## The two tests

**Quasistatic** ramps voltage very slowly, so acceleration is negligible. What is
left is the relationship between voltage and *velocity*, giving `kS` (voltage to
overcome static friction) and `kV` (volts per unit velocity).

**Dynamic** applies a voltage step, so acceleration dominates. That gives `kA`.

Run each in both directions. A mechanism that behaves differently up and down
usually has gravity or one-way friction you have not accounted for.

## What it gives you and what it does not

SysId measures **feedforward**: `kS`, `kV`, `kA`, and `kG` for gravity-loaded
mechanisms.

It does **not** measure `kP`, `kI`, `kD`. Feedback gains depend on how you want the
mechanism to behave, which is a choice, not a property of the hardware.

The consequence: with good feedforward, feedback has very little left to do and the
gains you need become small and easy. Most of "PID tuning is hard" is actually "we
never measured the feedforward".

## Watch out for

**Hitting a hard stop mid-test**, which corrupts the data. Add explicit limits.

**Not logging applied voltage**, so the tool has nothing to fit against.

**Treating simulation results as real.** In sim the physics model and the
feedforward model are the same model, so SysId recovers exactly the constants that
were fed in. It proves your wiring, not your robot.

## Done

You ran all four routines, extracted constants from the data, and saw tracking
error shrink.

```bash
./tools/frcprog next
```
