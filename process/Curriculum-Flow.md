# Curriculum Flow
### How FRCProgramming.org's lessons sequence, build on each other, and reflect the FRC community's pedagogical consensus

> **What this document is.** [Infrastructure-Analysis.md](Infrastructure-Analysis.md) is the architecture. [Path-B-Implementation.md](Path-B-Implementation.md) is the operational roadmap. **This is the pedagogy** — what we teach, in what order, why each concept lands when it does, which best practices we bake in from day one, and which anti-patterns we actively preempt.
>
> **Audience.** Lesson authors and curriculum reviewers. Mentors who want to understand why lessons are sequenced the way they are. Future contributors who need to slot a new lesson into the existing flow without breaking it.

---

## TL;DR

We teach Command-Based as the **answer to a problem students have just felt** — not as the starting point. Niwiden's pedagogy (pain → abstraction → relief), Oblarg's three modern best-practice principles (factories, triggers, bindings), and 6328's IO Layer pattern form the spine. We deliberately introduce subsystems *after* students have written messy `teleopPeriodic` code and felt it not scale. We teach the factory pattern from day one, never Command subclasses for the basic cases. We treat AdvantageKit and `Measure<T>` as Stage 2 extensions, not Stage 1 prerequisites.

The single most important pedagogical bet: **earned abstractions beat imposed abstractions.** Every new concept should solve a problem the student has felt 10 minutes ago, not a problem the curriculum tells them they will feel.

---

## 1. The pedagogical thesis: pain before abstraction

Our curriculum spine is borrowed from **Katie Niwiden's "From 0 to Robot: Teaching Programming to Beginners"** — a presentation given by an FRC alumna (Team 1675) turned CS teacher (Palo Alto HS / Team 253 mentor). Slides: <https://docs.google.com/presentation/d/15O2Xo5cHsYG3hVvQbMSB2SuvU9ED0Y3feaKdCbgaQyM/preview>.

Her central insight, paraphrased from the deck: *"This presentation is NOT a lesson on how to program. It is about how to teach people to program."* She advocates a sequence that deliberately delays formal abstractions until students have felt their absence:

| Lesson order (Niwiden) | What students do | What's deferred |
|---|---|---|
| 1. **Meet code & motors** | Deploy empty code, look at parts of project structure, identify electrical components | All abstraction |
| 2. **Drivetrain** | Multiple motors, leader/follower, reverse, tank drive | Subsystems, commands |
| 3. **Sensing** | Buttons, sensors, conditionals in `teleopPeriodic` (explicit: *"You may use command based programming to enable actions, but use conditionals and teleop periodic. We're still building the base."*) | Object-oriented organization |
| 4. **Objects (Subsystems)** | "How do we organize complex code? … Move code from previous lessons into relevant subsystem objects" | **Now command-based is introduced — to solve the disorganization students just felt** |
| 5. **Positional PID** | Encoders, PID, tuning, graphing | |
| 6. **My first auto** | Sequential commands, wait times | |
| Extensions | Velocity PID, pneumatics, gyros, PathPlanner | |

**The four pedagogical rules baked into our curriculum:**

1. **"Every lesson is relevant, supportive, hands-on, appropriately challenging."** Bloom's taxonomy + Zone of Proximal Development, made explicit.
2. **"Why don't you start with syntax? It's boring. It pushes students away. Too much extra info. They'll learn it as they need it."** We never front-load syntax.
3. **"Lazy mentoring is good teaching."** Lessons assume mentors will model not-knowing, let students fail, and have students teach each other. **"Do not type — the person typing is the person learning."**
4. **The Mentor Mantra:** *"Ask when they're off task. Show when they don't know. Do when they have no clue."*

**What this means for lesson authors:** never teach a concept in the lesson where you *introduce* it. Teach it in the lesson where the *previous* lesson made the student wish they had it.

---

## 2. The conceptual prerequisite graph

What we teach, and in what order, falls out of which concepts depend on which:

```
[ Variables / control flow ] → [ Methods ] → [ Classes / OOP basics ]
        │                          │                  │
        ▼                          ▼                  ▼
[ print / debug ]      [ function headers ]    [ encapsulation: private fields ]
                                                       │
[ Driver Station / deploy ] ──→ [ direct motor.set in teleopPeriodic ]
                                                       │
[ Joystick input ] ──→ [ conditionals + sensors + buttons ]
                                                       │
                                          [ ★ the pain of disorganization ★ ]
                                                       │
                                                [ Subsystem class ]
                                                       │
                                ┌──────────────────────┼──────────────────────┐
                                ▼                      ▼                      ▼
                          [ Command ]            [ Trigger ]            [ Constants ]
                                │                      │
                ┌───────────────┴───────────────┐      │
                ▼                               ▼      ▼
       [ factory methods ]        [ Suppliers / lambdas ]
                │                               │
                ▼                               ▼
       [ Command composition:                   [ Trigger.and / or / debounce ]
         andThen / alongWith /                            │
         race / deadline ]                                ▼
                │                            [ onTrue / whileTrue / etc ]
                └──────────────────┬──────────────────────┘
                                   ▼
                          [ Default commands ]
                                   ▼
                        [ CommandScheduler &
                          requirement system ]
                                   ▼
                          [ PID + Feedforward ]
                                   ▼
                    [ Motion profiling + autos /
                      PathPlanner / Choreo ]
                                   ▼
                 [ IO Layer / AdvantageKit / log replay ]
                                   ▼
                          [ Units / Measure<T> ]
                                   ▼
                          [ Swerve, vision,
                            advanced control ]
```

