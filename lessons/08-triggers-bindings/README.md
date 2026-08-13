# Lesson 08 — Buttons

**Stage 1C · 35 min · Needs: 07**

Sticks are continuous. Buttons are events. They want different machinery.

## Do this

Open `RobotContainer.java`, find `TODO (LESSON 08)` in `configureBindings()`:

```java
operator.a().whileTrue(flywheels.spinUpCommand());
operator.b().whileTrue(roller.intakeCommand());
operator.x().whileTrue(roller.ejectCommand());
operator.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
operator.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
operator.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));
```

## Check it

```bash
./tools/frcprog check 08-triggers-bindings
```

Four checks. Number 3 taps Y and releases immediately, then verifies the elevator
still arrives.

## onTrue or whileTrue

| Method | Starts | Ends |
|---|---|---|
| `onTrue` | on press | when the command finishes itself |
| `whileTrue` | on press | on release |
| `onFalse` | on release | when the command finishes |
| `whileFalse` | while not held | on press |

**`whileTrue` for things you do.** Spinning a shooter, running an intake. The
driver's thumb is the state, so they cannot lose track of it.

**`onTrue` for places you go.** "Go to L4" is a destination. Tap it, the elevator
finishes by itself, the operator's attention is freed. Bind that with `whileTrue`
and letting go abandons the elevator halfway.

## Not toggleOnTrue

WPILib supports it. Do not use it for human controls, and neither do the official
docs: *"toggles are not a highly-recommended option for user control, as they
require the driver to keep track of the robot state."*

Mid-match a driver presses the intake toggle. Did it register? They cannot see the
roller. They press again, and now they do not know whether it is on or off. A held
button has no ambiguity. Check 4 fails if `toggleOnTrue` appears anywhere.

## See it

```bash
./tools/frcprog sim
```

Drag **Keyboard 1** onto **Joystick[1]**. Plot `Flywheels/TargetRPM`. Hold A: it
jumps. Release: it falls. Tap Y: the elevator climbs without you holding anything.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**A Trigger is not a button.** It wraps any condition at all:

```java
new Trigger(() -> elevator.getHeightMeters() > 1.0)
new Trigger(() -> DriverStation.getMatchTime() < 30.0)
roller.hasGamePieceTrigger          // already exists on your roller
```

Lesson 11 combines them.
