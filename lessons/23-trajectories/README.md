# Lesson 23 — Choreo or PathPlanner

**Stage 2B · 60 min · Needs: 22**

!!! warning "Needs one online build"

    This lesson uses a vendor library. See `lessons/EXTENSIONS.md` for the
    four-step install. Everything else in the curriculum runs offline.

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Lesson 13 generated a path in code. That works, and changing it needs a programmer,
a rebuild and a redeploy.

## Do this

1. Install PathPlanner or Choreo as a vendordep
2. Draw a **two-waypoint straight line** first and follow it. A complex path that
   does not work tells you nothing about which part is wrong.
3. Then a three-waypoint path, saved to `src/main/deploy/`
4. Wire it into an auto routine
5. Add an event marker that fires the intake partway along

The concepts are lesson 13's, unchanged: sample the path, run a feedback
controller, convert to module states, command the modules. What changes is that the
path is data rather than code, and it was optimised properly rather than by your
guess at sensible constraints.

## Which one

**Choreo** solves for a time-optimal trajectory given real constraints: motor
torque, mass, moment of inertia. Genuinely fast, and only as good as the numbers
you give it.

**PathPlanner** is more forgiving, has richer event and command integration, and
supports regeneration on the fly. Easier to get moving.

Pick either. Concepts transfer; file formats do not.

## Watch out for

**Path files not in `src/main/deploy/`**, so they never reach the robot.

**Constraints that do not match reality.** Tell Choreo the robot is faster than it
is and it produces a trajectory the robot cannot follow. The follower saturates and
you end up somewhere else.

**Forgetting to reset pose** to the path's start.

## Done

The robot follows a multi-waypoint path in simulation and a mechanism fires at the
right point along it.

```bash
./tools/frcprog next
```
