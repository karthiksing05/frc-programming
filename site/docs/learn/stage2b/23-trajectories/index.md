# Lesson 23 — Trajectory following <small>· Stage 2B</small>

<span class="stage-badge">Stage 2B · Lesson 23</span>

*Auto routines built from `Commands.sequence(driveForward, ...)` get clunky as paths get complex. Designers want to draw a curve in a GUI, not hardcode waypoints in source.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2B |
    | **Time** | ~60 min |
    | **Prereqs** | [Lesson 22 — Odometry & pose estimation](../22-odometry/) |
    | **Edits** | Install Choreo vendordep, add `src/main/deploy/choreo/scurve.traj`, wire follower in `src/main/java/frc/robot/autos/SwerveAuto.java` |
    | **Tests** | `frc.robot.autos.SwerveAutoTest` (`@Tag("lesson-23")`) |
    | **Reference robot** | Kelpie · Choreo integration in [`subsystems/swerve/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/swerve) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Install Choreo (or PathPlanner) as a vendordep and add its files to your project.
2. Generate a multi-waypoint trajectory in Choreo's standalone GUI.
3. Reference a `.traj` file from code and feed it to a follower command.
4. Interleave scoring commands between path segments using composition (back to Lesson 09).

---

## The real-world problem

Lesson 13 introduced path-following with a single S-curve. Lessons 21–22 gave you a real swerve and a pose estimator. Today we put them together: multi-segment paths, designed in a GUI, executed in code, with scoring actions firing at waypoints. This is exactly the workflow Kelpie (and every Open Alliance team since 2023) uses in match.

Two tools dominate: **[Choreo](https://choreo.autos/)** (Team 6328's optimization-based planner — solves for time-optimal paths) and **[PathPlanner](https://pathplanner.dev/)** (community-favorite, more featureful editor). Pick one. We'll use Choreo because it's what Kelpie uses; PathPlanner is a drop-in mental model with different file extensions. The lesson's tests work with either.

---

## What you'll do

Install Choreo. Draw a three-waypoint path in the standalone editor — start in one corner of the field, swing through center, end at a scoring location. Save it to `src/main/deploy/choreo/scurve.traj`. Write `SwerveAuto.scurve()` that loads the trajectory, resets pose to its start, and calls `AutoBuilder.followPath(...)` (Choreo's swerve follower). Then add a scoring command at the midpoint using `andThen`.

---

## Install the Choreo vendordep

Add Choreo's vendordep JSON. From the project root:

```bash
./gradlew vendordep --url=https://lib.choreo.autos/dep/ChoreoLib2026.json
```

This drops a file into `vendordeps/`. Verify your IDE picks up the new classpath — `Choreo` and `AutoBuilder` should resolve in `import` statements. (If you prefer the WPILib VS Code extension, "Manage Vendor Libraries → Install new library (online)" with the same URL does the same thing.)

For PathPlanner:

```bash
./gradlew vendordep --url=https://3015rangerrobotics.github.io/pathplannerlib/PathplannerLib.json
```

Same pattern — choose one and stick with it for the rest of the course.

!!! note "What is a vendordep?"

    A `vendordep` is a JSON descriptor that tells WPILib's build system where to fetch a Maven artifact, its native libraries, and its simulation hooks. Lesson 13 introduced the concept; today's `./gradlew vendordep --url=...` is the same mechanism, just installed from the command line instead of clicked through the GUI.

---

## Draw the trajectory

Launch Choreo (download from [choreo.autos/installation](https://choreo.autos/installation/)). Open your project's `src/main/deploy/choreo/` directory as the Choreo workspace. Set the robot configuration to match yours — track width, wheelbase, max velocity, max acceleration. (Kelpie's numbers in `Constants.java` are a reasonable starting point if you're cargo-culting.)

Drop three waypoints:

1. Start: `(1.5 m, 5.5 m, 0°)` — blue-alliance left of field, near the speaker.
2. Mid: `(4.0 m, 4.0 m, -30°)` — midfield, heading rotated.
3. End: `(6.5 m, 2.5 m, 90°)` — opposite scoring location.

Hit **Generate Trajectory**. Choreo solves a time-optimal path and saves `scurve.traj` to disk. Re-open the file at any time to tweak.

!!! warning "Don't commit binary blobs without thought"

    `.traj` files are JSON — version-control them. But `.chor` (Choreo's project file) gets rewritten on every Generate; expect lots of diff churn. Some teams `.gitignore` `.chor` and only commit `.traj`. Others commit both. Pick a convention with your team.

---

## Wire the follower

Choreo's runtime library gives you `AutoBuilder.followPath(trajectoryName)`. Underneath it pulls the file, hands the swerve subsystem its sample function, and returns a `Command` you compose like any other.

```java linenums="1"
public Command scurve() {
  Trajectory<SwerveSample> traj = Choreo.loadTrajectory("scurve")
      .orElseThrow(() -> new RuntimeException("scurve.traj missing"));

  return Commands.sequence(
      swerve.resetPose(() -> traj.getInitialPose(false).orElse(new Pose2d())),
      AutoBuilder.followPath("scurve"));
}
```

The first command pins the pose estimator to the path's start (Lesson 22 made this possible). The second runs the follower. Both consume the swerve subsystem; the composition handles requirements automatically.

!!! warning "`followPath` consumes the pose estimator"

    The follower reads the *estimated* pose every cycle and computes feedback. If your pose estimator is drifting (Lesson 22's pure-odometry failure mode), the follower will course-correct based on bad data — and pull the robot off the real path. Pose estimation correctness is a hard prerequisite for trajectory following.

For PathPlanner, the equivalent is `AutoBuilder.followPath(PathPlannerPath.fromPathFile("scurve"))`. Different class names, same shape.

---

## Interleave scoring

The reason designers want a GUI is that they want to express *what happens when*. Half of every Reefscape auto is "drive here, score this, drive there, score that." Compose:

```java
public Command scoreAndPath() {
  return Commands.sequence(
      swerve.resetPose(() -> startPose),
      AutoBuilder.followPath("path1-to-reef"),
      superstructure.scoreL4Command().withTimeout(1.5),
      AutoBuilder.followPath("path2-reef-to-feeder"),
      superstructure.intakeCommand().until(beambreak::seesCoral),
      AutoBuilder.followPath("path3-feeder-to-reef"),
      superstructure.scoreL4Command().withTimeout(1.5));
}
```

Each `followPath` is a Command. Each `scoreL4Command()` is a Command. `Commands.sequence` chains them. This is Lesson 09 (command composition) applied at the auto-routine scale — you already know how to do this.

!!! tip "PathPlanner event markers"

    If you chose PathPlanner instead of Choreo, you can attach **event markers** to a path — named events fire when the robot crosses a waypoint, and you bind commands to those events with `NamedCommands.registerCommand("scoreL4", superstructure.scoreL4Command())`. Cleaner than `Commands.sequence` for paths with many fire-and-forget events. Choreo's equivalent is to slice paths at scoring points and use composition; both styles ship in Open Alliance auto code.

---

## Rubric

`SwerveAutoTest` asserts:

1. The path follows correctly in sim — final pose within 10 cm and 5° of the planned end pose.
2. Total time ≤ planned time + 1 s (path actually being followed, not stalled).
3. The intermediate scoring command runs between path segments (`Superstructure/state` transitions to `SCORING` at the expected time).
4. Re-running the auto twice in a row produces the same final pose (replay-safe; no captured state).

Run locally:

```bash
./gradlew test --tests '*SwerveAutoTest' -DincludeTags='lesson-23'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope's **3D Field** tab. Add three things:

- `RealOutputs/Swerve/Pose` — the estimated pose, rendered as your robot model.
- The planned trajectory — Choreo can export `Pose2d[]` samples; publish them once via `Logger.recordOutput("Auto/PathSamples", traj.getPoses())`.
- `RealOutputs/Swerve/ModuleStates` on the **Swerve States** tab — watch the wheels work through the curve.

Set the robot to auto mode in SimGUI. The robot should sweep the S-curve, pause briefly at the midpoint while the scoring command fires, and stop at the end pose.

---

## Going further

- Try the same path with PathPlanner. Note where the APIs differ and where they're identical — most of the conceptual shape transfers.
- Read Kelpie's auto-routine code under [`auto/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/auto). They run multi-piece autos by chaining 8–10 Choreo paths together.
- Add a second auto routine that mirrors yours to the red alliance side. The WPILib `AllianceFlipUtil` helper handles the math.
- Push the trajectory to fail: drop max velocity in Choreo to 0.5 m/s, regenerate, and watch the follower's behavior change.

---

??? tip "Full reveal — only open if you're really stuck"

    Minimum Choreo wiring in `RobotContainer`:

    ```java
    public RobotContainer() {
      // ... existing setup ...
      autoChooser.addOption("scurve", swerveAuto.scurve());
      autoChooser.addOption("score-and-path", swerveAuto.scoreAndPath());
      SmartDashboard.putData("Auto Routine", autoChooser);
    }

    public Command getAutonomousCommand() {
      return autoChooser.getSelected();
    }
    ```

    The trajectory loading itself happens once at `SwerveAuto` construction time, not every match start — `.traj` parsing is cheap but not free.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 22**
    Odometry & pose estimation

    [:octicons-arrow-left-24: Back to lesson 22](../22-odometry/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 24**
    PhotonVision single-tag

    [:octicons-arrow-right-24: Continue to lesson 24](../../stage2c/24-photonvision-singletag/)

</div>
