# Lesson 08 — Joystick Bindings & Triggers <small>· Stage 1C</small>

<span class="stage-badge">Stage 1C · Lesson 08</span>

*Default commands handle the always-on inputs. But how do you make the flywheel spin only while you're holding the A button?*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1C |
    | **Time** | ~35 min |
    | **Prereqs** | [Lesson 07 — Tank drive wiring](../07-tank-drive/) |
    | **Edits** | `src/main/java/frc/robot/RobotContainer.java` + `subsystems/flywheels/Flywheels.java` |
    | **Tests** | `frc.robot.RobotContainerTest` (`@Tag("lesson-08")`) |
    | **Reference robot** | Presto · [`flywheels/Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) + binding section of [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Use `CommandXboxController` to expose buttons as `Trigger` objects.
2. Bind a command factory to a button with `.onTrue(...)` and `.whileTrue(...)`.
3. Choose between `onTrue`, `onFalse`, `whileTrue`, and `whileFalse` based on what the driver expects.
4. Explain — in words — why `toggleOnTrue` is a bad fit for most driver controls.

---

## The real-world problem

In Lesson 07 you wired the drivetrain's default command. That works for analog inputs (sticks) that change continuously. But discrete events — *"spin up when I press A, stop when I release"* — need a different mechanism.

You *could* write decision logic inside `teleopPeriodic`:

```java
if (driver.getAButton()) flywheels.spinUp(); else flywheels.stop();
```

…and we now know exactly how that ends — back in the `teleopPeriodic` jungle of Lesson 03. Command-based already has the right tool: a **Trigger** is a `BooleanSupplier` you can bind commands to. Buttons on a `CommandXboxController` are already Triggers, ready to use.

---

## What you'll do

Build a tiny `Flywheels` subsystem with a `spinUpCommand()` factory (modeled on Presto's). In `RobotContainer.configureButtonBindings()`, write:

```java
operator.a().whileTrue(flywheels.spinUpCommand());
```

Add a `B` button for intake-eject and an `X` button that fires a one-shot LED flash. Run the sim, mash the buttons, and watch the AdvantageScope plots.

---

## Starter code

The flywheel factory looks identical in shape to Lesson 07's drive factory — same `run(...)` helper, same supplier discipline if you accept a target RPM as a supplier:

```java linenums="1"
public class Flywheels extends SubsystemBase {
  private final FlywheelsIO io;
  private final FlywheelsIOInputsAutoLogged inputs = new FlywheelsIOInputsAutoLogged();

  public Flywheels(FlywheelsIO io) { this.io = io; }

  public Command spinUpCommand() {
    return run(() -> io.setVoltage(Constants.Flywheels.SPIN_UP_VOLTS))
            .finallyDo(() -> io.setVoltage(0.0));
  }
}
```

The `finallyDo` is the key idiom: when the command ends — for any reason, including the button being released — the flywheel coasts to zero. The scheduler guarantees this even if another command interrupts.

---

## The four trigger semantics

`CommandXboxController` exposes each button as a `Trigger`. A `Trigger` is a thin wrapper around a `BooleanSupplier` with four scheduling semantics you'll use constantly:

| Method | What it schedules | When to reach for it |
|---|---|---|
| `.onTrue(cmd)` | Schedules `cmd` once, on the rising edge (false → true). | One-shot effects: flash LEDs, advance a state, fire a confetti cannon. |
| `.onFalse(cmd)` | Schedules `cmd` once, on the falling edge (true → false). | "When the driver releases the button, do exactly one thing." Rare. |
| `.whileTrue(cmd)` | Schedules `cmd` on the rising edge, **cancels it on the falling edge**. | Hold-to-do: spin a flywheel, run an intake, hold a position. |
| `.whileFalse(cmd)` | Same as `.whileTrue`, but for the inverse. | Niche — usually you `.negate()` a Trigger and use `.whileTrue` instead. |

The Lesson 09 composition operators (`andThen`, `withTimeout`, …) are built on these same primitives.

---

## Wiring the bindings

```java linenums="1"
public class RobotContainer {
  private final Drive drive = new Drive(new DriveIOSim());
  private final Flywheels flywheels = new Flywheels(new FlywheelsIOSim());
  private final Intake intake = new Intake(new IntakeIOSim());
  private final LEDs leds = new LEDs();

  private final CommandXboxController driver = new CommandXboxController(0);
  private final CommandXboxController operator = new CommandXboxController(1);

  public RobotContainer() {
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -driver.getLeftY(), driver::getRightX));

    configureButtonBindings();
  }

  private void configureButtonBindings() {
    operator.a().whileTrue(flywheels.spinUpCommand());
    operator.b().whileTrue(intake.intakeNoteCommand());
    driver.x().onTrue(leds.flashCommand());
  }
}
```

A few things worth noticing here:

- `operator.a()` and `driver.x()` *return* `Trigger` objects. They're not booleans you read; they're handles you bind commands to.
- We never call `flywheels.spinUpCommand()` inside the lambda. We hand the *command* to `whileTrue`, and the scheduler decides when to run it.
- Driver and operator are different controllers (ports 0 and 1). A common rookie mistake is binding everything to port 0 — the operator's commands then fight the driver for the same physical stick.

!!! quote "From the WPILib docs"

    *"While this functionality is supported, toggles are not a highly-recommended option for user control, as they require the driver to keep track of the robot state."*

    — [Binding Commands to Triggers · WPILib docs](https://docs.wpilib.org/en/stable/docs/software/commandbased/binding-commands-to-triggers.html)

---

## Why we discourage `toggleOnTrue` for driver controls

`Trigger` has a fifth method, `.toggleOnTrue(cmd)`. It schedules the command on the *first* rising edge, cancels it on the *next* rising edge, schedules it again on the next, and so on. Press to start, press to stop.

It sounds friendly. It is, in practice, a nightmare.

The problem is that the robot's state is no longer visible from the controller. The driver pressed A once — but was that the first press (flywheel now spinning) or the second (flywheel now off)? In a high-stress match they will forget. They will press A "to start the flywheel" and instead they'll stop a flywheel that was already running. They'll think the robot is broken. They will radio you.

For driver controls, prefer `.whileTrue` (hold-to-run, releases when you let go — state is in your hand, literally) or two separate buttons for two separate actions. Save `toggleOnTrue` for engineering-mode panels where a programmer is at the controller and a state indicator is visible.

!!! warning "Anti-pattern preempted"

    `toggleOnTrue` for driver controls. See [Curriculum-Flow §5.7](/process/Curriculum-Flow.md). If a driver needs to "latch" a state, expose two buttons (`enable` / `disable`) or use a `Trigger` from subsystem state, not from the controller alone.

---

## Rubric

`RobotContainerTest` (with `@Tag("lesson-08")`) asserts:

1. Holding `operator.a()` → `Flywheels/Inputs/velocityRPM` rises above `500 RPM` within 1 s; releasing → falls back below `100 RPM` within 2 s.
2. Holding `operator.b()` → `Intake/Inputs/output` is non-zero; releasing → drops to zero immediately.
3. Tapping `driver.x()` → `LEDs/flashing` becomes true for exactly the flash duration and then returns to false (without holding).
4. No binding uses `toggleOnTrue` (verified by reading the command graph).

Run it locally:

```bash
./gradlew test --tests '*RobotContainerTest' -DincludeTags='lesson-08'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope, connect to NT4 at `localhost`, and plot `Flywheels/Inputs/velocityRPM`. Open SimGUI's "System Joysticks" panel and watch the A button highlight while held. The trace should rise on press, decay on release — a clean rectangular envelope when you map both onto the same timeline.

---

## Going further

- Add a third binding: `operator.leftBumper().onFalse(flywheels.coastDownCommand())`. Why might you want to act on the *release* of a bumper rather than the press?
- Read Presto's binding section in [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java). Count how many bindings use `whileTrue` versus `onTrue`. Notice the pattern: *continuous-effect = whileTrue, one-shot = onTrue.*
- Try `.debounce(0.1)` on a button (`operator.a().debounce(0.1).whileTrue(...)`). Why might you want a 100 ms debounce on a physical button? (Lesson 11 covers this.)

??? tip "Full reveal — only open if you're really stuck"

    ```java
    private void configureButtonBindings() {
      operator.a().whileTrue(flywheels.spinUpCommand());
      operator.b().whileTrue(intake.intakeNoteCommand());
      driver.x().onTrue(leds.flashCommand());
    }
    ```

    Three lines. That's the whole point — once factories exist, bindings are one-liners.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 07**
    Tank drive wiring (factories)

    [:octicons-arrow-left-24: Back to lesson 07](../07-tank-drive/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 09**
    Command composition

    [:octicons-arrow-right-24: Continue to lesson 09](../09-command-composition/)

</div>
