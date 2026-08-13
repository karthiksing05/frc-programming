# Lesson 29 — State machines

**Stage 2D · 50 min · Needs: 28**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

A climb is `IDLE → EXTENDING → HOOKED → RETRACTING → CLIMBED`. Some transitions are
one-way. Express that as trigger composition and you get a page nobody can verify.

## Do this

**1. Write the enum and the guard first**, with no hardware. It is a pure function
and you can unit-test it in two minutes.

```java
public enum State { IDLE, EXTENDING, HOOKED, RETRACTING, CLIMBED }

public void requestTransition(State desired) {
  if (!isValidTransition(state, desired)) {
    DriverStation.reportWarning("Rejected " + state + " -> " + desired, false);
    return;
  }
  state = desired;
}

private boolean isValidTransition(State from, State to) {
  return switch (from) {
    case IDLE       -> to == State.EXTENDING;
    case EXTENDING  -> to == State.HOOKED || to == State.IDLE;
    case HOOKED     -> to == State.RETRACTING;
    case RETRACTING -> to == State.CLIMBED;
    case CLIMBED    -> false;              // one-way door
  };
}
```

That method is the whole value of the pattern: a complete, readable, reviewable
statement of what the mechanism may do, in one place.

**2. Connect it.** A command that requests transitions from sensors and operator
input, and a `periodic()` that drives motors from the current state. Exactly
lesson 04's roller, with more states and guards.

**3. Publish the state as a string** every loop. On a timeline it reads as a
staircase, and a rejected transition tells you which guard fired.

## How to tell which you need

**Trigger composition** when behaviour is a function of current conditions. "Score
when we have a piece and the operator asks" does not care how you got here.

**A state machine** when behaviour depends on history. "You may only retract if you
previously hooked" is not answerable from present conditions.

The tell: if you are adding boolean fields named `hasAlreadyDone...` to make trigger
composition work, you have built a state machine badly.

## Watch out for

**No guards**, which is an enum with extra steps.

**Requesting transitions from several places**, so nothing knows where a change came
from. Funnel every request through one method.

**Using one where the behaviour is not modal.** Most subsystems are not, and the
ceremony costs more than it returns.

## Done

The climber advances under the right conditions, rejects invalid transitions, and
its state is visible on a plot.

```bash
./tools/frcprog next
```
