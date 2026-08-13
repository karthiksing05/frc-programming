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

Order matters. Step the model, then read it. Reading first reports last loop's state
and puts your control loop a cycle behind.

`DriveIO.java` and `DriveIOReal.java` are written already. Read them both.

## Check it

```bash
./tools/frcprog check 16-io-layer
```

Six checks. **Notice what the test does not need:** no `Drive`, no
`RobotContainer`, no scheduler, no HAL ports. It constructs an object, calls two
methods, reads a struct.

## How it works

### The two jobs

Right now `Drive` decides things (arcade mixing, deadbands, odometry) and talks to
hardware (motors, encoders, a gyro, plus a `simulationPeriodic` running physics).

Those change for different reasons. Mixing changes when a driver asks for different
handling. Hardware changes when mechanical swaps a motor. Mixing them means every
change touches one large file.

```
Drive.java          logic. Knows no hardware.
  │
  │  DriveIO   ◄──  the boundary: a struct in, two voltages out
  │
  ├── DriveIOSim    a physics model
  └── DriveIOReal   motor controllers, encoders, a gyro
```

Open the two implementations side by side. Almost nothing in common: different
fields, different libraries, different failure modes. `DriveIOSim` allocates no HAL
channels at all, so several can exist at once; `DriveIOReal` can only exist once.

And the same two methods, the same struct, the same units. That shared surface is
the whole value.

### Three things it buys you

**Simulation stops being a special case.** `DriveIOSim` is not a mode the subsystem
switches into. It is a different object behind the same interface. `Drive` cannot
tell which one it has, so it cannot have a sim-only bug.

Compare against the `simulationPeriodic()` still sitting in `Drive.java`: a method
that runs only sometimes, whose presence changes behaviour.

**Swapping vendors is one file.** Kraken this season, SparkFlex next. Write a new
implementation, change the line that constructs it, delete nothing. Presto ships
`FlywheelsIOKrakenFOC` and `FlywheelsIOSparkFlex` side by side for exactly this.

**Log replay becomes possible.** If every reading arrives through `updateInputs`,
recording them records the subsystem's entire world. Feed them back and your logic
re-runs exactly, including a bug that happened once, in a match, three days ago.
Lesson 19.

??? question "Predict: what breaks replay, and why is it silent?"

    One sensor read that bypasses the interface:

    ```java
    // inside Drive.periodic()
    if (gyro.getAngle() > 90) { ... }
    ```

    That compiles. It works on the robot. And it quietly makes every log
    unreplayable, because during replay there is no gyro and that call returns
    something unrelated to the recorded match.

    Nothing warns you. The replay runs, produces numbers, and they are wrong.

    Replay is all or nothing, which is why lesson 16's discipline is stated
    absolutely rather than as a preference.

??? info "Why the struct is mutable, and filled rather than returned"

    ```java
    void updateInputs(DriveIOInputs inputs);      // fills yours
    DriveIOInputs updateInputs();                 // would allocate a new one
    ```

    The second reads better. It also allocates an object 50 times a second, for
    every subsystem, for the whole match.

    Java's garbage collector will handle that, at a moment of its choosing. An
    unpredictable pause of a few milliseconds during autonomous is not a theoretical
    concern; it is a robot that misses a shot.

    Filling a struct you already own allocates nothing. This is one of the few
    places in robot code where that trade is worth making explicitly, and it is why
    the pattern looks old-fashioned.

    AdvantageKit generates a class exactly like `DriveIOInputs` from an `@AutoLog`
    annotation. Writing one by hand once is how the generated version stops being
    magic.

## See it

Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
```

Nothing visibly changes. You have not altered `Drive`, only filled in a class it
does not use yet.

What the rubric proves is more interesting than what the sim shows: `DriveIOSim`
works standalone, with no robot around it.

??? example "Experiment: go the rest of the way"

    Nothing grades this, and it is the best exercise in Stage 2A.

    Actually refactor `Drive` to use the interface:

    1. Give it a constructor taking a `DriveIO`, and keep a `DriveIOInputs` field
    2. Call `io.updateInputs(inputs)` at the top of `periodic()`
    3. Replace every encoder and gyro read with `inputs.leftPositionMeters` and
       friends
    4. Replace `setVoltage` internals with `io.setVoltage(...)`
    5. Delete `simulationPeriodic()` entirely
    6. Make the no-argument constructor pick:
       `this(RobotBase.isSimulation() ? new DriveIOSim() : new DriveIOReal())`

    Then run `./tools/frcprog check --all`. Every lesson from 07 onward should still
    pass.

    You have a test suite that will tell you the truth, which is exactly the
    situation refactoring is safe in.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**Honest note.** The IO layer is not free: one more interface, one file per
implementation, one more indirection between "the code says setVoltage" and "a motor
moves". For a two-motor kitbot in a rookie team's first season it may genuinely not
pay for itself.

It pays when you have five subsystems, two people who each know one of them, a
competition in three weeks, and a bug that happened once. That is why this
curriculum puts it in Stage 2A rather than Stage 1: you have to have felt the
problem first.
