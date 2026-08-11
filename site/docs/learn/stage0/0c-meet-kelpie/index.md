# Lesson 0C — Meet Kelpie <small>· Stage 0</small>

<span class="stage-badge">Stage 0 · Lesson 0C</span>

*Last lesson: a robot that shoots. This lesson: a robot that picks things up and places them. Two very different games, two very different mechanism stacks — and you're going to learn both.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 0 |
    | **Time** | ~15 min |
    | **Prereqs** | [Lesson 0B — Meet Presto](../0b-meet-presto/) |
    | **Edits** | None — observation lesson. |
    | **Tests** | None — checkbox lesson. |
    | **Reference robot** | [Kelpie (Team 8033, Reefscape 2025)](https://github.com/HighlanderRobotics/Reefscape) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Name **Kelpie's** mechanisms (swerve, elevator, shoulder, wrist, roller, climber, funnel).
2. Locate `elevator/ElevatorIO.java` on GitHub.
3. Describe **one structural difference** between how Kelpie and Presto organize their code.
4. Recognize **maple-sim** as a Stage 2 power-up that Presto doesn't have.

---

## The real-world problem

There isn't one "right" way to organize an FRC robot. Last lesson you saw 6328's house style. This lesson you'll see a *different* team's house style on a *different* kind of game — and the differences will teach you more than the similarities.

If you only studied one team's code, you'd come away thinking that team's choices are The Way. Two teams gives you depth perception.

---

## Meet Kelpie

!!! note "Why Kelpie?"

    Kelpie is **Team 8033 Highlander Robotics's** robot from the 2025 Reefscape season. 8033 also maintains a free, public [Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training) repository — when Kelpie's production code gets too dense, you can drop into the training repo, read the warm-up version, and come back stronger. That's a huge teaching asset, and it's why we picked them.

**Repo:** [github.com/HighlanderRobotics/Reefscape](https://github.com/HighlanderRobotics/Reefscape) · License: WPILib BSD (open use; permission for curriculum reference pending written confirmation).

The game: Reefscape 2025. Robots picked up "coral" (PVC-pipe-like tubes) from intake stations and placed them on a multi-level reef structure. They also picked up "algae" (soft balls) and scored them in side processors. Climbing onto a cage at end-of-match was the cherry on top.

### Mechanism inventory

Each subsystem maps to a folder in [`src/main/java/frc/robot/subsystems/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems).

| Subsystem | Folder | What it does | Lessons that use it |
|---|---|---|---|
| **Swerve drive** | `swerve/` (with `ModuleIO`, `ModuleIOReal`, `ModuleIOSim`, `ModuleIOMapleSim`, `GyroIO`, `OdometryThreadIO`, …) | 4-module MK4i swerve with Kraken X60 drive + Falcon turning | Stage 1C drive intro · **Stage 2B swerve deep-dive** |
| **Elevator** | `elevator/` | Vertical lift that carries the end-effector (shoulder+wrist+roller) up to four reef heights | Lesson 04 (subsystems) · **Lesson 05 (PID)** · Lesson 16 (IO refactor) |
| **Shoulder** | `shoulder/` | Pivots the end-effector forward and back | **Lesson 06** (the canonical `kG · cos(angle)` example) |
| **Wrist** | `wrist/` | Rotates the gripper at the end of the shoulder | Lesson 06 extension |
| **Roller** | `roller/` | Intake rollers for coral and algae | Lesson 04 (state-machine subsystem) |
| **Funnel** | (`FunnelSubsystem.java`) | A passive-ish chute that helps coral slide into the gripper | Stage 1D composition |
| **Climber** | `climber/` | End-of-match cage climber | Stage 2 capstone |
| **Manipulator / Superstructure** | `ManipulatorSubsystem.java`, `Superstructure.java` | Top-level coordination across elevator + shoulder + wrist + roller | Stage 1D + Stage 2A |
| **Vision** | `camera/` | Multi-camera PhotonVision pose estimation | Stage 2C |
| **LEDs / Beam-break / Servo** | `led/`, `beambreak/`, `servo/` | Driver feedback + sensors | Stage 2A telemetry |

!!! info "Notice the difference from Presto"

    Presto's flywheels live in **one** folder (`flywheels/`) with two motors. Kelpie's end-effector is split into **four** separate subsystems (`elevator/`, `shoulder/`, `wrist/`, `roller/`), one per moving part. Same idea — *one mechanism per subsystem* — applied at a finer grain because Reefscape needs more independent motions. There's no "right" answer; it's a style call. The curriculum follows Kelpie's grain because it makes lessons easier to focus.

---

## What you'll do

Three observations, just like last lesson. Look; don't memorize.

### 1. Watch Kelpie play (5 min)

Find a five-minute clip of Team 8033 placing coral on the reef:

[Watch on YouTube :material-youtube:](https://www.youtube.com/@HighlanderRobotics){ .md-button }

Watch the *sequence*: drive to a station, intake a coral, drive to the reef, the elevator climbs, the shoulder pivots, the wrist rotates, the roller releases, the elevator returns. Each motion you see is a subsystem from the table above.

### 2. Open `ElevatorIO.java` on GitHub (3 min)

The starting point for almost every Stage 1B and Stage 2A lesson is Kelpie's elevator. Skim it:

[`elevator/ElevatorIO.java` :material-github:](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/elevator/ElevatorIO.java){ .md-button }

Notice it's only about 30 lines. It's an **interface**, not a class. It defines what *inputs* the elevator records (position, velocity, voltage, current, temperature) and what *commands* you can send it (set a voltage, set a height). The actual hardware logic lives in two separate files next to it:

- [`ElevatorIOReal.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/elevator/ElevatorIOReal.java) — talks to real Krakens on the real robot.
- [`ElevatorIOSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/elevator/ElevatorIOSim.java) — talks to WPILib's `ElevatorSim` physics model.

!!! tip "Kelpie vs Presto naming"

    Presto names its hardware implementations after the motor vendor: `FlywheelsIOKrakenFOC`, `FlywheelsIOSparkFlex`. Kelpie uses generic names: `ElevatorIOReal`, `ElevatorIOSim`. Both work; the interface is what matters. Kelpie's naming is what the curriculum teaches because *"Sim vs Real"* is the abstraction students need first. We'll show Presto's vendor-named variant as an evolution in Stage 2A.

### 3. Visualize the elevator articulation (5 min)

Kelpie doesn't ship a 3D `.glb` model (yet). Instead, the team logs a `Mechanism2d` — a 2D stick-figure drawing of the elevator going up and down with the shoulder + wrist pivoting on top.

For now, **skip the AdvantageScope step** — there's nothing pre-built to load. Instead:

1. Open the [Reefscape repo](https://github.com/HighlanderRobotics/Reefscape) on GitHub.
2. Use GitHub's file search (press `t`) to find `Mechanism2d` in the codebase.
3. You'll land in a file where the team builds up a `Mechanism2d` with `MechanismLigament2d` segments. That's the 2D drawing that AdvantageScope renders.

By Stage 2A Lesson 17, you'll build one of these for your own elevator. Today: just notice it's there.

!!! tip "Photo placeholder"

    *(A photo of the real Kelpie at champs, next to a `Mechanism2d` screenshot showing the elevator + shoulder + wrist articulation, will live here once assets are added to the site.)*

---

## One thing Kelpie has that Presto doesn't: maple-sim

Open [`build.gradle`](https://github.com/HighlanderRobotics/Reefscape/blob/main/build.gradle) in the Kelpie repo and search for `maple-sim`. You'll find a vendordep called [maple-sim](https://github.com/Shenzhen-Robotics-Alliance/maple-sim) that gives the swerve drive **physics-accurate** simulation — the simulated robot has mass, friction, and can push other robots or game pieces around in sim. Presto's swerve uses WPILib's kinematics-only sim by comparison.

!!! quote "Why this matters later"

    > "If your auto routine collides with another robot in real life, you want to have seen that collision happen in sim first. Maple-sim is the difference between *'it works on paper'* and *'it works in the chaos of a real match.'"*

You won't use maple-sim until Stage 2C Lesson 26. But noticing it exists today means it won't be a surprise then.

---

## Going further (optional)

- Skim [Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training) — the team's free training repo. It's a less intimidating on-ramp than the production Kelpie code.
- Read the [Reefscape game manual](https://www.firstinspires.org/robotics/frc/game-and-season) if you want to understand the game Kelpie was built for.
- Compare Kelpie's [`Superstructure.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/Superstructure.java) to Presto's [`superstructure/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/superstructure) folder. Two teams, two top-level coordination strategies.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 0B**
    Meet Presto

    [:octicons-arrow-left-24: Back to lesson 0B](../0b-meet-presto/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 0D**
    Git + project tour

    [:octicons-arrow-right-24: Continue to lesson 0D](../0d-git-tour/)

</div>
