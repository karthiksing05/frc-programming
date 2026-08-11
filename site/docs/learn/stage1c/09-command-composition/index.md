# Lesson 09 — Command Composition <small>· Stage 1C</small>

<span class="stage-badge">Stage 1C · Lesson 09</span>

*"Spin up the flywheel, then push a note in, then stop." One command can't do that. We need to compose primitives.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1C |
    | **Time** | ~40 min |
    | **Prereqs** | [Lesson 08 — Joystick bindings & Triggers](../08-triggers-bindings/) |
    | **Edits** | `src/main/java/frc/robot/RobotContainer.java` |
    | **Tests** | `frc.robot.composition.ScoreSequenceTest` (`@Tag("lesson-09")`) |
    | **Reference robot** | Presto · composition patterns in [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java) and `flywheels/Flywheels.java` |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Combine factories with `andThen`, `alongWith`, `race`, and `deadlineFor`.
2. Use `Commands.waitSeconds(t)` as the *only* way to "wait" inside a command.
3. Apply `.withTimeout(t)` as a safety bound on every step that talks to hardware.
4. Compose a multi-subsystem sequence in `RobotContainer` *without* any subsystem calling another.

---

## The real-world problem

Scoring a game piece on Presto is a sequence of steps that all need to happen in order, with the right overlap:

1. Spin the flywheels up to target RPM.
2. *Once they're at speed* — not before — run the indexer to push the note into the wheels.
3. Stop everything when the note is gone.
4. If anything takes longer than 1.5 seconds, bail out.

A single `Command` returned by a single factory can't express that. Each step lives in a different subsystem, and the *order* between them is a `RobotContainer` concern, not a subsystem concern — per Oblarg's third principle, cross-subsystem coordination lives where the subsystems meet, not inside them.

---

## What you'll do

Inside `RobotContainer`, build a `scoreCommand` by composing the flywheel, indexer, and waitSeconds primitives. Bind it to a button. Run the sim. Watch the AdvantageScope traces line up: flywheel ramps, *then* indexer kicks in, *then* both decay.

```java linenums="1"
private Command scoreCommand() {
  return flywheels.spinUpCommand()                      // start spinning
      .alongWith(                                       // ...and in parallel:
          Commands.waitSeconds(0.5)                     //   wait 0.5 s for speed
              .andThen(indexer.feedCommand()            //   then push the note
                  .withTimeout(0.4)))                   //   bounded to 0.4 s
      .withTimeout(1.5);                                // whole thing capped at 1.5 s
}
```

That's six lines for a four-step coordinated sequence across three subsystems. Read it top-to-bottom; it sounds like the description above.

---

## The composition operators

Each operator is introduced when it's the only way to express a sentence in plain English. Memorize these five — they cover ~95% of FRC composition needs.

