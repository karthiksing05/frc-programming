# Lesson 16 — The IO Layer pattern <small>· Stage 2A</small>

<span class="stage-badge">Stage 2A · Lesson 16</span>

*Your subsystems talk directly to motor controllers. That works — until you want to test, swap a vendor, or replay yesterday's match.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2A |
    | **Time** | ~60 min |
    | **Prereqs** | [Lesson 15 — Capstone teleop robot](../../stage1d/15-capstone-teleop/) |
    | **Edits** | Refactor `src/main/java/frc/robot/subsystems/drive/` into `Drive` + `DriveIO` + `DriveIOInputs` + `DriveIOSim` + `DriveIOReal` |
    | **Tests** | `frc.robot.subsystems.drive.DriveTest` (`@Tag("lesson-16")`) — every lesson 07-14 test must still pass after the refactor |
    | **Reference robot** | Presto · [`flywheels/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Define an `XxxIO` interface with an `@AutoLog` inputs class.
2. Write `XxxIOSim` against WPILib's `*Sim` classes — no hardware required.
3. Stub `XxxIOReal` (or a vendor-named variant) for the real robot.
4. Inject the right IO impl per `Constants.currentMode` (`REAL` / `SIM` / `REPLAY`).
5. Push every sensor read through `io.updateInputs(inputs)` + `Logger.processInputs("Drive", inputs)` — the line that makes replay possible.

---

## The real-world problem

Your capstone robot from Lesson 15 works in sim and (probably) on real hardware. But look at `Drive.java`: it `new`s a `TalonFX` directly, calls `motor.set(...)` in `periodic()`, reads encoder ticks straight from the motor. The subsystem and the motor controller are welded together.

This is fine until somebody asks one of these questions:

- *"Can we test this without the robot? It's in the bag."*
- *"We have to swap to SparkFlex — how much code changes?"*
- *"Why did the elevator slam at 0:42 in the quals 3 match? Can you replay it?"*

Each answer is "we'd have to rewrite the subsystem." The IO Layer dissolves all three at once. Presto's [`flywheels/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) is the canonical reference — read it before you start typing.

---

## What you'll do

Refactor your `Drive` subsystem into four files. The behavior — and every test from Lessons 07 through 14 — must be identical after. Diff your `Drive.java` before and after; the subsystem class should *get smaller*, because all the I/O bookkeeping moves to `DriveIOSim` / `DriveIOReal`.

---

## The four-file structure

Look at Presto's flywheels package. Five files do exactly what you're about to do:

```
flywheels/
├── FlywheelsIO.java           ← the interface + @AutoLog inputs
├── FlywheelsIOSim.java        ← WPILib FlywheelSim under the hood
├── FlywheelsIOKrakenFOC.java  ← real hardware (Kraken via Phoenix 6)
├── FlywheelsIOSparkFlex.java  ← real hardware (REV alternative)
└── Flywheels.java             ← the SubsystemBase — uses io, never new TalonFX
```

Your `drive/` package mirrors this. Pick your real-hardware name based on what you actually run. Kelpie uses `DriveIOReal`; Presto uses vendor names. Either is fine — **the interface is what matters.**

!!! info "Why two valid naming styles?"

    Kelpie writes `ElevatorIOReal` ("there is one real implementation; it lives here"). Presto writes `FlywheelsIOKrakenFOC` ("here's the Kraken impl; here's the Spark impl; pick one in `RobotContainer`"). The vendor-named style is honest about the fact that production code often needs multiple real impls. The `Real` style is cleaner pedagogically. Pick one and stay consistent.

---

## Step 1 — Define the inputs

```java linenums="1"
public interface DriveIO {
  @AutoLog
  class DriveIOInputs {
    public double leftPositionMeters = 0.0;
    public double leftVelocityMps = 0.0;
    public double rightPositionMeters = 0.0;
    public double rightVelocityMps = 0.0;
    public double leftAppliedVolts = 0.0;
    public double rightAppliedVolts = 0.0;
    public double[] leftCurrentAmps = new double[] {};
    public double[] rightCurrentAmps = new double[] {};
  }

  default void updateInputs(DriveIOInputs inputs) {}
  default void setVoltage(double leftVolts, double rightVolts) {}
}
```

The `@AutoLog` annotation runs at compile time. AdvantageKit generates a `DriveIOInputsAutoLogged` class with `toLog(...)` and `fromLog(...)` methods — that's the magic that makes replay work. The fields are **plain Java fields**, not getters. Logged-as-a-record-of-state, not as a behavior.

