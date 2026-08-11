# Lesson 18 — AdvantageKit logging discipline <small>· Stage 2A</small>

<span class="stage-badge">Stage 2A · Lesson 18</span>

*Your log has `drive_speed`, `Drive/Speed`, `Drive/speedMps`, and `Drive/Velocity`. They're the same value. Future-you will hate present-you.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2A |
    | **Time** | ~35 min |
    | **Prereqs** | [Lesson 17 — AdvantageScope first-class](../17-advantagescope/) |
    | **Edits** | Audit every subsystem; rename log keys to convention; add `@AutoLogOutput` to public getter-style state |
    | **Tests** | `frc.robot.LoggingDisciplineTest` (`@Tag("lesson-18")`) — verifies key naming + structured types |
    | **Reference robot** | Presto · [`flywheels/Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/Flywheels.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Use `@AutoLogOutput` on getter methods and fields — auto-log them every cycle, no boilerplate.
2. Apply the `SubsystemName/Field` key convention everywhere.
3. Distinguish **inputs** (logged via `Logger.processInputs`) from **outputs** (`Logger.recordOutput`).
4. Log structured types (`Pose2d`, `Pose3d`, `SwerveModuleState[]`, enums) — not just doubles.
5. Recognize when you're about to write `recordOutput("driveSpeed", ...)` and stop yourself.

---

## The real-world problem

Lesson 10 said "add `Logger.recordOutput`." You did. So did three teammates. Now your NT tree looks like:

```
drive_speed
Drive/Speed
Drive/speedMps
Drive/Velocity
elevator-height
Elevator/Height
ELEVATOR_HEIGHT
```

These are the same four values, four times each. AdvantageScope can't group them. The signal search box is useless. Tab layouts break when somebody renames a key. The problem isn't logging — it's *log hygiene*.

Read Presto's [`Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/Flywheels.java) and tail the `Logger.recordOutput` calls. Every key is `Flywheels/Something`. There are no exceptions. That's not style — that's *discipline*, and it's what makes a tree of 400 signals navigable.

---

## What you'll do

Walk every subsystem and:

1. Rename every NT key to `SubsystemName/PascalCaseField`.
2. Replace ad-hoc `Logger.recordOutput("Pose", pose.getX())` calls with `Logger.recordOutput("Drive/Pose", pose)` — the whole `Pose2d`, not a hand-decomposed double.
3. Add `@AutoLogOutput` to public getter-style state that should always be logged.
4. Run the linter test; it greps your source for key strings and asserts the regex.

---

## The two log channels

AdvantageKit has two ways to put a value into the log. They look similar; the distinction matters for replay.

| Channel | API | What it is | Lives where |
|---|---|---|---|
| **Inputs** | `Logger.processInputs("Drive", inputs)` | Things the *outside world* told the code. Sensors, encoder ticks, joystick values. | `Drive/Inputs/...` |
| **Outputs** | `Logger.recordOutput("Drive/Pose", pose)` | Things the *code computed*. Setpoints, derived poses, scheduled-command state. | `Drive/<Field>` |

The split exists because **only inputs need to be re-fed in replay**. Outputs are recomputed from the inputs each replay run. Get this wrong and replay diverges from reality. Get it right and you can add a new logged output and replay yesterday's match to see it.

!!! warning "If you `recordOutput` something that came from a sensor read, replay breaks"

    Sensor reads belong in `DriveIOInputs`. If `pose` comes from `gyro.getYaw()` directly inside the subsystem, you've bypassed the IO layer. Push the gyro read down into `GyroIO`; expose `inputs.yawRad`; compute `pose` from `inputs` in the subsystem; `recordOutput("Drive/Pose", pose)` in `periodic()`.

---

## The `@AutoLogOutput` annotation

Every cycle, AdvantageKit reflects over your subsystem's `@AutoLogOutput`-tagged fields/methods and logs their current values. No periodic `Logger.recordOutput` boilerplate:

```java linenums="1"
public class Drive extends SubsystemBase {
  @AutoLogOutput(key = "Drive/Pose")
  public Pose2d getPose() { return poseEstimator.getEstimatedPosition(); }

  @AutoLogOutput(key = "Drive/ModuleStates")
  public SwerveModuleState[] getModuleStates() { /* ... */ }

  @AutoLogOutput
  private boolean isAligned = false;  // logged as "Drive/isAligned"
}
```

