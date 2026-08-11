# Lesson 29 — Advanced state machines

> **Stage 2D · ~50 minutes · Prerequisite: 28-sysid**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Trigger composition has carried you a long way. Some behaviours defeat it.

A climb sequence is `IDLE → EXTENDING → HOOKED → RETRACTING → CLIMBED`. Each
transition has a guard. Some are irreversible — you cannot un-hook mid-climb. Express
that as trigger composition and you get a page of interlocking conditions that nobody
can verify.

Some behaviour is genuinely modal, and modal behaviour wants a state machine.

## What you'll learn

1. Tell "genuinely modal" apart from "merely composable".
2. Implement a state machine as an enum plus a transition method with guards.
3. Drive a state machine from a command.
4. Log state transitions so you can see what happened.

## What you'll do

Build `ClimberStateMachine`:

```java
public enum State { IDLE, EXTENDING, HOOKED, RETRACTING, CLIMBED }

private State state = State.IDLE;

public void requestTransition(State desired) {
  if (!isValidTransition(state, desired)) {
    DriverStation.reportWarning("Rejected " + state + " -> " + desired, false);
    return;
  }
  state = desired;
}

private boolean isValidTransition(State from, State to) {
  return switch (from) {
    case IDLE -> to == State.EXTENDING;
    case EXTENDING -> to == State.HOOKED || to == State.IDLE;
    case HOOKED -> to == State.RETRACTING;
    case RETRACTING -> to == State.CLIMBED;
    case CLIMBED -> false;   // one-way door
  };
}
```

That `isValidTransition` method is the entire value of the pattern. It is a complete,
readable, reviewable statement of what the mechanism may do, in one place. Try
writing the same guarantee as trigger composition and you will have it spread across
six bindings with no single place to check it.

### How to tell

**Use trigger composition when** behaviour is a function of current conditions.
"Score when we have a piece and the operator asks" does not care how you got here.

**Use a state machine when** behaviour depends on *history*. "You may only retract if
you previously hooked" is not answerable from present conditions alone.

The test: if you find yourself adding boolean fields named `hasAlreadyDone...` to
make trigger composition work, you have built a state machine badly. Build it
properly.

### Log the transitions

Publish the state as a string every loop. On an AdvantageScope timeline a state
machine reads as a staircase, and a rejected transition — logged as a warning — tells
you exactly which guard fired and why.

## Done?

The climber advances through its states under the right conditions, rejects invalid
transitions, and its state is visible on a plot.

```bash
./tools/frcprog next
```
