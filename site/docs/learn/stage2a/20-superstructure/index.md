# Lesson 20 — Subsystem composition at scale <small>· Stage 2A</small>

<span class="stage-badge">Stage 2A · Lesson 20</span>

*"Score at level 4" isn't an elevator command, a shoulder command, or an intake command. It's all three, in a specific order, with one debounced gating condition. Where should it live?*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2A |
    | **Time** | ~50 min |
    | **Prereqs** | [Lesson 19 — Log replay for debugging](../19-log-replay/) |
    | **Edits** | New `src/main/java/frc/robot/subsystems/Superstructure.java` |
    | **Tests** | `frc.robot.subsystems.SuperstructureTest` (`@Tag("lesson-20")`) |
    | **Reference robot** | Kelpie · [`Superstructure.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/Superstructure.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Build a top-level `Superstructure` that holds references to multiple subsystems.
2. Expose **composite triggers** (`atScoringPose`, `readyToShoot`) that combine atomic subsystem triggers.
3. Compose multi-subsystem command sequences as factory methods on the superstructure.
4. Use `@AutoLogOutput` on the superstructure's enum state for instant AdvantageScope visibility.
5. Defend the rule "subsystems never call each other; the superstructure (or `RobotContainer`) coordinates."

---

## The real-world problem

`RobotContainer` taught you trigger composition (Lesson 11): `intake.gamepieceDetected.and(operator.a()).onTrue(scoreCommand)`. That scales until it doesn't.

Now your scoring sequence is:

> Move elevator to level-4 height. Wait until elevator is within 2 cm of setpoint **and** shoulder is at scoring angle **and** the gamepiece is still detected for at least 100 ms — *then* fire the wrist outtake for 0.4 s and retract.

Stuffing all of that into `RobotContainer.configureButtonBindings()` produces a 30-line lambda that even you can't read tomorrow. Kelpie's answer — and the answer for any robot with more than three coordinated mechanisms — is to give cross-subsystem behavior its own home: `Superstructure.java`.

Read Kelpie's [`Superstructure.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/Superstructure.java) before you write yours. It holds references to the elevator, shoulder, wrist, roller, beam-break, and funnel. It exposes triggers like `atScoringPose`. It owns the `Command scoreLevelFour()` factory. The atomic subsystems remain *un-coupled* — they don't know each other exists.

---

## What you'll do

Create a new `Superstructure` class. Wire it to the elevator, shoulder, and intake. Expose at least one composite trigger and one factory command. Bind the command to a button via `RobotContainer`. Verify the boolean shows up in AdvantageScope at the right abstraction level.

---

## The shape

`Superstructure` is *not* a `SubsystemBase`. It owns no hardware. It holds references and exposes composition:

```java linenums="1"
public class Superstructure {
  private final Elevator elevator;
  private final Shoulder shoulder;
  private final Intake intake;

  public final Trigger atScoringPose;
  public final Trigger readyToScore;

  @AutoLogOutput(key = "Superstructure/State")
  private State state = State.IDLE;

  public Superstructure(Elevator elevator, Shoulder shoulder, Intake intake) {
    this.elevator = elevator;
    this.shoulder = shoulder;
    this.intake = intake;

    this.atScoringPose =
        elevator.atSetpoint
            .and(shoulder.atSetpoint)
            .debounce(0.1);
    this.readyToScore =
        atScoringPose.and(intake.gamepieceDetected);
  }

  public enum State { IDLE, PREPARING_SCORE, SCORING, RETRACTING }
}
```

Two things to notice:

1. **Triggers are `public final` fields**, not methods. Same pattern Curriculum-Flow §3.3 prescribed for the atomic subsystems — composite triggers follow the same rule.
2. **State is an enum, logged with `@AutoLogOutput`.** AdvantageScope renders enums as their string name, not as ordinal integers. `Superstructure/State` shows `"PREPARING_SCORE"` in the signal viewer, which is enormously more useful than `1`.

---

## Composite triggers — the abstraction unlock

`elevator.atSetpoint` is the elevator's business. `shoulder.atSetpoint` is the shoulder's. Whether *both* are simultaneously true is **superstructure** business. The superstructure builds higher-level facts:

| Trigger | What it means |
|---|---|
| `elevator.atSetpoint` (atomic) | The elevator alone is within tolerance. |
| `shoulder.atSetpoint` (atomic) | The shoulder alone is within tolerance. |
| `atScoringPose` (composite) | The robot's *posture* is correct. |
| `readyToScore` (composite) | Posture is correct *and* there's a piece *and* it's been stable. |

`RobotContainer` then writes:

```java linenums="1"
operator.rightBumper()
        .and(superstructure.readyToScore)
        .onTrue(superstructure.scoreLevelFour());
```

That's one line. The complex precondition lives behind one identifier. Compare to the lesson-14 version that inlined every clause.

!!! warning "The composite trigger is named after the *intent*, not the *mechanism*"

    `readyToScore` describes intent. `elevatorAtSetpointAndShoulderAtSetpointAndPieceDetected` describes mechanism. Always name the trigger after what *you'd say at a build meeting*. Kelpie's `atScoringPose` reads like English. Yours should too.

---

## The factory command

