# Lesson 17 — AdvantageScope as a first-class debugger

> **Stage 2A · ~40 minutes · Prerequisite: 16**


!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.


Plots are excellent for numbers. They are poor for geometry. "Did my elevator and
arm actually reach a sensible pose together?" is a question about a shape, and the
honest answer is a picture.

## What you'll learn

1. Publish a `Mechanism2d` — a stick-figure of an articulated mechanism.
2. Use the 2D field view for drivetrain pose.
3. Save and reload an AdvantageScope layout so you are not rebuilding it every time.

## What you'll do

Add a `Mechanism2d` to `ElevatorSubsystem` and `ShoulderSubsystem`, publish it, and
build a layout that shows the robot doing something.

A `Mechanism2d` is a little 2D canvas with jointed line segments (`MechanismLigament2d`).
You set each segment's length and angle every loop from real sensor values, and
AdvantageScope draws it.

```java
private final Mechanism2d viz = new Mechanism2d(1.0, 2.0);
private final MechanismRoot2d root = viz.getRoot("elevator", 0.5, 0.0);
private final MechanismLigament2d carriage =
    root.append(new MechanismLigament2d("carriage", 0.05, 90));

public ElevatorSubsystem() {
    // ...
    SmartDashboard.putData("Elevator/Mechanism", viz);
}

@Override
public void periodic() {
    // ...existing control code...
    carriage.setLength(getHeightMeters());
}
```

Do the same for the shoulder, using `setAngle(getAngleDegrees())`.

## Run it

There is no rubric. Run it and look:

```bash
./tools/frcprog sim
./tools/frcprog scope
```

In AdvantageScope, add a **Mechanism** tab and select your published mechanism. Tap
the elevator button and watch the carriage rise. Move the shoulder and watch it
pivot.

## See it

Build a layout you would use in a pit:

- a **Line Graph** with elevator height and setpoint
- a **Line Graph** with flywheel target and actual
- a **Mechanism** tab with the elevator and arm
- a **2D Field** tab with the drivetrain pose

Save it (**File → Save Layout**) into `lessons/17-advantagescope/AdvantageScope.json`.
Reloading a saved layout instead of rebuilding one is the difference between
debugging in thirty seconds and debugging in five minutes, and five minutes is
sometimes all the time you have between matches.

## Done?

You have a mechanism view that moves with the real subsystem, and a saved layout.

```bash
./tools/frcprog next
```

## Why bother

Because some bugs are invisible as numbers and obvious as pictures. An arm that
reaches its setpoint by swinging the wrong way round; an elevator that is fine at
every setpoint but collides with the arm in between; a pose estimate that jumps a
metre when a camera sees a tag. All of those read as "slightly odd numbers" and as
"oh, *that*" on a picture.
