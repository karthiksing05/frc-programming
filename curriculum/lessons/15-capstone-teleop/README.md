# Lesson 15 — Capstone: a complete teleop robot

> **Stage 1D · ~90 minutes · Prerequisites: all of Stage 1**

No new concepts. That is the point.

Fourteen lessons ago you could not write a method. You now have a robot with five
subsystems, a drivable teleop, two autonomous routines, a scoring sequence, live
telemetry, and a test suite that proves all of it. Today you check that it works as
one machine rather than as five things that each work.

## What you'll learn

Nothing new. You will find out what you already know, which is a different and more
useful thing.

## What you'll do

### 1. Run the whole rubric

```bash
./tools/frcprog check --all
```

Everything from lesson 01 forward. If anything is red, fix it — a rubric that used
to pass and now does not means a later lesson broke an earlier one, which is exactly
the kind of thing a capstone is for finding.

### 2. Drive it

```bash
./tools/frcprog sim
```

Drag **Keyboard 0** onto **Joystick[0]** and **Keyboard 1** onto **Joystick[1]**.
Click **Teleoperated**. Then actually operate it for a few minutes:

- Drive around. Does it stop when you let go?
- Intake with a game piece present, then absent. Does the roller back off by itself?
- Send the elevator to each setpoint. Does it overshoot? Come back down cleanly?
- Score. Does the roller wait for the shooter?
- Do two things at once. Does anything fight?

### 3. Instrument it

In AdvantageScope, build a dashboard you would actually use in a pit: drive
voltages, elevator height against setpoint, arm angle, flywheel target against
actual. Save the layout.

If you find yourself wanting a number that is not published, publish it. That
instinct — "I cannot see what I need" → "so I will make it visible" — is the single
most useful habit in this entire curriculum.

### 4. Run both autos

Select each from the chooser, click **Autonomous**, and watch on the 2D field view.

## Run it

```bash
./tools/frcprog check 15-capstone-teleop
```

Five integration scenarios, each one a thing a driver would really do:

1. **Drive** — push the stick, the robot moves; release, it stops.
2. **Intake** — hold B until the beam breaks; the roller backs off on its own.
3. **Score** — the full sequence runs and returns everything to idle.
4. **Autonomous** — a routine is selectable and runs to completion.
5. **No undefined state** — do several things at once, release everything, and every
   mechanism settles into a defined idle.

Scenario 5 is the one that catches real bugs. Any single mechanism is easy to leave
in a defined state; the combination is where a missing `finallyDo` or a default
command you forgot shows up.

If scenario 5 fails while every earlier lesson passes, the bug is in an
*interaction* — two commands fighting, a default command stomping something, a
sequence assuming a mechanism was already where it wanted. Those are the interesting
bugs, and finding them on a laptop is enormously cheaper than finding them on a
field.

## See it

Everything, at once, live. That is the deliverable.

## Done?

```bash
./tools/frcprog progress
```

Stage 1 complete.

## What you can now do, plainly

**Vocabulary you own:** subsystem, command, trigger, scheduler, requirement,
factory, supplier, PID, feedforward, deadband, telemetry, trajectory, kinematics.

**Habits you have:** hardware is `private final`; joystick reads happen inside
lambdas; cross-subsystem coordination lives in one file; factories before subclasses;
`waitSeconds` before `sleep`; every waiting command has a timeout; numbers that mean
something have names.

**Anti-patterns you can spot:** a `teleopPeriodic` doing eighty lines of work; a
`motor.set()` outside any subsystem; captured joystick values; decision logic in a
default command; `toggleOnTrue` on a driver control; one subsystem calling another.

**Tools you can use unassisted:** WPILib VS Code, `./gradlew`, `frcprog`, the robot
simulator, AdvantageScope, and a test suite you trust enough to refactor against.

That list is, more or less, what a team's senior programmer wishes every new member
arrived knowing.

## What Stage 2 adds

- **2A** — the IO layer, logging discipline, and log replay: how to debug something
  that happened yesterday.
- **2B** — swerve and odometry: the drivetrain every competitive team runs.
- **2C** — vision: knowing where you are on the field.
- **2D** — motion profiling, system identification, and a season-scale capstone.

Some of Stage 2 needs a vendor library downloaded once; `lessons/EXTENSIONS.md` says
which, and why.

Before you go on: consider doing lesson 05 again, from scratch, without the hints.
Re-solving something you have already solved, faster and more cleanly, is how the
knowledge actually settles.
