# Lesson 13 — Trajectories

**Stage 1D · 50 min · Needs: 12**

The routine you wrote in lesson 12 drives in straight lines and turns on the spot. That
is enough to leave the starting zone, and it stops being enough the moment anything is
in the way — another robot, a field element, or a scoring position that is off to one
side.

Stringing together more straight lines and turns does not fix it either. Each one starts
and stops, so the robot lurches, takes far longer than the path is worth, and the errors
in each segment accumulate into a final position that is nowhere near where you meant.

What you want instead is a smooth curve the robot follows at speed, described once by
the points it should pass through. That is a trajectory, and generating and following one
is the difference between a two-piece autonomous and a four-piece one.

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

Five checks. Number 4 requires the robot to actually go out and come back, because a
follower that drove straight at the destination would arrive in the right place
having never left the line, and check 3 would happily pass it.

## How it works

### A trajectory is a function of time

Not a shape. Give it 1.3 seconds and it returns where you should be, how fast, and
how sharply you should be turning at that instant.

```
sample(0.0)  ->  pose (0.0, 0.0),  0.0 m/s
sample(0.8)  ->  pose (0.9, 0.35), 1.8 m/s
sample(1.3)  ->  pose (1.6, 0.5),  2.0 m/s
...
```

`TrajectoryGenerator` builds that table once, at startup, from waypoints plus speed
and acceleration limits. Parameterising it takes real milliseconds, which is why it
is generated once and cached rather than inside a 50 Hz loop.

### The four lines, one question each

| Line | Question |
|---|---|
| `trajectory.sample(timer.get())` | where should I be right now? |
| `controller.calculate(pose, goal)` | what speeds close the gap? |
| `KINEMATICS.toWheelSpeeds(speeds)` | what does that mean per wheel? |
| `× kV_LINEAR` | how many volts is that? |

**Step 2 is feedback.** Without it, one slipped wheel offsets you for the rest of
the path and nothing ever notices. The controller compares where you actually are
with where the path says you should be, and bends the speeds to close it.

**Step 3 exists because a tank drive cannot strafe.** Chassis speeds are "forward
m/s and rotate rad/s". Turning that into two wheel speeds requires knowing the track
width, and the constraint has to be applied explicitly because the maths does not
know your robot cannot slide sideways.

**Step 4 is feedforward again**, the same idea as lessons 06 and 10. `kV` is volts
per metre per second. To travel at 2 m/s, ask for `2.0 × kV` volts.

??? question "Predict: what happens if you skip the pose reset at the start?"

    The `runOnce` step before your lambda does:

    ```java
    drive.resetPose(trajectory.getInitialPose());
    timer.restart();
    ```

    Skip it and the controller compares your **current** odometry, which still holds
    wherever you finished last time, against the path's first point at the origin.

    It sees a huge error and spends the first second driving to close it, in the
    wrong direction, before it ever starts following the curve.

    This is also why real robots reset pose from vision at the start of auto. The
    alternative is trusting that somebody placed the robot on the field exactly where
    the path assumed.

### Honest limit of the simulation

There is no PID on wheel velocity here. On a real robot you would add one.

In simulation the feedforward model and the physics model **are the same model**, so
tracking is nearly perfect. That is a property of simulation, not evidence that your
tuning is good. Worth knowing which of your successes are real.

??? info "Why not Choreo or PathPlanner"

    Both are excellent and both are what a competitive team uses. Both are vendor
    libraries that must be downloaded.

    This lesson uses the trajectory tools inside WPILib so it runs on a laptop that
    has not been online since the installer. The concepts transfer exactly: Choreo
    replaces step 1 with a far better GUI and leaves steps 2, 3 and 4 alone.

    `lessons/EXTENSIONS.md` walks through the swap, and lesson 23 is the full version.

## See it

This is the first lesson where the picture shows you something no number can.

Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

1. Select **S-Curve** in the auto chooser
2. Click **Autonomous**
3. In AdvantageScope, add a **2D Field** tab with the **+** button
4. Drag `Drive/Pose` onto it

Watch the robot drive the curve. Then switch to a Line Graph and plot the pose's Y
component: it should rise to about 0.5 m and come back to 0.

If Y stays flat at 0, the robot drove straight to the destination and your sampling
is wrong.

??? example "Experiment: sample the wrong thing"

    1. Change `trajectory.sample(timer.get())` to
       `trajectory.sample(trajectory.getTotalTimeSeconds())`
    2. Run Autonomous and watch the 2D Field

    The robot drives straight at the final pose and arrives roughly there. Check 3
    would pass. Check 4 would not.

    That is why check 4 exists, and it is a good example of a test written against
    the *mechanism* rather than the result.

## Done

The rubric passes, and the robot drives a smooth curve to a pose rather than a
sequence of straight lines and turns.

```bash
./tools/frcprog next
```

**Why 20 cm is the tolerance.** A differential drive cannot move sideways. Fixing a
10 cm lateral error means turn, drive, turn back, and a controller trying that at
the end of a path chatters. Swerve does not have the problem, which is one of
several reasons every competitive team runs it. That is lesson 21.
