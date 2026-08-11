# Lesson 28 — System identification (SysId) <small>· Stage 2D</small>

<span class="stage-badge">Stage 2D · Lesson 28</span>

*Lesson 27 made you guess at `kV` and `kA`. The numbers worked, sort of, but you never trusted them. Today the mechanism tells you what its constants actually are.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2D |
    | **Time** | ~60 min |
    | **Prereqs** | [Lesson 27 — Motion profiling](../27-motion-profiling/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java`, `src/main/java/frc/robot/subsystems/drive/Drive.java` |
    | **Tests** | `frc.robot.subsystems.elevator.SysIdRoutineTest` (`@Tag("lesson-28")`) |
    | **Reference robot** | Presto · [`flywheels/Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/Flywheels.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Wire a `SysIdRoutine` onto any subsystem whose hardware accepts a voltage.
2. Run **quasistatic** and **dynamic** tests in both directions and recognize what each one measures.
3. Load the resulting WPILOG into the WPILib SysId tool and extract `kS`, `kV`, `kA`.
4. Drop those constants into `ElevatorFeedforward` / `SimpleMotorFeedforward` and watch your tracking error shrink.
5. Explain — in one sentence — why SysId replaces tuning lore with measurement.

---

## The real-world problem

Every PID lesson so far has ended with "tune kP until it looks right." Every feedforward lesson has handed you a `kG` you took on faith. That style scales until it doesn't: a new mechanism, a different battery, a swapped gearbox, and your hand-tuned numbers no longer apply. The team mythology grows — *"kV is 0.12 because Sam said so in 2024"* — until nobody knows whether the value is wrong or whether the mechanism just feels off this week.

System identification breaks the cycle. You drive the mechanism with known voltage patterns, log position and velocity, and fit a linear model that *describes the physical system itself*. Out fall four numbers:

- **`kS`** — the voltage required to overcome static friction (the "stiction" floor).
- **`kV`** — volts per unit of velocity. Bigger gear ratio, bigger `kV`.
- **`kA`** — volts per unit of acceleration. Bigger mass, bigger `kA`.
- **`kG`** — gravity term (only for arms / elevators).

Once you have them, motion profiling (lesson 27) becomes trivial: your max velocity is whatever voltage budget you have left after `kS` and `kG`, divided by `kV`. Tuning stops being lore and becomes arithmetic.

---

## What you'll do

You'll add SysId routines to two subsystems: the elevator (vertical mechanism, gravity matters) and the drive (horizontal mechanism, gravity doesn't). Each routine emits four commands — quasistatic forward, quasistatic reverse, dynamic forward, dynamic reverse — which you'll bind to four buttons on the operator controller and run in sequence with the simulator open.

```java
private final SysIdRoutine sysId = new SysIdRoutine(
    new SysIdRoutine.Config(
        Volts.of(1.0).per(Second),   // ramp rate for quasistatic
        Volts.of(7.0),               // step voltage for dynamic
        Seconds.of(4),               // timeout
        state -> Logger.recordOutput("Elevator/SysIdState", state.toString())),
    new SysIdRoutine.Mechanism(
        voltage -> io.setVoltage(voltage.in(Volts)),
        log -> log.motor("elevator")
            .voltage(Volts.of(inputs.appliedVolts))
            .linearPosition(Meters.of(inputs.positionMeters))
            .linearVelocity(MetersPerSecond.of(inputs.velocityMetersPerSec)),
        this));
```

The IO Layer pattern from lesson 16 pays off here: SysId only needs a way to set voltage and a way to read motion. Your `ElevatorIO` already exposes both, so wiring SysId is mechanical.

---

## Quasistatic vs. dynamic — what they measure

The two test types feel similar in sim and look almost identical on the graphs. Internally they're measuring opposite ends of the same model.

!!! example "Quasistatic — for kS and kV"

    A **slow** voltage ramp (typically 1 V/s). Acceleration is small enough that the `kA` term is negligible — the equation reduces to *voltage ≈ kS + kV · velocity*. Plot voltage against velocity, fit a line, and the y-intercept is `kS`, the slope is `kV`.

