# Lesson 13 — Path-following intro <small>· Stage 1D</small>

<span class="stage-badge">Stage 1D · Lesson 13</span>

*"Drive forward two meters" is fine until the auto needs to curve around a game piece — at which point time-based steering falls off a cliff.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1D |
    | **Time** | ~50 min |
    | **Prereqs** | [Lesson 12 — Auto routines (basic)](../12-auto-basic/) |
    | **Edits** | `src/main/java/frc/robot/autos/PathAuto.java`, `vendordeps/`, `src/main/deploy/choreo/` |
    | **Tests** | `frc.robot.autos.PathAutoTest` (`@Tag("lesson-13")`) |
    | **Reference robot** | Kelpie · [`swerve/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/swerve) (Choreo integration) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Install a vendordep — either Choreo or PathPlanner — into a WPILib project.
2. Generate a trajectory file in the path-planning GUI and save it to `src/main/deploy/`.
3. Wire `Choreo.followPath(...)` (or `AutoBuilder.followPath(...)`) into an autonomous routine.
4. Decide between Choreo and PathPlanner for your team's needs.

---

## The real-world problem

Lesson 12's auto worked because it drove in a straight line. The moment your strategy involves "drive to the corner of the field, around the obstacle, and to the scoring zone," timed `drive.driveDistanceCommand(2.0)` collapses. You'd need separate turn-then-drive-then-turn segments, each tuned by hand, each re-tuned every time the carpet friction changes. Teams that work this way spend their off-season tuning autos and their season explaining why the auto didn't run.

The community-standard answer is **path-following**: define the *trajectory* — a continuous curve of position and velocity over time — in a graphical editor, save it as a file, and have the drivetrain follow it using closed-loop control. The drivetrain reads its odometry pose, compares it to the trajectory's pose-at-time-`t`, and computes corrective wheel speeds. Carpet friction stops mattering because the controller closes the loop on actual measured pose, not elapsed time.

Two vendor libraries dominate. **Choreo** (by SleipnirGroup) emphasizes precomputed time-optimal trajectories — generate offline, follow at runtime. **PathPlanner** (by mjansen4857) is more event-rich, with an "event marker" system for triggering commands mid-path. Either is a good choice; pick one. This lesson uses Choreo because Kelpie uses Choreo, but the workflow is structurally identical for both.

---

## What you'll do

Install the Choreo vendordep, draw an S-curve in Choreo's GUI, save it to `src/main/deploy/choreo/`, and write a `PathAuto.scurve()` factory that follows it.

### Step 1 — Install the vendordep

In VS Code, run the **WPILib: Manage Vendor Libraries** command, choose **Install new libraries (online)**, and paste:

```
https://sleipnirgroup.github.io/ChoreoLib/dep/ChoreoLib.json
```

Gradle will pull `choreolib` on the next build. The vendordep JSON drops into your project's `vendordeps/` directory — commit it.

!!! note "PathPlanner equivalent"

    For PathPlanner, the URL is `https://3015rangerrobotics.github.io/pathplannerlib/PathplannerLib.json` and the runtime class is `com.pathplanner.lib.auto.AutoBuilder`. The rest of this lesson maps over with renames.

### Step 2 — Draw the path

Open the **Choreo** desktop app (separate download from the vendordep). Create a new project pointed at your `src/main/deploy/choreo/` folder. Set the robot's mass, MOI, wheelbase, and module wheel parameters — Choreo uses these to compute a *time-optimal* trajectory.

Click **New Trajectory**. Drag two waypoints onto the field, then a third in the middle. Adjust the heading arrows to bend it into an S-shape. Hit **Generate** and Choreo solves for the fastest physically-realizable path between the waypoints. Name it `scurve.traj` and save.

!!! tip "What `time-optimal` means here"

    Choreo's solver finds a trajectory that respects your drivetrain's velocity and acceleration limits (the numbers you set in the project settings) and minimizes total time. If the curve looks aggressive, your limits are aggressive. Numbers that are too aggressive cause the follower to fall behind in the real world — start conservative, then push.

### Step 3 — Wire it into an auto

Create `frc/robot/autos/PathAuto.java`:

```java
public final class PathAuto {
  public static Command scurve(Drive drive) {
    Trajectory<SwerveSample> traj = Choreo.loadTrajectory("scurve").orElseThrow();
    return Commands.sequence(
        Commands.runOnce(() -> drive.resetPose(traj.getInitialPose())),
        Choreo.followTrajectory(
            traj,
            drive::getPose,
            drive::followSample,
            () -> false,           // mirror for red alliance? handle later
            drive)
    );
  }
}
```

The arguments to `followTrajectory` say: *here's the trajectory; here's how to read my current pose; here's how to consume the next sample (commanded chassis speeds); here's whether to mirror for alliance; here's the subsystem I require.* Choreo's helpers do the rest.

!!! warning "Reset pose at start, not during"

    `drive.resetPose(traj.getInitialPose())` is essential at the start of an auto — the trajectory is described in **field coordinates**, but the robot wakes up thinking its odometry origin is wherever it last was. Without the reset, you'll follow a beautifully-shaped path that starts from the wrong place and ends in the wall. We touch alliance mirroring and tag-based pose seeding in Lesson 22; for now, hand-set the pose.

---

## Choreo vs. PathPlanner — picking one

| | Choreo | PathPlanner |
|---|---|---|
| Trajectory generation | Offline, time-optimal solver | Online, polynomial interpolation |
| Event triggers along the path | Limited (use commands between paths) | First-class event markers |
| Robot model | Required (mass, MOI, modules) | Optional |
| GUI | Standalone desktop app | Standalone desktop app |
| Used by | Kelpie (8033), 2910, increasing share | Long-established majority |

Either choice survives a full season. Pick by team preference; the file structure and API surface are similar enough that switching mid-season is annoying but tractable. **Don't ship both** — the resulting vendordep conflicts are not a fun debugging session.

---

## Rubric

`PathAuthTest` asserts:

1. The trajectory file `src/main/deploy/choreo/scurve.traj` exists and loads.
2. Robot follows the path in sim — final pose within 10 cm of the planned end pose.
3. Final heading within 5° of planned end heading.
4. Total elapsed time ≤ planned time + 1 s.
5. `PathAuto.scurve(drive)` returns a non-null `Command` whose requirements include `drive`.

Run locally:

```bash
./gradlew test --tests '*PathAutoTest' -DincludeTags='lesson-13'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope's **3D field** tab, add the trajectory as an overlay (Choreo publishes one to `NT/Choreo/scurve` automatically if you call `Logger.recordOutput("Path", traj.getPoses())`). Then plot `Drive/Pose` as the live robot pose. Switch to auto — watch the robot trace the curve. The two paths should overlap to within a few centimeters.

If you have a tablet handy, AdvantageScope's web view shows the same trace and is a good demo for a mentor.

---

## Going further

- Draw a second path that ends pointed in a direction *other* than the start. Run them back-to-back as a single auto.
- Mid-path, what happens if you push the simulated robot with the **Force** panel in maple-sim? Choreo's follower will fight to recover. How big a push can it absorb?
- Read Kelpie's [Choreo loader code](https://github.com/HighlanderRobotics/Reefscape) — how do they handle alliance mirroring? You'll need this in Lesson 22 anyway.
- Look at PathPlanner's *event markers*. If you swap libraries later, this is the feature you'll miss most.

---

??? tip "Full reveal — only open if you're really stuck"

    Common failure: **the path loads but the robot drives wildly off course.** Almost always the cause is one of:

    1. Wheelbase / track-width / module wheel radius in the Choreo project don't match your `Constants.Drive` values. Recheck both.
    2. You forgot the `resetPose` step. Without it, your initial pose is wrong by however many meters you've driven since boot.
    3. Your drivetrain feedforward is off and the follower can't track high-speed segments. Lower Choreo's velocity ceiling until the robot keeps up.

    A correct minimal `PathAuto.scurve`:

    ```java
    public static Command scurve(Drive drive) {
      var traj = Choreo.loadTrajectory("scurve").orElseThrow();
      return Commands.sequence(
          Commands.runOnce(
              () -> drive.resetPose(traj.getInitialPose().orElseThrow()), drive),
          Choreo.followTrajectory(
              traj,
              drive::getPose,
              drive::followSample,
              () -> false,
              drive)
      ).withName("PathAuto/scurve");
    }
    ```

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 12**
    Auto routines (basic)

    [:octicons-arrow-left-24: Back to lesson 12](../12-auto-basic/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 14**
    Refactoring with *Bindings classes

    [:octicons-arrow-right-24: Continue to lesson 14](../14-bindings-refactor/)

</div>
