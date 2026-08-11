# Lesson 03 — Conditionals in `teleopPeriodic` <small>· Stage 1A</small>

<span class="stage-badge">Stage 1A · Lesson 03</span>

*This lesson is the pain. You will write a hairy `teleopPeriodic` on purpose, then notice it's hairy. The next lesson is the relief.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1A |
    | **Time** | ~35 min |
    | **Prereqs** | [Lesson 02 — Variables & types](../02-variables-and-types/) |
    | **Edits** | `src/main/java/frc/robot/Robot.java` (specifically `teleopPeriodic()`) |
    | **Tests** | `frc.robot.RobotTeleopTest` (`@Tag("lesson-03")`) |
    | **Reference robot** | Kelpie · [`roller/RollerSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller) — *what you're not doing yet* |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Read a boolean from a beam-break sensor.
2. Read a button press from an `XboxController`.
3. Write `if` / `else if` / `else` chains that combine multiple inputs.
4. Drive a motor based on combined sensor + button state.
5. **Recognize that this is getting hard to read.** This is the real objective.

---

## The real-world problem

Your robot has an intake roller and a beam-break sensor that detects a game piece inside the intake. You want:

- Operator holds **B** with no piece detected → roller runs *in* at `+0.6`.
- Operator holds **B** with a piece already in the intake → roller stops (don't crush the piece).
- Operator holds **X** → roller runs *out* at `-0.6`, regardless of the sensor (manual eject overrides everything).
- Nothing held → roller stops.

Today, you'll express all of that inside `Robot.teleopPeriodic()` — the method WPILib runs ~50 times a second while the robot is in teleop. No subsystems, no commands, no triggers. **Just `if` statements and direct motor calls.**

We are doing this *deliberately*. Niwiden's pedagogy — pain before abstraction — only works if you actually feel the pain. By the end of this lesson your `teleopPeriodic` will be twenty-five lines long and growing, and you'll be muttering about it. That's the lesson.

---

## What you'll do

Open `src/main/java/frc/robot/Robot.java`. Find `teleopPeriodic()` — it's near the bottom of the file, empty body, a comment telling you to write code there.

You'll wire up a `Spark` motor controller (or whatever the starter project uses) on the PWM port for the roller, a `DigitalInput` for the beam-break, and a `XboxController` for the operator. Then inside `teleopPeriodic`, you'll write the `if` / `else if` / `else` chain that implements the four rules above.

---

## Starter code

```java linenums="1"
package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.PWMSparkMax;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;

public class Robot extends TimedRobot {
    private final PWMSparkMax roller = new PWMSparkMax(5);
    private final DigitalInput beamBreak = new DigitalInput(0);
    private final XboxController operator = new XboxController(1);

    @Override
    public void teleopPeriodic() {
        // TODO (Lesson 03): implement the four rules above
    }
}
```

A reasonable first attempt:

```java linenums="1"
@Override
public void teleopPeriodic() {
    boolean piecePresent = !beamBreak.get(); // beam-break: false when blocked
    boolean intaking = operator.getBButton();
    boolean ejecting = operator.getXButton();

    if (ejecting) {
        roller.set(-0.6);
    } else if (intaking && !piecePresent) {
        roller.set(0.6);
    } else if (intaking && piecePresent) {
        roller.set(0.0);
    } else {
        roller.set(0.0);
    }
}
```

Notice the `!beamBreak.get()` — most beam-break sensors return `false` when the beam is broken (the path is blocked, *piece is present*) and `true` when it's clear. Sensors lie all the time about their polarity; check the datasheet.

---

## Rubric

`RobotTeleopTest` (`@Tag("lesson-03")`) drives `teleopPeriodic` with mocked input states and asserts the motor output:

1. Hold B, no piece → roller at `+0.6`.
2. Hold B, piece present → roller at `0.0`.
3. Hold X → roller at `-0.6` (overrides B).
4. Release everything → roller at `0.0`.

Run locally:

```bash
./gradlew test --tests '*RobotTeleopTest' -DincludeTags='lesson-03'
```

---

## See it run

```bash
./gradlew simulateJava
```

In SimGUI:

1. Open the **System Joysticks** window, drag a controller into **Joystick 1**, and bind the B and X buttons.
2. Open the **DIO** widget — DIO 0 is your beam-break. Click it to toggle "piece present."
3. Watch **PWM 5** — that's your roller output. Hold B with DIO 0 cleared and you'll see `+0.6`. Click DIO 0 to simulate a piece arriving and the output drops to `0.0`. Hold X and the output flips to `-0.6` regardless of DIO 0.

---

## The point: feel the pain

Now stop and look at what you wrote.

```java
public void teleopPeriodic() {
    boolean piecePresent = !beamBreak.get();
    boolean intaking = operator.getBButton();
    boolean ejecting = operator.getXButton();

    if (ejecting) {
        roller.set(-0.6);
    } else if (intaking && !piecePresent) {
        roller.set(0.6);
    } else if (intaking && piecePresent) {
        roller.set(0.0);
    } else {
        roller.set(0.0);
    }
}
```

That's twelve lines, and it controls *one mechanism*. Realistically your robot has five mechanisms. Right now, every one of them is going to need its own chunk of conditions, its own button reads, its own sensor reads, all interleaved in the same `teleopPeriodic`.

!!! danger "Your `teleopPeriodic` is now 25 lines and growing. Next lesson, we fix this."

    This is verbatim what the next lesson opens with. By the time you've added an LED status indicator, an elevator with three setpoints, and a shooter, your `teleopPeriodic` will be ninety lines of unstructured `if` chains and your laptop will catch fire from how much you scroll-wheel.

    Lesson 04 introduces `SubsystemBase`. Every mechanism becomes its own class. `Robot.java` shrinks back to about five lines. You'll feel it as relief — because today you felt it as pain.

Take a moment to predict, before Lesson 04 spoils it, what shape the *organized* version of this code might take. What's the noun in this code that wants its own home? (Hint: it starts with "r" and ends with "oller.")

---

## What's *not* there yet

A non-exhaustive list of things this lesson deliberately does not use, because they'd defang the pain:

- **`SubsystemBase`.** Lesson 04.
- **Commands or factories.** Lesson 07.
- **`Trigger`, `onTrue`, `whileTrue`.** Lesson 08.
- **`CommandScheduler`.** Lesson 08.
- **Enums for state machines.** Lesson 04.
- **`Logger.recordOutput` for telemetry.** Lesson 10.

You'll meet each of those *exactly* when the previous lesson has made you wish for them. That's the whole pedagogy.

!!! info "Where we're refactoring toward"

    Kelpie's [`roller/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/roller) package shows the destination. Compare what you wrote today to `RollerSubsystem.java`. The same four rules live in there, but: the motor is `private final`, the rules read like sentences (`setMode(State.INTAKING)`), and there's no mention of buttons inside the subsystem — buttons live in `RobotContainer` and call into the subsystem. We're not there yet. That's lessons 04 and 08.

