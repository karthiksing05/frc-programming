# Lesson 06 — Arm with gravity feedforward <small>· Stage 1B</small>

<span class="stage-badge">Stage 1B · Lesson 06</span>

*Yesterday's elevator fought a constant downward pull. Today's arm fights a pull that depends on which way it's pointing — and pure PID can't keep up.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1B |
    | **Time** | ~45 min |
    | **Prereqs** | [Lesson 05 — PID introduction (Elevator)](../05-pid-elevator/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/shoulder/ShoulderSubsystem.java` |
    | **Tests** | `frc.robot.subsystems.shoulder.ShoulderTest` (`@Tag("lesson-06")`) |
    | **Reference robot** | Kelpie · [`shoulder/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/shoulder) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Compute angle-dependent gravity compensation with `kG * cos(angle)`.
2. Combine a PID output and a feedforward output into a single motor voltage.
3. Articulate the difference between *constant gravity* (elevator) and *angle-dependent gravity* (arm).
4. Recognize when "just crank `kP` higher" stops being the answer.

---

## The real-world problem

You finished Lesson 05 with an elevator that sat happily at any height. Gravity on the elevator was a constant — about 0.06 V of motor effort holds the carriage up at any position. You could fold that constant straight into `kI` or even cheat and add a small voltage offset by hand. Either way: one number, one mechanism, done.

Now point a 60-cm arm out horizontally. Gravity pulls it straight down — the motor sees a giant torque load. Now rotate the arm straight up. Gravity points down the shaft of the arm, contributing zero torque to the motor. Same arm, same battery, but the load just dropped from "huge" to "nothing." Your Lesson 05 tune held the elevator perfectly — apply it here and the arm sags 10° at horizontal and overshoots straight up.

If you tried to fix this with PID alone, you'd see exactly the rookie response: crank `kP` to fight horizontal sag, then watch the arm slam into hard stops at the top. Integral wind-up makes it worse — `kI` learns "I need a lot of effort," then keeps pushing after the load disappears. The cure is to *tell* the controller what gravity is doing, instead of making it learn the same lesson over and over.

---

## What you'll do

You'll add one constant — `kG`, in volts — and one line of trigonometry. In `periodic()`, you'll compute `ffVolts = kG * Math.cos(currentAngleRadians)`, then add it to the PID output before sending the sum to the motor. Then you'll tune `kG` until the arm hovers motionless at 0° (horizontal) with `kP = 0`. That's the test: the feedforward alone should balance gravity. PID's only job is to clean up what FF doesn't get exactly right.

---

## Why `cos(angle)`

A pivoting arm is a lever. The torque gravity applies about the pivot is `m * g * L * cos(θ)` where θ is measured from horizontal. At θ = 0° (horizontal), `cos(0) = 1` — torque is maximum. At θ = 90° (straight up or straight down), `cos(90°) = 0` — gravity pulls along the arm's axis and contributes zero torque to the joint. In between, it scales smoothly.

You don't need to compute mass, length, or `g` separately. You roll all three constants into a single `kG` that you measure empirically: the voltage required to hold the arm motionless at horizontal. Then for any other angle, gravity's voltage demand is `kG * cos(angle)`. Same shape, different magnitude.

!!! example "Two angles, two voltages"

    Say `kG = 0.45 V` (you measured this with the arm horizontal and no PID).

    - Horizontal (θ = 0°): `ffVolts = 0.45 * cos(0)  = 0.45 V`
    - 45° above horizontal: `ffVolts = 0.45 * cos(45°) ≈ 0.318 V`
    - Straight up (θ = 90°): `ffVolts = 0.45 * cos(90°) = 0 V`

    The controller barely has to do anything — feedforward is doing the heavy lifting, PID just nudges out the last few degrees of error.

---

## How Kelpie does it

Kelpie's [`shoulder/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/shoulder) pivots Team 8033's entire end effector — wrist, roller, the works — through a wide arc. Their feedforward is exactly the `kG * cos(θ)` pattern you're about to write, packaged inside WPILib's `ArmFeedforward` class. The `ArmFeedforward` constructor also takes `kS` (static friction) and `kV` (velocity), but for today's lesson you'll use the simplest possible form — just `kG`. You'll meet `kS` and `kV` again in Lesson 27 when motion profiles arrive.

!!! quote "The `ArmFeedforward` contract"

    `new ArmFeedforward(kS, kG, kV).calculate(positionRadians, velocityRadiansPerSec)` returns the voltage required to *move at* `velocityRadiansPerSec` *while at* `positionRadians`. For a static hold (zero velocity), the answer collapses to `kS * sign(v) + kG * cos(position)`. With `kS = 0`, it's pure gravity compensation.

