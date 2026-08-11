# Lesson 11 — Default commands done right <small>· Stage 1D</small>

<span class="stage-badge">Stage 1D · Lesson 11</span>

*Your robot has a teleop loop, autos in your head, and one stubborn question — what should each subsystem do when **nothing** is telling it to do anything?*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1D |
    | **Time** | ~35 min |
    | **Prereqs** | [Lesson 10 — Telemetry & AdvantageScope basics](../../stage1c/10-telemetry/) |
    | **Edits** | `src/main/java/frc/robot/RobotContainer.java` + light touch in subsystems |
    | **Tests** | `frc.robot.RobotContainerDefaultsTest` (`@Tag("lesson-11")`) |
    | **Reference robot** | Presto · [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Give every subsystem an explicit idle behavior with `setDefaultCommand`.
2. Keep default commands **trivial** — no `if`/`else` chains, no decision logic.
3. Compose triggers with `.and()`, `.or()`, `.negate()`, and `.debounce()`.
4. Explain why decision logic belongs in trigger composition, not in default commands.

---

## The real-world problem

In Lesson 09 you wrote a score sequence that finishes after about a second. After it ends, the flywheel keeps spinning on its last commanded voltage — until something else interrupts it. The intake's behavior on idle is whatever its `periodic()` happens to compute when no command is running. The LEDs stay frozen on whatever pattern fired last.

Robots that ship to events without explicit idle behavior have one of two failure modes. The friendly one is "weird stuff happens between commands." The unfriendly one is "the flywheel kept running at 6000 RPM for the rest of the match because the driver let go of the button mid-sequence and the scheduler had nothing to fall back on." Lesson 11 is the immune system against both.

The fix has two halves. First: every subsystem gets a default command — a single, boring, no-decisions command that says *"here is what you do when nobody else is telling you anything."* Second: any logic that *did* belong in those default commands moves into triggers, which are composable in a way that `if`/`else` will never be.

---

## What you'll do

In `RobotContainer`, set default commands on every subsystem:

- **Drive** keeps its existing `arcadeDriveCommand(...)` from Lesson 07 as its default — joystick suppliers all the way down.
- **Flywheels** default to a one-liner that holds 0 V.
- **LEDs** default to a slow idle pattern.

Then add a single compound trigger — `gamepieceDetected.and(operator.a()).onTrue(scoreCommand)` — that demonstrates trigger composition. The score sequence only fires when both conditions are simultaneously true.

---

## Default commands, in one line each

The rule of thumb borrowed straight from BoVLB's distillation of Oblarg's principles: **if your default command needs a comment, it shouldn't be a default command.** Anything that reads like "*if X then do Y else do Z*" belongs in a `Trigger`.

```java
// In RobotContainer constructor, after subsystems are constructed:
drive.setDefaultCommand(
    drive.arcadeDriveCommand(driver::getLeftY, driver::getRightX));

flywheels.setDefaultCommand(
    flywheels.run(() -> flywheels.setVoltage(0.0)));

leds.setDefaultCommand(leds.idlePatternCommand());
```

Three lines. Three subsystems. Three completely predictable idle states.

!!! warning "The footgun"

    A default command that looks like this:

    ```java
    flywheels.setDefaultCommand(flywheels.run(() -> {
        if (intake.hasGamepiece()) flywheels.setVoltage(6.0);
        else flywheels.setVoltage(0.0);
    }));
    ```

    …is the anti-pattern this lesson exists to kill. Cross-subsystem checks inside a default command tangle two subsystems together and bypass the scheduler's requirement system. If `flywheels` is required by another command, this code is dead weight; if it isn't, you have two sources of truth fighting each other. Move the check into a trigger.

---

## Trigger composition

`Trigger` is not just for buttons. Anywhere you have a `BooleanSupplier`, you have a trigger. And triggers compose:

| Operator | What it does |
|---|---|
| `a.and(b)` | New trigger; true only when both are true. |
| `a.or(b)` | New trigger; true when either is true. |
| `a.negate()` | Inverts. |
| `a.debounce(0.1)` | Only fires after `a` has held its state for 100 ms — kills sensor noise. |

The pattern your `RobotContainer` is heading toward:

```java
// In configureBindings()
final Trigger ready = intake.gamepieceDetected
        .debounce(0.05)
        .and(operator.a());

ready.onTrue(scoreCommand);
```

Read out loud: *"When the beam-break has seen a piece for at least 50 ms AND the operator presses A, run the score sequence once."* This is a sentence, not a state machine. That's the goal.

!!! tip "Where the trigger comes from"

    `intake.gamepieceDetected` is a `public final Trigger` field on the `Intake` subsystem itself — Oblarg principle 2, *get information from subsystems using triggers*. Don't expose `boolean hasGamepiece()` and force every caller to wrap it.

    ```java
    public final Trigger gamepieceDetected =
        new Trigger(() -> sensor.getRange() < 200);
    ```

---

## Rubric

`RobotContainerDefaultsTest` asserts:

1. With no buttons pressed, the drive still tracks joystick input (default command active).
2. With no buttons pressed, `Flywheels/AppliedVolts` reads 0 V.
3. Holding the operator A button while no gamepiece is present → score sequence does **not** start.
4. Pressing A while the beam-break debounced trigger is true → score fires exactly once.
5. No subsystem reports an undefined idle behavior (every subsystem has `getDefaultCommand() != null`).

Run locally:

```bash
./gradlew test --tests '*RobotContainerDefaultsTest' -DincludeTags='lesson-11'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope, drop three boolean indicators side by side:

- `Intake/gamepieceDetected`
- `Operator/A`
- A `RealOutputs/Triggers/ScoreReady` you publish from `RobotContainer` for visibility

Now poke the simulated beam-break. Watch the first indicator turn green. Press A. The second goes green. The third — the `.and()` — is the only one that pulses *just once* and kicks the score sequence. This is what Lesson 11 buys you: a debugger view where the composed trigger is itself a first-class signal.

---

## Going further

- Add `.debounce(0.10)` to a noisy boolean (the limit switch, the CAN-bus presence flag). Plot the raw input and the debounced one. Watch what 100 ms of hysteresis costs you and what it saves you.
- Replace one of your trivial default commands with `Commands.idle(subsystem)` — WPILib's built-in "do absolutely nothing" command. Confirm tests still pass.
- Read Presto's [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java). Find the `Trigger.and(...)` chains in their binding section. Most of their cross-subsystem behavior reads as sentences in English, not state machines.

---

??? tip "Full reveal — only open if you're really stuck"

    The minimum to get all five rubric checks green:

    ```java
    public RobotContainer() {
        drive.setDefaultCommand(
            drive.arcadeDriveCommand(driver::getLeftY, driver::getRightX));
        flywheels.setDefaultCommand(
            flywheels.run(() -> flywheels.setVoltage(0.0)));
        leds.setDefaultCommand(leds.idlePatternCommand());

        configureBindings();
    }

    private void configureBindings() {
        intake.gamepieceDetected
              .debounce(0.05)
              .and(operator.a())
              .onTrue(scoreCommand());
    }
    ```

    If the test for "score does not fire when no piece" is failing, you forgot the `.and(...)` — your trigger is firing on the button alone.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 10**
    Telemetry & AdvantageScope basics

    [:octicons-arrow-left-24: Back to lesson 10](../../stage1c/10-telemetry/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 12**
    Auto routines (basic)

    [:octicons-arrow-right-24: Continue to lesson 12](../12-auto-basic/)

</div>