**Notes on this DAG:**

- The **★ pain of disorganization ★** node is real and deliberate. Niwiden's deck explicitly makes students write `teleopPeriodic` code with conditionals *before* introducing subsystems. Don't skip this step. It's what makes the abstraction feel earned.
- **IO Layer / AdvantageKit** is on the critical path *to log replay and serious sim-driven debugging*, but it is NOT a prereq for "write your first command." Treat it as a Stage 2 graduation, not a Stage 1 requirement. 6328's own training acknowledges this; FRC 8033's training repo (<https://github.com/HighlanderRobotics/Highlanders-Training>) sequences AdvantageKit *after* basic command-based.
- **Units (`Measure<T>`)** is in the same boat — a graduation concept, not an onboarding one. The API has also shifted year-over-year (2024 generic `Measure<Distance>` → 2025 dimension-specific records like `Distance`), so link to live docs rather than freeze syntax.

---

## 3. The three load-bearing best practices (Oblarg 2025)

Modern command-based pedagogy as of 2025-2026 is built on three principles from **Oblarg's "Command-Based Best Practices for 2025"** post (<https://www.chiefdelphi.com/t/command-based-best-practices-for-2025-community-feedback/465602>), best summarized by **BoVLB's distillation** at <https://bovlb.github.io/frc-tips/commands/best-practices.html>. Quoted from BoVLB:

> **(1) Control subsystems using command factories.**
> **(2) Get information from subsystems using triggers.**
> **(3) Co-ordinate between subsystems by binding commands to triggers.**
>
> *"Following these three rules will reduce dependencies between subsystems and gather all cross-subsystem behaviour in one place. This makes your code easier to write, easier to maintain, less likely to have bugs, and more reusable."*

Every lesson from Stage 1C onward enforces these three principles. The implication for lesson design:

### 3.1 Factories over Command subclasses

The factory pattern — a public `Command`-returning method on the subsystem — is now the WPILib-official recommendation. From the official docs (<https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html>):

> *"Through the use of lambdas, these commands can cover almost all use cases and teams should rarely need to write custom command classes. … As [subclassing] is significantly more verbose, it's recommended to use the more concise factories."*

**What we teach (canonical pattern):**

```java
public class Intake extends SubsystemBase {
    private final TalonFX motor = new TalonFX(5);

    public Command intakeNote() {
        return run(() -> motor.setVoltage(12.0))
                .until(this::hasNote)
                .finallyDo(() -> motor.setVoltage(0));
    }
}
```

**What we used to teach (and no longer do):**

```java
public class IntakeNoteCommand extends Command {
    private final Intake intake;
    public IntakeNoteCommand(Intake i) { intake = i; addRequirements(i); }
    @Override public void execute() { intake.setMotor(12); }
    @Override public boolean isFinished() { return intake.hasNote(); }
    @Override public void end(boolean i) { intake.setMotor(0); }
}
```

The factory version is shorter, lives next to the hardware it controls, and `require`s the subsystem automatically (because it's defined inside the subsystem). Command subclasses remain appropriate **only** for stateful commands (e.g., motion profiles, multi-phase routines).

This shift is significant enough that the WPILib "Organizing Command-Based Projects" page (<https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html>) now makes it the default — and reframes Command-subclass-heavy code as *"just as cumbersome as the original repetitive code, if not more verbose."* Our curriculum follows the official position.

### 3.2 Suppliers, not captured values

The single most common subtle bug in beginner command-based code. From WPILib docs and BoVLB:

```java
// ✗ WRONG — joystick value is read ONCE at construction time
drive.driveCommand(controller.getLeftY());

// ✓ RIGHT — joystick value is read every tick, inside the lambda
drive.driveCommand(() -> controller.getLeftY());
// or, equivalently:
drive.driveCommand(controller::getLeftY);
```

The wrong version "works" the first time the command runs — then never updates. The student will think the command is broken when actually the lambda is. **Every lesson involving joystick input must demonstrate this pattern explicitly**, ideally with a "now break it and see what happens" exercise.

### 3.3 State exposure via Triggers, not getters

Subsystems should expose state as `Trigger` instances, not as numeric getters:

```java
// ✗ Old-style: numeric getter forces every caller to interpret
public double getBeamBreakDistance() { return sensor.getRange(); }

// ✓ Trigger-style: subsystem makes the decision once, in problem-domain language
public final Trigger gamepieceDetected =
    new Trigger(() -> sensor.getRange() < 200);
```

The trigger version means cross-subsystem behavior reads naturally:

```java
intake.gamepieceDetected
    .onTrue(elevator.moveToScoringPosition()
                .andThen(shooter.score()));
```

This is the third Oblarg principle in action: cross-subsystem coordination is binding commands (factories) to triggers, **all in `RobotContainer`**, with no subsystem directly calling another. The result: subsystems compose like Lego, with no surprise dependencies.

---

## 4. Stage-by-stage curriculum flow

Each stage answers a different developmental question. The stage boundaries map to the existing structure from [FRCDesign-Analysis.md](FRCDesign-Analysis.md) and the lesson sequence in [Path-B-Implementation.md §3.1](Path-B-Implementation.md#31-deliverables).

### Stage 0 — Onboarding (no programming yet)

| What | Why | Resources |
|---|---|---|
| Install WPILib + VS Code (bundled JDK!) | The single highest-friction step in FRC programming; if a student bounces, it's here. | <https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html> |
| Driver Station, RoboRIO flash, radio flash, Game Tools | Hardware/software interaction starts as a video + checklist lesson, not interactive code. | FRC Game Tools docs |
| Git basics (`clone`, `commit`, `push`) | Git is taught as workflow ritual, not concept. *"Run these three commands"* — questions deferred. | Real GitHub repo from day one |
| Project tour | Open the template repo. Show `Main.java`, `Robot.java`, `RobotContainer.java`. Don't explain — just point. | Niwiden's slide deck pattern |

**Pedagogical principle here:** the onboarding lesson succeeds when the student has clicked Deploy on an empty robot and seen "no errors." That's it. We don't teach Java, command-based, or anything conceptual in Stage 0.

### Stage 1A — Java fundamentals (in robot context)

The "Variables / Methods / Classes" trio, but always grounded in a robot scenario the student can run.

| Lesson | What | Why this lesson, why this place |
|---|---|---|
| 01. Methods | `MathUtils.applyDeadband(value, threshold)` — see [path-b-demo/lessons/01-methods/](../examples/path-b-demo/lessons/01-methods/) | Smallest meaningful piece of robot code. Solves a real problem (joystick noise). One concept (method = reusable behavior). |
| 02. Variables & types | Tunable PID constants on a flywheel sim — change values, see effect | Variables are introduced as *things that change runtime behavior*, not as syntax to memorize. Niwiden's principle in action. |
| 03. Conditionals & loops | Beam-break-driven indexer logic, still in `teleopPeriodic` | Deliberately *not* command-based yet. Students write the "messy" form so subsystems feel like relief. |

**What's actively NOT taught in 1A:** Subsystems. Commands. PID theory. Anything OOP-heavy. Static state. Streams or lambdas (beyond what `() -> something` requires for joystick suppliers — that's introduced visually-as-pattern, not theoretically). We resist the urge to teach Java "properly" — students learn the syntax they need, when they need it.

### Stage 1B — Subsystems & PID

This is **the** transition stage. Students go from `teleopPeriodic` jungle to organized subsystems.

| Lesson | What | Pedagogical purpose |
|---|---|---|
| 04. Subsystems as State Machines | Turn the 1A indexer code into a real `IntakeSubsystem extends SubsystemBase` | Reading order: "Look at your code from lesson 03. Painful, right? Today we fix that." |
| 05. PID introduction (Elevator) | Tune `kP`, `kI`, `kD`, `kG` on an elevator sim; see [elevator-pid-poc/](../examples/elevator-pid-poc/) | PID is taught as "make the motor reach a number," not as control theory. Theory deferred. |
| 06. Arm with PID + gravity compensation | `SingleJointedArmSim` + `kG` that varies with angle | Reuses lesson 05 mechanics; introduces `kG = cos(angle) × constant`. The first time a constant becomes a function. |

**Key anti-pattern preempted in 1B:** the "god subsystem" — one class doing too many things. Lesson 04 deliberately splits the indexer logic into two subsystems (intake roller + indexer belt) so students see the boundary.

**Key best practice introduced in 1B:** subsystem fields are `private final`. The student is told this once, and every subsequent lesson's starter code has private fields. By Stage 1C, it's invisible muscle memory.

### Stage 1C — My First Robot (basic command-based)

Now subsystems exist, and the student feels the next pain: *how do these subsystems coordinate?* Enter commands.

| Lesson | What | Why now |
|---|---|---|
| 07. Tank Drive Wiring (the factory pattern's first appearance) | [path-b-demo/lessons/02-tank-drive/](../examples/path-b-demo/lessons/02-tank-drive/) | Introduces the *factory pattern* in its simplest form: `drive.driveArcade(forwardSupplier, rotationSupplier)`. **Always with suppliers.** |
| 08. Joystick bindings & triggers | `controller.a().onTrue(intake.intakeNote())` | First introduction to `Trigger`. Framed as the answer to: "how do I make this happen when I press a button?" Not "let's learn the Trigger API." |
| 09. Command composition | `andThen`, `alongWith`, `race`, `deadline` via small game-piece scoring sequence | Each operator is introduced when it's the only way to express a sentence in plain English: "score, then return home" → `andThen`. |
| 10. Telemetry & SmartDashboard | `Logger.recordOutput("Drive/Speed", speed)`; open AdvantageScope | First exposure to AdvantageScope. Still not the full AdvantageKit IO pattern — just "you can plot things." |

**What we teach about commands in Stage 1C:** factories, factories, factories. Subclasses of `Command` are not introduced in 1C *at all*. Students who reach for a `Command` subclass are gently redirected back to `run`, `runOnce`, `startEnd`, and `FunctionalCommand` factories. From the WPILib docs (<https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html>):

> *"Through the use of lambdas, these commands can cover almost all use cases and teams should rarely need to write custom command classes."*

The first Command subclass our curriculum introduces is in Stage 2A, when motion-profiled commands actually need to hold per-instance state.

**What's actively NOT taught in 1C:** Default commands beyond trivial ones. `toggleOnTrue` (WPILib actively discourages it: *"toggles are not a highly-recommended option for user control, as they require the driver to keep track of the robot state"*). State machines as a separate concept. Multi-subsystem requirements.

### Stage 1D — Composition, autos, and refactoring

The "graduation from rookie" stage. By the end, the student has shipped a complete teleop + simple auto robot.

| Lesson | What | Builds on |
|---|---|---|
| 11. Default commands done right | Drivetrain's default command is "joystick-driven drive"; everything else is on triggers. | 07, 08 |
| 12. Auto routines (basic) | Two-step auto: drive forward, score game piece. Pure factory composition. | 09 |
| 13. Path-following intro | PathPlanner or Choreo for a simple S-curve. | 12 |
| 14. Refactoring & extracting helpers | Take a long `RobotContainer` method, extract it into a `*Bindings` class. | All |
| 15. The capstone | Full teleop bot, ~15 commands, 4 subsystems. | All |

**Key principle introduced in 1D:** *coordinate via triggers in RobotContainer, never via direct subsystem-to-subsystem calls.* This is Oblarg's third principle, made explicit. Students who try to write `intake.scoreThenIntake(elevator)` are guided to instead write the composition in `RobotContainer`:

```java
intake.gamepieceDetected
      .onTrue(elevator.toScoringHeight()
              .andThen(shooter.fire())
              .andThen(elevator.toStow()));
```

The subsystems don't know about each other. The orchestration lives in one place. This makes the codebase reviewable.

### Stage 2A — Mechanism mastery + AdvantageKit introduction

Stage 2A is where AdvantageKit's IO Layer pattern is finally introduced — *after* the student has felt their `Drive` subsystem talking directly to motor controllers and felt the pain of "I can't test this without real hardware."

| Lesson | What | Niwiden parallel |
|---|---|---|
| 16. The IO Layer pattern | Refactor `Drive` into `Drive` + `DriveIO` + `DriveIOSim` + `DriveIOReal` | "Extension: how do we make this testable?" |
| 17. AdvantageScope as a first-class debugger | Reading plots, mechanism2d, 3D field | <https://docs.advantagescope.org> |
| 18. AdvantageKit logging discipline | `Logger.recordOutput` keys, `@AutoLogOutput` | <https://docs.advantagekit.org/data-flow/recording-outputs/> |
| 19. Log replay for debugging | Save a failing match log; replay it; find the bug | The killer feature; only fully appreciable after lessons 16-18 |
| 20. Subsystem composition at scale | Two subsystems coordinating via shared trigger state | Reinforces Oblarg principle 3 |

**Why AdvantageKit is in 2A, not 1A:** the IO pattern requires the student to already understand subsystems, commands, and the difference between "what hardware does" and "what my code asks for." Without those mental models, the IO interface looks like ceremony. With them, it looks like exactly the right separation.

### Stage 2B-2D — Advanced topics

Vision, swerve, advanced control. Each is its own deep dive; the curriculum flow gets less prescriptive here because student goals diverge. Reference resources:

- **Swerve basics:** YAGSL (<https://docs.yagsl.com/>), Choreo (<https://choreo.autos/>), 254/6328 build threads
- **Vision:** PhotonVision (<https://docs.photonvision.org/>), LimelightLib (<https://docs.limelightvision.io/>)
- **Advanced control:** WPILib `SwerveControllerCommand`, profiled PID, system identification (SysId — <https://docs.wpilib.org/en/stable/docs/software/pathplanning/system-identification/index.html>)
- **State machines (formal):** when trigger composition stops being enough, e.g. complex climber sequences. See <https://www.chiefdelphi.com/t/standardized-state-based-robot-control-vendor-dep/415582>

---

## 5. Anti-patterns to actively preempt

Each lesson is designed not just to teach a concept but to *prevent* students from forming a bad habit. The canonical list, with citations and where the curriculum addresses them:

### 5.1 "Everything in `teleopPeriodic`"

The hello-world of FRC programming, intentionally taught in Stage 1A as the *pain* the student will resolve in 1B. From BoVLB's distillation (<https://bovlb.github.io/frc-tips/commands/best-practices.html>): *"Following [the three principles] will reduce dependencies between subsystems and gather all cross-subsystem behaviour in one place."*

**Preempted in:** Lesson 04 (Subsystems as State Machines) — the migration step.

### 5.2 Direct `motor.set()` outside any subsystem

Violates encapsulation; subsystem hardware fields must be `private`. From WPILib subsystems doc (<https://docs.wpilib.org/en/stable/docs/software/commandbased/subsystems.html>): subsystems are *"an abstraction for a collection of robot hardware that operates together as a unit."* Direct access from outside breaks the abstraction.

**Preempted in:** Lesson 04. Reinforced every time `private final` appears in starter code.

### 5.3 Captured joystick values in commands

The Suppliers-not-values rule. Lesson 07 (Tank Drive Wiring) introduces the supplier pattern and includes a deliberate exercise: *"Try removing the `()` from `controller::getLeftY` — what happens?"* Students see the bug live.

### 5.4 Stateful Command subclasses for stateless commands

Cosmetic but corrupting — teaches students that "commands need a class" when they don't. We follow the WPILib docs (<https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html>): *"teams should rarely need to write custom command classes."*

**Preempted in:** Lesson 07 (factories introduced first). Reinforced in lesson 09 (compositions).

### 5.5 `Thread.sleep` / `Timer.delay` / busy-waits

Blocks the scheduler; every other command stops. The right primitive is `Commands.waitSeconds(t).andThen(...)`.

**Preempted in:** Lesson 12 (auto routines). The first time students reach for "wait a second," we hand them the right tool.

### 5.6 Decision logic inside default commands

Default commands should be one-liners. Decision logic belongs in triggers. From BoVLB: complex default commands are a listed anti-pattern.

**Preempted in:** Lesson 11 (Default commands done right).

### 5.7 `toggleOnTrue` for driver controls

WPILib docs (<https://docs.wpilib.org/en/stable/docs/software/commandbased/binding-commands-to-triggers.html>): *"while this functionality is supported, toggles are not a highly-recommended option for user control, as they require the driver to keep track of the robot state."* Prefer two separate buttons or `whileTrue`.

**Preempted in:** Lesson 08 — `toggleOnTrue` is introduced as something the API supports but the curriculum explicitly recommends against for driver inputs.

### 5.8 Magic numbers

A canonical `Constants` class is introduced in lesson 02 (Variables & types) as the answer to: *"the gear ratio appears in three places — how do I change it without bugs?"*

### 5.9 Static mutable state

Singletons-by-static-field. Breaks testability, fights the requirement system. Subsystems are instance fields of `RobotContainer`, period.

**Preempted in:** Lesson 04 (Subsystems as State Machines). Starter code uses `new` and `private final`, never static accessors.

### 5.10 "Test it on the robot"

Counteracted by the entire Stage 2A AdvantageKit arc — log replay makes "I'll see what happens on the robot" the most expensive option, not the cheapest.

### 5.11 Cross-subsystem direct method calls

The `intake.scoreThenIntake(elevator)` pattern. The cure is Oblarg principle 3: coordinate in `RobotContainer` via trigger composition. Preempted in lesson 14 (Refactoring & extracting helpers).

### 5.12 Reading sensors directly outside the IO interface

Stage 2A: once the IO Layer is introduced, sensors only flow through `io.updateInputs(inputs)`. Direct reads break log replay.

---

## 6. Where the community lives

Required reading, by stage:

### Official WPILib (canonical)
- **Command-Based index** — <https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html>
- **What Is Command-Based** — <https://docs.wpilib.org/en/stable/docs/software/commandbased/what-is-command-based.html>
- **Subsystems** — <https://docs.wpilib.org/en/stable/docs/software/commandbased/subsystems.html>
- **Commands** — <https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html>
- **Command Compositions** — <https://docs.wpilib.org/en/stable/docs/software/commandbased/command-compositions.html>
- **Binding Commands to Triggers** — <https://docs.wpilib.org/en/stable/docs/software/commandbased/binding-commands-to-triggers.html>
- **Organizing Command-Based Projects** — <https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html>
- **Java Units Library** — <https://docs.wpilib.org/en/stable/docs/software/basic-programming/java-units.html>
- **WPILib Examples repo** — <https://github.com/wpilibsuite/allwpilib/tree/main/wpilibjExamples>. Particularly `HatchbotTraditional` vs `HatchbotInlined` (side-by-side classes-vs-factories), `ArmBot`, `FrisbeeBot`, the swerve examples.

### Chief Delphi (must-read threads)
- **"Timed vs Command-Based"** (the marathon thread) — <https://www.chiefdelphi.com/t/timed-vs-command-based/418622>. The argument is best summarized as *"command-based vs. hand-rolled approximation of command-based"* — most teams do not have 254's institutional knowledge to roll their own.
- **"Command-based best practices for 2025"** (Oblarg's three principles) — <https://www.chiefdelphi.com/t/command-based-best-practices-for-2025-community-feedback/465602>
- **"2025 Command Framework Usage Patterns"** (annual community poll) — <https://www.chiefdelphi.com/t/2025-command-framework-usage-patterns/500034>
- **"Command based, using it? thoughts?"** — <https://www.chiefdelphi.com/t/command-based-using-it-thoughts/405184>
- **"Command Based Best Practices example"** — <https://www.chiefdelphi.com/t/command-based-best-practices-example/471176>
- **"Command vs Subsystem split"** — <https://www.chiefdelphi.com/t/command-vs-subsystem-split/497126>
- **"Confusion about WPILib Commands"** — <https://www.chiefdelphi.com/t/confusion-about-wpilib-commands/442588>
- **"2025 WPILib Feedback"** — <https://www.chiefdelphi.com/t/2025-wpilib-feedback/500529>
- **6328's annual Build Threads** — <https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2025-build-thread/477314> and prior years. A masterclass in continuous improvement.

### Community digests (the "TL;DR of the consensus")
- **BoVLB's FRC Tips, "Best Practices for Command-Based Programming"** — <https://bovlb.github.io/frc-tips/commands/best-practices.html>. **The single most concentrated distillation of Oblarg's 2024-2025 advice. Required reading for lesson authors.**
- **FRC Zero, Command-Based Programming chapter** — <https://www.frczero.org/programming/command-based-programming/>

### Team training repos (study these structures)
- **Team 8033 Highlanders Training** — <https://github.com/HighlanderRobotics/Highlanders-Training>. The closest existing analogue to FRCProgramming.org. Sequenced as Java → Git → WPILib → command-based → AdvantageKit → controls → swerve.
- **Team 2928 Programmer Training** — <https://2928-frc-programmer-training.readthedocs.io/>. Romi-based, two-pass approach (Romi then RoboRIO).
- **Team 6328 (Mechanical Advantage)** — <https://github.com/Mechanical-Advantage>. AdvantageKit, AdvantageScope, full competition codebases (`RobotCode2025Public`, `RobotCode2026Public`).
- **Team 254** — <https://github.com/Team254>. The deliberate counter-example: command-based foil. Useful for showing students "here's what doing it yourself looks like."
- **Team 2910 Jack in the Bot** — <https://github.com/FRCTeam2910>. Long-running, Einstein-level command-based reference.
- **The aggregated list** — <https://github.com/flamingchickens1540/frc-software-releases>.

### Talks & supplementary
- **Katie Niwiden, "From 0 to Robot: Teaching Programming to Beginners"** — <https://docs.google.com/presentation/d/15O2Xo5cHsYG3hVvQbMSB2SuvU9ED0Y3feaKdCbgaQyM/preview>. The pedagogical spine of this curriculum.
- **FIRST Updates Now** kickoff streams (YouTube) — usually include a programming kickoff with WPILib developers, especially valuable for API change summaries.

---

## 7. Hot takes and open debates

We make explicit choices on contested issues. This section names where we stand and why — and where reasonable teams could disagree.

### 7.1 Java vs. C++ vs. Python

**Our position:** Java. It's the de facto WPILib majority, gets the most thorough docs, and AdvantageKit's strongest support. Python (RobotPy) is real but lags WPILib feature releases by weeks-to-months. C++ has performance advantages only at the very margin.

**Where reasonable people disagree:** if a team has a strong CS curriculum that already teaches Python, RobotPy is a legitimate alternative. We'd accept Python lessons as a parallel track later (Phase 3+), not as a replacement.

### 7.2 Inline factories vs. dedicated Command classes

**Our position:** factories by default, classes only for genuinely stateful commands. Aligned with the post-2024 WPILib official position and Oblarg's principles.

**Where reasonable people disagree:** teams with strong OOP backgrounds sometimes prefer classes for readability of complex multi-phase commands. We acknowledge both; we lead with factories.

### 7.3 AdvantageKit vs. plain WPILib logging

**Our position:** plain WPILib logging through Stage 1; AdvantageKit + IO Layer in Stage 2A. AK's replay capability is genuinely unique, but the IO pattern is conceptual overhead that confuses rookies. WPILib's `DataLogManager` + NetworkTables 4 has narrowed the gap for the simple "I want to see a number" case.

**Honest caveat:** 6328's own Build Threads have flagged loop-timing issues from `.refreshAll()` calls. AK isn't free.

**Where reasonable people disagree:** some teams start *every* subsystem with the IO pattern, even rookie ones. That's defensible if your team has the mentor bandwidth.

### 7.4 `Constants.java` vs. records vs. config files

**Our position:** static `Constants.java` (`public static final` fields), grouped by subsystem in nested classes. This is what every WPILib template ships with and what Niwiden teaches. It's familiar; lessons can extend it without reshuffling existing imports.

**Where reasonable people disagree:** functional-Java folks prefer records grouped by subsystem (cleaner, immutable by construction). Some teams (notably FRC Open Alliance contributors) use YAML/JSON config so PID values can change without rebuilding. We mention these as Stage 2 alternatives; we teach the classic pattern first.

### 7.5 Subsystem `periodic()` vs. Command `execute()` for control loops

**Our position:** control loops live in **commands**, not `periodic()`. Per Oblarg's principles, subsystems stay state-light; `periodic()` is for input updates, telemetry, and odometry only.

**Where reasonable people disagree:** plenty of working code puts PID in `periodic()` (older WPILib material did, too). Both work; we choose the commands-driven version because it composes better with the trigger system.

### 7.6 State machines vs. trigger composition

**Our position:** start with trigger composition (Stage 1C-1D). Introduce formal state machines as a Stage 2 tool when behavior is genuinely modal (climber sequences, multi-stage scoring routines).

**Where reasonable people disagree:** Team 254 makes state machines the *primary* abstraction; many top teams agree. We choose trigger composition first because it's the WPILib-native idiom and requires fewer custom abstractions.

### 7.7 Where `RobotContainer` ends

**Our position:** small teams keep all bindings in `RobotContainer`; in Stage 1D we introduce splitting into per-feature `*Bindings` classes once `RobotContainer` exceeds ~200 lines.

**Where reasonable people disagree:** some teams keep `RobotContainer` slim from the start by always splitting. Either works; we prefer the just-in-time refactor for pedagogical clarity.

---

## 8. Graduation outcomes — what a Stage-2 graduate knows

By the end of Stage 2A, a student can:

**Vocabulary they own**
- Subsystem, Command, Trigger, CommandScheduler, requirement
- Factory, lambda, Supplier (and why it matters for joysticks)
- IO Layer, `@AutoLog`, `Logger.recordOutput`
- PID, feedforward, `kG` (and how it varies by mechanism)
- AdvantageScope: line chart, mechanism2d, NT4 connection

**Patterns they apply by reflex**
- Subsystem fields are `private final`
- Joystick reads happen inside lambdas/Suppliers
- Cross-subsystem coordination lives in `RobotContainer` via Triggers
- Factories before subclasses
- `Commands.waitSeconds(t)` before `Thread.sleep`
- `Constants.java` for magic numbers
- Tests in `src/test/java/` before "let's try it on the robot"

**Anti-patterns they recognize on sight**
- `teleopPeriodic` doing 80 lines of work
- `motor.set()` outside any subsystem
- Captured `controller.getLeftY()` values
- Default commands with `if`/`else` chains
- `toggleOnTrue` for driver buttons
- Cross-subsystem method calls (`intake.scoreThen(elevator)`)
- Static mutable state

**Tools they can use unassisted**
- VS Code + WPILib (deploy, simulate, test from the command palette)
- `./gradlew` + `./tools/frcprog.sh` workflow
- Git: clone, branch, commit, push, PR
- AdvantageScope (open logs, plot signals, share layouts)
- A Driver Station (deploy code, drive a sim or real robot)

A student who can do all of this is ready to contribute to a real season's robot code without supervision, and ready to start the deeper Stage 2B+ topics (swerve, vision, advanced control) at their own pace.

---

## Appendix A — Lesson-to-Concept map

For lesson authors deciding what goes where. Cross-reference with [Path-B-Implementation.md §3.1 and §4.2](Path-B-Implementation.md).

| # | Lesson | Stage | Introduces | Reinforces | Anti-patterns preempted |
|---:|---|:---:|---|---|---|
| 01 | Methods (Functions) | 1A | static methods, parameters, return values | — | Copy-paste duplication |
| 02 | Variables & types | 1A | typed constants, `Constants.java` pattern | methods | Magic numbers |
| 03 | Conditionals in periodic | 1A | `if`/`else`, button reads, sensor reads | variables | — (this lesson IS the anti-pattern, by design) |
| 04 | Subsystems as State Machines | 1B | `SubsystemBase`, `private final`, `periodic()` | classes/OOP | "Everything in teleopPeriodic," god-subsystem, static state |
| 05 | PID Introduction | 1B | `PIDController`, `kP`/`kI`/`kD`, encoder reads | subsystems | "Just bang-bang it" |
| 06 | Arm with gravity FF | 1B | feedforward, `kG`, mechanism-specific compensation | PID | Magic-number gravity compensation |
| 07 | Tank Drive Wiring | 1C | factory pattern, Suppliers, arcade-drive math | subsystems | Captured joystick values, `motor.set()` outside subsystem |
| 08 | Joystick bindings & Triggers | 1C | `Trigger`, `onTrue`, `whileTrue`, button bindings | factories | `toggleOnTrue` for driver controls |
| 09 | Command composition | 1C | `andThen`, `alongWith`, `race`, `deadline`, `until`, `withTimeout` | Triggers | Command subclasses for stateless commands |
| 10 | Telemetry & SmartDashboard | 1C | `Logger.recordOutput`, NT publishing, AdvantageScope basics | All prior | "Test it on the robot," `println` debugging |
| 11 | Default commands done right | 1D | trivial default commands, when to use them | Triggers, factories | Decision logic in default commands |
| 12 | Auto routines (basic) | 1D | `Commands.sequence`, `Commands.waitSeconds`, two-step autos | composition | `Thread.sleep`, busy-waits |
| 13 | Path-following intro | 1D | PathPlanner or Choreo, simple S-curve | autos | Hand-tuned timed autos |
| 14 | Refactoring & extracting helpers | 1D | `*Bindings` classes, `Commands.either`, problem-domain naming | All prior | God `RobotContainer`, cross-subsystem direct calls |
| 15 | Capstone teleop robot | 1D | (integration; no new concept) | All prior | All prior |
| 16 | IO Layer pattern | 2A | `XxxIO` interface, `@AutoLog` inputs, `XxxIOSim`/`XxxIOReal` | subsystems, abstractions | Reading sensors directly |
| 17 | AdvantageScope first-class | 2A | mechanism2d, 3D field, NT4 live connection | telemetry | "Test it on the robot" |
| 18 | AdvantageKit logging discipline | 2A | `Logger.recordOutput`, `@AutoLogOutput`, key naming conventions | IO layer | Stringly-typed log keys |
| 19 | Log replay for debugging | 2A | WPILOG replay, `REPLAY` mode | IO layer + logging | "Reproduce it on the robot" |
| 20 | Subsystem composition at scale | 2A | shared trigger state, multi-subsystem requirements | Triggers, factories | Cross-subsystem direct method calls |

---

## Appendix B — A note on the Niwiden "Mentor Mantra" in lesson copy

Every lesson includes a `hints.md` file with progressive hints (see [Path-B-Implementation.md §15](Path-B-Implementation.md#15-appendix-b-lesson-template-spec-one-page)). The structure of hints follows Niwiden's Mentor Mantra:

| Hint level | Mentor mode | What it gives |
|---|---|---|
| Hint 1 | *Ask* | A conceptual nudge: "Java's `Math.abs(x)` returns the absolute value of `x`. You need to compare …" |
| Hint 2 | *Ask* | A structural nudge: "You're returning a `double`. You have two cases…" |
| Hint 3 | *Show* | Near-answer scaffold with blanks: `if ( ... what condition? ... ) { return 0.0; } return value;` |
| Hint 4 | *Do* | Full working code in a collapsed `<details>` block, marked "Reference answer." |

Lesson authors are explicitly instructed: **don't put hint-4-level material in the README**. The student must choose to reveal it. This is the "lazy mentoring" principle expressed in copy.

---

## Closing

The curriculum is a long bet that the FRC programming community's current pedagogical consensus — pain before abstraction, factories over classes, triggers for state, IO layer for testability — is durable enough to be worth teaching as foundation. If WPILib's recommendations shift dramatically (it has happened — the 2022→2025 factory transition was real), this document is the one that gets updated first. The lessons follow.

Two pieces of internal coupling to remember:

1. **This document and [Infrastructure-Analysis.md](Infrastructure-Analysis.md) are joined at the hip.** The architecture decisions (IO Layer, lesson manifest schema, JUnit-tag rubrics) only make sense given the curriculum sequence here, and vice versa.
2. **[Path-B-Implementation.md](Path-B-Implementation.md) §3.1 + §4.2 are the lesson list this curriculum-flow blesses.** If the implementation roadmap changes lesson order, this doc's `Lesson-to-Concept map` must update with it.

Lesson authors: when in doubt, ask *"what pain does this concept solve, that the student felt in the previous lesson?"* If you can't answer that question in one sentence, the lesson is in the wrong place.
