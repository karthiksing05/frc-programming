# Lesson 03 — Conditionals in `teleopPeriodic`

> **Stage 1A · ~35 minutes · Prerequisite: 02**

Read this before you start, because this lesson is not what it looks like.

Today you will write working code in the wrong place, on purpose. It will pass its
rubric. It will drive a real mechanism correctly. And by the time you finish, you
should be able to feel — not be told, *feel* — why nobody builds a robot this way.

Then lesson 04 takes it away from you, and the relief is the actual lesson.

This is deliberate, and it is borrowed from
[Katie Niwiden's work on teaching FRC programming](https://docs.google.com/presentation/d/15O2Xo5cHsYG3hVvQbMSB2SuvU9ED0Y3feaKdCbgaQyM/preview):
an abstraction handed to you before you have felt the problem is just ceremony to
memorise. An abstraction that arrives right after the problem is a gift.

## What you'll learn

1. Read a boolean sensor — a beam-break.
2. Read a button from an `XboxController`.
3. Write an `if` / `else if` / `else` chain where the order genuinely matters.
4. Drive a motor from combined sensor and button state.
5. **Notice that this is already getting hard to read.** That is the real objective.

## What you'll do

Open `src/main/java/frc/robot/Robot.java` and find `teleopPeriodic()`. It runs
fifty times a second while a driver is in control.

Three fields are already declared at the top of the class — a motor, a beam-break
sensor, and a controller. Note where they are: sitting in `Robot`, the class whose
job is supposed to be the robot's *lifecycle*, not its mechanisms. Hold that
thought.

The drivers asked for this behaviour:

- **Hold B** → run the roller inward at `Constants.Roller.INTAKE_SPEED`
- **...unless a game piece is already in the throat**, in which case stop. Grinding
  a piece against a hard stop is how rollers eat belts.
- **Hold X** → eject at `Constants.Roller.EJECT_SPEED`, and this beats B. Ejecting a
  piece you are holding is the entire reason the button exists.
- **Nothing held** → 0.0

Calls you need:

```java
operator.getBButton()     // true while B is held
operator.getXButton()     // true while X is held
beamBreak.get()           // true when the beam is UNBROKEN
rollerMotor.set(speed)    // speed in [-1, 1]
```

### The sensor reads backwards, and it always will

A beam-break is an emitter and a detector facing each other. Nothing in the way →
the detector sees light → the input reads `true`. A game piece in the way → beam
broken → `false`.

So "we have a game piece" is `!beamBreak.get()`.

That inversion is a genuine, recurring source of bugs, and this is the first of
several times this curriculum will point out the same fix: **invert it once, in one
place, and give the result a name.**

```java
boolean hasGamePiece = !beamBreak.get();
```

Now every line below reads the way a human thinks.

### Order is the logic

`else if` means "only if none of the branches above matched". So the branch you
check *first* wins. X must beat B, therefore X is checked first. There is no other
mechanism at work here — the priority is literally the line order.

## Run it

```bash
./tools/frcprog check 03-conditionals-in-teleop
```

Four scenarios:

1. Hold B, nothing loaded → roller runs inward
2. Hold B, piece loaded → roller stops
3. Hold X (with or without B) → roller ejects
4. Nothing held → roller stops

## See it

```bash
./tools/frcprog sim
```

In the simulator window, drag **Keyboard 1** onto **Joystick[1]**, click
**Teleoperated**, and watch the **PWM** panel — output 5 is your roller. Press the
keys mapped to B and X and watch the number move.

## Done?

The rubric is green. Before you move on, do one thing:

**Count the lines you wrote.**

Probably ten to fifteen. Now imagine the elevator lives here too. And the arm. And
the shooter, and the climber, each with its own sensor and its own buttons, all
sharing one `else` chain in one method. That is roughly a hundred lines with no
structure, where every mechanism can see — and accidentally command — every other
mechanism's motor.

Teams ship robots like that. They work, right up until the Saturday somebody has to
change one behaviour under time pressure and cannot find where it lives.

```bash
./tools/frcprog next
```

## What this lesson deliberately does not teach

Subsystems. Commands. Triggers. Any of the machinery that would solve the mess you
just made.

That is lesson 04, and it will feel like an answer rather than a rule, because you
will have the question.
