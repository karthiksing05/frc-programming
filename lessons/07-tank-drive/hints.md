# Hints — Lesson 07

## Hint 1 — Where to start

Everything goes **inside** the `run(() -> { ... })` lambda. If you find yourself
writing code between the method's opening brace and the `return run(`, stop — that
is the bug this lesson exists to prevent.

Four steps, in order: read the axes, deadband them, mix them, send volts.

## Hint 2 — The shape of the answer

```java
return run(
    () -> {
        double fwd = /* read the forward supplier */;
        double rot = /* read the rotation supplier */;

        fwd = MathUtils.applyDeadband(fwd, Constants.Drive.DEADBAND);
        rot = /* same for rot */;

        double left  = MathUtil.clamp(fwd + rot, -1.0, 1.0);
        double right = MathUtil.clamp(fwd - rot, -1.0, 1.0);

        setVoltage(/* scale each to volts */);
    });
```

A `DoubleSupplier` is read with `.getAsDouble()`.

Arcade mixing: left is `fwd + rot`, right is `fwd - rot`. Turning right means the
left wheels go faster and the right wheels slower, which is what those two lines
say.

## Hint 3 — Break it on purpose

Do this. It takes two minutes and it will save you an afternoon some day.

Move the two reads *outside* the lambda:

```java
public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation) {
    double fwd = forward.getAsDouble();      // ✗ outside — read once, at startup
    double rot = rotation.getAsDouble();     // ✗
    return run(() -> {
        double left  = MathUtil.clamp(fwd + rot, -1.0, 1.0);
        double right = MathUtil.clamp(fwd - rot, -1.0, 1.0);
        setVoltage(left * 12.0, right * 12.0);
    });
}
```

It compiles without a single warning. Run `frcprog sim` and push the stick: nothing
happens, ever. The command captured the stick's value at the instant it was built —
during startup, at zero — and will faithfully command zero for the rest of the
match.

Now imagine meeting that at a competition, with a driver insisting the robot is
broken. Rubric check 4 exists so you meet it here instead.

Put the reads back inside.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

**Drive.arcadeDriveCommand**

```java
public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation) {
    return run(
        () -> {
            // Read the sticks HERE, inside the lambda, every loop. This is the
            // whole reason the parameters are suppliers.
            double fwd = forward.getAsDouble();
            double rot = rotation.getAsDouble();

            // Lesson 01's method, called with lesson 02's constant.
            fwd = MathUtils.applyDeadband(fwd, Constants.Drive.DEADBAND);
            rot = MathUtils.applyDeadband(rot, Constants.Drive.DEADBAND);

            // Arcade mixing. Clamp before scaling so that full throttle plus full
            // turn saturates at one side stopped rather than asking for 24 volts.
            double left = MathUtil.clamp(fwd + rot, -1.0, 1.0);
            double right = MathUtil.clamp(fwd - rot, -1.0, 1.0);

            setVoltage(left * Constants.Drive.MAX_VOLTS, right * Constants.Drive.MAX_VOLTS);
        });
}
```

**RobotContainer.configureDefaultCommands**

```java
drive.setDefaultCommand(
    drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> -driver.getRightX()));
```

</details>
