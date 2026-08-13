# Lesson 10 — Telemetry

**Stage 1C · 30 min · Needs: 09**

"Why isn't the shooter reaching speed?" Stare at the code for an hour, or plot
three numbers and know in five seconds.

## Do this

Open `subsystems/flywheels/Flywheels.java`. Two TODOs.

**1. Two more publishers**, next to `targetPublisher`:

```java
private final DoublePublisher actualPublisher = table.getDoubleTopic("ActualRPM").publish();
private final DoublePublisher errorPublisher  = table.getDoubleTopic("ErrorRPM").publish();
```

**2. Set them every loop** in `periodic()`:

```java
actualPublisher.set(getVelocityRpm());
errorPublisher.set(getErrorRpm());
```

Also close both in `close()`.

## Check it

```bash
./tools/frcprog check 10-telemetry
```

## See it

This is the lesson where the tooling pays you back. Do it properly.

```bash
./tools/frcprog sim          # one terminal
./tools/frcprog scope        # another
```

In AdvantageScope: **File → Connect to Simulator**. Find `NT/Flywheels` in the
sidebar. Drag all three onto one line chart. Hold A.

You are looking at a step response:

| Term | What it is |
|---|---|
| Rise time | how long until actual gets near target |
| Settling time | how long until it stays there |
| Overshoot | how far past it goes |
| Steady-state error | the gap left once settled |

`ErrorRPM` is the vertical distance between the other two traces, drawn for you.

**Watch what happens when the roller fires.** Actual dips, because the game piece
takes energy out of the wheels, then recovers. On a real robot that recovery time
sets how fast you can take a second shot.

## Why

NetworkTables is a shared key-value store. The robot publishes, anything watching
subscribes. Table name plus topic name gives the path: `/Flywheels/TargetRPM`.

**Why a publisher and not `SmartDashboard.putNumber`.** That looks the string up in
a hash map every call, fifty times a second, for every value. A `DoublePublisher`
resolves the topic once. On a robot publishing 200 signals the difference shows in
your loop time.

**Naming:** `SubsystemName/FieldName`, PascalCase. AdvantageScope builds its tree
from the slashes. Inconsistent names give you alphabetical soup at exactly the
moment you are debugging in a pit with four minutes left.

## Done

Rubric is green. Stage 1C complete.

```bash
./tools/frcprog next
```

**The rule: if you did not plot it, it did not happen.** Add telemetry in the same
commit as the control loop, not later when it breaks. `System.out.println` scrolls
away, has no time axis, cannot compare two signals, and slows the loop.
