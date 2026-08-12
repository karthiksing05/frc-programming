# Lesson 07 — Tank drive wiring (the factory pattern)

> **Stage 1C · ~40 minutes · Prerequisite: 06**

Every mechanism so far took discrete orders: intake, go to L4, level the arm. You
set a state and the subsystem handled it.

A drivetrain does not work like that. It needs a fresh number from a human fifty
times a second, for as long as they are holding the stick, and it needs to stop the
instant they let go. There is no state to set. There is only "keep doing this until
something else needs the drivetrain".

That is a `Command`, and today it earns its keep.

## What you'll learn

1. Write a factory method that returns a `Command`.
2. Take `DoubleSupplier` parameters instead of `double` — and understand why that
   distinction is not pedantry.
3. Mix two joystick axes into two wheel speeds (arcade drive).
4. Set a subsystem's default command.

## What you'll do

### 1. `Drive.arcadeDriveCommand`

Open `src/main/java/frc/robot/subsystems/drive/Drive.java`. The method already
exists and returns `run(() -> { ... })`. Fill in the lambda:

```java
double fwd = forward.getAsDouble();
double rot = rotation.getAsDouble();

fwd = MathUtils.applyDeadband(fwd, Constants.Drive.DEADBAND);
rot = MathUtils.applyDeadband(rot, Constants.Drive.DEADBAND);

double left  = MathUtil.clamp(fwd + rot, -1.0, 1.0);
double right = MathUtil.clamp(fwd - rot, -1.0, 1.0);

setVoltage(left * Constants.Drive.MAX_VOLTS, right * Constants.Drive.MAX_VOLTS);
```

Look at the middle two lines. That is lesson 01's method, called with lesson 02's
constant, six lessons later, in code you needed today. That is what "the project
grows" means.

### 2. Wire it up in `RobotContainer`

```java
drive.setDefaultCommand(
    drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> -driver.getRightX()));
```

A **default command** runs whenever nothing else has claimed the subsystem, and is
interrupted automatically the moment something does. For a drivetrain that is
exactly right: drive normally, unless an auto-align routine is running, then go back
to driving.

**The minus signs are not typos.** An Xbox stick reports *negative* when pushed
forward, because the axis is measured screen-style with +Y pointing down. Every FRC
codebase negates it. Now you know why yours does.

### Factories, not Command subclasses

`arcadeDriveCommand` is a *factory*: a method on a subsystem that returns a Command
controlling that subsystem. This is the modern WPILib recommendation, and the
official docs are unambiguous — *"through the use of lambdas, these commands can
cover almost all use cases and teams should rarely need to write custom command
classes."*

Older material teaches writing a `class ArcadeDriveCommand extends Command` with
`initialize`, `execute`, `isFinished`, and `end`. Thirty lines to say what `run(() ->
...)` says in one, in a separate file from the hardware it controls, with an
`addRequirements` call you can forget.

`run(...)` comes from `SubsystemBase`, so the command automatically requires the
subsystem it was built on. That requirement is what stops two commands driving the
same motors at once — the scheduler cancels one of them, deterministically, rather
than letting them fight.

Command subclasses still have a place: genuinely stateful, multi-phase things.
Lesson 27 has the first honest example. Until then, factories.

### The supplier thing

`arcadeDriveCommand` takes `DoubleSupplier`, not `double`. This is the most common
subtle bug in beginner command-based code, and it is worth causing on purpose —
`hints.md` walks you through it.

The short version: a `double` is read **once**, when the command object is
constructed. Commands are constructed during robot startup, when the stick reads
zero. A supplier is re-read every loop, forever.

## Run it

```bash
./tools/frcprog check 07-tank-drive
```

Six checks:

1. Pushing forward drives both sides forward, equally.
2. Rotation splits the sides in opposite directions.
3. A stick inside the deadband is ignored.
4. **The command re-reads the sticks every loop.** (The supplier check.)
5. Full forward plus full turn saturates instead of asking for 24 volts.
6. The robot actually moves in simulation.

## See it

```bash
./tools/frcprog sim
```

Drag **Keyboard 0** onto **Joystick[0]** and click **Teleoperated**. WASD now drives
the robot. Release and it stops immediately — that is the deadband and the supplier
working together.

Connect AdvantageScope and plot the left and right commanded voltages. Drive
straight: they match. Turn: they split. Release: both snap to zero.

## Done?

```bash
./tools/frcprog next
```

You have a drivable robot.

## Why clamp before scaling

```java
double left = MathUtil.clamp(fwd + rot, -1.0, 1.0);
```

Full forward *and* full turn is `1.0 + 1.0 = 2.0`. Scale that by 12 and you have
asked a 12-volt battery for 24 volts. The motor controller will clip it, so nothing
breaks — but now the left side is saturated and the right is not, and the robot
turns differently at full throttle than at half. Clamping first keeps the mixing
predictable across the whole stick range.

There are more sophisticated mixings — normalising both sides by the larger
magnitude preserves the turn ratio better. Clamping is the standard starting point
and is what most teams ship.
