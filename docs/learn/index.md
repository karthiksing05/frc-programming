# The lessons

Thirty-four lessons across nine stages. They are sequential and the order is load-bearing —
each one solves a problem the previous one made you feel.

Start at Stage 0 even if you have programmed before. It is short, and it catches the
things that bite later.

[Set up first :material-arrow-right:](../setup/index.md){ .md-button }
[Start Lesson 0A :material-rocket-launch:](stage0/0a-first-run-install/index.md){ .md-button .md-button--primary }

---

## Two kinds of lesson

**Graded (01–16).** Starter code with a `TODO`, a JUnit rubric that grades you, four
hints, and a reference answer. Run `frcprog check <lesson>` and it tells you exactly
what is wrong, in words.

**Guided (17–30, and Stage 0).** A clear goal, working code to model yourself on, and
the simulator as your check. No automatic grader.

That shift is deliberate. Real programming does not come with a rubric, and at some
point somebody has to stop writing exercises for you.

Five lessons are marked ⬇ — they teach a vendor library and need one online build.
[Details](../setup/offline.md).

---

## Stage 0 — Onboarding

Install once. Meet the two robots you will be referencing forever. Touch version
control so it does not bite later.

- [0A · First-run install](stage0/0a-first-run-install/index.md) — 60 min
- [0B · Meet Presto](stage0/0b-meet-presto/index.md) — 15 min
- [0C · Meet Kelpie](stage0/0c-meet-kelpie/index.md) — 15 min
- [0D · Project tour](stage0/0d-project-tour/index.md) — 30 min

## Stage 1A — Java, in a robot

A method, some named constants, and a deliberately messy `teleopPeriodic` — written
that way on purpose, so that Stage 1B feels like relief.

- [01 · Methods](stage1a/01-methods/index.md) — 25 min
- [02 · Variables & types](stage1a/02-variables-and-types/index.md) — 30 min
- [03 · Conditionals in `teleopPeriodic`](stage1a/03-conditionals-in-teleop/index.md) — 35 min

## Stage 1B — Subsystems & control

Lift the mess into a real subsystem. Make a motor reach a number. Compensate for
gravity that changes with angle.

- [04 · Subsystems as state machines](stage1b/04-subsystems-state-machines/index.md) — 45 min
- [05 · PID introduction](stage1b/05-pid-elevator/index.md) — 50 min
- [06 · Arm with gravity feedforward](stage1b/06-arm-gravity-ff/index.md) — 45 min

## Stage 1C — Command-based

Factories, triggers, composition, telemetry. By the end you have a drivable robot
you can see inside.

- [07 · Tank drive wiring](stage1c/07-tank-drive/index.md) — 40 min
- [08 · Bindings & Triggers](stage1c/08-triggers-bindings/index.md) — 35 min
- [09 · Command composition](stage1c/09-command-composition/index.md) — 40 min
- [10 · Telemetry & AdvantageScope](stage1c/10-telemetry/index.md) — 30 min

## Stage 1D — Composition, autos, refactoring

Idle behaviour. Two autonomous routines. A refactor you can prove was safe. A
capstone.

- [11 · Default commands](stage1d/11-default-commands/index.md) — 35 min
- [12 · Auto routines](stage1d/12-auto-basic/index.md) — 40 min
- [13 · Trajectory auto](stage1d/13-trajectory-auto/index.md) — 50 min
- [14 · Bindings refactor](stage1d/14-bindings-refactor/index.md) — 45 min
- [15 · Capstone teleop robot](stage1d/15-capstone-teleop/index.md) — 90 min

## Stage 2A — Structure & logging

The pattern every serious team uses, and the tooling that makes yesterday's bug
reproducible.

- [16 · The IO Layer pattern](stage2a/16-io-layer/index.md) — 60 min
- [17 · AdvantageScope first-class](stage2a/17-advantagescope/index.md) — 40 min
- [18 · Logging discipline](stage2a/18-logging-discipline/index.md) — 35 min
- [19 · Log replay](stage2a/19-log-replay/index.md) — 50 min ⬇
- [20 · Superstructure](stage2a/20-superstructure/index.md) — 50 min

## Stage 2B — Swerve

The drivetrain every competitive team runs, and knowing where you are.

- [21 · Swerve intro](stage2b/21-swerve-intro/index.md) — 75 min
- [22 · Odometry & pose estimation](stage2b/22-odometry/index.md) — 55 min
- [23 · Trajectory following](stage2b/23-trajectories/index.md) — 60 min ⬇

## Stage 2C — Vision

AprilTags, sensor fusion, and physics that can push you around.

- [24 · PhotonVision single-tag](stage2c/24-photonvision-singletag/index.md) — 45 min ⬇
- [25 · Multi-tag pose estimation](stage2c/25-multitag/index.md) — 60 min ⬇
- [26 · maple-sim](stage2c/26-maplesim/index.md) — 55 min ⬇

## Stage 2D — Advanced control

- [27 · Motion profiling](stage2d/27-motion-profiling/index.md) — 55 min
- [28 · System identification](stage2d/28-sysid/index.md) — 60 min
- [29 · Advanced state machines](stage2d/29-state-machines/index.md) — 50 min
- [30 · Season capstone](stage2d/30-season-capstone/index.md) — 180 min

---

## If you get stuck

1. **Read the failure message.** They are written as advice, not as stack traces.
2. **Open the hints.** Four of them, escalating; the answer is only in the last.
3. **Plot it.** `frcprog sim` and `frcprog scope`. Most control problems are obvious
   as a picture and invisible as a number.
4. **Ask a person.** Peer help beats page help, every time.
