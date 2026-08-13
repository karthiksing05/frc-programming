# Lesson 09 — Composition

**Stage 1C · 40 min · Needs: 08**

"Spin up, wait for speed, then feed." One sentence, three commands, one ordering
constraint that matters.

## Do this

**1. `RobotContainer.scoreCommand()`:**

```java
return flywheels
    .spinUpCommand()
    .alongWith(
        Commands.waitUntil(flywheels::isReadyToShoot)
            .andThen(roller.ejectCommand().withTimeout(0.4)))
    .withTimeout(1.5)
    .withName("Score");
```

**2. Bind it:**

```java
operator.rightBumper().whileTrue(scoreCommand());
```

## Check it

```bash
./tools/frcprog check 09-command-composition
```

Four checks. Number 2 watches the whole run loop by loop and fails if the roller
fires before the flywheels are up to speed.

## The operators

| Operator | Means |
|---|---|
| `a.andThen(b)` | a, then b when a finishes |
| `a.alongWith(b)` | both at once, done when **both** finish |
| `a.raceWith(b)` | both at once, done when **either** finishes |
| `a.deadlineFor(b)` | both, done when **a** finishes |
| `a.until(cond)` | a until the condition is true |
| `a.withTimeout(t)` | a for at most t seconds |

`alongWith` here, because the flywheels must keep spinning *while* the roller
feeds. `andThen` would spin up, stop, then feed into slowing wheels.

## Two rules

**Never `Thread.sleep` or `Timer.delay`.** Robot code runs on one thread. Sleeping
stops every subsystem's `periodic()`, stops odometry, stops the watchdog being fed.
The robot goes deaf while the driver pushes a stick nothing is reading.
`Commands.waitSeconds` and `waitUntil` yield instead. Check 4 greps for both.

**Wait for the condition, not a duration.** `waitUntil(flywheels::isReadyToShoot)`,
not `waitSeconds(0.5)`. Half a second is right for one battery at one temperature.
Ask the mechanism and it is right every time.

**And bound it anyway.** `isReadyToShoot` depends on an encoder. If that wire comes
loose, `waitUntil` waits forever holding both subsystems. Anything that waits on a
sensor gets a timeout.

## Why it lives in RobotContainer

The sequence needs two subsystems. You could put `scoreWith(Flywheels f)` on the
roller. Do not. Once a subsystem holds a reference to another you cannot reason
about either alone, or test either alone, and the dependency graph grows edges
nobody drew.

## See it

```bash
./tools/frcprog sim
```

Plot `Flywheels/TargetRPM` and `ActualRPM`, hold the right bumper, watch the roller
kick in where the two traces meet.

## Done

Rubric is green.

```bash
./tools/frcprog next
```
