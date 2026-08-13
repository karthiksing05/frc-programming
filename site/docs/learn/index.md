# The lessons

34 lessons, in order. Each one solves a problem the previous one made you feel, so
the order matters.

Start at Stage 0 even if you have programmed before. It is short and it catches the
things that bite later.

[Set up first](../setup/index.md){ .md-button }
[Start Lesson 0A](stage0/0a-first-run-install/index.md){ .md-button .md-button--primary }

---

## Two kinds of lesson

**Graded (01–16).** Starter code with a `TODO`, a JUnit rubric, four hints, and a
reference answer. Run `frcprog check <lesson>` and it tells you what is wrong, in
words.

**Guided (17–30, and Stage 0).** A clear goal and working code to copy from. No
automatic grader. Your check is the simulator.

Five lessons are marked ⬇ and need one online build.
[Details](../setup/offline.md).

---

## Stage 0 — Onboarding

Install once. Meet the two robots. Learn where files live.

| | Lesson | Time |
|---|---|---|
| 0A | [Install](stage0/0a-first-run-install/index.md) | 60 min |
| 0B | [Meet Presto](stage0/0b-meet-presto/index.md) | 15 min |
| 0C | [Meet Kelpie](stage0/0c-meet-kelpie/index.md) | 15 min |
| 0D | [Project tour](stage0/0d-project-tour/index.md) | 30 min |

## Stage 1A — Java, in a robot

A method, some named constants, and a deliberately messy `teleopPeriodic`.

| | Lesson | Time |
|---|---|---|
| 01 | [Methods](stage1a/01-methods/index.md) | 25 min |
| 02 | [Variables and types](stage1a/02-variables-and-types/index.md) | 30 min |
| 03 | [Conditionals in teleopPeriodic](stage1a/03-conditionals-in-teleop/index.md) | 35 min |

## Stage 1B — Subsystems and control

Lift the mess into a subsystem. Make a motor stop at a number. Fight gravity that
changes with angle.

| | Lesson | Time |
|---|---|---|
| 04 | [Subsystems](stage1b/04-subsystems-state-machines/index.md) | 45 min |
| 05 | [PID](stage1b/05-pid-elevator/index.md) | 50 min |
| 06 | [Gravity feedforward](stage1b/06-arm-gravity-ff/index.md) | 45 min |

## Stage 1C — Command-based

Factories, buttons, composition, telemetry. Ends with a drivable robot you can see
inside.

| | Lesson | Time |
|---|---|---|
| 07 | [Commands and suppliers](stage1c/07-tank-drive/index.md) | 40 min |
| 08 | [Buttons](stage1c/08-triggers-bindings/index.md) | 35 min |
| 09 | [Composition](stage1c/09-command-composition/index.md) | 40 min |
| 10 | [Telemetry](stage1c/10-telemetry/index.md) | 30 min |

## Stage 1D — Autos and refactoring

Idle behaviour, two autos, a refactor you can prove was safe, a capstone.

| | Lesson | Time |
|---|---|---|
| 11 | [Default commands](stage1d/11-default-commands/index.md) | 35 min |
| 12 | [Autonomous](stage1d/12-auto-basic/index.md) | 40 min |
| 13 | [Trajectories](stage1d/13-trajectory-auto/index.md) | 50 min |
| 14 | [Splitting up RobotContainer](stage1d/14-bindings-refactor/index.md) | 45 min |
| 15 | [Capstone](stage1d/15-capstone-teleop/index.md) | 90 min |

## Stage 2A — Structure and logging

The pattern every serious team uses, and the tooling that makes yesterday's bug
reproducible.

| | Lesson | Time |
|---|---|---|
| 16 | [The IO layer](stage2a/16-io-layer/index.md) | 60 min |
| 17 | [Mechanism views](stage2a/17-advantagescope/index.md) | 40 min |
| 18 | [Logging discipline](stage2a/18-logging-discipline/index.md) | 35 min |
| 19 | [Log replay](stage2a/19-log-replay/index.md) ⬇ | 50 min |
| 20 | [Superstructure](stage2a/20-superstructure/index.md) | 50 min |

## Stage 2B — Swerve

| | Lesson | Time |
|---|---|---|
| 21 | [Swerve](stage2b/21-swerve-intro/index.md) | 75 min |
| 22 | [Pose estimation](stage2b/22-odometry/index.md) | 55 min |
| 23 | [Choreo or PathPlanner](stage2b/23-trajectories/index.md) ⬇ | 60 min |

## Stage 2C — Vision

| | Lesson | Time |
|---|---|---|
| 24 | [AprilTags](stage2c/24-photonvision-singletag/index.md) ⬇ | 45 min |
| 25 | [Multi-tag](stage2c/25-multitag/index.md) ⬇ | 60 min |
| 26 | [Physics simulation](stage2c/26-maplesim/index.md) ⬇ | 55 min |

## Stage 2D — Advanced control

| | Lesson | Time |
|---|---|---|
| 27 | [Motion profiling](stage2d/27-motion-profiling/index.md) | 55 min |
| 28 | [System identification](stage2d/28-sysid/index.md) | 60 min |
| 29 | [State machines](stage2d/29-state-machines/index.md) | 50 min |
| 30 | [Season capstone](stage2d/30-season-capstone/index.md) | 180 min |

---

## Stuck?

1. **Read the failure message.** They are written as advice, not stack traces.
2. **Open the hints.** Four, escalating. The answer is only in the last.
3. **Plot it.** `frcprog sim` and `frcprog scope`. Most control problems are obvious
   as a picture and invisible as a number.
4. **Ask a person.** Peer help beats page help.
