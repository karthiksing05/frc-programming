# Lesson 23 — Trajectory following with Choreo or PathPlanner

> **Stage 2B · ~60 minutes · Prerequisite: 22-odometry**
> **Extension lesson — needs one online build. See `lessons/EXTENSIONS.md`.**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

Lesson 13 followed a trajectory generated in code. That works, and it means a path
change requires a programmer, a rebuild, and a redeploy.

Choreo and PathPlanner replace step one with a GUI. Somebody drags waypoints on a
field diagram, the tool solves for a time-optimal trajectory respecting your
drivetrain's real limits, and the robot reads the result from a file. Now the person
designing the auto does not have to be the person who writes Java.

## What you'll learn

1. Install a vendordep — the thing that makes this an extension lesson.
2. Draw a multi-waypoint path in a GUI.
3. Follow it with a holonomic controller.
4. Trigger mechanisms at points along a path.

## Before you start

This needs a download. See `lessons/EXTENSIONS.md` — one online build, then offline
again forever.

## What you'll do

1. Install PathPlanner or Choreo as a vendordep.
2. Draw a three-waypoint path and save it to `src/main/deploy/`.
3. Wire the follower into an auto routine.
4. Add an event marker that fires the intake partway along, and compose that with
   your scoring sequence.

The concepts are lesson 13's, unchanged. Sample the path, run a feedback controller,
convert to module states, command the modules. What changes is that the path is data
rather than code, and it was optimised properly rather than by your guess at
sensible constraints.

### Choreo or PathPlanner?

Both are good and teams argue about it.

**Choreo** solves for a time-optimal trajectory given your real constraints — motor
torque, mass, moment of inertia. The result is genuinely fast, and it is only as good
as the numbers you gave it.

**PathPlanner** is more forgiving, has richer event/command integration, and supports
on-the-fly regeneration. Easier to get moving with.

Pick either. The concepts transfer; the file format does not.

## Done?

The robot follows a multi-waypoint path in simulation, and a mechanism fires at the
right point along it.

```bash
./tools/frcprog next
```
