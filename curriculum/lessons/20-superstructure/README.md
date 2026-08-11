# Lesson 20 — Subsystem composition at scale

> **Stage 2A · ~50 minutes · Prerequisite: 19 (or 18, if you skipped the extension)**


!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.


Lesson 14's bindings classes handle buttons. But some behaviours are not about
buttons at all:

> *To score at L4, the elevator must be up **and** the shoulder at the scoring angle
> **and** we must actually be holding a game piece.*

That is not a binding. It is a fact about the robot, and it wants a home.

## What you'll learn

1. Write a `Superstructure` that coordinates several subsystems.
2. Expose composite conditions as `Trigger`s.
3. Keep coordination in one place without letting subsystems reference each other.

## What you'll do

Create `src/main/java/frc/robot/subsystems/Superstructure.java`.

It holds references to several subsystems, exposes composite triggers, and offers
command factories that span them:

```java
public class Superstructure {
  private final ElevatorSubsystem elevator;
  private final ShoulderSubsystem shoulder;
  private final RollerSubsystem roller;

  public final Trigger readyToScore;

  public Superstructure(ElevatorSubsystem e, ShoulderSubsystem s, RollerSubsystem r) {
    elevator = e; shoulder = s; roller = r;
    readyToScore =
        elevator.atGoalTrigger.and(shoulder.atGoalTrigger).and(roller.hasGamePieceTrigger)
            .debounce(0.1);
  }

  public Command scoreHigh() {
    return elevator.goToCommand(Constants.Elevator.HIGH_METERS)
        .alongWith(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS))
        .andThen(Commands.waitUntil(readyToScore))
        .andThen(roller.ejectCommand().withTimeout(0.4))
        .withTimeout(4.0);
  }
}
```

Then publish `readyToScore` as telemetry and watch it light up in AdvantageScope
only when all three conditions converge.

### Why this is not a subsystem

`Superstructure` does not extend `SubsystemBase`, and that is deliberate. It owns no
hardware. Making it a subsystem would mean commands could *require* it, which would
mean two superstructure commands could not run at once even when they touch entirely
different mechanisms.

It is a coordinator. The subsystems underneath remain the units of exclusive
ownership, and the requirement system keeps working exactly as before.

### What it is not allowed to do

It is not a place to put things that did not fit elsewhere. The test: if a behaviour
involves exactly one subsystem, it belongs on that subsystem. If it involves several,
it belongs here or in `RobotContainer`.

Kelpie's [`Superstructure.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/Superstructure.java)
is the production version of this idea.

## Done?

`readyToScore` reports true only when all three conditions hold, and `scoreHigh()`
sequences the mechanisms correctly.

```bash
./tools/frcprog next
```

## The rule, restated

Subsystems never reference each other. Every cross-subsystem behaviour lives in one
of exactly two places: `RobotContainer` (for bindings) or `Superstructure` (for
behaviours complex enough to deserve a name).

Follow that and you can open any subsystem file and understand it completely without
opening another. That property is worth defending, and it is the first thing to go
when somebody is in a hurry.
