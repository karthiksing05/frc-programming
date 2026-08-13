# Lesson 14 — Splitting up RobotContainer

**Stage 1D · 45 min · Needs: 13**

Nothing in `configureBindings()` is wrong. It is just all in one pile.

## Do this

Move one thing at a time and run the matching check after each.

**1. `bindings/DriverBindings.java`:**

```java
drive.setDefaultCommand(
    drive.arcadeDriveCommand(() -> -controller.getLeftY(), () -> -controller.getRightX()));
controller.leftBumper().whileTrue(drive.stopCommand());
```

Then `./tools/frcprog check 07-tank-drive`.

**2. `bindings/OperatorBindings.java`** — move the operator's buttons and lesson
11's composed trigger. Needs `import frc.robot.Constants;`.

Then `./tools/frcprog check 08-triggers-bindings`.

**3. `RobotContainer.java`** — delete the originals, construct the new classes:

```java
new DriverBindings(drive, driver);
new OperatorBindings(elevator, shoulder, flywheels, roller, operator, scoreCommand());
```

## Check it

```bash
./tools/frcprog check 14-bindings-refactor
```

Then the real test:

```bash
./tools/frcprog check --all
```

Every rubric from lesson 01, unchanged, against restructured code. A green board is
the proof that this was a refactor and not a rewrite. That is what a test suite is
for: not catching the bug you were thinking about, but making you unafraid to move
code.

## The objects are never stored

That looks wrong and is not. Each constructor's whole job is a side effect:
registering bindings with the scheduler. Once that has happened there is nothing
left to talk to.

If it bothers you, keep the instinct. Silently discarded objects are usually a
smell. This is the exception.

## Split by human, not by subsystem

`DriverBindings` and `OperatorBindings`, not `ElevatorBindings` and
`ShooterBindings`.

When a driver says "the intake button isn't working" you want one file to open, and
the file you want is named after them.

## Constructor injection

`OperatorBindings` takes four subsystems, a controller and a command. It does not
take `RobotContainer` and cannot reach the drivetrain.

A class that cannot reach something cannot couple to it by accident. Same idea as
lesson 16's IO layer.

## See it

```bash
./tools/frcprog sim
```

Drive it, press every button. Nothing should differ.

## Done

Rubric is green and `check --all` is green.

```bash
./tools/frcprog next
```

**When to do this for real:** keep everything in `RobotContainer` until it stops
fitting on a screen and a half. The real signal is the first time you scroll past a
binding you were not looking for.
