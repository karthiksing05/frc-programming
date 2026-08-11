# Lesson 13 — Trajectory-following auto

> **Stage 1D · ~50 minutes · Prerequisite: 12**

"Drive forward two metres" gets you out of the starting zone. It will not get you
around a game piece, and it cannot be adjusted by anybody who does not write Java.

A **trajectory** — a path through space with a velocity attached to every point on
it — can do both.

## What you'll learn

1. Generate a trajectory from waypoints and speed limits.
2. Follow one with a feedback controller, closing the loop on pose.
3. Convert chassis speeds into per-wheel speeds with kinematics.
4. Turn wheel speeds into volts with a `kV` feedforward.

## What you'll do

Open `src/main/java/frc/robot/autos/TrajectoryAuto.java` and fill in the `run(...)`
lambda inside `follow`:

```java
Trajectory.State goal = trajectory.sample(timer.get());
ChassisSpeeds speeds = controller.calculate(drive.getPose(), goal);
DifferentialDriveWheelSpeeds wheels = KINEMATICS.toWheelSpeeds(speeds);
drive.setVoltage(
    wheels.leftMetersPerSecond * Constants.Drive.kV_LINEAR,
    wheels.rightMetersPerSecond * Constants.Drive.kV_LINEAR);
```

Then add it to the chooser in `RobotContainer.configureAutos()`:

```java
autoChooser.addOption("S-Curve", TrajectoryAuto.sCurveAuto(drive));
```

### Four lines, four ideas

**`trajectory.sample(timer.get())`** — where the path says we should be *right now*.
A trajectory is a function of time: give it 1.3 seconds and it returns a pose, a
velocity, and a curvature.

**`controller.calculate(currentPose, goal)`** — the feedback. Compares where we
actually are with where we should be and returns chassis speeds that close the gap.
This is what lets the robot recover from wheel slip instead of compounding it. Open
loop, a single slipped wheel offsets you for the rest of the path.

**`KINEMATICS.toWheelSpeeds(speeds)`** — chassis speeds are "forward metres per
second, rotate radians per second". A tank drive cannot do those independently — it
cannot strafe — so the geometry of that constraint has to be applied explicitly.
Track width is the only parameter it needs.

**`× kV_LINEAR`** — `kV` is volts per metre per second. To travel at 2 m/s, ask for
`2.0 × kV` volts. This is the same feedforward idea as lesson 06's `kG` and lesson
10's flywheel `kV`: compute the voltage the physics needs, then let feedback handle
the difference.

### An honest word about simulation

There is no PID on wheel velocity here, and on a real robot you would add one.

In simulation, the feedforward model and the physics model are *the same model*, so
tracking is nearly perfect. That is a limitation of simulation, not a triumph of your
tuning, and it is worth knowing which of your successes are real.

### Why not Choreo or PathPlanner?

Both are excellent, both are what a competitive team uses, and both are vendor
libraries that must be downloaded. This lesson uses the trajectory tools inside
WPILib itself so it runs on a laptop that has not been online since the installer.

The concepts transfer exactly. Choreo replaces step 1 — generating the path — with a
much better GUI, and leaves steps 2, 3 and 4 alone. `lessons/EXTENSIONS.md` walks
through the swap once you have network.

## Run it

```bash
./tools/frcprog check 13-trajectory-auto
```

Five checks:

1. The trajectory generates and ends where it should.
2. The follower commands the drivetrain.
3. The robot ends within 20 cm and 15° of the planned end pose.
4. **The robot followed the curve, not the chord.**
5. The routine ends and leaves the drivetrain stopped.

Check 4 is the one worth understanding. The S-curve bulges half a metre sideways
before returning to centre. A follower that simply drove at the destination would
arrive at the right place having never left the straight line — and check 3 would
happily pass it. So check 4 watches the robot's lateral position throughout and
requires it to actually go out and come back.

## See it

```bash
./tools/frcprog sim
```

Select **S-Curve** from the auto chooser, then click **Autonomous**.

In AdvantageScope, open a **2D Field** tab and drop the robot's pose onto it. Watch
it drive the curve. This is the first lesson where the visualisation shows you
something a number could not.

## Done?

```bash
./tools/frcprog next
```

Two lessons left in Stage 1: a refactor, and a capstone.

## The tolerances, and why they are what they are

Twenty centimetres sounds generous. It is roughly what an honest tank-drive
trajectory follower delivers.

The reason is the same constraint as `toWheelSpeeds`: a differential drive cannot
move sideways. If the robot ends up 10 cm to the left of the path, the only way to
fix that is to turn, drive, and turn back — and a controller trying to do that at
the end of a path chatters. Holonomic drivetrains (swerve, lesson 21) do not have
this problem, which is one of several reasons every competitive team runs swerve.

Real teams also correct their pose from vision (lessons 24–25) precisely because
path-following alone drifts.