---

## Starter code

```java
public class ShoulderSubsystem extends SubsystemBase {
  private final ShoulderIO io;
  private final ShoulderIOInputsAutoLogged inputs = new ShoulderIOInputsAutoLogged();
  private final PIDController pid = new PIDController(0.0, 0.0, 0.0);
  private final ArmFeedforward ff = new ArmFeedforward(0.0, Constants.Shoulder.kG, 0.0);
  private double setpointRad = 0.0;

  public void setAngle(double radians) { setpointRad = radians; }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Shoulder", inputs);
    // TODO (LESSON 06): combine pid.calculate(...) + ff.calculate(...)
  }
}
```

Two reminders from Lesson 05:

- Still **volts**, not throttle. The FF term is in volts; the PID output is in volts; the sum is in volts; `io.setVoltage(...)` takes volts.
- `inputs.positionRadians` is the angle measured from horizontal. If your encoder zero is somewhere else (straight up, straight down, the home position), you must offset it. *Get this wrong and the FF term will push the wrong direction — the arm will accelerate into gravity instead of fighting it.*

!!! warning "Sign and zero matter"

    The `cos` trick assumes θ = 0 means horizontal. If your encoder zero is at the stowed (down) position, you need `Math.cos(inputs.positionRadians - HORIZONTAL_OFFSET)`. Test this with the simulator at very low gains — the arm should hang, not climb.

---

## Rubric

`ShoulderTest` exercises the arm with `SingleJointedArmSim` and asserts:

1. Arm holds horizontal under gravity within ±2°, with `kP = 0`. (Proves FF is doing real work.)
2. With FF wired correctly, the arm reaches three setpoints (down, mid, up) within tolerance and 2 s.
3. With `kG` set to zero and PID gains restored, the rubric *fails*. (Proves the failure mode the lesson preempts.)

```bash
./gradlew test --tests '*ShoulderTest' -DincludeTags='lesson-06'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope, open the `Mechanism2d` view and plot a `MechanismLigament2d` rooted at the pivot — you'll see the arm swing through its setpoints. On a separate chart, overlay:

- `Shoulder/Inputs/positionRadians`
- `Shoulder/setpointRadians`
- `Shoulder/ffVolts`
- `Shoulder/pidVolts`

You should see `ffVolts` trace a smooth cosine curve as the arm sweeps; `pidVolts` should stay small. If `pidVolts` is doing all the work and `ffVolts` is flat, your encoder zero is wrong.

---

## Going further

- Try a quick "FF only" experiment: set `kP = kI = kD = 0` and command a small velocity. Does the arm coast cleanly, or stall? That tells you whether you also need `kV`.
- Read the [`SingleJointedArmSim` Javadoc](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj/simulation/SingleJointedArmSim.html). Notice it takes the same constants you're tuning — mass, length, gear ratio — to compute a physically accurate gravity model. Your `kG` is folding all of that into one empirical number.
- Compare to Kelpie's [`shoulder/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/shoulder) and notice it uses `ArmFeedforward` rather than hand-rolling `kG * cos(θ)`. Both are correct; the class is just a tidier package.
- Stretch: their [`wrist/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/wrist) sits at the end of the shoulder. The wrist's gravity depends on the *shoulder's* angle plus its own. That's a second-order arm — a worthy puzzle once you've nailed this one.

---

??? tip "Full reveal — only open if you're really stuck"

    The minimal completion of `periodic()`:

    ```java
    @Override
    public void periodic() {
      io.updateInputs(inputs);
      Logger.processInputs("Shoulder", inputs);
      double pidVolts = pid.calculate(inputs.positionRadians, setpointRad);
      double ffVolts  = ff.calculate(inputs.positionRadians, 0.0);
      io.setVoltage(pidVolts + ffVolts);
      Logger.recordOutput("Shoulder/pidVolts", pidVolts);
      Logger.recordOutput("Shoulder/ffVolts", ffVolts);
    }
    ```

    A reasonable starting `kG` for the sim is somewhere around 0.4–0.5 V. Tune it against rubric assertion #1 first (hold horizontal, `kP = 0`) — once FF is right, PID has almost nothing to do.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 05**
    PID introduction — Elevator

    [:octicons-arrow-left-24: Back to lesson 05](../05-pid-elevator/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 07**
    Tank drive wiring (factory pattern)

    [:octicons-arrow-right-24: Continue to lesson 07](../../stage1c/07-tank-drive/)

</div>
