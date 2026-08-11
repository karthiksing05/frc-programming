# Lesson 04 — Subsystems as state machines <small>· Stage 1B</small>

<span class="stage-badge">Stage 1B · Lesson 04</span>

*Your `teleopPeriodic` is 25 lines of `if`/`else` and growing. Today you lift it into a real class that knows how to behave.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1B |
    | **Time** | ~45 min |
    | **Prereqs** | [Lesson 03 — Conditionals in `teleopPeriodic`](../../stage1a/03-conditionals-in-teleop/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/roller/RollerSubsystem.java` |
    | **Tests** | `frc.robot.subsystems.roller.RollerSubsystemTest` (`@Tag("lesson-04")`) |
    | **Reference robot** | Kelpie · [`roller/RollerSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Write a `class Foo extends SubsystemBase` and explain why it inherits.
2. Hold motor and sensor references as `private final` fields.
3. Expose a small, intent-named API (`setMode(State.INTAKING)`), not raw motor controls.
4. Implement `periodic()` so it reads the world, decides, and acts — once per scheduler tick.
5. Recognize when an `enum` is the right shape for a subsystem's behavior.

---

## The real-world problem

If you finished Lesson 03 honestly, you have a `teleopPeriodic` method that does roughly this: read a button, read a beam-break, decide which way the roller should spin, and call `motor.set(...)`. It works — but every new feature adds another `if`. A second operator button. A timeout. A "spit if jammed" recovery. Soon `Robot.java` is the only file that knows anything about the roller, and every sensor reading on the robot lives in the same method.

This is the pain Niwiden's pedagogy promises will pay off: you *felt* the lack of structure, so the structure now feels like relief rather than ceremony. Subsystems are how WPILib codebases stay readable past 100 lines. They wrap one mechanism's hardware, one mechanism's sensors, and one mechanism's decisions inside a single class — so the rest of the robot only has to say *what* it wants, not *how*.

---

## What you'll do

You'll create a `RollerSubsystem` class that owns the roller motor and the beam-break sensor that lesson 03 was hammering with `if`s. You'll add an `enum State { OFF, INTAKING, EJECTING }` and a `setMode(State)` method. The decision logic — *"if INTAKING and the beam-break sees a piece, stop; if EJECTING, run reverse no matter what"* — moves from `Robot.teleopPeriodic()` into `RollerSubsystem.periodic()`. By the end, `Robot.teleopPeriodic` should be a single line: `roller.setMode(operator.b().getAsBoolean() ? State.INTAKING : State.OFF);` (or similar).

---

## Starter code

```java linenums="1"
package frc.robot.subsystems.roller;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class RollerSubsystem extends SubsystemBase {
  public enum State { OFF, INTAKING, EJECTING }

  private final PWMSparkMax motor = new PWMSparkMax(Constants.Roller.MOTOR_PORT);
  private final DigitalInput beamBreak = new DigitalInput(Constants.Roller.BEAM_BREAK_PORT);

  private State mode = State.OFF;

  public void setMode(State next) {
    // TODO (LESSON 04): store the requested mode
  }

  @Override
  public void periodic() {
    // TODO (LESSON 04): read the beam-break and drive the motor based on `mode`
  }
}
```

Notice three things before you start typing:

- **`private final`** for both hardware fields. Nothing outside this class ever touches the motor or sensor directly. This is the encapsulation rule that makes subsystems composable — break it once and the whole pattern unravels.
- **`mode`** is `private` and mutable; everyone else changes it through `setMode`. The `enum` makes illegal states unrepresentable — there is no fourth value of `State` that someone can accidentally pass.
- **`periodic()`** runs every 20 ms whether you tell it to or not. WPILib's `CommandScheduler` calls it for you. Your job is to make each tick a pure read-decide-act.

---

## How Kelpie does it

Team 8033's [`roller/RollerSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller) is the closest production analog. Their version is slightly more advanced — it sits on top of an IO Layer you'll meet in Lesson 16 — but the shape is identical: a `SubsystemBase`, a small set of public modes, and a `periodic()` that turns the requested mode plus the current sensor reading into a single motor command.

!!! quote "From Kelpie's roller package"

    Kelpie's roller exposes `setMode` and a couple of state-asking getters. Cross-subsystem coordination happens up in `Superstructure`, never inside the roller. That's the principle: a subsystem knows itself, nothing else.

---

## Rubric

`RollerSubsystemTest` asserts:

1. `RollerSubsystem extends SubsystemBase`.
2. The motor and beam-break fields are `private final` (checked via reflection).
3. `setMode(State.INTAKING)` runs the motor at +0.6 *unless* the beam-break reports a piece, in which case the motor is 0.
4. `setMode(State.EJECTING)` runs the motor at −0.6 unconditionally.
5. `setMode(State.OFF)` holds the motor at 0.
6. After the refactor, `Robot.teleopPeriodic()` is one line of code that delegates to the roller.

```bash
./gradlew test --tests '*RollerSubsystemTest' -DincludeTags='lesson-04'
```

!!! warning "Common mistake"

    Calling `motor.set(...)` from inside `setMode`. Tempting, because it feels direct — but it means the motor command updates only when buttons change, not when the beam-break changes. Always drive the motor from `periodic()`. `setMode` only updates a variable.

---

## See it run

```bash
./gradlew simulateJava
```

Open SimGUI and watch the **PWM 5** widget while you press buttons in the keyboard joystick window. Then open AdvantageScope and plot:

- `Roller/state` — the enum, printed each tick
- `Roller/motorOutput` — the commanded value
- `Roller/beamBroken` — boolean from the sensor

The behavior should be identical to your Lesson 03 robot. The difference is that `Robot.java` is now boring, and `RollerSubsystem.java` is small enough to read in one screenful.

---

## Going further

- Add a fourth state — `STALLED` — that the subsystem enters on its own when the motor has been commanded for >1 s without the beam-break tripping. This is your first taste of subsystem-detected state.
- Read [WPILib's Subsystems page](https://docs.wpilib.org/en/stable/docs/software/commandbased/subsystems.html). Notice the line: *"a subsystem encapsulates lower-level hardware objects (motor controllers, sensors, etc.) and provides methods through which they can be used by Commands."* That's the entire pattern, in one sentence.
- Look at Kelpie's [`FunnelSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems). Same shape, different mechanism — the pattern transfers.

---

??? tip "Full reveal — only open if you're really stuck"

    The minimal completion of `periodic()`:

    ```java
    @Override
    public void periodic() {
      boolean piecePresent = !beamBreak.get(); // beam-break is true when *unbroken*
      double output = switch (mode) {
        case INTAKING -> piecePresent ? 0.0 : 0.6;
        case EJECTING -> -0.6;
        case OFF      -> 0.0;
      };
      motor.set(output);
    }
    ```

    Resist peeking until your tests have failed at least twice — the failures are how the rubric teaches.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 03**
    Conditionals in `teleopPeriodic`

    [:octicons-arrow-left-24: Back to lesson 03](../../stage1a/03-conditionals-in-teleop/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 05**
    PID introduction — Elevator

    [:octicons-arrow-right-24: Continue to lesson 05](../05-pid-elevator/)

</div>
