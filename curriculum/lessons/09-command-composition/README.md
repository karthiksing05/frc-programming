# Lesson 09 — Command composition

> **Stage 1C · ~40 minutes · Prerequisite: 08**

"Spin the shooter up, wait until it is actually at speed, then feed a game piece in,
then stop."

One sentence. Four things, two subsystems, and an ordering constraint that matters:
feed the piece early and it dribbles off the front bumper at 400 RPM.

No single command does that. Composition does.

## What you'll learn

1. Combine commands with `andThen`, `alongWith`, and `withTimeout`.
2. Wait on a *condition* with `Commands.waitUntil`, and on *time* with
   `Commands.waitSeconds`.
3. Never write `Thread.sleep` in robot code, and be able to say why.
4. Put multi-subsystem sequences in `RobotContainer`, not inside a subsystem.

## What you'll do

Fill in `RobotContainer.scoreCommand()`:

```java
return flywheels
    .spinUpCommand()
    .alongWith(
        Commands.waitUntil(flywheels::isReadyToShoot)
            .andThen(roller.ejectCommand().withTimeout(0.4)))
    .withTimeout(1.5)
    .withName("Score");
```

and bind it:

```java
operator.rightBumper().whileTrue(scoreCommand());
```

Read the composition aloud: *"spin the flywheels up, and alongside that, wait until
they are ready and then run the roller for four tenths of a second — and give the
whole thing a second and a half before giving up."*

That is the sentence at the top of this page, in Java, with the same structure.

### The operators

| Operator | Means |
|---|---|
| `a.andThen(b)` | run a; when it finishes, run b |
| `a.alongWith(b)` | run both at once; done when **both** finish |
| `a.raceWith(b)` | run both at once; done when **either** finishes |
| `a.deadlineFor(b)` | run both; done when **a** finishes, whatever b is doing |
| `a.until(cond)` | run a until the condition becomes true |
| `a.withTimeout(t)` | run a for at most t seconds |

`alongWith` is the right choice here because the flywheels must keep spinning *while*
the roller feeds. `andThen` would spin up, stop, and then feed into wheels that are
already slowing down.

### Waiting without blocking

This is a rule, not a preference:

> **Never call `Thread.sleep` or `Timer.delay` in robot code.**

Robot code runs on one thread. Sleeping stops every subsystem's `periodic()`, stops
odometry updating, stops NetworkTables publishing, and stops the watchdog being fed.
The robot goes deaf for the duration. Meanwhile the driver is pushing a stick that
nothing is reading.

`Commands.waitSeconds(t)` and `Commands.waitUntil(cond)` do not block. They are
commands that report "not finished yet" each loop and let everything else carry on.
Rubric check 4 greps your source for the blocking versions.

### Wait for the condition, not for a duration

`waitUntil(flywheels::isReadyToShoot)` rather than `waitSeconds(0.5)`.

Half a second is right for one battery at one temperature at one target speed. On a
sagging battery, spin-up takes longer and the shot goes short. Ask the mechanism
whether it is ready and it is right every time.

### And bound it anyway

`.withTimeout(1.5)` on the whole thing. `isReadyToShoot` depends on an encoder; if
that encoder's wire comes loose mid-match, `waitUntil` waits forever, holding both
subsystems, until somebody power-cycles the robot.

**Anything that waits on a sensor gets a timeout.** A broken sensor should cost you
one scoring cycle, not the remaining ninety seconds of the match.

### Why this lives in `RobotContainer`

The sequence needs the flywheels *and* the roller. You could put a
`scoreWith(Flywheels f)` method on the roller. Do not.

The moment a subsystem holds a reference to another subsystem, you can no longer
reason about either in isolation, you cannot test either without the other, and the
dependency graph starts growing edges nobody drew on purpose. Cross-subsystem
behaviour lives in one file — this one — where you can see all of it at once.

## Run it

```bash
./tools/frcprog check 09-command-composition
```

Four checks:

1. The sequence is actually built.
2. **The roller does not fire until the flywheels are up to speed.** (Graded by
   watching the whole run loop by loop, not by looking at the end state.)
3. The whole sequence gives up on its own.
4. No `Thread.sleep` or `Timer.delay` anywhere.

## See it

```bash
./tools/frcprog sim
```

Plot `Flywheels/TargetRPM` and `Flywheels/ActualRPM` together, and hold the right
bumper. Watch actual climb toward target, and watch the roller kick in at the moment
they meet. That gap between the two traces is your spin-up time — and lesson 10 is
where you learn to read it properly.

## Done?

```bash
./tools/frcprog next
```
