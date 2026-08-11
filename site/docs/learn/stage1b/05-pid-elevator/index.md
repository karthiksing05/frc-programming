# Lesson 05 — PID introduction (Elevator) <small>· Stage 1B</small>

<span class="stage-badge">Stage 1B · Lesson 05</span>

*You can ask a motor to spin. You can't yet ask it to stop at exactly 1.2 meters. Today you fix that — without any control-theory math.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1B |
    | **Time** | ~50 min |
    | **Prereqs** | [Lesson 04 — Subsystems as state machines](../04-subsystems-state-machines/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java` |
    | **Tests** | `frc.robot.subsystems.elevator.ElevatorTest` (`@Tag("lesson-05")`) |
    | **Reference robot** | Kelpie · [`elevator/ElevatorSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/elevator) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Wire WPILib's `PIDController` into a subsystem.
2. Tune `kP`, `kI`, and `kD` by reading a step-response plot — not by guessing.
3. Read encoder position from an inputs struct each cycle.
4. Send **voltage** to a motor, not normalized throttle, and explain why that matters.

---

## The real-world problem

Last lesson, your roller had three states: OFF, INTAKING, EJECTING. Each one was a single motor speed. That works because the roller doesn't care *where* it is — it only cares which way to spin.

An elevator does care. If the operator asks for "L4," the carriage has to travel exactly to L4 — not 5 cm short, not 5 cm long, not oscillating up-and-down past it forever. The naïve fix — `motor.set(1.0)` until you're close, then `motor.set(0)` — is called bang-bang control and it produces exactly what it sounds like: the carriage slams into the target, overshoots, slams back, and gives up only when friction wins.

PID is the standard answer. It is also, despite its reputation, simple in the cases you'll meet for the next two seasons. Treat it as a recipe today; the theory will keep until you actually need it.

---

## What you'll do

You'll wire a `PIDController` into `ElevatorSubsystem`. Each tick of `periodic()`, you'll ask the controller "given that I'm at `inputs.positionMeters` and want to be at `setpoint`, what voltage should I send?" — then you'll send that voltage. Then you'll tune `kP`, `kI`, `kD` until the simulated elevator reaches four setpoints (stow, low, mid, high) without overshoot. The widget below is your sandbox: drag the sliders, watch the response, build intuition before you touch Java.

<iframe class="lesson-widget"
        src="/examples/elevator-pid-poc/index.html"
        width="100%"
        height="720"
        title="Elevator PID — interactive PoC"></iframe>

---

## PID intuition, not PID theory

Three knobs. Each one answers a different question about how to react to error (error = `setpoint - currentPosition`).

- **`kP` — proportional.** *"How hard should I push, in proportion to how far off I am?"* Big error → big push. Small error → small push. This is the workhorse term; you'll do most of your tuning here.
- **`kI` — integral.** *"How long have I been a little bit off?"* If error doesn't shrink, the integral keeps building, and the controller pushes harder. Useful for fighting steady offsets (friction, a constant load). Easy to overdo — too much `kI` and the controller winds up, then overshoots when error finally clears.
- **`kD` — derivative.** *"How fast is the error changing?"* If the carriage is racing toward the setpoint, `kD` slams the brakes early. Damps oscillation. Sensitive to noise — too much `kD` and small sensor jitter becomes large motor jitter.

The standard tuning recipe for an elevator-shaped problem: start with everything at zero. Crank `kP` until the carriage reaches the setpoint quickly but rings (oscillates). Add `kD` until the ring damps. If the carriage settles a couple cm short of the setpoint and never closes the gap, add a sliver of `kI`. That's it. That recipe will get you through the entire Stage 1B/1C curriculum.

!!! tip "Play before you tune"

    The widget above has three sliders for `kP`, `kI`, `kD` and a step-response plot. Try `kP = 10, kI = 0, kD = 0` first — feel the oscillation. Add `kD` until it stops. Notice the carriage never *quite* gets to the setpoint — that's steady-state error, and a tiny `kI` fixes it.

