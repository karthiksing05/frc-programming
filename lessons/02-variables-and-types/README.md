# Lesson 02 — Variables and types

**Stage 1A · 30 min · Needs: 01**

In lesson 01 you wrote a deadband that took `0.1` as a parameter. That number is going
to be needed by the drivetrain, by the test that checks the drivetrain, and by the
autonomous routine. If you type `0.1` into each of them, you now have three copies of
one decision, and the day a driver asks you to make the sticks less twitchy you will
change two of them and spend the rest of practice wondering why the robot still feels
wrong.

This lesson is about giving numbers names so that there is exactly one copy.

## Do this

Open `src/main/java/frc/robot/Constants.java` and find the two `TODO (LESSON 02)`
comments. Four numbers are currently placeholders. Replace them:

```java
public static final class Drive {

  // 0.0 means "no deadband at all" — the exact bug lesson 01 taught you to fix.
  public static final double DEADBAND  = 0.10;   // ignore stick readings smaller than this

  // The most voltage we will ever command.
  public static final double MAX_VOLTS = 12.0;   // a fresh battery is about 12 V

  // Motor turns per wheel turn, decided by whichever gearbox mechanical bolted on.
  public static final double GEAR_RATIO = 8.45;  // stock AndyMark ToughBox Mini
}

public static final class Flywheels {

  // How fast this motor spins with 12 V and nothing loading it.
  public static final double MAX_RPM = 5800.0;   // "free speed", from the motor's datasheet
}
```

Keep the names exactly as they are, and keep `public static final` on every line. The
rubric checks the modifiers as well as the values, for a reason the next section gets
into.

## Spot the bug

Somebody on your team writes this while cleaning up. It compiles, and it runs.

```java
public static final class Drive {
  public static final double GEAR_RATIO = 8.45;
}

public class Drivetrain {
  private double gearRatio = Constants.Drive.GEAR_RATIO;

  public void useNewGearbox(double ratio) {
    gearRatio = ratio;              // mechanical swapped the gearbox mid-competition
  }

  public double wheelRotations(double motorRotations) {
    return motorRotations / gearRatio;
  }
}
```

Nothing here is a syntax error, and every test of `wheelRotations` passes. What is
wrong with it?

??? success "Answer"

    `GEAR_RATIO` is a constant, but `gearRatio` is a copy of it, and the copy is not
    final. The moment anything calls `useNewGearbox`, this class and the rest of the
    robot disagree about the gear ratio — and nothing anywhere reports a problem.

    Autonomous, which reads `Constants.Drive.GEAR_RATIO` directly, will now compute
    different distances than the drivetrain does. The robot will drive the wrong
    distance in a way that is completely invisible in the code, because both numbers
    are "correct" in their own file.

    The fix is not to make `gearRatio` final. It is to delete it and read
    `Constants.Drive.GEAR_RATIO` at the point of use, so there is one copy and one
    place to change it.

    This is the actual reason constants exist. Not tidiness — the elimination of
    disagreement.

## Check it

```bash
./tools/frcprog check 02-variables-and-types
```

The rubric checks the four values, confirms every constant is `public static final`
using reflection, and confirms nothing subsystem-specific leaked to the top level of
`Constants`.

## How it works

### What the three words buy you

```java
public static final double DEADBAND = 0.10;
```

`public` lets any class read it, which is the entire point of putting it here.

`static` attaches it to the class rather than to an object, so there is one `DEADBAND`
in the whole program and you never write `new Constants.Drive()`. Without `static`,
every class that wanted the number would need its own instance of a class that holds
nothing but numbers.

`final` stops reassignment, and the compiler enforces it. This is the one that earns
its keep at eleven at night at a competition, when somebody is editing quickly and
autocomplete offers `Constants.Drive.DEADBAND =`. A constant that is not `final` is
just a global variable that nobody has changed yet.

