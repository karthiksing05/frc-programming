# Lesson 01 — Methods

> **Stage 1A · ~25 minutes · Prerequisite: 0D**

Your robot is sitting still. Nobody is touching the controller. It creeps sideways
across the field anyway.

That is not a bug in your code — you have not written any yet. It is the joystick.
The potentiometers inside an Xbox controller are cheap, and the circuit reading
them is noisy, so a stick at rest reports something like ±0.05 rather than exactly
zero. Send that number to a drivetrain and the robot obeys it, because obeying is
all a drivetrain knows how to do.

Every FRC robot fixes this the same way, and today you write the fix.

## What you'll learn

1. Write a `public static` method with parameters and a return value.
2. Call a method on another class: `ClassName.method(args)`.
3. Recognise a piece of arithmetic that appears twice and pull it out into a name.
4. Use `Math.abs` to handle both signs of a number in one comparison.

## What you'll do

Open `src/main/java/frc/robot/util/MathUtils.java`. There is a method waiting for a
body:

```java
public static double applyDeadband(double value, double threshold) {
    // TODO (LESSON 01)
    return value;
}
```

Make it return `0.0` when the magnitude of `value` is below `threshold`, and return
`value` unchanged otherwise. That is a *deadband*: a zone around zero where the
robot deliberately ignores the human.

Read the signature before you write anything, because it tells you almost
everything:

- **`public`** — other classes may call it. Without this, only `MathUtils` could.
- **`static`** — it belongs to the class, not to an object. You call
  `MathUtils.applyDeadband(...)`, never `new MathUtils()`. It makes sense because
  this method needs no memory of anything: same inputs, same answer, forever.
- **`double`** (the first one) — what it hands back.
- **`(double value, double threshold)`** — what it needs from you.

The threshold is a *parameter*, not a fixed number, and that matters. A drivetrain
stick might want 0.10; a fine-adjustment trigger might want 0.02. One method, two
call sites, two thresholds.

## Run it

```bash
./tools/frcprog check 01-methods
```

The rubric checks five things:

1. A small positive reading (0.05 against a 0.1 band) becomes 0.0.
2. A small negative reading (−0.05) also becomes 0.0 — both signs, one rule.
3. A real push (±0.8) passes through untouched, sign included.
4. A different threshold is respected — 0.15 inside a 0.20 band is still zero.
5. Zero stays zero, and a reading exactly *at* the threshold passes through.

That last one is a genuine design decision, not a trick. Use `<`, not `<=`: a
reading exactly at the threshold is the smallest input the driver could
deliberately produce, and eating it would be rude.

## See it

```bash
./tools/frcprog sim
```

The simulator will not show you much yet — there is no drivetrain wired up until
lesson 07. What it does prove is that your code compiles and loads onto a running
robot program, which is worth seeing once.

The interesting view comes in lesson 10, when you plot a noisy stick trace against
its deadbanded output and watch one line jitter while the other sits flat.

## Done?

The rubric is green. Save your work, then:

```bash
./tools/frcprog next
```

## Why this is a real lesson and not a warm-up

Two reasons.

**You just wrote production code.** WPILib ships
[`MathUtil.applyDeadband`](https://github.com/wpilibsuite/allwpilib/blob/main/wpimath/src/main/java/edu/wpi/first/math/MathUtil.java)
and it is your method with a couple of extra features. Kelpie and Presto both call
it, several times a second, all season. You are not doing a toy version of the real
thing; you are doing the real thing, slightly smaller.

**This method does not disappear.** In lesson 07 you will call it from the
drivetrain. In lesson 02 the `0.1` becomes a named constant and you will change it
in one place. That compounding — each lesson using the last one — is the whole
design of this curriculum, and it starts here.
