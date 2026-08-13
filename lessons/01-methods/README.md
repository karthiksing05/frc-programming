# Lesson 01 — Methods

**Stage 1A · 25 min · Needs: 0D**

Your robot creeps across the field while nobody is touching the joystick.

## Do this

1. Open `src/main/java/frc/robot/util/MathUtils.java`
2. Find `TODO (LESSON 01)`
3. Make `applyDeadband` return `0.0` when `Math.abs(value) < threshold`,
   and return `value` unchanged otherwise.

## Check it

```bash
./tools/frcprog check 01-methods
```

| # | Input | Expected | Why |
|---|---|---|---|
| 1 | `0.05`, band `0.1` | `0.0` | inside the band |
| 2 | `-0.05`, band `0.1` | `0.0` | sticks drift both ways |
| 3 | `±0.8`, band `0.1` | unchanged | real input, keep the sign |
| 4 | `0.15`, band `0.20` | `0.0` | use the parameter, not a hard-coded 0.1 |
| 5 | `0.1`, band `0.1` | `0.1` | use `<`, not `<=` |

## Why

An Xbox stick at rest reads about ±0.05. The potentiometers are cheap and the
circuit is noisy. Send that to a drivetrain and the robot obeys it, because
obeying is all a drivetrain does.

A deadband ignores anything below a threshold. Write it once, call it from
everywhere a stick value enters your code.

The threshold is a parameter, not a fixed number. A drive stick wants 0.10. A fine
adjustment trigger wants 0.02.

## See it

```bash
./tools/frcprog sim
```

Not much to watch yet. There is no drivetrain until lesson 07. What it proves is
that your code compiles and loads into a running robot program.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**Worth knowing:** WPILib ships `MathUtil.applyDeadband`, which is your method plus
a couple of extras. Kelpie and Presto both call it several times a second all
season. You did not write a toy version.
