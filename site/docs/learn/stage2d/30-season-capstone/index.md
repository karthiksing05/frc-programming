# Lesson 30 — Season capstone <small>· Stage 2D</small>

<span class="stage-badge">Stage 2D · Lesson 30</span>

*Twenty-nine lessons asked you to fill in a blank, run a test, observe a graph. The thirtieth asks something different: pick a game, design a robot, ship it. Nobody is going to tell you what to write today.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2D |
    | **Time** | ~180 min (or an off-season weekend) |
    | **Prereqs** | [Lesson 29 — Advanced state machines](../29-state-machines/) — and effectively every prior lesson |
    | **Edits** | Open — student-directed |
    | **Tests** | `frc.robot.SeasonCapstoneTest` (`@Tag("lesson-30")`) — 10+ scenarios you write yourself |
    | **Reference robot** | Both — pick one to clone-and-improve, or design fresh |

---

## What this lesson is — and what it isn't

This isn't a lesson in the usual shape. There's no `applyDeadband` body to fill in, no rubric where the test class is already written. The point of the capstone is to find out whether the twenty-nine prior lessons turned into *judgment* — and the only way to test for judgment is to give you an open problem and watch what you build.

You're going to act as the entire programming subteam for a season. Pick a game. Decide a strategy. Build the robot in sim. Defend your design.

!!! info "What 'shipping' means here"

    A capstone is "done" when your robot drives, scores, climbs, and runs an auto in sim; your tests pass and meaningfully exercise the robot; a mentor (or a graduate of this curriculum) has reviewed your PR and you've responded to feedback; and you can explain — out loud, without notes — why your code is structured the way it is.

---

## The project

Pick one path:

### Path A — Reefscape (Kelpie's game)

