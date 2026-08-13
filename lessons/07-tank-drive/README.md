# Lesson 07 — Commands and suppliers

**Stage 1C · 40 min · Needs: 06**

A drivetrain needs a fresh number fifty times a second, for as long as somebody
holds the stick.

## Do this

**1. `subsystems/drive/Drive.java`** — fill in the lambda inside
`arcadeDriveCommand`:

```java
double fwd = forward.getAsDouble();
double rot = rotation.getAsDouble();

fwd = MathUtils.applyDeadband(fwd, Constants.Drive.DEADBAND);
rot = MathUtils.applyDeadband(rot, Constants.Drive.DEADBAND);

double left  = MathUtil.clamp(fwd + rot, -1.0, 1.0);
double right = MathUtil.clamp(fwd - rot, -1.0, 1.0);

setVoltage(left * Constants.Drive.MAX_VOLTS, right * Constants.Drive.MAX_VOLTS);
```

**2. `RobotContainer.java`** — give the drivetrain a default command:

```java
drive.setDefaultCommand(
    drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> -driver.getRightX()));
```

The minus signs are not typos. An Xbox stick reads negative when pushed forward.

## Check it

```bash
./tools/frcprog check 07-tank-drive
```

Six checks. Number 4 is the one that matters: it changes the stick value **after**
scheduling the command and requires the output to follow.

## Break it on purpose

Two minutes, and it will save you an afternoon someday. Move the two reads outside
the lambda:

```java
public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation) {
    double fwd = forward.getAsDouble();      // outside. read once, at startup.
    return run(() -> setVoltage(fwd * 12, fwd * 12));
}
```

It compiles with no warning. Run the sim and push the stick: nothing, ever. The
command captured the value during startup, when the stick was at zero, and will
faithfully command zero all match.

Put it back inside.

## Why

**Factories.** `arcadeDriveCommand` is a method on the subsystem that returns a
Command. This is WPILib's own recommendation: *"teams should rarely need to write
custom command classes."* The old way was a 30-line class in a separate file with
an `addRequirements` call you could forget.

`run(...)` comes from `SubsystemBase`, so the command automatically requires the
subsystem. That requirement is what stops two commands driving the same motors.

**Clamp before scaling.** Full forward plus full turn is `2.0`. Scale that by 12
and you asked a 12 V battery for 24 V. Clamping first keeps the mixing predictable
across the whole stick range.

**Three lessons meet here.** Lesson 01's method, called with lesson 02's constant,
inside lesson 07's command.

## See it

```bash
./tools/frcprog sim
```

Drag **Keyboard 0** onto **Joystick[0]**, click **Teleoperated**. WASD drives.
Release and it stops immediately.

## Done

Rubric is green. You have a drivable robot.

```bash
./tools/frcprog next
```
