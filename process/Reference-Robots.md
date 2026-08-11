# Reference Robots
### Two real, published competition robots that anchor every lesson in the curriculum

> **What this document is.** [Curriculum-Flow.md](Curriculum-Flow.md) establishes *what* we teach. This doc establishes *which two specific robots* the lessons keep coming back to — the running examples a student is hand-holding all the way through Stage 1 and 2.
>
> **Why two robots?** Per the brainstorm in [FRCDesign-Analysis.md](FRCDesign-Analysis.md): one shooter, one pick-and-place. Between them, they cover every mechanism a Stage 1-2 student needs to see — swerve drivetrain, elevator, pivoting arm/shoulder, wrist, intake rollers, flywheels, climber. Recurring examples build familiarity; familiarity reduces cognitive load.

---

## TL;DR

| Role | Robot | Team | Game | Why |
|---|---|---|---|---|
| **Pick-and-place** | "Kelpie" | [Team 8033 Highlander Robotics](https://github.com/HighlanderRobotics/Reefscape) | Reefscape 2025 | Cleanest IO Layer pedagogy. Only candidate with `maple-sim` (physics-accurate swerve simulation). Distinct `elevator` / `shoulder` / `wrist` / `roller` subsystems map perfectly to Stage 1-2 lessons. Pairs with team's [training repo](https://github.com/HighlanderRobotics/Highlanders-Training). |
| **Shooter** | "Presto" | [Team 6328 Mechanical Advantage](https://github.com/Mechanical-Advantage/RobotCode2024Public) | Crescendo 2024 | The canonical AdvantageKit reference (written by AdvantageKit's authors). MIT-licensed. Ships a 3D `.glb` AdvantageScope model. `FlywheelsIO.java` is *the* example pattern everyone copies. |

**Action items before locking these in:**
1. **Email Team 8033** about Kelpie's license — currently ambiguous (only WPILib boilerplate; no team LICENSE file).
2. **Pin specific commit SHAs** for both repos. These are "Open Alliance" repos that get updated continuously.
3. **Decide on a 3D model strategy for Kelpie** — either ask 8033 to publish one or fall back to `Mechanism2d` + robot photos in AdvantageScope.

Why NOT REBUILT 2026 robots (despite REBUILT being the current shooter season): codebases are still in active development as of May 2026, missing 3D models, hardware IO impls incomplete. Crescendo is mature, frozen, and battle-tested as a teaching reference.

---

## 1. Why these two

### Why a fixed reference pair at all?

Lessons that invent a new fictional robot for every concept force the student to absorb robot details *and* concept details simultaneously. By the third lesson they're learning their fourth mechanism. Cognitive load wins; pedagogy loses.

The FRCDesign.org playbook (which we're cloning) sidesteps this with **mechanism examples** — real teams' real robots, broken down with photos. We do the same, but doubled-down: every lesson references the same two robots, so by Stage 1C the student is already familiar with "Kelpie's elevator" and "Presto's flywheels."

Per the original brainstorm: *"Two main robots, a shooting robot and a pick-and-place robot. … Give robot tours at the beginning (make people familiar with interacting with these robots at the beginning)."*

### Why these two specific robots?

Both satisfy the seven non-negotiable criteria:

1. ✓ **Public, OSS-compatible code** (caveat on Kelpie license; see §5).
2. ✓ **Command-based** (not custom frameworks like 254's).
3. ✓ **AdvantageKit + IO Layer pattern** — `XxxIO` / `XxxIOSim` / `XxxIOReal` co-located with each subsystem, with `@AutoLog` inputs.
4. ✓ **Working simulation** — both run `./gradlew simulateJava` and produce meaningful behavior in AdvantageScope.
5. ✓ **Pedagogically readable** — top-tier teams; not 1000-line god-subsystems.
6. ✓ **Mechanism coverage** — between them: swerve, elevator, shoulder/arm, wrist, intake roller, climber, flywheel shooter, pivoting arm.
7. ✓ **Tractable complexity** — small enough to read in an afternoon, big enough to be real.

---

## 2. The pick-and-place robot: 8033 "Kelpie" (Reefscape 2025)

**Repo:** <https://github.com/HighlanderRobotics/Reefscape>

### 2.1 What it is

Team 8033 Highlander Robotics's 2025 Reefscape competition robot. Reefscape's pick-and-place challenge involves picking up "coral" (PVC-pipe-like tubes) from intake stations and placing them on a multi-level reef structure, plus picking up "algae" balls and scoring them in processors/nets.

### 2.2 Mechanism inventory

Each maps to lessons in the curriculum:

| Subsystem | Files (in `src/main/java/frc/robot/subsystems/`) | What it does | Curriculum lessons that use it |
|---|---|---|---|
| **Swerve drive** | `swerve/{ModuleIO, ModuleIOReal, ModuleIOSim, ModuleIOMapleSim, GyroIO, GyroIOPigeon2, GyroIOSim, OdometryThreadIO, SwerveSubsystem}` | 4-module swerve, MK4i with Kraken X60 + falcon turning | Stage 1C drivetrain intro; Stage 2B swerve deep-dive |
| **Elevator** | `elevator/{ElevatorIO, ElevatorIOReal, ElevatorIOSim, ElevatorSubsystem}` | Vertical extension carrying the shoulder+wrist+roller end effector | Lesson 04 (Subsystems), Lesson 05 (PID), Lesson 16 (IO Layer refactor) |
| **Shoulder** | `shoulder/` | Pivots the end-effector forward/back | Lesson 06 (Arm + gravity FF) — the canonical `kG = constant × cos(angle)` example |
| **Wrist** | `wrist/` | Rotates the gripper at the end of the shoulder | Lesson 06 extension — second-order arm |
| **Roller** | `roller/` | Coral/algae intake rollers | Lesson 04 (state machine subsystem) |
| **Climber** | `climber/` | End-of-match cage climber | Stage 2 capstone material |
| **Funnel + Manipulator + Superstructure** | `FunnelSubsystem.java`, `ManipulatorSubsystem.java`, `Superstructure.java` | Top-level coordination | Stage 1D (composition) + Stage 2A (multi-subsystem state) |
| **Vision** | `camera/` | PhotonVision multi-camera pose estimation | Stage 2C (vision) |
| **LEDs / Beam-break / Servo** | `led/`, `beambreak/`, `servo/` | Driver feedback + sensors | Stage 2A telemetry, Stage 2A sensors |

### 2.3 Why it's the pick-and-place pick

**(a) IO Layer naming is exactly what the curriculum teaches.**
Files are named `ElevatorIOReal.java` and `ElevatorIOSim.java`, not vendor-specific names like `ElevatorIOTalonFX.java`. This is the abstraction the curriculum teaches: "hardware vs sim, not Talon vs Spark." Stack-rank pedagogical win.

**(b) Maple-sim is integrated for the swerve drive.**
The vendordep `maple-sim.json` is present, and `ModuleIOMapleSim.java` exists alongside `ModuleIOSim.java`. [maple-sim](https://github.com/Shenzhen-Robotics-Alliance/maple-sim) provides physics-accurate (not just kinematic) swerve simulation — the robot can actually push around game pieces and other robots in sim. **No other candidate had maple-sim**. This is a significant teaching upgrade for Stage 2 drivetrain lessons.

**(c) Mechanism granularity is ideal for teaching.**
Separate `shoulder` + `wrist` + `elevator` + `roller` subsystems means each can be the focus of one lesson without the others getting in the way. Compare to robots that bundle "arm + intake + claw" into one giant subsystem class — pedagogical disaster.

**(d) Sim implementations are real, not stubs.**
`ElevatorIOSim` uses WPILib's `ElevatorSim` with proper Kraken X60 FOC motor model, 2:1 gear ratio, and 0.06 V gravity feedforward — the exact form Lessons 05/16 teach. Students reading this code see what real, production-ready sim code looks like.

**(e) Companion training repo.**
The team also maintains [Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training) — a public training repository covering WPILib → command-based → AdvantageKit → controls → swerve → Choreo → PhotonVision. Students confused by the production Kelpie code can drop down to the training repo and back. This is a *huge* pedagogical asset, and it's free.

### 2.4 Tradeoffs / honest caveats

**License is ambiguous.** The repo contains `WPILib-License.md` (the WPILib boilerplate BSD-3) but no team-specific `LICENSE` file. GitHub's license detector reports "Other." The WPILib boilerplate covers WPILib code that ships with the project, not the team's own code. **Action item:** email <https://www.frc8033.com/> to ask for explicit written permission to use the repo as a curriculum reference. Given they publish a training repo (clearly inviting reuse), they'll likely consent — but get it in writing.

**Comments are casual/sparse.** The team's culture shows up in comments like `// lmao` and `// i'm quite scared` (mostly in their 2024 Crescendo repo, but still). Our curriculum copy will provide the explanatory layer; don't expect Kelpie's source to teach itself.

**No 3D AdvantageScope model.** The repo has no `ascope_assets/` directory, so AdvantageScope's 3D field view will show a generic robot box, not Kelpie specifically. **Mitigation options:**
- Ask 8033 to publish a `.glb` model (likely a small effort if they have CAD).
- Use `Mechanism2d` for the elevator/shoulder/wrist articulation instead — also works, less photogenic.
- Pair lessons with photos of the real robot.

**Swerve is intimidating.** Five swerve-related files (`ModuleIO`, `ModuleIOReal`, `ModuleIOSim`, `ModuleIOMapleSim`, `GyroIO`+impls) before students even see the elevator. **Mitigation:** Stage 1 lessons use the simpler `DriveIO` pattern from [path-b-demo/](../examples/path-b-demo/); Kelpie's swerve is introduced as a Stage 2B+ deep dive, *not* as Stage 1's first example.

### 2.5 Lesson-author cheat sheet for Kelpie

When writing a lesson that references Kelpie:

```
Concept                          | Code to reference
---------------------------------|--------------------------------------------
"What an IO interface looks like"| src/.../elevator/ElevatorIO.java
"What an IOReal looks like"      | src/.../elevator/ElevatorIOReal.java
"What an IOSim looks like"       | src/.../elevator/ElevatorIOSim.java
"What a SubsystemBase does"      | src/.../elevator/ElevatorSubsystem.java
"Multi-module IO" (advanced)     | src/.../swerve/ModuleIO + ModuleIOSim + ModuleIOMapleSim
"Sensors in the IO inputs"       | src/.../beambreak/  +  any inputs with sensors
"Two subsystems coordinating"    | src/.../Superstructure.java
```

---

## 3. The shooter robot: 6328 "Presto" (Crescendo 2024)

**Repo:** <https://github.com/Mechanical-Advantage/RobotCode2024Public>

### 3.1 What it is

Team 6328 Mechanical Advantage's 2024 Crescendo competition robot, named "Presto." Crescendo's shooter challenge involved picking up foam "notes" (donut shapes), aiming with a pivoting shooter, and firing them into a hub from variable distances. Trap and amp scoring required additional mechanisms.

Team 6328 are the authors of [AdvantageKit](https://github.com/Mechanical-Advantage/AdvantageKit) and [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope). Their public competition code is, definitionally, the canonical reference for "how to use AdvantageKit well."

### 3.2 Mechanism inventory

| Subsystem | Files (`src/main/java/org/littletonrobotics/frc2024/subsystems/`) | What it does | Curriculum lessons |
|---|---|---|---|
| **Swerve drive** | `drive/` | 4-module swerve | Stage 1C / Stage 2B |
| **Flywheels** | `flywheels/{FlywheelsIO, FlywheelsIOKrakenFOC, FlywheelsIOSparkFlex, FlywheelsIOSim, Flywheels}` | The shooter wheels (two independent flywheel pairs for spin control) | **The canonical reference for Lesson 18 (flywheels & feedforward)** |
| **Rollers** | `rollers/` | Note conveyance from intake to shooter | Lesson 04 (basic subsystem) |
| **Superstructure (Arm + Climber + Backpack)** | `superstructure/arm/`, `superstructure/climber/`, `superstructure/backpackactuator/` | Pivoting shooter arm + trap climber + amp backpack | Stage 1D composition; Stage 2A advanced |
| **AprilTag Vision** | `apriltagvision/` | Multi-tag pose estimation | Stage 2C |
| **LEDs** | `leds/` | Driver feedback | Stage 2A |
| **Generic Slam Elevator** | `GenericSlamElevator.java` | Reusable pattern for "slam to limit, set zero" mechanisms | Stage 2A pattern lesson |

### 3.3 Why it's the shooter pick

**(a) It's the canonical AdvantageKit reference.**
Written by AdvantageKit's own authors. `FlywheelsIO.java` is exactly the pattern the [AdvantageKit docs](https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/) describe: `@AutoLog`-annotated `FlywheelsIOInputs` class with `positionRotations`, `velocityRPM`, `appliedVolts`, `currentAmps`, `tempCelsius`. Other teams copy this file structure directly.

**(b) MIT license, unambiguous.**
The repo has an explicit `LICENSE` file (MIT). SPDX-compliant. No license-clarification phone calls needed.

**(c) 3D AdvantageScope model included.**
`ascope_assets/Robot_Presto/` contains `config.json`, `model.glb`, and `model_0.glb` (articulated component model). Students who download this repo can open AdvantageScope and immediately see Presto rendered in 3D — building visual intuition for "this is my robot's pose right now."

**(d) Conceptually clean shooter mechanism.**
Two independent flywheels + pivot arm + indexing rollers + drive. Easy to explain in one sentence: *"spin two wheels at variable speed, aim with the pivot, push notes in with rollers."* Two-flywheel design is also the gateway to teaching **why** independent left/right flywheel speeds matter (spin imparts curve to the projectile, just like in baseball).

**(e) FlywheelSim with Kraken FOC is the textbook example.**
`FlywheelsIOSim` uses WPILib's `FlywheelSim` with `DCMotor.getKrakenX60Foc(1)`, the modern recommended motor model. Stage 1B's PID-introduction lesson can quote this code directly.

**(f) Real hardware impls exist for both Kraken and SparkFlex.**
`FlywheelsIOKrakenFOC.java` and `FlywheelsIOSparkFlex.java` side-by-side teach "the IO interface lets you swap motor vendors without changing the subsystem." This is the entire point of the IO pattern, made concrete.

### 3.4 Tradeoffs / honest caveats

**The "superstructure" coordination class is non-trivial.** 6328 wraps `arm + climber + backpackactuator` in a `superstructure/` package with its own state machine. For a Stage 1 student, this is overkill — they're still learning what a subsystem *is*. **Mitigation:** Stage 1 lessons reference Presto's `flywheels/` + `drive/` only. The superstructure becomes Stage 2A's "how subsystems coordinate" capstone.

**No `maple-sim` (unlike Kelpie).** Crescendo's shooter doesn't really need it — WPILib's `FlywheelSim` is the right tool for flywheel physics, and swerve was simpler in 2024. But Presto's drivetrain sim isn't as visually impressive as Kelpie's maple-sim swerve.

**Hardware IO names are vendor-specific** (`FlywheelsIOKrakenFOC`, `FlywheelsIOSparkFlex`), not the more pedagogical `FlywheelsIOReal`. Two ways to read this:
- *Negative:* breaks the "real vs sim" abstraction the curriculum teaches.
- *Positive:* shows students that production code often needs vendor-specific impls, and the IO interface is what makes that survivable.

We'll lean positive in lessons — explain it as: *"The 'Real' vs vendor naming choice is a style call. 8033 uses `Real`; 6328 uses vendor names. Both work; the interface is what matters."*

**Built around 2024 WPILib + AdvantageKit.** The codebase predates some 2025/2026 API changes (especially the units library refactor). For lessons that demonstrate `Measure<T>` or other recent APIs, look at 6328's [RobotCode2025Public](https://github.com/Mechanical-Advantage/RobotCode2025Public) ("Manta") instead — but Manta is significantly more complex (16+ vendor deps, custom Python vision, native C++ trajectory solver). Crescendo Presto is the *teaching* sweet spot; Manta is too big.

### 3.5 Lesson-author cheat sheet for Presto

```
Concept                              | Code to reference
-------------------------------------|--------------------------------------------
"Canonical AdvantageKit IO interface"| src/.../flywheels/FlywheelsIO.java
"FlywheelSim with motor model"       | src/.../flywheels/FlywheelsIOSim.java
"Real hardware impl (Kraken)"        | src/.../flywheels/FlywheelsIOKrakenFOC.java
"Real hardware impl (different vendor)" | src/.../flywheels/FlywheelsIOSparkFlex.java
"Subsystem class with factory commands"  | src/.../flywheels/Flywheels.java
"Coordinated multi-mechanism state"  | src/.../superstructure/
"AdvantageScope 3D model setup"      | ascope_assets/Robot_Presto/config.json
```

---

## 4. Candidate comparison matrix

The full list of repos investigated, with verdicts. Detail on each in [the research notes from the agent that produced this doc].

| Repo | Year | Type | License | IO Layer | Sim | maple-sim | 3D Model | Verdict |
|---|---|---|---|---|---|---|---|---|
| **6328 RobotCode2024Public (Presto)** | 2024 | Shooter | MIT | ✓ Full + @AutoLog | ✓ Full | — | ✓ .glb | **PICK** |
| **8033 Reefscape (Kelpie)** | 2025 | P&P | ⚠ Unclear | ✓ Full | ✓ Full + maple | ✓ | — | **PICK** |
| 6328 RobotCode2025Public (Manta) | 2025 | P&P | MIT | ✓ Full | ✓ Full | — | ✓ 7 .glb | Backup P&P. Too complex for Stage 1. |
| 6328 RobotCode2026Public (Darwin) | 2026 | Shooter | MIT | ⚠ Incomplete | ⚠ Partial | — | ✗ | Moving target. Re-evaluate post-season. |
| 6328 RobotCode2023 (Banana Split) | 2023 | P&P | MIT | ✓ Full | ⚠ Partial | — | — | Arm uses Python-based inverse kinematics — pedagogically distracting. |
| 8033 Crescendo | 2024 | Shooter | ⚠ Unclear | ✓ Full | WPILib | — | — | Casual comments; possible logical bug. |
| 8033 Rebuilt | 2026 | Shooter | ⚠ Unclear | ⚠ Partial | maple | — | — | In-progress. Re-evaluate later. |
| 1678 C2025-Public | 2025 | P&P | ⚠ Unclear | ⚠ Non-standard | ⚠ Unclear | — | — | 16 subsystems. Non-standard IO organization. |
| 2910 2024CompetitionRobot-Public | 2024 | Shooter | MIT+GPL | ⚠ Partial | ⚠ Partial | — | — | Archived. FSM-heavy. |
| 2910 2025CompetitionRobot-Public | 2025 | P&P | MIT+GPL | ⚠ Non-standard | CTRE sim | — | — | Archived. CTRE swerve bypasses AK abstraction. |
| 3061 frc-software-2024 (Huskies) | 2024 | Shooter | MIT | ⚠ Custom (3061-lib) | WPILib | — | — | Custom template obscures AK pattern. |

Legend: ✓ great fit, ⚠ partial/concern, — not present, ✗ blocker

---

## 5. Action items before locking these in

In rough order:

### 5.1 Get the license question answered (Kelpie)
**Owner:** Curriculum lead.
**Action:** Email Team 8033 (<https://www.frc8033.com/> contact page or open a GitHub issue on the Reefscape repo) asking:
- Is Kelpie's code intended to be open-source under a standard OSS license? (MIT? Apache 2? BSD?)
- Are we welcome to reference specific files and quote short snippets in curriculum material that links back to your repo?
- (If they want attribution beyond the default link, get the wording in writing.)

The team's existence of [Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training) strongly suggests they'll consent. But "strongly suggests" is not "documented permission."

### 5.2 Pin specific commit SHAs

**Owner:** Tech lead.
**Action:** As part of Phase 0 finalization, choose a specific commit on each repo's `main` branch and pin that commit in the curriculum. Open Alliance repos update continuously; lessons referencing line numbers will rot.

Suggested approach:
```
shooter-robot/  → vendored as a git submodule pinned to <SHA>
pick-and-place-robot/ → vendored as a git submodule pinned to <SHA>
```

Or simpler: maintain a `references.json` in the curriculum repo listing the SHAs, with a CI check that flags drift.

### 5.3 Decide on Kelpie's 3D-model strategy

**Owner:** Curriculum lead + Tech lead.
**Options ranked:**

| Option | Effort | Quality |
|---|---|---|
| Ask 8033 to publish a `.glb` (if they have CAD) | Email + maybe 1 day on their side | Best (real robot in 3D) |
| Commission a community contributor to model it from photos | 1-3 weeks of someone's time | Good |
| Skip 3D — use `Mechanism2d` + robot photos in lessons | Zero | Acceptable for Stage 1 |
| Use 6328 Manta's 3D model as a stand-in for "a Reefscape robot" | Zero | Misleading |

Recommended: start with `Mechanism2d` + photos for Stage 1 lessons, ask 8033 in parallel, and slot in a real `.glb` if/when it materializes.

### 5.4 Write the "robot tour" lessons

**Owner:** Curriculum lead.
**Per the original brainstorm:** *"Give robot tours at the beginning (make people familiar with interacting with these robots at the beginning)."*

Concrete plan:
- **Lesson 0A — "Meet Presto"** — 10-15 minute introductory lesson at the start of Stage 1. Photos of the real Presto, what each mechanism does in plain language ("two wheels that spin really fast to launch a foam donut"), a video of it scoring at champs, the GitHub link.
- **Lesson 0B — "Meet Kelpie"** — same shape, for the pick-and-place robot.

These appear before Lesson 01 in the order. Both lessons end with the student looking at the real GitHub repo for that robot — *not* as an exercise (no code yet), but as an "this is what your code is going to look like in six months" hook.

### 5.5 Build a `Reefscape-Tour.md` and `Crescendo-Tour.md` in the curriculum

A two-page tour for each, used by lesson authors as reference material. Topics:
- Game rules (one paragraph)
- Robot photo (1-2 images)
- Mechanism inventory (the tables in §2.2 and §3.2)
- "What's interesting about this robot" — the pedagogical hooks
- Links to the team's reveal video, build thread, season recap

These pages double as student reference material when they're confused by something later ("wait, what does the funnel even do?").

---

## 6. Risks and re-evaluation triggers

What could change the picks. Watch for:

| Trigger | Response |
|---|---|
| 8033 declines OSS use of Kelpie | Fall back to **6328 Manta (RobotCode2025Public)** as the pick-and-place reference. MIT-licensed, has 3D model, but ~2x more complex. |
| 6328 publishes a clean tutorial-focused version of RobotCode2026Public (Darwin) before Phase 2 | Consider swapping in Darwin for Presto — same authors, same patterns, current-season relevance. Verify 3D model + sim completeness first. |
| AdvantageKit 27.0 introduces breaking API changes | Both reference robots will need re-verification at next kickoff. Plan into the [Path-B-Implementation.md §6.1 yearly ritual](Path-B-Implementation.md#61-the-kickoff-season-ritual-every-january). |
| `maple-sim` is deprecated or replaced | Re-evaluate Kelpie's drivetrain story; possibly degrade to vanilla WPILib swerve sim. Doesn't kill the pick, just diminishes it. |
| A new team publishes a perfectly-pedagogical reference repo with explicit teaching intent | Evaluate against current picks. Strong contenders worth watching: any team that explicitly publishes a "training" + "competition" pair like 8033 does. |

The two-robot pair is meant to be stable across 2-3 seasons — re-evaluating annually but not casually swapping. Students invest in learning these robots; pulling the rug undermines the curriculum.

---

## 7. Why not the current-season (2026 REBUILT) robots?

REBUILT 2026 is the real shooter game this season (it's a hub-shooter inspired by 2012's Rebound Rumble and 2017's Steamworks). Two strong teams already have public code in flight:

- [6328 RobotCode2026Public (Darwin)](https://github.com/Mechanical-Advantage/RobotCode2026Public)
- [8033 Rebuilt](https://github.com/HighlanderRobotics/Rebuilt)

It's tempting to use a current-season robot — relevance is a real motivator. But three problems:

**(a) Moving targets.** Both repos are pushing commits as of May 2026. Lessons that reference specific file paths or class structures will go stale weekly.

**(b) Missing assets.** Neither repo has a complete AdvantageScope 3D model published yet. Darwin's `ascope_assets/Robot_Darwin/` directory does not exist; the model file is absent. Lessons that depend on visual feedback will be hobbled.

**(c) Hardware IO impls incomplete.** Darwin's `FlywheelIO.java` and `FlywheelIOSim.java` exist; the real-hardware impl is not yet in the public repo. Students who want to compare `IOReal` and `IOSim` side-by-side can't.

**Plan:** Re-evaluate at the **August 2026 build-season closeout**. If 6328 or 8033 publishes a clean, complete, 3D-modeled REBUILT robot by then, consider swapping. Until then, **Crescendo Presto is the stable shooter reference**.

---

## 8. Connecting back to the rest of the docs

- [Curriculum-Flow.md Appendix A](Curriculum-Flow.md#appendix-a-lesson-to-concept-map) maps lessons to concepts. **Future work:** add a column to that table linking each lesson to the specific Kelpie or Presto file it references.
- [Path-B-Implementation.md §10](Path-B-Implementation.md#10-content-authoring-at-scale) specifies the lesson template. **Future work:** lesson templates should auto-suggest a Kelpie or Presto reference based on the lesson's stage/concept.
- [Infrastructure-Analysis.md §3.4](Infrastructure-Analysis.md#34-the-advantagekit-io-layer-pattern-as-the-teaching-substrate) explains why the IO Layer pattern matters. Kelpie and Presto are the two best examples of that pattern in the wild; this doc is where the architectural argument gets named files.

---

## Appendix — Quick-link bibliography

**Primary robots:**
- 8033 Kelpie (Reefscape 2025): <https://github.com/HighlanderRobotics/Reefscape>
- 6328 Presto (Crescendo 2024): <https://github.com/Mechanical-Advantage/RobotCode2024Public>

**Companion teaching resources:**
- 8033 Highlanders-Training: <https://github.com/HighlanderRobotics/Highlanders-Training>
- 6328 AdvantageKit: <https://github.com/Mechanical-Advantage/AdvantageKit> / <https://docs.advantagekit.org>
- 6328 AdvantageScope: <https://github.com/Mechanical-Advantage/AdvantageScope> / <https://docs.advantagescope.org>

**maple-sim** (physics-accurate swerve sim used by Kelpie):
- <https://github.com/Shenzhen-Robotics-Alliance/maple-sim>

**Backup candidates:**
- 6328 Manta (Reefscape 2025): <https://github.com/Mechanical-Advantage/RobotCode2025Public>
- 6328 Banana Split (Charged Up 2023): <https://github.com/Mechanical-Advantage/RobotCode2023>
- 6328 Darwin (REBUILT 2026, in-progress): <https://github.com/Mechanical-Advantage/RobotCode2026Public>
- 8033 Rebuilt (REBUILT 2026, in-progress): <https://github.com/HighlanderRobotics/Rebuilt>

**For seasonal reference / not chosen:**
- 1678 C2025-Public: <https://github.com/frc1678/C2025-Public>
- 2910 2024CompetitionRobot-Public: <https://github.com/FRCTeam2910/2024CompetitionRobot-Public>
- 2910 2025CompetitionRobot-Public: <https://github.com/FRCTeam2910/2025CompetitionRobot-Public>
- 3061 frc-software-2024: <https://github.com/HuskieRobotics/frc-software-2024>

**FRC 2026 REBUILT game info:**
- <https://www.firstinspires.org/programs/frc/game-and-season>
- <https://www.frcmanual.com/2026/game-details>
