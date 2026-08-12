# Hints — Lesson 14

## Hint 1 — Where to start

Move one thing at a time and run `frcprog check 07-tank-drive` after each. A refactor
done in one big move, discovered broken at the end, is a bad afternoon.

Order that works:

1. Drive default command → `DriverBindings`. Check 07.
2. Operator buttons → `OperatorBindings`. Check 08.
3. Composed trigger → `OperatorBindings`. Check 11.
4. Delete the originals, construct both classes. Check 14.

## Hint 2 — The shape of the answer

The bindings classes are already stubbed with the right constructor signatures. Both
have a `TODO (LESSON 14)` listing what belongs in them.

The lines you move barely change. `operator.a()` becomes `controller.a()`, because
inside the new class the controller arrives as a parameter with that name.

`OperatorBindings` needs one import you will have to add: `frc.robot.Constants`.

## Hint 3 — Almost there

**Check 2 failing (still too many lines):** you moved code without deleting the
original. Commented-out code does not count toward the limit — it is real code still
sitting in `RobotContainer`.

**Check 4 failing (drive has no default command):** you moved the line but never
constructed `DriverBindings`, so the constructor never ran.

**Lesson 11's rubric failing:** the composed trigger came across but references
`operator` rather than the constructor's `controller` parameter — or it was left
behind in `RobotContainer`, which now has no bindings but two copies of one.

**`scoreCommand()` in `OperatorBindings`:** it is passed in as a parameter, because
it spans subsystems this class holds and ones it does not. Pass `scoreCommand()`
when you construct it.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

**DriverBindings**

```java
public DriverBindings(Drive drive, CommandXboxController controller) {
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -controller.getLeftY(), () -> -controller.getRightX()));

    // A panic button: hold to stop the drivetrain regardless of the sticks.
    controller.leftBumper().whileTrue(drive.stopCommand());
}
```

**OperatorBindings** (add `import frc.robot.Constants;`)

```java
public OperatorBindings(
    ElevatorSubsystem elevator,
    ShoulderSubsystem shoulder,
    Flywheels flywheels,
    RollerSubsystem roller,
    CommandXboxController controller,
    Command scoreCommand) {

    controller.a().whileTrue(flywheels.spinUpCommand());
    controller.b().whileTrue(roller.intakeCommand());
    controller.x().whileTrue(roller.ejectCommand());
    controller.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    controller.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
    controller.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));
    controller.rightBumper().whileTrue(scoreCommand);

    roller.hasGamePieceTrigger.and(controller.rightTrigger()).debounce(0.1).onTrue(scoreCommand);
}
```

**RobotContainer.configureBindings** collapses to:

```java
private void configureBindings() {
    // Every binding that used to be here now lives with the human it belongs to.
    // Nothing about the robot's behaviour changed — this is a pure refactor, and
    // the fact that lessons 07 to 13's rubrics still pass is the proof.
    new DriverBindings(drive, driver);
    new OperatorBindings(elevator, shoulder, flywheels, roller, operator, scoreCommand());
}
```

and `configureDefaultCommands()` keeps only the flywheels' idle behaviour, since the
drivetrain's moved into `DriverBindings`.

Add the two imports at the top of `RobotContainer`.

</details>
