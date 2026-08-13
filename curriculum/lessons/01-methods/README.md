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

## How it works

### Where the noise comes from

An Xbox stick is two potentiometers, each a resistive strip with a wiper on it. The
controller reads them with an 8-bit ADC and sends the result over USB. Cheap strip,
cheap converter, and the spring never returns the stick to exactly the same spot.

The result is a stick nobody is touching reporting something between −0.08 and
+0.08, wandering. Your drivetrain does not know that number is meaningless. It just
obeys.

At 12 volts a 0.05 command is 0.6 V. That is enough to creep on a smooth floor and
not enough to be obvious in the pit, which is the worst combination.

### What a deadband actually does

It is a piecewise function. One flat region around zero, pass-through outside it.

```
output
   ^
 1 |                    ____/
   |                   /
   |                  /
 0 |____________-----
   |          /
   |         /
-1 |____/
   +--------|---|--------> input
          -0.1  0.1
```

Everything inside ±0.1 becomes exactly zero. Everything outside is untouched.

Notice what it costs you: the first 10% of stick travel now does nothing. That is a
real trade. Too small a band and the robot still creeps; too large and the driver
loses fine control. FRC teams land between 0.05 and 0.15, and 0.10 is the common
default.

??? question "Predict: why does `Math.abs` matter here?"

    Without it you would write `if (value < threshold)`. That is true for `0.05`,
    which is what you wanted. It is also true for `-0.8`, which is a driver asking
    for full reverse.

    Your robot would drive forward normally and refuse to reverse at all. And a test
    that only tried positive numbers would pass.

    That is exactly what rubric check 2 exists to catch.

??? info "Why `<` and not `<=`"

    A reading of exactly `0.1` against a band of `0.1` is the smallest input a
    driver could deliberately produce. Eating it means the robot ignores the first
    thing they ask for.

    It almost never matters in practice, because floating point rarely lands exactly
    on the boundary. It matters for how you think about boundaries, which is the
    part that transfers.

### Why a method and not two lines inline

You will need this in the drivetrain, the turret, a test, and an auto routine.
Written inline, that is four copies. Change the threshold and you will find three of
them.

The method also gives the idea a name. `applyDeadband(y, 0.1)` says what is
happening. `if (Math.abs(y) < 0.1) y = 0;` makes the reader work it out.

## See it

```bash
./tools/frcprog sim
```

There is no drivetrain until lesson 07, so there is nothing to drive yet. What this
proves is that your code compiles and loads into a running robot program, which is
worth confirming once.

Full walkthrough of the simulator and AdvantageScope:
**[Running the simulator](../../../setup/simulator.md)**.

??? example "Experiment: feel the numbers"

    Skip if you are short on time. Two minutes if you are not.

    1. Start the sim and bind a controller (see the simulator guide)
    2. Open **Hardware → PWM Outputs** and watch the numbers
    3. Do not touch the sticks. Watch them drift.

    With a real Xbox controller you will see the last digits move constantly. With a
    keyboard you will not, because keyboard axes are exactly −1, 0 or 1.

    That drift is the entire problem you just solved.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

WPILib ships `MathUtil.applyDeadband`, which is your method plus a scaling option so
the output ramps from zero at the band edge instead of jumping. Kelpie and Presto
both call it several times a second all season. You did not write a toy.
