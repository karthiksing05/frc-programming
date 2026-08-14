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

The minus signs are not typos. An Xbox stick reads negative when pushed forward,
because the axis is measured screen-style with +Y downward.

## Check it

```bash
./tools/frcprog check 07-tank-drive
```

Six checks. Number 4 changes the stick value **after** scheduling the command and
requires the output to follow.

## How it works

### Why a Command and not a state

Every mechanism so far took discrete orders: intake, go to L4, level the arm. You
set a state, `periodic` acted on it.

A drivetrain has no state to set. There is no "driving mode". There is a number that
changes continuously, for as long as a human is holding something, and then stops.

A `Command` is a unit of work with three parts: something at the start, something
repeated every loop, and something at the end. That maps onto "drive while held"
exactly.

### Requirements, and why they matter

`run(...)` comes from `SubsystemBase`, so the Command it builds automatically
**requires** the drivetrain.

Requirements are how the scheduler stops two things fighting. If an auto-align
command and your drive command are both scheduled, the scheduler cancels one. It
does not let both write to the motors and leave the last one winning.

Without that you would have two pieces of code writing 50 times a second, and
behaviour depending on which ran last. That is not a hypothetical; it is what
happens in `teleopPeriodic`-style code.

### Default commands

`setDefaultCommand` means "run this whenever nothing else has claimed the
drivetrain". It is interrupted the moment something does, and resumes automatically
when that finishes.

For a drivetrain this is exactly right: drive normally, unless something else needs
it, then go back to driving. You never write that logic.

### The supplier thing

This is the important part of the lesson.

`arcadeDriveCommand` takes `DoubleSupplier`, not `double`.

A `double` is evaluated **once**, at the moment the method is called. Commands are
built in `RobotContainer`'s constructor, during robot startup, when every stick
reads 0.0. So a captured value is 0.0 forever.

A `DoubleSupplier` is a function. `() -> -driver.getLeftY()` is not a number, it is
"how to get the number". Calling `.getAsDouble()` inside the lambda runs it fresh
every loop.

??? question "Predict: what exactly happens with a captured value?"

    ```java
    public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation) {
        double fwd = forward.getAsDouble();      // outside the lambda
        return run(() -> setVoltage(fwd * 12, fwd * 12));
    }
    ```

    It compiles with no warning. It runs. The scheduler dutifully calls the lambda
    50 times a second, and the lambda dutifully commands `0.0 * 12` every time,
    because `fwd` was read during startup and never again.

    The driver pushes the stick. Nothing. They push harder. Nothing. They conclude
    the drivetrain is broken, and somebody spends an hour on the motor controllers.

    Do this on purpose now, in the sim, so you recognise it in three months. Then
    put it back.

### Arcade mixing

```java
left  = forward + rotation
right = forward - rotation
```

Push forward: both sides equal, robot goes straight. Turn right: left speeds up,
right slows down, robot rotates. Both at once: a curve.

??? info "Why clamp before scaling to volts"

    Full forward plus full turn is `1.0 + 1.0 = 2.0`. Scale that by 12 and you asked
    a 12 V battery for 24 V.

    Nothing breaks; the motor controller clips it. But now the left side is
    saturated and the right side is not, so the robot turns differently at full
    throttle than at half. The driver feels the handling change and cannot say why.

    Clamping first keeps the mixing predictable across the whole stick range.

    There are better mixings. Normalising both sides by the larger magnitude
    preserves the turn ratio instead of clipping it. Clamping is the standard
    starting point and what most teams ship.

**Three lessons meet in the middle of this method.** Lesson 01's function, called
with lesson 02's constant, inside lesson 07's command. That is what "the project
grows" means in practice.

## See it

```bash
./tools/frcprog sim
```

Full walkthrough: **[Running the simulator](../../../setup/simulator.md)**.

1. Drag **Keyboard 0** onto **Joysticks** slot **0**
2. Check **DS → Keyboard 0 Settings** for which keys drive axis 1 and axis 4
3. Click **Teleoperated**
4. Open **Hardware → PWM Outputs**, watch channels **0** and **1**

Press forward: both go positive together. Press turn: they split. Release: both snap
to zero.

Then plot them in AdvantageScope on one graph. Straight driving is two traces on top
of each other; turning is a clean split.

??? example "Experiment: break the supplier on purpose"

    Two minutes.

    1. Move the two `getAsDouble()` calls **outside** the `run(() -> ...)` lambda
    2. `./gradlew build`, then `./tools/frcprog sim`
    3. Enable Teleoperated and press your drive keys
    4. Watch PWM 0 and 1 stay at 0.000

    No error. No warning. Nothing in the log. Just a robot that ignores you.

    Now put them back inside and watch it work. That contrast is the thing to
    remember.

## Done

The rubric passes and you have a robot you can actually drive around.

```bash
./tools/frcprog next
```

**On factories.** `arcadeDriveCommand` is a method on a subsystem that returns a
Command. WPILib's own docs say *"teams should rarely need to write custom command
classes"*. The older style was a 30-line class in a separate file, with an
`addRequirements` call you could forget, holding a reference to the subsystem it
controlled. The factory version is one method, next to the hardware, and it cannot
forget its requirement.

Command subclasses still have a place, for genuinely stateful multi-phase things.
Lesson 27 has the first honest example.
