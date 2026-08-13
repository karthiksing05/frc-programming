# Lesson 16 — The IO layer

**Stage 2A · 60 min · Needs: 15**

Your `Drive` does two unrelated jobs. Separate them.

## Do this

Open `subsystems/drive/DriveIOSim.java` and fill in `updateInputs`:

```java
physics.setInputs(leftVolts, rightVolts);
physics.update(Constants.LOOP_PERIOD_SECONDS);

inputs.leftPositionMeters        = physics.getLeftPositionMeters();
inputs.rightPositionMeters       = physics.getRightPositionMeters();
inputs.leftVelocityMetersPerSec  = physics.getLeftVelocityMetersPerSecond();
inputs.rightVelocityMetersPerSec = physics.getRightVelocityMetersPerSecond();
inputs.leftAppliedVolts          = leftVolts;
inputs.rightAppliedVolts         = rightVolts;
inputs.gyroYawRadians            = physics.getHeading().getRadians();
```

Order matters. Step the model, then read it. Reading first reports last loop's
state and puts your control loop a cycle behind.

`DriveIO.java` and `DriveIOReal.java` are already written. Read them.

## Check it

```bash
./tools/frcprog check 16-io-layer
```

Six checks. **Notice what the test does not need:** no `Drive`, no
`RobotContainer`, no scheduler, no ports. It constructs an object, calls two
methods, reads a struct. That is the practical difference the pattern makes.

## The shape

```
Drive.java          logic: mixing, odometry, commands. Knows no hardware.
  │
  │  DriveIO   ◄──  the boundary: a struct in, two voltages out
  │
  ├── DriveIOSim    a physics model
  └── DriveIOReal   motor controllers, encoders, a gyro
```

Open the two implementations side by side. Almost nothing in common: different
fields, different libraries, different failure modes. And the same two methods, the
same struct, the same units. That shared surface is the whole value.

## What it buys you

**Simulation stops being a special case.** `DriveIOSim` is not a mode the subsystem
switches into. It is a different object behind the same interface. `Drive` cannot
tell, so it cannot have a sim-only bug.

**Swapping vendors is one file.** Presto ships `FlywheelsIOKrakenFOC` and
`FlywheelsIOSparkFlex` side by side for exactly this reason.

**Log replay becomes possible.** If every reading arrives through `updateInputs`,
recording them records the subsystem's whole world. Feed them back and your logic
re-runs exactly, including a bug that happened once, three days ago. That is
lesson 19.

Which is why the discipline is absolute: **a sensor read that bypasses the
interface silently breaks replay.** A stray `gyro.getAngle()` inside `Drive`
compiles and works and quietly makes your logs unreplayable.

## Why the struct is mutable

It fills a struct you pass in rather than returning a new one. Allocating a fresh
object fifty times a second for every subsystem gives the garbage collector work at
unpredictable moments, and an unpredictable pause during autonomous is not
theoretical.

AdvantageKit generates this class from an `@AutoLog` annotation. Writing one by
hand once is how the generated version stops being magic.

## See it

```bash
./tools/frcprog sim
```

Nothing changes. You have not altered `Drive`. What changed is that the hardware
boundary now has a name.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**Honest note:** the IO layer is not free. One more interface, one file per
implementation, one more indirection. For a two-motor kitbot in a rookie season it
may not pay for itself. It pays when you have five subsystems, two people who each
know one, a competition in three weeks, and a bug that happened once.
