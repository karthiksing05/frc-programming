# Trajectories & path-following

!!! warning "Coming in Phase 2"

    This page is a stub. The full handbook entry is authored in Phase 2 — see [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Until then, lessons 13 and 23 are the canonical reference.

## What this page will cover

The two halves of "drive somewhere autonomously" — generating a path and following it:

- **Generating:** [PathPlanner](https://pathplanner.dev/) vs [Choreo](https://choreo.autos/) — when to use each
- **Following:** holonomic (swerve) vs differential (tank/west-coast) controllers
- WPILib's `Trajectory`, `TrajectoryGenerator`, `RamseteController`, `HolonomicDriveController`
- Odometry as the prereq — you can't follow what you can't localize
- Stitching paths into multi-leg autos with command composition
- When to hand-author vs generate (almost always: generate)

## One useful nugget right now

**Path-following depends on odometry, which depends on either wheel encoders + gyro (good) or wheel encoders + gyro + vision pose fusion (better).** A perfect path with bad odometry produces a robot driving into a wall confidently. From [Curriculum-Flow.md DAG §2](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Curriculum-Flow.md): trajectories sit downstream of PID, motion profiling, and odometry — all of which must work first.

```java
Command auto = Commands.sequence(
    drive.followPath("DriveToReefAB"),
    superstructure.scoreL4(),
    drive.followPath("DriveBackToStation")
);
```

## Lessons that cover this material today

- [Lesson 13 — Path-following intro](../learn/stage1d/13-trajectory-auto/index.md) — first S-curve
- [Lesson 22 — Odometry & pose estimation](../learn/stage2b/22-odometry/index.md)
- [Lesson 23 — Trajectory following](../learn/stage2b/23-trajectories/index.md)
- [Lesson 27 — Motion profiling](../learn/stage2d/27-motion-profiling/index.md)

## See also

- [PathPlanner docs](https://pathplanner.dev/home.html)
- [Choreo docs](https://choreo.autos/)
- Kelpie's auto routines — see the [Reefscape tour](../robots/kelpie-reefscape-tour.md)
