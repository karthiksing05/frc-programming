# Lesson 15 — Capstone

**Stage 1D · 90 min · Needs: all of Stage 1**

No new code. Find out what you already know.

## Do this

**1. Run everything:**

```bash
./tools/frcprog check --all
```

Anything red means a later lesson broke an earlier one. Fix it first.

**2. Drive it for a few minutes:**

```bash
./tools/frcprog sim
```

Keyboard 0 → Joystick[0], Keyboard 1 → Joystick[1], click Teleoperated. Then
actually operate it:

- Drive around. Does it stop when you let go?
- Intake with a piece present, then absent. Does the roller back off by itself?
- Send the elevator to each setpoint. Overshoot? Clean on the way down?
- Score. Does the roller wait for the shooter?
- Do two things at once. Does anything fight?

**3. Build a dashboard** in AdvantageScope you would actually use in a pit: drive
voltages, elevator height against setpoint, arm angle, flywheel target against
actual. Save the layout.

If you want a number that is not published, publish it. That instinct is the most
useful habit in this whole curriculum.

**4. Run both autos** from the chooser and watch on the 2D field view.

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

Scenario 5 catches the real bugs. Any one mechanism is easy to leave in a defined
state; the combination is where a missing `finallyDo` shows up.

If 5 fails while every earlier lesson passes, the bug is an *interaction*: two
commands fighting, a default command stomping something, a sequence assuming a
mechanism was already in place. Those are the interesting ones, and finding them on
a laptop is much cheaper than on a field.

## See it

Everything at once. That is the deliverable.

## Done

```bash
./tools/frcprog progress
```

Stage 1 complete. You can now:

- name and use subsystems, commands, triggers, requirements, factories, suppliers
- keep hardware `private final` and joystick reads inside lambdas
- put cross-subsystem coordination in one file
- reach for `waitSeconds` before `sleep`, and put a timeout on anything that waits
- spot an 80-line `teleopPeriodic`, a captured joystick value, or a toggle on a
  driver control

That list is roughly what a team's senior programmer wishes every new member
arrived knowing.

```bash
./tools/frcprog next
```

**Before you go on:** try lesson 05 again from scratch without the hints.
Re-solving something faster is how it settles.
