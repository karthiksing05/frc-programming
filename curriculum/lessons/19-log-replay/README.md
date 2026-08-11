# Lesson 19 — Log replay

> **Stage 2A · ~50 minutes · Prerequisite: 18**
> **Extension lesson — needs one online build. See `lessons/EXTENSIONS.md`.**


!!! note "This is a guided lesson"

    Lessons 01–16 hand you a rubric and grade you. From here on, the work is
    open-ended: there is a clear goal, working code to model yourself on, and no
    automated grader. That is not a downgrade — it is what programming looks like
    once somebody stops writing exercises for you.

    Your check is the simulator and AdvantageScope. If the mechanism does what the
    lesson describes, and you can point at the plot that proves it, you are done.


"I think there is a bug at the exact moment the game piece hands off from the intake
to the indexer, but it only happened once and I cannot reproduce it."

Every team has this conversation. Replay is the answer, and it is the reason lesson
16 was worth the extra files.

## What you'll learn

1. What replay actually does, and what it cannot do.
2. Why the IO layer is a hard prerequisite rather than a nice-to-have.
3. How to add a logged value *after the fact* and see it in an old run.

## The idea

Your robot code is, at bottom, a pure function: inputs in, outputs out, fifty times
a second.

- **Inputs** — every sensor reading, every joystick axis, the match clock.
- **Outputs** — every voltage, every setpoint, every decision.

If all inputs arrive through `updateInputs` — which is exactly what lesson 16
enforced — then recording them records the robot's entire world. Feed that recording
back into the same code on a laptop and it re-runs the match *exactly*: same
decisions, same bug, at your desk, with a debugger, days later.

And here is the part that surprises people: you can **add a logged value that was
never recorded** and it appears in the replay. You are not replaying a video of what
was logged; you are re-executing the code. A value you wish you had logged is one
line and one replay away.

That is the entire pitch, and it is a very good one.

## What breaks it

One stray sensor read that bypasses the IO layer:

```java
// inside Drive.periodic()
if (gyro.getAngle() > 90) { ... }   // ✗ not from inputs
```

That line compiles, works on the robot, and quietly makes every log unreplayable —
because during replay there is no gyro, and the reading it returns has nothing to do
with the recorded match.

Replay is all-or-nothing. That is why lesson 16's discipline is stated absolutely.

## What you'll do

This lesson needs [AdvantageKit](https://docs.advantagekit.org/), which is a vendor
library and therefore a download. `lessons/EXTENSIONS.md` has the exact steps.

Once it is installed:

1. Convert one subsystem's inputs class to use `@AutoLog`, which generates the
   serialisation you would otherwise write by hand.
2. Switch `Robot` to extend `LoggedRobot` and register a `WPILOGWriter`.
3. Run a simulation, do something interesting, and stop. You now have a `.wpilog`.
4. Open it in AdvantageScope and scrub the timeline.
5. Re-run the robot in replay mode against that log.
6. **Add a new `Logger.recordOutput(...)` line, replay the same log, and watch the
   new value appear** — in a run that happened before the line existed.

Step 6 is the moment the idea lands.

## Done?

You have a log, you have replayed it, and you have retroactively added a signal.

```bash
./tools/frcprog next
```

## Honest limits

**It does not replay physics.** Replay re-runs your *code* against recorded inputs.
It cannot tell you what would have happened if you had commanded different voltages,
because the robot's response to those voltages was not recorded — it happened in the
real world.

**It needs discipline to stay true.** One bypassed sensor read, one `Math.random()`,
one dependence on wall-clock time, and the replay diverges from reality. The
divergence is silent.

**It is a debugging tool, not a test suite.** Useful for "what happened in match 34";
not a substitute for the unit tests you have been writing since lesson 01.

Used well it collapses the loop between "something went wrong at a competition" and
"I know why" from a week to an afternoon. That is worth a lot.
