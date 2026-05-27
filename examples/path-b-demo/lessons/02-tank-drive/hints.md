# Hints — Lesson 02

## Hint 1 — How to call your method from lesson 01

Since `MathUtils.applyDeadband` is a `public static` method, you call
it with the class name:

```java
MathUtils.applyDeadband(forward, DEADBAND)
```

(The `import frc.robot.util.MathUtils;` at the top of `Drive.java` is
what makes it visible.)

## Hint 2 — Arcade mixing

The formula is in the lesson body. Just translate it:

```java
double leftDemand  = forwardClean + rotationClean;
double rightDemand = forwardClean - rotationClean;
```

This *can* push values outside ±1.0 (e.g., forward=1 + rotation=1 →
left=2). That's fine — `io.setVoltage(...)` clamps for you, and a real
robot would clamp at the motor controller. A common refinement is to
divide by `max(1, |left|, |right|)` to preserve the turn ratio at
saturation, but skip that for now.

## Hint 3 — Sending it to the IO layer

`io.setVoltage(leftVolts, rightVolts)` expects volts, not normalized
demand. Battery is nominally 12 V, so:

```java
io.setVoltage(leftDemand * 12.0, rightDemand * 12.0);
```

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
@Override
public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Drive", inputs);

    double forward  = -controller.getLeftY();
    double rotation =  controller.getRightX();

    double forwardClean  = MathUtils.applyDeadband(forward,  DEADBAND);
    double rotationClean = MathUtils.applyDeadband(rotation, DEADBAND);

    double leftDemand  = forwardClean + rotationClean;
    double rightDemand = forwardClean - rotationClean;

    io.setVoltage(leftDemand * 12.0, rightDemand * 12.0);

    Logger.recordOutput("Drive/LeftDemand",  leftDemand);
    Logger.recordOutput("Drive/RightDemand", rightDemand);
}
```

</details>
