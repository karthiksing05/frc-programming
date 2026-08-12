# Hints — Lesson 10

## Hint 1 — Where to start

Two TODOs in `Flywheels.java`: one where the publishers are declared, one in
`periodic()`. Both comments contain the lines you need, commented out.

Copy the shape of `targetPublisher` exactly. This is deliberately mechanical — the
learning in this lesson happens in AdvantageScope, not in the typing.

## Hint 2 — The shape of the answer

Declarations, next to `targetPublisher`:

```java
private final DoublePublisher actualPublisher = table.getDoubleTopic("ActualRPM").publish();
private final DoublePublisher errorPublisher  = table.getDoubleTopic("ErrorRPM").publish();
```

In `periodic()`, next to the existing `targetPublisher.set(...)`:

```java
actualPublisher.set(getVelocityRpm());
errorPublisher.set(getErrorRpm());
```

`getVelocityRpm()` and `getErrorRpm()` already exist.

## Hint 3 — Almost there

If check 1 passes but check 3 or 4 fails, the topics exist but the values are wrong
or stale. Two likely causes:

- The `.set(...)` calls are in the constructor rather than in `periodic()`. A value
  published once is a value frozen at startup.
- You published a stored field instead of calling the getter, so it lags a loop
  behind.

Also add both new publishers to `close()`, next to `targetPublisher.close()`.
Publishers hold NetworkTables resources; leaking them in a long-running program
leaks memory.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
private final NetworkTable table = NetworkTableInstance.getDefault().getTable("Flywheels");
private final DoublePublisher targetPublisher = table.getDoubleTopic("TargetRPM").publish();
private final DoublePublisher actualPublisher = table.getDoubleTopic("ActualRPM").publish();
private final DoublePublisher errorPublisher = table.getDoubleTopic("ErrorRPM").publish();
```

```java
@Override
public void periodic() {
    double feedforward = Constants.Flywheels.kV * targetRpm;
    double feedback = Constants.Flywheels.kP * getErrorRpm();

    appliedVolts = targetRpm <= 0.0 ? 0.0 : MathUtil.clamp(feedforward + feedback, 0.0, 12.0);
    motor.setVoltage(appliedVolts);

    targetPublisher.set(targetRpm);
    actualPublisher.set(getVelocityRpm());
    errorPublisher.set(getErrorRpm());
}
```

```java
@Override
public void close() {
    motor.close();
    encoder.close();
    targetPublisher.close();
    actualPublisher.close();
    errorPublisher.close();
}
```

**Worth reading, while you are in this file:** the control loop above your new lines.

```java
double feedforward = Constants.Flywheels.kV * targetRpm;
double feedback = Constants.Flywheels.kP * getErrorRpm();
```

Feedforward carries the load — `kV` is "volts per RPM", so this is the voltage that
*sustains* the target speed once you are there. Feedback only cleans up the
difference. That ordering is why flywheels tune easily and position loops do not,
and it is the same idea as lesson 06's `kG`, applied to speed instead of gravity.

</details>
