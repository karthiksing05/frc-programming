# Lesson 14 — Splitting up RobotContainer

**Stage 1D · 45 min · Needs: 13**

Nothing in `configureBindings()` is wrong. It is just all in one pile.

## Do this

Move one thing at a time and run the matching check after each. A refactor done in
one move, discovered broken at the end, is a bad afternoon.

**1. `bindings/DriverBindings.java`:**

```java
drive.setDefaultCommand(
    drive.arcadeDriveCommand(() -> -controller.getLeftY(), () -> -controller.getRightX()));
controller.leftBumper().whileTrue(drive.stopCommand());
```

Then `./tools/frcprog check 07-tank-drive`.

**2. `bindings/OperatorBindings.java`** — move the operator's buttons and lesson
11's composed trigger. Add `import frc.robot.Constants;`.

Then `./tools/frcprog check 08-triggers-bindings` and `check 11-default-commands`.

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

## How it works

### What a refactor is

A change that alters structure and preserves behaviour. Both halves matter, and the
second is the one that needs proving.

That is why this lesson's real check is `check --all`: every rubric from lesson 01,
unchanged, run against restructured code. If they all still pass, you moved code. If
any fails, you rewrote something.

This is what a test suite is actually for. Not catching the bug you were already
thinking about, but making you unafraid to move code. Without it, "it works, do not
touch it" wins every argument and the codebase calcifies.

### Split by human, not by mechanism

Two obvious ways to divide bindings: by subsystem (`ElevatorBindings`,
`ShooterBindings`) or by person (`DriverBindings`, `OperatorBindings`).

Split by person.

When somebody says "the intake button isn't working", you want exactly one file to
open, and the file you want is the one named after them. Control-scheme questions
are asked by humans about their own controller, not about a mechanism.

A subsystem split also scatters one person's controls across four files, so nobody
can see their whole control scheme at once, which is the thing you most often want
to review before a competition.

### Constructor injection

`OperatorBindings` takes four subsystems, a controller and a command. It does not
take `RobotContainer`.

That is the point. It **cannot** reach the drivetrain, so it cannot accidentally
couple to it. Handing a class what it depends on makes the dependency visible in the
signature, swappable in a test, and impossible to acquire by accident.

Pass `RobotContainer` instead and the class can reach everything, and in six months
it will.

Same idea as lesson 16's IO layer, applied to a different problem.

??? question "Predict: why is the object never stored in a field?"

    ```java
    new DriverBindings(drive, driver);     // result discarded
    ```

    The constructor's entire job is a side effect: registering bindings with the
    scheduler and setting a default command. Once that has happened there is nothing
    left to talk to. A field holding it would be a field nobody reads.

    If this bothers you, keep the instinct. A silently discarded object is usually a
    smell worth investigating. This is the exception: a class whose construction
    *is* the work.

    The alternative is a static `configure(...)` method, which some teams prefer and
    which reads more honestly. Both are fine.

??? info "When to actually do this on a real team"

    Not on day one, and not never.

    Keep everything in `RobotContainer` until it stops fitting on a screen and a
    half, call it 150 lines of real code. Then split.

    The honest signal is not line count. It is the first time you scroll past a
    binding you were not looking for while hunting for one you were.

## See it

Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
```

Drive it. Press every button. Nothing should be different from before you started.

That is the whole success condition for a refactor, and it is worth doing by hand
rather than trusting the tests, because the tests only check what you thought to
check.

## Done

`check 14-bindings-refactor` is green **and** `check --all` is green.

```bash
./tools/frcprog next
```
