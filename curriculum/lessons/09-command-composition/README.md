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

## How it works

### Compositions are commands

`a.andThen(b)` does not run anything. It returns a **new Command** that, when
scheduled, runs `a` then `b`.

That is why they nest arbitrarily. A composition is a command, so it can be an
argument to another composition. The whole tree is scheduled as one unit and
cancelled as one unit.

### The operators

| Operator | Runs | Finishes when |
|---|---|---|
| `a.andThen(b)` | a, then b | b finishes |
| `a.alongWith(b)` | both at once | **both** finish |
| `a.raceWith(b)` | both at once | **either** finishes |
| `a.deadlineFor(b)` | both at once | **a** finishes |
| `a.until(cond)` | a | cond becomes true |
| `a.withTimeout(t)` | a | t seconds elapse |

Read the one you wrote aloud: *spin the flywheels up, and alongside that, wait until
they are ready and then run the roller for four tenths of a second, and give the
whole thing a second and a half.*

That is the sentence at the top of this page, with the same structure.

??? question "Predict: why alongWith and not andThen for the outer step?"

    ```java
    return flywheels.spinUpCommand()
        .andThen(roller.ejectCommand().withTimeout(0.4));
    ```

    `andThen` waits for the first command to **finish**. `spinUpCommand()` never
    finishes on its own; it holds the target until cancelled.

    So the roller step is never reached. The shooter spins forever and nothing is
    ever fed.

    Even if it did finish, you would then be feeding a game piece into wheels that
    had already stopped being commanded and were slowing down.

    `alongWith` keeps the flywheels running **during** the feed, which is what the
    mechanism needs.

### Requirements compose too

The composition requires the union of what its parts require: flywheels and roller.

If anything else needs either subsystem while this is running, the scheduler
cancels the whole composition rather than letting half of it continue. That is
usually what you want, and it is worth knowing it is happening.

### Never block the thread

Robot code runs on **one** thread.

```java
Thread.sleep(500);   // never
Timer.delay(0.5);    // never
```

Sleeping stops every subsystem's `periodic()`, stops odometry updating, stops
NetworkTables publishing, and stops the watchdog being fed. The robot goes deaf for
half a second while the driver pushes a stick nothing is reading.

`Commands.waitSeconds(t)` and `Commands.waitUntil(cond)` are commands that report
"not finished yet" each loop and let everything else carry on. Check 4 greps your
source for both blocking versions.

### Wait for the condition, not the clock

`waitUntil(flywheels::isReadyToShoot)`, not `waitSeconds(0.5)`.

Half a second is right for one battery, at one temperature, at one target speed. On
a sagging battery spin-up takes longer and the shot goes short. Asking the mechanism
is right every time.

??? info "But bound it anyway"

    `isReadyToShoot` depends on an encoder. If that wire comes loose mid-match,
    `waitUntil` waits forever, holding both subsystems, until somebody power-cycles
    the robot.

    **Anything that waits on a sensor gets a timeout.** A broken sensor should cost
    you one scoring cycle, not the rest of the match.

    That is what `.withTimeout(1.5)` on the outside is doing. It is not there for the
    happy path.

### Why this lives in RobotContainer

The sequence needs two subsystems. You could put `scoreWith(Flywheels f)` on the
roller instead. Do not.

Once a subsystem holds a reference to another, you cannot reason about either
alone, you cannot test either alone, and the dependency graph grows edges nobody
drew deliberately. Within a season it becomes a thing nobody can hold in their head.

Cross-subsystem behaviour lives in one file, where you can see all of it.

## See it

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

Setup: **[Running the simulator](../../../setup/simulator.md)**.

Plot three signals on one graph: `Flywheels/TargetRPM`, `Flywheels/ActualRPM`, and
the roller output.

Hold your right-bumper key and watch the sequence unfold:

1. Target jumps to 3000 immediately
2. Actual climbs toward it over about half a second
3. The roller stays at 0 the whole time
4. The moment actual crosses the ready threshold, the roller kicks to −0.6
5. 0.4 s later the roller stops
6. Everything winds down

That staircase is your composition drawn in time. If the roller trace moves at step
1 instead of step 4, your `waitUntil` is in the wrong place.

## Done

Rubric is green.

```bash
./tools/frcprog next
```
