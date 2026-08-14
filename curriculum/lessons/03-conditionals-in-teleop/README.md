# Lesson 03 — Conditionals in teleopPeriodic

**Stage 1A · 35 min · Needs: 02**

You are about to write working code in the wrong place, on purpose, so that lesson 04
can show you why nobody leaves it there.

The mechanism is an intake roller: a spinning wheel that pulls a game piece into the
robot and can spit it back out. Across the throat of the intake is a beam-break sensor
— an infrared beam that the game piece interrupts when it is loaded — so the robot can
tell whether it is already holding something.

## Do this

Open `src/main/java/frc/robot/Robot.java` and find `TODO (LESSON 03)` inside
`teleopPeriodic()`. Write the behaviour your drive team asked for using plain `if` /
`else if` / `else`.

These are the four calls you have to work with:

```java
operator.getBButton()     // true for as long as B is held down
operator.getXButton()     // true for as long as X is held down
beamBreak.get()           // true when the beam is UNBROKEN, so !get() means "loaded"
rollerMotor.set(speed)    // -1.0 to 1.0; negative runs the roller the other way
```

And this is what the operator expects to happen:

```java
// Holding X ejects, always. Check this first — it has to beat B, because
// spitting out a piece you are already holding is the whole point of the button.
rollerMotor.set(Constants.Roller.EJECT_SPEED);     // -0.6, runs outward

// Holding B pulls a piece in, but only if there is not one in there already.
// Running the intake against a piece that is already seated just grinds it.
rollerMotor.set(Constants.Roller.INTAKE_SPEED);    // 0.6, runs inward

// Holding B with a piece already loaded, or holding nothing at all: stop.
rollerMotor.set(0.0);
```

Every path through your code must end in exactly one `rollerMotor.set(...)` call. A
path that sets nothing leaves the motor doing whatever it was doing last.

## Check it

```bash
./tools/frcprog check 03-conditionals-in-teleop
```

## Spot the bug

This is the most common way this lesson gets written wrong. It compiles, and if you
test it by holding B on an empty robot it looks perfect.

```java
if (operator.getBButton() && beamBreak.get()) {
  rollerMotor.set(Constants.Roller.INTAKE_SPEED);
} else if (operator.getXButton()) {
  rollerMotor.set(Constants.Roller.EJECT_SPEED);
} else {
  rollerMotor.set(0.0);
}
```

What does the robot do when the driver holds X and B at the same time while empty —
and why is that going to happen during a match?

??? success "Answer"

    It intakes, when the driver asked it to eject.

    The `if` chain checks B first, so as soon as B and an empty intake are both true,
    the eject branch is never reached. Java stops at the first matching branch and
    never looks at the rest.

    The reason this matters is that operators do not press one button at a time. In a
    match they are holding B almost permanently — it is the "collect anything you drive
    over" button — and they hit X to score. With this ordering, X does nothing until
    they consciously release B, which is not something anybody remembers to do while
    being defended.

    Order the branches by priority, not by the order you happened to think of them.
    Check X first, and B becomes the fallback it should have been all along.

    This is worth generalising: in an `if`/`else if` chain, the order *is* the
    priority. That is a design decision, and it should be a deliberate one.

## How it works

### What teleopPeriodic is

WPILib runs your robot as a loop at 50 Hz. Every 20 ms it reads the Driver Station,
works out which mode you are in, and calls the matching method.

```
                 every 20 ms
  robotPeriodic() ─┬─ disabledPeriodic()
                   ├─ autonomousPeriodic()
                   ├─ teleopPeriodic()      ← you are here
                   └─ testPeriodic()
```

Your code gets 20 ms to decide everything and return. It does not loop, it does not
wait, it does not sleep. It runs once and gives control back. Anything that blocks
here stops the whole robot.

That is why the method has no `while` loop in it. The loop is outside, and it is
WPILib's.

### The sensor reads backwards

A beam-break is an emitter facing a detector. Nothing in the way means the detector
sees light and the input reads **high**. A game piece blocks the beam and it reads
**low**.

So "we have a game piece" is `!beamBreak.get()`. The inversion is real and it will
bite somebody on your team this season.

Fix it once, at the top, with a name:

```java
boolean hasGamePiece = !beamBreak.get();
```

Now every line below reads the way you think. You will see this pattern repeatedly:
**invert once, name it, never think about it again.**

??? question "Predict: what breaks if you check B before X?"

    ```java
    if (operator.getBButton() && !hasGamePiece) { intake(); }
    else if (operator.getXButton())            { eject();  }
    ```

    Hold both buttons with nothing loaded and you intake, because the first
    matching branch wins and nothing below it runs.

    The operator, trying to clear a jam, gets the opposite of what they asked for.

    `else if` means "only if nothing above matched", so **line order is the
    priority**. There is no other mechanism. That is worth internalising now,
    because it is how every `if` chain you ever write behaves.

??? question "Predict: what happens if you leave out the final `else`?"

    ```java
    if (operator.getXButton())                 { rollerMotor.set(-0.6); }
    else if (operator.getBButton() && !loaded) { rollerMotor.set(0.6);  }
    // nothing here
    ```

    Release everything and the roller keeps spinning. Forever.

    A motor controller holds the last value it was given. Not commanding it is not
    the same as commanding zero. Every 20 ms you have to say what the motor should
    be doing, including "nothing".

    This is the single most common bug in first-week FRC code, and it is why rubric
    check 4 exists.

## See it

```bash
./tools/frcprog sim
```

Full walkthrough: **[Running the simulator](../../../setup/simulator.md)**.

The short version for this lesson:

1. Drag **Keyboard 1** onto **Joysticks** slot **1**
2. Check **DS → Keyboard 0 Settings** to see which keys are your B and X buttons
3. Click **Teleoperated** in Robot State
4. Open **Hardware → PWM Outputs** and watch channel **5**
5. Open **Hardware → DIO** to toggle channel **4**, the beam-break

Now walk the truth table. Hold B with DIO 4 high: PWM 5 goes to 0.6. Click DIO 4 to
break the beam: it drops to 0. Hold X as well: it goes to −0.6.

Being able to make any sensor state happen instantly, without a field or a game
piece, is the reason simulation is better than hardware for learning.

## Done

The rubric passes, so count the lines you just wrote — probably ten to fifteen for one
motor and one sensor.

Now picture the rest of a competition robot in the same method: an elevator, an arm, a
shooter, and a climber, each with its own sensors and buttons, all sharing one `if`
chain. That is somewhere around a hundred lines with no structure at all, inside a
method whose actual job is only "be the teleop mode".

Three specific things go wrong once it reaches that size. Everything can reach
everything, so nothing stops climber code from writing to the roller motor by mistake.
None of it can be tested, because there is no way to ask "what does the roller do when
it is loaded?" without constructing an entire robot first. And the sensor polarity, the
speed constants and the button layout are all tangled together, so changing any one of
them means reading all of it.

Plenty of teams do ship robots written this way, and they work fine right up until the
Saturday somebody has to change one behaviour under time pressure. Lesson 04 takes this
code away from you and shows you where it belongs instead.

```bash
./tools/frcprog next
```
