# Lesson 01 — Methods (Functions) <small>· Stage 1A</small>

<span class="stage-badge">Stage 1A · Lesson 01</span>

*Your robot creeps across the field while the driver isn't touching the joystick. The fix is one method, written once, called everywhere.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1A |
    | **Time** | ~25 min |
    | **Prereqs** | [Lesson 0D — Git + project tour](../../stage0/0d-git-tour/) |
    | **Edits** | `src/main/java/frc/robot/util/MathUtils.java` |
    | **Tests** | `frc.robot.util.MathUtilsTest` (`@Tag("lesson-01")`) |
    | **Reference robot** | Both · WPILib's [`MathUtil.applyDeadband`](https://github.com/wpilibsuite/allwpilib/blob/main/wpimath/src/main/java/edu/wpi/first/math/MathUtil.java) is "your code, but production" |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Write a `public static` method with parameters and a return value.
2. Call a method from another class with `ClassName.method(args)`.
3. Spot a chunk of math that appears more than once and pull it into a method.
4. Use `Math.abs` and either an `if` statement or the ternary operator to branch.

---

## The real-world problem

Xbox-controller sticks drift. Even when nobody is touching them, the sensors inside the controller report values around ±0.05 because the hardware isn't perfect. If you forward those readings straight to a motor, the robot creeps across the field while the driver is standing still — annoying at the practice field, dangerous next to a teammate during pit setup.

The fix is a **deadband**: ignore any stick reading whose magnitude is below some threshold (a typical FRC value is 0.10). Done in one place, called from everywhere a stick value enters your robot code. The day you find a better threshold, you change it in one file — not in thirty.

---

## What you'll do

Open `src/main/java/frc/robot/util/MathUtils.java`. There's an empty method called `applyDeadband(double value, double threshold)` waiting for a body. Your job is to fill it in so that values inside the band collapse to zero, and values outside the band pass through unchanged.

Before you write a single line in Java, drive the same algorithm in the browser PoC below. The widget pipes a noisy joystick trace through your code — the green output trace should sit flat at zero whenever the red raw trace is inside the band, and track the raw trace exactly outside the band.

<iframe class="lesson-widget"
        src="/examples/functions-poc/index.html"
        width="100%"
        height="640"
        title="Functions PoC — applyDeadband interactive"></iframe>

---

## Starter code

```java linenums="1"
package frc.robot.util;

public final class MathUtils {
    private MathUtils() {} // utility class — never instantiated

    /** Returns 0 if |value| < threshold, else value. */
    public static double applyDeadband(double value, double threshold) {
        // TODO (Lesson 01): implement
        return value;
    }
}
```

A few things to notice before you start typing:

- The class is `public final` with a `private` constructor. That's the canonical Java idiom for a utility class — you call its methods on the *class* (`MathUtils.applyDeadband(...)`), not on an instance you `new`.
- The method is `public static`. `public` so other classes can call it; `static` so they don't need a `MathUtils` instance to do so.
- It takes two `double`s and returns one `double`. The header tells you everything about how to call it.

---

## Rubric

The test class `MathUtilsTest` (`@Tag("lesson-01")`) asserts five behaviors:

1. Positive input below threshold returns `0.0`.
2. Negative input below threshold returns `0.0`.
3. Input exactly at threshold returns `value` (the boundary belongs to the live side).
4. Input above threshold returns `value` unchanged.
5. A non-default threshold (e.g., `0.20`) is respected — your code must use the parameter, not a hard-coded `0.10`.

Run locally:

```bash
./gradlew test --tests '*MathUtilsTest' -DincludeTags='lesson-01'
```

!!! warning "Don't hard-code the threshold"

    A classic rookie shortcut is to write `if (Math.abs(value) < 0.1) return 0;`. That passes test 1 and 2 but fails test 5, because the test calls your method with `threshold = 0.20` and expects you to honor it. Use the parameter you were given.

---

## See it run

```bash
./gradlew simulateJava
```

The starter project ships a `JoystickIOSim` that sweeps the simulated stick through the deadband region while emitting realistic noise. In another terminal, open AdvantageScope, connect to NetworkTables 4 at `localhost`, and plot:

- `RealOutputs/Joystick/Inputs/rawValue` — the noisy raw signal (red).
- `RealOutputs/Joystick/Inputs/cleanValue` — the same signal through your method (green).
- `RealOutputs/Lesson01/Pass` — a boolean that stays true while your code is correct.

When your implementation is right, the green trace sits flat at zero whenever the red trace is inside the ±0.10 band, and tracks the red exactly outside the band.

---

## Going further

- Try `MathUtil.applyDeadband` from [WPILib's source](https://github.com/wpilibsuite/allwpilib/blob/main/wpimath/src/main/java/edu/wpi/first/math/MathUtil.java). WPILib's version doesn't just zero the deadband — it also *rescales* the live range so the first non-zero output is a tiny number, not a jump from 0 to 0.10. Your version doesn't have to do this in Lesson 01, but read theirs and notice the difference.
- Add a second method `clamp(double value, double min, double max)` for symmetry. You'll need it the moment you're sending motor voltage.
- Both reference robots use `MathUtil.applyDeadband` somewhere in their drive code. Search Kelpie and Presto for `applyDeadband(` and notice that you've just written, in miniature, a function that production-grade competition robots depend on.

!!! info "Where this method goes next"

    You'll `import frc.robot.util.MathUtils;` and call `applyDeadband(...)` from your tank drive code in [Lesson 07](../../stage1c/07-tank-drive/). The lesson exists *now* because the lesson where it's used assumes you can already write methods — pain before abstraction, in miniature.

---

??? tip "Full reveal — only open if you're really stuck"

    The minimal body is one expression:

    ```java
    public static double applyDeadband(double value, double threshold) {
        return Math.abs(value) < threshold ? 0.0 : value;
    }
    ```

    The `?:` is the ternary operator: *condition* `?` *value-if-true* `:` *value-if-false*. The equivalent `if`/`else` version is fine too — pick whichever you find more readable.

    Try to derive this before peeking. The test failures will tell you exactly which case is off.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 0D**
    Git + project tour

    [:octicons-arrow-left-24: Back to lesson 0D](../../stage0/0d-git-tour/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 02**
    Variables & types

    [:octicons-arrow-right-24: Continue to lesson 02](../02-variables-and-types/)

</div>