The annotation infers a key from the field/method name; pass `key = "..."` to override. **Prefer overriding** — relying on Java identifier names couples your log keys to your refactor decisions, and the keys end up in saved AdvantageScope layouts.

---

## The naming convention

Two rules, learned by reflex:

1. **`SubsystemName/Field`** — PascalCase on both sides, `/` separator. `Drive/Pose`, `Elevator/HeightMeters`, `Flywheels/LeftVelocityRpm`. Match Presto: signals nest naturally in AdvantageScope's tree.
2. **`SubsystemName/Inputs/Field` for inputs**, `SubsystemName/Field` for outputs. The `Inputs` subtree is generated automatically by `Logger.processInputs("Drive", inputs)`. Don't write `Drive/InputsDelay` as an output — it lives at a different level.

The `LoggingDisciplineTest` greps your `src/main/java/frc/robot/subsystems/` tree for `Logger.recordOutput("..."` and asserts the literal matches `^[A-Z][A-Za-z]*\/[A-Z][A-Za-z]*(\/[A-Z][A-Za-z]*)*$`. If it fails, the error message is the offending key.

---

## Structured types over decomposed doubles

This is the upgrade most teams skip and most regret. Before:

```java
Logger.recordOutput("Drive/PoseX", pose.getX());
Logger.recordOutput("Drive/PoseY", pose.getY());
Logger.recordOutput("Drive/PoseTheta", pose.getRotation().getRadians());
```

After:

```java
Logger.recordOutput("Drive/Pose", pose);
```

AdvantageKit knows how to serialize `Pose2d`, `Pose3d`, `Translation2d`, `Rotation2d`, `Transform3d`, `SwerveModuleState[]`, `ChassisSpeeds`, `Mechanism2d`, and enums (as their string name). AdvantageScope **groups them**: dropping `Drive/Pose` onto the 3D field works. Dropping three doubles doesn't — you'd have to manually wire each one to a coordinate.

!!! example "Structured outputs cheat sheet"

    ```java
    Logger.recordOutput("Drive/Pose", pose);                 // Pose2d
    Logger.recordOutput("Drive/PoseGoal", target);           // Pose2d
    Logger.recordOutput("Drive/ModuleStates", moduleStates); // SwerveModuleState[]
    Logger.recordOutput("Drive/ModuleStatesGoal", goals);    // SwerveModuleState[]
    Logger.recordOutput("Drive/ChassisSpeeds", speeds);      // ChassisSpeeds
    Logger.recordOutput("Robot/State", robotState);          // enum — recorded as name
    Logger.recordOutput("Vision/Tags", tagPoses);            // Pose3d[]
    ```

    Each of these unlocks an AdvantageScope tab that wouldn't work with raw doubles.

---

## Rubric

`LoggingDisciplineTest` asserts:

1. Every NT key matches `^[A-Z][A-Za-z]*\/[A-Z]` (subsystem-prefixed, PascalCase).
2. `Drive/Pose` is a `Pose2d`, not three doubles (verified by reading back the type tag).
3. Every subsystem has at least one `@AutoLogOutput` field or method.
4. No `Logger.recordOutput` call uses snake_case or lowercase-leading keys.

```bash
./gradlew test --tests '*LoggingDisciplineTest' -DincludeTags='lesson-18'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope. The signal tree is now a tidy hierarchy — every key under a subsystem prefix, structured values clearly marked. Drop `Drive/Pose` onto the 3D field; it just works. Drop `Drive/ModuleStates` onto a Swerve view (in Stage 2B); same. The next two lessons reuse this hygiene heavily.

---

## Going further

- Audit a real match log from Presto (download a `.wpilog` from their build thread). Compare their key density to yours: how many signals per subsystem? It's higher than you'd guess.
- Add `@AutoLogOutput` to the *enum state* of your superstructure (you'll write the superstructure in Lesson 20). Enums get logged by `name()` — readable in AdvantageScope as strings, not as ordinal integers.

!!! note "Why we didn't teach this in Lesson 10"

    Lesson 10 introduced *the act* of logging. Drilling on key naming there would have buried the lesson. By now, you've logged things ad-hoc for 8 lessons and the mess has cost you. *That's* when the rules land.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 17**
    AdvantageScope first-class

    [:octicons-arrow-left-24: Back to lesson 17](../17-advantagescope/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 19**
    Log replay for debugging

    [:octicons-arrow-right-24: Continue to lesson 19](../19-log-replay/)

</div>
