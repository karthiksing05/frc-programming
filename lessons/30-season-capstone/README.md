# Lesson 30 — Season capstone

> **Stage 2D · ~180 minutes · Prerequisite: 29-state-machines**

!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.

No instructions. That is the point.

Pick a game — Reefscape, Crescendo, or the current season — decide on a strategy, and
build a robot to it. Everything in this curriculum is available; nothing is
prescribed.

## What you'll learn

Nothing this curriculum can teach you directly. What you find out is whether the
previous twenty-nine lessons became knowledge or stayed as notes — and the only way
to find that out is to build something nobody wrote instructions for.

## What a finished capstone has

**A working robot in simulation.** Swerve, at least three mechanisms, teleop that a
human can actually drive.

**Three autonomous routines**, each one bounded and each one tested. Not one routine
with three branches — three routines, so that a bad field position has an answer.

**Vision-corrected pose estimation**, if you did Stage 2C.

**A dashboard you would actually use.** Not every signal you have; the ten you would
want with four minutes left in the pit.

**A test suite.** Not one per lesson — one per behaviour you would be embarrassed to
break. Write them as you go, not at the end.

**A README explaining your design choices**, including the ones you would do
differently. This is the hardest and most valuable part.

## How to run it

Treat it like a real build season, compressed.

**Decide the strategy first.** What does this robot do, and what does it deliberately
not do? A robot that does two things reliably beats one that does five badly. Write
it down before you write code, and re-read it whenever you are tempted to add
something.

**Build the drivetrain first, and make it good.** Everything else depends on it, and
a drivetrain you do not trust poisons every debugging session downstream.

**One mechanism at a time, all the way to done.** Subsystem, IO layer, telemetry,
tests, bindings. Do not start the second until the first is finished — half-built
mechanisms interact in ways that are very hard to debug.

**Autos last.** They compose everything else. Building them early means rebuilding
them.

**Log everything you tune, as you tune it.** Future-you will want to know why `kP`
is 12.

## Done?

Somebody who has not seen it can clone your project, run the simulator, drive the
robot, run an auto, and understand your README without asking you a question.

## After this

You are ready to contribute to a real season without supervision. Concretely:

- You can read Presto and Kelpie and understand what they are doing.
- You can add a subsystem to an existing codebase without breaking anything.
- You can debug from a log rather than by guessing.
- You can review someone else's code and say something useful.

The remaining gap is experience, and the only source of that is a season.

Two things worth doing next: read a full competition codebase end to end, and teach
lesson 04 to someone who has not done it. Teaching is the fastest way to find out
what you only *thought* you understood.

## What you'll do

See the walkthrough above.


```bash
./tools/frcprog next
```
