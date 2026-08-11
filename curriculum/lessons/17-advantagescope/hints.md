# Hints — Lesson 17

## Hint 1 — Where to start

Start with the elevator, because it has one moving part. `Mechanism2d` lives in `edu.wpi.first.wpilibj.smartdashboard`, along with `MechanismRoot2d` and `MechanismLigament2d`.

## Hint 2 — The shape of the answer

Construct the `Mechanism2d` and its ligaments as fields, publish it once with `SmartDashboard.putData(...)` in the constructor, and update the ligament every loop in `periodic()`. Publishing once and updating in place is the whole pattern.

## Hint 3 — What usually goes wrong

Publishing inside `periodic()` rather than the constructor — this creates a new dashboard entry every loop and will bring NetworkTables to its knees.

Using metres where the API wants degrees, or vice versa. `setLength` is in the units of your `Mechanism2d` canvas; `setAngle` is degrees.

A canvas too small for the mechanism, so the ligament draws off the edge and appears to be missing.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

```java
private final Mechanism2d viz = new Mechanism2d(1.0, 2.0);
private final MechanismRoot2d root = viz.getRoot("base", 0.5, 0.0);
private final MechanismLigament2d carriage =
    root.append(new MechanismLigament2d("carriage", 0.05, 90));

public ElevatorSubsystem() {
  encoder.setDistancePerPulse(Constants.Elevator.METERS_PER_PULSE);
  // ...
  SmartDashboard.putData("Elevator/Mechanism", viz);
}

@Override
public void periodic() {
  // ...control code...
  carriage.setLength(getHeightMeters());
}
```

For the shoulder, append a ligament to a root and call
`arm.setAngle(getAngleDegrees())` each loop. Appending the arm's ligament to the
elevator carriage's ligament — rather than to a separate root — draws the real
kinematic chain, which is what makes collisions visible.

</details>
