# Lesson 16 — The IO Layer pattern

> **Stage 2A · ~60 minutes · Prerequisite: 15**

Your `Drive` subsystem currently does two unrelated jobs. It decides things —
arcade mixing, deadbands, odometry — and it talks to hardware, including a
`simulationPeriodic()` that runs a physics model.

Those are different jobs and they change for different reasons. Today you separate
them, and three useful things fall out.

This is the pattern that 6328, 8033, 254 and most other serious teams use. It is the
single most consequential structural decision in modern FRC code.

## What you'll learn

1. Define a hardware boundary as an interface plus an inputs struct.
2. Implement the same interface twice: physics, and real hardware.
3. Test a mechanism with no HAL, no ports, and no scheduler.
4. Understand why this is the precondition for log replay.

## What you'll do

Three files are already stubbed. `DriveIO.java` (the interface) and
`DriveIOReal.java` (real hardware) are complete — read them. Your job is
`DriveIOSim.updateInputs`:

```java
physics.setInputs(leftVolts, rightVolts);
physics.update(Constants.LOOP_PERIOD_SECONDS);

inputs.leftPositionMeters = physics.getLeftPositionMeters();
inputs.rightPositionMeters = physics.getRightPositionMeters();
inputs.leftVelocityMetersPerSec = physics.getLeftVelocityMetersPerSecond();
inputs.rightVelocityMetersPerSec = physics.getRightVelocityMetersPerSecond();
inputs.leftAppliedVolts = leftVolts;
inputs.rightAppliedVolts = rightVolts;
inputs.gyroYawRadians = physics.getHeading().getRadians();
```

### The shape

```
Drive.java          logic: mixing, odometry, commands.  Knows nothing about hardware.
  │
  │  DriveIO  ◄──── the boundary: a struct in, two voltages out
  │
  ├── DriveIOSim    a physics model
  └── DriveIOReal   motor controllers, encoders, a gyro
```

Above the line: what the robot does. Below: what it is made of. The interface is the
contract, and it is deliberately tiny — read everything into a struct, write two
voltages.

### Read the two implementations side by side

Open `DriveIOSim.java` and `DriveIOReal.java` next to each other.

Almost nothing in common. Different fields, different libraries, different failure
modes. One owns a differential-drive model; the other owns PWM channels and DIO
channels and can only exist once because the HAL allocates those channels
exclusively.

And yet: the same two methods, the same struct, the same units. That shared surface
is the entire value of the pattern.

### Three things this buys you

**Simulation stops being a special case.** `DriveIOSim` is not a mode the subsystem
switches into — it is a different object implementing the same interface. `Drive`
cannot tell, and therefore cannot have a sim-only bug. Compare that with the
`simulationPeriodic()` still sitting in `Drive.java`: a method that only runs
sometimes, whose absence changes behaviour.

**Swapping vendors is a one-file change.** Kraken this season, SparkFlex next:
write a new implementation, change the line that constructs it. Presto ships
`FlywheelsIOKrakenFOC` and `FlywheelsIOSparkFlex` side by side for exactly this
reason.

**Log replay becomes possible.** If *every* sensor reading the subsystem sees arrives
through `updateInputs`, then recording those readings is a complete recording of the
subsystem's world. Feed them back later and your control logic re-runs exactly,
including a bug that happened once, in a match, three days ago. That is lesson 19,
and it is the payoff.

That last one is why the discipline is absolute: **any sensor read that sneaks
around the interface silently breaks replay.** A stray `gyro.getAngle()` inside
`Drive` compiles, works, and quietly makes your logs unreplayable.

### Why the struct is mutable

```java
void updateInputs(DriveIOInputs inputs);
```

It fills a struct you pass in, rather than returning a new one. That looks
old-fashioned. It is: allocating a fresh object fifty times a second, for every
subsystem, all match, gives the garbage collector work to do at unpredictable
moments — and an unpredictable pause during autonomous is not a theoretical concern.

AdvantageKit generates a class exactly like `DriveIOInputs` from an `@AutoLog`
annotation. Writing one by hand, once, is how the generated version stops being
magic.

## Run it

```bash
./tools/frcprog check 16-io-layer
```

Six checks:

1. Voltage in, motion out.
2. Velocity is reported, not just position.
3. Applied voltage is reported back.
4. Opposite voltages spin the robot and the gyro says so.
5. Zero volts coasts to a stop.
6. Both implementations satisfy the same interface — driven through the interface
   type, so the test genuinely cannot tell them apart.

**Look at what this test does not need.** No `Drive`. No `RobotContainer`. No
scheduler, no ports, no `HAL` juggling. It constructs an object, calls two methods,
and reads a struct. That is the practical difference the pattern makes, and it is
why IO-layer code is so much easier to test than code that reaches for a motor
controller directly.

## See it

```bash
./tools/frcprog sim
```

Nothing visibly changes — you have not altered `Drive`. What changed is that the
hardware boundary now has a name, and the next three lessons build on it.

## Done?

```bash
./tools/frcprog next
```

## Where this goes, and what it costs

**Lesson 17** puts a `Mechanism2d` on the AdvantageScope field so you can watch the
articulation rather than read numbers.
**Lesson 18** makes logging systematic instead of ad hoc.
**Lesson 19** is replay, and it needs a vendor library — see
`lessons/EXTENSIONS.md`.

An honest note: the IO layer is not free. It is one more interface, one more file
per implementation, and one more indirection between "the code says setVoltage" and
"a motor moves". For a two-motor kitbot in a rookie team's first season, that
overhead may genuinely not pay for itself.

It pays when you have five subsystems, two people who each know one of them, a
competition in three weeks, and a bug that happened once. That is most of why
serious teams adopt it, and why this curriculum puts it in Stage 2 rather than
Stage 1: you have to have felt the problem.