!!! warning "Don't add behavior to the inputs class"

    `DriveIOInputs` is a *data class*. No methods, no constructors that do work, no `Logger.recordOutput` calls. If you need a derived value, expose it on the subsystem (next lesson) or compute it in the IO impl before stuffing it into the inputs.

---

## Step 2 — Write `DriveIOSim`

The sim impl owns a WPILib `DifferentialDrivetrainSim` (or `DCMotorSim` per side). Each `updateInputs(...)` call advances the sim by 20 ms and copies the resulting state into the inputs struct:

```java linenums="1"
@Override
public void updateInputs(DriveIOInputs inputs) {
  sim.setInputVoltage(leftVolts, rightVolts);
  sim.update(0.020);
  inputs.leftPositionMeters = sim.getLeftPositionMeters();
  inputs.leftVelocityMps    = sim.getLeftVelocityMetersPerSecond();
  // ... etc
}
```

Compare to Presto's [`FlywheelsIOSim.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/FlywheelsIOSim.java) — they use `DCMotor.getKrakenX60Foc(1)` to model the motor. Yours doesn't need the same fidelity; what matters is that `updateInputs` produces *some* plausible state.

---

## Step 3 — Stub `DriveIOReal`

In sim, this file is unreachable. In real life, it's where the `TalonFX` lives:

```java linenums="1"
public class DriveIOReal implements DriveIO {
  private final TalonFX leftLeader  = new TalonFX(Constants.Drive.LEFT_ID);
  private final TalonFX rightLeader = new TalonFX(Constants.Drive.RIGHT_ID);

  @Override public void updateInputs(DriveIOInputs inputs) { /* read motors */ }
  @Override public void setVoltage(double leftVolts, double rightVolts) { /* setControl */ }
}
```

If you're sim-only, leave the bodies empty — they won't run. The compile success is the point: the contract holds even if the hardware doesn't.

---

## Step 4 — Pick the impl in `RobotContainer`

```java linenums="1"
DriveIO driveIO = switch (Constants.currentMode) {
  case REAL   -> new DriveIOReal();
  case SIM    -> new DriveIOSim();
  case REPLAY -> new DriveIO() {};   // no-op — replay reads from the log
};
Drive drive = new Drive(driveIO);
```

The `REPLAY` case is a no-op IO. Replay (Lesson 19) reads inputs from the log file, so the impl never runs. We'll come back to this.

---

## Step 5 — The subsystem uses `io`, never the motor

Inside `Drive.java`:

```java linenums="1"
private final DriveIO io;
private final DriveIOInputsAutoLogged inputs = new DriveIOInputsAutoLogged();

@Override
public void periodic() {
  io.updateInputs(inputs);
  Logger.processInputs("Drive", inputs);
  // ... use inputs.leftPositionMeters etc to do work ...
}
```

`Logger.processInputs("Drive", ...)` is the line that earns its keep in Lesson 19. It records the inputs to the log *and* — in replay mode — overwrites them from the log. Either way, the subsystem code is identical.

---

## Rubric

The test class `DriveTest` and every earlier lesson's drive test must pass:

1. `DriveIO` exists as an interface with `@AutoLog DriveIOInputs`.
2. `DriveIOSim` and `DriveIOReal` both `implements DriveIO`.
3. `RobotContainer` picks the impl via `Constants.currentMode`.
4. Lesson 07-14 drive tests all still pass.

```bash
./gradlew test --tests '*DriveTest' -DincludeTags='lesson-16'
```

---

## See it run

```bash
./gradlew simulateJava
```

AdvantageScope's NT tree now shows `Drive/Inputs/leftPositionMeters` (etc.) coming from the *log record*, not raw subsystem code. Behavior is identical to Lesson 15. That's the win: this is a pure refactor.

---

## Going further

- Read Presto's [`Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/Flywheels.java) and count how many times the subsystem touches `io` vs. how many times it touches a motor controller directly. (Answer: only through `io`. That's the rule.)
- Refactor one *more* subsystem the same way — try the elevator. The second time is muscle memory.

!!! tip "The smell test for IO Layer correctness"

    `grep -r "TalonFX\|SparkMax\|CANSparkFlex" src/main/java/frc/robot/subsystems/drive/Drive.java`

    If that returns *any* hits, you missed something. Vendor types should only appear in `DriveIOReal` / `DriveIOKrakenFOC` / etc. The subsystem and `DriveIO` interface should be vendor-clean.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 15**
    Capstone teleop robot

    [:octicons-arrow-left-24: Back to lesson 15](../../stage1d/15-capstone-teleop/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 17**
    AdvantageScope first-class

    [:octicons-arrow-right-24: Continue to lesson 17](../17-advantagescope/)

</div>
