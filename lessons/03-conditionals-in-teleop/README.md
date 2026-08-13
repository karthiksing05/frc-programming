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

## Two things that will catch you

**The sensor reads backwards.** A beam-break pulls high when nothing blocks it. So
"we have a game piece" is `!beamBreak.get()`. Invert it once, at the top, and give
it a name:

```java
boolean hasGamePiece = !beamBreak.get();
```

**Order is the logic.** `else if` means "only if nothing above matched". The branch
you check first wins. X goes first.

## See it

```bash
./tools/frcprog sim
```

Drag **Keyboard 1** onto **Joystick[1]**, click **Teleoperated**, watch **PWM 5**.

## Done

Rubric is green. Now count the lines you wrote.

Probably ten to fifteen. Imagine the elevator here too. And the arm, the shooter,
the climber, each with its own sensor and buttons, sharing one `else` chain. That
is roughly a hundred lines with no structure.

Teams ship robots like that. They work, until somebody has to change one behaviour
under time pressure and cannot find it.

Lesson 04 takes it away from you.

```bash
./tools/frcprog next
```
