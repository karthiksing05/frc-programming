# Lesson 14 — Refactoring with `*Bindings` classes <small>· Stage 1D</small>

<span class="stage-badge">Stage 1D · Lesson 14</span>

*Your `RobotContainer` is 250 lines and you can't find the score binding. The fix isn't a smaller file — it's the right number of files.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1D |
    | **Time** | ~45 min |
    | **Prereqs** | [Lesson 13 — Path-following intro](../13-path-following/) |
    | **Edits** | New: `bindings/DriverBindings.java`, `bindings/OperatorBindings.java`; shrink `RobotContainer.java` |
    | **Tests** | `frc.robot.RobotContainerSizeTest` (`@Tag("lesson-14")`) |
    | **Reference robot** | Presto · [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Recognize when a `RobotContainer` has crossed the size threshold that calls for refactoring.
2. Extract a group of bindings into a dedicated class — `DriverBindings`, `OperatorBindings`.
3. Pass subsystems and controllers by constructor injection, never as statics.
4. Keep `RobotContainer` to subsystem ownership + binding-class wiring + auto chooser.

---

## The real-world problem

You finished Lesson 13. Your `RobotContainer` is now somewhere north of 200 lines. Some bindings are driving-related — joystick deadbands, drive mode toggle, gyro reset. Some are scoring-related — operator A/B/X bindings, the triggers from Lesson 11. Some are LEDs and rumble. Some are auto wiring. They were added one at a time, in chronological order, with no organizing principle beyond "it goes after the last thing I added."

This is **god-`RobotContainer`** — a single file that owns every responsibility in the project, and which becomes the merge-conflict capital of your repo. When two teammates touch bindings the same week, they both touch `RobotContainer.java`, and the conflict is mechanical but irritating every single time.

There are two refactors students reach for here. The wrong one is to push bindings *into* the subsystems — `intake.configureMyBindings(operator)` — which puts cross-subsystem orchestration inside individual subsystems, exactly the dependency tangle Oblarg's principles exist to prevent. The right one is to extract bindings *outward* into dedicated classes that take subsystems and controllers as constructor arguments. The orchestration still happens "above" the subsystems, in the same conceptual place as `RobotContainer`. There's just more than one file for it now.

---

## What you'll do

Create two new classes:

```
src/main/java/frc/robot/bindings/
├── DriverBindings.java
└── OperatorBindings.java
```

Move every driver-related binding into `DriverBindings`. Move every operator-related binding into `OperatorBindings`. Constructor of each takes the relevant subsystems plus the relevant `CommandXboxController` and wires the bindings in the constructor body. `RobotContainer` shrinks to subsystem construction + two binding-class instantiations + auto chooser. Target: `RobotContainer` ≤ 100 lines.

---

## The pattern

```java
public final class DriverBindings {
  public DriverBindings(
      CommandXboxController driver,
      Drive drive,
      Vision vision) {

    // Default command: arcade drive (joystick suppliers — Lesson 07)
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(driver::getLeftY, driver::getRightX));

    driver.start().onTrue(Commands.runOnce(drive::resetGyro, drive));
    driver.leftBumper().whileTrue(drive.brakeCommand());
    driver.rightTrigger().whileTrue(vision.snapshotCommand());
  }
}
```

Three things to notice:

1. **No static state, no singletons.** Everything the class needs comes through the constructor. The instance itself can be discarded after construction — the bindings live in the `CommandScheduler`, not in this object.
2. **No return value.** This is a side-effect class. The `new DriverBindings(...)` call *does* the wiring. WPILib calls this pattern "scopes" in some examples; the name doesn't matter.
3. **Only driver-related concerns.** Operator bindings, LED bindings, auto chooser — all go elsewhere.

`OperatorBindings` mirrors the shape:

```java
public final class OperatorBindings {
  public OperatorBindings(
      CommandXboxController operator,
      Intake intake,
      Flywheels flywheels,
      Indexer indexer) {

    intake.gamepieceDetected
          .debounce(0.05)
          .and(operator.a())
          .onTrue(RobotCommands.score(flywheels, indexer));

    operator.b().whileTrue(intake.intakeCommand());
    operator.x().whileTrue(intake.ejectCommand());
  }
}
```

`RobotContainer` becomes legible:

```java
public class RobotContainer {
  private final Drive drive = new Drive();
  private final Vision vision = new Vision();
  private final Intake intake = new Intake();
  private final Flywheels flywheels = new Flywheels();
  private final Indexer indexer = new Indexer();
  private final Leds leds = new Leds();

  private final CommandXboxController driver = new CommandXboxController(0);
  private final CommandXboxController operator = new CommandXboxController(1);

  private final SendableChooser<Command> autoChooser = new SendableChooser<>();

  public RobotContainer() {
    new DriverBindings(driver, drive, vision);
    new OperatorBindings(operator, intake, flywheels, indexer);
    new LedBindings(leds, intake, flywheels);
    configureAutos();
  }

  private void configureAutos() {
    autoChooser.setDefaultOption("Do nothing", Commands.none());
    autoChooser.addOption("Drive + Score",
        SimpleAuto.driveAndScore(drive, flywheels, indexer));
    autoChooser.addOption("S-Curve", PathAuto.scurve(drive));
    SmartDashboard.putData("Auto", autoChooser);
  }

  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }
}
```

That's ~30 lines of meaningful code. You can read it in 15 seconds.

---

## What goes where — and what doesn't

| If a binding involves… | Put it in… |
|---|---|
| Joystick 0 (driver controls) | `DriverBindings` |
| Joystick 1 (operator controls) | `OperatorBindings` |
| LED feedback driven by subsystem state | `LedBindings` |
| Cross-subsystem compositions called by multiple bindings | A `RobotCommands` factory class |
| Auto chooser + auto routines | `RobotContainer` |

`RobotCommands` is a small static class that holds composed commands which multiple bindings reuse:

```java
public final class RobotCommands {
  public static Command score(Flywheels fly, Indexer idx) {
    return Commands.sequence(
        fly.spinUpCommand().withTimeout(1.0),
        idx.feedCommand().withTimeout(0.4)
    ).withName("Score");
  }
  private RobotCommands() {}
}
```

The reason for `RobotCommands` separate from any specific bindings class: the score sequence is used by the operator's A button **and** the auto routines from Lesson 12. Putting it in `OperatorBindings` would force `SimpleAuto` to instantiate `OperatorBindings`, which is nonsense.

!!! warning "Don't push cross-subsystem logic into subsystems"

    The most common wrong refactor at this stage is to write `intake.scoreThenIntake(elevator, flywheels)`. This makes `Intake` aware of `Elevator` and `Flywheels`, breaks the subsystem-as-Lego promise, and makes `Intake` unreusable in any project that doesn't have all three. The whole point of `*Bindings` / `RobotCommands` is to put cross-subsystem orchestration **outside** the subsystems — in classes that exist precisely to compose.

---

## Rubric

`RobotContainerSizeTest` asserts:

1. `RobotContainer.java` is ≤ 100 non-blank, non-comment lines.
2. `DriverBindings` and `OperatorBindings` classes exist in `frc.robot.bindings`.
3. All bindings from prior lessons still fire (the prior tests still pass: `lesson-08`, `lesson-09`, `lesson-11`, `lesson-12`).
4. No `static` fields hold subsystem references.
5. No subsystem class references another subsystem class.

Run locally:

```bash
./gradlew test --tests '*' -DincludeTags='lesson-14,lesson-08,lesson-09,lesson-11,lesson-12'
```

The combined-tag run is deliberate — refactors are only safe if the old tests still pass. **Don't trust the diff; trust the rerun.**

---

## See it run

```bash
./gradlew simulateJava
```

The robot behaves identically to before this lesson. That's the win — pure refactor, observable behavior unchanged, code 60% shorter. Skim through the simulator with every binding and confirm nothing regressed. Then run `git diff --stat` and admire how many lines `RobotContainer.java` lost.

---

## Going further

- Read Presto's [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java). Notice how their bindings region is still inside `RobotContainer` because the file is still readable — 6328 refactored when *they* felt it, not on a fixed line-count rule. Use judgment.
- Extract `RobotCommands` into its own file if it grows past ~10 factories.
- If your auto chooser is more than 5 entries, extract `Autos.java` with a static `Autos.register(SendableChooser<Command>, …)` method.
- Look at WPILib's [Organizing Command-Based Projects](https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html) — most of what this lesson taught is documented there as the recommended structure.

---

??? tip "Full reveal — only open if you're really stuck"

    The two most common refactor mistakes:

    1. **Forgetting `addRequirements` somewhere.** When you move a binding into a new file, lambdas that used `this` (when written inside the subsystem) become explicit subsystem references. If you wrote `Commands.runOnce(drive::resetGyro)` without the `drive` argument, you have no requirement; add it: `Commands.runOnce(drive::resetGyro, drive)`.
    2. **Constructing the bindings class but not retaining the result.** This is actually fine — the bindings are registered with `CommandScheduler` during construction, so the instance can be GC'd. But IntelliJ's "result of constructor call ignored" warning will haunt you. Either suppress it or hold a `private final` reference; the code is correct either way.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 13**
    Path-following intro

    [:octicons-arrow-left-24: Back to lesson 13](../13-path-following/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 15**
    Capstone teleop robot

    [:octicons-arrow-right-24: Continue to lesson 15](../15-capstone-teleop/)

</div>
