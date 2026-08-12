# Hints — Lesson 16

## Hint 1 — Where to start

Read `DriveIOReal.java` first. It is complete, it is short, and it does exactly what
you are about to do — read hardware, fill the struct.

Then read `DriveIO.DriveIOInputs`. Every field is a slot you need to fill. Filling
some and not others is the most common way to half-finish this lesson.

## Hint 2 — The shape of the answer

Two phases, in this order:

```java
// 1. advance the model
physics.setInputs(leftVolts, rightVolts);
physics.update(Constants.LOOP_PERIOD_SECONDS);

// 2. report what it now says
inputs.leftPositionMeters = /* ... */;
// ...and the other six fields
```

Order matters: stepping the model after reading it reports last loop's state, and
your control loop is quietly one cycle behind.

The `physics` object is a `DifferentialDrivetrainSim`. Its getters are named the way
you would guess: `getLeftPositionMeters()`, `getLeftVelocityMetersPerSecond()`,
`getHeading()`.

## Hint 3 — Almost there

**Check 1 fails, everything zero:** either `physics.update(...)` is missing, or the
inputs struct is not being written to at all.

**Check 3 fails:** `leftAppliedVolts` and `rightAppliedVolts` come from the fields
`setVoltage` stored — not from the physics model, which has no opinion about what
you asked for.

**Check 4 fails:** `getHeading()` returns a `Rotation2d`. The struct wants radians,
so `.getRadians()`.

**Check 6 fails but 1–5 pass:** that check runs the *same calls* through both
implementations. If `DriveIOSim` works and the combined check does not, look at
whether both are reporting `leftAppliedVolts`.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
@Override
public void updateInputs(DriveIOInputs inputs) {
    physics.setInputs(leftVolts, rightVolts);
    physics.update(Constants.LOOP_PERIOD_SECONDS);

    inputs.leftPositionMeters = physics.getLeftPositionMeters();
    inputs.rightPositionMeters = physics.getRightPositionMeters();
    inputs.leftVelocityMetersPerSec = physics.getLeftVelocityMetersPerSecond();
    inputs.rightVelocityMetersPerSec = physics.getRightVelocityMetersPerSecond();
    inputs.leftAppliedVolts = leftVolts;
    inputs.rightAppliedVolts = rightVolts;
    inputs.gyroYawRadians = physics.getHeading().getRadians();
}
```

**Notice where the physics runs.** Inside `updateInputs`, not in a
`simulationPeriodic()` on the subsystem.

That relocation is the whole lesson. Open `Drive.java` and look at the
`simulationPeriodic()` still sitting there — thirty lines of `EncoderSim` and
`AnalogGyroSim` bookkeeping, inside a subsystem that has no business knowing
simulation exists. `DriveIOSim` is what replaces it.

**Going further, if you want to:** the natural next step is to actually refactor
`Drive` to hold a `DriveIO` — give it a constructor taking one, replace the encoder
reads with `inputs.leftPositionMeters`, delete `simulationPeriodic()` entirely, and
have the no-argument constructor pick `DriveIOSim` or `DriveIOReal` based on
`RobotBase.isSimulation()`.

Nothing grades that, and everything from lesson 07 onward should keep passing if you
do it correctly — which makes it an excellent exercise, because you have a test suite
that will tell you the truth.

</details>