---

## Going further (only if you finished early)

- Add an LED that turns red when ejecting, green when a piece is present, off otherwise. Write it inside the same `teleopPeriodic`. Notice how much worse that makes the file.
- Add a third operator button (Y) that runs the roller at `+0.3` (a slower intake speed). Where does that case go in your `if` chain? How do you make sure it interacts correctly with the X-button override? This is the kind of question that gets *easier* under the next lesson's abstractions, and stays painful under today's.
- Re-read Niwiden's slides (linked from [Curriculum-Flow §1](/process/Curriculum-Flow.md)) — specifically the lesson where she has students write conditional logic in `teleopPeriodic` *before* introducing subsystems. You just did the same exercise her students did.

---

??? tip "Full reveal — only open if you're really stuck"

    A complete `teleopPeriodic`, with the conditions flattened a little for symmetry:

    ```java linenums="1"
    @Override
    public void teleopPeriodic() {
        boolean piecePresent = !beamBreak.get();
        boolean intaking = operator.getBButton();
        boolean ejecting = operator.getXButton();

        double rollerOutput;
        if (ejecting) {
            rollerOutput = -0.6;
        } else if (intaking && !piecePresent) {
            rollerOutput = 0.6;
        } else {
            rollerOutput = 0.0; // covers "B + piece present" and "nothing held"
        }
        roller.set(rollerOutput);
    }
    ```

    Note the single `roller.set` at the bottom instead of one per branch. Slightly more readable, identical behavior. This is what the next lesson's `periodic()` will start to look like — and then you'll wrap it in a class.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 02**
    Variables & types

    [:octicons-arrow-left-24: Back to lesson 02](../02-variables-and-types/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 04**
    Subsystems as state machines

    [:octicons-arrow-right-24: Continue to lesson 04](../../stage1b/04-subsystems-state-machines/)

</div>
