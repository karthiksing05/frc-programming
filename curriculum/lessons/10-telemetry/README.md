# Lesson 10 — Telemetry

**Stage 1C · 30 min · Needs: 09**

"Why isn't the shooter reaching speed?" Stare at the code for an hour, or plot three
numbers and know in five seconds.

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

## How it works

### What NetworkTables is

A key-value store shared over the network. The robot writes; anything else
subscribes: the Driver Station, a dashboard, AdvantageScope, your laptop.

Table name plus topic name makes the path. `getTable("Flywheels")` plus
`getDoubleTopic("TargetRPM")` gives `/Flywheels/TargetRPM`.

It updates at about 10 Hz by default over the wire, not 50. Fine for watching;
worth knowing if you are chasing something that happens in one loop.

### Why a publisher and not SmartDashboard

`SmartDashboard.putNumber("Flywheels/TargetRPM", x)` works. It also looks that
string up in a hash map on **every call**, 50 times a second, for every value you
publish.

A `DoublePublisher` resolves the topic once, at construction, and holds the handle.
Setting it writes straight through.

On a robot publishing a couple of hundred signals, which is normal, the difference
is measurable in loop time. You have 20 ms; spending it on string lookups is a poor
trade.

??? info "Why publishers are fields, not locals"

    Creating a publisher allocates a NetworkTables handle. Creating one inside
    `periodic()` would allocate a new handle 50 times a second and leak all of them.

    This is also why you must restart the simulator after adding a publisher: they
    are created once, during construction.

### Naming

`SubsystemName/FieldName`, PascalCase.

AdvantageScope builds its sidebar tree from the slashes, so a consistent scheme
gives you a browsable list grouped by mechanism. An inconsistent one gives you a
flat alphabetical soup at exactly the moment you are debugging in a pit with four
minutes on the clock.

### Reading a step response

This is the actual skill the lesson is teaching.

| Term | What it is | Why you care |
|---|---|---|
| Rise time | until it first gets near | how quick |
| Settling time | until it stops moving | when you can trust it |
| Overshoot | how far past it goes | too aggressive |
| Steady-state error | the gap that remains | never quite arrives |

`ErrorRPM` is the vertical distance between the other two traces, drawn for you. It
crosses zero exactly when actual crosses target.

??? question "Predict: what does the flywheel trace do when the roller fires?"

    It **dips**, then recovers.

    The game piece takes energy out of the wheels. The controller sees error appear
    and pushes harder to recover.

    How fast it recovers determines how quickly you can take a second shot, which is
    one of the most useful things a plot will ever tell you about a shooter. Teams
    tune `kP` on a flywheel largely for this recovery, not for the initial spin-up.

    Watch for it in the See it section below.

## See it

This is the payoff lesson for the tooling. Do it properly.

Full walkthrough: **[Running the simulator](../../../setup/simulator.md)**.

**Terminal 1:**

```bash
./tools/frcprog sim
```

**Terminal 2:**

```bash
./tools/frcprog scope
```

Then:

1. **File → Connect to Simulator**
2. Sidebar: expand `NT` → `Flywheels`
3. Drag `TargetRPM`, `ActualRPM` and `ErrorRPM` onto **one** Line Graph
4. In the sim, bind Keyboard 1 to Joysticks 1 and click **Teleoperated**
5. Hold your A key

Now look at the shape, not the numbers:

- Target is a flat line at 3000, a step
- Actual is a curve climbing toward it
- Error starts at 3000 and decays to zero

Hold the right bumper to score and watch actual dip when the roller feeds.

**Save the layout.** File → Save Layout. You will want these three signals again in
lessons 27 and 28.

??? example "Experiment: make the plot tell you something"

    1. In `Constants.Flywheels`, set `kP` to `0.0`. Rebuild, rerun.
    2. Plot again. The wheels still spin up, driven by feedforward alone, but they
       settle slightly below 3000 and stay there.
    3. That gap is steady-state error, and it is what feedback is for.
    4. Put `kP` back to `0.0015`. The gap closes.
    5. Now try `kP = 0.02`. Watch it overshoot and oscillate.

    Same four shapes as the elevator in lesson 05, on a completely different
    mechanism. That is the point: you are learning to read a picture, not memorising
    one robot.

## Done

The rubric passes, the robot is publishing everything you need to debug it, and
that completes Stage 1C.

```bash
./tools/frcprog next
```

**The rule: if you did not plot it, it did not happen.** Add the telemetry in the
same commit as the control loop, not later when it breaks. `System.out.println`
scrolls away, has no time axis, cannot show two signals together, and slows the
loop down.
