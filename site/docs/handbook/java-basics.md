# Java basics for FRC

This is the subset of Java that robot code actually uses, explained in robot terms.
It is not a full Java course, and it deliberately skips a great deal of the language
you will never touch on a robot.

Read it start to finish if you have never programmed, or jump to whichever piece just
confused you in a lesson.

---

## Values and types

Every value in Java has a *type*, and you have to say which one when you create it.

```java
double speed   = 0.6;      // a number with a decimal point
int    port    = 5;        // a whole number
boolean loaded = true;     // true or false, nothing else
String name    = "roller"; // text
```

On a robot the choice is almost always between `double` and `int`, and the rule is
simple: **`double` for anything measured, `int` for anything counted.** Distances,
angles, volts and speeds are measured. Port numbers, encoder ticks and game pieces held
are counted.

Getting this wrong has a specific and nasty failure mode, because Java throws away the
remainder when it divides two whole numbers:

```java
int a = 5, b = 2;
System.out.println(a / b);            // 2      not 2.5 — the .5 is discarded
System.out.println(a / (double) b);   // 2.5    forcing one side to double fixes it
System.out.println(5.0 / 2);          // 2.5    so does writing 5.0 in the first place
```

Nothing warns you about the first line. The robot simply drives slightly short, every
time, and it looks like a mechanical problem.

## Methods

A method is a named piece of work that takes some values in and hands one back.

```java
// "public"  anyone may call it
// "static"  it belongs to the class, not to a particular object
// "double"  the type of the value it hands back
//                        ┌─ parameters: the values it needs
public static double applyDeadband(double value, double threshold) {
  if (Math.abs(value) < threshold) {
    return 0.0;        // hand back zero and stop here
  }
  return value;        // otherwise hand back what we were given
}
```

You call it by name, passing values in the same order:

```java
applyDeadband(0.05, 0.1);   // 0.0
applyDeadband(0.80, 0.1);   // 0.8
```

A method that hands nothing back is declared `void`, which is most of the methods you
will write on a robot — they exist to *do* something rather than to compute something.

```java
public void stop() {
  motor.set(0.0);      // no return statement needed
}
```

## Classes, objects and fields

A class is a description of a thing; an object is one actual instance of it. On a robot
there is usually one class per mechanism, and exactly one object of it.

```java
public class RollerSubsystem {

  // Fields: the data this object owns and keeps between calls.
  private final PWMSparkMax motor = new PWMSparkMax(5);
  private State state = State.OFF;

  // A method that changes the object's own data.
  public void setMode(State desired) {
    state = desired;
  }
}
```

`new RollerSubsystem()` builds one. The `motor` field belongs to that object, and lives
for as long as it does.

### `private` and `public`

`private` means only code inside this class can touch it. `public` means anybody can.

This matters more on a robot than it does in most software, because a motor with two
different pieces of code writing to it fifty times a second behaves unpredictably —
whichever line ran last wins. Making the motor `private` means the compiler enforces
that only the roller can command the roller.

```java
private final PWMSparkMax motor = ...;   // nothing outside this class can call motor.set()
public void setMode(State s) { ... }     // this is how the outside world asks for things
```

### `final`

`final` means the value can never be reassigned after it is set, and the compiler
refuses to compile code that tries.

```java
private final PWMSparkMax motor = new PWMSparkMax(5);
motor = new PWMSparkMax(6);   // compiler error — and that is the point
```

Use it on essentially every field that holds hardware. There is no situation where a
subsystem should swap out which motor it owns while the robot is running.

### `static`

`static` attaches something to the class itself rather than to any object of it. There
is one copy, shared by everything, and you reach it through the class name.

```java
public final class Constants {
  public static final double DEADBAND = 0.10;
}

Constants.DEADBAND      // no "new Constants()" anywhere — there is only ever one
```

Constants are the main place you will write it. `public static final` together means
"one shared value, readable by anyone, that nobody can change".

## Enums

An enum is a type with a fixed, listed set of values.

```java
public enum State {
  OFF,
  INTAKING,
  EJECTING
}
```

The reason robot code uses these constantly is that the obvious alternative — a pile of
booleans — allows states that cannot physically exist:

```java
boolean isIntaking = true;
boolean isEjecting = true;   // the roller is now spinning both ways at once?
```

An enum has exactly the values you listed and no others, so the impossible combination
is not expressible. This idea is worth remembering by name: **make illegal states
unrepresentable.**

## Making decisions

```java
if (operator.getXButton()) {
  roller.setMode(State.EJECTING);
} else if (operator.getBButton()) {
  roller.setMode(State.INTAKING);
} else {
  roller.setMode(State.OFF);
}
```

Java takes the **first** branch that is true and skips the rest, which means the order
you write them in *is* a priority order. If X and B are held at once, this code ejects,
because X is checked first. Reverse the two branches and the same button press does
something different.