| Operator | Sentence it expresses | Returns when |
|---|---|---|
| `a.andThen(b)` | "Do `a`, then `b`." | `b` finishes. If `a` never finishes, `b` never starts. |
| `a.alongWith(b)` | "Do `a` and `b` together." | Both `a` and `b` finish. |
| `a.raceWith(b)` | "Do `a` and `b` together; whichever finishes first cancels the other." | Either one finishes. |
| `a.deadlineFor(b)` | "Do `a` and `b` together until `a` finishes; then cancel `b`." | `a` finishes (regardless of `b`'s state). |
| `a.withTimeout(t)` | "Do `a` for at most `t` seconds." | `a` finishes, or `t` elapses — whichever is first. |

Two more you'll see in compositions:

| Operator | Sentence it expresses |
|---|---|
| `a.until(condition)` | "Do `a` until `condition` is true." |
| `Commands.waitSeconds(t)` | A command that does nothing for `t` seconds, then ends. |

!!! quote "From the WPILib docs"

    *"Command compositions are commands themselves … they can be used in higher-level compositions to build complex robot actions out of simpler component commands."*

    — [Command Compositions · WPILib docs](https://docs.wpilib.org/en/stable/docs/software/commandbased/command-compositions.html)

---

## Reading a composition

The `scoreCommand` above is worth re-reading slowly. The trick is to notice the *parallelism*: `alongWith` runs two branches at once, and one of those branches is itself a sequence.

```
spinUpCommand ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ (until outer ends)
                ┌── waitSeconds(0.5) ──┐
alongWith ──────┤                      ├─── (branch done)
                └── feedCommand .withTimeout(0.4) starts at t=0.5 ─┘

(outer)  withTimeout(1.5) ─────────────────────────────────────── cancels if not done by t=1.5
```

Each subsystem's factory ends its own way: `spinUpCommand` runs forever (it's `whileTrue`-style and only ends when interrupted), `feedCommand` ends when the beam-break clears (or its 0.4 s timeout). The outer `.withTimeout(1.5)` is the floor: if the indexer jams and never clears the beam-break, the whole sequence still ends and the flywheels stop.

---

## Why not `Thread.sleep`?

The "wait 0.5 seconds for the flywheel to reach speed" step looks like it could be a `Thread.sleep(500)` call. It absolutely cannot.

The `CommandScheduler` runs on the main robot thread. `Thread.sleep` halts that thread. While it sleeps:

- *No other command runs.* Your default drive command stops. The robot freezes in place.
- *No subsystem `periodic()` runs.* No sensor inputs update. No telemetry is logged.
- *The watchdog will fire.* You'll see `Loop overrun: 0.500s` in the console and the FMS will mark your robot as unresponsive.

`Commands.waitSeconds(0.5)` returns a command that yields back to the scheduler every tick. The drive command keeps running. Sensors keep updating. Other commands keep being scheduled. The sequence simply doesn't advance for half a second.

!!! warning "Never `Thread.sleep` inside a command"

    Neither `Thread.sleep`, `Timer.delay`, nor a busy-wait loop. Every "wait" inside a command must be expressed as `Commands.waitSeconds(t)` or `until(...)` or similar. See [Curriculum-Flow §5.5](/process/Curriculum-Flow.md). The watchdog will catch this on the robot, but it can corrupt sim runs invisibly.

---

## Subsystem requirements come along for free

A subtle but load-bearing fact: when you build `flywheels.spinUpCommand().alongWith(indexer.feedCommand())`, the resulting composed command **requires both subsystems**. The scheduler tracks this automatically through the factories.

That means:

- If a different command tries to schedule against the flywheels mid-score, the scheduler will cancel one of them (and your `finallyDo` will set voltages back to zero).
- You don't need `addRequirements(...)` calls anywhere. The factories handled it on the way in.

This is one of the quiet wins of the factory pattern. Subclassing `Command` forces you to manage requirements by hand; factories make them implicit and correct.

---

## Wiring it up

```java
private void configureButtonBindings() {
  operator.a().whileTrue(flywheels.spinUpCommand());   // hold-to-spin (Lesson 08)
  operator.rightBumper().onTrue(scoreCommand());        // tap-to-score
}
```

The score sequence is `onTrue` — a single tap fires the whole sequence; you don't have to hold the button for the full 1.5 seconds. The 1.5 s timeout is the bound, not the duration.

---

## Rubric

`ScoreSequenceTest` (with `@Tag("lesson-09")`) asserts:

1. Tapping the bound button runs the sequence end-to-end (flywheels spin up, then indexer feeds).
2. `Flywheels/Inputs/velocityRPM` reaches `>70%` of the target before `Indexer/Inputs/output` becomes non-zero (proves the `waitSeconds` is doing real work).
3. The whole sequence is complete by `t = 1.5 s` regardless of inputs (timeout is active).
4. Both subsystems are listed in the composition's `getRequirements()` set (proves automatic requirement gathering).

Run it locally:

```bash
./gradlew test --tests '*ScoreSequenceTest' -DincludeTags='lesson-09'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope, plot:

- `Flywheels/Inputs/velocityRPM`
- `Indexer/Inputs/output`

Tap the score button. You should see the flywheel trace rise, then — half a second later — the indexer trace kick in, then both decay together. The shape is what you composed; if it doesn't match, the composition is wrong.

---

## Going further

- Add an `intake` phase: `intake.intakeNoteCommand().andThen(scoreCommand())`. The whole pickup-and-score is one composition.
- Read Presto's [`Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) — notice their factories return commands parameterized by *suppliers* of target RPM. The same composition tools work, but each piece is reusable across different targets.
- Try replacing the inner `waitSeconds(0.5)` with `.until(flywheels::atSpeed)`. Why might that be better? When might it be worse?

??? tip "Full reveal — only open if you're really stuck"

    ```java
    private Command scoreCommand() {
      return flywheels.spinUpCommand()
          .alongWith(
              Commands.waitSeconds(0.5)
                  .andThen(indexer.feedCommand().withTimeout(0.4)))
          .withTimeout(1.5);
    }
    ```

    Read it as English: *"Spin up alongside (wait 0.5 s then feed for up to 0.4 s), for at most 1.5 s total."*

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 08**
    Joystick bindings & Triggers

    [:octicons-arrow-left-24: Back to lesson 08](../08-triggers-bindings/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 10**
    Telemetry & AdvantageScope basics

    [:octicons-arrow-right-24: Continue to lesson 10](../10-telemetry/)

</div>
