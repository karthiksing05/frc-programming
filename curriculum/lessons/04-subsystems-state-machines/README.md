# Lesson 04 — Subsystems

**Stage 1B · 45 min · Needs: 03**

You are going to take the code from lesson 03 apart and move the pieces somewhere they
can be owned, tested and reused. This is called a **subsystem**: one class that owns
one mechanism's hardware and is the only thing allowed to command it.

The idea underneath it is that the roller should not be told *what speed to run*. It
should be told *what it is trying to do* — intaking, ejecting, or nothing — and left
to work out the speed itself. That is a **state machine**, and it is how essentially
every serious FRC codebase is organised.

## Do this

Three files, in this order.

**1. `subsystems/roller/RollerSubsystem.java`** has two TODOs.

`setMode(State desired)` records what the roller is being asked to do. It is one line,
and it deliberately does not touch the motor — it only stores the request, and
`periodic()` acts on it later.

`periodic()` is where state becomes motor output. WPILib calls it for you fifty times a
second, whatever else is going on. The shape you are filling in:

```java
public void periodic() {
  double output =
      switch (state) {
        case OFF      -> // nothing should be moving
        case INTAKING -> // pull a piece in, but not if one is already seated —
                         // hasGamePiece() tells you, and running the roller against
                         // a piece that is already in there just grinds it
        case EJECTING -> // spit it out; this one has no condition on it at all
      };

  lastOutput = output;      // recorded so the rubric can see what you decided
  motor.set(output);        // the single place this subsystem touches hardware
}
```

The `INTAKING` case is the interesting one, and it is the reason this class exists. The
"stop when loaded" rule now lives *inside* the roller, so no caller anywhere can forget
it. In lesson 03 that rule lived in `teleopPeriodic()`, which meant autonomous would
have had to remember it separately.

**2. `RobotContainer.java`** — create the subsystem so it exists and gets scheduled:

```java
private final RollerSubsystem roller = new RollerSubsystem();
```

**3. `Robot.java`** — delete the motor and beam-break fields entirely. Then rewrite
`teleopPeriodic()` so that it only reports what the operator is asking for, calling
`roller.setMode(...)` with one of the three `RollerSubsystem.State` values. Ask
`robotContainer.getRoller()` for the subsystem rather than making a new one — there
must only ever be one object holding that motor.

Keep the same X-beats-B priority you worked out in lesson 03.

When you are done, `teleopPeriodic()` should be about four lines, and not one of them
should mention a motor, a voltage or a sensor. Notice in particular what leaves this
file along with the hardware: the beam-break check. `Robot.java` no longer knows or
cares whether a game piece is loaded, because that was never the driver's decision to
make.

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

The beam-break check leaves `Robot.java` entirely, and not because it stopped
mattering. "Stop intaking once you have one" was never the driver's decision to make —
it is a fact about how rollers work, so it belongs with the roller.

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

The rubric passes, and the roller's behaviour is identical to lesson 03 — which is
exactly the point of a refactor. What changed is where the code lives.

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
