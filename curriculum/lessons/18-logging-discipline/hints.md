# Hints — Lesson 18

## Hint 1 — Where to start

Open AdvantageScope against your current robot and write down every key it shows. That list is your work queue, and it is usually shorter and messier than expected.

## Hint 2 — The shape of the answer

Rename toward `Subsystem/Inputs/Field` and `Subsystem/Outputs/Field`. Then add the pairs you are missing — every setpoint should have its measurement published alongside it.

## Hint 3 — What usually goes wrong

Renaming a key without renaming it in the saved AdvantageScope layout, so the layout silently shows nothing.

Publishing a `Pose2d` as three doubles out of habit, then wondering why it cannot be dropped onto a field view.

Logging inside a lambda that only runs sometimes, producing a signal with gaps that look like the robot stopped.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

A structured publisher, once per subsystem:

```java
private final StructPublisher<Pose2d> posePublisher =
    NetworkTableInstance.getDefault()
        .getTable("Drive")
        .getStructTopic("Pose", Pose2d.struct)
        .publish();

@Override
public void periodic() {
  odometry.update(...);
  posePublisher.set(getPose());
}
```

Drop `Drive/Pose` onto a 2D Field tab in AdvantageScope and the robot appears. That
is the payoff for using the structured type rather than three loose doubles.

</details>
