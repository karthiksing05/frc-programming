# Learning Course

Thirty lessons, organized into seven stages. Each lesson builds on the last — your project grows in place rather than starting over. Start at Stage 0 even if you've programmed before; the install + tour lessons are short and they catch the things that bite later.

## How to use this course

- **Sequential.** The order matters. Each lesson assumes the previous one is complete.
- **Self-paced.** Estimates next to each lesson are medians, not deadlines.
- **Hands-on.** Every lesson has a rubric you run locally with `./gradlew` (or, in Stage 0–1B, an in-browser widget).
- **Stuck?** Open the *Full reveal* details block at the bottom of the lesson. Then ask your mentor or your team's Discord — peer help beats site-help.

---

## Stage 0 — Onboarding

Get installed. Meet the two robots you'll be referencing forever. Touch Git once so it doesn't bite later.

[Start with Lesson 0A :material-arrow-right:](stage0/0a-install/){ .md-button .md-button--primary }

- [0A — First-run install](stage0/0a-install/)
- [0B — Meet Presto (the shooter robot)](stage0/0b-meet-presto/)
- [0C — Meet Kelpie (the pick-and-place robot)](stage0/0c-meet-kelpie/)
- [0D — Git + project tour](stage0/0d-git-tour/)

---

## Stage 1A — Java fundamentals

Three lessons. By the end you've written a helper method, named some constants, and felt firsthand why everything-in-`teleopPeriodic` doesn't scale.

[Start Stage 1A :material-arrow-right:](stage1a/01-methods/){ .md-button }

- [01 — Methods (Functions)](stage1a/01-methods/)
- [02 — Variables & types](stage1a/02-variables-and-types/)
- [03 — Conditionals in `teleopPeriodic`](stage1a/03-conditionals-in-teleop/)

---

## Stage 1B — Subsystems & PID

Lift the `teleopPeriodic` jungle into real `SubsystemBase` classes. Make a motor reach a number with WPILib's `PIDController`. Add gravity feedforward for an arm.

[Start Stage 1B :material-arrow-right:](stage1b/04-subsystems-state-machines/){ .md-button }

- [04 — Subsystems as state machines](stage1b/04-subsystems-state-machines/)
- [05 — PID introduction (Elevator)](stage1b/05-pid-elevator/)
- [06 — Arm with gravity feedforward](stage1b/06-arm-gravity-ff/)

---

## Stage 1C — My first robot

Command-based for real. Factories from day one. Triggers. Telemetry. By the end of Stage 1C you have a teleop-drivable robot.

[Start Stage 1C :material-arrow-right:](stage1c/07-tank-drive/){ .md-button }

- [07 — Tank drive wiring (factory pattern)](stage1c/07-tank-drive/)
- [08 — Joystick bindings & Triggers](stage1c/08-triggers-bindings/)
- [09 — Command composition](stage1c/09-command-composition/)
- [10 — Telemetry & AdvantageScope basics](stage1c/10-telemetry/)

---

## Stage 1D — Composition, autos, refactoring

Default commands. First autos. Path-following. Refactoring `RobotContainer` before it becomes a 500-line god class. Stage 1D ends with a capstone — a complete teleop robot.

[Start Stage 1D :material-arrow-right:](stage1d/11-default-commands/){ .md-button }

- [11 — Default commands done right](stage1d/11-default-commands/)
- [12 — Auto routines (basic)](stage1d/12-auto-basic/)
- [13 — Path-following intro](stage1d/13-path-following/)
- [14 — Refactoring with `*Bindings` classes](stage1d/14-bindings-refactor/)
- [15 — Capstone teleop robot](stage1d/15-capstone-teleop/)

---

## Stage 2A — AdvantageKit & mechanism mastery

The IO Layer pattern. AdvantageScope as a first-class debugging surface. Logging discipline. Replay. Superstructure composition.

[Start Stage 2A :material-arrow-right:](stage2a/16-io-layer/){ .md-button }

- [16 — The IO Layer pattern](stage2a/16-io-layer/)
- [17 — AdvantageScope first-class](stage2a/17-advantagescope/)
- [18 — AdvantageKit logging discipline](stage2a/18-logging-discipline/)
- [19 — Log replay for debugging](stage2a/19-log-replay/)
- [20 — Subsystem composition at scale](stage2a/20-superstructure/)

---

## Stage 2B — Swerve & trajectories

Swerve drivetrain. Pose estimation. Trajectory following with Choreo or PathPlanner.

[Start Stage 2B :material-arrow-right:](stage2b/21-swerve-intro/){ .md-button }

- [21 — Swerve drivetrain (intro)](stage2b/21-swerve-intro/)
- [22 — Odometry & pose estimation](stage2b/22-odometry/)
- [23 — Trajectory following](stage2b/23-trajectories/)

---

## Stage 2C — Vision

PhotonVision single-tag and multi-tag. Physics-accurate sim with maple-sim.

[Start Stage 2C :material-arrow-right:](stage2c/24-photonvision-singletag/){ .md-button }

- [24 — PhotonVision single-tag](stage2c/24-photonvision-singletag/)
- [25 — Multi-tag pose estimation](stage2c/25-multitag/)
- [26 — maple-sim & game-piece physics](stage2c/26-maplesim/)

---

## Stage 2D — Advanced control & capstone

Motion profiling. SysId. Advanced state machines. A self-directed season capstone.

[Start Stage 2D :material-arrow-right:](stage2d/27-motion-profiling/){ .md-button }

- [27 — Motion profiling](stage2d/27-motion-profiling/)
- [28 — System identification (SysId)](stage2d/28-sysid/)
- [29 — Advanced state machines](stage2d/29-state-machines/)
- [30 — Season capstone](stage2d/30-season-capstone/)
