# Lesson 07 — Tank Drive Wiring (Factory Pattern) <small>· Stage 1C</small>

<span class="stage-badge">Stage 1C · Lesson 07</span>

*Your subsystems work. But pressing the joystick should make the robot go — and `setMode(State.DRIVING)` is the wrong abstraction for a value that changes every tick.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1C |
    | **Time** | ~40 min |
    | **Prereqs** | [Lesson 06 — Arm with gravity feedforward](../../stage1b/06-arm-gravity-ff/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/drive/Drive.java` |
    | **Tests** | `frc.robot.subsystems.drive.DriveTest` (`@Tag("lesson-07")`) |
    | **Reference robot** | Presto · [`drive/Drive.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/drive) (theirs is swerve; we use tank for clarity) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Write a `Command`-returning factory method on a subsystem.
2. Use `DoubleSupplier` (and `() -> ...` lambdas) for live joystick input — never captured values.
3. Apply `MathUtils.applyDeadband` (from Lesson 01) inside a running command.
4. Register the factory as a subsystem's default command in `RobotContainer`.

---

## The real-world problem

In Lesson 04 your roller had three discrete states — `OFF`, `INTAKING`, `EJECTING`. You called `setMode(...)` once and the subsystem held that mode. That works because rollers are *modal*: they're either intaking or they aren't.

A drivetrain is different. The joystick value isn't a mode — it's a *signal that changes every 20 ms*. You can't call `drive.setMode(State.DRIVING_AT_0.37)` and expect anything sensible. You need a command that runs continuously, reads the joystick *fresh each tick*, and writes the right voltages to the motors.

Command-based gives you exactly that primitive: a `Command` returned by a factory method on the subsystem, parameterized by *suppliers* that the command queries every iteration.

---

## What you'll do

Open `Drive.java`. Write a factory method:

```java
public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation) { ... }
```

Inside, return `run(() -> ...)` that pulls fresh values from `forward.getAsDouble()` and `rotation.getAsDouble()` each tick, deadbands them, mixes them into left/right demands, and sends voltages through the IO layer. Then wire it as the drive's default command in `RobotContainer`.

The interactive PoC below is the same lesson in a browser sandbox — push the simulated stick, watch the chassis respond, then try the "break it on purpose" exercise at the bottom of this page and see what changes.

<iframe class="lesson-widget"
        src="/examples/tank-drive-poc/index.html"
        width="100%"
        height="720"
        title="Tank drive — interactive PoC"></iframe>

---

## Starter code

```java linenums="1"
public class Drive extends SubsystemBase {
  private final DriveIO io;
  private final DriveIOInputsAutoLogged inputs = new DriveIOInputsAutoLogged();

  public Drive(DriveIO io) { this.io = io; }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Drive", inputs);
  }

  // TODO (LESSON 07): write the factory
  public Command arcadeDriveCommand(
      DoubleSupplier forward, DoubleSupplier rotation) {
    return run(() -> { /* ... */ });
  }
}
```

Notice the subsystem itself stays small. The behavior (`run(() -> ...)`) lives inside a `Command` that the scheduler will call every 20 ms while the command is running.

---

## The factory pattern, explained

A **factory** is a public method on a subsystem that *returns* a `Command`. It doesn't run anything itself — it just hands the scheduler an object that, when scheduled, will execute the code inside the lambda.

```java
public Command arcadeDriveCommand(
    DoubleSupplier forward, DoubleSupplier rotation) {
  return run(() -> {
    double fwd = MathUtils.applyDeadband(forward.getAsDouble(), Constants.Drive.DEADBAND);
    double rot = MathUtils.applyDeadband(rotation.getAsDouble(), Constants.Drive.DEADBAND);
    double left  = fwd + rot;
    double right = fwd - rot;
    io.setVoltage(left * 12.0, right * 12.0);
  });
}
```

Three things to notice:

1. `run(...)` is a helper inherited from `SubsystemBase`. It builds a command that calls the lambda every tick *and* automatically requires this subsystem. No `addRequirements(this)` needed.
2. The lambda body is what runs each tick. Pull suppliers, compute, write to IO.
3. The caller in `RobotContainer` decides *when* to schedule this command. The subsystem doesn't care.

!!! quote "From the WPILib docs"

    *"Through the use of lambdas, these commands can cover almost all use cases and teams should rarely need to write custom command classes."*

    — [Commands · WPILib docs](https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html)

---

## Why Suppliers, not values

This is the single most common subtle bug in beginner command-based code. Suppliers are not optional ceremony — they're the load-bearing reason the command works at all.

```java
// WRONG — joystick is read ONCE, at the moment the factory is called.
drive.arcadeDriveCommand(controller.getLeftY(), controller.getRightX());

// RIGHT — joystick is read every tick, inside the lambda.
drive.arcadeDriveCommand(() -> -controller.getLeftY(), controller::getRightX);
```

