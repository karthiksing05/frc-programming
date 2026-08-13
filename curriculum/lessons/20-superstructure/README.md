# Lesson 20 — Superstructure

**Stage 2A · 50 min · Needs: 18**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Some behaviours are not about buttons at all.

> To score at L4, the elevator must be up **and** the shoulder at angle **and**
> we must actually be holding a piece.

That is not a binding. It is a fact about the robot, and it wants a home.

## Do this

Create `subsystems/Superstructure.java`.

**1. A composite trigger:**

```java
public final Trigger readyToScore;

public Superstructure(ElevatorSubsystem e, ShoulderSubsystem s, RollerSubsystem r) {
  elevator = e; shoulder = s; roller = r;
  readyToScore =
      elevator.atGoalTrigger
          .and(shoulder.atGoalTrigger)
          .and(roller.hasGamePieceTrigger)
          .debounce(0.1);
}
```

**2. A command factory that spans subsystems:**

```java
public Command scoreHigh() {
  return elevator.goToCommand(Constants.Elevator.HIGH_METERS)
      .alongWith(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS))
      .andThen(Commands.waitUntil(readyToScore))
      .andThen(roller.ejectCommand().withTimeout(0.4))
      .withTimeout(4.0);
}
```

**3. Publish `readyToScore`** and watch it light up in AdvantageScope only when all
three conditions converge.

Start with the trigger, not the command. Getting it plotted tells you immediately
whether your conditions are what you thought.

## It is not a subsystem

`Superstructure` does not extend `SubsystemBase`, deliberately. It owns no
hardware. Making it a subsystem would let commands *require* it, which would stop
two superstructure commands running at once even when they touch different
mechanisms.

It coordinates. The subsystems underneath stay the units of exclusive ownership.

## What it is not for

Things that did not fit elsewhere. The test: one subsystem involved means it
belongs on that subsystem. Several means here or in `RobotContainer`.

## Watch out for

**Storing state.** If it has mutable fields beyond the subsystem references, ask
what they are for. Usually they duplicate something a subsystem already knows.

**Forgetting `.debounce()`.** Three noisy conditions ANDed are noisier than any one.


## See it

Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

Publish `readyToScore` as a boolean and plot it alongside the three conditions it is
built from: elevator at goal, shoulder at goal, piece present.

Four traces on one graph. The composite should go true only where all three others
overlap, and 100 ms after the last one arrives.

Watching a boolean go green exactly when three separate conditions converge is the
clearest confirmation that your composition says what you meant. If it goes true
early, check whether you used `.or()` somewhere.

## Done

`readyToScore` is true only when all three hold, and `scoreHigh()` sequences
correctly.

```bash
./tools/frcprog next
```

**The rule, restated:** subsystems never reference each other. Cross-subsystem
behaviour lives in `RobotContainer` or here. Follow it and you can open any
subsystem and understand it without opening another.
