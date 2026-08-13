# Lesson 17 — Mechanism views

**Stage 2A · 40 min · Needs: 16**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Plots are good for numbers and bad for shapes.

## Do this

**1. Add a `Mechanism2d` to `ElevatorSubsystem`:**

```java
private final Mechanism2d viz = new Mechanism2d(1.0, 2.0);
private final MechanismRoot2d root = viz.getRoot("base", 0.5, 0.0);
private final MechanismLigament2d carriage =
    root.append(new MechanismLigament2d("carriage", 0.05, 90));
```

Publish it **once**, in the constructor:

```java
SmartDashboard.putData("Elevator/Mechanism", viz);
```

Update it every loop in `periodic()`:

```java
carriage.setLength(getHeightMeters());
```

**2. Do the same for `ShoulderSubsystem`**, using `setAngle(getAngleDegrees())`.

Append the arm's ligament to the carriage's ligament rather than to its own root.
That draws the real kinematic chain, which is what makes collisions visible.

**3. Build a layout** in AdvantageScope and save it:

- Line Graph: elevator height and setpoint
- Line Graph: flywheel target and actual
- Mechanism: elevator and arm
- 2D Field: drivetrain pose

Save to `lessons/17-advantagescope/AdvantageScope.json`.

## Watch out for

**Publishing inside `periodic()`.** That creates a new dashboard entry every loop
and will bring NetworkTables down. Constructor only.

**Units.** `setLength` uses your canvas units. `setAngle` is degrees.

**A canvas too small** for the mechanism, so the ligament draws off the edge and
looks missing.


## See it

Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

1. **File → Connect to Simulator**
2. Add a tab with **+** and choose **Mechanism**
3. Pick your published mechanism from the dropdown
4. In the sim, enable Teleoperated and tap your elevator key

The carriage should rise in the drawing as the real height changes. Move the
shoulder and watch it pivot.

Now open a **2D Field** tab and drag `Drive/Pose` onto it. Drive around with the
keyboard and watch the robot move on the field.

Build the four-tab layout described above and **File → Save Layout**.

## Done

The mechanism view moves with the real subsystem, and you have a saved layout.

Reloading a layout instead of rebuilding one is the difference between debugging in
thirty seconds and five minutes. Between matches, five minutes is all you have.

```bash
./tools/frcprog next
```
