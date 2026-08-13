# Lesson 13 — Trajectories

**Stage 1D · 50 min · Needs: 12**

Driving straight gets you out of the zone. It will not get you around anything.

## Do this

**1. `autos/TrajectoryAuto.java`** — fill in the lambda inside `follow`:

```java
Trajectory.State goal = trajectory.sample(timer.get());
ChassisSpeeds speeds = controller.calculate(drive.getPose(), goal);
DifferentialDriveWheelSpeeds wheels = KINEMATICS.toWheelSpeeds(speeds);
drive.setVoltage(
    wheels.leftMetersPerSecond * Constants.Drive.kV_LINEAR,
    wheels.rightMetersPerSecond * Constants.Drive.kV_LINEAR);
```

**2. `RobotContainer.configureAutos()`:**

```java
autoChooser.addOption("S-Curve", TrajectoryAuto.sCurveAuto(drive));
```

## Check it

```bash
./tools/frcprog check 13-trajectory-auto
```

Five checks. Number 4 is the interesting one: it requires the robot to actually go
out and come back, because a follower that drove straight at the destination would
arrive in the right place having never left the line, and check 3 would pass it.

## What the four lines do

| Line | Question it answers |
|---|---|
| `trajectory.sample(timer.get())` | where should I be right now? |
| `controller.calculate(pose, goal)` | what speeds close the gap? |
| `KINEMATICS.toWheelSpeeds(speeds)` | what does that mean per wheel? |
| `× kV_LINEAR` | how many volts is that? |

Step 2 is the feedback that lets the robot recover from wheel slip. Open loop, one
slipped wheel offsets you for the rest of the path.

Step 3 exists because a tank drive cannot strafe, so that constraint has to be
applied explicitly. Track width is the only parameter.

## Honest limit

There is no PID on wheel velocity here. On a real robot you would add one.

In simulation the feedforward model and the physics model are the same model, so
tracking is nearly perfect. That is a limitation of simulation, not a triumph of
your tuning. Worth knowing which of your successes are real.

## Why not Choreo or PathPlanner

Both are excellent and both are downloads. This lesson uses the trajectory tools
inside WPILib so it runs offline. The concepts transfer exactly: Choreo replaces
step 1 with a much better GUI and leaves steps 2, 3 and 4 alone.
`lessons/EXTENSIONS.md` covers the swap.

## See it

```bash
./tools/frcprog sim
```

Select **S-Curve**, click **Autonomous**. In AdvantageScope open a **2D Field** tab
and drop the robot pose on it. First lesson where the picture shows you something a
number cannot.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**Why 20 cm is the tolerance.** A differential drive cannot move sideways. Fixing a
10 cm lateral error means turn, drive, turn back, and a controller trying that at
the end of a path chatters. Swerve does not have this problem, which is one of
several reasons every competitive team runs it.