Same factory pattern from Lesson 07, scaled up:

```java linenums="1"
public Command scoreLevelFour() {
  return Commands.sequence(
        Commands.runOnce(() -> state = State.PREPARING_SCORE),
        Commands.parallel(
            elevator.moveTo(Constants.Elevator.L4_HEIGHT),
            shoulder.moveTo(Constants.Shoulder.SCORING_ANGLE)),
        Commands.waitUntil(readyToScore),
        Commands.runOnce(() -> state = State.SCORING),
        intake.eject().withTimeout(0.4),
        Commands.runOnce(() -> state = State.RETRACTING),
        shoulder.moveTo(Constants.Shoulder.STOW_ANGLE),
        Commands.runOnce(() -> state = State.IDLE))
      .withName("Superstructure/scoreLevelFour");
}
```

Key choices:

- **The factory is on the superstructure**, not on `Elevator` or `Shoulder`. Lesson 14's rule still holds: subsystems don't call each other. The superstructure is the *one place* allowed to know they exist together.
- **`Commands.waitUntil(readyToScore)`** — the composite trigger gates the next phase. No polling loop, no `Thread.sleep`, no busy-wait. This is what Lesson 09 was building toward.
- **State transitions are explicit** — assigning `state = State.SCORING` makes the enum visible in AdvantageScope every cycle.
- **`.withName(...)`** — gives the composed command a stable name in the scheduler dashboard. Useful when ten commands are queued and you're staring at the live view.

---

## Where requirements live

`Commands.sequence(...)` of subsystem factories *automatically* aggregates the requirements of the inner commands. `scoreLevelFour()` therefore requires elevator + shoulder + intake. If a default command on any of those is running, the scheduler will interrupt it cleanly when `scoreLevelFour()` starts — and resume the defaults when it ends.

You do **not** call `addRequirements(...)` on the composed command yourself. The factories on the inner subsystems already did. Doubling up would be a no-op at best and a bug at worst.

---

## Rubric

`SuperstructureTest` asserts:

1. `Superstructure.atScoringPose` reports `true` only when `elevator.atSetpoint` *and* `shoulder.atSetpoint` are both `true` for at least 100 ms.
2. `scoreLevelFour()` aggregates requirements over elevator + shoulder + intake (verified via `Command.getRequirements()`).
3. `Superstructure/State` is `@AutoLogOutput`-ed and transitions through `IDLE → PREPARING_SCORE → SCORING → RETRACTING → IDLE` during one execution.

```bash
./gradlew test --tests '*SuperstructureTest' -DincludeTags='lesson-20'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope: drop `Superstructure/State` onto a Console tab — you'll see the enum names tick by as the sequence runs. Drop `Superstructure/atScoringPose` (a boolean) into the indicator widget alongside `Elevator/Inputs/positionMeters` and `Shoulder/Inputs/angleRad`. The boolean lights up at *exactly* the moment both mechanisms converge. That's the picture of a state machine that was 30 lines in `RobotContainer` ten minutes ago.

---

## Going further

- Add a `coralIntake()` factory to the same superstructure: posture → wait for beam-break → retract. Reuse the same triggers.
- Read Kelpie's [`Superstructure.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/Superstructure.java) line by line. Notice how many *atomic* subsystem methods it calls vs. how many *composed* factories it owns — the ratio is what an experienced team looks like.
- When the superstructure grows past ~300 lines, the formal state-machine pattern from Lesson 29 becomes the next move. You're not there yet; trigger composition is.

!!! quote "Curriculum-Flow §3.3"

    *"Cross-subsystem coordination is binding commands (factories) to triggers, all in `RobotContainer` (or a dedicated `Superstructure`), with no subsystem directly calling another. The result: subsystems compose like Lego, with no surprise dependencies."*

You've now built every piece of that sentence. Stage 2A is complete.

---

??? tip "Full reveal — minimal Superstructure skeleton"

    If you're stuck on the shape:

    ```java
    public class Superstructure {
      private final Elevator elevator;
      private final Shoulder shoulder;
      private final Intake intake;

      public final Trigger atScoringPose;
      public final Trigger readyToScore;

      @AutoLogOutput(key = "Superstructure/State")
      private State state = State.IDLE;

      public Superstructure(Elevator e, Shoulder s, Intake i) {
        this.elevator = e; this.shoulder = s; this.intake = i;
        this.atScoringPose = e.atSetpoint.and(s.atSetpoint).debounce(0.1);
        this.readyToScore  = atScoringPose.and(i.gamepieceDetected);
      }

      public Command scoreLevelFour() { /* see body above */ }

      public enum State { IDLE, PREPARING_SCORE, SCORING, RETRACTING }
    }
    ```

    Wire it in `RobotContainer`:

    ```java
    Superstructure superstructure = new Superstructure(elevator, shoulder, intake);
    operator.rightBumper().onTrue(superstructure.scoreLevelFour());
    ```

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 19**
    Log replay for debugging

    [:octicons-arrow-left-24: Back to lesson 19](../19-log-replay/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 21**
    Swerve drivetrain (intro)

    [:octicons-arrow-right-24: Continue to lesson 21](../../stage2b/21-swerve-intro/)

</div>
