# Lesson 29 — Advanced state machines <small>· Stage 2D</small>

<span class="stage-badge">Stage 2D · Lesson 29</span>

*A climber doesn't compose. It transitions. The whole curriculum has trained you to reach for `Triggers` and `andThen` — and for the climber, that's the wrong instinct.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2D |
    | **Time** | ~50 min |
    | **Prereqs** | [Lesson 28 — System identification (SysId)](../28-sysid/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/climber/ClimberSubsystem.java`, `src/main/java/frc/robot/subsystems/climber/ClimberStateMachine.java` |
    | **Tests** | `frc.robot.subsystems.climber.ClimberStateMachineTest` (`@Tag("lesson-29")`) |
    | **Reference robot** | Presto · [`superstructure/climber/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/superstructure/climber) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Tell the difference between behavior that is **composable** (trigger composition wins) and behavior that is **modal** (state machine wins).
2. Implement a finite state machine as an `enum` of states plus a transition method with explicit guards.
3. Wire that state machine into command-based — a single factory method returns a command that drives the SM.
4. Log state transitions so AdvantageScope shows the climber's life as a discrete-valued signal.
5. Avoid the "stateful subsystem with hidden mode flags" anti-pattern that state machines are too often mistaken for.

---

## The real-world problem — why trigger composition isn't enough

Everything in Stage 1C taught you to express coordination as triggers binding factories. That works because most robot behavior really is composable: *intake while a button is held; score when the elevator is up; flash LEDs when a gamepiece is detected.* Each clause is independent, and the scheduler decides which ones run.

Climbers are different. A climber walks through phases that have a strict order, where each phase has a different motor command, and where the wrong transition damages the robot.

```
IDLE → EXTENDING → READY → HOOKED → RETRACTING → CLIMBED
```

You can't intake while you're climbing. You can't go back to `IDLE` once you're past `HOOKED` without breaking the latch. You can't enter `RETRACTING` until the hook sensor is engaged — and if you do, the climber drags the robot across the field. The transitions matter as much as the states.

[Curriculum-Flow.md §7.6](/process/Curriculum-Flow.md) names this directly: trigger composition is the default; formal state machines are the tool for genuinely modal subsystems. The climber is the canonical example.

---

## What you'll do

You'll write `ClimberStateMachine.java` next to your existing `ClimberSubsystem.java`. The state machine owns three things: the current state, the rules for legal transitions, and the desired motor command for each state. The subsystem owns the hardware and the IO interface, exactly as it has for every prior lesson.

```java
public enum ClimberState {
  IDLE, EXTENDING, READY, HOOKED, RETRACTING, CLIMBED;
}

public class ClimberStateMachine {
  private ClimberState state = ClimberState.IDLE;

  public ClimberState current() { return state; }

  public boolean tryTransition(ClimberState next, ClimberSubsystem hw) {
    if (!isLegal(state, next, hw)) return false;
    state = next;
    return true;
  }

  private boolean isLegal(ClimberState from, ClimberState to, ClimberSubsystem hw) {
    return switch (from) {
      case IDLE        -> to == ClimberState.EXTENDING;
      case EXTENDING   -> to == ClimberState.READY && hw.atFullExtension();
      case READY       -> to == ClimberState.HOOKED && hw.hookEngaged();
      case HOOKED      -> to == ClimberState.RETRACTING;
      case RETRACTING  -> to == ClimberState.CLIMBED && hw.atFullRetraction();
      case CLIMBED     -> false; // terminal
    };
  }
}
```

Notice what the state machine **isn't**: it isn't a subsystem, it doesn't have a `periodic()`, and it doesn't talk to motors. It's a small object that the climber subsystem holds and asks for permission before changing state. The hardware-side `periodic()` reads `stateMachine.current()` and commands the right voltage for that state — `+6 V` for `EXTENDING`, `-6 V` for `RETRACTING`, `0 V` for `IDLE`/`READY`/`CLIMBED`, `+1 V hold` for `HOOKED`.

---

## Wiring it into command-based

