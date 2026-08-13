# Lesson 15 — Capstone

**Stage 1D · 90 min · Needs: all of Stage 1**

No new code. Find out what you already know.

## Do this

**1. Run everything:**

```bash
./tools/frcprog check --all
```

Anything red means a later lesson broke an earlier one. Fix that first, in lesson
order, because earlier failures often cause later ones.

**2. Drive it for a few minutes.** Setup: **[Running the simulator](../../../setup/simulator.md)**.

```bash
./tools/frcprog sim
```

Bind Keyboard 0 to Joysticks 0 and Keyboard 1 to Joysticks 1, click
**Teleoperated**, then actually operate it:

| Try | Watch for |
|---|---|
| Drive around, then release | does it stop, or coast on a stale command? |
| Intake with DIO 4 high, then click it low | does the roller back off by itself? |
| Send the elevator to each setpoint | overshoot? clean on the way down? |
| Score | does the roller wait for the shooter? |
| Do two things at once | does anything fight for a subsystem? |

**3. Build a pit dashboard** in AdvantageScope:

- Line Graph: elevator height and setpoint
- Line Graph: flywheel target and actual
- Line Graph: drive left and right volts
- 2D Field: drive pose

**File → Save Layout.** You will reload this constantly from here on.

If you find yourself wanting a number that is not published, publish it. That
instinct, "I cannot see what I need, so I will make it visible", is the most useful
habit in this whole curriculum.

**4. Run both autos** from the chooser and watch on the 2D Field.

## Check it

```bash
./tools/frcprog check 15-capstone-teleop
```

Five scenarios, each one something a driver would really do:

| # | Scenario |
|---|---|
| 1 | Push the stick, robot moves. Release, it stops. |
| 2 | Hold B until the beam breaks. Roller backs off on its own. |
| 3 | Score runs end to end and returns to idle. |
| 4 | An auto is selectable and runs to completion. |
| 5 | Do several things, release everything, nothing is left running. |

## How it works

### Why scenario 5 is the one that finds bugs

Any single mechanism is easy to leave in a defined state. You wrote the command, you
wrote its cleanup, you tested it.

The combination is different. Scenario 5 does four things at once and then stops
asking for any of them. That exercises every cleanup path simultaneously, including
the ones that only run when a command is *interrupted* rather than finishing
normally.

If 5 fails while every earlier lesson passes, the bug is an **interaction**:

- two commands fighting over a subsystem
- a default command stomping something that was still needed
- a sequence assuming a mechanism was already in position
- a `run(...)` where `startEnd(...)` was wanted, so nothing undoes what it did

Those are the interesting bugs, and finding them on a laptop costs minutes. Finding
them on a field costs a match.

??? info "What to do if check --all shows old failures"

    Work in lesson order and re-run after each fix.

    | Failing | Look at |
    |---|---|
    | 01–02 | did you edit `MathUtils` or `Constants` since? |
    | 03–04 | `Robot.java` and `RollerSubsystem.periodic` |
    | 05–06 | gains in `Constants`, and that `appliedVolts` is set |
    | 07 | suppliers read inside the lambda |
    | 08–09 | bindings, and whether lesson 14 moved them |
    | 11 | default commands, and `.debounce()` placement |
    | 12–13 | is a routine still added to `autoChooser`? |

## See it

Everything at once, live. That is the deliverable.

## Done

```bash
./tools/frcprog progress
```

Stage 1 complete. Concretely, you can now:

**Name and use:** subsystem, command, trigger, scheduler, requirement, factory,
supplier, deadband, PID, feedforward, telemetry, trajectory, kinematics.

**Do by reflex:** keep hardware `private final`; read joysticks inside lambdas;
coordinate between subsystems in one file; reach for `waitSeconds` before `sleep`;
put a timeout on anything that waits on a sensor; give numbers names.

**Spot on sight:** an 80-line `teleopPeriodic`; a `motor.set()` outside a subsystem;
a captured joystick value; decision logic in a default command; `toggleOnTrue` on a
driver control; one subsystem calling another.

That list is roughly what a team's senior programmer wishes every new member arrived
knowing.

```bash
./tools/frcprog next
```

**Before Stage 2:** try lesson 05 again from scratch, without the hints. Re-solving
something faster and more cleanly is how it settles.