Build a pick-and-place robot for the 2025 Reefscape game. Your reference robot is [Kelpie](https://github.com/HighlanderRobotics/Reefscape). You're allowed — encouraged, even — to study Kelpie's code in detail. You're **not** allowed to copy-paste; every file in your repo should be code you wrote, with citations in comments where you took an idea from Kelpie.

Minimum mechanism inventory: swerve drive, elevator, shoulder, wrist, roller end-effector. Vision is optional but expected for the auto routines.

### Path B — Crescendo (Presto's game)

Build a shooter robot for the 2024 Crescendo game. Your reference is [Presto](https://github.com/Mechanical-Advantage/RobotCode2024Public). Same rules: study deeply, cite ideas, write your own code.

Minimum mechanism inventory: swerve drive, intake, indexer rollers, pivoting shooter, two-flywheel shot. Climber is optional but a strong move.

### Path C — Current-season game

Pick the current FRC game (or one from the last three years). You have no reference robot to lean on; the freedom is the point. Use Kelpie and Presto as pattern sources rather than mechanism analogs. This path is harder and more rewarding; it's also where most graduates of this curriculum will land in a real season.

Whichever path you choose, write it down at the top of your repo's `README.md`. Capstones drift; an explicit choice anchors the work.

---

## What we expect to see

Not a step-by-step — a list of properties.

!!! example "The shape of a passing capstone"

    - **Subsystems** follow the four-file IO Layer pattern from lesson 16 (`IO`, `IOSim`, placeholder `IOReal`).
    - **Coordination** lives in `RobotContainer` (or a thin `Superstructure`), via trigger composition and factory binding. No subsystem calls another directly. (Lesson 14 / 20.)
    - **Constants** live in `Constants.java`, grouped by subsystem. PID/FF values came from lesson-28 SysId runs, not `// TODO tune later`. (Lesson 02 / 28.)
    - **Motion-profiled control** is used on at least one mechanism where it earns its keep, with the choice documented. (Lesson 27.)
    - **A state machine** is used on a mechanism if — and only if — it's genuinely modal. (Lesson 29.)
    - **AdvantageKit logging** covers every subsystem with `Logger.processInputs` and `@AutoLogOutput`/`recordOutput`. (Lesson 18.)
    - **Three auto routines** are selectable from a `SendableChooser`; at least one uses a Choreo/PathPlanner trajectory with mid-path scoring events. (Lesson 12 / 13 / 23.)
    - **Vision** integrates with the pose estimator via multi-tag fusion; bad measurements are rejected, not averaged. (Lesson 24 / 25.)
    - **Ten integration scenarios** in `SeasonCapstoneTest` exercise full robot behavior end-to-end.

The reviewer reads your code top-to-bottom. Every shortcut shows; every clean abstraction shows.

---

## The ten test scenarios

Lesson 15 asked for five; this asks for ten. Adapt the details to your game; the categories are non-negotiable.

1. **Full teleop loop.** Drive, intake, score, return to idle.
2. **Autonomous: drive-and-score.** Single piece, single waypoint.
3. **Autonomous: multi-piece.** At least three pieces collected and scored in one auto.
4. **Vision alignment.** From a non-trivial starting pose, align to a target.
5. **Vision localization at distance.** Drive from outside tag range into range and verify the pose snaps.
6. **Dropped piece.** Sensor sees a piece, then loses it mid-sequence. The robot recovers gracefully.
7. **Low battery / brownout.** Simulated voltage sag does not cause catastrophic failure — timeouts trigger, the robot ends safe.
8. **Replay.** Re-run a recorded log in REPLAY mode (lesson 19); confirm determinism by diffing outputs.
9. **Mentor review.** Your PR has at least one round of meaningful feedback addressed. "LGTM" with no comments doesn't count.
10. **Honest design README.** Three design decisions named, the alternative for each, and why you went the way you did. This is the artifact that proves you have judgment.

---

## Working through it

180 minutes of focused work won't finish a capstone; it sequences one. A reasonable cadence:

!!! note "Suggested cadence"

    - **Strategy (30 min).** Write `README.md` first — game choice, scoring strategy, mechanism inventory, auto plan. No code yet.
    - **Scaffolding (60 min).** Subsystems and IO interfaces. No control logic, just the shapes. Confirm AdvantageScope sees every subsystem in an empty sim.
    - **One vertical slice (60 min).** Pick one subsystem (start with drive) and make it work end-to-end: IO, sim, default command, telemetry, one integration test. This is your template; every other subsystem follows the same pattern.
    - **Replicate and coordinate.** Other subsystems, vision wiring, auto routines, the ten integration tests.
    - **Final 15 min.** README polish, PR, request review.

You won't finish in one sitting. That's fine — the schedule is a forcing function for sequencing, not a deadline.

---

## What you're being assessed on

Not "does it run." A graduate of this curriculum can ship a robot in sim — that's the baseline. The capstone is about *how you got there*.

- **Reviewability.** A teammate can read your code without you in the room. Names are descriptive, files are short, patterns are consistent.
- **Honesty.** Your README admits what you didn't finish and what surprised you. Engineering teams hate surprises more than they hate gaps.
- **Restraint.** You used the right tool for each job — factory or subclass, trigger or state machine, profiled controller or vanilla PID — and you can defend each choice in one sentence.
- **Independence.** When you got stuck, you read the WPILib docs and the reference robots before asking. When you asked, your question was specific.

---

## Where to go after this

You finished. A few directions: join your team's real season as a contributor (push back when something feels wrong — you have the vocabulary now); study one reference robot deeply; file an issue or PR on this curriculum naming what was confusing or missing; or teach the next cohort. Niwiden's mantra applies: *"Do not type — the person typing is the person learning."*

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 29**
    Advanced state machines

    [:octicons-arrow-left-24: Back to lesson 29](../29-state-machines/)

-   :material-check-circle:{ .lg .middle } __Course complete__

    ---

    **You finished the curriculum.**
    Thirty lessons. One reference-robot-grade codebase. Ship it.

    [:octicons-home-24: Back to the learning index](../../)

</div>
