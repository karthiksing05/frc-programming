# Hints — Lesson 23

## Hint 1 — Where to start

Read `lessons/EXTENSIONS.md` and do the vendordep install as its own step, verified by a clean build, before touching any path code.

## Hint 2 — The shape of the answer

Draw the simplest possible path first — two waypoints, a straight line — and follow it. A complex path that does not work tells you nothing about which part is wrong.

## Hint 3 — What usually goes wrong

Path files not in `src/main/deploy/`, so they are not deployed and the robot cannot find them at runtime.

Drivetrain constraints in the GUI that do not match reality. Tell Choreo the robot is faster than it is and it produces a trajectory the robot physically cannot follow, then the follower saturates and the robot ends up somewhere else.

Forgetting to reset pose to the path's start.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

[Choreo docs](https://choreo.autos/) and [PathPlanner docs](https://pathplanner.dev/). Both have quickstarts that assume a working swerve drive, which lesson 21 gave you.

</details>