??? question "Predict the output"

    Given `DEADBAND = 0.10`, what does each line print?

    ```java
    System.out.println(MathUtils.applyDeadband(0.05, Constants.Drive.DEADBAND));
    System.out.println(MathUtils.applyDeadband(-0.30, Constants.Drive.DEADBAND));
    System.out.println(MathUtils.applyDeadband(0.10, Constants.Drive.DEADBAND));
    ```

    Work all three out before opening this.

    ---

    `0.0` — 0.05 is smaller than the deadband, so it is treated as noise.

    `-0.3` — past the deadband, and the sign is preserved. A deadband that returned
    `0.3` here would send the robot the wrong way.

    `0.0` — and this one is worth arguing about. Lesson 01's rubric uses
    `Math.abs(value) < threshold`, so a reading exactly at the threshold is *not*
    smaller than it and passes through as `0.10`. If you wrote `<=`, you got `0.0`.

    Both are defensible, and no driver will ever notice the difference. But you should
    know which one you wrote, because this is the kind of boundary that produces a
    "sometimes it does not work" bug report when it matters.

### Why the rubric uses reflection

Check 3 asks the JVM to describe each field and confirms all three modifiers are
present, rather than just reading the value.

Values alone would not catch somebody dropping `final`. The number would still be
`0.10`, every behavioural test would still pass, and the problem would stay invisible
until the day something assigned to it. When the property you care about is the
*shape* of the code rather than its output, reflection is how a test reaches it.

### Why nested classes

```java
public final class Constants {
  public static final class Drive { ... }
  public static final class Elevator { ... }
}
```

Call sites end up reading like sentences: `Constants.Elevator.MAX_HEIGHT_METERS` tells
you what the number is and which mechanism owns it without a comment.

The second payoff shows up later. Adding a subsystem never disturbs anybody else's
code — you add a nested class and stop. In one flat namespace, `MAX_HEIGHT` from the
elevator and `MAX_HEIGHT` from next year's climber collide, and somebody resolves it by
naming one `MAX_HEIGHT_2`, which is how a file starts rotting.

Presto and Kelpie both go a step further and keep each group in its subsystem's own
folder. Either layout works. What nobody does successfully is scatter loose numbers
through the code.

### Types, and one trap

Use `double` for anything measured — distances, angles, volts, speeds. Use `int` for
anything counted — port numbers, encoder ticks per revolution, game pieces held.

The trap is that Java truncates integer division instead of rounding it:

```java
int a = 5, b = 2;
System.out.println(a / b);          // 2, not 2.5
System.out.println(a / (double) b); // 2.5
```

Store a distance in centimetres as an `int` because it looks tidy, divide it somewhere,
and the robot ends up consistently a little short. Nothing warns you, no test fails
unless somebody wrote one, and the error is small enough to look like a mechanical
problem.

??? info "When is a number NOT worth naming?"

    `(a + b) / 2` is an average, and naming the `2` helps nobody.

    Two questions decide it. Could this value ever change? And if it changed, would I
    have to hunt for copies?

    For `0.1` as a deadband, both answers are yes. For `8.45` as a gear ratio,
    emphatically yes — that is a physical part that can be swapped between matches.
    For the `2` in an average, both answers are no, because it is arithmetic rather
    than a decision.

## See it

Naming a number does not change it, so nothing about the robot behaves differently
yet. You can prove the constants are real and connected, though, and it takes about a
minute.

Open `Robot.java`, find the last line of `teleopPeriodic()`, and change it temporarily:

```java
rollerMotor.set(Constants.Roller.INTAKE_SPEED);   // was: rollerMotor.set(0.0);
```

Then:

```bash
./tools/frcprog sim
```

In the Simulation GUI, set the mode to **Teleoperated**, then find the **PWM** panel.
Port 5 is the roller. You should see it holding at **0.60** — the exact value of
`Constants.Roller.INTAKE_SPEED`.

Change the constant to `0.25`, save, restart the sim, and the same slider reads `0.25`.
That is the entire relationship between a named constant and something physical: one
number in one file, reaching a motor controller.

Put `rollerMotor.set(0.0)` back before you move on. Lesson 03 wants it as it was.

## Done

The rubric passes, and you have four named constants instead of four literals that
were going to be copied.

```bash
./tools/frcprog next
```

Lesson 03 has you write the roller logic the obvious way, directly in `Robot.java` —
and then lesson 04 makes you feel exactly why nobody keeps it there.
