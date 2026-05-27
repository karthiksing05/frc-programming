# Lesson 01 — Methods (Functions)

> **Stage 1A · ~25 minutes · No prerequisites**

In Java, a **method** (often called a "function" elsewhere) is a named,
reusable piece of behavior with **inputs** (parameters) and an **output**
(return value). Writing the same logic twice is a bug waiting to happen
— give it a name once, call it everywhere.

This lesson is the smallest possible meaningful method in robot code:
**joystick deadband**.

## The real-world problem

Xbox-controller sticks drift. Even untouched, they read ±0.05 because
the hardware sensors aren't perfect. If you forward those readings
straight to motors, the robot creeps across the field while the driver
is standing still — annoying at best, dangerous next to a teammate at
worst.

The fix is a **deadband**: ignore any reading whose magnitude is below
some threshold. Done in one place, called from everywhere a stick value
enters the robot code.

## What you'll do

You'll fill in one method body:

```java
// in src/main/java/frc/robot/util/MathUtils.java
public static double applyDeadband(double value, double threshold) {
    // TODO: return 0.0 if |value| < threshold, else value
    return value;
}
```

## Run it

```bash
./tools/frcprog.sh check 01-methods
# equivalently:
./gradlew lesson01
```

The rubric is a JUnit 5 test class
([`MathUtilsTest`](../../src/test/java/frc/robot/util/MathUtilsTest.java))
that asserts your method's behavior at the boundaries:
- Below threshold (positive): returns 0
- Below threshold (negative): returns 0
- Exactly at threshold: returns the value
- Above threshold: returns the value unchanged
- Zero input: returns 0

## See it in action

```bash
./gradlew simulateJava       # opens the WPILib SimGUI
```

The robot project includes a `JoystickIOSim` that sweeps the simulated
stick through the deadband region while emitting noise. Open
**AdvantageScope** (download from
[github.com/Mechanical-Advantage/AdvantageScope/releases](https://github.com/Mechanical-Advantage/AdvantageScope/releases)),
connect to NetworkTables at `localhost`, and plot:

- `RealOutputs/Joystick/Inputs/rawValue` — the noisy raw signal (red)
- `RealOutputs/Joystick/Inputs/cleanValue` — after your method (green)
- `RealOutputs/Lesson01/Pass` — a boolean that should stay green

When your method is right, the green trace sits flat at zero whenever
the red trace is inside the ±0.10 band — and tracks the red trace
exactly outside the band.

## Done?

If `./tools/frcprog.sh check 01-methods` returns ✓ and `Lesson01/Pass`
stays true throughout the sim, commit + push:

```bash
git add src/main/java/frc/robot/util/MathUtils.java
git commit -m "Lesson 01: implement applyDeadband"
git push
```

CI re-runs the same tests and your feedback PR will get a sticky
"Lesson 01 ✓" comment. **Next: [Lesson 02 — Tank Drive Wiring](../02-tank-drive/README.md)**,
which `import`s the file you just wrote.

## Why this is structured like a real project

This isn't a sandbox — it's a real WPILib + AdvantageKit project. The
`MathUtils.java` you edit is the same file a real subsystem would import
in lesson 02 and beyond. By the end of Stage 1, your `src/` directory
contains a working teleop robot you wrote line by line.

This is also why we use the **IO Layer pattern** (interface +
`@AutoLog` inputs + `Sim`/`Real` impls) starting from lesson 02 — it's
the same pattern Mechanical Advantage uses in their own competition
code. You learn the real shape, not a teaching abstraction.

> **Stuck?** [`hints.md`](hints.md) has progressive hints. Try them in
> order — peeking at the answer last.