A `switch` is tidier when you are choosing between the values of an enum, and modern
Java lets it produce a value directly:

```java
double output = switch (state) {
  case OFF      -> 0.0;
  case INTAKING -> 0.6;
  case EJECTING -> -0.6;
};
```

This form has a real advantage over an `if` chain: if somebody adds a `JAMMED` state to
the enum next season, the compiler will point at every switch that no longer handles
every case. An `if`/`else` chain just quietly falls through to the `else`.

## Lambdas, and the most common bug in robot code

A **lambda** is a small unnamed function you can pass around like a value.

```java
() -> controller.getLeftY()      // "a thing that, when asked, returns the stick value"
```

Read the arrow as *"goes to"*. The empty brackets mean it takes no arguments.

A **method reference** is shorthand for a lambda that just calls one method:

```java
controller::getLeftY             // exactly the same thing, written shorter
```

### `Supplier<T>` and why this matters

A `Supplier<Double>` is Java's name for "something you can ask for a `double`, over and
over". Command-based robot code takes joystick inputs this way, and the reason is the
single most common subtle bug beginners hit.

```java
// WRONG — reads the stick once, at the moment the command is built,
//         and then drives at that value forever.
drive.arcadeDriveCommand(controller.getLeftY(), controller.getRightX());

// RIGHT — hands the command a way to ask for the stick value,
//         so it reads a fresh one every single loop.
drive.arcadeDriveCommand(() -> controller.getLeftY(), () -> controller.getRightX());
```

The wrong version compiles without complaint and looks correct. Commands are usually
built once, at startup, while the robot is sitting still — so the value captured is
`0.0`, and the robot never moves at all no matter what the driver does.

The rule that avoids it: **if a value changes over time, pass a way to read it, not the
value.**

??? question "Spot the bug"

    ```java
    public Command scoreHigh() {
      double target = elevator.getHeight() + 0.5;
      return elevator.goToCommand(target);
    }
    ```

    This method returns a command that raises the elevator half a metre above where it
    currently is. What actually happens when the operator presses the button twice?

    ---

    Nothing the second time, and probably nothing the first time either.

    `scoreHigh()` runs once, when the bindings are set up at startup. At that moment
    the elevator is at `0.0`, so `target` is fixed forever at `0.5` — it is a `double`,
    captured at construction, not a live reading.

    Press the button at any height and the elevator goes to `0.5`, not half a metre
    higher. The method reads like "half a metre above here" and behaves like "go to
    0.5".

    The fix is the same as above: take a supplier, or compute the target inside the
    command when it actually starts running, using `Commands.defer` or a
    `runOnce` that reads the height at that moment.

## The Java that WPILib leans on

A few pieces of modern Java show up in WPILib code that you should recognise even if
you never write them.

**`var`** lets the compiler work out the type from the right-hand side. It is a
shorthand, nothing more.

```java
var pose = new Pose2d();          // identical to: Pose2d pose = new Pose2d();
```

**Records** are short classes whose only job is to hold data.

```java
public record Waypoint(double xMeters, double yMeters) {}
```

That one line gives you a constructor, accessor methods, and sensible equality, where
writing it out by hand would take about forty. Records are also immutable — once built,
the values cannot change.

That last property is the reason this curriculum's own `DriveIOInputs` is a plain class
rather than a record, which is worth knowing before you assume records are always the
better choice. It gets filled in fifty times a second for a whole match, and refilling
one object in place creates no garbage for the collector to clean up, whereas building
a fresh record every loop creates a great deal. Read the comment on it in
`DriveIO.java` when you get to [lesson 16](../learn/stage2a/16-io-layer/index.md).

**Generics** are the angle brackets, and they say what a container holds.

```java
List<Pose2d> waypoints;       // a list, and everything in it is a Pose2d
Supplier<Double> stick;       // something that supplies a Double
```

You need to be able to read them. You will rarely need to write your own.

---

## Where each of these first appears

If you would rather learn these in the order the curriculum introduces them, each one
arrives as the answer to a problem you have just run into:

- Methods — [Lesson 01](../learn/stage1a/01-methods/index.md)
- Types, `static`, `final` — [Lesson 02](../learn/stage1a/02-variables-and-types/index.md)
- `if`/`else` and branch order — [Lesson 03](../learn/stage1a/03-conditionals-in-teleop/index.md)
- Classes, fields, `private`, enums — [Lesson 04](../learn/stage1b/04-subsystems-state-machines/index.md)
- Lambdas and `Supplier` — [Lesson 07](../learn/stage1c/07-tank-drive/index.md)

For the language itself, beyond what robots need, the
[Oracle Java tutorials](https://docs.oracle.com/javase/tutorial/) are the reference.
