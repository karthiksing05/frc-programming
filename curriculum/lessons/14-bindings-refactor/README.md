# Lesson 14 — Refactoring with `*Bindings` classes

> **Stage 1D · ~45 minutes · Prerequisite: 13**

Open `RobotContainer.configureBindings()` and read it.

Nothing in there is wrong. Every line is a reasonable binding. The problem is that
answering "what does the operator's B button do?" now means reading all of it, and
next season there will be twice as much.

Today you split it up. The robot's behaviour will not change at all — and proving
that is half the lesson.

## What you'll learn

1. Extract a group of bindings into its own class.
2. Pass dependencies through a constructor instead of reaching for them.
3. Keep `RobotContainer` to subsystem ownership and wiring.
4. Verify a refactor by re-running earlier lessons' rubrics.

## What you'll do

### 1. `DriverBindings`

Move the drivetrain's default command into
`src/main/java/frc/robot/bindings/DriverBindings.java`:

```java
public DriverBindings(Drive drive, CommandXboxController controller) {
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -controller.getLeftY(), () -> -controller.getRightX()));

    controller.leftBumper().whileTrue(drive.stopCommand());
}
```

### 2. `OperatorBindings`

Move the operator's bindings into `OperatorBindings.java` — including lesson 11's
composed trigger.

### 3. Slim down `RobotContainer`

Delete the moved lines and construct the new classes instead:

```java
new DriverBindings(drive, driver);
new OperatorBindings(elevator, shoulder, flywheels, roller, operator, scoreCommand());
```

### The objects are never stored

That looks wrong the first time you see it, and it is not.

Each constructor's entire job is a side effect: registering bindings with the
scheduler and setting a default command. Once that has happened there is nothing
left to talk to. Storing the object in a field would just be a field nobody reads.

If it bothers you, that instinct is worth keeping — silently-discarded objects are
usually a smell. This is the exception: a class whose constructor *is* the work.

### Split by human, not by subsystem

There are two obvious ways to divide bindings: by mechanism (`ElevatorBindings`,
`ShooterBindings`) or by person (`DriverBindings`, `OperatorBindings`).

Split by person. When a driver says "the intake button isn't working", you want one
file to open, and the file you want is the one named after them. Control-scheme
questions are asked by humans about their own controller.

### Constructor injection

`OperatorBindings` takes four subsystems, a controller, and a command. It does not
take `RobotContainer`, and it has no way to reach the drivetrain.

That is the point. A class that *cannot* reach something cannot accidentally couple
to it. Handing a class what it depends on — rather than letting it go and find
things — makes the dependency visible in the signature, swappable in a test, and
impossible to acquire by accident.

It is the same idea as lesson 16's IO layer, applied to a different problem.

## Run it

```bash
./tools/frcprog check 14-bindings-refactor
```

Four checks:

1. The bindings classes are no longer empty shells.
2. `RobotContainer` has actually shrunk — measured in *code* lines, ignoring
   comments and blanks, so explaining yourself is never penalised.
3. `RobotContainer` constructs both classes.
4. Behaviour is unchanged — the buttons still work.

Then the real test:

```bash
./tools/frcprog check --all
```

Every rubric from lesson 01 to 14, unchanged, against your refactored code. A green
board is the actual proof that this was a refactor and not a rewrite. This is what a
test suite is *for*: not catching the bug you were thinking about, but giving you
the confidence to restructure code without being afraid of it.

## See it

```bash
./tools/frcprog sim
```

Drive it. Press every button. Nothing should be different. If something is, the
refactor moved a line's meaning as well as its location.

## Done?

```bash
./tools/frcprog next
```

Next is the capstone.

## When to do this for real

Not on day one, and not never.

The just-in-time version: keep everything in `RobotContainer` until it stops fitting
on a screen and a half — call it 150 lines of actual code — then split. Doing it
early adds structure before there is anything to organise; doing it never means the
file is 400 lines by competition and nobody can find anything.

The signal is not line count, really. It is the first time you scroll past a binding
you were not looking for while hunting for one you were.
