# Lesson 26 — Physics simulation

**Stage 2C · 55 min · Needs: 25**

!!! warning "Needs one online build"

    This lesson uses a vendor library. See `lessons/EXTENSIONS.md` for the
    four-step install. Everything else in the curriculum runs offline.

!!! note "Guided lesson"

    No rubric from here on. Clear goal, working code to copy from, and the
    simulator as your check. If it does what this page describes and you can point
    at the plot that proves it, you are done.

Everything so far has been kinematic. The robot passes through walls and game
pieces do not exist.

## Do this

1. Install the maple-sim vendordep
2. **Register the drivetrain with the arena and drive it.** Wall collisions are the
   simplest thing to verify.
3. Swap `ModuleIOSim` for `ModuleIOMapleSim`
4. Add **one** game piece and drive into it:

```java
SimulatedArena.getInstance().addGamePiece(new CrescendoNoteOnField(new Translation2d(2, 2)));
```

5. Confirm the intake's beam-break triggers because a physical object physically
   got there

## What it catches

- autos that assume you can push through a wall
- intakes that "work" in sim because the code says they do
- paths tuned on a frictionless model that are wrong on carpet
- robot-on-robot contact, which is most of a real match

## What it costs

Physics is more expensive than kinematics, so loop times go up. And it is another
vendordep to maintain across seasons.

Kelpie ships both `ModuleIOSim` and `ModuleIOMapleSim` for exactly this reason: the
cheap one for control-loop work, the expensive one for validating an auto. That the
choice is a one-line swap is the IO layer paying off again. This is the fourth
implementation of the same interface, and nothing above the line changed.

## Watch out for

**Registering the drivetrain twice**, or leaving the old `ModuleIOSim` running, so
two models fight over the same state.

**Mass or moment of inertia far from reality**, giving collisions that look wrong in
a way that is hard to name.

**Expecting your existing autos to still work.** They will need re-tuning. That is
the lesson.

## Done

The robot collides with walls, driving into a piece triggers the intake, and you
can be pushed off a target pose.

```bash
./tools/frcprog next
```
