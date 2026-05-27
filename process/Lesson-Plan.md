# Lesson Plan
### The 34 lessons of FRCProgramming.org — Stage 0 through Stage 2D — fully specified

> **What this document is.** The complete syllabus. Each lesson is a block with everything a content author needs to write it: the pain it solves, the file the student edits, the test rubric, the reference-robot file to point at, the AdvantageScope view to ship, and the anti-patterns it preempts.
>
> **Not** the lesson content itself — that lives in `lessons/<slug>/README.md` in the actual project. This is the spec each content author works against. Per-lesson block format is consistent so an author can write a lesson by filling in a known template.

> **Source-of-truth dependencies.** This doc weaves three together: the architecture in [Infrastructure-Analysis.md](Infrastructure-Analysis.md), the pedagogy in [Curriculum-Flow.md](Curriculum-Flow.md), and the reference robots in [Reference-Robots.md](Reference-Robots.md). If any of those changes meaningfully, update this. The shape of the lesson template itself is set by [Path-B-Implementation.md §15](Path-B-Implementation.md#15-appendix-b--lesson-template-spec-one-page).

---

## How to read each lesson block

```
### Lesson NN — Title
| Stage / Time / Prereqs / Edits / Tests / Reference robot files | ← header table

Pain solved      ← one-line Niwiden-style framing
Objectives       ← 3-5 bullets: what the student can do at the end
What they do     ← 1-2 sentences: the concrete task
Rubric           ← numbered list of what the test asserts
See it in sim    ← what runs, what AdvantageScope shows
Introduces       ← concepts new in this lesson
Reinforces       ← concepts re-touched
Preempts         ← anti-patterns this lesson defangs
Not taught       ← explicit deferrals
Resources        ← 1-3 links
```

The **Reference column** points at specific files in [Kelpie (8033 Reefscape)](https://github.com/HighlanderRobotics/Reefscape) or [Presto (6328 Crescendo 2024)](https://github.com/Mechanical-Advantage/RobotCode2024Public) so a student can compare their toy code to real production code.

---

## Master table

| # | Title | Stage | Time | Robot ref | Mechanism focus |
|---:|---|:---:|---:|---|---|
| 0A | First-run install | 0 | 60m | — | — |
| 0B | Meet Presto | 0 | 15m | Presto | overview |
| 0C | Meet Kelpie | 0 | 15m | Kelpie | overview |
| 0D | Git + project tour | 0 | 30m | — | — |
| 01 | Methods (Functions) | 1A | 25m | both | joystick math |
| 02 | Variables & types | 1A | 30m | Presto | flywheel constants |
| 03 | Conditionals in `teleopPeriodic` | 1A | 35m | Kelpie | beam-break + roller |
| 04 | Subsystems as state machines | 1B | 45m | Kelpie | roller subsystem |
| 05 | PID introduction (Elevator) | 1B | 50m | Kelpie | elevator |
| 06 | Arm with gravity feedforward | 1B | 45m | Kelpie | shoulder/wrist |
| 07 | Tank drive wiring (factories) | 1C | 40m | both | drivetrain |
| 08 | Joystick bindings & Triggers | 1C | 35m | Presto | shooter button |
| 09 | Command composition | 1C | 40m | Presto | sequence to score |
| 10 | Telemetry & AdvantageScope basics | 1C | 30m | Presto | flywheel speed plot |
| 11 | Default commands done right | 1D | 35m | both | drive default |
| 12 | Auto routines (basic) | 1D | 40m | Presto | two-step auto |
| 13 | Path-following intro | 1D | 50m | Kelpie | drive S-curve |
| 14 | Refactoring with `*Bindings` classes | 1D | 45m | Presto | extracting bindings |
| 15 | Capstone teleop robot | 1D | 90m | both | full robot |
| 16 | The IO Layer pattern | 2A | 60m | Presto | refactor drive |
| 17 | AdvantageScope first-class | 2A | 40m | Presto | mechanism2d + 3D |
| 18 | AdvantageKit logging discipline | 2A | 35m | Presto | Logger.recordOutput |
| 19 | Log replay for debugging | 2A | 50m | Presto | REPLAY mode |
| 20 | Subsystem composition at scale | 2A | 50m | Kelpie | superstructure |
| 21 | Swerve drivetrain (intro) | 2B | 75m | Kelpie | swerve modules |
| 22 | Odometry & pose estimation | 2B | 55m | Kelpie | gyro + module pos |
| 23 | Trajectory following | 2B | 60m | Kelpie | Choreo path |
| 24 | PhotonVision single-tag | 2C | 45m | both | apriltag basics |
| 25 | Multi-tag pose estimation | 2C | 60m | Presto | apriltagvision |
| 26 | maple-sim & game-piece physics | 2C | 55m | Kelpie | swerve sim upgrade |
| 27 | Motion profiling | 2D | 55m | Presto | profiled arm |
| 28 | System identification (SysId) | 2D | 60m | Presto | flywheel kV/kA |
| 29 | Advanced state machines | 2D | 50m | Presto | climber sequence |
| 30 | Season capstone | 2D | 180m | both | full integration |

---

## Stage 0 — Onboarding

The student hasn't written a line of code yet. Goal: install once, see two real robots, understand the project layout, never come back here.

### Lesson 0A — First-run install

| | |
|---|---|
| Stage | 0 |
| Time | ~60 min (40 + 20 min waiting on downloads) |
| Prereqs | None |
| Edits | (none) |
| Tests | (none) |
| Reference | — |

**Pain solved.** First-time install is the single biggest funnel killer in FRC. Students who walk away here never come back. We make it impossible to bounce.

**Objectives.** By the end the student has:
- WPILib 2026 installed (the bundled JDK + bundled VS Code, NOT system Java)
- AdvantageScope downloaded and runnable
- Git installed and `git --version` works
- Cloned the curriculum template repo
- Run `./tools/frcprog.sh doctor` → all green

**What they do.** Follow a one-page screenshot-heavy guide. Run `frcprog doctor` at the end; the script tells them what's wrong if anything is.

**Rubric.** `frcprog doctor` returns exit 0.

**See it.** N/A — this is install, not code.

**Introduces.** The terminal as a tool. WPILib's bundled-everything model.

**Preempts.** JDK mismatch (system Java overriding WPILib's bundled JDK). Wrong VS Code (system Code instead of WPILib's). Cloning into OneDrive-synced folder. Forgetting `chmod +x gradlew` on macOS/Linux.

**Not taught.** Java syntax. WPILib APIs. Git beyond `clone`.

**Resources.** [WPILib install guide](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html) · [AdvantageScope releases](https://github.com/Mechanical-Advantage/AdvantageScope/releases) · [Infrastructure-Analysis.md §3.10](Infrastructure-Analysis.md#310-what-path-b-costs-the-student--the-honest-friction-list) (friction list — every item shows up here)

---

### Lesson 0B — Meet Presto

| | |
|---|---|
| Stage | 0 |
| Time | ~15 min |
| Prereqs | 0A |
| Edits | (none — observation lesson) |
| Tests | (none) |
| Reference | All of [Presto](https://github.com/Mechanical-Advantage/RobotCode2024Public) |

**Pain solved.** "What does the code I'm going to write *look like* in the wild?" New programmers benefit enormously from seeing the destination before walking toward it.

**Objectives.** By the end the student can:
- Name Presto's mechanisms (swerve drive, flywheels, rollers, arm, climber, backpack)
- Open Presto's GitHub and locate `flywheels/FlywheelsIO.java`
- Open AdvantageScope, load Presto's `.glb` 3D model, see it rendered

**What they do.** Watch a 5-minute video of Presto scoring at champs. Read a 1-page tour. Open AdvantageScope and import the Presto layout.

**Rubric.** None — checkbox lesson.

**See it.** AdvantageScope with `Robot_Presto/model.glb` loaded; the 3D field view shows a rendered Presto.

**Introduces.** AdvantageKit, AdvantageScope (by sight), the IO Layer file naming (`FlywheelsIO`, `FlywheelsIOSim`, `FlywheelsIOKrakenFOC`).

**Preempts.** Imposter syndrome ("real teams' code is too scary"). The lesson is literally "look at this, it's not magic."

**Not taught.** Anything you'd be tested on.

**Resources.** [Reference-Robots.md §3](Reference-Robots.md#3-the-shooter-robot-6328-presto-crescendo-2024) · [Team 6328 build thread for 2024 Crescendo](https://www.chiefdelphi.com/c/general/build-blogs/189)

---

### Lesson 0C — Meet Kelpie

| | |
|---|---|
| Stage | 0 |
| Time | ~15 min |
| Prereqs | 0A |
| Edits | (none) |
| Tests | (none) |
| Reference | All of [Kelpie](https://github.com/HighlanderRobotics/Reefscape) |

**Pain solved.** Same as 0B but for the other robot.

**Objectives.** By the end the student can:
- Name Kelpie's mechanisms (swerve, elevator, shoulder, wrist, roller, funnel, climber)
- Locate `elevator/ElevatorIO.java` in the repo
- Articulate one difference between Kelpie's and Presto's organization (`IOReal` vs vendor-named impls)

**What they do.** Watch a 5-minute video of Kelpie placing coral. Tour the repo. Note that Kelpie includes `maple-sim` while Presto doesn't.

**Rubric.** None.

**See it.** AdvantageScope with `Mechanism2d` visualization of an articulated arm/elevator (since Kelpie has no `.glb` yet — see [Reference-Robots.md §5.3](Reference-Robots.md#53-decide-on-kelpies-3d-model-strategy)).

**Introduces.** That FRC has both shooter and pick-and-place games; that swerve is the modern drivetrain default; that maple-sim is an optional power-up for serious sim work.

**Preempts.** "There's only one way to organize code" — by showing two valid styles side-by-side.

**Not taught.** Anything tested.

**Resources.** [Reference-Robots.md §2](Reference-Robots.md#2-the-pick-and-place-robot-8033-kelpie-reefscape-2025) · [Team 8033 Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training)

---

### Lesson 0D — Git + project tour

| | |
|---|---|
| Stage | 0 |
| Time | ~30 min |
| Prereqs | 0A, 0B, 0C |
| Edits | (just a README.md to verify push works) |
| Tests | (none) |
| Reference | Project root + `path-b-demo/` layout |

**Pain solved.** Git is taught as ritual ("run these three commands when you want to save your work") not as concept. Students who hit Git anxiety here never write code.

**Objectives.** By the end the student can:
- `git add`, `git commit -m`, `git push` from VS Code's source-control panel
- Locate `src/main/java/frc/robot/` and explain the package layout
- Find `lessons/<slug>/README.md` for the next lesson

**What they do.** Edit a `README.md` line. Stage. Commit. Push. See the change on GitHub.com.

**Rubric.** A push reaches GitHub. (Detected by repo state, not by JUnit.)

**See it.** N/A.

**Introduces.** Project layout (`src/main/java/frc/robot/`, `lessons/`, `tools/`). The three-command Git ritual.

**Preempts.** Git rabbit holes (rebase, merge conflicts, branching strategies) on day one.

**Not taught.** Branching, merge conflicts, pull requests, `.gitignore`, the staging area concept. All deferred to when they bite.

**Resources.** [GitHub Docs — Git basics](https://docs.github.com/en/get-started/using-git/about-git) — link only; don't make them read it now.

---

## Stage 1A — Java fundamentals (in robot context)

Three lessons. By the end the student has written one helper method, defined some constants, and felt the pain of doing everything in `teleopPeriodic`. The third lesson is *deliberately* the anti-pattern.

### Lesson 01 — Methods (Functions)

| | |
|---|---|
| Stage | 1A |
| Time | ~25 min |
| Prereqs | 0D |
| Edits | `src/main/java/frc/robot/util/MathUtils.java` |
| Tests | `frc.robot.util.MathUtilsTest` (`@Tag("lesson-01")`) |
| Reference | Both robots use `MathUtil.applyDeadband` from WPILib stdlib; show the [WPILib source](https://github.com/wpilibsuite/allwpilib/blob/main/wpimath/src/main/java/edu/wpi/first/math/MathUtil.java) as "your code, but production" |

**Pain solved.** Joystick noise: even at rest, sticks read ±0.05 — robot creeps. Without a deadband, the student's robot wanders the field.

**Objectives.** By the end the student can:
- Write a `public static` method with parameters and a return value
- Call a method from another class with `ClassName.method(args)`
- Recognize when a piece of math appears more than once → extract to a method
- Use `Math.abs` and the ternary operator (or `if`/`return`)

**What they do.** Fill in the body of `applyDeadband(double value, double threshold)` so the noisy joystick visualization in the sim stabilizes at rest.

**Rubric.**
1. Returns `0.0` when `Math.abs(value) < threshold` (positive input)
2. Same for negative input
3. Returns `value` when `Math.abs(value) >= threshold`
4. Works with a non-default threshold (e.g., `0.20`)
5. Returns `0.0` for zero input

**See it in sim.** `./gradlew simulateJava`. AdvantageScope plots `RealOutputs/Joystick/Inputs/rawValue` (red, jittery) and `Lesson01/Output` (green). Push past the threshold and the green trace tracks; release and it snaps to zero.

**Introduces.** `public static`, parameters, return types, `Math.abs`, the ternary operator.

**Reinforces.** Reading code (the stub).

**Preempts.** Copy-pasting the same calculation in multiple places.

**Not taught.** Inheritance. Visibility modifiers beyond `public`. Generic methods. Method overloading. Exceptions.

**Resources.** [WPILib `MathUtil.applyDeadband` source](https://github.com/wpilibsuite/allwpilib/blob/main/wpimath/src/main/java/edu/wpi/first/math/MathUtil.java) · The browser PoC of this same lesson: [functions-poc/](../examples/functions-poc/)

---

### Lesson 02 — Variables & types

| | |
|---|---|
| Stage | 1A |
| Time | ~30 min |
| Prereqs | 01 |
| Edits | `src/main/java/frc/robot/Constants.java` |
| Tests | `frc.robot.ConstantsTest` (`@Tag("lesson-02")`) |
| Reference | Presto: [`flywheels/FlywheelsConstants` — see how 6328 organizes constants per-subsystem](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) |

**Pain solved.** Lesson 01's `0.1` threshold appears in two places. When the student wants to change it, they have to find both. They will *miss one*.

**Objectives.** By the end the student can:
- Declare `public static final` constants
- Use primitive types appropriately (`double` for measurements, `int` for counts)
- Organize related constants in nested classes (`Constants.Drive`, `Constants.Flywheels`, …)
- Refactor a magic number into a named constant

**What they do.** Extract `0.1` (the deadband threshold) into `Constants.Drive.DEADBAND`. Update the call sites. Add three more constants the next lessons will need (gear ratio, max voltage, max RPM).

**Rubric.**
1. `Constants.Drive.DEADBAND == 0.10`
2. `MathUtils.applyDeadband` still passes its lesson-01 tests
3. Constants are `public static final` (verified via reflection)
4. Constants live in a nested class, not the root of `Constants`

**See it.** Same sim as lesson 01; numbers come from `Constants.Drive` instead of literals.

**Introduces.** `static final`, primitive types, nested classes, named constants over magic numbers.

**Reinforces.** Methods (the call site still uses lesson-01's method).

**Preempts.** Magic numbers scattered through code. "I changed the gear ratio in one place but the robot still drives wrong" debugging sessions.

**Not taught.** `enum` constants (lesson 04). Records. Configuration files (Stage 2 backup option). Tunable numbers via NetworkTables (Stage 2A).

**Resources.** [WPILib docs — basic programming](https://docs.wpilib.org/en/stable/docs/software/basic-programming/index.html) · [Curriculum-Flow.md §7.4 on `Constants` vs records vs config](Curriculum-Flow.md#74-constantsjava-vs-records-vs-config-files)

---

### Lesson 03 — Conditionals in `teleopPeriodic` (the deliberate anti-pattern)

| | |
|---|---|
| Stage | 1A |
| Time | ~35 min |
| Prereqs | 02 |
| Edits | `src/main/java/frc/robot/Robot.java` (specifically `teleopPeriodic()`) |
| Tests | `frc.robot.RobotTeleopTest` (`@Tag("lesson-03")`) |
| Reference | Kelpie: `roller/RollerSubsystem.java` (so the student sees what they're *not* doing yet) |

**Pain solved.** None — *this lesson IS the pain.* Per [Niwiden's pedagogy](Curriculum-Flow.md#1-the-pedagogical-thesis-pain-before-abstraction), the student writes a hairy `teleopPeriodic()` so lesson 04 feels like relief.

**Objectives.** By the end the student can:
- Read a beam-break sensor (boolean)
- Read a button press from `XboxController`
- Write `if`/`else if`/`else` chains
- Drive a motor based on combined sensor + button state
- **Recognize that this is getting hard to read** (the actual learning goal)

**What they do.** Inside `teleopPeriodic()`: if the operator holds the B button AND the beam-break sees no game piece, run the roller at +0.6. If the beam-break sees a game piece, stop the roller. If the X button is held, eject at -0.6 regardless of the beam-break.

**Rubric.**
1. Hold B, no piece → roller +0.6
2. Hold B, piece present → roller 0
3. Hold X → roller −0.6 (overrides B)
4. Release everything → roller 0

**See it.** SimGUI's "PWM 5" widget shows roller output as the student presses buttons. Lesson copy explicitly observes: *"Notice your `teleopPeriodic` is now 25 lines and growing. Next lesson, we fix this."*

**Introduces.** Boolean sensor reads, button reads, `if`/`else if`/`else`, motor output bounded to ±1.

**Reinforces.** Methods (`MathUtils.applyDeadband` still in use elsewhere).

**Preempts.** Nothing — the lesson is the anti-pattern. The *next* lesson preempts.

**Not taught.** Subsystems. Commands. Triggers. Anything that would solve the disorganization students just felt.

**Resources.** Kelpie's [`roller/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller) — "this is what we're refactoring toward."

---

## Stage 1B — Subsystems & PID

The transition stage. `teleopPeriodic` jungle → organized subsystems. PID becomes "make the motor reach a number," not "control theory."

### Lesson 04 — Subsystems as state machines

| | |
|---|---|
| Stage | 1B |
| Time | ~45 min |
| Prereqs | 03 |
| Edits | `src/main/java/frc/robot/subsystems/roller/RollerSubsystem.java` |
| Tests | `frc.robot.subsystems.roller.RollerSubsystemTest` (`@Tag("lesson-04")`) |
| Reference | Kelpie: [`roller/RollerSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller) — line-by-line analog of what student is building |

**Pain solved.** Lesson 03's `teleopPeriodic` is 25 lines and unreadable. Today we lift it into a real `SubsystemBase`.

**Objectives.** By the end the student can:
- Write a `class Foo extends SubsystemBase`
- Use `private final` for hardware fields
- Define a public state-setting API (`setMode(State.INTAKING)`)
- Implement `periodic()` that reads inputs and commands motors based on state

**What they do.** Create `RollerSubsystem` with an `enum State { OFF, INTAKING, EJECTING }` and a `setMode(State)` method. `periodic()` reads the beam-break and drives the motor accordingly. `Robot.java` shrinks back to ~5 lines.

**Rubric.**
1. `RollerSubsystem extends SubsystemBase`
2. Motor and beam-break fields are `private final`
3. `setMode(State.INTAKING)` runs the motor at +0.6 *unless* beam-break sees a piece
4. `setMode(State.EJECTING)` runs the motor at −0.6 unconditionally
5. `setMode(State.OFF)` stops the motor
6. `Robot.teleopPeriodic()` is now `roller.setMode(...)` and nothing else

**See it.** SimGUI shows the same PWM behavior as lesson 03 — but now `Robot.java` is clean. AdvantageScope plots `Roller/state` (enum string) and `Roller/motorOutput`.

**Introduces.** `SubsystemBase`, `private final` discipline, `periodic()` lifecycle, `enum` types, state-machine pattern.

**Reinforces.** Constants (motor port from `Constants.Roller`). Conditionals (state-switch logic).

**Preempts.** "Everything in `teleopPeriodic`" (Curriculum-Flow §5.1). Public mutable hardware. Magic motor IDs.

**Not taught.** Commands. Triggers. `CommandScheduler`. Subsystem requirements. Default commands.

**Resources.** [WPILib Subsystems doc](https://docs.wpilib.org/en/stable/docs/software/commandbased/subsystems.html) · Kelpie's [`roller/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller) (slightly more advanced — has IO Layer, but the subsystem class itself reads similarly)

---

### Lesson 05 — PID introduction (Elevator)

| | |
|---|---|
| Stage | 1B |
| Time | ~50 min |
| Prereqs | 04 |
| Edits | `src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java` |
| Tests | `frc.robot.subsystems.elevator.ElevatorTest` (`@Tag("lesson-05")`) |
| Reference | Kelpie: [`elevator/ElevatorSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/elevator) + `ElevatorIOSim.java` |

**Pain solved.** Setting `motor.set(1.0)` and hoping it stops near the target — without PID, the elevator either misses or oscillates wildly.

**Objectives.** By the end the student can:
- Use WPILib's `PIDController`
- Tune `kP`, `kI`, `kD` by observing graphs
- Read encoder position from an IOInputs struct
- Send voltage (not normalized output) to a motor

**What they do.** Wire `kP`/`kI`/`kD` constants into a `PIDController`; in `periodic()`, call `pid.calculate(currentHeight, setpoint)` and pass the result to `io.setVoltage(...)`. Then tune until the elevator reaches four setpoints (stow, low, mid, high) without overshoot. The browser PoC [elevator-pid-poc/](../examples/elevator-pid-poc/) is the visual hint.

**Rubric.**
1. Elevator reaches each setpoint within ±2 cm
2. Settles within 1.5 s for each setpoint
3. Doesn't oscillate (no >5 cm overshoot)
4. `Lesson05/Pass` boolean stays true through a full 4-setpoint sweep

**See it.** AdvantageScope plots `Elevator/Inputs/positionMeters` and `Elevator/setpointMeters` on one chart; a `Mechanism2d` visualizes the elevator going up and down. Reaching each setpoint cleanly looks like a step response that settles fast.

**Introduces.** `PIDController`, kP/kI/kD intuition, the "voltage not throttle" rule, encoder reads from inputs.

**Reinforces.** Subsystems, constants, `periodic()`.

**Preempts.** Bang-bang control. Using `motor.set(throttle)` instead of `setVoltage`. Tuning constants in source and forgetting which value worked.

**Not taught.** Feedforward (lesson 06). Motion profiles (lesson 27). PID theory (we teach intuition + tuning recipe). `ProfiledPIDController` (lesson 27).

**Resources.** [WPILib PID overview](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-pid.html) · [elevator-pid-poc/](../examples/elevator-pid-poc/) (browser version) · [WPILib `ElevatorSim` docs](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/physics-sim.html)

---

### Lesson 06 — Arm with gravity feedforward

| | |
|---|---|
| Stage | 1B |
| Time | ~45 min |
| Prereqs | 05 |
| Edits | `src/main/java/frc/robot/subsystems/shoulder/ShoulderSubsystem.java` |
| Tests | `frc.robot.subsystems.shoulder.ShoulderTest` (`@Tag("lesson-06")`) |
| Reference | Kelpie: [`shoulder/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/shoulder) |

**Pain solved.** Lesson 05's elevator had constant gravity force. An arm's gravity load depends on its angle (`cos(theta)`). Pure PID can compensate, but only at the cost of long-tail integral wind-up.

**Objectives.** By the end the student can:
- Use `ArmFeedforward` (or a custom `kG * cos(angle)` calculation)
- Combine PID output + feedforward output → motor voltage
- Distinguish "constant gravity" (elevator) from "angle-dependent gravity" (arm)

**What they do.** Add a `kG` constant. In `periodic()`, compute `ffVolts = kG * Math.cos(currentAngle)` and add to the PID output before sending to the motor.

**Rubric.**
1. Arm holds horizontal under gravity within ±2°
2. Arm reaches three setpoints (down, mid, up) within tolerance
3. With `kG = 0`, the rubric fails (proves feedforward is doing work)

**See it.** AdvantageScope `Mechanism2d` shows a pivoting arm. The "with kG = 0" exercise is graphed side-by-side so students see how much PID has to fight when feedforward is absent.

**Introduces.** Feedforward concept, `ArmFeedforward` class, angle-dependent compensation, the "FF + PID" combined-output pattern.

**Reinforces.** PID tuning, voltage output, subsystem periodic.

**Preempts.** "Just crank kP higher" — the rookie response when integral wind-up creates steady-state error.

**Not taught.** `kS` (static friction) or `kV` (velocity FF) for the arm — kept to `kG` only to focus the lesson. `ElevatorFeedforward` (mentioned in passing). Coriolis/centripetal for double-pivot arms.

**Resources.** [WPILib feedforward docs](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/feedforward.html) · [SingleJointedArmSim Javadoc](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj/simulation/SingleJointedArmSim.html)

---

## Stage 1C — My first robot (basic command-based)

Subsystems exist. Now commands coordinate them. **Factories from day one; no Command subclasses.**

### Lesson 07 — Tank drive wiring (factory pattern)

| | |
|---|---|
| Stage | 1C |
| Time | ~40 min |
| Prereqs | 06 |
| Edits | `src/main/java/frc/robot/subsystems/drive/Drive.java` |
| Tests | `frc.robot.subsystems.drive.DriveTest` (`@Tag("lesson-07")`) |
| Reference | Presto: [`drive/Drive.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/drive) (theirs is swerve, not tank — we use tank for simplicity) |

**Pain solved.** Pressing the joystick should make the robot go. Doing this through `setMode(State.DRIVING)` is silly when the speed varies smoothly. We need a different abstraction: a *command* that runs continuously while a stick is pushed.

**Objectives.** By the end the student can:
- Write a `Command`-returning factory method on a subsystem
- Use `Suppliers` (`DoubleSupplier`, lambdas) for live joystick input — **never captured values**
- Apply `MathUtils.applyDeadband` (lesson 01!) to live joystick input
- Wire the factory into `RobotContainer` as a default command

**What they do.** Inside `Drive.java`, write `public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation)` that uses `run(() -> ...)` and applies deadband + arcade-drive mixing each tick. Wire it as the drive subsystem's default command in `RobotContainer`.

**Rubric.**
1. Pushing forward on the joystick → robot moves forward
2. Holding the stick at 0.05 → robot doesn't move (deadband)
3. Releasing the joystick mid-motion → robot stops
4. `Drive/LeftDemand` and `Drive/RightDemand` plots respond live to the supplier (verified by reading values across multiple ticks)

**See it.** AdvantageScope `Mechanism2d` shows a top-down tank chassis. WASD = simulated joystick (HALSim's Keyboard 0). Push forward, robot drives forward; release, it stops.

**Introduces.** Factory pattern (`public Command xxxCommand(...)`), `run(() -> ...)`, `DoubleSupplier`, default commands.

**Reinforces.** `applyDeadband` (lesson 01), constants (lesson 02), subsystems (lesson 04).

**Preempts.** **Captured joystick values** — the canonical command-based bug. The lesson includes a "break it on purpose" exercise: remove `() ->` and watch what happens.

**Not taught.** `Trigger` (lesson 08). Multi-subsystem composition (lesson 09). `Command` subclasses (lesson 27 only). Auto driving (lesson 12).

**Resources.** [Oblarg's 2025 best practices](https://www.chiefdelphi.com/t/command-based-best-practices-for-2025-community-feedback/465602) · [BoVLB distillation](https://bovlb.github.io/frc-tips/commands/best-practices.html) · [tank-drive-poc/](../examples/tank-drive-poc/)

---

### Lesson 08 — Joystick bindings & Triggers

| | |
|---|---|
| Stage | 1C |
| Time | ~35 min |
| Prereqs | 07 |
| Edits | `src/main/java/frc/robot/RobotContainer.java` |
| Tests | `frc.robot.RobotContainerTest` (`@Tag("lesson-08")`) |
| Reference | Presto: [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java) — look at the binding section |

**Pain solved.** "How do I make the flywheel spin when I hold the A button?" Lesson 07 only handled live-input driving (default command). Discrete events need a different mechanism.

**Objectives.** By the end the student can:
- Bind a command to a `CommandXboxController` button using `.onTrue` and `.whileTrue`
- Recognize the four core trigger semantics: `onTrue`, `onFalse`, `whileTrue`, `whileFalse`
- Explain *in words* why `toggleOnTrue` is discouraged for driver controls

**What they do.** Add a flywheel subsystem with a `spinUpCommand()` factory. In `RobotContainer.configureButtonBindings()`, write `operator.a().whileTrue(flywheel.spinUpCommand())`. Add a separate B button for `intake.intakeNoteCommand()`. Wire `controller.x().onTrue(led.flashCommand())` as a fun extra.

**Rubric.**
1. Holding A → flywheel spins up; releasing → flywheel coasts to 0
2. Tapping B → intake runs once then stops (`runOnce` semantics if used) or stops on release (`whileTrue`)
3. Tapping X → LEDs flash once

**See it.** AdvantageScope plots `Flywheels/Inputs/velocityRPM` jumping up on A-press, decaying on release. SimGUI "System Joysticks" shows the A button highlighted while held.

**Introduces.** `CommandXboxController`, `Trigger`, `.onTrue`, `.whileTrue`, button-to-command binding pattern.

**Reinforces.** Factories (lesson 07), subsystem encapsulation (lesson 04).

**Preempts.** `toggleOnTrue` for driver buttons (lesson explicitly demonstrates *why* it's a bad UX — driver loses track of state).

**Not taught.** `.and()`/`.or()`/`.negate()` composition (lesson 11). `.debounce()` (lesson 11). Subsystem-state triggers (lesson 20). `BooleanSupplier`-based triggers from non-buttons.

**Resources.** [WPILib Binding Commands to Triggers](https://docs.wpilib.org/en/stable/docs/software/commandbased/binding-commands-to-triggers.html) · [Curriculum-Flow.md §5.7 on `toggleOnTrue`](Curriculum-Flow.md#57-toggleontrue-for-driver-controls)

---

### Lesson 09 — Command composition

| | |
|---|---|
| Stage | 1C |
| Time | ~40 min |
| Prereqs | 08 |
| Edits | `src/main/java/frc/robot/RobotContainer.java` (composition lives in `RobotContainer`, per Oblarg principle 3) |
| Tests | `frc.robot.composition.ScoreSequenceTest` (`@Tag("lesson-09")`) |
| Reference | Presto: composition patterns in [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java) |

**Pain solved.** "Spin up the flywheel, *then* push a note in, *then* stop." A single command can't do that; we need to compose primitive commands.

**Objectives.** By the end the student can:
- Compose commands with `andThen`, `alongWith`, `race`, `deadlineFor`
- Use `Commands.waitSeconds(t)` (never `Thread.sleep`)
- Use `.withTimeout(t)` for safety bounds
- Compose multi-subsystem command sequences in `RobotContainer`

**What they do.** Write a `scoreCommand` in `RobotContainer` that: spins up the flywheel, waits 0.5 s for it to reach speed, runs the indexer roller for 0.4 s, then stops everything. Bind it to a button.

**Rubric.**
1. Holding the bound button runs the sequence end-to-end
2. The flywheel reaches >70% target RPM before the indexer starts (verified by inspecting recorded outputs)
3. After 1.5 s the whole sequence ends regardless (timeout active)
4. The sequence has all required subsystems (verified — no missing `addRequirements`)

**See it.** AdvantageScope plots `Flywheels/Inputs/velocityRPM` rising, then `Indexer/Inputs/output` kicking in at the right moment, then both decaying.

**Introduces.** `andThen`, `alongWith`, `race`, `deadlineFor`, `until`, `withTimeout`, `Commands.waitSeconds`.

**Reinforces.** Factories, button bindings.

**Preempts.** `Thread.sleep` / `Timer.delay` (lesson explicitly demonstrates that this would freeze the scheduler).

**Not taught.** `Commands.either` (lesson 14). `ProxyCommand` (advanced). Subclassing `Command` (lesson 27).

**Resources.** [WPILib Command Compositions](https://docs.wpilib.org/en/stable/docs/software/commandbased/command-compositions.html)

---

### Lesson 10 — Telemetry & AdvantageScope basics

| | |
|---|---|
| Stage | 1C |
| Time | ~30 min |
| Prereqs | 09 |
| Edits | `src/main/java/frc/robot/subsystems/flywheels/Flywheels.java` |
| Tests | `frc.robot.subsystems.flywheels.FlywheelsTelemetryTest` (`@Tag("lesson-10")`) |
| Reference | Presto: [`flywheels/Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) — `Logger.recordOutput` calls scattered throughout |

**Pain solved.** "Why isn't my flywheel reaching target?" Without telemetry, the student is debugging blind. With it, the answer is visible in five seconds.

**Objectives.** By the end the student can:
- Add `Logger.recordOutput("Flywheels/TargetRPM", targetRpm)` to any subsystem
- Open AdvantageScope, connect to NT4 at `localhost`, plot a value
- Read a step-response trace and identify "overshoot" vs "settle time"
- Choose a logging key naming convention (`Subsystem/Field`)

**What they do.** Add `Logger.recordOutput` calls for target RPM, actual RPM, and error in the flywheel subsystem. Open AdvantageScope. Connect. Plot all three. Identify the overshoot.

**Rubric.**
1. `RealOutputs/Flywheels/TargetRPM` exists in NT
2. `RealOutputs/Flywheels/ActualRPM` exists
3. `RealOutputs/Flywheels/ErrorRPM` is correctly computed each cycle

**See it.** AdvantageScope plots three traces. The student literally sees the bug their friend was stuck on yesterday.

**Introduces.** `Logger.recordOutput`, NetworkTables 4 connection from AdvantageScope, key-naming conventions, the "if you didn't plot it, it didn't happen" rule.

**Reinforces.** Subsystems, `periodic()`.

**Preempts.** `println` debugging. "Test it on the robot." (lesson establishes that you saw the bug in sim first.)

**Not taught.** `@AutoLogOutput` (lesson 18). Custom AdvantageScope tabs (lesson 17). Log replay (lesson 19). 3D field views (lesson 17).

**Resources.** [AdvantageKit docs — Recording outputs](https://docs.advantagekit.org/data-flow/recording-outputs/) · [AdvantageScope docs](https://docs.advantagescope.org/)

---

## Stage 1D — Composition, autos, refactoring

Graduation from rookie. End state: a complete teleop robot.

### Lesson 11 — Default commands done right

| | |
|---|---|
| Stage | 1D |
| Time | ~35 min |
| Prereqs | 10 |
| Edits | `src/main/java/frc/robot/RobotContainer.java` + a small refactor in subsystems |
| Tests | `frc.robot.RobotContainerDefaultsTest` (`@Tag("lesson-11")`) |
| Reference | Presto: default-command setup in `RobotContainer`; LED idle behavior |

**Pain solved.** Lesson 09 had a sequence that ends — but what should the flywheel do when no command is running? Spin? Coast? Snap to zero? Without explicit answers, the robot has random behavior.

**Objectives.** By the end the student can:
- Use `setDefaultCommand` to give a subsystem an idle behavior
- Write a default command that is *trivial* (no decision logic)
- Compose triggers with `.and()`/`.or()`/`.negate()`/`.debounce()`

**What they do.** Drive's default is `arcadeDriveCommand` (lesson 07, already done — reinforced). Flywheel's default is "coast at 0 V." LEDs default to idle pattern. Then add a *trigger composition*: `intake.gamepieceDetected.and(operator.a()).onTrue(scoreCommand)`.

**Rubric.**
1. No buttons pressed → drive responds to joystick, flywheels at 0, LEDs idle
2. Holding A only → score does *not* start (no gamepiece)
3. Gamepiece present *and* A held → score sequence fires once

**See it.** AdvantageScope's boolean indicators: `Intake/gamepieceDetected` (green when piece present), `Operator/A` (green when held), combined trigger (green only when both).

**Introduces.** Default commands, trigger composition (`and`/`or`/`negate`), `.debounce(0.1)` for noisy boolean inputs.

**Reinforces.** Factories, button bindings.

**Preempts.** Decision logic in default commands ("if gamepiece, then …"). The lesson explicitly contrasts the wrong way and the right way.

**Not taught.** Subsystem-exposed `Trigger` fields (lesson 20). `Trigger`-from-`Supplier<Boolean>` patterns beyond simple booleans.

**Resources.** [WPILib default commands](https://docs.wpilib.org/en/stable/docs/software/commandbased/subsystems.html#default-commands) · [Curriculum-Flow.md §5.6](Curriculum-Flow.md#56-decision-logic-inside-default-commands)

---

### Lesson 12 — Auto routines (basic)

| | |
|---|---|
| Stage | 1D |
| Time | ~40 min |
| Prereqs | 11 |
| Edits | `src/main/java/frc/robot/autos/SimpleAuto.java` |
| Tests | `frc.robot.autos.SimpleAutoTest` (`@Tag("lesson-12")`) |
| Reference | Presto: `auto/` package — basic two-step autos |

**Pain solved.** Teleop is great, but FRC matches have a 15-second autonomous period. We need pre-programmed sequences.

**Objectives.** By the end the student can:
- Wire `RobotContainer.getAutonomousCommand()` to return a composed Command
- Use `Commands.sequence(...)` and `Commands.parallel(...)`
- Use `Commands.waitSeconds(t)` (never `Thread.sleep`)
- Use `.withTimeout(t)` as a safety net on every auto step

**What they do.** Write `SimpleAuto.driveAndScore()`: drive forward 2 m, then run the score sequence from lesson 09. Selectable via a `SendableChooser` on the dashboard.

**Rubric.**
1. Auto mode → robot drives forward
2. Reaches ≥1.8 m forward position within 3 s
3. Score sequence runs after drive
4. Total auto duration ≤ 8 s

**See it.** AdvantageScope plots `Drive/Inputs/leftPositionMeters` rising during auto, then `Flywheels/Inputs/velocityRPM` spiking during the score, all without driver input.

**Introduces.** `Commands.sequence`, `Commands.parallel`, `Commands.waitSeconds`, `SendableChooser`, autonomous routines as compositions.

**Reinforces.** Composition (lesson 09).

**Preempts.** `Thread.sleep` in auto (the deal-breaker error).

**Not taught.** Path-following / trajectory generation (lesson 13). Vision-aligned auto (lesson 24+). Choosable autos with parameter sweeps (advanced).

**Resources.** [WPILib autonomous overview](https://docs.wpilib.org/en/stable/docs/software/commandbased/structuring-command-based-project.html#autonomous-routines)

---

### Lesson 13 — Path-following intro

| | |
|---|---|
| Stage | 1D |
| Time | ~50 min |
| Prereqs | 12 |
| Edits | `src/main/java/frc/robot/autos/PathAuto.java` + add Choreo/PathPlanner vendordep |
| Tests | `frc.robot.autos.PathAutoTest` (`@Tag("lesson-13")`) |
| Reference | Kelpie: [`swerve/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/swerve) — Choreo integration |

**Pain solved.** Driving "forward 2 m" is fine. But a real auto needs to curve around game pieces. Time-based steering doesn't scale.

**Objectives.** By the end the student can:
- Install a vendordep (PathPlanner or Choreo)
- Generate a trajectory in the path-planning GUI
- Wire `FollowPathCommand` (or Choreo's equivalent) into an auto routine

**What they do.** Install Choreo. Draw a simple S-curve in Choreo's GUI. Save to `src/main/deploy/choreo/`. Reference it in `PathAuto.scurve()`. Run in sim.

**Rubric.**
1. Robot follows the path in sim
2. End pose is within 10 cm and 5° of the planned end pose
3. Total time ≤ planned time + 1 s

**See it.** AdvantageScope 3D field — the robot drives the actual S-curve. Plot `Drive/Inputs/pose` overlaid on the planned path.

**Introduces.** Vendordep installation, Choreo (or PathPlanner) GUI, trajectory files in `src/main/deploy/`, `FollowPathCommand`.

**Reinforces.** Auto composition, telemetry.

**Preempts.** Hand-tuned timed-drive autos (the predecessor that doesn't scale).

**Not taught.** Custom holonomic controllers. Trajectory generation in code. Reset-to-pose at auto start (lesson 22). On-the-fly path replanning (advanced).

**Resources.** [Choreo docs](https://choreo.autos/) · [PathPlanner](https://pathplanner.dev/) · Kelpie's swerve `Choreo` usage

---

### Lesson 14 — Refactoring with `*Bindings` classes

| | |
|---|---|
| Stage | 1D |
| Time | ~45 min |
| Prereqs | 13 |
| Edits | New `src/main/java/frc/robot/bindings/{DriverBindings,OperatorBindings}.java` |
| Tests | `frc.robot.RobotContainerSizeTest` (`@Tag("lesson-14")`) — checks RobotContainer line count |
| Reference | Presto: how 6328 organized bindings in 2024 |

**Pain solved.** `RobotContainer.java` is now 250 lines. Some bindings are scoring-related, some are driving-related, some are LED-related. They blur together.

**Objectives.** By the end the student can:
- Extract a group of bindings into a separate class (e.g., `DriverBindings`)
- Pass subsystems as constructor arguments
- Keep `RobotContainer` to ~50 lines (just subsystem ownership + binding-class instantiation)

**What they do.** Move all driver-button bindings into `DriverBindings(Drive drive, Intake intake, CommandXboxController driver)`. Same for operator. Constructor of each does the `whileTrue`/`onTrue` wiring.

**Rubric.**
1. `RobotContainer` is ≤ 100 lines
2. Bindings work identically (same tests from previous lessons still pass)
3. `DriverBindings` and `OperatorBindings` exist

**See it.** Same robot behavior as before — pure refactor, observable behavior unchanged.

**Introduces.** When-to-refactor instinct, the `*Bindings` pattern, constructor injection.

**Reinforces.** Encapsulation, factories, triggers.

**Preempts.** God `RobotContainer`. Cross-subsystem direct method calls (lesson explicitly forbids the alternative refactor — putting cross-subsystem logic inside a subsystem).

**Not taught.** Dependency injection frameworks. Builder patterns. Static command factories (Stage 2 alternative).

**Resources.** [WPILib Organizing Command-Based Projects](https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html) · [Curriculum-Flow.md §7.7](Curriculum-Flow.md#77-where-robotcontainer-ends)

---

### Lesson 15 — Capstone teleop robot

| | |
|---|---|
| Stage | 1D |
| Time | ~90 min |
| Prereqs | 14 (and all of Stage 1) |
| Edits | Everything composed; small polish across files |
| Tests | `frc.robot.CapstoneIntegrationTest` (`@Tag("lesson-15")`) — 5+ scenario tests |
| Reference | Both — students compare their robot to both Kelpie and Presto |

**Pain solved.** None new — this is integration. The pain is "did I actually learn this?" The lesson is the answer.

**Objectives.** By the end the student has:
- A working teleop robot with 4-5 subsystems
- Two auto routines (timed + path)
- Telemetry plots for every subsystem
- A capstone PR for mentor review

**What they do.** No new code, but lots of polish. Add missing telemetry. Tune PIDs. Clean up bindings. Add a `SendableChooser` for auto. Write 5 integration tests that simulate driver scenarios.

**Rubric.** 5+ integration scenarios all pass:
1. Drive forward + score
2. Pick up game piece + place
3. Auto routine 1 completes
4. Auto routine 2 completes
5. No subsystem ever sits in an undefined state

**See it.** Full robot runs in sim. AdvantageScope dashboard shows everything live.

**Introduces.** Nothing new — *that's the point*.

**Reinforces.** All of Stage 1.

**Preempts.** "Have I really learned this?" anxiety. Capstone makes it concrete.

**Not taught.** Anything advanced — that's Stage 2.

**Resources.** Pair with the team's mentor for the PR review.

---

## Stage 2A — AdvantageKit + mechanism mastery

Students *already have* a working robot. Now we explain *why* it survives mentor review.

### Lesson 16 — The IO Layer pattern

| | |
|---|---|
| Stage | 2A |
| Time | ~60 min |
| Prereqs | 15 |
| Edits | Refactor `drive/Drive.java` → `Drive` + `DriveIO` + `DriveIOSim` + `DriveIOReal` |
| Tests | Existing `DriveTest` (`@Tag("lesson-16")`) — must still pass after refactor |
| Reference | Presto: [`flywheels/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) — the canonical reference structure |

**Pain solved.** "How do I test my subsystem without a real robot?" Students who've been running `simulateJava` have been getting away with it via WPILib magic. Now they learn the pattern that makes it explicit.

**Objectives.** By the end the student can:
- Define an `XxxIO` interface with `@AutoLog` inputs
- Write `XxxIOSim` using WPILib `*Sim` classes
- Write `XxxIOReal` stub for real hardware (won't run in sim but exists)
- Inject the right IO based on `Constants.currentMode`
- Use `Logger.processInputs("Subsystem", inputs)` correctly

**What they do.** Refactor the lesson-07 `Drive` into the four-file IO pattern. Verify existing tests still pass after refactor.

**Rubric.**
1. `DriveIO.java` exists as an interface with `@AutoLog DriveIOInputs`
2. `DriveIOSim` and `DriveIOReal` both `implements DriveIO`
3. `RobotContainer` picks the right impl via `Constants.currentMode`
4. All lesson-07 through lesson-14 tests still pass

**See it.** Same robot behavior. But now AdvantageScope shows `Drive/Inputs/leftPositionMeters` etc. coming from a logged record (not directly from the subsystem code).

**Introduces.** IO Layer pattern, `@AutoLog`, `LoggedRobot`, mode switching.

**Reinforces.** Subsystems, interfaces, dependency injection (informally — "pass the right IO to the constructor").

**Preempts.** Direct sensor reads in subsystem code (breaks replay). Tight coupling between subsystem logic and motor controller APIs.

**Not taught.** Replay (lesson 19). Custom annotation processors. Per-cycle logging overhead.

**Resources.** [AdvantageKit IO interfaces](https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/) · [Presto's FlywheelsIO](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/FlywheelsIO.java) · [Reference-Robots.md §3.5](Reference-Robots.md#35-lesson-author-cheat-sheet-for-presto)

---

### Lesson 17 — AdvantageScope first-class

| | |
|---|---|
| Stage | 2A |
| Time | ~40 min |
| Prereqs | 16 |
| Edits | `src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java` — add Mechanism2d publishing |
| Tests | `frc.robot.subsystems.elevator.MechanismVizTest` (`@Tag("lesson-17")`) |
| Reference | Presto: `ascope_assets/Robot_Presto/` (the 3D model). Kelpie: any `Mechanism2d` usage |

**Pain solved.** Plots are great for numbers. But "did my elevator actually move correctly?" wants a picture.

**Objectives.** By the end the student can:
- Publish a `Mechanism2d` for the elevator + shoulder + wrist
- Load Presto's `.glb` 3D model into AdvantageScope
- Configure a per-lesson AdvantageScope layout JSON
- Use the 3D field view for the drivetrain pose

**What they do.** Wire `Mechanism2d` into the elevator + shoulder + wrist subsystems. Save a custom AdvantageScope layout. Commit the layout as `lessons/17-advantage-scope/AdvantageScope.json`.

**Rubric.**
1. `Mechanism2d` publishes correctly (verified by NT key presence)
2. The articulated viz updates as the subsystem moves
3. Saved layout JSON loads correctly into AdvantageScope

**See it.** AdvantageScope mechanism view shows the elevator going up while the shoulder pivots forward — the same motion that was abstract numbers before is now an animation.

**Introduces.** `Mechanism2d`/`MechanismLigament2d`, 3D field tab, custom layouts, robot model files.

**Reinforces.** Telemetry (lesson 10), IO layer (lesson 16).

**Preempts.** Number-plot-only debugging when geometry is involved.

**Not taught.** Custom `.glb` modeling (asset creation is out of scope). AdvantageScope plugin development.

**Resources.** [AdvantageScope tab reference](https://docs.advantagescope.org/tab-reference/) · [WPILib `Mechanism2d`](https://docs.wpilib.org/en/stable/docs/software/dashboards/glass/mech2d-widget.html)

---

### Lesson 18 — AdvantageKit logging discipline

| | |
|---|---|
| Stage | 2A |
| Time | ~35 min |
| Prereqs | 17 |
| Edits | Audit all subsystems; add missing logs; rename ad-hoc keys to a convention |
| Tests | `frc.robot.LoggingDisciplineTest` (`@Tag("lesson-18")`) — verifies key naming |
| Reference | Presto: every subsystem has `Logger.recordOutput` calls following a convention |

**Pain solved.** Lesson 10 told the student to add `Logger.recordOutput`. They've been doing it ad-hoc. Now key names are inconsistent (`drive_speed` here, `Drive/Speed` there) and ad-hoc keys aren't `@AutoLogOutput`-ed.

**Objectives.** By the end the student can:
- Use `@AutoLogOutput` on getter methods (auto-logs each cycle)
- Follow the `SubsystemName/Field` key convention consistently
- Distinguish "inputs" (logged via `Logger.processInputs`) from "outputs" (logged via `recordOutput`)
- Use `Logger.recordOutput` for `Pose2d`/`Pose3d`/`SwerveModuleState[]` values

**What they do.** Audit every subsystem. Rename keys to convention. Add `@AutoLogOutput` to all public getter methods that should always be logged. Add structured types (`Pose2d`, etc.) where applicable.

**Rubric.**
1. All NT keys match `^[A-Z][a-zA-Z]*\/[A-Z]` pattern
2. `Drive/Pose` is a `Pose2d` (verified by reading it back as one)
3. Every subsystem has at least one `@AutoLogOutput` field/method

**See it.** AdvantageScope's key tree is now organized by subsystem; `Drive/Pose` shows up as a Pose-typed value that can be dropped into the 3D field view.

**Introduces.** `@AutoLogOutput`, structured types, key-naming conventions.

**Reinforces.** Telemetry, IO layer.

**Preempts.** Stringly-typed log keys. Inconsistent naming.

**Not taught.** Custom serializers. Log file rotation.

**Resources.** [AdvantageKit recording outputs](https://docs.advantagekit.org/data-flow/recording-outputs/)

---

### Lesson 19 — Log replay for debugging

| | |
|---|---|
| Stage | 2A |
| Time | ~50 min |
| Prereqs | 18 |
| Edits | None — students use existing infra |
| Tests | `frc.robot.ReplayTest` (`@Tag("lesson-19")`) — verifies replay output matches expected |
| Reference | Presto's replay setup |

**Pain solved.** "I think there's a bug at exactly the moment the gamepiece transitions from intake to indexer, but I can't reproduce it." Replay makes any captured run reproducible *forever*.

**Objectives.** By the end the student can:
- Save a WPILOG from a sim run (or a real match)
- Launch the robot binary in REPLAY mode with `setUseTiming(false)`
- Open the resulting `_sim.wpilog` in AdvantageScope
- Add a new `Logger.recordOutput` line, replay the same log, see the new value as if it had been there

**What they do.** Run a sim, save the log. Replay it. Add a new logged value to the elevator subsystem. Replay again. Confirm the new value appears retroactively.

**Rubric.**
1. Replay run produces a `_sim.wpilog`
2. The new logged value appears in the replay log
3. Original inputs (`Drive/Inputs/leftPositionMeters` etc.) match between original and replay (proves determinism)

**See it.** AdvantageScope opens both logs; comparing them shows identical inputs and outputs except for the newly-added log line.

**Introduces.** REPLAY mode, `setUseTiming(false)`, `LogFileUtil.findReplayLog`, the "log it once, query it forever" mental model.

**Reinforces.** IO layer (replay only works because all reads go through inputs), logging discipline.

**Preempts.** "Just reproduce it on the robot" — a non-starter once the student has felt the alternative.

**Not taught.** AdvantageKit-replay-based CI grading (still a research direction per [Infrastructure-Analysis.md §3.8](Infrastructure-Analysis.md#38-grading-junit-today-advantagekit-replay-tomorrow)). Log compression. Distributed replay clusters.

**Resources.** [AdvantageKit log replay theory](https://docs.advantagekit.org/theory/log-replay-comparison/) · `replayWatch` Gradle task

---

### Lesson 20 — Subsystem composition at scale

| | |
|---|---|
| Stage | 2A |
| Time | ~50 min |
| Prereqs | 19 |
| Edits | New `src/main/java/frc/robot/subsystems/Superstructure.java` |
| Tests | `frc.robot.subsystems.SuperstructureTest` (`@Tag("lesson-20")`) |
| Reference | Kelpie: [`Superstructure.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/Superstructure.java) |

**Pain solved.** Lesson 14's `*Bindings` classes work for buttons. But complex behaviors (e.g., "to score at level 4, the elevator must be up AND the shoulder must be at angle X AND the gamepiece must be detected") want their own home.

**Objectives.** By the end the student can:
- Define a top-level `Superstructure` class that holds references to multiple subsystems
- Expose composite Triggers on the superstructure (`atScoringPose`, `readyToShoot`)
- Compose multi-subsystem commands as factory methods on `Superstructure`
- Use `@AutoLogOutput` on the superstructure's state for visibility

**What they do.** Create `Superstructure(elevator, shoulder, intake)`. Add `Trigger atScoringPose = elevator.atSetpoint.and(shoulder.atSetpoint).debounce(0.1)`. Add `Command scoreLevel4()` that composes the underlying subsystem commands.

**Rubric.**
1. `Superstructure.atScoringPose` reports `true` only when both atomic conditions hold
2. `scoreLevel4` correctly requires elevator + shoulder + intake subsystems
3. State is `@AutoLogOutput`ed for AdvantageScope visibility

**See it.** AdvantageScope plots `Superstructure/atScoringPose` as a boolean indicator; lights up only when the right conditions converge.

**Introduces.** Top-level composition class, composite triggers, exposing state at the right abstraction level.

**Reinforces.** Triggers, factories, `*Bindings` pattern.

**Preempts.** Cross-subsystem direct method calls. State machine logic spread across multiple files.

**Not taught.** Formal state-machine libraries (lesson 29). Multi-subsystem requirement priorities.

**Resources.** Kelpie's [`Superstructure.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/Superstructure.java) · [Curriculum-Flow.md §3.3](Curriculum-Flow.md#33-state-exposure-via-triggers-not-getters)

---

## Stage 2B — Swerve & trajectories

Students who get here are committing to programming. Swerve is the differentiator.

### Lesson 21 — Swerve drivetrain (intro)

| | |
|---|---|
| Stage | 2B |
| Time | ~75 min |
| Prereqs | 20 |
| Edits | Refactor `drive/` to swerve: 4 modules + gyro IO |
| Tests | `frc.robot.subsystems.swerve.SwerveTest` (`@Tag("lesson-21")`) |
| Reference | Kelpie: [`swerve/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/swerve) |

**Pain solved.** Tank drive can't strafe. To pick up a coral two meters to the side, you must rotate, drive, rotate back. Real teams don't do this.

**Objectives.** By the end the student can:
- Articulate the four-module kinematic model (drive + turning motor per module)
- Use `SwerveDriveKinematics` to convert chassis speeds → module states
- Use `SwerveDriveOdometry` to track pose from module positions + gyro
- Wire a `Module` IO layer (`ModuleIO`, `ModuleIOSim`, `ModuleIOReal`)

**What they do.** Heavy lift. Replace tank drive with 4 swerve modules. Each module has its own IO. Wire chassis-speeds-driven default command using `ChassisSpeeds.fromFieldRelativeSpeeds(...)`.

**Rubric.**
1. Joystick X/Y translates the robot in field-relative direction regardless of heading
2. Joystick rotation spins the robot in place
3. Module states logged for AdvantageScope swerve view
4. Gyro yaw publishes correctly

**See it.** AdvantageScope **Swerve States** view shows four wheel direction arrows. 3D field view shows the robot moving holonomically.

**Introduces.** Swerve kinematics, module composition, field-relative control, `ChassisSpeeds`, gyro integration.

**Reinforces.** IO layer (now four modules' worth).

**Preempts.** "Just adapt the tank drive math to swerve" — doesn't work; swerve needs its own kinematic model.

**Not taught.** Module-level closed-loop control (uses open-loop voltage initially). Steer/drive coupling. Software-side current limiting.

**Resources.** [WPILib swerve drive docs](https://docs.wpilib.org/en/stable/docs/software/kinematics-and-odometry/swerve-drive-kinematics.html) · Kelpie's swerve module setup · [YAGSL](https://docs.yagsl.com/) (alternative high-level lib students may evaluate)

---

### Lesson 22 — Odometry & pose estimation

| | |
|---|---|
| Stage | 2B |
| Time | ~55 min |
| Prereqs | 21 |
| Edits | Replace `SwerveDriveOdometry` with `SwerveDrivePoseEstimator` |
| Tests | `frc.robot.subsystems.swerve.PoseEstimationTest` (`@Tag("lesson-22")`) |
| Reference | Kelpie: pose-estimator wiring in `SwerveSubsystem` |

**Pain solved.** Pure odometry drifts. Over a 2-minute match the robot's "knowledge of where it is" diverges from reality. Without correction, vision-based scoring breaks.

**Objectives.** By the end the student can:
- Replace `SwerveDriveOdometry` with `SwerveDrivePoseEstimator`
- Reset pose at the start of auto
- Configure standard-deviation parameters (without diving into Kalman theory)
- Log the estimated pose for AdvantageScope visualization

**What they do.** Swap odometry → pose estimator. Add a `resetPose` factory method. Add a pose-publishing `@AutoLogOutput`.

**Rubric.**
1. Pose updates correctly on sim drives
2. `resetPose` works
3. `Drive/Pose` shows up in AdvantageScope as a `Pose2d` and renders on the 3D field

**See it.** AdvantageScope 3D field: the robot's estimated pose tracks its commanded movement.

**Introduces.** Pose estimator vs. odometry, sensor fusion (foreshadowing vision).

**Reinforces.** Swerve kinematics, telemetry.

**Preempts.** "Why does my robot think it's somewhere it's not?" 30-minute debugging sessions.

**Not taught.** Custom Kalman tuning, multi-vision fusion (lesson 25).

**Resources.** [WPILib pose estimator docs](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/state-space/state-space-pose-estimators.html)

---

### Lesson 23 — Trajectory following

| | |
|---|---|
| Stage | 2B |
| Time | ~60 min |
| Prereqs | 22 |
| Edits | Add Choreo path file; wire `Choreo.choreoSwerveCommand` (or PathPlanner equivalent) |
| Tests | `frc.robot.autos.SwerveAutoTest` (`@Tag("lesson-23")`) |
| Reference | Kelpie: Choreo integration |

**Pain solved.** Auto routines built from `Commands.sequence(driveForward, ...)` get clunky as paths get complex. Trajectory tools let designers (not coders) plan routes.

**Objectives.** By the end the student can:
- Generate a multi-waypoint trajectory in Choreo's GUI
- Reference the path JSON from code
- Wire path-following into auto routines
- Combine path-following + scoring commands using composition (lesson 09)

**What they do.** Three-waypoint Choreo path. Wire as auto. Add intermediate "score" commands using `andThen` between path segments.

**Rubric.**
1. Path follows correctly in sim (pose tracks within tolerance)
2. Intermediate scoring command runs at the right waypoint

**See it.** AdvantageScope 3D field with the robot following the curve, scoring at the right poses.

**Introduces.** Multi-segment paths, parameterized path-following, event markers (PathPlanner) or sequence interleaving (Choreo).

**Reinforces.** Compositions, pose estimation, auto routines.

**Preempts.** Hardcoded waypoints in source.

**Not taught.** Dynamic replanning, on-the-fly trajectory generation.

**Resources.** [Choreo docs](https://choreo.autos/) · [PathPlanner docs](https://pathplanner.dev/)

---

## Stage 2C — Vision

### Lesson 24 — PhotonVision single-tag

| | |
|---|---|
| Stage | 2C |
| Time | ~45 min |
| Prereqs | 23 |
| Edits | New `vision/VisionSubsystem.java` with IO layer |
| Tests | `frc.robot.subsystems.vision.VisionTest` (`@Tag("lesson-24")`) |
| Reference | Presto: [`apriltagvision/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/apriltagvision) |

**Pain solved.** Odometry alone can't tell the robot it's misaligned by 3° from the goal. Vision can.

**Objectives.** By the end the student can:
- Install PhotonVision vendordep
- Define a `VisionIO` interface with target list as input
- Use `PhotonVisionSim` to simulate camera in sim
- Apply a single-tag pose update to the pose estimator (lesson 22)

**What they do.** Build a 1-camera, 1-AprilTag detection subsystem. Feed measurements into the pose estimator from lesson 22.

**Rubric.**
1. Sim shows a simulated tag visible from the camera
2. When the robot drives near the tag, the estimator updates
3. The robot's logged pose stays within 5 cm of ground truth (sim provides ground-truth pose)

**See it.** AdvantageScope 3D field: when the robot "sees" the tag, the pose snaps to ground truth.

**Introduces.** PhotonVision, AprilTags, vision IO pattern, simulated cameras.

**Reinforces.** Pose estimator, IO layer.

**Preempts.** Treating vision as the only source of truth (vision should *update* odometry, not replace it).

**Not taught.** Multi-tag (lesson 25). Custom vision pipelines. Limelight.

**Resources.** [PhotonVision docs](https://docs.photonvision.org/)

---

### Lesson 25 — Multi-tag pose estimation

| | |
|---|---|
| Stage | 2C |
| Time | ~60 min |
| Prereqs | 24 |
| Edits | Extend `VisionIO` to support multi-camera arrays; refine fusion logic |
| Tests | `frc.robot.subsystems.vision.MultiTagTest` (`@Tag("lesson-25")`) |
| Reference | Presto: full multi-camera AprilTag fusion |

**Pain solved.** Single-tag has ambiguity (rotation around the tag axis). Multi-tag is dramatically more accurate.

**Objectives.** By the end the student can:
- Configure multiple simulated cameras
- Use `PhotonPoseEstimator.MULTI_TAG_PNP_ON_COPROCESSOR` strategy
- Tune per-camera standard deviations based on distance/ambiguity
- Reject obviously-bad measurements (out-of-field poses, etc.)

**What they do.** Add a second camera. Configure multi-tag pose estimation. Add stddev-based rejection of outlier measurements.

**Rubric.**
1. Two cameras both publish data
2. Multi-tag estimate is closer to ground truth than single-tag
3. Out-of-field poses are filtered out

**See it.** AdvantageScope 3D field shows two camera frustums; the estimated pose stays tight even at long distances.

**Introduces.** Multi-tag PNP, measurement rejection heuristics, stddev tuning.

**Reinforces.** Pose estimator, vision IO.

**Preempts.** Trusting any vision measurement that comes in. "Just average them" naïve fusion.

**Not taught.** Coprocessor latency compensation. Game-piece detection. Multi-target tracking.

**Resources.** [PhotonVision pose estimation strategies](https://docs.photonvision.org/en/latest/docs/programming/photonlib/robot-pose-estimator.html)

---

### Lesson 26 — maple-sim & game-piece physics

| | |
|---|---|
| Stage | 2C |
| Time | ~55 min |
| Prereqs | 25 |
| Edits | Add `maple-sim` vendordep; create `ModuleIOMapleSim.java`; switch sim mode |
| Tests | `frc.robot.subsystems.swerve.MapleSimTest` (`@Tag("lesson-26")`) |
| Reference | Kelpie: [`swerve/ModuleIOMapleSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIOMapleSim.java) |

**Pain solved.** Until now, sim has been kinematic — the robot teleports through walls and game pieces have been imaginary. For complex auto routines, this hides bugs.

**Objectives.** By the end the student can:
- Install the `maple-sim` vendordep
- Swap `ModuleIOSim` → `ModuleIOMapleSim` for sim mode
- Add simulated game pieces to the field
- Detect intake-by-collision in sim

**What they do.** Add maple-sim. Add coral game pieces to the simulated field. Confirm the robot can physically push into them and intake.

**Rubric.**
1. Robot collides with walls in sim
2. Driving into a coral piece triggers the intake's beam-break
3. Robot can be pushed off a target pose by simulated impact

**See it.** AdvantageScope 3D field with realistic robot-wall and robot-piece interactions.

**Introduces.** Physics-accurate simulation, game-piece simulation.

**Reinforces.** IO layer (multiple sim impls coexist), swerve.

**Preempts.** False confidence from kinematic sim (e.g., autos that work in sim but fail in reality because they assume zero-friction wall contact).

**Not taught.** Custom physics tuning, multi-robot sim.

**Resources.** [maple-sim docs](https://shenzhen-robotics-alliance.github.io/maple-sim/) · Kelpie's maple-sim setup

---

## Stage 2D — Advanced control & capstone

### Lesson 27 — Motion profiling

| | |
|---|---|
| Stage | 2D |
| Time | ~55 min |
| Prereqs | 26 |
| Edits | Refactor `shoulder/ShoulderSubsystem.java` to use `ProfiledPIDController` |
| Tests | `frc.robot.subsystems.shoulder.ProfiledMotionTest` (`@Tag("lesson-27")`) |
| Reference | Presto: profiled arm control in `superstructure/arm/` |

**Pain solved.** Lesson 06's arm goes from "at rest" to "moving fast" instantly. Mechanically violent, drains battery, induces oscillation.

**Objectives.** By the end the student can:
- Replace `PIDController` with `ProfiledPIDController`
- Tune max velocity and max acceleration constraints
- Add `ArmFeedforward` velocity term (lesson 06 had only kG)
- Identify when motion profiling matters and when it's overkill

**What they do.** Refactor the shoulder to `ProfiledPIDController` with `TrapezoidProfile.Constraints`. Tune. Plot. The first lesson where a Command subclass might actually make sense (stateful: holds the trajectory).

**Rubric.**
1. Shoulder reaches setpoint within tolerance
2. Velocity never exceeds configured max
3. Settle time improves vs. unprofiled PID (verified by graph inspection)

**See it.** AdvantageScope plots velocity stays inside bounds; acceleration is smooth, not square-wave.

**Introduces.** `ProfiledPIDController`, trapezoid profiles, velocity FF.

**Reinforces.** PID + FF (lesson 06).

**Preempts.** Mechanical violence from naïve PID.

**Not taught.** S-curve profiles, optimal control.

**Resources.** [WPILib profiled PID](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/profiled-pidcontroller.html)

---

### Lesson 28 — System identification (SysId)

| | |
|---|---|
| Stage | 2D |
| Time | ~60 min |
| Prereqs | 27 |
| Edits | Add SysId routines to elevator + drive |
| Tests | `frc.robot.subsystems.elevator.SysIdRoutineTest` (`@Tag("lesson-28")`) |
| Reference | Presto: SysId tooling integration |

**Pain solved.** Lesson 05's PID was tuned by feel. SysId gives data-driven `kS`, `kV`, `kA` values.

**Objectives.** By the end the student can:
- Wire a `SysIdRoutine` for any subsystem
- Run quasistatic and dynamic tests in sim
- Analyze results in WPILib SysId tool
- Plug results into `ElevatorFeedforward` / `SimpleMotorFeedforward`

**What they do.** Add SysId routines. Run them in sim. Process the data. Update FF constants in `Constants`.

**Rubric.**
1. SysId routines complete without error
2. Produced log file is analyzable
3. New FF values improve sim tracking error

**See it.** AdvantageScope plots show clean quasistatic ramps and dynamic step responses.

**Introduces.** SysId, `kV`/`kA` extraction, data-driven tuning.

**Reinforces.** PID, feedforward.

**Preempts.** Black-magic PID tuning lore.

**Not taught.** State-space control (deferred).

**Resources.** [WPILib SysId](https://docs.wpilib.org/en/stable/docs/software/pathplanning/system-identification/index.html)

---

### Lesson 29 — Advanced state machines

| | |
|---|---|
| Stage | 2D |
| Time | ~50 min |
| Prereqs | 28 |
| Edits | New `climber/ClimberStateMachine.java` |
| Tests | `frc.robot.subsystems.climber.ClimberStateMachineTest` (`@Tag("lesson-29")`) |
| Reference | Presto: `superstructure/climber/` |

**Pain solved.** Some sequences (a climbing routine with retract → extend → hook → retract → hold) are genuinely modal. Pure trigger composition gets hairy.

**Objectives.** By the end the student can:
- Recognize when behavior is truly modal (vs. just composable)
- Implement a state machine as an `enum` + transition method
- Combine the state machine with command-based (factory returns a command that drives the SM)

**What they do.** Climber state machine: `IDLE → EXTENDING → HOOKED → RETRACTING → CLIMBED`. Each transition has guards. Driven by both sensors and operator inputs.

**Rubric.**
1. Each transition occurs under the right conditions
2. Invalid transitions are rejected
3. State is `@AutoLogOutput`ed

**See it.** AdvantageScope plots `Climber/state` as a discrete signal; transitions visible.

**Introduces.** Explicit state machines, transition guards.

**Reinforces.** Subsystems, factories, triggers.

**Preempts.** Trigger spaghetti for genuinely modal subsystems.

**Not taught.** Hierarchical state machines, statecharts.

**Resources.** [Curriculum-Flow.md §7.6 on state machines vs trigger composition](Curriculum-Flow.md#76-state-machines-vs-trigger-composition) · [CD discussion](https://www.chiefdelphi.com/t/standardized-state-based-robot-control-vendor-dep/415582)

---

### Lesson 30 — Season capstone

| | |
|---|---|
| Stage | 2D |
| Time | ~180 min (or off-season weekend) |
| Prereqs | 29 (and effectively all prior) |
| Edits | Open — student-directed |
| Tests | `frc.robot.SeasonCapstoneTest` (`@Tag("lesson-30")`) — 10+ scenarios |
| Reference | Both robots — student picks one to clone-and-improve, or builds from scratch |

**Pain solved.** "Am I ready for a real season?" Capstone answers it.

**Objectives.** By the end the student has:
- A full swerve + vision + path-following + multi-mechanism robot in sim
- Three written-from-scratch auto routines
- Comprehensive AdvantageScope dashboards
- A capstone PR reviewed and merged by mentors

**What they do.** Open-ended. Pick a real game (Reefscape or Crescendo, or a current-season game), specify a strategy, build a robot to it. Use everything from Stages 1-2.

**Rubric.** 10+ scenarios cover:
1. Full teleop loop (drive + score + intake + climb)
2. Autonomous: drive + score
3. Autonomous: multi-piece
4. Vision: align to target
5. Vision: localize from far
6. Edge case: dropped piece detection
7. Edge case: battery brownout
8. Replay: previous-run analysis
9. Mentor PR with clean diffs
10. Honest README explaining design choices

**See it.** AdvantageScope dashboard with everything live. 3D field view shows full robot doing things.

**Introduces.** Self-direction. Engineering judgement.

**Reinforces.** Literally everything.

**Preempts.** "Was that real learning?" Capstone is the answer.

**Resources.** Mentor + team. Possible peer review by graduates.

---

## Cross-cutting notes for content authors

### Naming conventions

- Lesson directories: `lessons/NN-kebab-slug/` (e.g., `lessons/04-subsystems-as-state-machines/`)
- JUnit tags: `lesson-NN` (zero-padded to two digits)
- Per-lesson AdvantageScope layout: `lessons/<slug>/AdvantageScope.json`
- Per-lesson exemplar (author-only): `.meta/exemplar/<slug>/...`

### Per-lesson AdvantageScope layout (required for Stage 1C+)

Each lesson in Stage 1C onward ships an `AdvantageScope.json` configured with the plots, indicators, and 3D-field placements the lesson references in "See it in sim." The student imports it once at lesson start.

### Lesson estimate calibration

The `Time` field is a *median* expectation. From [Path-B-Implementation.md §3.2](Path-B-Implementation.md#32-phase-1-gate), if Phase 1 students' actual times deviate >30% from these estimates, the lesson is mis-scoped.

### Defer columns explicitly

The **"Not taught"** field is non-decorative. It's the contract with the student: *"We are not teaching X today. If you need X, here's where it lives in the curriculum."* It also prevents content authors from scope-creeping a lesson.

### Reference-robot pinning

All references to Kelpie/Presto file paths assume the commit SHAs pinned in `references.json` per [Reference-Robots.md §5.2](Reference-Robots.md#52-pin-specific-commit-shas). When references shift, this doc updates first.

### Anti-pattern preempt audit

Every anti-pattern in [Curriculum-Flow.md §5](Curriculum-Flow.md#5-anti-patterns-to-actively-preempt) appears as a "Preempts" line in at least one lesson. Cross-reference: §5.1 → L04, §5.2 → L04, §5.3 → L07, §5.4 → L07/L09, §5.5 → L09/L12, §5.6 → L11, §5.7 → L08, §5.8 → L02, §5.9 → L04, §5.10 → L17/L19, §5.11 → L14/L20, §5.12 → L16.

---

## What's not in this plan (and where it lives instead)

- **Python (RobotPy) parallel track** — deferred per [Curriculum-Flow.md §7.1](Curriculum-Flow.md#71-java-vs-c-vs-python). If we ever build it, it parallels this plan 1:1.
- **Stage 0.5 "wire it up" track** (real-hardware lessons) — listed in [Path-B-Implementation.md §13](Path-B-Implementation.md#13-open-decisions-to-make-explicitly), authored only after Stage 2 is solid.
- **CTRE Phoenix-specific or REV-specific deep dives** — vendored lessons are deferred until evidence of demand.
- **Custom robots beyond Kelpie/Presto** — handled by referring students to other open-alliance teams (8033 training, 6328 build threads) once they outgrow the two references.