!!! example "Dynamic — for kA"

    A **step** voltage (typically 7 V). Acceleration is enormous at the start. With `kS` and `kV` already known from the quasistatic test, *voltage − kS − kV · velocity ≈ kA · acceleration* — and the leftover voltage divided by acceleration is `kA`.

Run each test in both directions. Friction is often asymmetric (gravity-loaded elevators are an obvious case, but even horizontal drives can have hot bearings on one side). The SysId tool fits a single `kS` and reports an "ideal direction" warning if forward and reverse disagree wildly.

!!! warning "Mind the travel limits"

    Your elevator has finite range. If a 7 V dynamic step runs for 4 seconds, you will hit the top — hard. Wrap the routine commands with `.until(() -> elevator.isNearTop())` for the forward direction and the reverse for the down direction, **before** binding to buttons. SysId has a built-in `.quasistatic(Direction)` method; the `.until` decorator goes around the returned command.

---

## Loading the log

The routine writes a WPILOG by default. After your four runs:

1. Open the WPILib SysId tool (it ships in the WPILib install, alongside the simulation tools).
2. Load the WPILOG.
3. Tell it which test segment is which (the `state -> Logger.recordOutput` callback tags them; the tool can auto-detect from the tags).
4. Pick "Arm" for the shoulder, "Elevator" for the elevator, "Simple" for the drive.
5. The tool fits the model and spits out `kS`, `kV`, `kA`, and (for arm/elevator) `kG`.

Copy those into `Constants.Elevator` and `Constants.Drive`. Re-run lesson 05's elevator test. Watch the tracking error drop without you touching `kP`.

---

## Rubric

`SysIdRoutineTest` asserts:

1. All four routines (quasistatic ±, dynamic ±) complete without throwing.
2. The produced log contains motor entries for both elevator and drive.
3. After you've pasted the extracted constants into `Constants.java`, the lesson-05 elevator test still passes (the model didn't break anything), and the tracking-error metric on a four-setpoint sweep is at least 25% better than the lesson-05 baseline.

```bash
./gradlew test --tests '*SysIdRoutineTest' -DincludeTags='lesson-28'
```

---

## See it run

```bash
./gradlew simulateJava
```

Bind quasistatic forward to operator A, quasistatic reverse to B, dynamic forward to X, dynamic reverse to Y. Drive the simulated robot to a safe area. Hold each button in turn for about four seconds. AdvantageScope plots `Elevator/SysIdState` as a discrete signal, so you can verify the routine progresses through `quasistatic-forward → quasistatic-reverse → dynamic-forward → dynamic-reverse`.

---

## Going further

- Run SysId on the **flywheel** subsystem. Velocity-only systems (no position term) use the "Simple" model. Compare your extracted `kV` to Presto's hardcoded value in `FlywheelsConstants` — are they in the same ballpark?
- Read [WPILib's SysId guide](https://docs.wpilib.org/en/stable/docs/software/pathplanning/system-identification/index.html) and find the section on "characterizing a swerve drive." Lesson 21's swerve modules are the next natural candidate.
- Write a Gradle task that fails CI if `Constants.Elevator.kV` deviates from the value in the latest SysId log by more than 10%. Drift detection beats faith.

---

??? tip "Full reveal — quasistatic + .until safety wrapper"

    ```java
    public Command sysIdQuasistatic(SysIdRoutine.Direction dir) {
      return sysId.quasistatic(dir)
          .until(() -> dir == SysIdRoutine.Direction.kForward
                       ? inputs.positionMeters > MAX_HEIGHT - 0.1
                       : inputs.positionMeters < 0.05);
    }
    ```

    The same pattern wraps `sysId.dynamic(dir)`. Then your bindings stay short: `operator.a().whileTrue(elevator.sysIdQuasistatic(Direction.kForward));`

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 27**
    Motion profiling

    [:octicons-arrow-left-24: Back to lesson 27](../27-motion-profiling/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 29**
    Advanced state machines

    [:octicons-arrow-right-24: Continue to lesson 29](../29-state-machines/)

</div>
