# Lesson 30 — Season capstone

**Stage 2D · 180 min · Needs: 29**

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

There are deliberately no instructions for this one, because being handed a game and
having to decide what to build is the actual job.

Pick a game, decide a strategy, build a robot to it. Everything in this curriculum
is available. Nothing is prescribed.

## Do this, in this order

**1. Write the strategy first.** One page, before any code: what this robot does,
what it deliberately does not, and how it scores. Re-read it whenever you are
tempted to add something.

**2. Build the drivetrain and make it good.** Everything depends on it, and a
drivetrain you do not trust poisons every later debugging session.

**3. One mechanism at a time, all the way to done.** Subsystem, IO layer, telemetry,
tests, bindings. Do not start the second until the first is finished.

**4. Autos last.** They compose everything else. Build them early and you rebuild
them.

**5. Log what you tune, as you tune it.** Future-you will want to know why `kP` is 12.

## What finished looks like

- a working robot in simulation: swerve, three or more mechanisms, drivable teleop
- **three** autonomous routines, each bounded and tested, so a bad field position
  has an answer
- vision-corrected pose estimation, if you did Stage 2C
- a dashboard you would actually use: not every signal, the ten you would want with
  four minutes left in the pit
- tests for the behaviours you would be embarrassed to break, written as you go
- a README explaining your design choices, including what you would do differently

That last one is the hardest and most valuable part.

## Watch out for

**Starting five mechanisms and finishing none.** The most common way a capstone dies.

**Saving testing for the end.** Tests written afterwards test what the code does,
not what it should do.

**Scope that grows every time something works.** The strategy document exists to be
re-read.

## Done

Someone who has not seen it can clone your project, run the simulator, drive the
robot, run an auto, and understand your README without asking you anything.

## After this

You are ready to contribute to a real season unsupervised. You can read Presto and
Kelpie and follow them, add a subsystem without breaking anything, debug from a log
instead of guessing, and review someone else's code usefully.

The gap left is experience, and the only source of that is a season.

Two things worth doing next: read a full competition codebase end to end, and teach
lesson 04 to somebody who has not done it. Teaching is the fastest way to find out
what you only thought you understood.
