# Lesson 08 — Joystick bindings & Triggers

> **Stage 1C · ~35 minutes · Prerequisite: 07**

Lesson 07 handled continuous input: a stick that always has a value, feeding a
command that always runs. Buttons are a different shape of thing — discrete events,
with a moment they go down and a moment they come up — and they want different
machinery.

## What you'll learn

1. Bind a command to a button with `.onTrue` and `.whileTrue`.
2. Tell the four trigger semantics apart, and pick the right one.
3. Explain, in words, why `toggleOnTrue` is a poor fit for driver controls.

## What you'll do

Open `RobotContainer.configureBindings()` and wire the operator's controls:

```java
operator.a().whileTrue(flywheels.spinUpCommand());
operator.b().whileTrue(roller.intakeCommand());
operator.x().whileTrue(roller.ejectCommand());
operator.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
operator.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
operator.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));
```

`operator` is a `CommandXboxController`. Every button on it is a `Trigger` — an
object representing "this condition, over time" — rather than a boolean you have to
remember to poll.

### The four semantics

| Method | Fires | Ends |
|---|---|---|
| `onTrue` | when the condition becomes true | when the command decides it is done |
| `whileTrue` | when it becomes true | when the condition goes false |
| `onFalse` | when it becomes false | when the command is done |
| `whileFalse` | while it is false | when it becomes true |

Choosing between the first two is a question about the mechanism, not about the
code.

**`whileTrue` for things the human is doing.** Spinning a shooter, running an
intake. Hold to act, release to stop. The driver's thumb *is* the state, so they can
never lose track of it.

**`onTrue` for destinations.** "Go to L4" is a place, not an activity. The operator
taps it, the elevator finishes the journey by itself, and the operator's attention
is freed for something else. Bind that with `whileTrue` and letting go of the button
abandons the elevator wherever it happened to be — usually halfway, usually at the
worst moment.

Rubric check 3 taps Y and releases it immediately, then verifies the elevator still
arrives.

### Why not `toggleOnTrue`

WPILib supports it. This curriculum recommends against it for anything a human
presses, and so do the official docs: *"toggles are not a highly-recommended option
for user control, as they require the driver to keep track of the robot state."*

The failure is behavioural, not technical. Mid-match, a driver presses the intake
toggle. Did it register? They cannot see the roller from the driver station. They
press again — and now they have turned it off, or on, or off, and they genuinely do
not know which. A held button has no such ambiguity.

Two separate buttons (intake / eject) or `whileTrue` costs one button and removes an
entire category of driver error. Rubric check 4 fails if `toggleOnTrue` appears
anywhere in `RobotContainer`.

## Run it

```bash
./tools/frcprog check 08-triggers-bindings
```

Four checks:

1. Holding A spins the flywheels up; releasing lets them coast.
2. Holding B intakes, holding X ejects, releasing both returns the roller to OFF.
3. **Tapping** Y sends the elevator to the high setpoint and it keeps going.
4. No `toggleOnTrue` on any human control.

## See it

```bash
./tools/frcprog sim
```

Drag **Keyboard 1** onto **Joystick[1]**. Open AdvantageScope and plot
`Flywheels/TargetRPM`. Hold the A key and watch it jump; release and watch it fall
to zero. Tap the Y key and watch the elevator climb without you holding anything.

## Done?

```bash
./tools/frcprog next
```

## What a Trigger actually is

Not a button. A `Trigger` wraps a `BooleanSupplier` — any condition at all — and
watches it every loop for changes.

Buttons are the easy case. But you can build one from anything:

```java
new Trigger(() -> elevator.getHeightMeters() > 1.0)
new Trigger(() -> DriverStation.getMatchTime() < 30.0)
roller.hasGamePieceTrigger
```

That last one already exists on your roller. Lesson 11 combines triggers with
`.and()`, `.or()` and `.debounce()`, and once you can do that, a whole class of
"robot does the right thing automatically" behaviour becomes one readable line
instead of a nest of `if` statements spread across `periodic` methods.
