# Lesson 19 — Log replay

**Stage 2A · 50 min · Needs: 18**

!!! warning "Needs one online build"

    This lesson uses a vendor library. See `lessons/EXTENSIONS.md` for the
    four-step install. Everything else in the curriculum runs offline.

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

"There is a bug at the moment the piece hands off, but it happened once and I
cannot reproduce it."

## The idea

Your robot code is a pure function. Inputs in, outputs out, fifty times a second.

If every input arrives through `updateInputs` (which lesson 16 enforced), then
recording them records the robot's entire world. Feed it back on a laptop and the
code re-runs the match exactly: same decisions, same bug, at your desk, with a
debugger, days later.

The part that surprises people: you can **add a logged value that was never
recorded** and it appears in the replay. You are not replaying a video of what was
logged. You are re-executing the code.

## Do this

Install AdvantageKit first (`lessons/EXTENSIONS.md`). Then:

1. Convert one subsystem's inputs class to `@AutoLog`
2. Make `Robot` extend `LoggedRobot` and register a `WPILOGWriter`
3. Run a simulation, do something interesting, stop. You have a `.wpilog`.
4. Open it in AdvantageScope and scrub the timeline
5. Re-run the robot in replay mode against that log
6. **Add a new `Logger.recordOutput(...)` line and replay the same log.** Watch the
   new value appear in a run that happened before the line existed.

Step 6 is the moment the idea lands.

Convert one subsystem, not all of them. Get one round trip working first.

## What breaks it

One sensor read that bypasses the IO layer:

```java
if (gyro.getAngle() > 90) { ... }   // not from inputs
```

That compiles, works on the robot, and quietly makes every log unreplayable. During
replay there is no gyro. Replay is all or nothing, which is why lesson 16's rule
was stated absolutely.

## Honest limits

**It does not replay physics.** It re-runs your code against recorded inputs. It
cannot tell you what would have happened if you had commanded different voltages.

**It needs discipline to stay true.** One bypassed read, one `Math.random()`, one
dependence on wall-clock time, and the replay diverges silently.

**It is a debugging tool, not a test suite.** Useful for "what happened in match
34". Not a substitute for the unit tests you have been writing since lesson 01.

## Done

You have a log, you replayed it, and you added a signal retroactively.

```bash
./tools/frcprog next
```
