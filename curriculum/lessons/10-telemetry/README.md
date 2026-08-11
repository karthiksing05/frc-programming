# Lesson 10 — Telemetry & AdvantageScope

> **Stage 1C · ~30 minutes · Prerequisite: 09**

"Why isn't the shooter getting up to speed?"

You can stare at the code for an hour. Or you can plot three numbers and know in
five seconds.

## What you'll learn

1. Publish a value to NetworkTables so anything can read it.
2. Connect AdvantageScope to a running simulation.
3. Read a step response and name what you are seeing.
4. Pick a key-naming convention and stick to it.

## What you'll do

Open `src/main/java/frc/robot/subsystems/flywheels/Flywheels.java`.

One publisher already exists:

```java
private final NetworkTable table = NetworkTableInstance.getDefault().getTable("Flywheels");
private final DoublePublisher targetPublisher = table.getDoubleTopic("TargetRPM").publish();
```

Add two more, `actualPublisher` ("ActualRPM") and `errorPublisher` ("ErrorRPM"), and
set all three every loop in `periodic()`:

```java
targetPublisher.set(targetRpm);
actualPublisher.set(getVelocityRpm());
errorPublisher.set(getErrorRpm());
```

Also close the two new publishers in `close()`.

### What NetworkTables is

A key-value store shared between the robot and everything watching it — the driver
station, dashboards, AdvantageScope, your laptop. The robot publishes; anyone
subscribes. Table name plus topic name gives the path: `/Flywheels/TargetRPM`.

### Why a publisher instead of `SmartDashboard.putNumber`

`SmartDashboard.putNumber("Flywheels/TargetRPM", x)` also works. It also looks up
that string in a hash map every single time you call it, fifty times a second, for
every value you publish.

A `DoublePublisher` is a handle to one topic, resolved once at construction. Set it
and the value goes straight out. On a robot publishing a couple of hundred signals —
which is normal — the difference is measurable in your loop time.

### The naming convention

`SubsystemName/FieldName`, in PascalCase.

It seems fussy until you have two hundred signals. AdvantageScope builds a tree from
the slashes, so a consistent convention gives you a browsable list grouped by
mechanism. An inconsistent one — `drive_speed` here, `Drive/Speed` there,
`elevatorHeight` somewhere else — gives you a flat alphabetical soup at exactly the
moment you are trying to debug something in a pit with four minutes on the clock.

## Run it

```bash
./tools/frcprog check 10-telemetry
```

Four checks:

1. All three topics exist on NetworkTables.
2. `TargetRPM` reports what was commanded.
3. `ActualRPM` tracks the real wheel speed during spin-up.
4. `ErrorRPM` is target minus actual, checked repeatedly through the run.

## See it

This is the lesson where the tooling starts paying you back. Do it properly.

```bash
./tools/frcprog sim
```

Then, in a second terminal:

```bash
./tools/frcprog scope
```

In AdvantageScope: **File → Connect to Simulator** (or connect to `localhost`). The
left sidebar fills with a tree of everything the robot is publishing. Find
`NT/Flywheels`, and drag `TargetRPM`, `ActualRPM` and `ErrorRPM` onto the same line
chart.

Hold A in the simulator window.

You are now looking at a **step response**, and it has a vocabulary:

- **Rise time** — how long actual takes to get near target.
- **Settling time** — how long until it stays there.
- **Overshoot** — how far past it goes, if it does.
- **Steady-state error** — the gap that remains once it has settled.

`ErrorRPM` is the vertical distance between the other two traces, drawn for you. When
it reaches zero and stays there, the shooter is at speed.

**Look at what happens when the roller fires.** Actual dips — the game piece takes
energy out of the wheels — and then recovers. On a real robot, how fast that recovery
happens determines how quickly you can take a second shot, and it is one of the most
useful things a plot will ever tell you.

## Done?

```bash
./tools/frcprog next
```

Stage 1C is complete: you have a drivable robot with buttons, sequences, and
instrumentation.

## The rule

**If you did not plot it, it did not happen.**

The habit worth building: when you add a control loop, add its telemetry in the same
commit. Not later, when it breaks. The five minutes now is the difference between
debugging with data and debugging by argument.

`System.out.println` is the alternative, and it is worse in every way that matters —
it scrolls away, it has no time axis, you cannot compare two signals, and it slows
the loop down. Plot it.
