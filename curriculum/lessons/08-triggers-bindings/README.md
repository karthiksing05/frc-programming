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

## How it works

### What a Trigger actually is

Not a button. A `Trigger` wraps a `BooleanSupplier` and watches it for **changes**.

Every loop the scheduler polls every trigger, compares the result to last loop's,
and fires the bindings that just became true or just became false. Edges, not levels.

That is why you write bindings once, in the constructor, and never poll anything.
`operator.a()` returns a Trigger; `.whileTrue(...)` registers what to do with it.

Because the input is any `BooleanSupplier`, buttons are only the easy case:

```java
new Trigger(() -> elevator.getHeightMeters() > 1.0)
new Trigger(() -> DriverStation.getMatchTime() < 30.0)
roller.hasGamePieceTrigger            // already on your roller
```

Lesson 11 combines them.

### The four semantics

| Method | Starts | Ends |
|---|---|---|
| `onTrue` | on press | when the command finishes **itself** |
| `whileTrue` | on press | on release |
| `onFalse` | on release | when the command finishes itself |
| `whileFalse` | while not held | on press |

The difference between the first two is entirely about **who decides when it is
over**.

**`whileTrue` for things you do.** Spinning a shooter, running an intake. The
human's thumb is the state, so they cannot lose track of it.

**`onTrue` for places you go.** "Go to L4" is a destination. Tap it, the elevator
finishes the journey by itself, and the operator's attention is free for something
else.

??? question "Predict: what happens if you bind the shooter with onTrue?"

    `spinUpCommand()` never finishes on its own. It holds the target speed until
    something cancels it.

    Bound with `onTrue`, the press starts it and nothing ever stops it. The shooter
    spins for the rest of the match, draining the battery and making noise, and
    releasing the button does nothing.

    Bound with `whileTrue`, release cancels it and `finallyDo` sets the target back
    to zero.

    The general question to ask of every command you write: **does this terminate on
    its own?** `goToCommand` does, when it arrives. `spinUpCommand` does not. That
    single property decides which binding is correct.

??? question "Predict: what happens if you bind the elevator with whileTrue?"

    The test taps Y and releases immediately. `whileTrue` cancels on release, so the
    elevator stops wherever it happened to be, usually halfway.

    In a match that is worse than not pressing it, because the operator believes
    they sent it and looks away.

### Not toggleOnTrue

WPILib supports it. This curriculum recommends against it for anything a human
presses, and so do the official docs: *"toggles are not a highly-recommended option
for user control, as they require the driver to keep track of the robot state."*

The failure is behavioural, not technical. Mid-match, a driver presses the intake
toggle. Did it register? They cannot see the roller from the driver station. They
press again, and now they genuinely do not know whether it is on or off.

Two buttons, or `whileTrue`, costs one button and removes the whole category. Check
4 fails if `toggleOnTrue` appears anywhere in `RobotContainer`.

??? info "Why startEnd matters for the roller"

    `roller.intakeCommand()` is built with `startEnd(...)`: one lambda on start, one
    on end.

    The end lambda runs however the command ends. Released button, cancelled by
    something higher priority, interrupted by a requirement conflict, robot
    disabled. All of them.

    Cleanup that only happens on the path you were thinking about is not cleanup.
    That is why the roller reliably returns to `OFF` and does not need anybody to
    remember to stop it.

## See it

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

Setup: **[Running the simulator](../../../setup/simulator.md)**.

1. Drag **Keyboard 1** onto **Joysticks** slot **1**
2. **DS → Keyboard 0 Settings** to find your A/B/X/Y keys
3. Click **Teleoperated**
4. In AdvantageScope plot `Flywheels/TargetRPM`

Hold your A key: the trace jumps to 3000. Release: it drops to 0. That is
`whileTrue`.

Tap your Y key and let go immediately. Open **Hardware → PWM Outputs** and watch
channel 6: the elevator keeps driving after you released. That is `onTrue`.

Keep the **Joysticks** panel visible while you do this. If a button does not light
up there, the robot never saw the press and no amount of binding will help.

## Done

Rubric is green.

```bash
./tools/frcprog next
```