---

## The voltage rule

WPILib motor APIs let you call either `motor.set(0.7)` (normalized throttle from −1 to +1) or `motor.setVoltage(8.4)` (actual volts). They look interchangeable. They are not.

A 12 V battery during a hard auto routine sags to 10.5 V. If you called `motor.set(0.7)`, the controller multiplies by *current* bus voltage: at 12 V you get 8.4 V, at 10.5 V you get 7.35 V. Same code, different physics. Your nicely-tuned `kP` is now wrong, mid-match.

`setVoltage` compensates internally. Your PID output is in volts. Volts are physics. **Tune in volts, send volts.** Every reference robot in this curriculum does this; Kelpie's `ElevatorSubsystem` is no exception.

---

## Starter code

```java
public class ElevatorSubsystem extends SubsystemBase {
  private final ElevatorIO io;
  private final ElevatorIOInputsAutoLogged inputs = new ElevatorIOInputsAutoLogged();
  private final PIDController pid = new PIDController(0.0, 0.0, 0.0);
  private double setpoint = 0.0;

  public void setSetpoint(double meters) { setpoint = meters; }

  @Override
  public void periodic() {
    // TODO (LESSON 05): updateInputs, calculate, setVoltage
  }
}
```

The `ElevatorIO` interface and `inputs` struct are already written — they expose `inputs.positionMeters` that you read each tick. Your job is to fill in three lines of `periodic()`.

---

## Rubric

`ElevatorTest` runs a 4-setpoint sweep (stow → low → mid → high) in WPILib's `ElevatorSim` and asserts:

1. Elevator reaches each setpoint within ±2 cm.
2. Settles within 1.5 s.
3. No overshoot greater than 5 cm.
4. `Lesson05/Pass` stays true through the entire sweep.

```bash
./gradlew test --tests '*ElevatorTest' -DincludeTags='lesson-05'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope, plot on the same chart:

- `Elevator/Inputs/positionMeters` — where the carriage actually is
- `Elevator/setpointMeters` — where you asked it to be

A well-tuned step response looks like a fast climb that decelerates smoothly and parks on the line. A bad tune is obvious: oscillation rings, overshoot lobes, or a permanent gap between the two traces. Drop a `Mechanism2d` viewer on the same tab to watch the carriage climb in 2D — sometimes the picture beats the plot.

---

## Going further

- Tune the same loop with a step input twice as large. Does your tune still settle in 1.5 s, or does saturation (the motor maxing out) blow your budget?
- Crank `kP` until the loop becomes unstable. Then halve it. That's a real tuning heuristic (Ziegler-Nichols, lite version).
- Read Kelpie's [`ElevatorIOSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/elevator/ElevatorIOSim.java). Notice their `0.06 V` gravity feedforward sitting alongside the PID. That `0.06` is constant — gravity on an elevator doesn't change with position. Tomorrow's lesson on the arm changes that.
- The browser PoC and the Java path solve the same problem with the same controller. Open them side-by-side; the muscle memory transfers.

---

??? tip "Full reveal — only open if you're really stuck"

    The minimal completion of `periodic()`:

    ```java
    @Override
    public void periodic() {
      io.updateInputs(inputs);
      Logger.processInputs("Elevator", inputs);
      double output = pid.calculate(inputs.positionMeters, setpoint);
      io.setVoltage(output);
    }
    ```

    With reasonable starting gains for the sim: `kP = 60, kI = 0, kD = 4`. Adjust from there — these will get you in the ballpark but not on the bullseye.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 04**
    Subsystems as state machines

    [:octicons-arrow-left-24: Back to lesson 04](../04-subsystems-state-machines/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 06**
    Arm with gravity feedforward

    [:octicons-arrow-right-24: Continue to lesson 06](../06-arm-gravity-ff/)

</div>