The state machine itself has no commands. The subsystem exposes **one** factory that requests a transition, and command-based does the rest:

```java
public Command requestState(ClimberState next) {
  return runOnce(() -> stateMachine.tryTransition(next, this))
      .withName("Climber→" + next);
}
```

Then `RobotContainer` binds buttons to specific transition requests:

```java
operator.povUp().onTrue(climber.requestState(ClimberState.EXTENDING));
operator.povDown().onTrue(climber.requestState(ClimberState.RETRACTING));
operator.povRight().onTrue(climber.requestState(ClimberState.HOOKED));
```

Illegal transitions don't throw — they just silently fail. The operator pressing "retract" while the climber is still in `IDLE` is a no-op. The state machine's job is to never let the hardware enter an inconsistent state.

!!! tip "Expose `Trigger`s, not flags"

    Even though the climber is internally state-machine-driven, the rest of the robot still consumes it through triggers — exactly the pattern from lesson 11. Add `public final Trigger climbed = new Trigger(() -> stateMachine.current() == ClimberState.CLIMBED);` and your LED subsystem can react to it with no knowledge of the FSM. State machines live *inside* a subsystem; their outputs face the world as triggers.

Presto's climber follows essentially this shape — an enum-driven state, sensor-guarded transitions, and a thin command surface. Compare your `ClimberStateMachine.java` to their [`Climber.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/superstructure/climber). You'll see the same idea expressed with their AdvantageKit logging conventions; the underlying pattern is the same.

---

## When **not** to write one

The risk of this lesson is that you fall in love with state machines and start writing them for everything. Most subsystems aren't modal. The intake from lesson 04 has an `enum State { OFF, INTAKING, EJECTING }` — that's a state-tagged subsystem, not a state machine. Nothing guards the transitions because nothing *needs* to: any state is reachable from any other state, the hardware is fine with it, and trigger composition handles the choreography.

!!! warning "If transitions don't have guards, you don't have a state machine"

    You have a mode flag wearing a costume. Modes flags are fine, but don't formalize them. The whole point of the FSM machinery is the transition table — `isLegal(from, to, hw)` — and if every entry is `true`, you're paying the abstraction tax for nothing.

The honest rule: write a state machine when (a) the order of states matters, (b) some transitions are physically unsafe, and (c) you find yourself writing `if (mode == X && sensor && !otherSensor && previousMode == Y)` in three places. That third symptom is the strongest. The climber tripped all three. Most subsystems trip none.

---

## Rubric

`ClimberStateMachineTest` asserts:

1. `IDLE → EXTENDING → READY → HOOKED → RETRACTING → CLIMBED` succeeds with the right sensor preconditions met at each step.
2. Illegal transitions (e.g., `IDLE → HOOKED`) return `false` and leave the state unchanged.
3. `Climber/state` appears in the log as a string enum each tick.
4. The motor voltage commanded in `periodic()` matches the table for each state.

```bash
./gradlew test --tests '*ClimberStateMachineTest' -DincludeTags='lesson-29'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope, plot `Climber/state` as a discrete signal — you'll see horizontal bands stepping up through the enum as you press buttons. Plot `Climber/Inputs/appliedVolts` underneath; the voltage steps line up with the transitions. The `Mechanism2d` from lesson 17 should show the climber arm extending and retracting on cue.

---

## Going further

- Add a `FAULT` state that any other state can transition into when a current spike or stall is detected. Make it terminal until manually reset.
- Replace the `switch` expression in `isLegal` with a `Map<ClimberState, Set<ClimberState>>` — does it read better or worse? Why?
- Read the [Chief Delphi thread on standardized state-based control](https://www.chiefdelphi.com/t/standardized-state-based-robot-control-vendor-dep/415582). Several teams have tried to ship state-machine vendordeps. None has caught on — the patterns are simple enough that teams roll their own. Why might that be?

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 28**
    System identification (SysId)

    [:octicons-arrow-left-24: Back to lesson 28](../28-sysid/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 30**
    Season capstone

    [:octicons-arrow-right-24: Continue to lesson 30](../30-season-capstone/)

</div>
