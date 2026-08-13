# Lesson 04 — Subsystems

**Stage 1B · 45 min · Needs: 03**

Take yesterday's code apart. The pieces end up somewhere better.

## Do this

Three files, in this order.

**1. `subsystems/roller/RollerSubsystem.java`** — two TODOs.

`setMode(State desired)` stores the state. One line. It does not touch the motor.

`periodic()` turns state into motor output:

| State | Output |
|---|---|
| `OFF` | `0.0` |
| `INTAKING` | `INTAKE_SPEED`, or `0.0` if `hasGamePiece()` |
| `EJECTING` | `EJECT_SPEED`, always |

**2. `RobotContainer.java`** — make the roller real:

```java
private final RollerSubsystem roller = new RollerSubsystem();
```

**3. `Robot.java`** — delete the motor and beam-break fields. Replace
`teleopPeriodic()` with three or four lines that only say what the operator wants.

## Check it

```bash
./tools/frcprog check 04-subsystems-state-machines
```

Six checks. The first four are the same four scenarios as lesson 03, because the
robot's behaviour must not change during a refactor. Checks 5 and 6 are new: the
hardware fields must be `private final`, and `Robot.java` must no longer mention
`PWMSparkMax` or `DigitalInput`.

## How it works

### Deciding and acting are different jobs

`setMode` stores a value. `periodic` reads it and drives a motor. Splitting them
looks like extra ceremony until you notice what it enables.

**Deciding is instant and can happen from anywhere.** A button, an auto routine, a
sensor trigger. It costs one field write.

**Acting happens exactly once per loop, in one known place.** So there is precisely
one line in the codebase that writes to that motor, and it runs 50 times a second
whether or not anybody pressed anything.

That second property is what makes "stop when the beam breaks" work. The piece
arrives 400 ms after the button press. Nothing is pressing anything at that moment.
But `periodic` is still running, still checking, and it stops.

Put the motor write in `setMode` instead and the roller only reacts at the instant
somebody presses a button. The game piece arrives and nothing notices.

??? question "Predict: what runs periodic(), and how often?"

    The `CommandScheduler`, once per loop, from `Robot.robotPeriodic()`.

    `SubsystemBase`'s constructor registers the subsystem with the scheduler
    automatically. That is the whole reason you extend it. You never call
    `periodic()` yourself, and you never need a loop inside it.

    It runs in **every mode**, including disabled. That is why telemetry keeps
    updating while the robot sits on the field waiting for a match.

### Why the roller was null until now

WPILib allocates each PWM channel to exactly one object. While `Robot` owned a motor
on PWM 5, constructing a `RollerSubsystem` would have thrown at startup:

```
PWM 5 already allocated
```

That error is the framework refusing to let two things own one motor. It is worth
seeing at least once, because the underlying rule matters: **one piece of hardware,
one owner.**

Break that rule and you get a motor with two masters, each overwriting the other at
50 Hz, and behaviour that depends on which line ran last.

### What disappears, and why that is the point

The beam-break check leaves `Robot.java` entirely.

Not because it stopped mattering. Because "stop intaking once you have one" was
never the driver's decision. It is a fact about how rollers work, and it now lives
with the roller.

Ask yourself which file you would open to change that behaviour. Before: `Robot`,
which is also where the climber and the arm and the drive would be. After:
`RollerSubsystem`, which is 60 lines and about one mechanism.

??? info "Why an enum instead of booleans"

    The obvious alternative is `boolean isIntaking`. It works until you add
    ejecting, and then you have two booleans and four combinations:

    | isIntaking | isEjecting | Means |
    |---|---|---|
    | false | false | off |
    | true | false | intaking |
    | false | true | ejecting |
    | true | true | ??? |

    That fourth row describes a mechanism that cannot physically exist, and nothing
    stops you writing it.

    An enum has exactly three values and no fourth. A `switch` over one is also
    exhaustive: add a `JAMMED` state next season and the compiler shows you every
    place that now needs to handle it. Two booleans give you no such warning.

    This idea has a name worth knowing: **make illegal states unrepresentable.**

## See it

```bash
./tools/frcprog sim
```

Behaviour should be identical to lesson 03. Same keys, same PWM 5 numbers, same DIO
4 toggling. That is the success condition for a refactor.

What changed is which file you open when it misbehaves.

??? example "Experiment: prove periodic() is doing the work"

    1. Start the sim, enable Teleoperated, hold your intake key
    2. PWM 5 reads 0.6
    3. **Keep holding it.** Click DIO 4 to break the beam.
    4. PWM 5 drops to 0 while your finger is still down

    Nobody pressed anything at the moment it stopped. `periodic()` noticed.

    Now imagine implementing that with the motor write inside `setMode`. You would
    need something else polling the sensor and calling `setMode` again, which is
    `periodic()` with extra steps.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**A subsystem makes two promises.**

It owns its hardware. The fields are `private final`, so nothing outside can call
`motor.set()`. A roller bug is in one file, always.

It exposes intent, not mechanism. Callers say `setMode(INTAKING)`. They do not say
"0.6 unless the beam is broken". Deciding what 0.6 means is the roller's job.

Kelpie's `roller/` is this exact class on a competition robot, one refactor further
along. You do that refactor in lesson 16.
