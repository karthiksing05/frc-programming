# Lesson 02 — Variables & types <small>· Stage 1A</small>

<span class="stage-badge">Stage 1A · Lesson 02</span>

*Your `0.10` deadband threshold lives in two files. The day you decide to make it `0.08`, you will miss one. Names exist so this can't happen.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1A |
    | **Time** | ~30 min |
    | **Prereqs** | [Lesson 01 — Methods (Functions)](../01-methods/) |
    | **Edits** | `src/main/java/frc/robot/Constants.java` |
    | **Tests** | `frc.robot.ConstantsTest` (`@Tag("lesson-02")`) |
    | **Reference robot** | Presto · [`flywheels/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) for the per-subsystem constants pattern |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Declare `public static final` constants in Java.
2. Pick the right primitive type (`double` for measurements, `int` for counts, `boolean` for yes/no).
3. Organize related constants in nested classes — `Constants.Drive`, `Constants.Flywheels`, and so on.
4. Refactor a magic number into a named constant without breaking the call sites.

---

## The real-world problem

Look at the code you wrote in Lesson 01. The threshold `0.10` is a *magic number* — a literal value with no name attached to it. Right now it appears in one place (the test) and one place (your method body). Innocuous.

Now imagine Lesson 07 lands and the tank-drive code passes `0.10` again. And Lesson 11 picks it up for the default command. By the end of Stage 1, that number lives in four files. The driver complains the deadband feels too small. You change it in three of the four files. The fourth file? Subtle, intermittent creep that you'll spend two practice nights chasing.

The fix is older than Java itself: **give the number a name, define it in exactly one place, refer to it by name everywhere else.** In Java, that means `public static final double DEADBAND = 0.10;`. Read it as "a constant `double` named `DEADBAND`, equal to `0.10`, visible to any class in the project."

---

## What you'll do

Open `src/main/java/frc/robot/Constants.java`. It exists as a near-empty shell. You'll:

1. Add a nested class `Drive` and put `DEADBAND = 0.10` inside it.
2. Update the Lesson 01 test's call site (and any other call site the starter project has) so it reads `Constants.Drive.DEADBAND` instead of the literal `0.10`.
3. Add three more constants the upcoming lessons will need: a gear ratio, a max voltage, and a max RPM. They go in their own nested classes.

By the end, `Constants.java` looks like a tree of small nested groups — one per upcoming subsystem — with five constants in it.

---

## Starter code

The skeleton you'll start from:

```java linenums="1"
package frc.robot;

public final class Constants {
    private Constants() {}

    public static final class Drive {
        // TODO (Lesson 02): public static final double DEADBAND = ...;
    }

    // TODO (Lesson 02): add Flywheels and Elevator nested classes
}
```

A finished version (don't peek too long — try writing yours first):

```java linenums="1" hl_lines="6 10 14"
public final class Constants {
    private Constants() {}

    public static final class Drive {
        public static final double DEADBAND = 0.10;
        public static final double GEAR_RATIO = 8.45; // toughbox mini
    }

    public static final class Flywheels {
        public static final double MAX_VOLTAGE = 12.0;
        public static final double MAX_RPM = 6000.0;
    }

    public static final class Elevator {
        public static final int MOTOR_ID = 21;
    }
}
```

Things worth noticing:

- **`public static final`** is the canonical Java spelling of "constant." `public` for visibility, `static` so it lives on the class (no instance needed), `final` so nothing can reassign it. WPILib templates spell every constant this way. So do both reference robots.
- **Naming.** `SCREAMING_SNAKE_CASE` for the constant itself, `PascalCase` for the nested class. This is the standard Java convention, and your IDE will quietly underline you if you forget.
- **Type choice.** `double` for everything continuous (meters, seconds, volts, deadband fractions). `int` for counts and CAN IDs. `boolean` for yes/no flags. Stage 1B will add `enum` for state machines, but that's later.

---

## Why nested classes, not one flat list?

Open Presto's [`flywheels/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) package and look at how 6328 organizes their constants. Their flywheel-specific tunables live *inside* the flywheel subsystem package, in a class named `FlywheelConstants` — not as a flat dump in a single `Constants.java`.

