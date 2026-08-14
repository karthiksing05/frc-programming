# Lesson 01 — Methods

**Stage 1A · 25 min · Needs: 0D**

The robot is sitting still on the field, nobody is touching the controller, and it
starts creeping toward the wall. This happens to every team, and the cause is not the
robot — it is that a joystick at rest does not report exactly zero.

## Do this

Open `src/main/java/frc/robot/util/MathUtils.java` and find `TODO (LESSON 01)`. Write
`applyDeadband` so that it returns `0.0` when the reading is smaller than the
threshold, and returns the reading unchanged otherwise.

These are the calls the rubric will make, and what each one has to give back:

```java
applyDeadband(0.05,  0.1);   // 0.0    small enough to be noise, so throw it away
applyDeadband(-0.05, 0.1);   // 0.0    sticks drift both ways, so handle both signs
applyDeadband(0.8,   0.1);   // 0.8    a real request — pass it through untouched
applyDeadband(-0.8,  0.1);   // -0.8   and keep the sign, or the robot reverses wrongly
applyDeadband(0.15,  0.2);   // 0.0    use the threshold you were given, not a fixed 0.1
applyDeadband(0.1,   0.1);   // 0.1    exactly on the edge still counts — use <, not <=
```

The last two are the ones people get wrong. If you hard-code `0.1` inside the method
the fifth line fails, and if you write `<=` the sixth does.

## Check it

```bash
./tools/frcprog check 01-methods
```

## Spot the bug

Here is a version somebody wrote quickly. It compiles, and it passes four of the six
calls above.

```java
public static double applyDeadband(double value, double threshold) {
  if (Math.abs(value) < 0.1) {
    return 0.0;
  }
  return value;
}
```

Which two does it fail, and why is this the more dangerous kind of mistake?

??? success "Answer"

    It fails `applyDeadband(0.15, 0.2)`, which should be `0.0` but comes back as
    `0.15`, and it changes behaviour for any threshold that is not `0.1`.

    The method takes a `threshold` parameter and then ignores it, using a hard-coded
    `0.1` instead. Every call that happens to pass `0.1` works perfectly.

    That is what makes it dangerous. This bug is invisible for as long as the whole
    robot uses one deadband value. The day somebody adds a turret that needs a tighter
    band of `0.03`, the turret silently gets `0.1` instead — and the person debugging
    the turret will be reading turret code, not this method, because this method has
    a passing test suite and has worked all season.

    A parameter that is accepted and then not used is worth treating as a red flag
    whenever you see one.

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

There is no drivetrain until lesson 07, but you can wire your method to a motor right
now and watch it do its job. It takes about two minutes and it is worth doing, because
the difference is obvious on screen.

Open `Robot.java`, find the last line of `teleopPeriodic()`, and replace it with the
raw stick reading:

```java
rollerMotor.set(operator.getLeftY());          // no deadband yet — on purpose
```

Then start the simulator:

```bash
./tools/frcprog sim
```

Set the mode to **Teleoperated**. In the **Joysticks** panel, drag the *LeftY* axis
slider a tiny amount away from centre — somewhere around `0.05`. Now watch the **PWM**
panel: port 5 is following it exactly. On a real robot that is a motor turning while
nobody is asking for anything.

Now put your method in the way:

```java
rollerMotor.set(MathUtils.applyDeadband(operator.getLeftY(), 0.1));
```

VS Code will underline `MathUtils` in red, because `Robot.java` lives in the package
`frc.robot` and your method lives in `frc.robot.util` — a different folder, so Java
does not know the name yet. Add this line up with the other imports at the top of the
file:

```java
import frc.robot.util.MathUtils;
```

That is all an import is: telling this file where to find a name that lives somewhere
else. Every one of the six imports already at the top of `Robot.java` is doing the
same job for a WPILib class.

Restart the sim and drag the same slider to `0.05` again. The PWM output stays at
`0.00`. Push the slider past `0.1` and it starts following again, immediately and at
full value.

That flat region around zero is the thing you built. Set the line back to
`rollerMotor.set(0.0)` when you are done looking.

If you have a real Xbox controller plugged in, there is a second thing worth seeing:
leave the sticks completely alone and watch the raw axis value in the Joysticks panel.
The last digits wander on their own. That drift is not a fault, it is what every
controller does, and it is the entire reason this lesson exists.

The full tour of the simulator is in
**[Running the simulator](../../../setup/simulator.md)**.

## Done

The rubric passes and you have a method the rest of the robot can use.

```bash
./tools/frcprog next
```

WPILib ships its own `MathUtil.applyDeadband`, which is the method you just wrote plus
an option to ramp the output smoothly from zero at the band edge instead of jumping.
Kelpie and Presto both call it several times a second for an entire season, so this
was not a toy exercise — it is one of the most-executed lines of code on a real robot.
