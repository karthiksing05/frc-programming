# Lesson 03 — Conditionals in teleopPeriodic

**Stage 1A · 35 min · Needs: 02**

Read this first: you are about to write working code in the wrong place, on purpose.

## Do this

1. Open `src/main/java/frc/robot/Robot.java`
2. Find `TODO (LESSON 03)` inside `teleopPeriodic()`
3. Write the behaviour below with plain `if` / `else if` / `else`

| Operator does | Roller should |
|---|---|
| Holds X | eject at `Constants.Roller.EJECT_SPEED` |
| Holds B, nothing loaded | intake at `Constants.Roller.INTAKE_SPEED` |
| Holds B, piece already loaded | stop |
| Nothing | stop |

X beats B. Ejecting a piece you are holding is the whole reason for the button.

## Check it

```bash
./tools/frcprog check 03-conditionals-in-teleop
```

## Calls you need

```java
operator.getBButton()     // true while held
operator.getXButton()     // true while held
beamBreak.get()           // true when the beam is UNBROKEN
rollerMotor.set(speed)    // -1 to 1
```

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

Rubric is green. Now count the lines you wrote.

Probably ten to fifteen. Now imagine the elevator here too. And the arm, the
shooter, the climber, each with its own sensor and buttons, all sharing one `else`
chain. That is roughly a hundred lines with no structure, in a method whose actual
job is "be the teleop mode".

Three specific things go wrong at that size:

- **Everything can reach everything.** Nothing stops the climber code writing to the
  roller motor.
- **You cannot test any of it.** There is no way to ask "what does the roller do
  when loaded" without constructing an entire robot.
- **The sensor polarity, the speed constants, and the button layout are all mixed
  together.** Change any one and you read all of it.

Teams ship robots like this. They work. They stop working the Saturday somebody has
to change one behaviour under time pressure.

Lesson 04 takes it away from you.

```bash
./tools/frcprog next
```