We're going to converge on that pattern by Stage 2A, but to get there we need a stepping stone. In Stage 1, every team's `Constants.java` looks like a tree:

```java
Constants
├── Drive
│   ├── DEADBAND
│   └── GEAR_RATIO
├── Flywheels
│   ├── MAX_VOLTAGE
│   └── MAX_RPM
└── Elevator
    └── MOTOR_ID
```

Read as: *"Each subsystem owns its own little namespace of constants."* When Lesson 07 needs the deadband, it writes `Constants.Drive.DEADBAND` and the reader immediately knows: *this lives with the drivetrain.* When Lesson 04 splits the elevator off into its own package, we'll lift `Constants.Elevator` along with it — and at that point you're one step away from Presto's per-subsystem layout.

!!! note "Presto's stepping stone, made concrete"

    Presto's `FlywheelConstants` sits at `src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/FlywheelConstants.java`. It's a single-purpose constants file, scoped to one subsystem. Your Stage 1A `Constants.Flywheels` is the same idea expressed as a nested class instead of a separate file — the same shape, one refactor away.

---

## Rubric

`ConstantsTest` (`@Tag("lesson-02")`) asserts four things:

1. `Constants.Drive.DEADBAND == 0.10` (the exact value).
2. The Lesson 01 deadband tests *still* pass — your refactor of the call site can't have changed behavior.
3. Constants are `public static final` (verified via reflection).
4. Constants live in a nested class, not at the root of `Constants`.

Run locally:

```bash
./gradlew test --tests '*ConstantsTest' -DincludeTags='lesson-02'
./gradlew test --tests '*MathUtilsTest' -DincludeTags='lesson-01'   # must still pass
```

!!! warning "Refactoring should be invisible"

    The point of this lesson is that *behavior stays the same and the code reads better.* If your Lesson 01 tests fail after you refactor the call site, your refactor was wrong — the value got changed somewhere along the way. Stage the changes in two commits so you can `git diff` and verify the only thing that changed is which symbol the call site refers to.

---

## See it run

Same sim as Lesson 01:

```bash
./gradlew simulateJava
```

You should see the *exact same* behavior on the AdvantageScope traces: green output flat in the band, tracking outside the band. The only difference now is in the source — if you `Cmd-Click` (or Ctrl-Click) on `Constants.Drive.DEADBAND`, your IDE will jump to one and only one definition.

---

## Going further

- Add a `Constants.OperatorInterface` nested class with `DRIVER_PORT = 0` and `OPERATOR_PORT = 1`. You'll need them in Lesson 08.
- Look at Kelpie's [`elevator/ElevatorSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/elevator). Notice that 8033 doesn't even centralize constants — they live as `private static final` fields inside the subsystem that uses them. That's another valid style; we're teaching the nested-class flavor first because it's easier to navigate before you have many subsystems.
- Read [Curriculum-Flow §7.4](/process/Curriculum-Flow.md) on the `Constants.java` vs records vs config-files debate. You'll meet records and tunable-via-NetworkTables values in Stage 2.

---

??? tip "Full reveal — only open if you're really stuck"

    The call-site refactor looks like:

    ```diff
    - public static double applyDeadband(double value) {
    -     return Math.abs(value) < 0.10 ? 0.0 : value;
    - }
    + public static double applyDeadband(double value) {
    +     return Math.abs(value) < Constants.Drive.DEADBAND ? 0.0 : value;
    + }
    ```

    …plus the `import frc.robot.Constants;` line at the top. If your IDE has an "Introduce constant" refactor (IntelliJ: `Cmd-Alt-C` on macOS), it'll do the whole transform for you in one keystroke.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 01**
    Methods (Functions)

    [:octicons-arrow-left-24: Back to lesson 01](../01-methods/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 03**
    Conditionals in `teleopPeriodic`

    [:octicons-arrow-right-24: Continue to lesson 03](../03-conditionals-in-teleop/)

</div>
