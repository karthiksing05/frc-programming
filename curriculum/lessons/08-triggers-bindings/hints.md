# Hints — Lesson 08

## Hint 1 — Where to start

Find the `TODO (LESSON 08)` in `configureBindings()`. The comment above it lists the
bindings; each is one line of the form:

```java
operator.<button>().<semantic>(<command>);
```

Before writing each one, ask: is this something the operator *holds*, or something
they *tap*? That answers `whileTrue` versus `onTrue`.

## Hint 2 — The shape of the answer

The button methods are named after the buttons: `a()`, `b()`, `x()`, `y()`,
`leftBumper()`, `rightBumper()`, `povUp()`, `povDown()`.

The commands already exist — you are not writing any new ones today:

- `flywheels.spinUpCommand()`
- `roller.intakeCommand()` and `roller.ejectCommand()`
- `elevator.goToCommand(height)`
- `shoulder.goToCommand(radians)`

Heights and angles come from `Constants.Elevator` and `Constants.Shoulder`.

## Hint 3 — Almost there

If check 1 fails on *release* — the flywheels keep spinning after you let go — you
used `onTrue`. `onTrue` fires the command and then leaves it running to its own
completion, and `spinUpCommand()` never completes on its own. That is a shooter that
runs for the rest of the match.

If check 3 fails — the elevator stops partway — you used `whileTrue` on Y. The test
taps and releases, so `whileTrue` cancels the command almost immediately.

The rule of thumb: **hold → `whileTrue`, tap → `onTrue`.**

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
private void configureBindings() {
    // Hold to spin, release to coast — the driver never has to remember whether
    // the shooter is currently on, because their thumb is the state.
    operator.a().whileTrue(flywheels.spinUpCommand());

    // Hold to intake, hold to eject. Two buttons rather than one toggle.
    operator.b().whileTrue(roller.intakeCommand());
    operator.x().whileTrue(roller.ejectCommand());

    // onTrue, not whileTrue: "go to L4" is a destination, not a thing you hold.
    // The command finishes by itself when the carriage arrives.
    operator.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    operator.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
    operator.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));
}
```

**Worth noticing:** `roller.intakeCommand()` is built with `startEnd(...)`, whose
second lambda runs when the command ends *however it ends* — released button,
cancelled by something else, interrupted by a higher-priority command. That is what
guarantees the roller returns to OFF rather than being left running by an exit path
somebody forgot about.

Cleanup that only happens on the expected exit path is not cleanup.

</details>