In the wrong version, Java evaluates `controller.getLeftY()` once — *at the call site, before the command is ever scheduled* — and passes the resulting `double` (probably `0.0`, since you're not touching the stick yet) to the factory. The lambda captures that one number forever.

In the right version, you pass a *function that knows how to ask the controller for its current value*. The lambda calls that function every tick. The joystick is read fresh, every 20 ms, for as long as the command runs.

!!! warning "The Suppliers-not-captured-values rule"

    Every joystick read inside a command **must** happen inside the lambda, via a `Supplier`/`DoubleSupplier`/`BooleanSupplier`. If you see a raw `double` parameter that came from a controller getter, the command will only ever see the first value. See [Curriculum-Flow §3.2](/process/Curriculum-Flow.md) for the canonical write-up.

---

## Break it on purpose

You don't really understand the supplier pattern until you watch it fail. In the iframe above (or in your own `RobotContainer.java`), delete the `() ->` from the rotation argument:

```diff
- drive.setDefaultCommand(
-     drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> driver.getRightX()));
+ drive.setDefaultCommand(
+     drive.arcadeDriveCommand(() -> -driver.getLeftY(), driver.getRightX()));
```

The code still compiles — `getRightX()` returns a `double`, and `DoubleSupplier` happens to be a `@FunctionalInterface`, so the compiler errors live in a slightly different place depending on how you mangle it. Let your IDE complain and read the error before "fixing" it.

Now run the sim. Drive forward — fine. Try to turn. The robot rotates… exactly as much as the stick was deflected the instant you constructed the default command (i.e., zero). It will *never* turn, because the rotation value is frozen.

Put the `() ->` back. Turning works again. This bug doesn't exist anymore, and you've felt it once instead of debugging it during competition.

---

## Wiring it in `RobotContainer`

```java
private final Drive drive = new Drive(new DriveIOSim());
private final CommandXboxController driver = new CommandXboxController(0);

public RobotContainer() {
  drive.setDefaultCommand(
      drive.arcadeDriveCommand(
          () -> -driver.getLeftY(),    // forward: stick up = +Y down in WPILib axes
          () -> driver.getRightX()));  // rotation
}
```

`setDefaultCommand` tells the scheduler: *"whenever nothing else has the drive subsystem, run this."* When you later bind a different command to a button (Lesson 08), that command takes priority while it runs, and the default resumes when it finishes.

---

## Rubric

`DriveTest` (with `@Tag("lesson-07")`) asserts:

1. Pushing forward on the simulated joystick → both left and right demands become positive and roughly equal.
2. Holding the stick at `0.05` (below deadband) → both demands are `0.0`.
3. Releasing the joystick mid-motion → both demands return to `0.0` within one tick.
4. The factory's lambda reads the supplier multiple times (verified by passing a counting supplier and asserting the count grows across ticks).

Run it locally:

```bash
./gradlew test --tests '*DriveTest' -DincludeTags='lesson-07'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope, connect to NetworkTables 4 at `localhost`, and plot:

- `Drive/Inputs/leftPositionMeters`
- `Drive/LeftDemand`
- `Drive/RightDemand`

Use the keyboard joystick (HALSim "Keyboard 0") for WASD-style driving. Watch the demands respond live to your key presses.

---

## Going further

- Compare your factory to Presto's swerve [`Drive.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/drive). The mechanism is different, but notice they also expose a *command factory* (`runVelocity`, `stopWithX`, etc.) parameterized by suppliers — same pattern, more axes.
- Add a "slow mode" factory `arcadeDriveCommand(forward, rotation, scale)` where `scale` is also a `DoubleSupplier`. Bind it to the left trigger so holding the trigger halves the max voltage.

??? tip "Full reveal — only open if you're really stuck"

    ```java
    public Command arcadeDriveCommand(
        DoubleSupplier forward, DoubleSupplier rotation) {
      return run(() -> {
        double fwd = MathUtils.applyDeadband(
            forward.getAsDouble(), Constants.Drive.DEADBAND);
        double rot = MathUtils.applyDeadband(
            rotation.getAsDouble(), Constants.Drive.DEADBAND);
        io.setVoltage((fwd + rot) * 12.0, (fwd - rot) * 12.0);
      });
    }
    ```

    Try to derive this before peeking — the supplier pattern is muscle memory you want to build now.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 06**
    Arm with gravity feedforward

    [:octicons-arrow-left-24: Back to lesson 06](../../stage1b/06-arm-gravity-ff/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 08**
    Joystick bindings & Triggers

    [:octicons-arrow-right-24: Continue to lesson 08](../08-triggers-bindings/)

</div>
