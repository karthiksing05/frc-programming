# Lesson 15 — Capstone teleop robot <small>· Stage 1D</small>

<span class="stage-badge">Stage 1D · Lesson 15</span>

*Fourteen lessons of building muscle memory. Today you assemble — no new APIs, just everything you already know, polished to the point a mentor would sign off on it.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1D |
    | **Time** | ~90 min |
    | **Prereqs** | [Lesson 14 — Refactoring with `*Bindings`](../14-bindings-refactor/) — and every lesson before it |
    | **Edits** | Everything, lightly. Polish across files. |
    | **Tests** | `frc.robot.CapstoneIntegrationTest` (`@Tag("lesson-15")`) — 5+ scenario tests |
    | **Reference robots** | Both · [Presto](https://github.com/Mechanical-Advantage/RobotCode2024Public) · [Kelpie](https://github.com/HighlanderRobotics/Reefscape) |

---

## What you'll learn

Nothing new. **That's the point.**

By the end of this lesson you'll be able to:

1. Hand a working teleop robot — 4-5 subsystems, full bindings, two autos, complete telemetry — to a mentor for review.
2. Justify every line of code in your `RobotContainer` and your `*Bindings` classes by pointing to the lesson that introduced the pattern.
3. Write integration tests that exercise the robot the way a driver would.
4. Recognize what *you* would write differently the next time, and articulate why.

---

## The real-world problem

There isn't a new one. The pain Lesson 15 dissolves is the lingering doubt every rookie carries through Stage 1: *did I actually learn this, or did I just type along?* The capstone is a forcing function. You don't get five integration tests green by transcribing — you get them green by knowing the system.

The capstone is also the gate between rookie status and Stage 2. Stage 2A opens with the IO Layer refactor, and that refactor only makes sense if your `Drive` subsystem already works, is tested, and is understood well enough that you can imagine why splitting it might help. Lesson 15 is the proof of "yes, you understand it well enough."

---

## What you'll do

Take your existing project and **finish it**. Not "add features" — finish. Three workstreams:

1. **Audit telemetry.** Every subsystem should publish at least one input and one output via `Logger.recordOutput(...)`. Open AdvantageScope. Walk through every key. Anything missing? Add it. Anything misnamed? Rename it.
2. **Tune.** Drive your robot through every binding in sim. PID values that worked in Lesson 05 might need a tweak now that the robot has weight from a full elevator + shoulder + roller chain. Don't trust constants from earlier lessons; verify them in the final integrated state.
3. **Write five integration tests.** These belong in `frc.robot.CapstoneIntegrationTest`. Each one simulates a driver scenario end-to-end. Sample scenarios in the rubric below.

That's the whole lesson. No starter code, no new API to wire in. Just the engineering discipline that turns a working prototype into a thing you'd put on a competition field.

---

## The capstone checklist

Run this list yourself. Anything you can't check is a bug.

### Subsystems

- [ ] Each subsystem extends `SubsystemBase`.
- [ ] Hardware fields are `private final`.
- [ ] Each subsystem exposes state via `Trigger` fields where it makes sense (`gamepieceDetected`, `atSetpoint`, etc.), not via boolean getters.
- [ ] Each subsystem has at least one `Command`-returning factory method.
- [ ] No subsystem references another subsystem.

### Bindings

- [ ] Every binding is in `DriverBindings`, `OperatorBindings`, or `LedBindings` — not in `RobotContainer`.
- [ ] Every joystick read inside a command happens through a `DoubleSupplier`/`BooleanSupplier`, not a captured value.
- [ ] No `toggleOnTrue` on driver-facing buttons.
- [ ] Compound triggers (`.and`, `.or`, `.debounce`) are used where they read more naturally than a long `if`.

### Autos

- [ ] `SendableChooser<Command>` published as `Auto` on the dashboard.
- [ ] At least two routines available: a "do nothing" default and one real auto.
- [ ] Every step in every auto has `.withTimeout(...)`.
- [ ] No `Thread.sleep` or `Timer.delay` anywhere in the codebase. (`./gradlew check` will surface them.)
- [ ] Path-following routine (Lesson 13) resets pose at start.

### Telemetry

- [ ] `Logger.recordOutput("Subsystem/Field", ...)` for every key piece of state.
- [ ] Key naming follows `Subsystem/Field` PascalCase.
- [ ] At least one `Pose2d` output (e.g., `Drive/Pose`).
- [ ] AdvantageScope layout JSON committed at `lessons/15-capstone/AdvantageScope.json`.

### Code health

- [ ] `RobotContainer.java` ≤ 100 lines (Lesson 14 contract).
- [ ] All prior lesson tests still pass.
- [ ] No `@SuppressWarnings("unused")` on subsystem fields. If you constructed it and never use it, delete it.

---

## Integration tests

`CapstoneIntegrationTest` should contain at least five end-to-end scenarios. Examples (your team's robot may suggest others):

```java
@Tag("lesson-15")
class CapstoneIntegrationTest {

  @Test void drivesForwardAndScores() {
    // 1. Run SimpleAuto.driveAndScore for 8 simulated seconds.
    // 2. Assert final pose ≥ 1.8 m forward.
    // 3. Assert flywheels reached > 70% of target during the run.
  }

  @Test void teleopIntakeThenScore() {
    // Simulate operator B held until gamepieceDetected,
    // then operator A held → score sequence fires once.
  }

  @Test void scurveAutoEndsAtTarget() { /* from Lesson 13 */ }

  @Test void defaultCommandsRecoverAfterInterruption() {
    // Schedule a manual command on each subsystem; cancel it;
    // assert the default command is now running.
  }

  @Test void noSubsystemEnterUndefinedState() {
    // Run a full match-length sim; assert every subsystem's
    // getCurrentCommand() is non-null at every tick.
  }
}
```

Run them all:

```bash
./gradlew test --tests '*CapstoneIntegrationTest' -DincludeTags='lesson-15'
```

If any fail, fix the *robot*, not the test.

---

## Comparing to the reference robots

This is the moment Lessons 0B and 0C pay off. Open your `RobotContainer` next to Presto's. Open your `RollerSubsystem` next to Kelpie's. Things to notice:

- **Their style isn't perfectly consistent either.** Presto uses vendor-named IO impls (`FlywheelsIOKrakenFOC`); Kelpie uses `ElevatorIOReal`. Both work. Pick one and stay consistent within your project.
- **Their `RobotContainer` is bigger than yours** — they have more subsystems, vision, climber, multi-camera apriltag fusion. But the shape is the same: subsystems on top, bindings below, autos at the bottom.
- **Their commands compose the way yours do.** Look at Presto's score sequences and verify that your mental model matches.

!!! quote "From Presto's RobotContainer header comments"

    *"Cross-subsystem coordination happens here. If you find yourself wanting Subsystem A to call Subsystem B, the answer is almost always to bind a Trigger here instead."*

    That's Oblarg's third principle, restated in their own words. Your code should answer the same call.

---

## Rubric

Five integration scenarios pass:

1. Drive forward + score auto completes within 8 s.
2. Teleop intake → score path fires exactly one score sequence.
3. Path-follow auto ends within tolerance.
4. Default commands resume after manual command cancellation.
5. No subsystem reports `getCurrentCommand() == null` during a full-match sim.

Plus the capstone checklist (above) is fully ticked.

---

## The mentor PR

Open a PR titled `Stage 1 Capstone — <your-name>`. The PR description should include:

- One sentence per subsystem describing what it does.
- A link to the AdvantageScope screenshot showing telemetry working.
- The output of `./gradlew test --tests '*' -DincludeTags='lesson-01,…,lesson-15'` (every prior lesson's tests still passing).
- One paragraph: *"If I started over, here's what I'd do differently."*

That last paragraph is the most important. It tells the mentor — and yourself — that you've moved from "executing the lesson plan" to "having opinions about the lesson plan." That's the rookie-to-Stage-2 transition.

---

## Going further

There isn't a "going further" for Lesson 15. Going further is **Stage 2**.

Specifically: Lesson 16 introduces the IO Layer pattern, which will refactor your `Drive` subsystem (and later every subsystem) into four files. The reason it'll feel like relief instead of bureaucracy: you'll already have wished, on this capstone, that there was a cleaner way to test `Drive` without spinning up `simulateJava`. Stage 2A is that cleaner way.

Take a break first. You earned it.

---

??? tip "Full reveal — what mentors look for in capstone PRs"

    The thing experienced reviewers scan first isn't the bindings or the autos — it's whether the *subsystems* are clean. A `Drive.java` that's 80 lines of `private final` fields, a `periodic()` that only updates telemetry, and three or four well-named factory methods is the green light. A `Drive.java` that's 400 lines with public mutable state and a `setEverything(...)` method is the red one.

    The other tell: how many places does `motor.set(...)` or `motor.setVoltage(...)` appear? If it's only inside subsystem private methods, you've understood encapsulation. If it leaks into `RobotContainer` or any binding class, that's the first comment a mentor will leave.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 14**
    Refactoring with *Bindings classes

    [:octicons-arrow-left-24: Back to lesson 14](../14-bindings-refactor/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 16**
    The IO Layer pattern

    [:octicons-arrow-right-24: Continue to lesson 16](../../stage2a/16-io-layer/)

</div>
